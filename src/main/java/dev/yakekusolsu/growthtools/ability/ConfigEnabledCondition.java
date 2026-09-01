package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityCondition;
import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import java.util.function.BooleanSupplier;

public record ConfigEnabledCondition(BooleanSupplier enabled) implements AbilityCondition {
    @Override public boolean test(AbilityContext context) { return enabled.getAsBoolean(); }
}
