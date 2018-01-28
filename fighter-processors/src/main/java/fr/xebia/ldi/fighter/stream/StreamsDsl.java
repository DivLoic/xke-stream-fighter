package fr.xebia.ldi.fighter.stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Round;
import fr.xebia.ldi.fighter.schema.Victory;
import fr.xebia.ldi.fighter.stream.queries.Query;
import fr.xebia.ldi.fighter.stream.utils.JobConfig;
import fr.xebia.ldi.fighter.stream.utils.Parsing;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static fr.xebia.ldi.fighter.entity.GameEntity.StreetFighter;
import static fr.xebia.ldi.fighter.stream.utils.Parsing.extractArenaId;
import static fr.xebia.ldi.fighter.stream.utils.Parsing.groupedDataKey;

/**
 * Created by loicmdivad.
 */
public class StreamsDsl {

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

        TimeWindows window = TimeWindows.of(TimeUnit.SECONDS.toMillis(15));

        Materialized<GenericRecord, Long, WindowStore<Bytes, byte[]>> mat = Materialized.as("VICTORIES-STORE");

        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, Arena> arenaTable = builder
                .table("ARENAS", Consumed.with(Serdes.String(), arenaSerde));

        KStream<String, Round> rounds = builder
                .stream("ROUNDS", Consumed.with(Serdes.String(), roundSerde));

        rounds

                .filter((String key, Round round) -> round.getGame() == StreetFighter)

                .map((String key, Round round) -> new KeyValue<>(extractArenaId(key), round.getWinner()))

                .join(arenaTable, Victory::new, Joined.with(Serdes.String(), playerSerde, arenaSerde))

                .map((String key, Victory value) -> new KeyValue<>(groupedDataKey(value), value))

                .groupByKey().windowedBy(window).count(mat)

                // PRESENTATION PURPOSE ONLY
                .toStream().map(Parsing::parseWindowKey).to("RESULTS-DSL");

        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), JobConfig.properties(config));

        kafkaStreams.cleanUp();

        kafkaStreams.start();

        Query.start(kafkaStreams, "VICTORIES-STORE", config);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    kafkaStreams.close();
                    kafkaStreams.cleanUp();
                })
        );

        //System.out.println(kafkaStreams.localThreadsMetadata());
    }

}
