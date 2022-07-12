package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Configuration;
import com.github.davgarcia.brodrebalancer.config.Registered;

public interface LeaderStrategy<T> extends Registered<T> {

    void electLeaders(final Configuration config, final Assignments assignments);
}
