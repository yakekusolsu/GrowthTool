package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.config.ExperienceSettings;
import dev.yakekusolsu.growthtools.config.PluginConfiguration;

/** Owns the reloadable, immutable settings snapshot used during gameplay events. */
public final class ExperienceSettingsService {
    private final PluginConfiguration configuration;
    private ExperienceSettings settings;

    public ExperienceSettingsService(PluginConfiguration configuration) {
        this.configuration = configuration;
        reload();
    }

    public void reload() {
        settings = configuration.createExperienceSettings();
    }

    public ExperienceSettings current() {
        return settings;
    }
}
