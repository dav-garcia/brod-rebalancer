package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.LeaderStrategy;
import com.github.davgarcia.brodrebalancer.config.BrokersConfig;

import java.util.Collections;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShuffleLeaderStrategy implements LeaderStrategy<Object> {

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @SuppressWarnings("java:S2119") // Intentionally create a new random for better uniform distribution.
    @Override
    public void electLeaders(final BrokersConfig config, final Assignments assignments) {
        final var random = new Random();

        printLeaders("Leaders before reelection", assignments);
        assignments.getPartitions().forEach(p -> Collections.shuffle(p.getReplicas(), random));
        printLeaders("Leaders after reelection", assignments);
    }

    private void printLeaders(final String title, final Assignments assignments) {
        final var leaders = assignments.getPartitions().stream()
                .map(p -> p.getReplicas().get(0))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.printf("%s: %s%n", title, leaders);
    }
}
