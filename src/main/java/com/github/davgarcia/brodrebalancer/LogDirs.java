package com.github.davgarcia.brodrebalancer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

@Value
@Builder
@SuppressWarnings("java:S1700") // The field names are intentionally the same as their containing classes.
public class LogDirs {

    @Min(1)
    @Max(1)
    int version;
    @NotEmpty
    List<@Valid Broker> brokers;

    public void filterTopics(final Set<String> include, final Set<String> exclude) {
        if (CollectionUtils.isNotEmpty(include)) {
            brokers.forEach(b -> b.includeTopics(include));
        } else if (CollectionUtils.isNotEmpty(exclude)) {
            brokers.forEach(b -> b.excludeTopics(exclude));
        }
    }

    @Value
    @Builder
    public static class Broker {

        @Min(0)
        int broker;
        @NotEmpty
        List<@Valid LogDir> logDirs;

        public void includeTopics(final Set<String> include) {
            logDirs.forEach(l -> l.includeTopics(include));
        }

        public void excludeTopics(final Set<String> exclude) {
            logDirs.forEach(l -> l.excludeTopics(exclude));
        }
    }

    @Value
    @Builder
    public static class LogDir {

        @NotEmpty
        String logDir;
        String error;
        List<@Valid Partition> partitions;

        public void includeTopics(final Set<String> include) {
            partitions.removeIf(p -> !include.contains(getTopicName(p.partition)));
        }

        public void excludeTopics(final Set<String> exclude) {
            partitions.removeIf(p -> exclude.contains(getTopicName(p.partition)));
        }

        private String getTopicName(final String partition) {
            return StringUtils.substringBeforeLast(partition, "-");
        }
    }

    @Value
    @Builder
    public static class Partition {

        @NotEmpty
        String partition;
        long size;
        long offsetLag;
        boolean isFuture;
    }
}
