package com.github.davgarcia.brodrebalancer.config;

import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationLoaderTest {

    private final ConfigurationLoader sut = new ConfigurationLoader();

    @Test
    void givenValidConfigThenLoadIt() throws URISyntaxException {
        final var path = Path.of(getClass().getResource("/valid-brokers-config.json").toURI());

        final var result = sut.loadFromPath(path);

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getBrokers()).containsExactly(
                new Configuration.Broker(1, 1.5),
                new Configuration.Broker(2, 1.0),
                new Configuration.Broker(3, 1.0),
                new Configuration.Broker(4, 2.0),
                new Configuration.Broker(5, 1.5));
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

    @Test
    void givenValidTopicsThenLoadThem() throws URISyntaxException {
        final var path = Path.of(getClass().getResource("/valid-topics-config.json").toURI());

        final var result = sut.loadFromPath(path);

        assertThat(result.getTopics().getInclude()).isNullOrEmpty();
        assertThat(result.getTopics().getExclude()).containsExactly("c");
    }

    @Test
    void givenInvalidTopicsThenLoadThem() throws URISyntaxException {
        final var path = Path.of(getClass().getResource("/invalid-topics-config.json").toURI());

        Assertions.assertThrows(BrodRebalancerException.class, () -> sut.loadFromPath(path));
    }
}
