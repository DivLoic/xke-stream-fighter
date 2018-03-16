# xke-stream-fighter
[![Build Status](https://travis-ci.org/DivLoic/xke-stream-fighter.svg?branch=talk)](https://travis-ci.org/DivLoic/xke-stream-fighter)

This project is related to the talk: [**Processor API**](#/). 
It gathers a few code examples showing how the *Streams DSL*,
from [Kafka Streams](https://kafka.apache.org/documentation/streams/),
relies on a lower level api and why this api is exposed. While describing
the library, this modules shows a few stream processing concepts. 

### Stream DSL
This higher level API brings the `KStream` & `KTable` abstractions.
It's simple, expressive and declarative. Here is a simple aggregation.

```java
StreamsBuilder builder = new StreamsBuilder();
GlobalKTable<String, Arena> arenaTable = builder.globalTable(/** */ "ARENAS");
KStream<String, Round> rounds = builder.stream(/** */ "ROUNDS");
rounds
       .filter((String arenaId, Round round) -> round.getGame() == StreetFighter)
       .map((String arenaId, Round round) -> new KeyValue<>(arenaId, round.getWinner()))

       .join(arenaTable, (arena, player) -> arena, Victory::new)
       .selectKey(Parsing::extractConceptAndCharacter)
       .groupByKey().windowedBy(window).count(/** */);
```
But this api won't late you directly access the state of your streaming app. 


### Processor API
```java
public class ProcessPlayer implements Processor<String, Player> {

    private ProcessorContext context;
    private KeyValueStore<String, Arena> arenaStore;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.arenaStore = (KeyValueStore) context.getStateStore("ARENA-STORE");
    }

    @Override
    public void process(String key, Player value) {
        Optional<Arena> mayBeArena = Optional.ofNullable(this.arenaStore.get(key));
        
        mayBeArena.ifPresent(arena -> {
                    Victory victory = new Victory(value, arena);
                    GenericRecord victoryKey = groupedDataKey(victory);
                    context.forward(victoryKey, victory);
                }
        );
    }
}
```

### Token Provider

## tl;dr

Prerequisites: 
- sbt
- scala
- docker
```bash
git clone git@github.com:DivLoic/xke-stream-fighter.git
cd xke-stream-fighter
sbt dockerComposeUp
```
This will trigger a set of containers including: the confluent stacks, a dataset generator
and the streaming application example. 

