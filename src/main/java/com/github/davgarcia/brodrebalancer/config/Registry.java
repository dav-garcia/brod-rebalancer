package com.github.davgarcia.brodrebalancer.config;

import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomDestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomSourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.rebalancer.FirstFitDecreasingRebalancer;
import com.github.davgarcia.brodrebalancer.LogDirsInput;
import com.github.davgarcia.brodrebalancer.ReassignmentsOutput;
import com.github.davgarcia.brodrebalancer.Rebalancer;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.ReassignmentsFileAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Registry {

    private final List<LogDirsInput<?>> logDirsInputs;
    private final List<ReassignmentsOutput<?>> reassignmentsOutputs;
    private final List<Rebalancer<?>> rebalancers;
    private final List<SourceBrokerStrategy<?>> srcBrokerStrategies;
    private final List<DestinationBrokerStrategy<?>> dstBrokerStrategies;

    public Registry() {
        logDirsInputs = List.of(new LogDirsFileAdapter());
        reassignmentsOutputs = List.of(new ReassignmentsFileAdapter());
        rebalancers = List.of(new FirstFitDecreasingRebalancer());
        srcBrokerStrategies = List.of(new RandomSourceBrokerStrategy());
        dstBrokerStrategies = List.of(new RandomDestinationBrokerStrategy());
    }

    public List<Registered<?>> getAllRegistered() {
        final var result = new ArrayList<Registered<?>>();
        Stream.of(logDirsInputs, reassignmentsOutputs, rebalancers, srcBrokerStrategies, dstBrokerStrategies)
                .forEach(result::addAll);
        return result;
    }

    public LogDirsInput<?> getLogDirsInput(final String name) {
        return getRegistered(logDirsInputs, name);
    }

    public ReassignmentsOutput<?> getReassignmentsOutput(final String name) {
        return getRegistered(reassignmentsOutputs, name);
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

    private <T extends Registered<?>> T getRegistered(final List<T> list, final String name) {
        return list.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BrodRebalancerException("Unknown name: " + name));
    }

    public void printUsage() {
        System.out.println(format("Inputs", logDirsInputs));
        System.out.println(format("Outputs", reassignmentsOutputs));
        System.out.println(format("Rebalancers", rebalancers));
        System.out.println(format("Source broker strategies", srcBrokerStrategies));
        System.out.println(format("Destination broker strategies", dstBrokerStrategies));
    }

    private <T extends Registered<?>> String format(final String title, final List<T> list) {
        return String.format("%s: %s", title, list.stream()
                .map(Registered::getName)
                .collect(Collectors.joining(", ")));
    }
}
