package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Test
    void givenValidBrokersConfigThenSuccess() throws URISyntaxException {
        final var config = BrokersConfig.builder()
                .brokers(List.of(
                        BrokersConfig.BrokerConfig.builder().id(1).capacity(1.0).build(),
                        BrokersConfig.BrokerConfig.builder().id(2).capacity(2.0).build()))
                .build();
        final var path = Path.of(getClass().getResource("/log-dirs.json").toURI()).toString();
        final var logDirsInput = new LogDirsFileAdapter();
        logDirsInput.getCliOptions().setInputPath(path);
        final var logDirs = logDirsInput.load();

        final var result = Status.from(config, logDirs);
        result.print();

        assertThat(result.getBrokers()).containsExactly(
                Status.BrokerStatus.builder()
                        .id(1)
                        .capacity(1.0)
                        .goalSize(12.0)
                        .currentSize(6.0)
                        .replicasBySize(List.of(
                                Status.Replica.builder().partition("topic-a-1").size(1.0).build(),
                                Status.Replica.builder().partition("topic-a-2").size(2.0).build(),
                                Status.Replica.builder().partition("topic-b-1").size(3.0).build()))
                        .build(),
                Status.BrokerStatus.builder()
                        .id(2)
                        .capacity(2.0)
                        .goalSize(24.0)
                        .currentSize(30.0)
                        .replicasBySize(List.of(
                                Status.Replica.builder().partition("topic-c-1").size(4.0).build(),
                                Status.Replica.builder().partition("topic-b-4").size(5.0).build(),
                                Status.Replica.builder().partition("topic-b-3").size(6.0).build(),
                                Status.Replica.builder().partition("topic-b-2").size(7.0).build(),
                                Status.Replica.builder().partition("topic-a-3").size(8.0).build()))
                        .build());
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
