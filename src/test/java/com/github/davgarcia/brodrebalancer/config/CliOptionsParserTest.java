package com.github.davgarcia.brodrebalancer.config;

import com.beust.jcommander.Parameter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CliOptionsParserTest {

    private final CliOptionsParser sut = new CliOptionsParser("test", "This is a test");

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
        final var registered1 = new Registered<>() {

            @Override
            public String getName() {
                return "test1";
            }

            @Override
            public String getHelp() {
                return null;
            }

            @Override
            public Object getCliOptions() {
                return new Object();
            }
        };
        final var registered2 = new Registered<>() {

            @Override
            public String getName() {
                return "test2";
            }

            @Override
            public String getHelp() {
                return null;
            }

            @Override
            public Object getCliOptions() {
                return new Object();
            }
        };
        final var registry = mock(Registry.class);
        doReturn(List.of(registered1, registered2)).when(registry).getAllRegistered();

        sut.clearCliOptions();
        sut.addAllCliOptions(registry);

        assertThat(sut.getCliOptions()).hasSize(2);
    }
}
