package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.ReassignmentsFileAdapter;

import java.util.List;

public class Registry {

    private final List<LogDirsInput<?>> logDirsInputs;
    private final List<ReassignmentsOutput<?>> reassignmentsOutputs;
    private final List<Rebalancer<?>> rebalancers;

    public Registry() {
        logDirsInputs = List.of(new LogDirsFileAdapter());
        reassignmentsOutputs = List.of(new ReassignmentsFileAdapter());
        rebalancers = List.of(new SimpleRebalancer());
    }

    public List<LogDirsInput<?>> getAllLogDirsInputs() {
        return logDirsInputs;
    }

    public List<ReassignmentsOutput<?>> getAllReassignmentsOutputs() {
        return reassignmentsOutputs;
    }

    public List<Rebalancer<?>> getAllRebalancers() {
        return rebalancers;
    }

    public LogDirsInput<?> getLogDirsInput(final String name) {
        return logDirsInputs.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BrodRebalancerException("Unknown log dirs input: " + name));
    }

    public ReassignmentsOutput<?> getReassignmentsOutput(final String name) {
        return reassignmentsOutputs.stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BrodRebalancerException("Unknown reassignments output: " + name));
    }

    public Rebalancer<?> getRebalancer(final String name) {
        return rebalancers.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BrodRebalancerException("Unknown rebalancer: " + name));
    }
}
