package com.github.davgarcia.brodrebalancer.adapter.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.PartitionsReassignments;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReassignPartitionsFileAdapterTest {

    private final ReassignPartitionsFileAdapter sut = new ReassignPartitionsFileAdapter();

    @Test
    void givenPartitionReassignmentThenSaveIt() throws IOException {
        final var path = File.createTempFile("test", ".json").getPath();
        final var reassignments = PartitionsReassignments.builder()
                .version(1)
                .partitions(List.of(PartitionsReassignments.Partition.builder()
                                .topic("topic-x")
                                .partition(1)
                                .replicas(List.of(1, 2, 3))
                        .build()))
                .build();

        sut.getCliOptions().setOutputPath(path);
        sut.save(reassignments);

        final var result = new ObjectMapper().readValue(new File(path), PartitionsReassignments.class);
        assertThat(result).isEqualTo(reassignments);
    }

    @Test
    void givenNoOutputOptionThenFail() {
        sut.getCliOptions().setOutputPath(null);

        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.save(PartitionsReassignments.builder().build()));
    }
}
