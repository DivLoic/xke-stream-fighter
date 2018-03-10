package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Arena;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Created by loicmdivad.
 */
public class ProcessGlobal implements Processor<String, Arena> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> store;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.store = (KeyValueStore) this.context.getStateStore("ARENA-STORE");
    }

    @Override
    public void process(String key, Arena value) {
        store.putIfAbsent(key, value);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}

