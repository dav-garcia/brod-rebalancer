package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.BrokersConfig;
import com.github.davgarcia.brodrebalancer.config.Registered;

public interface LeaderStrategy<T> extends Registered<T> {

    void electLeaders(final BrokersConfig config, final Assignments assignments);
}
