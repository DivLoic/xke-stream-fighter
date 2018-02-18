package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Arena;
import fr.xebia.ldi.fighter.schema.Player;
import fr.xebia.ldi.fighter.schema.Victory;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.groupedDataKey;

/**
 * Created by loicmdivad.
 */
public class ProcessPlayer implements Processor<String, Player> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> arenaStore;
    private HashMap<String, Arena> arenaMap;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.arenaStore = (KeyValueStore) context.getStateStore("ARENA-STORE");
        this.arenaMap = loadFromLocalState();

        this.context.schedule(500, PunctuationType.WALL_CLOCK_TIME, (timestamp) ->
                this.arenaMap = loadFromLocalState());
    }

    @Override
    public void process(String key, Player value) {
        Arena origin = arenaMap.get(key);
        if(origin != null){
            Victory victory = new Victory(value, origin);
            GenericRecord victoryKey = groupedDataKey(victory);
            context.forward(victoryKey, victory);
        }
    }

    @Override
    public void punctuate(long timestamp) { }

    @Override
    public void close() {

    }

    private HashMap<String, Arena> loadFromLocalState() {
        KeyValueIterator<String, Arena> iter = this.arenaStore.all();
        HashMap<String, Arena> arenas = new HashMap<>();
        while (iter.hasNext()) {
            KeyValue<String, Arena> entry = iter.next();
            arenas.put(entry.key, entry.value);
        }
        iter.close();
        return arenas;
    }

}
