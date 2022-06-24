package com.github.davgarcia.brodrebalancer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class Configuration {

    @Min(1)
    @Max(1)
    int version;
    @NotEmpty
    List<@Valid BrokerConfiguration> brokers;
    Map<String, String> options = new HashMap<>();

    @Value
    @Builder
    public static class BrokerConfiguration {

        @Min(0)
        int id;
        @DecimalMin("0.1")
        double capacity;
    }
}
