package dev.yakekusolsu.growthtools.config;

import org.bukkit.configuration.file.FileConfiguration;

/** Adds the Phase 4 version marker without replacing other configuration values. */
public final class ConfigVersionStampMigration implements ConfigMigration {
    @Override
    public int fromVersion() {
        return 0;
    }

    @Override
    public int toVersion() {
        return 1;
    }

    @Override
    public FileConfiguration migrate(FileConfiguration configuration) {
        configuration.set("config-version", 1);
        return configuration;
    }
}
