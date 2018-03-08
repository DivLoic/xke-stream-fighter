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

        long now = this.context.timestamp();

        long windowStart = computeWindowStart(now, TimeUnit.SECONDS.toMillis(15));

        WindowStoreIterator<Long> it = this.victoryStore.fetch(key, windowStart, now);

        if(it.hasNext()){
            KeyValue<Long, Long> keyValue = it.next();
            total = keyValue.value + 1L;
        } else {
            total = 1L;
        }

        this.victoryStore.put(key, total, windowStart);

        KeyValue<GenericRecord, GenericRecord> kvDisplay = parseWindowKey(windowStart, key, total);

        context.forward(kvDisplay.key, kvDisplay.value);
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}
