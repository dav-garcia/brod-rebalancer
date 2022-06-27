package com.github.davgarcia.brodrebalancer.config;

public interface Registered<T> {

    String getName();
    T getCliOptions();
}
