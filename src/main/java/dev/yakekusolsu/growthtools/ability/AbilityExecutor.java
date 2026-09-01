package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;

@FunctionalInterface
public interface AbilityExecutor {
    AbilityResult execute(PaperAbilityExecutionContext context, AbilityDefinition definition);
}
