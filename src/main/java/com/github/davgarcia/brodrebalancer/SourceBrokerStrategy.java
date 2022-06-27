package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Registered;

import java.util.Collection;

public interface SourceBrokerStrategy<T> extends Registered<T> {

    Status.Broker select(final MovablePartitions.Partition partition, final Collection<Status.Broker> brokers);
}
