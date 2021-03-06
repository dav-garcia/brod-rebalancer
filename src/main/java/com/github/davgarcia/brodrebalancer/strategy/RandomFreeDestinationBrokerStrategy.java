package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Status;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomFreeDestinationBrokerStrategy implements DestinationBrokerStrategy<Object> {

    private final Random random;

    public RandomFreeDestinationBrokerStrategy() {
        random = new Random();
    }

    @Override
    public String getName() {
        return "random-free";
    }

    @Override
    public String getHelp() {
        return "The destination broker is chosen randomly between those not having" + System.lineSeparator() +
                "  the replica already and not being overloaded after the move.";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @Override
    public Status.Broker select(final MovablePartitions.Partition partition, final List<Status.Broker> brokers) {
        final var filteredBrokers = brokers.stream()
                .filter(b -> !b.isOverloadedAfterAdding(partition.getSize()))
                .collect(Collectors.toList());
        return filteredBrokers.isEmpty() ? null : filteredBrokers.get(random.nextInt(filteredBrokers.size()));
    }
}
