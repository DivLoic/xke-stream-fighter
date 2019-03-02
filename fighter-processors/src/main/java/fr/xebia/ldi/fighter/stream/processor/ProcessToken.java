package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Gift;
import fr.xebia.ldi.fighter.schema.Round;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

/**
 * Created by loicmdivad.
 */
public class ProcessToken implements Transformer<String, Round, KeyValue<Integer, Gift>> {

    private ProcessorContext context;
    private KeyValueStore<String, String> tokenStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.tokenStore = (KeyValueStore) this.context.getStateStore("TOKEN-STORE");
    }

    @Override
    public KeyValue<Integer, Gift> transform(String key, Round value) {
        KeyValueIterator<String, String> tokenIter = this.tokenStore.all();
        Optional<KeyValue<String, String>> kvToken = Optional.of(tokenIter.next());
        tokenIter.close();
        Optional<KeyValue<Integer, Gift>> kvGift = kvToken
                .map((token) -> new Gift(
                        token.key,
                        token.value,
                        value.getArena(),
                        value.getTerminal(),
                        value.getGame().name(),
                        value.getWinner().getName()))
                .map((gift) -> new KeyValue<>(gift.getArena(), gift));

        return kvGift.orElse(null);
    }

    @Override
    public void close() {

    }
}
