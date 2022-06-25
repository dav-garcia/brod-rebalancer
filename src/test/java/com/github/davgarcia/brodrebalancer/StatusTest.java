package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Test
    void givenValidBrokersConfigThenSuccess() throws URISyntaxException {
        final var config = BrokersConfig.builder()
                .brokers(List.of(
                        BrokersConfig.BrokerConfig.builder().id(1).capacity(1.0).build(),
                        BrokersConfig.BrokerConfig.builder().id(2).capacity(1.0).build(),
                        BrokersConfig.BrokerConfig.builder().id(3).capacity(2.0).build()))
                .build();
        final var path = Path.of(getClass().getResource("/log-dirs.json").toURI()).toString();
        final var logDirsInput = new LogDirsFileAdapter();
        logDirsInput.getCliOptions().setInputPath(path);
        final var logDirs = logDirsInput.load();

        final var result = Status.from(config, logDirs);
        result.print();

        final var expectedBrokers = new TreeMap<Integer, Status.BrokerStatus>();
        final var expectedReplicas1 = new TreeSet<>(Set.of(
                Status.Replica.builder().partition("topic-a-1").size(1.0).build(),
                Status.Replica.builder().partition("topic-a-2").size(2.0).build(),
                Status.Replica.builder().partition("topic-b-1").size(6.0).build()));
        expectedBrokers.put(1, Status.BrokerStatus.builder()
                .id(1)
                .capacity(1.0)
                .goalSize(10.0)
                .currentSize(9.0)
                .replicasBySize(expectedReplicas1)
                .build());
        final var expectedReplicas2 = new TreeSet<>(Set.of(
                Status.Replica.builder().partition("topic-b-1").size(6.0).build(),
                Status.Replica.builder().partition("topic-b-2").size(7.0).build(),
                Status.Replica.builder().partition("topic-a-2").size(2.0).build(),
                Status.Replica.builder().partition("topic-c-1").size(4.0).build()));
        expectedBrokers.put(2, Status.BrokerStatus.builder()
                .id(2)
                .capacity(1.0)
                .goalSize(10.0)
                .currentSize(19.0)
                .replicasBySize(expectedReplicas2)
                .build());
        final var expectedReplicas3 = new TreeSet<>(Set.of(
                Status.Replica.builder().partition("topic-a-1").size(1.0).build(),
                Status.Replica.builder().partition("topic-b-2").size(7.0).build(),
                Status.Replica.builder().partition("topic-c-1").size(4.0).build()));
        expectedBrokers.put(3, Status.BrokerStatus.builder()
                .id(3)
                .capacity(2.0)
                .goalSize(20.0)
                .currentSize(12.0)
                .replicasBySize(expectedReplicas3)
                .build());
        assertThat(result.getBrokers()).containsExactlyEntriesOf(expectedBrokers);

        final var expectedMovableReplicas = new TreeMap<>(Map.of(
                Status.Replica.builder().partition("topic-a-2").size(2.0).build(), Set.of(2),
                Status.Replica.builder().partition("topic-b-1").size(6.0).build(), Set.of(2),
                Status.Replica.builder().partition("topic-b-2").size(7.0).build(), Set.of(2),
                Status.Replica.builder().partition("topic-c-1").size(4.0).build(), Set.of(2)));
        assertThat(result.getMovableReplicas()).containsExactlyEntriesOf(expectedMovableReplicas);
    }

    @Test
    void givenInvalidBrokersConfigThenFail() {
        final var config = BrokersConfig.builder()
                .brokers(List.of(
                        BrokersConfig.BrokerConfig.builder().id(1).build()))
                .build();
        final var logDirs = LogDirs.builder()
                .brokers(List.of(
                        LogDirs.Broker.builder().broker(1).build(),
                        LogDirs.Broker.builder().broker(2).build()))
                .build();

        Assertions.assertThrows(BrodRebalancerException.class,
                () -> Status.from(config, logDirs));
    }
}
