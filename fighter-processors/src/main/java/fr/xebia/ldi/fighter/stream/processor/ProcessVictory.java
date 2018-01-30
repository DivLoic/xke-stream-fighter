package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.util.concurrent.TimeUnit;

import static fr.xebia.ldi.fighter.stream.queries.QueryTask.computeWindowStart;
import static fr.xebia.ldi.fighter.stream.utils.Parsing.groupedDataKey;
import static fr.xebia.ldi.fighter.stream.utils.Parsing.parseWindowKey;

/**
 * Created by loicmdivad.
 */
public class ProcessVictory implements Processor {

    private ProcessorContext context;
    private WindowStore<GenericRecord, Long> victoryStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.victoryStore = (WindowStore) this.context.getStateStore("VICTORIES-STORE");
    }

    @Override
    public void process(Object key, Object value) {
        this.context.timestamp();

        long now = this.context.timestamp();
        //long since = now - TimeUnit.SECONDS.toMillis(15);
        //long windowStart = computeWindowStart(now);
        long windowStart = computeWindowStart(now, TimeUnit.SECONDS.toMillis(15));

        GenericRecord groupKey = groupedDataKey((Victory) value);

        WindowStoreIterator<Long> it = this.victoryStore.fetch(groupKey, windowStart, now);

        if(it.hasNext()){
            KeyValue<Long, Long> keyValue = it.next();
            long total = keyValue.value + 1L;
            this.victoryStore.put(groupKey, total, windowStart);
            KeyValue<GenericRecord, GenericRecord> kvDisplay = parseWindowKey(windowStart, groupKey, total);
            context.forward(kvDisplay.key, kvDisplay.value);
        } else {
            this.victoryStore.put(groupKey, 1L, windowStart);
            KeyValue<GenericRecord, GenericRecord> kvDisplay = parseWindowKey(windowStart, groupKey, 1);
            context.forward(kvDisplay.key, kvDisplay.value);
        }

    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}
