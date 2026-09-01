package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.ability.BuiltinAbilities;
import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.config.PluginConfiguration;

/** Atomically replaces the immutable ability configuration snapshot on reload. */
public final class AbilitySettingsService {
    private final PluginConfiguration configuration;
    private final AbilityRegistry registry;
    private volatile AbilitySettings settings;

    public AbilitySettingsService(PluginConfiguration configuration, AbilityRegistry registry) {
        this.configuration = configuration;
        this.registry = registry;
        reload();
    }

    public void reload() {
        AbilitySettings replacement = configuration.createAbilitySettings();
        BuiltinAbilities.register(registry, replacement);
        settings = replacement;
    }

    public AbilitySettings current() {
        return settings;
    }
}
