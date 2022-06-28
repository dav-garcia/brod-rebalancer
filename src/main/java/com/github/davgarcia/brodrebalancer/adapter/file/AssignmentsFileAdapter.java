package com.github.davgarcia.brodrebalancer.adapter.file;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import com.github.davgarcia.brodrebalancer.Assignments;
import com.github.davgarcia.brodrebalancer.AssignmentsOutput;
import lombok.Getter;
import lombok.Setter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AssignmentsFileAdapter implements AssignmentsOutput<AssignmentsFileAdapter.CliOptions> {

    private final ObjectMapper objectMapper;
    private final CliOptions cliOptions;

    public AssignmentsFileAdapter() {
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
    public void save(final Assignments assignments) {
        if (cliOptions.getOutputPath() == null) {
            throw new BrodRebalancerException("Required option is missing: --output-path");
        }

        try (final var writer = new FileWriter(cliOptions.getOutputPath(), StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, assignments);
        } catch (IOException e) {
            throw new BrodRebalancerException("Error writing assignments to path: " + cliOptions.getOutputPath(), e);
        }
    }

    @Getter
    @Setter // For testing.
    public static class CliOptions {

        @Parameter(names = "--output-path",
                description = "Location to save the JSON file to be passed to kafka-reassign-partitions.sh (required if --output file).")
        private String outputPath;
    }
}
