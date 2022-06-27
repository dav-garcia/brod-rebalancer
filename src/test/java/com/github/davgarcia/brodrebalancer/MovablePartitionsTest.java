package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class MovablePartitionsTest {

    @Test
    void givenValidInfoThenSuccess() {
        final var config = ObjectMother.newConfig();
        final var logDirs = ObjectMother.newLogDirs();
        final var status = Status.from(config, logDirs);

        final var result = MovablePartitions.from(logDirs, status);

        assertThat(result.getPartitions()).containsExactly(
                MovablePartitions.Partition.builder().id("topic-b-2").size(7.0).replicas(Set.of(2)).build(),
                MovablePartitions.Partition.builder().id("topic-b-1").size(6.0).replicas(Set.of(2)).build(),
                MovablePartitions.Partition.builder().id("topic-c-1").size(4.0).replicas(Set.of(2)).build(),
                MovablePartitions.Partition.builder().id("topic-a-2").size(2.0).replicas(Set.of(2)).build());
    }

    @Test
    void givenPartitionsThenFindLargest() {
        final var partition1 = MovablePartitions.Partition.builder().id("test-1").size(1.0).build();
        final var partition2 = MovablePartitions.Partition.builder().id("test-2").size(2.0).build();
        final var partition3 = MovablePartitions.Partition.builder().id("test-3").size(3.0).build();
        final var sut = MovablePartitions.builder()
                .partitions(new TreeSet<>(Set.of(
                        partition1,
                        partition2,
                        partition3)))
                .build();

        final var result = sut.findLargest(2.0);

        assertThat(result.orElseThrow()).isEqualTo(partition2);
    }

    @Test
    void givenNoPartitionsThenFindNothing() {
        final var sut = MovablePartitions.builder()
                .partitions(new TreeSet<>())
                .build();

        final var result = sut.findLargest(10.0);

        assertThat(result).isNotPresent();
    }
}
