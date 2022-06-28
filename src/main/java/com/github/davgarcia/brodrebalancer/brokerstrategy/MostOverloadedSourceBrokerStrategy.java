package com.github.davgarcia.brodrebalancer.brokerstrategy;

import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.Comparator;
import java.util.List;

public class MostOverloadedSourceBrokerStrategy implements SourceBrokerStrategy<Object> {
    @Override
    public String getName() {
        return "most-overloaded";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @Override
    public Status.Broker select(final MovablePartitions.Partition partition, final List<Status.Broker> brokers) {
        return brokers.stream()
                .max(Comparator.comparingDouble(Status.Broker::computeUsageRatio))
                .orElse(null);
    }
}
