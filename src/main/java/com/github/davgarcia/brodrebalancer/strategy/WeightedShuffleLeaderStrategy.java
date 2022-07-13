package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.LeaderStrategy;
import com.github.davgarcia.brodrebalancer.config.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Based on <a href="http://utopia.duth.gr/~pefraimi/research/data/2007EncOfAlg.pdf">Weighted Random Sampling (2005; Efraimidis, Spirakis)</a>.
 * <p>
 * Beware this leader election strategy can overload your most powerful brokers, as the rebalancing algorithm has already
 * moved more and/or larger (busier) replicas to them.<br>
 * A uniform leader distribution (e.g. default shuffle) will ensure the weights applied during rebalancing are not altered.
 */
public class WeightedShuffleLeaderStrategy implements LeaderStrategy<Object> {

    @Override
    public String getName() {
        return "weighted-shuffle";
    }

    @Override
    public String getHelp() {
        return "Weighted random (not uniform) distribution of leader replicas." + System.lineSeparator() +
                "  Beware this leader election strategy might overload your most powerful brokers.";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @SuppressWarnings("java:S2119") // Intentionally create a new random for better uniform distribution.
    @Override
    public void electLeaders(final Configuration config, final Assignments assignments) {
        final var random = new Random();

        electLeaders(config, assignments, random);
    }

    void electLeaders(final Configuration config, final Assignments assignments, final Random random) { // For testing.
        final var weights = buildWeights(config);

        assignments.getPartitions().stream()
                .map(Assignments.Partition::getReplicas)
                .forEach(r -> shuffle(r, filterWeights(weights, r), random));
    }

    private List<Double> buildWeights(final Configuration config) {
        return config.getBrokers().stream()
                .sorted(Comparator.comparingInt(Configuration.Broker::getId)) // Config is user given => Can be unsorted.
                .map(Configuration.Broker::getCapacity)
                .collect(Collectors.toList());
    }

    private List<Double> filterWeights(final List<Double> weights, List<Integer> replicas) {
        return replicas.stream()
                .map(r -> weights.get(r - 1))
                .collect(Collectors.toList());
    }

    private void shuffle(final List<Integer> replicas, final List<Double> weights, final Random random) {
        final var positions = IntStream.range(0, replicas.size()).boxed().collect(Collectors.toList());
        final var weightedRandoms = weights.stream()
                .map(w -> 1.0 / w * random.nextDouble())
                .collect(Collectors.toList());
        positions.sort(Comparator.comparingDouble(weightedRandoms::get));

        final var copy = new ArrayList<>(replicas);
        for (int i = 0; i < copy.size(); i++) {
            replicas.set(i, copy.get(positions.get(i)));
        }
    }
}
