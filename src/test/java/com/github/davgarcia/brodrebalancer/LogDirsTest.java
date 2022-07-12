package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LogDirsTest {

    @Test
    void givenIncludedTopicsThenIncludeThem() {
        final var sut = ObjectMother.newLogDirs();

        sut.filterTopics(Set.of("topic-a"), null);
        final var result = getPartitions(sut);

        assertThat(result).containsExactly("topic-a-1", "topic-a-2", "topic-a-2", "topic-a-1");
    }

    @Test
    void givenExcludedTopicsThenExcludeThem() {
        final var sut = ObjectMother.newLogDirs();

        sut.filterTopics(null, Set.of("topic-a"));
        final var result = getPartitions(sut);

        assertThat(result).containsExactly("topic-b-1", "topic-b-1", "topic-b-2", "topic-c-1", "topic-b-2", "topic-c-1");
    }

    private List<String> getPartitions(final LogDirs logDirs) {
        return logDirs.getBrokers().stream()
                .flatMap(b -> b.getLogDirs().stream())
                .flatMap(l -> l.getPartitions().stream())
                .map(LogDirs.Partition::getPartition)
                .collect(Collectors.toList());
    }
}
