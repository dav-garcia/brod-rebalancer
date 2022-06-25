package com.github.davgarcia.brodrebalancer;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@Builder
public class Status {

    SortedMap<Integer, BrokerStatus> brokers;
    SortedMap<Replica, Set<Integer>> movableReplicas;

    public BrokerStatus findHighestDiff() {
        return brokers.values().stream()
                .max(Comparator.comparingDouble(BrokerStatus::computeDiff))
                .orElseThrow();
    }

    public double computeGap() {
        return brokers.values().stream()
                .mapToDouble(BrokerStatus::computeGap)
                .sum();
    }

    public void moveReplica(int fromBrokerId, int toBrokerId, Replica replica) {
        final var fromBroker = brokers.get(fromBrokerId);
        final var toBroker = brokers.get(toBrokerId);

        fromBroker.removeReplica(replica);
        toBroker.addReplica(replica);
    }

    public void print() {
        System.out.println("Broker    Capacity      Current size         Goal size         Diff size       Usage");
        brokers.values().stream()
                .map(b -> String.format("%6d  %10.1f  %16.0f  %16.0f  %+16.0f  %9.1f%%",
                        b.getId(), b.getCapacity(), b.getCurrentSize(), b.getGoalSize(),
                        b.getGoalSize() - b.getCurrentSize(),
                        b.getCurrentSize() / b.getGoalSize() * 100))
                .forEach(System.out::println);
        System.out.printf("Total gap: %16.0f%n", computeGap());
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

        final var brokers = config.getBrokers().stream()
                .map(b -> BrokerStatus.builder()
                        .id(b.getId())
                        .capacity(b.getCapacity())
                        .goalSize(computeGoalSize(totalSize, totalCapacity, b.getCapacity()))
                        .currentSize(currentSizes.get(b.getId()))
                        .replicasBySize(replicasBySize.get(b.getId()))
                        .build())
                .collect(Collectors.toMap(BrokerStatus::getId, Function.identity(), Status::failingMerge, TreeMap::new));

        final var movableReplicas = brokers.values().stream()
                .filter(b -> b.getCurrentSize() > b.getGoalSize())
                .flatMap(b -> b.getReplicasBySize().stream()
                        .map(r -> Pair.of(r, b.getId())))
                .collect(Collectors.groupingBy(Pair::getLeft, TreeMap::new, Collectors.mapping(Pair::getRight, Collectors.toSet())));

        return Status.builder()
                .brokers(brokers)
                .movableReplicas(movableReplicas)
                .build();
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

    private static Map<Integer, SortedSet<Replica>> computeReplicas(final LogDirs logDirs) {
        return logDirs.getBrokers().stream()
                .map(b -> Pair.of(b.getBroker(), b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream())
                        .map(p -> Replica.builder()
                                .partition(p.getPartition())
                                .size(p.getSize())
                                .build())
                        .collect(Collectors.toCollection(TreeSet::new))))
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

    private static <T> T failingMerge(T dummy1, T dummy2) {
        throw new BrodRebalancerException("Won't merge duplicate keys");
    }

    @Value
    @Builder
    public static class BrokerStatus {

        int id;
        double capacity;
        double goalSize;
        @NonFinal
        double currentSize;
        SortedSet<Replica> replicasBySize;

        public void addReplica(final Replica replica) {
            if (replicasBySize.contains(replica)) {
                throw new BrodRebalancerException(String.format("Broker %d already contains replica: %s", id, replica.getPartition()));
            }

            currentSize += replica.getSize();
            replicasBySize.add(replica);
        }

        public void removeReplica(final Replica replica) {
            if (!replicasBySize.contains(replica)) {
                throw new BrodRebalancerException(String.format("Broker %d does not contain replica: %s", id, replica.getPartition()));
            }

            currentSize -= replica.getSize();
            replicasBySize.remove(replica);
        }

        public double computeDiff() {
            return goalSize - currentSize;
        }

        public double computeGap() {
            return Math.abs(goalSize - currentSize);
        }
    }

    @Value
    @Builder
    public static class Replica implements Comparable<Replica> {

        String partition;
        double size;

        @Override
        public int compareTo(final Replica other) {
            return -Double.compare(size, other.size);
        }
    }
}
