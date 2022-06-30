package com.github.davgarcia.brodrebalancer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@SuppressWarnings("java:S1700") // The field names are intentionally the same as their containing classes.
public class LogDirs {

    @Min(1)
    @Max(1)
    int version;
    @NotEmpty
    List<@Valid Broker> brokers;

    @Value
    @Builder
    public static class Broker {

        @Min(0)
        int broker;
        @NotEmpty
        List<@Valid LogDir> logDirs;
    }

    @Value
    @Builder
    public static class LogDir {

        @NotEmpty
        String logDir;
        String error;
        List<@Valid Partition> partitions;
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
