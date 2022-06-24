package com.github.davgarcia.brodrebalancer;

public interface ReassignPartitionsOutput<T> {

    T getCliOptions();
    void save(final PartitionsReassignments reassignments);
}
