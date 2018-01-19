package fr.xebia.ldi.fighter.stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Round;
import fr.xebia.ldi.fighter.schema.Victory;
import fr.xebia.ldi.fighter.stream.processor.ProcessArena;
import fr.xebia.ldi.fighter.stream.processor.ProcessPlayer;
import fr.xebia.ldi.fighter.stream.processor.ProcessRound;
import fr.xebia.ldi.fighter.stream.processor.ProcessVictory;
import fr.xebia.ldi.fighter.stream.queries.QueryTask;
import fr.xebia.ldi.fighter.stream.utils.JobConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.*;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by loicmdivad.
 */
public class ProcessorAPI {

    public static void main(String[] args){

        Config config = ConfigFactory.load();
        Map<String, String> props = JobConfig.mapProperties(config);

        GenericAvroSerde avroSerde = new GenericAvroSerde();
        avroSerde.configure(props, true);

        SpecificAvroSerde<Round> roundSerde = new SpecificAvroSerde<>();
        roundSerde.configure(props, false);

        SpecificAvroSerde<Arena> arenaSerde = new SpecificAvroSerde<>();
        arenaSerde.configure(props, false);

        SpecificAvroSerde<Player> playerSerde = new SpecificAvroSerde<>();
        playerSerde.configure(props, false);

        SpecificAvroSerde<Victory> victorySerde = new SpecificAvroSerde<>();
        victorySerde.configure(props, false);

        Topology builder = new Topology();

        StoreBuilder<KeyValueStore<String, Arena>> arenaStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("ARENA-STORE"),
                Serdes.String(),
                arenaSerde
        );

        StoreBuilder<WindowStore<GenericRecord, Long>> victoriesStoreBuilder = Stores.windowStoreBuilder(
                Stores.persistentWindowStore("VICTORIES-STORE", TimeUnit.SECONDS.toMillis(15), 2, 1, false),
                avroSerde,
                Serdes.Long()
        );

        builder
                .addSource("ARENAS", Serdes.String().deserializer(), arenaSerde.deserializer(), "ARENAS")

                .addSource("ROUNDS", Serdes.String().deserializer(), roundSerde.deserializer(), "ROUNDS")

                .addProcessor("ARENA-ROUND", ProcessArena::new, "ARENAS")

                .addProcessor("PROCESS-ROUND", ProcessRound::new, "ROUNDS")

                .addProcessor("PROCESS-PLAYER", ProcessPlayer::new, "PROCESS-ROUND")

                .addProcessor("PROCESS-VICTORY", ProcessVictory::new, "PROCESS-PLAYER")

                .addStateStore(arenaStoreBuilder, "ARENA-ROUND", "PROCESS-PLAYER")

                .addStateStore(victoriesStoreBuilder, "PROCESS-VICTORY")

                .addSink("SINK", "RESULTS", Serdes.String().serializer(), victorySerde.serializer(), "PROCESS-VICTORY");

        KafkaStreams kafkaStream = new KafkaStreams(builder, JobConfig.properties(config));

        kafkaStream.start();

        ReadOnlyWindowStore<GenericRecord, Long> windowStore;

        try {
            Timer timer = new Timer();

            windowStore = QueryTask.waitUntilStoreIsQueryable("VICTORIES-STORE", kafkaStream);

            timer.schedule(new QueryTask(windowStore, config.getString("tmp.output-table")), 0, 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
