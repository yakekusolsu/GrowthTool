package dev.yakekusolsu.growthtools.integration;

public record DetectedIntegration(String id, String pluginName) implements IntegrationAdapter {
    @Override public void initialize() { }
}
