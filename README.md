# brod-rebalancer
Apache Kafka companion to assist in rebalancing partitions across your brokers.

Unlike `kafka-reassign-partitions.sh`, `brod-rebalancer` takes into account your broker's capacity and your partition sizes when deciding where to move a replica. \
It will also try to move as less replicas as possible to minimize network traffic and CPU overload.
