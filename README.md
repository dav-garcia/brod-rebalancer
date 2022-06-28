# brod-rebalancer

Apache Kafka companion to assist in rebalancing partitions across your brokers.

Unlike `kafka-reassign-partitions.sh`, `brod-rebalancer` takes into account your broker's capacity and your partition sizes when deciding where to move a replica. \
It will also try to move as less replicas as possible to minimize network traffic and CPU overload.

The current implementation doesn't try to spread each topic's partitions, although this could be added through a source broker strategy.

## How to use

PROCEDURE TO GATHER INPUT, RUN THE TOOL AND USE THE OUTPUT

## Options

EXPLAIN DIFFERENT COMPONENTS

## Contributing

Although the tool works and has been used in real environments, clearly it lacks support for scenarios different than the ones faced by me.

In particular, alternative rebalancing algorithms and source/destination broker selection strategies would be very welcome.

## Why Brod

[Max Brod](https://en.wikipedia.org/wiki/Max_Brod) was a prolific writer best known for being one of Kafka's closest friend. \
As `brod-rebalancer` is also a good friend of Apache Kafka, it seemed appropriate to me to name this tool after him.
