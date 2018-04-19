package fr.xebia.ldi.fighter.stream.utils;

import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by loicmdivad.
 */
public class Parsing {

    public static final DateTimeFormatter windowformatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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

    public static String printWindowStart(Long start){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.of("Europe/Paris")).format(windowformatter);
    }

    private static GenericRecord generateWindowKey(String character, String concept) {
        GenericRecord record = new GenericData.Record(characterConceptKey);
        record.put("character", character);
        record.put("concept", concept);
        return record;
    }

    public static Stream<GenericRecord> generateWindowKeys(String concept){
        return Arrays.stream(characters)
                .map((String character) -> generateWindowKey(character, concept));
    }

    public static GenericRecord groupedDataKey(Victory v) {
        GenericRecord record = new GenericData.Record(characterConceptKey);
        record.put("character", v.getCharacter().getName());
        record.put("concept", v.getArena().getType());
        return record;
    }

    public static GenericRecord extractConceptAndCharacter(String arenaId, Victory victory) {
        GenericRecord record = new GenericData.Record(characterConceptKey);
        record.put("character", victory.getCharacter().getName());
        record.put("concept", victory.getArena().getType());
        return record;
    }

    public static KeyValue<GenericRecord, GenericRecord> parseWindowKey(Windowed<GenericRecord> windowKey, Long count) {
        String startTime = printWindowStart(windowKey.window().start());
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
        String startTime = printWindowStart(windowStart);
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
