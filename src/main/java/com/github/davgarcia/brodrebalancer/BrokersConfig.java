package com.github.davgarcia.brodrebalancer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class BrokersConfig {

    @Min(1)
    @Max(1)
    int version;
    @NotEmpty
    List<@Valid BrokerConfig> brokers;

    @Value
    @Builder
    public static class BrokerConfig {

        @Min(0)
        int id;
        @DecimalMin("0.1")
        double capacity;
    }
}
