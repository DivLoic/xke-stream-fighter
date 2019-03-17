package fr.xebia.ldi.fighter.stream.queries;

import fr.xebia.ldi.fighter.schema.VictoriesCount;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.generateWindowKeys;

/**
 * Created by loicmdivad.
 */
public class QueryTask extends TimerTask {

    private String outpath;
    private ReadOnlyWindowStore<GenericRecord, Long> windowStore;
    private Logger logger = LoggerFactory.getLogger(QueryTask.class);

    public QueryTask(ReadOnlyWindowStore<GenericRecord, Long> windowStore, String outpath) {
        this.setOutpath(outpath);
        this.setWindowStore(windowStore);
    }

    private void setOutpath(String outpath) {
        this.outpath = outpath;
    }

    private void setWindowStore(ReadOnlyWindowStore<GenericRecord, Long> windowStore) {
        this.windowStore = windowStore;
    }

    public static Long computeWindowStart(long timestamp, long windowSize) {
        return (Math.max(0, timestamp - windowSize + windowSize) / windowSize) * windowSize;
    }

    @Override
    public void run() {
        Instant now = LocalDateTime.now(ZoneId.systemDefault()).toInstant(ZoneOffset.UTC);
        Instant windowstart = now.minus(TimeUnit.SECONDS.toSeconds(15), ChronoUnit.SECONDS);

        String[] lines = generateWindowKeys("PRO")
                .map((GenericRecord key) -> querying(key, windowstart, now, windowStore))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(VictoriesCount::getVictories).reversed())
                .map(QueryUtil::line).toArray(String[]::new);

        if(lines.length != 0){
            printLayout(this.outpath, QueryUtil::header);

            try {
                Files.write(
                        Paths.get(this.outpath),
                        Arrays.asList(lines),
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            printLayout(this.outpath, QueryUtil::footer);

        } else {
            logger.debug("No lines to write were found");
        }
    }

    /**
     * Get a count number from a kafka stream store and return a typed VictoriesCount
     * @param key {"concept": ..., "character": ...}
     * @param since start of the first window in Long
     * @param now end of the last window in Long
     * @param windowStore a ReadOnlyWindowStore of type (GenericRecord, Long)
     * @return instance of VictoriesCount (one character, one concept, one window) ... or null
     */
    private VictoriesCount querying(GenericRecord key, Instant since, Instant now, ReadOnlyWindowStore<GenericRecord, Long> windowStore){
        WindowStoreIterator<Long> iterator = windowStore.fetch(key, since, now);
        VictoriesCount victoriesCount = null;

        while(iterator.hasNext()){
            KeyValue<Long, Long> count = iterator.next();
            victoriesCount = new VictoriesCount(
                    count.key,
                    key.get("character").toString(),
                    key.get("concept").toString(),
                    count.value
            );
            iterator.close();
        }
        return victoriesCount;
    }

    private void printLayout(String filePath, Callable<String> layout){
        File file = new File(filePath);
        FileWriter writer = null;
        try {
            String lines = layout.call();
            writer = new FileWriter(file, lines.length() < 60);
            writer.write(lines);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Wait until the streaming jobs is in state RUNNING.
     * Recommended by the confluent doc: <a href="https://docs.confluent.io/current/streams/faq.html#handling-invalidstatestoreexception-the-state-store-may-have-migrated-to-another-instance">https://docs.confluent.io/current/streams/faq</a>
     * @param storeName name of window store
     * @param streams an instance of KafkaStreams
     * @return a ReadOnlyWindowStore(GenericRecord, Long)
     * @throws InterruptedException from a while/true that may be killed
     */
    public static final ReadOnlyWindowStore<GenericRecord, Long> waitUntilStoreIsQueryable(
            final String storeName,
            final KafkaStreams streams) throws InterruptedException {

        int attempt = 0;
        Logger logger = LoggerFactory.getLogger(QueryTask.class);

        while (true) {
            try {
                return streams.store(storeName, QueryableStoreTypes.windowStore());
            } catch (InvalidStateStoreException ignored) {
                // store not yet ready for querying
                attempt = attempt + 1;
                if(attempt % 10 == 0) {
                    logger.info(String.format("Trying to query the state store for the %s time.", attempt));
                }
                Thread.sleep(100);
            }
        }
    }
}
