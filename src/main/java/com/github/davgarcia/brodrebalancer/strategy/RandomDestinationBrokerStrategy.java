package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.List;
import java.util.Random;

public class RandomDestinationBrokerStrategy implements DestinationBrokerStrategy<Object> {

    private final Random random;

    public RandomDestinationBrokerStrategy() {
        random = new Random();
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public String getHelp() {
        return "The replica is moved to any broker (not having it already) randomly.";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @Override
    public Status.Broker select(final MovablePartitions.Partition partition, final List<Status.Broker> brokers) {
        return brokers.isEmpty() ? null : brokers.get(random.nextInt(brokers.size()));
    }
}
