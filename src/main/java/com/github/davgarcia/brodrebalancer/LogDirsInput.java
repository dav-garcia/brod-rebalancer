package com.github.davgarcia.brodrebalancer;

public interface LogDirsInput<T> {

    T getCliOptions();
    LogDirs load();
}
