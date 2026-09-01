package dev.yakekusolsu.growthtools.config;

import dev.yakekusolsu.growthtools.util.migration.MigrationPlanner;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

/** Detects and migrates config versions without replacing unrelated settings. */
public final class ConfigMigrationService {
    public static final int CURRENT_CONFIG_VERSION = 1;

    private final MigrationPlanner<FileConfiguration> planner;
    private final ConfigVersionValidator versionValidator =
            new ConfigVersionValidator(CURRENT_CONFIG_VERSION);

    public ConfigMigrationService(List<? extends ConfigMigration> migrations) {
        planner = new MigrationPlanner<>(migrations);
    }

    public boolean migrate(FileConfiguration configuration) {
        int version = configuration.getInt("config-version", 0);
        ConfigVersionValidator.Result result = versionValidator.validate(version);
        if (result == ConfigVersionValidator.Result.NEWER
                || result == ConfigVersionValidator.Result.INVALID) {
            throw new IllegalStateException("Config version " + version
                    + " is unsupported; current version is " + CURRENT_CONFIG_VERSION);
        }
        if (version == CURRENT_CONFIG_VERSION) {
            return false;
        }
        planner.migrate(configuration, version, CURRENT_CONFIG_VERSION);
        return true;
    }

    public List<? extends dev.yakekusolsu.growthtools.util.migration.MigrationStep<FileConfiguration>>
            select(int fromVersion) {
        return planner.plan(fromVersion, CURRENT_CONFIG_VERSION);
    }
}
