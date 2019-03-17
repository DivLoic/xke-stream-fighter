package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import java.util.concurrent.TimeUnit;

import static fr.xebia.ldi.fighter.stream.queries.QueryTask.computeWindowStart;
import static fr.xebia.ldi.fighter.stream.utils.Parsing.parseWindowKey;

/**
 * Created by loicmdivad.
 */
public class ProcessVictory implements Processor<GenericRecord, Victory> {

    private ProcessorContext context;
    private WindowStore<GenericRecord, Long> victoryStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.victoryStore = (WindowStore) this.context.getStateStore("VICTORIES-STORE");
    }

    @Override
    public void process(GenericRecord key, Victory value) {
        long total;

        // TODO 4 -> B: find the now timestamp
        long now = this.context.timestamp();

        long windowStart = computeWindowStart(now, TimeUnit.SECONDS.toMillis(15));
        // 11:51:48 ---> 11:51:45

        // TODO 4 -> C: fetch the window from the victoryStore
        WindowStoreIterator<Long> it = null; // ???

        if(it.hasNext()){
            KeyValue<Long, Long> keyValue = it.next();
            total = keyValue.value + 1L;
        } else {
            total = 1L;
        }

        // TODO 4 -> D: put the new count into the victoryStore store
        //this.victoryStore.put(???, ???, ???)


        // ~*~ presentation purpose only ~*~
        KeyValue<GenericRecord, GenericRecord> kvDisplay = parseWindowKey(windowStart, key, total);

        this.context.forward(kvDisplay.key, kvDisplay.value);
    }

    @Override
    public void close() {

    }
}
