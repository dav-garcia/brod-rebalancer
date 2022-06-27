package com.github.davgarcia.brodrebalancer.config;

import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.ReassignmentsFileAdapter;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomDestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.brokerstrategy.RandomSourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.rebalancer.FirstFitDecreasingRebalancer;
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
        final var result = sut.getRebalancer("ffd");

        assertThat(result).isInstanceOf(FirstFitDecreasingRebalancer.class);
    }

    @Test
    void givenInvalidRebalancerNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getRebalancer("invalid"));
    }

    @Test
    void givenValidSourceBrokerStrategyNameThenReturnIt() {
        final var result = sut.getSourceBrokerStrategy("random");

        assertThat(result).isInstanceOf(RandomSourceBrokerStrategy.class);
    }

    @Test
    void givenInvalidSourceBrokerStrategyNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getSourceBrokerStrategy("invalid"));
    }

    @Test
    void givenValidDestinationBrokerStrategyNameThenReturnIt() {
        final var result = sut.getDestinationBrokerStrategy("random");

        assertThat(result).isInstanceOf(RandomDestinationBrokerStrategy.class);
    }

    @Test
    void givenInvalidDestinationBrokerStrategyNameThenFail() {
        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.getDestinationBrokerStrategy("invalid"));
    }
}
