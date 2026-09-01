package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;

public interface ExperienceModifier {
    AbilityId id();

    double multiplier(ExperienceModificationContext context);
}
