package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Configuration;
import com.github.davgarcia.brodrebalancer.config.Registered;

public interface Rebalancer<T> extends Registered<T> {

    void setBrokerStrategies(final SourceBrokerStrategy<?> srcBrokerStrategy, final DestinationBrokerStrategy<?> dstBrokerStrategy);
    Assignments rebalance(final Configuration config, final LogDirs logDirs);
}
