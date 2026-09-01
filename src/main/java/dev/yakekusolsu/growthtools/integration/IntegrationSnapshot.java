package dev.yakekusolsu.growthtools.integration;

public record IntegrationSnapshot(String id, String pluginName,
        IntegrationState state, String detail) { }
