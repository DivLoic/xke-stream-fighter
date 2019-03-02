package fr.xebia.ldi.fighter.stream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Round;
import fr.xebia.ldi.fighter.stream.processor.ProcessToken;
import fr.xebia.ldi.fighter.stream.utils.EventTimeExtractor;
import fr.xebia.ldi.fighter.stream.utils.JobConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Map;

import static fr.xebia.ldi.fighter.entity.GameEntity.StreetFighter;
import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

/**
 * Created by loicmdivad.
 */
public class TokenProvider {

    public static void main(String[] args) {

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

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Round> rounds = builder
                .stream("ROUNDS", Consumed.with(Serdes.String(), roundSerde, new EventTimeExtractor(), LATEST));

        rounds

                .filter((arenaId, round) -> round.getGame() == StreetFighter)

                .filter((arenaId, round) -> round.getWinner().getCombo() >= 5)

                .filter((arenaId, round) -> round.getWinner().getLife() >= 75)

                .through("ONE-PARTITION-WINNER-TOPIC")

                .transform(ProcessToken::new, "TOKEN-STORE")

                .to("TOKEN-PROVIDED");
    }
}
