package fr.xebia.ldi.fighter.stream.processor;

import fr.xebia.ldi.fighter.schema.Round;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * Created by loicmdivad.
 */
public class ProcessRound implements Processor<String, Round> {

    private ProcessorContext context;
    // no store here, this processing is state less

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(String key, Round value) {
        if(value.getWinner().getCombo() > 3){
            context.forward(key, value.getWinner());
        }
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}