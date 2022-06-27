package com.github.davgarcia.brodrebalancer.brokerstrategy;

import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.Collection;

public class RandomSourceBrokerStrategy implements SourceBrokerStrategy<Object> {

    @Override
    public Status.Broker select(final MovablePartitions.Partition partition, final Collection<Status.Broker> brokers) {
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
