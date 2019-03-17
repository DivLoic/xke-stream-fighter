package fr.xebia.ldi.fighter.stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Round;
import fr.xebia.ldi.fighter.schema.Victory;
import fr.xebia.ldi.fighter.stream.queries.Query;
import fr.xebia.ldi.fighter.stream.utils.EventTimeExtractor;
import fr.xebia.ldi.fighter.stream.utils.JobConfig;
import fr.xebia.ldi.fighter.stream.utils.Parsing;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static fr.xebia.ldi.fighter.entity.GameEntity.StreetFighter;
import static fr.xebia.ldi.fighter.stream.utils.JobScheduling.delayProcessing;
import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

/**
 * Created by loicmdivad.
 */
public class StreamsDsl {

    private static Logger logger = LoggerFactory.getLogger(ProcessorAPI.class);

    public static void main(String[] args){

        Config config = ConfigFactory.load();
        Map<String, Object> props = JobConfig.avroProperties(config);

        GenericAvroSerde avroSerde = new GenericAvroSerde();
        avroSerde.configure(props, true);

        SpecificAvroSerde<Round> roundSerde = new SpecificAvroSerde<>();
        roundSerde.configure(props, false);

        SpecificAvroSerde<Arena> arenaSerde = new SpecificAvroSerde<>();
        arenaSerde.configure(props, false);

        SpecificAvroSerde<Player> playerSerde = new SpecificAvroSerde<>();
        playerSerde.configure(props, false);

        TimeWindows window = TimeWindows.of(Duration.ofSeconds(15));

        Materialized<GenericRecord, Long, WindowStore<Bytes, byte[]>> mat = Materialized.as("VICTORIES-STORE");

        StreamsBuilder builder = new StreamsBuilder();

        GlobalKTable<String, Arena> arenaTable = builder
                .globalTable("ARENAS", Consumed.with(Serdes.String(), arenaSerde));

        KStream<String, Round> rounds = builder
                .stream("ROUNDS", Consumed.with(Serdes.String(), roundSerde, new EventTimeExtractor(), LATEST));

        rounds

                .filter((String arenaId, Round round) -> round.getGame() == StreetFighter)

                .mapValues(Round::getWinner)

                .join(arenaTable, (arenaId, player) -> arenaId, Victory::new)

                .selectKey(Parsing::extractConceptAndCharacter)

                .groupByKey().windowedBy(window).count(mat)

                // PRESENTATION PURPOSE ONLY
                .toStream().map(Parsing::parseWindowKey).to("RESULTS-DSL");

        delayProcessing(config.getLong("start-lag"));

        Topology topology = builder.build();

        Arrays.asList(

                topology.describe().toString().split("\n")

        ).forEach( node -> logger.info(node));

        KafkaStreams kafkaStreams = new KafkaStreams(topology, JobConfig.properties(config));

        kafkaStreams.cleanUp();

        kafkaStreams.start();

        Query.start(kafkaStreams, "VICTORIES-STORE", config);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    kafkaStreams.close();
                    kafkaStreams.cleanUp();
                })
        );
    }
}
