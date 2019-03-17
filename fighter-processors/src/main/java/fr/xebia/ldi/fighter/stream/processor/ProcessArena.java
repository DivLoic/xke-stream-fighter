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

        this.arenaStore = (KeyValueStore) this.context.getStateStore("ARENA-STORE");

        this.context.schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            KeyValueIterator<String, Arena> it = this.arenaStore.all();

            it.forEachRemaining(kv ->
                    this.context.forward(
                            String.format("(%tc)  %-15S :", timestamp, kv.value.getName()),
                            kv.value.getTerminals().toString(),
                            To.child("TERMINALS-COUNT")
                    )
            );

            try {
                it.close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

        });
    }

    @Override
    public void process(String key, Arena value) {
        this.arenaStore.put(key, value);
    }

    @Override
    public void close() {

    }
}
