package dev.yakekusolsu.growthtools.integration;

public interface IntegrationAdapter extends AutoCloseable {
    String id();
    String pluginName();
    void initialize();
    @Override default void close() { }
}
