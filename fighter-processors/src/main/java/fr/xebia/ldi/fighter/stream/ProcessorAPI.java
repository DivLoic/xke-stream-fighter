package fr.xebia.ldi.fighter.stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.xebia.ldi.fighter.schema.*;
import fr.xebia.ldi.fighter.stream.processor.ProcessArena;
import fr.xebia.ldi.fighter.stream.processor.ProcessPlayer;
import fr.xebia.ldi.fighter.stream.processor.ProcessRound;
import fr.xebia.ldi.fighter.stream.processor.ProcessVictory;
import fr.xebia.ldi.fighter.stream.queries.Query;
import fr.xebia.ldi.fighter.stream.utils.EventTimeExtractor;
import fr.xebia.ldi.fighter.stream.utils.JobConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

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

        SpecificAvroSerde<VictoriesCount> victoryCountSerde = new SpecificAvroSerde<>();
        victoryCountSerde.configure(props, false);

        Topology builder = new Topology();

        // TODO 2 -> D: create the arenaStoreBuilder as ARENA-STORE
        //StoreBuilder<KeyValueStore<String, Arena>> arenaStoreBuilder =

        // TODO 4 -> E: create the victoriesStoreBuilder as VICTORIES-STORE
        //StoreBuilder<WindowStore<GenericRecord, Long>> victoriesStoreBuilder =

        builder
                // TODO 1 -> A: add the ARENAS input topic as ARENAS
                //.add

                // TODO 1 -> B: add the ROUNDS input topic as ROUNDS
                //.add

                // TODO 1 -> C: add the ProcessRound as PROCESS-ROUND after ROUNDS
                //.add

                // TODO 2 -> A: add the ProcessArena as PROCESS-ARENA after ARENAS
                //.add

                // TODO 3 -> A: add the ProcessPlayer as PROCESS-PLAYER after PROCESS-ROUND
                //.add

                // TODO 4 -> A: add the ProcessVictory as PROCESS-VICTORY after REPARTITIONED
                //.add

                // TODO 2 -> E: grant access to arenaStoreBuilder for PROCESS-ARENA
                // TODO 3 -> E: grant access to arenaStoreBuilder for PROCESS-PLAYER

                // TODO 4 -> F: add the victoriesStoreBuilder to the PROCESS-VICTORY processor
                //.add

                // TODO: update the final sink
                .addSink("SINK", "RESULTS-PROC", Serdes.String().serializer(), roundSerde.serializer(), "PROCESS-ROUND");

        KafkaStreams kafkaStreams = new KafkaStreams(builder, JobConfig.properties(config));

        kafkaStreams.cleanUp();

        kafkaStreams.start();
/*

        Query.start(kafkaStreams, "VICTORIES-STORE", config);

*/
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    kafkaStreams.close();
                    kafkaStreams.cleanUp();
                })
        );
    }
}
