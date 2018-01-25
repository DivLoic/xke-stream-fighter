package fr.xebia.ldi.fighter.stream.queries;

import fr.xebia.ldi.fighter.schema.VictoriesCount;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.generateWindowKeys;

/**
 * Created by loicmdivad.
 */
public class QueryTask extends TimerTask {

    String outpath;
    ReadOnlyWindowStore<GenericRecord, Long> windowStore;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long since = now - TimeUnit.SECONDS.toMillis(15);

        String[] lines = generateWindowKeys("PRO")
                .map((GenericRecord key) -> querying(key, since, now, windowStore))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(VictoriesCount::getVictories).reversed())
                .map(this::writeTable).toArray(String[]::new);

        if(lines.length != 0){
            printHeadAndFooter(this.outpath, false);

            try {
                Files.write(
                        Paths.get(this.outpath),
                        Arrays.asList(lines),
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            printHeadAndFooter(this.outpath, true);
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
    private VictoriesCount querying(GenericRecord key, Long since, Long now, ReadOnlyWindowStore<GenericRecord, Long> windowStore){
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

    /**
     * Format a line of the table from a VictoriesCount
     * @param count VictoriesCount value extracted from a window store
     * @return One line of the table
     */
    private String writeTable(VictoriesCount count){
        return "| " + Stream.of(
                count.getConcept(),
                new DateTime(count.getWindowStart()).toString("HH:mm:ss"),
                count.getCharacter(),
                count.getVictories().toString()
        )
                .map(Object::toString)
                .map((String cell) -> String.format("%1$-" + 12 + "s", cell))
                .collect((Collectors.joining("| "))) + "| ";
    }


    private static String header(){
        return " -------------+-------------+-------------+------------- \n" +
                "| " + Stream.of("concept", "window", "characters", "victories")
                .map((String cell) -> String.format("%1$-" + 12 + "s", cell) + "| ")
                .collect((Collectors.joining(""))) + "\n" +
                " -------------+-------------+-------------+------------- \n";
    }

    private static String footer(){
        return " -------------+-------------+-------------+------------- \n";
    }


    private void printHeadAndFooter(String filePath, Boolean footer){
        File file = new File(filePath);
        String lines = footer ? footer() : header();
        try {
            FileWriter writer = new FileWriter(file, footer);
            writer.write(lines);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
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
