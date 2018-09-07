package fr.xebia.ldi.fighter.stream.queries;

import com.typesafe.config.Config;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * Created by loicmdivad.
 */
public class Query {

    private static Logger logger = LoggerFactory.getLogger(Query.class);

    public static void start(KafkaStreams kafkaStreams, String store, Config config) {
        try {
            Timer timer = new Timer();

            ReadOnlyWindowStore<GenericRecord, Long> window = QueryTask.waitUntilStoreIsQueryable(store, kafkaStreams);

            logger.info("Successfully create a read only window store: " + window);

            timer.schedule(new QueryTask(window, config.getString("tmp.output-table")), 1000, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
