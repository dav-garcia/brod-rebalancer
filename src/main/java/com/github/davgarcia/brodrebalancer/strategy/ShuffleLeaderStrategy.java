package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.LeaderStrategy;
import com.github.davgarcia.brodrebalancer.config.Configuration;

import java.util.Collections;
import java.util.Random;

public class ShuffleLeaderStrategy implements LeaderStrategy<Object> {

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp() {
        return "Random (uniform) distribution of leader replicas between brokers.";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @SuppressWarnings("java:S2119") // Intentionally create a new random for better uniform distribution.
    @Override
    public void electLeaders(final Configuration config, final Assignments assignments) {
        final var random = new Random();

        assignments.getPartitions().forEach(p -> Collections.shuffle(p.getReplicas(), random));
    }
}
