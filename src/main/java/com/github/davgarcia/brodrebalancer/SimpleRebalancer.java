package com.github.davgarcia.brodrebalancer;

public class SimpleRebalancer implements Rebalancer<SimpleRebalancer.CliOptions> {

    private final CliOptions cliOptions;

    public SimpleRebalancer() {
        cliOptions = new CliOptions();
    }

    @Override
    public String getName() {
        return "simple";
    }

    @Override
    public CliOptions getCliOptions() {
        return cliOptions;
    }

    @Override
    public Reassignments rebalance(final BrokersConfig config, final LogDirs logDirs) {
        return null;
    }

    public static class CliOptions {

    }
}
