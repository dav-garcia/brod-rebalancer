package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.Parameter;
import com.github.davgarcia.brodrebalancer.config.BrokersConfigLoader;
import com.github.davgarcia.brodrebalancer.config.CliOptionsParser;
import com.github.davgarcia.brodrebalancer.config.Registry;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

public class Main {

    private static final String HEADER =
            "Builds a smart partition reassignment plan for Kafka" + System.lineSeparator() +
            "taking into account brokers capacity and partition sizes." + System.lineSeparator();

    public static void main(String[] args) {
        final var registry = new Registry();
        final var cliOptions = new CliOptions();

        final var optionsParser = new CliOptionsParser("brod-rebalancer", HEADER);
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
            return;
        }

        final var config = new BrokersConfigLoader().loadFromPath(Path.of(cliOptions.getBrokers()));
        final var input = registry.getLogDirsInput(cliOptions.getInput());
        final var output = registry.getAssignmentsOutput(cliOptions.getOutput());
        final var rebalancer = registry.getRebalancer(cliOptions.getRebalancer());
        final var srcBrokerStrategy = registry.getSourceBrokerStrategy(cliOptions.getSrcBrokerStrategy());
        final var dstBrokerStrategy = registry.getDestinationBrokerStrategy(cliOptions.getDstBrokerStrategy());
        final var checker = new Checker();

        rebalancer.setBrokerStrategies(srcBrokerStrategy, dstBrokerStrategy);

        final var logDirs = input.load();
        final var reassignments = rebalancer.rebalance(config, logDirs);
        checker.check(Assignments.from(logDirs), reassignments);
        output.save(reassignments);
    }

    @Getter
    public static class CliOptions {

        @Setter
        @Parameter(names = "--help", help = true, description = "Print this help.")
        private boolean help = false;

        @Parameter(names = "--brokers", required = true, description = "Brokers' capacity definition file location.")
        private String brokers = "brokers.json";

        @Parameter(names = "--input", description = "Type of log dirs input loader.")
        private String input = "file";

        @Parameter(names = "--output", description = "Type of reassignments output saver.")
        private String output = "file";

        @Parameter(names = "--rebalancer", description = "Type of rebalancer algorithm.")
        private String rebalancer = "ffd";

        @Parameter(names = "--src-broker-strategy", description = "How to choose the source broker holding the replica to be moved.")
        public String srcBrokerStrategy = "random";

        @Parameter(names = "--dst-broker-strategy", description = "How to choose the destination broker where the replica will be moved.")
        public String dstBrokerStrategy = "random-free";
    }
}
