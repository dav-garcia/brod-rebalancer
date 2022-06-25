package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.Parameter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CliOptionsParserTest {

    private final CliOptionsParser sut = new CliOptionsParser("test");

    @Test
    void givenCliOptionsThenAddIt() {
        final var cliOptions = new Object() {

            @Parameter(names = "--test")
            private String test;
        };

        sut.clearCliOptions();
        sut.addCliOptions(cliOptions);
        sut.parse(new String[] {"--test", "test"});

        assertThat(sut.getCliOptions()).hasSize(1);
        assertThat(cliOptions.test).isEqualTo("test");
    }

    @Test
    void givenRegistryThenAddAllCliOptions() {
        final var registry = new Registry();

        sut.clearCliOptions();
        sut.addAllCliOptions(registry);

        assertThat(sut.getCliOptions()).hasSize(3);
    }
}
