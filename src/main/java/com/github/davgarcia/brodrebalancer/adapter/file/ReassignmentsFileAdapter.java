package com.github.davgarcia.brodrebalancer.adapter.file;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.Reassignments;
import com.github.davgarcia.brodrebalancer.ReassignmentsOutput;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReassignmentsFileAdapter implements ReassignmentsOutput<ReassignmentsFileAdapter.CliOptions> {

    private final ObjectMapper objectMapper;
    private final CliOptions cliOptions;

    public ReassignmentsFileAdapter() {
        objectMapper = new ObjectMapper();
        cliOptions = new CliOptions();
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public CliOptions getCliOptions() {
        return cliOptions;
    }

    @Override
    public void save(final Reassignments reassignments) {
        if (cliOptions.getOutputPath() == null) {
            throw new BrodRebalancerException("Required option is missing: --output-path");
        }

        try (final var writer = new FileWriter(cliOptions.getOutputPath(), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, reassignments);
        } catch (IOException e) {
            throw new BrodRebalancerException("Error writing reassignments to path: " + cliOptions.getOutputPath(), e);
        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE) // For testing.
    public static class CliOptions {

        @Parameter(names = "--output-path",
                description = "Location to save the JSON file to be passed to kafka-reassign-partitions.sh (required if --output file).")
        private String outputPath;
    }
}
