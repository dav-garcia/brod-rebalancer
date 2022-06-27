package com.github.davgarcia.brodrebalancer.rebalancer;

import com.github.davgarcia.brodrebalancer.DestinationBrokerStrategy;
import com.github.davgarcia.brodrebalancer.LogDirs;
import com.github.davgarcia.brodrebalancer.MovablePartitions;
import com.github.davgarcia.brodrebalancer.Reassignments;
import com.github.davgarcia.brodrebalancer.Rebalancer;
import com.github.davgarcia.brodrebalancer.SourceBrokerStrategy;
import com.github.davgarcia.brodrebalancer.Status;
import com.github.davgarcia.brodrebalancer.config.BrokersConfig;

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

        status.print();

        MovablePartitions.Partition partition;
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
        } while (partition != null);

        return Reassignments.from(status);
    }
}
