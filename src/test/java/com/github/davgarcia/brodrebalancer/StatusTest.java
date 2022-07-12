package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    // BEWARE: These constants cannot be directly used in tests that change their status!
    private static final Status.Broker BROKER_1 = Status.Broker.builder()
            .id(1)
            .goalSize(7.0)
            .currentSize(14.0)
            .partitions(Set.of("test-1", "test-2", "test-3"))
            .build();
    private static final Status.Broker BROKER_2 = Status.Broker.builder()
            .id(2)
            .goalSize(7.0)
            .currentSize(3.0)
            .partitions(Set.of("test-1"))
            .build();
    private static final Status.Broker BROKER_3 = Status.Broker.builder()
            .id(3)
            .goalSize(7.0)
            .currentSize(4.0)
            .partitions(Set.of("test-2"))
            .build();
    private static final Status.Broker BROKER_4 = Status.Broker.builder()
            .id(4)
            .goalSize(7.0)
            .currentSize(7.0)
            .partitions(Set.of("test-3"))
            .build();
    private static final Status SUT = Status.builder()
            .brokers(new TreeMap<>(Map.of(
                    1, BROKER_1,
                    2, BROKER_2,
                    3, BROKER_3,
                    4, BROKER_4)))
            .build();

    @Test
    void givenValidBrokersConfigThenSuccess() {
        final var config = ObjectMother.newConfig();
        final var logDirs = ObjectMother.newLogDirs();

        final var result = Status.from(config, logDirs);
        result.print();

        final var expectedBrokers = new TreeMap<Integer, Status.Broker>();
        expectedBrokers.put(1, Status.Broker.builder()
                .id(1)
                .capacity(1.0)
                .goalSize(10.0)
                .currentSize(9.0)
                .partitions(Set.of("topic-a-1", "topic-a-2", "topic-b-1"))
                .build());
        expectedBrokers.put(2, Status.Broker.builder()
                .id(2)
                .capacity(1.0)
                .goalSize(10.0)
                .currentSize(19.0)
                .partitions(Set.of("topic-b-1", "topic-b-2", "topic-a-2", "topic-c-1"))
                .build());
        expectedBrokers.put(3, Status.Broker.builder()
                .id(3)
                .capacity(2.0)
                .goalSize(20.0)
                .currentSize(12.0)
                .partitions(Set.of("topic-a-1", "topic-b-2", "topic-c-1"))
                .build());
        assertThat(result.getBrokers()).containsExactlyEntriesOf(expectedBrokers);
    }

    @Test
    void givenInvalidBrokersConfigThenFail() {
        final var config = Configuration.builder()
                .brokers(List.of(
                        Configuration.BrokerConfig.builder().id(1).build()))
                .build();
        final var logDirs = LogDirs.builder()
                .brokers(List.of(
                        LogDirs.Broker.builder().broker(1).build(),
                        LogDirs.Broker.builder().broker(2).build()))
                .build();

        Assertions.assertThrows(BrodRebalancerException.class,
                () -> Status.from(config, logDirs));
    }

    @Test
    void givenBrokerIdsThenReturnBrokers() {
        final var result = SUT.getBrokers(Set.of(1, 3));

        assertThat(result).containsExactlyInAnyOrder(BROKER_1, BROKER_3);
    }

    @Test
    void givenPartitionThenFindDestinations() {
        final var result = SUT.findDestinations("test-1");

        assertThat(result).containsExactlyInAnyOrder(BROKER_3);
    }

    @Test
    void givenValidSourceAndDestinationThenMovePartition() {
        final var broker1 = Status.Broker.from(BROKER_1);
        final var broker2 = Status.Broker.from(BROKER_2);
        final var sut = Status.builder()
                .brokers(new TreeMap<>(Map.of(
                        1, broker1,
                        2, broker2,
                        3, BROKER_3,
                        4, BROKER_4)))
                .build();

        sut.move(broker1, broker2, "test-2", 4);

        assertThat(broker1.getId()).isEqualTo(1);
        assertThat(broker1.getGoalSize()).isEqualTo(7.0);
        assertThat(broker1.getCurrentSize()).isEqualTo(10.0);
        assertThat(broker1.getPartitions()).containsExactlyInAnyOrder("test-1", "test-3");
        assertThat(broker2.getId()).isEqualTo(2);
        assertThat(broker2.getGoalSize()).isEqualTo(7.0);
        assertThat(broker2.getCurrentSize()).isEqualTo(7.0);
        assertThat(broker2.getPartitions()).containsExactlyInAnyOrder("test-1", "test-2");
    }

    @Test
    void givenInvalidSourceThenFailMovePartition() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> SUT.move(BROKER_3, BROKER_4, "test-1", 3.0));
    }

    @Test
    void givenInvalidDestinationThenFailMovePartition() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> SUT.move(BROKER_3, BROKER_2, "test-1", 3.0));
    }

    @Test
    void givenBrokersThenComputeMaxDiff() {
        final var result = SUT.computeMaxDiff();

        assertThat(result).isEqualTo(4.0);
    }

    @Test
    void givenBrokersThenComputeGap() {
        final var result = SUT.computeGap();

        assertThat(result).isEqualTo(14.0);
    }
}
