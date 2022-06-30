package com.github.davgarcia.brodrebalancer.rebalancer;

import com.beust.jcommander.Parameter;
import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.LogDirs;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Rebalancer;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.Status;
import com.github.davgarcia.brodrebalancer.config.BrokersConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * Rebalancer based on the First-Fit-Decreasing (FFD) algorithm for the
 * <a href="https://en.wikipedia.org/wiki/Bin_packing_problem">bin packing problem</a>.
 * <p>
 * The items to pack are the movable partitions and the bins are the free brokers.<br>
 * The "first fit" function is chosen from the CLI option --dst-broker-strategy.
 */
public class FirstFitDecreasingRebalancer implements Rebalancer<FirstFitDecreasingRebalancer.CliOptions> {

    private final CliOptions cliOptions;

    private SourceBrokerStrategy<?> srcBrokerStrategy;
    private DestinationBrokerStrategy<?> dstBrokerStrategy;

    public FirstFitDecreasingRebalancer() {
        this.cliOptions = new CliOptions();
    }

    @Override
    public String getName() {
        return "ffd";
    }

    @Override
    public String getHelp() {
        return "Applies a First-Fit-Decreasing (FFD) algorithm where the items to pack" + System.lineSeparator() +
                "  are the movable partitions (i.e. the ones in overloaded brokers)" + System.lineSeparator() +
                "  and the bins are the free brokers." + System.lineSeparator() +
                "  Source/destination broker strategies are taken into account.";
    }

    @Override
    public CliOptions getCliOptions() {
        return cliOptions;
    }

    @Override
    public void setBrokerStrategies(final SourceBrokerStrategy<?> srcBrokerStrategy, final DestinationBrokerStrategy<?> dstBrokerStrategy) {
        this.srcBrokerStrategy = srcBrokerStrategy;
        this.dstBrokerStrategy = dstBrokerStrategy;
    }

    @Override
    public Assignments rebalance(final BrokersConfig config, final LogDirs logDirs) {
        final var status = Status.from(config, logDirs);
        final var movablePartitions = MovablePartitions.from(logDirs, status);

        status.print();

        MovablePartitions.Partition partition;
        double currentGap;
        final double maxGap = status.computeGap();
        final double maxThreshold = cliOptions.getMaxThreshold() / 100.0;
        do {
            final var maxDiff = status.computeMaxDiff();
            partition = maxDiff > 0.0 ? movablePartitions.findLargest(maxDiff) : null;
            if (partition != null) {
                final var srcBrokers = status.getBrokers(partition.getReplicas());
                final var srcBroker = srcBrokerStrategy.select(partition, srcBrokers);
                final var dstBrokers = status.findDestinations(partition.getId());
                final var dstBroker = dstBrokerStrategy.select(partition, dstBrokers);

                if (srcBroker != null) {
                    movablePartitions.remove(partition, srcBroker.getId());
                }
                if (srcBroker != null && dstBroker != null) {
                    status.move(srcBroker, dstBroker, partition.getId(), partition.getSize());

                    status.print();
                }
            }
            currentGap = status.computeGap();
        } while (partition != null && (currentGap / maxGap) > maxThreshold);

        return Assignments.from(status);
    }

    @Getter
    @Setter // For testing
    public static class CliOptions {

        @Parameter(names = "--ffd-max-threshold",
                description = "Max gap (as a % from the initial gap) that is acceptable for the reassignment. " +
                        "Set to a lower value to force an even better rebalance.")
        private double maxThreshold = 0.05;
    }
}
