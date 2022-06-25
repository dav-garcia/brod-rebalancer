package com.github.davgarcia.brodrebalancer;

public interface ReassignmentsOutput<T> {

    String getName();
    T getCliOptions();
    void save(final Reassignments reassignments);
}
