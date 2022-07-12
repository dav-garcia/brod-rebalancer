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

### Broker capacity and other configurations

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

This configuration file can also list topic names to include or exclude. \
For example:
```json
{
  "version": 1,
  "brokers": [
    ...
  ],
  "topics": {
    "exclude": ["__internal_topic", "SPECIAL_TOPIC"]
  }
}
```

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
$ java -jar target/brod-rebalancer-1.0-SNAPSHOT-jar-with-dependencies.jar --config config.json --input-path 
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

The table also shows the expected percent usage for each broker. \
The closer these values are to 100%, the more optimal the algorithm is.

Last but not least, you will see how the leader replica election has gone. \
A real example:
```shell
Leaders before reelection: {1=881, 2=932, 3=883, 4=916, 5=889, 6=82, 7=4, 8=14}
Leaders after reelection: {1=702, 2=723, 3=713, 4=709, 5=708, 6=350, 7=339, 8=357}
```

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

Run the tool with `--help` to see all the available options, including a short description of each optional strategy or rebalancer:
```shell
$ java -jar target/brod-rebalancer-1.0-SNAPSHOT-jar-with-dependencies.jar --help
Builds a smart partition reassignment plan for Kafka
taking into account brokers capacity and partition sizes.

Usage: brod-rebalancer [options]
  Options:
  * --config
      Location of Brokers' capacity definition and other configurations.
      Default: config.json
    --dst-broker-strategy
      How to choose the destination broker where the replica will be moved.
      Default: random-free
    --ffd-max-threshold
      Max gap (as a % from the initial gap) that is acceptable for the 
      reassignment. Set to a lower value to force an even better rebalance.
      Default: 0.05
    --help
      Print this help.
    --input
      Type of log dirs input loader.
      Default: file
    --input-path
      Location of the JSON file generated by kafka-log-dirs.sh (required if 
      --input file).
    --leader-strategy
      How the leader replica will be chosen for each partition.
      Default: weighted-shuffle
    --output
      Type of reassignments output saver.
      Default: file
    --output-path
      Location to save the JSON file to be passed to 
      kafka-reassign-partitions.sh (required if --output file).
    --rebalancer
      Type of rebalancer algorithm.
      Default: ffd
    --src-broker-strategy
      How to choose the source broker holding the replica to be moved.
      Default: most-overloaded

Inputs:
- file: Reads log dirs information from a JSON file generated by kafka-log-dirs.sh.

Outputs:
- file: Writes output to a JSON file compatible with kafka-reassign-partitions.sh.

Rebalancers:
- ffd: Applies a First-Fit-Decreasing (FFD) algorithm where the items to pack
  are the movable partitions (i.e. the ones in overloaded brokers)
  and the bins are the free brokers.
  Source/destination broker strategies are taken into account.

Source broker strategies:
- most-overloaded: The replica to be moved is taken from the most overloaded broker.
- random: Choose a random source broker for the next replica to be moved.

Destination broker strategies:
- random-free: The destination broker is chosen randomly between those not having
  the replica already and not being overloaded after the move.
- random: The replica is moved to any broker (not having it already) randomly.

Leader election strategies:
- weighted-shuffle: Weighted random (not uniform) distribution of leader replicas.
  Beware this leader election strategy might overload your most powerful brokers.
- shuffle: Random (uniform) distribution of leader replicas between brokers.
```

With `--input` and `--output` you can choose where the log dirs data comes from and where the reassignments proposal is sent.

Use `--src-broker-strategy` and `--dst-broker-strategy` to respectively define how to select the replica to be moved from one of the overloaded brokers and how to select the destination broker.

The `--rebalancer` option chooses the rebalancing algorithm, which decides what partitions will be moved.

Then, `--leader-strategy` decides the leader election strategy.

## Contributing

Although the tool works and has been used in real environments, clearly it lacks support for scenarios different than the ones faced by me.

In particular, alternative rebalancing algorithms and source/destination broker selection strategies would be very welcome.

## Why Brod

[Max Brod](https://en.wikipedia.org/wiki/Max_Brod) was a prolific writer best known for being one of Kafka's closest friends. \
As `brod-rebalancer` is also a good friend of Apache Kafka, it seemed appropriate to me to name this tool after him.
