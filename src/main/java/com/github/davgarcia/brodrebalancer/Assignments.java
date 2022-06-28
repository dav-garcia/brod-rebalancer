package com.github.davgarcia.brodrebalancer;

import lombok.Builder;
import lombok.Value;
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
    public static class Partition {

        String topic;
        int partition;
        List<Integer> replicas;
    }
}
