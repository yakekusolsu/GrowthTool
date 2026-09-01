package dev.yakekusolsu.growthtools.util.migration;

/** Generic forward-only version migration step. */
public interface MigrationStep<T> {
    int fromVersion();

    int toVersion();

    T migrate(T value);
}
