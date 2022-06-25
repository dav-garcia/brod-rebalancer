package com.github.davgarcia.brodrebalancer;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder
public class Status {

    List<BrokerStatus> brokers;

    public void print() {
        System.out.println("Broker    Capacity      Current size         Goal size       Usage        Diff");
        brokers.stream()
                .map(b -> String.format("%6d  %10.1f  %16.0f  %16.0f  %9.1f%%  %9.1f%%",
                        b.getId(), b.getCapacity(), b.getCurrentSize(), b.getGoalSize(),
                        b.getCurrentSize() / b.getGoalSize() * 100,
                        (b.getCurrentSize() / b.getGoalSize() - 1) * 100))
                .forEach(System.out::println);
    }

    public static Status from(final BrokersConfig config, final LogDirs logDirs) {
        validate(config, logDirs);

        final var replicasBySize = computeReplicas(logDirs);
        final var currentSizes = computeCurrentSizes(logDirs);
        final var totalSize = currentSizes.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        final var totalCapacity = config.getBrokers().stream()
                .mapToDouble(BrokersConfig.BrokerConfig::getCapacity)
                .sum();

        final var builder = Status.builder();
        final var brokers = config.getBrokers().stream()
                .map(b -> BrokerStatus.builder()
                        .id(b.getId())
                        .capacity(b.getCapacity())
                        .goalSize(computeGoalSize(totalSize, totalCapacity, b.getCapacity()))
                        .currentSize(currentSizes.get(b.getId()))
                        .replicasBySize(replicasBySize.get(b.getId()))
                        .build())
                .collect(Collectors.toList());
        builder.brokers(brokers);
        return builder.build();
    }

    private static void validate(final BrokersConfig config, final LogDirs logDirs) {
        final var configIds = config.getBrokers().stream()
                .map(BrokersConfig.BrokerConfig::getId)
                .collect(Collectors.toSet());
        final var logDirsIds = logDirs.getBrokers().stream()
                .map(LogDirs.Broker::getBroker)
                .collect(Collectors.toSet());
        if (!configIds.equals(logDirsIds)) {
            throw new BrodRebalancerException("Configured brokers don't match brokers in log dirs input.");
        }
    }

    private static Map<Integer, List<Replica>> computeReplicas(final LogDirs logDirs) {
        return logDirs.getBrokers().stream()
                .map(b -> Pair.of(b.getBroker(), b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream())
                        .map(p -> Replica.builder()
                                .partition(p.getPartition())
                                .size(p.getSize())
                                .build())
                        .sorted(Comparator.comparingDouble(Replica::getSize))
                        .collect(Collectors.toList())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private static Map<Integer, Double> computeCurrentSizes(final LogDirs logDirs) {
        return logDirs.getBrokers().stream()
                .map(b -> Pair.of(b.getBroker(), b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream())
                        .mapToDouble(LogDirs.Partition::getSize)
                        .sum()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private static double computeGoalSize(final double totalSize, final double totalCapacity, final double capacity) {
        return totalSize * capacity / totalCapacity;
    }

    @Value
    @Builder
    public static class BrokerStatus {

        int id;
        double capacity;
        double goalSize;
        @NonFinal
        double currentSize;
        List<Replica> replicasBySize;
    }

    @Value
    @Builder
    public static class Replica {

        String partition;
        double size;
    }
}
