package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityCondition;
import dev.yakekusolsu.growthtools.api.ability.AbilityContext;

public record ToolLevelCondition(int minimumLevel) implements AbilityCondition {
    public ToolLevelCondition {
        if (minimumLevel < 1) throw new IllegalArgumentException("minimumLevel must be positive");
    }
    @Override public boolean test(AbilityContext context) {
        return context.toolLevel() >= minimumLevel;
    }
}
