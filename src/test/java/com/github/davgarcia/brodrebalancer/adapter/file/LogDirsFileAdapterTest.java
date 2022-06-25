package com.github.davgarcia.brodrebalancer.adapter.file;

import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.LogDirs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogDirsFileAdapterTest {

    private final LogDirsFileAdapter sut = new LogDirsFileAdapter();

    @Test
    void givenValidLogDirsThenLoadIt() throws URISyntaxException {
        final var path = Path.of(getClass().getResource("/log-dirs.json").toURI()).toString();

        sut.getCliOptions().setInputPath(path);
        final var result = sut.load();

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getBrokers()).containsExactly(
                LogDirs.Broker.builder()
                        .broker(1)
                        .logDirs(List.of(
                                LogDirs.LogDir.builder()
                                        .logDir("/opt/kafka/data")
                                        .partitions(List.of(
                                                LogDirs.Partition.builder().partition("topic-a-1").size(1).build(),
                                                LogDirs.Partition.builder().partition("topic-a-2").size(2).build(),
                                                LogDirs.Partition.builder().partition("topic-b-1").size(6).build()))
                                        .build()))
                        .build(),
                LogDirs.Broker.builder()
                        .broker(2)
                        .logDirs(List.of(
                                LogDirs.LogDir.builder()
                                        .logDir("/opt/kafka/data-1")
                                        .partitions(List.of(
                                                LogDirs.Partition.builder().partition("topic-b-1").size(6).build(),
                                                LogDirs.Partition.builder().partition("topic-b-2").size(7).build()))
                                        .build(),
                                LogDirs.LogDir.builder()
                                        .logDir("/opt/kafka/data-2")
                                        .partitions(List.of(
                                                LogDirs.Partition.builder().partition("topic-a-2").size(2).build(),
                                                LogDirs.Partition.builder().partition("topic-c-1").size(4).build()))
                                        .build()))
                        .build(),
                LogDirs.Broker.builder()
                        .broker(3)
                        .logDirs(List.of(
                                LogDirs.LogDir.builder()
                                        .logDir("/opt/kafka/data")
                                        .partitions(List.of(
                                                LogDirs.Partition.builder().partition("topic-a-1").size(1).build(),
                                                LogDirs.Partition.builder().partition("topic-b-2").size(7).build(),
                                                LogDirs.Partition.builder().partition("topic-c-1").size(4).build()))
                                        .build()))
                        .build());
    }

    @Test
    void givenNoInputOptionThenFail() {
        sut.getCliOptions().setInputPath(null);

        Assertions.assertThrows(BrodRebalancerException.class, sut::load);
    }
}
