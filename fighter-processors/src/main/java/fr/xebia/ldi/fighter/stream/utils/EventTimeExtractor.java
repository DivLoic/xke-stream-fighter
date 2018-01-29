package fr.xebia.ldi.fighter.stream.utils;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * Created by loicmdivad.
 */
public class EventTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
        try {
            GenericRecord value = (GenericRecord) record.value();
            return (long) value.get("timestamp");
        } catch (Exception e) {
            return record.timestamp();
        }
    }
}
