package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.apache.kafka.streams.state.internals.KeyValueStoreBuilder;
import org.apache.kafka.streams.state.internals.MeteredWindowStore;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;

import java.lang.management.OperatingSystemMXBean;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.groupedDataKey;

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

        GenericRecord groupKey = groupedDataKey((Victory) value);

        WindowStoreIterator<Long> it = this.victoryStore.fetch(groupKey, fifteenSecAgo(), now());

        if(it.hasNext()){
            KeyValue<Long, Long> keyValue = it.next();
            Long total = keyValue.value + 1L;
            this.victoryStore.put(groupKey, total, computeWindowStart(keyValue.key));
        } else {
            this.victoryStore.put(groupKey, 1L, computeWindowStart(DateTime.now().getMillis()));
        }

    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }

    private static Long fifteenSecAgo() {
        return new DateTime().minus(TimeUnit.SECONDS.toMillis(15)).getMillis();
    }

    private static Long now() {
        return DateTime.now().getMillis();
    }

    private static Long computeWindowStart(Long timestamp) {
        DateTime datetime = new DateTime(timestamp);
        int start = datetime.get(DateTimeFieldType.secondOfMinute()) / 15;
        DateTime windowStart = datetime.secondOfMinute().setCopy(start * 15);
        return windowStart.getMillis();
    }


}
