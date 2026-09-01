package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.service.AbilitySettingsService;

public final class AbilityExperienceBoostModifier implements ExperienceModifier {
    private final AbilityRegistry registry;
    private final AbilitySettingsService settings;

    public AbilityExperienceBoostModifier(AbilityRegistry registry, AbilitySettingsService settings) {
        this.registry = registry;
        this.settings = settings;
    }

    @Override
    public AbilityId id() {
        return BuiltinAbilities.EXPERIENCE_BOOST;
    }

    @Override
    public double multiplier(ExperienceModificationContext context) {
        AbilityDefinition definition = registry.get(id()).orElse(null);
        if (definition == null || !definition.enabled()
                || context.tool().level() < definition.unlockLevel()
                || !definition.supportedToolTypes().contains(context.tool().type())) {
            return 1.0D;
        }
        return settings.current().experienceBoost().multiplier();
    }
}
