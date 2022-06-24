package com.github.davgarcia.brodrebalancer;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PartitionsReassignments {

    int version;
    List<Partition> partitions;

    @Value
    @Builder
    public static class Partition {

        String topic;
        int partition;
        List<Integer> replicas;
    }
}
