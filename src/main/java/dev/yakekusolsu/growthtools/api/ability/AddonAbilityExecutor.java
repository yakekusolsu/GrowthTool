package dev.yakekusolsu.growthtools.api.ability;

/** Addon effect callback. Invoked synchronously on the Paper main thread. */
@FunctionalInterface
public interface AddonAbilityExecutor {
    AbilityResult execute(AddonAbilityContext context, AbilityDefinition definition);
}
