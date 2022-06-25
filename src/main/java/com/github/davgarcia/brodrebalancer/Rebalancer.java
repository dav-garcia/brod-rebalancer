package com.github.davgarcia.brodrebalancer;

public interface Rebalancer<T> {

    String getName();
    T getCliOptions();
    Reassignments rebalance(final BrokersConfig config, final LogDirs logDirs);
}
