package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentsTest {

    @Test
    void givenLogDirsThenReturnAssignments() {
        final var logDirs = LogDirs.builder()
                .brokers(List.of(
                        LogDirs.Broker.builder().broker(1).logDirs(List.of(
                                LogDirs.LogDir.builder().partitions(List.of(
                                        LogDirs.Partition.builder().partition("test-1").build(),
                                        LogDirs.Partition.builder().partition("other-1").build(),
                                        LogDirs.Partition.builder().partition("test-2").build()
                                )).build()
                        )).build(),
                        LogDirs.Broker.builder().broker(2).logDirs(List.of(
                                LogDirs.LogDir.builder().partitions(List.of(
                                        LogDirs.Partition.builder().partition("test-1").build()
                                )).build()
                        )).build(),
                        LogDirs.Broker.builder().broker(3).logDirs(List.of(
                                LogDirs.LogDir.builder().partitions(List.of(
                                        LogDirs.Partition.builder().partition("test-2").build()
                                )).build()
                        )).build()
                )).build();

        final var result = Assignments.from(logDirs);

        doAssert(result);
    }

    @Test
    void givenStatusThenReturnAssignments() {
        final var status = Status.builder()
                .brokers(new TreeMap<>(Map.of(
                        1, Status.Broker.builder().id(1).partitions(Set.of("test-1", "other-1", "test-2")).build(),
                        2, Status.Broker.builder().id(2).partitions(Set.of("test-1")).build(),
                        3, Status.Broker.builder().id(3).partitions(Set.of("test-2")).build())))
                .build();

        final var result = Assignments.from(status);

        doAssert(result);
    }

    private void doAssert(final Assignments result) {
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getPartitions()).containsExactly(
                Assignments.Partition.builder().topic("other").partition(1).replicas(List.of(1)).build(),
                Assignments.Partition.builder().topic("test").partition(1).replicas(List.of(1, 2)).build(),
                Assignments.Partition.builder().topic("test").partition(2).replicas(List.of(1, 3)).build());
    }
}
