package com.github.davgarcia.brodrebalancer;

public interface LogDirsInput<T> {

    String getName();
    T getCliOptions();
    LogDirs load();
}
