package dev.yakekusolsu.growthtools.api.ability;

@FunctionalInterface
public interface AbilityCondition {
    boolean test(AbilityContext context);
}
