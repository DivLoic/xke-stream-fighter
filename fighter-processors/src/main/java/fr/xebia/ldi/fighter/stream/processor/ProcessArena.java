package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Arena;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;


/**
 * Created by loicmdivad.
 */
public class ProcessArena implements Processor<String, Arena> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> store;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;

        this.store = (KeyValueStore) this.context.getStateStore("ARENA-STORE");

        this.context.schedule(3000, PunctuationType.WALL_CLOCK_TIME, (timestamp) ->
            this.store.all().forEachRemaining((arenaKeyValue) ->
                    this.context.forward(
                            String.format("(%tc)  %-15S :", timestamp, arenaKeyValue.value.getName()),
                            arenaKeyValue.value.getTerminals().toString(),
                            "TERMINALS-COUNT"
                    )
            )
        );
    }

    @Override
    public void process(String key, Arena value) {
        store.put(key, value);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}
