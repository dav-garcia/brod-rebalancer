package com.github.davgarcia.brodrebalancer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;
import java.util.stream.Collectors;

public class Checker {

    public void check(final Assignments before, final Assignments after) {
        checkVersion(after);
        checkPartitions(before, after);
    }

    private void checkVersion(final Assignments assignments) {
        if (assignments.getVersion() != 1) {
            throw new BrodRebalancerException("Reassignment check failed: version must be 1");
        }
    }

    private void checkPartitions(final Assignments before, final Assignments after) {
        final var beforePartitions = computePartitions(before);
        final var afterPartitions = computePartitions(after);

        if (!beforePartitions.equals(afterPartitions)) {
            final var removedPairs = CollectionUtils.subtract(beforePartitions, afterPartitions);
            final var addedPairs = CollectionUtils.subtract(afterPartitions, beforePartitions);
            final var message = String.format("Reassignment check failed:%n- Lost partitions: %s%n- Added partitions: %s",
                    StringUtils.join(removedPairs, ", "),
                    StringUtils.join(addedPairs, ", "));

            throw new BrodRebalancerException(message);
        }
    }

    private Set<PartitionReplicas> computePartitions(final Assignments before) {
        return before.getPartitions().stream()
                .map(PartitionReplicas::from)
                .collect(Collectors.toSet());
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PartitionReplicas {

        String topic;
        int partition;
        int numReplicas;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                    .append("partition", String.format("%s-%d", topic, partition))
                    .append("numReplicas", numReplicas)
                    .toString();
        }

        public static PartitionReplicas from(final Assignments.Partition partition) {
            return new PartitionReplicas(partition.getTopic(), partition.getPartition(), partition.getReplicas().size());
        }
    }
}
