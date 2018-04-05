package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.groupedDataKey;

/**
 * Created by loicmdivad.
 */
public class ProcessPlayer implements Processor<String, Player> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> arenaStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;

        this.arenaStore = (KeyValueStore) context.getStateStore("ARENA-STORE");
    }

    @Override
    public void process(String key, Player value) {

        Optional<Arena> mayBeArena = Optional.ofNullable(this.arenaStore.get(key));

        mayBeArena.ifPresent(arena -> {

                    Victory victory = new Victory(value, arena);

                    GenericRecord victoryKey = groupedDataKey(victory);

                    context.forward(victoryKey, victory);
                }
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}
