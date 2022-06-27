package com.github.davgarcia.brodrebalancer.rebalancer;

import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.LogDirs;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Reassignments;
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
 * The items to pack are the movable partitions and the bins are the not overloaded brokers.<br>
 * The "first fit" function is chosen from the CLI option --dst-broker-strategy.
 */
public class FirstFitDecreasingRebalancer implements Rebalancer<Object> {

    private SourceBrokerStrategy<?> srcBrokerStrategy;
    private DestinationBrokerStrategy<?> dstBrokerStrategy;


    @Override
    public String getName() {
        return "ffd";
    }

    @Override
    public Object getCliOptions() {
        return new Object();
    }

    @Override
    public void setBrokerStrategies(final SourceBrokerStrategy<?> srcBrokerStrategy, final DestinationBrokerStrategy<?> dstBrokerStrategy) {
        this.srcBrokerStrategy = srcBrokerStrategy;
        this.dstBrokerStrategy = dstBrokerStrategy;
    }

    @Override
    public Reassignments rebalance(final BrokersConfig config, final LogDirs logDirs) {
        final var status = Status.from(config, logDirs);
        final var movablePartitions = MovablePartitions.from(logDirs, status);

        double gapBefore;
        double gapAfter = status.computeGap();
        do {
            gapBefore = gapAfter;

            final var maxDiff = status.computeMaxDiff();
            if (maxDiff > 0.0) {
                movablePartitions.findLargest(maxDiff).ifPresent(p -> {
                    final var srcBrokers = status.getBrokers(p.getReplicas());
                    final var srcBroker = srcBrokerStrategy.select(p, srcBrokers);
                    final var dstBrokers = status.findDestinations(p.getId());
                    final var dstBroker = dstBrokerStrategy.select(p, dstBrokers);

                    status.move(srcBroker, dstBroker, p.getId(), p.getSize());
                    movablePartitions.remove(p, srcBroker.getId());
                });
            }

            gapAfter = status.computeGap();
        } while (gapBefore > gapAfter);

        return Reassignments.from(status);
    }

    @Getter
    @Setter // For testing
    public static class CliOptions {

        // Empty.
    }
}
