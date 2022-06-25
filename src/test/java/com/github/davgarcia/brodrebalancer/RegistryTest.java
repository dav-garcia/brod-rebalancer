package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.ReassignmentsFileAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryTest {

    private final Registry sut = new Registry();

    @Test
    void givenValidInputNameThenReturnIt() {
        final var result = sut.getLogDirsInput("file");

        assertThat(result).isInstanceOf(LogDirsFileAdapter.class);
    }

    @Test
    void givenInvalidInputNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getLogDirsInput("invalid"));
    }

    @Test
    void givenValidOutputNameThenReturnIt() {
        final var result = sut.getReassignmentsOutput("file");

        assertThat(result).isInstanceOf(ReassignmentsFileAdapter.class);
    }

    @Test
    void givenInvalidOutputNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getReassignmentsOutput("invalid"));
    }

    @Test
    void givenValidRebalancerNameThenReturnIt() {
        final var result = sut.getRebalancer("simple");

        assertThat(result).isInstanceOf(SimpleRebalancer.class);
    }

    @Test
    void givenInvalidRebalancerNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getRebalancer("invalid"));
    }
}
