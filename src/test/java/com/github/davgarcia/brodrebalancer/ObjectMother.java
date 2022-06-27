package com.github.davgarcia.brodrebalancer;

import com.github.davgarcia.brodrebalancer.adapter.file.LogDirsFileAdapter;
import com.github.davgarcia.brodrebalancer.config.BrokersConfig;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class ObjectMother {

    public static BrokersConfig newConfig() {
        return BrokersConfig.builder()
                .brokers(List.of(
                        BrokersConfig.BrokerConfig.builder().id(1).capacity(1.0).build(),
                        BrokersConfig.BrokerConfig.builder().id(2).capacity(1.0).build(),
                        BrokersConfig.BrokerConfig.builder().id(3).capacity(2.0).build()))
                .build();
    }

    public static LogDirs newLogDirs() {
        try {
            final var path = Path.of(ObjectMother.class.getResource("/log-dirs.json").toURI()).toString();
            final var logDirsInput = new LogDirsFileAdapter();
            logDirsInput.getCliOptions().setInputPath(path);
            return logDirsInput.load();
        } catch (URISyntaxException e) {
            throw new BrodRebalancerException(e);
        }
    }
}
