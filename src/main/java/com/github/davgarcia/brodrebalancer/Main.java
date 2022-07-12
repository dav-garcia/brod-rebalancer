package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.Parameter;
import com.github.davgarcia.brodrebalancer.config.CliOptionsParser;
import com.github.davgarcia.brodrebalancer.config.ConfigurationLoader;
import com.github.davgarcia.brodrebalancer.config.Registry;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.logging.LogManager;

public class Main {

    private static final String NAME = "brod-rebalancer";
    private static final String HEADER =
            "Builds a smart partition reassignment plan for Kafka" + System.lineSeparator() +
            "taking into account brokers capacity and partition sizes." + System.lineSeparator();

    public static void main(String[] args) {
        LogManager.getLogManager().reset(); // Disable logging.

        final var registry = new Registry();
        final var cliOptions = new CliOptions();
        if (!parseOptions(registry, cliOptions, args)) {
            return;
        }
        run(registry, cliOptions);
    }

    private static boolean parseOptions(final Registry registry, final CliOptions cliOptions, final String[] args) {
        final var optionsParser = new CliOptionsParser(NAME, HEADER);
        optionsParser.addAllCliOptions(registry);
        optionsParser.addCliOptions(cliOptions);
        try {
            optionsParser.parse(args);
        } catch (BrodRebalancerException e) {
            System.out.println(e.getMessage());
            System.out.println();
            cliOptions.setHelp(true);
        }
        if (cliOptions.isHelp() ) {
            optionsParser.printUsage();
            registry.printUsage();
            return false;
        }
        return true;
    }

    private static void run(final Registry registry, final CliOptions cliOptions) {
        final var config = new ConfigurationLoader().loadFromPath(Path.of(cliOptions.getConfig()));
        final var input = registry.getLogDirsInput(cliOptions.getInput());
        final var output = registry.getAssignmentsOutput(cliOptions.getOutput());
        final var rebalancer = registry.getRebalancer(cliOptions.getRebalancer());
        final var srcBrokerStrategy = registry.getSourceBrokerStrategy(cliOptions.getSrcBrokerStrategy());
        final var dstBrokerStrategy = registry.getDestinationBrokerStrategy(cliOptions.getDstBrokerStrategy());
        final var leaderStrategy = registry.getLeaderStrategy(cliOptions.getLeaderStrategy());

        new Runner(config, input, output, rebalancer, srcBrokerStrategy, dstBrokerStrategy, leaderStrategy).run();
    }

    @Getter
    public static class CliOptions {

        @Setter
        @Parameter(names = "--help", help = true, description = "Print this help.")
        private boolean help = false;

        @Parameter(names = "--config", required = true, description = "Location of Brokers' capacity definition and other configurations.")
        private String config = "config.json";

        @Parameter(names = "--input", description = "Type of log dirs input loader.")
        private String input = "file";

        @Parameter(names = "--output", description = "Type of reassignments output saver.")
        private String output = "file";

        @Parameter(names = "--rebalancer", description = "Type of rebalancer algorithm.")
        private String rebalancer = "ffd";

        @Parameter(names = "--src-broker-strategy", description = "How to choose the source broker holding the replica to be moved.")
        public String srcBrokerStrategy = "most-overloaded";

        @Parameter(names = "--dst-broker-strategy", description = "How to choose the destination broker where the replica will be moved.")
        public String dstBrokerStrategy = "random-free";

        @Parameter(names = "--leader-strategy", description = "How the leader replica will be chosen for each partition.")
        public String leaderStrategy = "weighted-shuffle";
    }
}
