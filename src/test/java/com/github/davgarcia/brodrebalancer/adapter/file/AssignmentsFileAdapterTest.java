package com.github.davgarcia.brodrebalancer.adapter.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.Assignments;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentsFileAdapterTest {

    private final AssignmentsFileAdapter sut = new AssignmentsFileAdapter();

    @Test
    void givenPartitionAssignmentThenSaveIt() throws IOException {
        final var path = File.createTempFile("test", ".json").getPath();
        final var assignments = Assignments.builder()
                .version(1)
                .partitions(List.of(Assignments.Partition.builder()
                                .topic("topic-x")
                                .partition(1)
                                .replicas(List.of(1, 2, 3))
                        .build()))
                .build();

        sut.getCliOptions().setOutputPath(path);
        sut.save(assignments);

        final var result = new ObjectMapper().readValue(new File(path), Assignments.class);
        assertThat(result).isEqualTo(assignments);
    }

    @Test
    void givenNoOutputOptionThenFail() {
        sut.getCliOptions().setOutputPath(null);

        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.save(Assignments.builder().build()));
    }
}
