package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Comparator;
import java.util.List;

public final class AbilityUnlockService {
    private final AbilityRegistry registry;

    public AbilityUnlockService(AbilityRegistry registry) {
        this.registry = registry;
    }

    public List<AbilityDefinition> unlockedBetween(GrowthToolType type, int oldLevel, int newLevel) {
        return registry.getAll().stream()
                .filter(AbilityDefinition::enabled)
                .filter(definition -> definition.unlockLevel() > oldLevel
                        && definition.unlockLevel() <= newLevel)
                .filter(definition -> definition.supportedToolTypes().contains(type))
                .sorted(Comparator.comparingInt(AbilityDefinition::unlockLevel))
                .toList();
    }
}
