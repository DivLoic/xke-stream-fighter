package fr.xebia.ldi.fighter.stream.queries;

import com.typesafe.config.Config;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;

import java.util.Timer;

/**
 * Created by loicmdivad.
 */
public class Query {

    public static void start(KafkaStreams kafkaStreams, String store, Config config) {
        try {
            Timer timer = new Timer();

            ReadOnlyWindowStore<GenericRecord, Long> window = QueryTask.waitUntilStoreIsQueryable(store, kafkaStreams);

            timer.schedule(new QueryTask(window, config.getString("tmp.output-table")), 1000, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
