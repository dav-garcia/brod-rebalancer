# brod-rebalancer

Apache Kafka companion to assist in rebalancing partitions across your brokers.

Unlike `kafka-reassign-partitions.sh`, `brod-rebalancer` takes into account your broker's capacity and your partition sizes when deciding where to move a replica. \
It will also try to move as less replicas as possible to minimize network traffic and CPU overload.

The current implementation doesn't try to spread each topic's partitions, although this could be added through a source broker strategy.

## How to build

You will need Java 11 and Maven to build the project with:
```shell
$ mvn clean package
```

This will produce the file `brod-rebalancer-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.

## How to use

### Broker capacity configuration

First, define your Kafka brokers capacity in a JSON file like this one:
```json
{
  "version": 1,
  "brokers": [
    {
      "id": 1,
      "capacity": 1.5
    }, {
      "id": 2,
      "capacity": 1.0
    }, {
      "id": 3,
      "capacity": 1.0
    }, {
      "id": 4,
      "capacity": 2.0
    }, {
      "id": 5,
      "capacity": 1.5
    }
  ]
}
```

Each item represents one broker and its capacity relative to the others. \
The exact numbers are not important; what matters is the relation between them.

In the example above, broker 4 is twice as powerful as brokers 2 and 3. \
On the other hand, brokers 1 and 5 are 50% more powerful than brokers 2 and 3. \
That's it.

### Log-dirs input

Run `kafka-log-dirs.sh` from Apache Kafka `bin` directory to export information about all your partitions, for example:
```shell
$ ./bin/kafka-log-dirs.sh --bootstrap-server my-kafka-broker:9092 --broker-list 1,2,3,4,5 --describe > log-dirs.json
```

Edit the file to remove the first two lines which are just garbage.

### Reassignments output

Now it's `brod-rebalancer`'s turn to generate a partition reassignment proposal from the two previous files.

> **Note**
>
> If the brokers capacity list doesn't match the list of brokers from your `log-dirs.json` file, then an error will be thrown.

See the section below for more details about the available options, but a common run would be like this:
```shell
$ java -jar target/brod-rebalancer-1.0-SNAPSHOT-jar-with-dependencies.jar --brokers brokers-config.json --input-path 
log-dirs.json --output-path reassignments.json
```

On standard output, the tool will display the reassignment proposal progress. \
At the end, you will be able to see the number of movements (how many replicas will be moved between brokers) and the total amount of bytes to be moved. \
An example from the tests:
```shell
____________________________________________________________________________________
Broker    Capacity      Current size         Goal size         Diff size       Usage
     1         1,0                 9                10                +1       90,0%
     2         1,0                11                10                -1      110,0%
     3         2,0                20                20                +0      100,0%
Total gap: 2                 No of moves: 2           Amount moved: 8
```

Other important data are the expected percent usage for each broker. \
The closer these values are to 100%, the more optimal the algorithm is.

### Reassignments execution & verification

> **Warning**
> 
> The reassignment proposal can be very large and its execution can seriously overload your brokers. \
> Besides, it can be hard to recover from broker failures during this process.
> 
> All in all, if your reassignment incurs in far too many replica movements and/or too much network traffic, consider splitting it in smaller chunks.
> 
> With this approach you will be able to better control the process in multiple execute/verify cycles.
> 
> It is also recommended to set throttling.

Back to your official Apache Kafka `bin` tools, start the rebalancing process with this command:
```shell
$ ./kafka/bin/kafka-reassign-partitions.sh --bootstrap-server my-kafka-broker:9092 --execute --reassignment-json-file reassignments.json --throttle 20000000
```

Then, periodically check the progress with a similar command:
```shell
$ ./kafka/bin/kafka-reassign-partitions.sh --bootstrap-server my-kafka-broker:9092 --verify --reassignment-json-file reassignments.json
```

> **Note**
>
> You must run the verify command at least once after the reassignment is complete to make sure the throttling is removed.

## Options

EXPLAIN DIFFERENT COMPONENTS

## Contributing

Although the tool works and has been used in real environments, clearly it lacks support for scenarios different than the ones faced by me.

In particular, alternative rebalancing algorithms and source/destination broker selection strategies would be very welcome.

## Why Brod

[Max Brod](https://en.wikipedia.org/wiki/Max_Brod) was a prolific writer best known for being one of Kafka's closest friends. \
As `brod-rebalancer` is also a good friend of Apache Kafka, it seemed appropriate to me to name this tool after him.
