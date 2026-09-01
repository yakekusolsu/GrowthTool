package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import java.util.List;

public record ExperienceModificationResult(long amount, List<AbilityId> appliedAbilities) {
    public ExperienceModificationResult {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        appliedAbilities = List.copyOf(appliedAbilities);
    }
}
