package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Configuration;

import java.util.List;
import java.util.Set;

public class Runner {

    private final Configuration config;
    private final LogDirsInput<?> input;
    private final AssignmentsOutput<?> output;
    private final Rebalancer<?> rebalancer;
    private final LeaderStrategy<?> leaderStrategy;

    public Runner(final Configuration config,
                  final LogDirsInput<?> input,
                  final AssignmentsOutput<?> output,
                  final Rebalancer<?> rebalancer,
                  final SourceBrokerStrategy<?> srcBrokerStrategy,
                  final DestinationBrokerStrategy<?> dstBrokerStrategy,
                  final LeaderStrategy<?> leaderStrategy) {
        this.config = config;
        this.input = input;
        this.output = output;
        this.rebalancer = rebalancer;
        this.leaderStrategy = leaderStrategy;
        rebalancer.setBrokerStrategies(srcBrokerStrategy, dstBrokerStrategy);
    }

    public void run() {
        final var logDirs = input.load();
        logDirs.filterTopics(toSet(config.getTopics().getInclude()), toSet(config.getTopics().getExclude()));
        final var reassignments = rebalancer.rebalance(config, logDirs);
        leaderStrategy.electLeaders(config, reassignments);
        new Checker().check(Assignments.from(logDirs), reassignments);
        output.save(reassignments);
    }

    private <T> Set<T> toSet(final List<T> list) {
        return list == null ? null : Set.copyOf(list);
    }
}
