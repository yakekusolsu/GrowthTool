package dev.yakekusolsu.growthtools.config;

import dev.yakekusolsu.growthtools.util.migration.MigrationStep;
import org.bukkit.configuration.file.FileConfiguration;

/** Migration boundary that preserves existing user values. */
public interface ConfigMigration extends MigrationStep<FileConfiguration> {}
