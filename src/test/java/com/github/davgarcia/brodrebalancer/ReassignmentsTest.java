package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReassignmentsTest {

    @Test
    void givenStatusThenReturnReassignments() {
        final var status = Status.builder()
                .brokers(new TreeMap<>(Map.of(
                        1, Status.Broker.builder().id(1).partitions(Set.of("test-1", "other-1", "test-2")).build(),
                        2, Status.Broker.builder().id(2).partitions(Set.of("test-1")).build(),
                        3, Status.Broker.builder().id(3).partitions(Set.of("test-2")).build())))
                .build();

        final var result = Reassignments.from(status);

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getPartitions()).containsExactly(
                Reassignments.Partition.builder().topic("other").partition(1).replicas(List.of(1)).build(),
                Reassignments.Partition.builder().topic("test").partition(1).replicas(List.of(1, 2)).build(),
                Reassignments.Partition.builder().topic("test").partition(2).replicas(List.of(1, 3)).build()
        );
    }
}
