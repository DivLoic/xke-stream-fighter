package fr.xebia.ldi.fighter.stream.utils;

import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by loicmdivad.
 */
public class Parsing {

    private static final String[] characters = {
            "Ken", "Ryu", "Geki", "Chun-Li", "Akuma", "Sakura", "Dhalsim", "Blair",
            "Jin", "Asuka", "Emilie", "Kazuya", // takken queries
            "Mai", "Ramon", "Nelson", "Vanessa", // kof queries
            "Kilik", "Ivy", "Siegfried", "Nightmare" // soul queries
    };

    private static final Schema characterConceptKey = SchemaBuilder.builder()
            .record("CharacterConceptKey")
            .namespace("fr.xebia.ldi")
            .fields()
            .requiredString("character")
            .requiredString("concept")
            .endRecord();


    private static final Schema aggValueSchema = SchemaBuilder.builder()
            .record("VictoriesCountValue")
            .namespace("fr.xebia.ldi")
            .fields()
            .requiredString("dt")
            .requiredString("concept")
            .requiredString("character")
            .requiredLong("victories")
            .endRecord();

    public static Stream<GenericRecord> generateWindowKeys(String concept){
        return Arrays.stream(characters)
                .map((String character) -> generateWindowKey(character, concept));
    }

    private static GenericRecord generateWindowKey(String character, String concept) {
        GenericRecord record = new GenericData.Record(characterConceptKey);
        record.put("character", character);
        record.put("concept", concept);
        return record;
    }

    public static GenericRecord groupedDataKey(Victory v) {
        GenericRecord record = new GenericData.Record(characterConceptKey);
        record.put("character", v.getCharacter().getName());
        record.put("concept", v.getArena().getType());
        return record;
    }

    public static KeyValue<GenericRecord, GenericRecord> parseWindowKey(Windowed<GenericRecord> windowKey, Long count) {
        String startTime = new DateTime(windowKey.window().start()).toString("HH:mm:ss");
        String concept = windowKey.key().get("concept").toString();
        String character = windowKey.key().get("character").toString();

        GenericRecord record = new GenericData.Record(aggValueSchema);
        record.put("character", character);
        record.put("concept", concept);
        record.put("victories", count);
        record.put("dt", startTime);

        return new KeyValue<>(windowKey.key(), record);
    }

    public static KeyValue<GenericRecord, GenericRecord> parseWindowKey(long windowStart, GenericRecord group, long count) {
        String startTime = new DateTime(windowStart).toString("HH:mm:ss");
        String concept = group.get("concept").toString();
        String character = group.get("character").toString();

        GenericRecord record = new GenericData.Record(aggValueSchema);
        record.put("character", character);
        record.put("concept", concept);
        record.put("victories", count);
        record.put("dt", startTime);

        return new KeyValue<>(group, record);
    }
}
