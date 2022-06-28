package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.config.Registered;

public interface AssignmentsOutput<T> extends Registered<T> {

    void save(final Assignments assignments);
}
