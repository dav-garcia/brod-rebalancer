package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Configuration;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@Builder
public class Status {

    SortedMap<Integer, Broker> brokers;
    @NonFinal
    int cumulativeNumMoves;
    @NonFinal
    double cumulativeSizeMoved;

    public Broker getBroker(final int id) {
        return brokers.get(id);
    }

    public List<Broker> getBrokers(final Set<Integer> ids) {
        return brokers.values().stream()
                .filter(b -> ids.contains(b.id))
                .collect(Collectors.toList());
    }

    public List<Broker> findDestinations(final String partition) {
        return brokers.values().stream()
                .filter(Broker::isFree)
                .filter(b -> !b.hasPartition(partition))
                .collect(Collectors.toList());
    }

    public void move(final Broker srcBroker, final Broker dstBroker, final String partition, final double size) {
        srcBroker.removePartition(partition, size);
        dstBroker.addPartition(partition, size);

        cumulativeNumMoves++;
        cumulativeSizeMoved += size;
    }

    public double computeMaxDiff() {
        return brokers.values().stream()
                .mapToDouble(Broker::computeDiff)
                .max()
                .orElse(0.0);
    }

    public double computeGap() {
        return brokers.values().stream()
                .mapToDouble(Broker::computeGap)
                .sum();
    }

    public void print() {
        System.out.println("________________________________________________________________________________");
        System.out.println("Broker  Replicas  Capac    Current size       Goal size       Diff size    Usage");
        brokers.values().stream()
                .map(b -> String.format("%6d  %8d  %5.1f  %14.0f  %14.0f  %+14.0f  %6.1f%%",
                        b.id, b.partitions.size(), b.capacity, b.currentSize, b.goalSize,
                        b.goalSize - b.currentSize,
                        100.0 * b.computeUsageRatio()))
                .forEach(System.out::println);
        System.out.printf("Total gap: %-14.0f  No of moves: %-10d  Amount moved: %-14.0f%n",
                computeGap(), cumulativeNumMoves, cumulativeSizeMoved);
    }

    public static Status from(final Configuration config, final LogDirs logDirs) {
        validate(config, logDirs);

        final var partitions = computePartitions(logDirs);
        final var currentSizes = computeCurrentSizes(logDirs);
        final var totalSize = currentSizes.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        final var totalCapacity = config.getBrokers().stream()
                .mapToDouble(Configuration.Broker::getCapacity)
                .sum();

        final var brokers = config.getBrokers().stream()
                .map(b -> Broker.builder()
                        .id(b.getId())
                        .capacity(b.getCapacity())
                        .goalSize(computeGoalSize(totalSize, totalCapacity, b.getCapacity()))
                        .currentSize(currentSizes.get(b.getId()))
                        .partitions(partitions.get(b.getId()))
                        .build())
                .collect(Collectors.toMap(Broker::getId, Function.identity(), Status::failingMerge, TreeMap::new));

        return Status.builder()
                .brokers(brokers)
                .build();
    }

    private static void validate(final Configuration config, final LogDirs logDirs) {
        final var configIds = config.getBrokers().stream()
                .map(Configuration.Broker::getId)
                .collect(Collectors.toSet());
        final var logDirsIds = logDirs.getBrokers().stream()
                .map(LogDirs.Broker::getBroker)
                .collect(Collectors.toSet());
        if (!configIds.equals(logDirsIds)) {
            throw new BrodRebalancerException("Configured brokers don't match brokers in log dirs input.");
        }
    }

    private static Map<Integer, Set<String>> computePartitions(final LogDirs logDirs) {
        return logDirs.getBrokers().stream()
                .map(b -> Pair.of(b.getBroker(), b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream())
                        .map(LogDirs.Partition::getPartition)
                        .collect(Collectors.toSet())))
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
    public static class Broker {

        int id;
        double capacity;
        double goalSize;
        @NonFinal
        double currentSize;
        Set<String> partitions;

        public boolean isFree() {
            return goalSize > currentSize;
        }

        public boolean isFreeAfterAdding(final double size) {
            return goalSize > currentSize + size;
        }

        public boolean isOverloaded() {
            return goalSize < currentSize;
        }

        public boolean isOverloadedAfterAdding(final double size) {
            return goalSize < currentSize + size;
        }

        public boolean hasPartition(final String partition) {
            return partitions.contains(partition);
        }

        public void addPartition(final String partition, final double size) {
            if (partitions.contains(partition)) {
                throw new BrodRebalancerException(String.format("Broker %d already contains partition: %s", id, partition));
            }

            currentSize += size;
            partitions.add(partition);
        }

        public void removePartition(final String partition, final double size) {
            if (!partitions.contains(partition)) {
                throw new BrodRebalancerException(String.format("Broker %d does not contain partition: %s", id, partition));
            }

            currentSize -= size;
            partitions.remove(partition);
        }

        public double computeDiff() {
            return goalSize - currentSize;
        }

        public double computeGap() {
            return Math.abs(goalSize - currentSize);
        }

        public double computeUsageRatio() {
            return currentSize / goalSize;
        }

        public static Broker from(final Broker broker) {
            return Broker.builder()
                    .id(broker.id)
                    .capacity(broker.capacity)
                    .goalSize(broker.goalSize)
                    .currentSize(broker.currentSize)
                    .partitions(new HashSet<>(broker.partitions))
                    .build();
        }

        // TODO: Implement id-based equals & hashcode.
    }
}
