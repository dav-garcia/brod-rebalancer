package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.Parameter;
import lombok.Getter;

import java.nio.file.Path;

public class Main {

    private static final String HEADER =
            "Builds a smart partition reassignment plan for Kafka" + System.lineSeparator() +
            "taking into account brokers capacity and partition sizes." + System.lineSeparator();

    public static void main(String[] args) {
        final var registry = new Registry();
        final var cliOptions = new CliOptions();
        final var optionsParser = new CliOptionsParser("brod-rebalancer");

        optionsParser.addAllCliOptions(registry);
        optionsParser.addCliOptions(cliOptions);
        optionsParser.parse(args);
        if (cliOptions.isHelp()) {
            System.out.println(HEADER);
            optionsParser.printUsage();
            return;
        }

        final var config = new BrokersConfigLoader().loadFromPath(Path.of(cliOptions.getBrokers()));
        final var input = registry.getLogDirsInput(cliOptions.getInput());
        final var output = registry.getReassignmentsOutput(cliOptions.getOutput());
        final var rebalancer = registry.getRebalancer(cliOptions.getRebalancer());

        final var logDirs = input.load();
        final var reassignments = rebalancer.rebalance(config, logDirs);
        output.save(reassignments);
    }

    @Getter
    public static class CliOptions {

        @Parameter(names = "--help", help = true, description = "Print this help.")
        private boolean help = false;

        @Parameter(names = "--brokers", required = true, description = "Brokers' capacity definition file location.")
        private String brokers = "brokers.json";

        @Parameter(names = "--input", description = "Type of log dirs input loader.")
        private String input = "file";

        @Parameter(names = "--output", description = "Type of reassignments output saver.")
        private String output = "file";

        @Parameter(names = "--rebalancer", description = "Type of rebalancer algorithm.")
        private String rebalancer = "simple";
    }
}
