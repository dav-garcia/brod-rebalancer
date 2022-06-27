package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.BrokersConfig;
import com.github.davgarcia.brodrebalancer.config.Registered;

public interface Rebalancer<T> extends Registered<T> {

    void setBrokerStrategies(final SourceBrokerStrategy<?> srcBrokerStrategy, final DestinationBrokerStrategy<?> dstBrokerStrategy);
    Reassignments rebalance(final BrokersConfig config, final LogDirs logDirs);
}
