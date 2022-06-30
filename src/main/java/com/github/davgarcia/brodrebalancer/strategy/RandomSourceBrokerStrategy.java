package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.List;
import java.util.Random;

public class RandomSourceBrokerStrategy implements SourceBrokerStrategy<Object> {

    private final Random random;

    public RandomSourceBrokerStrategy() {
        random = new Random();
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public String getHelp() {
        return "Choose a random source broker for the next replica to be moved.";
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
