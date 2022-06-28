package com.github.davgarcia.brodrebalancer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CheckerTest {

    private final Checker sut = new Checker();

    @Test
    void givenEqualAssignmentsThenSuccess() {
        final var before = Assignments.from(ObjectMother.newLogDirs());
        final var after = Assignments.from(ObjectMother.newLogDirs());

        sut.check(before, after);
    }

    @Test
    void givenValidReassignmentsThenSuccess() {
        final var before = Assignments.from(ObjectMother.newLogDirs());
        final var after = Assignments.from(ObjectMother.newLogDirs());
        final var topicB1 = findPartition(after, "topic-b", 1);

        // Move a replica from broker 2 to broker 3.
        topicB1.getReplicas().remove(Integer.valueOf(2)); // Beware of remove(int index) vs remove(T object).
        topicB1.getReplicas().add(3);

        sut.check(before, after);
    }

    @Test
    void givenInvalidReassignmentsThenFail() {
        final var before = Assignments.from(ObjectMother.newLogDirs());
        final var after = Assignments.from(ObjectMother.newLogDirs());
        final var topicB1 = findPartition(after, "topic-b", 1);
        final var topicA2 = findPartition(after, "topic-a", 2);

        // Remove an existing replica and add a non-existing one.
        topicB1.getReplicas().remove(Integer.valueOf(2)); // Beware of remove(int index) vs remove(T object).
        topicA2.getReplicas().add(3);

        Assertions.assertThrows(BrodRebalancerException.class,
                () -> sut.check(before, after));
    }

    private Assignments.Partition findPartition(final Assignments assignments, final String topic, final int partition) {
        return assignments.getPartitions().stream()
                .filter(p -> p.getTopic().equals(topic) && p.getPartition() == partition)
                .findFirst()
                .orElseThrow();
    }
}
