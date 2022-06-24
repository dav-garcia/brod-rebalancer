package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.adapter.file.ReassignPartitionsFileAdapter;
import lombok.Getter;

import java.nio.file.Path;

public class Main {

    private static final String HEADER =
            "Builds a smart partition reassignment plan for Kafka" + System.lineSeparator() +
            "taking into account brokers capacity and partition sizes." + System.lineSeparator();

    public static void main(String[] args) {
        final var logDirsInput = new LogDirsFileAdapter();
        final var reassignPartitionsOutput = new ReassignPartitionsFileAdapter();

        final var cliOptions = new CliOptions();
        final var optionsParser = JCommander.newBuilder()
                .addObject(cliOptions)
                .addObject(logDirsInput.getCliOptions())
                .addObject(reassignPartitionsOutput.getCliOptions())
                .programName("brod-rebalancer")
                .build();

        optionsParser.parse(args);
        if (cliOptions.isHelp()) {
            System.out.println(HEADER);
            optionsParser.usage();
            return;
        }

        final var path = Path.of(cliOptions.getConfig());
        final var config = new ConfigurationLoader().loadFromPath(path);
        final var logDirs = logDirsInput.load();
    }

    @Getter
    public static class CliOptions {

        @Parameter(names = "--help", help = true, description = "Print this help.")
        private boolean help = false;

        @Parameter(names = "--config", description = "Configuration file location.")
        private String config = "config.json";
    }
}
