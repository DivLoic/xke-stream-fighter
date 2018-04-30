# xke-stream-fighter
[![Build Status](https://travis-ci.org/DivLoic/xke-stream-fighter.svg?branch=master)](https://travis-ci.org/DivLoic/xke-stream-fighter)
[![codecov](https://codecov.io/gh/DivLoic/xke-stream-fighter/branch/master/graph/badge.svg)](https://codecov.io/gh/DivLoic/xke-stream-fighter)

This project is related to the talk: [**Processor API**](#/). 
It gathers a few code examples showing how the *Streams DSL*,
from [Kafka Streams](https://kafka.apache.org/documentation/streams/),
relies on a lower level api and why this api is exposed. While describing
the library, this modules shows a few stream processing concepts. 

## tl;dr

Prerequisites: 
- [:arrow_down:](https://www.scala-sbt.org/download.html) sbt
- [:arrow_down:](http://www.scala-lang.org/download/) scala
- [:arrow_down:](https://docs.docker.com/install/) docker
- [:arrow_down:](https://www.confluent.io/download/) confluent
```bash
git clone git@github.com:DivLoic/xke-stream-fighter.git
cd xke-stream-fighter
sbt dockerComposeUp
```
This will trigger a set of containers including: the confluent stack, a dataset generator
and the streaming application example. Start a consumer to see the input stream:
```bash
docker-compose -p <id> exec registry kafka-avro-console-consumer --bootstrap-server kafka:9092 --topic ROUNDS
```

The output stream represent the append log of the aggregation and can be seen with the following command:
```bash
docker-compose -p <id> exec registry kafka-avro-console-consumer --bootstrap-server kafka:9092 --topic RESULTS-DSL
```

The interractives queries are dumped in a file inside of the stream app container.
Given the project name `id` provided by the docker-compose plugin you can watch this file:
```bash
docker-compose -p <id> exec processors scripts/watch-interactive-queries.sh DSL
```
[![asciicast](https://asciinema.org/a/MSumeCHBTSmzAORfCjxxEThqX.png)](https://asciinema.org/a/MSumeCHBTSmzAORfCjxxEThqX)

## Motivation

Despite the importance of the processor api in the kafka-streams library, their
no mush resources (talk, examples, demos ...) about it. This modules were created
to demonstrate most of his features. It's based on the [confluent documentation](
https://docs.confluent.io/current/streams/developer-guide/processor-api.html).

### Stream DSL
The high level API brings the `KStream` & `KTable` abstractions.
It's simple, expressive and declarative. Here is a simple aggregation.

```java
StreamsBuilder builder = new StreamsBuilder();
GlobalKTable<String, Arena> arenaTable = builder.globalTable(/* */ "ARENAS");
KStream<String, Round> rounds = builder.stream(/* */ "ROUNDS");
rounds
       .filter((String arenaId, Round round) -> round.getGame() == StreetFighter)
       .map((String arenaId, Round round) -> new KeyValue<>(arenaId, round.getWinner()))

       .join(arenaTable, (arena, player) -> arena, Victory::new)
       .selectKey(Parsing::extractConceptAndCharacter)
       .groupByKey().windowedBy(window).count(/* */);
```
But this api won't let you access the states stores directly. 

### Processor API
By implementing a processor you have access to a processor context, containing a lot
of metadata and services.
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
Finally, the best part of using the Processor API appears when we combine 
it with the Stream DSL high level API. The `.transform()` method allow us to 
use Processor within a Kstream. 
```java
StreamsBuilder builder = new StreamsBuilder();
KStream<String, Round> rounds = builder
        .stream("ROUNDS", Consumed.with(Serdes.String(), roundSerde, new EventTimeExtractor(), LATEST));
rounds
        .filter((arenaId, round) -> round.getGame() == StreetFighter)
        .filter((arenaId, round) -> round.getWinner().getCombo() >= 5)
        .filter((arenaId, round) -> round.getWinner().getLife() >= 75)
        .through("ONE-PARTITION-WINNER-TOPIC")
        .transform(ProcessToken::new, "TOKEN-STORE")
        .to("TOKEN-PROVIDED");
```


