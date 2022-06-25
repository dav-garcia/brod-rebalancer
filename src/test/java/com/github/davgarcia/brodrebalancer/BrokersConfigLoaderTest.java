package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BrokersConfigLoaderTest {

    private final BrokersConfigLoader sut = new BrokersConfigLoader();

    @Test
    void givenValidConfigThenLoadIt() throws URISyntaxException {
        final var path = Path.of(getClass().getResource("/valid-brokers-config.json").toURI());

        final var result = sut.loadFromPath(path);

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getBrokers()).containsExactly(
                new BrokersConfig.BrokerConfig(1, 1.5),
                new BrokersConfig.BrokerConfig(2, 1.0),
                new BrokersConfig.BrokerConfig(3, 1.0),
                new BrokersConfig.BrokerConfig(4, 2.0),
                new BrokersConfig.BrokerConfig(5, 1.5));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/invalid-version-config.json",
            "/invalid-brokers-config.json",
            "/invalid-broker-config.json"
    })
    void givenInvalidConfigThenFail(final String stringPath) throws URISyntaxException {
        final var path = Path.of(getClass().getResource(stringPath).toURI());
        Assertions.assertThrows(BrodRebalancerException.class, () -> sut.loadFromPath(path));
    }
}
