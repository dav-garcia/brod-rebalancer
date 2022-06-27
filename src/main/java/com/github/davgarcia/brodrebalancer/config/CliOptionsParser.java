package com.github.davgarcia.brodrebalancer.config;

import com.beust.jcommander.JCommander;

import java.util.List;

public class CliOptionsParser {

    private final JCommander parser;
    private final String header;

    public CliOptionsParser(final String name, final String header) {
        parser = JCommander.newBuilder()
                .programName(name)
                .build();
        this.header = header;
    }

    public void addCliOptions(final Object cliOptions) {
        parser.addObject(cliOptions);
    }

    public void addAllCliOptions(final Registry registry) {
        registry.getAllRegistered().stream()
                .map(Registered::getCliOptions)
                .forEach(parser::addObject);
    }

    public void clearCliOptions() {
        parser.getObjects().clear();
    }

    public List<Object> getCliOptions() {
        return parser.getObjects();
    }

    public void parse(final String[] args) {
        parser.parse(args);
    }

    public void printUsage() {
        System.out.println(header);
        parser.usage();
    }
}
