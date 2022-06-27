package com.github.davgarcia.brodrebalancer.brokerstrategy;

import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.Set;

public class RandomDestinationBrokerStrategy implements DestinationBrokerStrategy<Object> {

    @Override
    public Status.Broker select(final MovablePartitions.Partition partition, final Set<Status.Broker> brokers) {
        // TODO: Implement...
        return null;
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }
}
