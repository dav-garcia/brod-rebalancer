package com.github.davgarcia.brodrebalancer.rebalancer;

import com.github.davgarcia.brodrebalancer.ObjectMother;
import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomFreeDestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomSourceBrokerStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FirstFitDecreasingRebalancerTest {

    private final FirstFitDecreasingRebalancer sut = new FirstFitDecreasingRebalancer();

    @Test
    void givenValidConfigAndInputThenReturnOutput() {
        final var config = ObjectMother.newConfig();
        final var logDirs = ObjectMother.newLogDirs();

        sut.setBrokerStrategies(new RandomSourceBrokerStrategy(), new RandomFreeDestinationBrokerStrategy());
        final var result = sut.rebalance(config, logDirs);

        assertThat(result).isEqualTo(Assignments.builder()
                .version(1)
                .partitions(List.of(
                        Assignments.Partition.builder().topic("topic-a").partition(1).replicas(List.of(1, 3)).build(),
                        Assignments.Partition.builder().topic("topic-a").partition(2).replicas(List.of(1, 3)).build(),
                        Assignments.Partition.builder().topic("topic-b").partition(1).replicas(List.of(1, 3)).build(),
                        Assignments.Partition.builder().topic("topic-b").partition(2).replicas(List.of(2, 3)).build(),
                        Assignments.Partition.builder().topic("topic-c").partition(1).replicas(List.of(2, 3)).build()))
                .build());
    }
}
