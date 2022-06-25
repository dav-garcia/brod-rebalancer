package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.JCommander;

public class CliOptionsParser {

    private final JCommander parser;

    public CliOptionsParser(final String name) {
        parser = JCommander.newBuilder()
                .programName(name)
                .build();
    }

    public void addCliOptions(final Object cliOptions) {
        parser.addObject(cliOptions);
    }

    public void addAllCliOptions(final Registry registry) {
        registry.getAllLogDirsInputs().stream()
                .map(LogDirsInput::getCliOptions)
                .forEach(parser::addObject);
        registry.getAllReassignmentsOutputs().stream()
                .map(ReassignmentsOutput::getCliOptions)
                .forEach(parser::addObject);
        registry.getAllRebalancers().stream()
                .map(Rebalancer::getCliOptions)
                .forEach(parser::addObject);
    }

    public void parse(final String[] args) {
        parser.parse(args);
    }

    public void printUsage() {
        parser.usage();
    }
}
