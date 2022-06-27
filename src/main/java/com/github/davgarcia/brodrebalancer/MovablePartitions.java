package com.github.davgarcia.brodrebalancer;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Value
@Builder
public class MovablePartitions {

    SortedSet<Partition> partitions;

    public Partition findLargest(final double maxSize) {
        return partitions.stream()
                .filter(p -> p.getSize() <= maxSize)
                .findFirst()
                .orElse(null);
    }

    public void remove(final Partition partition, final int replica) {
        partition.remove(replica);

        if (partition.getReplicas().isEmpty()) {
            partitions.remove(partition);
        }
    }

    public static MovablePartitions from(final LogDirs logDirs, final Status status) {
        final var partitions = new HashMap<String, Partition>();

        logDirs.getBrokers().stream()
                .filter(b -> status.getBroker(b.getBroker()).isOverloaded())
                .forEach(b -> b.getLogDirs().stream()
                        .flatMap(l -> l.getPartitions().stream())
                        .forEach(p -> registerPartition(partitions, b, p)));

        return MovablePartitions.builder()
                .partitions(new TreeSet<>(partitions.values()))
                .build();
    }

    private static void registerPartition(final Map<String, Partition> partitions,
                                          final LogDirs.Broker broker, final LogDirs.Partition partition) {
        partitions.computeIfAbsent(partition.getPartition(), dummy -> Partition.builder()
                .id(partition.getPartition())
                .size(partition.getSize())
                .replicas(new HashSet<>())
                .build()).getReplicas().add(broker.getBroker());
    }

    @Value
    @Builder
    public static class Partition implements Comparable<Partition> {

        String id;
        double size;
        Set<Integer> replicas;

        @Override
        public int compareTo(final Partition other) {
            return -Double.compare(size, other.size);
        }

        public void remove(final int replica) {
            if (!replicas.contains(replica)) {
                throw new BrodRebalancerException(String.format("Replica %d is not movable in partition: %s", replica, id));
            }

            replicas.remove(replica);
        }

        // TODO: Implement id-based equals & hashcode.
    }
}
