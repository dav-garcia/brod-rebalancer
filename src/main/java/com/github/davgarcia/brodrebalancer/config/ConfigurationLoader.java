package com.github.davgarcia.brodrebalancer.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davgarcia.brodrebalancer.BrodRebalancerException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class ConfigurationLoader {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ConfigurationLoader() {
        objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public Configuration loadFromPath(final Path path) {
        try {
            final var result = objectMapper.readValue(path.toFile(), Configuration.class);
            final var violations = validator.validate(result);
            if (!violations.isEmpty()) {
                final var errors = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(System.lineSeparator()));
                throw new BrodRebalancerException("Invalid configuration:" + System.lineSeparator() + errors);
            }
            return result;
        } catch (IOException e) {
            throw new BrodRebalancerException("Error loading configuration from path: " + path, e);
        }
    }
}
