package com.github.davgarcia.brodrebalancer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class BrokersConfigLoader {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public BrokersConfigLoader() {
        objectMapper = new ObjectMapper();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public BrokersConfig loadFromPath(final Path path) {
        try {
            final var result = objectMapper.readValue(path.toFile(), BrokersConfig.class);
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
