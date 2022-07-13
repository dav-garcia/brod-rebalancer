package com.github.davgarcia.brodrebalancer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Value
@Builder
public class Assignments {

    int version;
    List<Partition> partitions;

    public void print() {
        final var brokers = new TreeMap<Integer, Broker>();

        for (final var partition : partitions) {
            if (!partition.replicas.isEmpty()) {
                final var leader = partition.replicas.get(0);
                brokers.computeIfAbsent(leader, Broker::of).leader++;

                partition.replicas.stream()
                        .skip(1) // Skips the leader.
                        .forEach(f -> brokers.computeIfAbsent(f, Broker::of).follower++);
            }
        }

        final var leaders = brokers.values().stream()
                .mapToInt(b -> b.leader)
                .sum();
        final var totals = brokers.values().stream()
                .mapToInt(Broker::computeTotal)
                .sum();

        System.out.println("________________________________________________________________________________");
        System.out.println("Broker    Replicas    Leader  Follower   % leader  Leader distrib  Total distrib");
        brokers.values().stream()
                .map(b -> String.format("%6d  %10d  %8d  %8d      %4.1f%%           %4.1f%%          %4.1f%%",
                        b.id, b.leader + b.follower, b.leader, b.follower,
                        100.0 * b.leader / b.computeTotal(),
                        100.0 * b.leader / leaders, 100.0 * b.computeTotal() / totals))
                .forEach(System.out::println);
    }

    public static Assignments from(final LogDirs logDirs) {
        final var rawPartitions = logDirs.getBrokers().stream()
                .flatMap(b -> b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream()
                                .map(p -> Pair.of(p.getPartition(), b.getBroker()))))
                .collect(Collectors.groupingBy(Pair::getLeft, TreeMap::new,
                        Collectors.mapping(Pair::getRight, Collectors.toList())));
        return doBuild(rawPartitions);
    }

    public static Assignments from(final Status status) {
        final var rawPartitions = status.getBrokers().values().stream()
                .flatMap(b -> b.getPartitions().stream()
                        .map(p -> Pair.of(p, b.getId())))
                .collect(Collectors.groupingBy(Pair::getLeft, TreeMap::new,
                        Collectors.mapping(Pair::getRight, Collectors.toList())));
        return doBuild(rawPartitions);
    }

    private static Assignments doBuild(final TreeMap<String, List<Integer>> rawPartitions) {
        final var partitions = rawPartitions.entrySet().stream()
                .map(e -> Partition.builder()
                        .topic(getTopicName(e.getKey()))
                        .partition(getPartitionNumber(e.getKey()))
                        .replicas(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return Assignments.builder()
                .version(1)
                .partitions(partitions)
                .build();
    }

    private static String getTopicName(final String partition) {
        return StringUtils.substringBeforeLast(partition, "-");
    }

    private static int getPartitionNumber(final String partition) {
        return Integer.parseInt(StringUtils.substringAfterLast(partition, "-"));
    }

    @Value
    @Builder
    @SuppressWarnings("java:S1700") // The field name must be "partition".
    public static class Partition {

        String topic;
        int partition;
        List<Integer> replicas;
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Broker {

        int id;
        @NonFinal
        int leader;
        @NonFinal
        int follower;

        public int computeTotal() {
            return leader + follower;
        }

        public static Broker of(final int id) {
            return new Broker(id, 0, 0);
        }
    }
}
