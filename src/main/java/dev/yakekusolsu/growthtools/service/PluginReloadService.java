package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.config.PluginConfiguration;

/** Coordinates runtime services that must adopt reloaded configuration. */
public final class PluginReloadService {
    private final PluginConfiguration configuration;
    private final GrowthToolItemService itemService;
    private final ExperienceService experienceService;
    private final ExperienceSettingsService settingsService;
    private final AbilitySettingsService abilitySettingsService;
    private final Runnable integrationReload;

    public PluginReloadService(
            PluginConfiguration configuration,
            GrowthToolItemService itemService,
            ExperienceService experienceService,
            ExperienceSettingsService settingsService,
            AbilitySettingsService abilitySettingsService,
            Runnable integrationReload) {
        this.configuration = configuration;
        this.itemService = itemService;
        this.experienceService = experienceService;
        this.settingsService = settingsService;
        this.abilitySettingsService = abilitySettingsService;
        this.integrationReload = integrationReload;
    }

    public void reload() {
        configuration.reload();
        LevelingService levelingService = configuration.createLevelingService();
        itemService.updateLevelingService(levelingService);
        experienceService.updateLevelingService(levelingService);
        settingsService.reload();
        abilitySettingsService.reload();
        integrationReload.run();
    }
}
