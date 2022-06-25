package com.github.davgarcia.brodrebalancer;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;

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
        final var status = Status.from(config, logDirs);
        double gapBefore;
        double gapAfter = status.computeGap();

        do {
            gapBefore = gapAfter;

            final var toBroker = status.findHighestDiff();
            final var toBeMoved = toBroker.computeDiff();

            // For each broker with excess...
            // Find one replica whose size is closest
            // Then do the same with 2, 3, 4 replicas

            gapAfter = status.computeGap();
        } while (gapBefore > gapAfter);


        return null;
    }

    @Getter
    @Setter // For testing
    public static class CliOptions {

        @Parameter(names = "--overassign", description = "Allow overassigning new replicas to a broker (if --rebalancer simple).")
        private boolean overassign;

        @Parameter(names = "--max-group", description = "Max number of replicas to move as a group (if --rebalancer simple).")
        private int maxGroup = 4;

    }
}
