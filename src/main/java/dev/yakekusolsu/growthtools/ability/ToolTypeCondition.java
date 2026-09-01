package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityCondition;
import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Set;

public record ToolTypeCondition(Set<GrowthToolType> acceptedTypes) implements AbilityCondition {
    public ToolTypeCondition { acceptedTypes = Set.copyOf(acceptedTypes); }
    @Override public boolean test(AbilityContext context) {
        return acceptedTypes.contains(context.toolType());
    }
}
