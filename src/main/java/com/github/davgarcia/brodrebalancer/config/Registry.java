package com.github.davgarcia.brodrebalancer.config;

import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.LeaderStrategy;
import com.github.davgarcia.brodrebalancer.strategy.MostOverloadedSourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.strategy.RandomDestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.strategy.RandomFreeDestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.strategy.RandomSourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.rebalancer.FirstFitDecreasingRebalancer;
import com.github.davgarcia.brodrebalancer.LogDirsInput;
import com.github.davgarcia.brodrebalancer.AssignmentsOutput;
import com.github.davgarcia.brodrebalancer.Rebalancer;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.AssignmentsFileAdapter;
import com.github.davgarcia.brodrebalancer.strategy.ShuffleLeaderStrategy;
import com.github.davgarcia.brodrebalancer.strategy.WeightedShuffleLeaderStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("java:S1452") // Use of wildcards is required.
public class Registry {

    private final List<LogDirsInput<?>> logDirsInputs;
    private final List<AssignmentsOutput<?>> assignmentsOutputs;
    private final List<Rebalancer<?>> rebalancers;
    private final List<SourceBrokerStrategy<?>> srcBrokerStrategies;
    private final List<DestinationBrokerStrategy<?>> dstBrokerStrategies;
    private final List<LeaderStrategy<?>> leaderStrategies;

    public Registry() {
        logDirsInputs = List.of(new LogDirsFileAdapter());
        assignmentsOutputs = List.of(new AssignmentsFileAdapter());
        rebalancers = List.of(new FirstFitDecreasingRebalancer());
        srcBrokerStrategies = List.of(new MostOverloadedSourceBrokerStrategy(), new RandomSourceBrokerStrategy());
        dstBrokerStrategies = List.of(new RandomFreeDestinationBrokerStrategy(), new RandomDestinationBrokerStrategy());
        leaderStrategies = List.of(new ShuffleLeaderStrategy(), new WeightedShuffleLeaderStrategy());
    }

    public List<Registered<?>> getAllRegistered() {
        final var result = new ArrayList<Registered<?>>();
        Stream.of(logDirsInputs, assignmentsOutputs, rebalancers, srcBrokerStrategies, dstBrokerStrategies, leaderStrategies)
                .forEach(result::addAll);
        return result;
    }

    public LogDirsInput<?> getLogDirsInput(final String name) {
        return getRegistered(logDirsInputs, name);
    }

    public AssignmentsOutput<?> getAssignmentsOutput(final String name) {
        return getRegistered(assignmentsOutputs, name);
    }

    public Rebalancer<?> getRebalancer(final String name) {
        return getRegistered(rebalancers, name);
    }

    public SourceBrokerStrategy<?> getSourceBrokerStrategy(final String name) {
        return getRegistered(srcBrokerStrategies, name);
    }

    public DestinationBrokerStrategy<?> getDestinationBrokerStrategy(final String name) {
        return getRegistered(dstBrokerStrategies, name);
    }

    public LeaderStrategy<?> getLeaderStrategy(final String name) {
        return getRegistered(leaderStrategies, name);
    }

    private <T extends Registered<?>> T getRegistered(final List<T> list, final String name) {
        return list.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BrodRebalancerException("Unknown name: " + name));
    }

    public void printUsage() {
        System.out.println(format("  Inputs", logDirsInputs));
        System.out.println(format("  Outputs", assignmentsOutputs));
        System.out.println(format("  Rebalancers", rebalancers));
        System.out.println(format("  Source broker strategies", srcBrokerStrategies));
        System.out.println(format("  Destination broker strategies", dstBrokerStrategies));
        System.out.println(format("  Leader election strategies", leaderStrategies));
    }

    private <T extends Registered<?>> String format(final String title, final List<T> list) {
        return String.format("%s: %s", title, list.stream()
                .map(Registered::getName)
                .collect(Collectors.joining(", ")));
    }
}
