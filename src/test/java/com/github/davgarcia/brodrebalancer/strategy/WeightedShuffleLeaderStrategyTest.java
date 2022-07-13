package com.github.davgarcia.brodrebalancer.strategy;

import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.config.Configuration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class WeightedShuffleLeaderStrategyTest {

    private final WeightedShuffleLeaderStrategy sut = new WeightedShuffleLeaderStrategy();

    @Test
    void givenAssignmentsAndUnsortedWeightsThenSuffleReplicas() {
        final var config = Configuration.builder()
                .brokers(List.of(
                        Configuration.Broker.builder().id(5).capacity(0.5).build(),
                        Configuration.Broker.builder().id(4).capacity(3.0).build(),
                        Configuration.Broker.builder().id(3).capacity(0.1).build(),
                        Configuration.Broker.builder().id(2).capacity(2.0).build(),
                        Configuration.Broker.builder().id(1).capacity(1.0).build()))
                .build();
        final var assignments = Assignments.builder()
                .partitions(List.of(
                        Assignments.Partition.builder().replicas(new ArrayList<>(List.of(1, 2, 3))).build(),
                        Assignments.Partition.builder().replicas(new ArrayList<>(List.of(1, 3, 5))).build(),
                        Assignments.Partition.builder().replicas(new ArrayList<>(List.of(3, 4, 5))).build()))
                .build();
        final var random = mock(Random.class);
        doReturn(1.0).when(random).nextDouble();

        sut.electLeaders(config, assignments, random);

        assertThat(assignments.getPartitions()).containsExactly(
                Assignments.Partition.builder().replicas(List.of(2, 1, 3)).build(),
                Assignments.Partition.builder().replicas(List.of(1, 5, 3)).build(),
                Assignments.Partition.builder().replicas(List.of(4, 5, 3)).build());
    }
}
