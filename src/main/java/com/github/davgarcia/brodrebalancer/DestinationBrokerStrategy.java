package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Registered;

import java.util.Set;

public interface DestinationBrokerStrategy<T> extends Registered<T> {

    Status.Broker select(final MovablePartitions.Partition partition, final Set<Status.Broker> brokers);
}
