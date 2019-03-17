package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Arena;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.To;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;


/**
 * Created by loicmdivad.
 */
public class ProcessArena implements Processor<String, Arena> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> arenaStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;

        // TODO 2 -> B: get the KeyValueStore as ARENA-STORE

        // TODO 2 -> F: send a list of point of sales every 10min (WALL_CLOCK_TIME)
    }

    @Override
    public void process(String key, Arena value) {
        // TODO 2 -> C: put each message in the store
    }

    @Override
    public void close() {

    }
}
