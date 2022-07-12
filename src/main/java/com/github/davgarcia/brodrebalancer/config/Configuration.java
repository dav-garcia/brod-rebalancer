package com.github.davgarcia.brodrebalancer.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Value
@Builder
public class Configuration {

    @Min(1)
    @Max(1)
    int version;
    @NotEmpty
    List<@Valid BrokerConfig> brokers;
    @Valid TopicsConfig topics;

    @Value
    @Builder
    public static class BrokerConfig {

        @Min(0)
        int id;
        @DecimalMin("0.1")
        double capacity;
    }

    @Value
    @Builder
    public static class TopicsConfig {

        List<String> include;
        List<String> exclude;

        @AssertTrue
        private boolean isValid() {
            return CollectionUtils.isEmpty(include) || CollectionUtils.isEmpty(exclude);
        }
    }
}
