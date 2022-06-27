package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Registered;

public interface ReassignmentsOutput<T> extends Registered<T> {

    void save(final Reassignments reassignments);
}
