package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Registered;

public interface LogDirsInput<T> extends Registered<T> {

    LogDirs load();
}
