package dev.yakekusolsu.growthtools.api.ability;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Immutable domain context; Paper objects belong in the execution adapter. */
public record AbilityContext(
        UUID toolId,
        GrowthToolType toolType,
        int toolLevel,
        AbilityTrigger trigger,
        Map<String, String> attributes) {
    public AbilityContext {
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(toolType, "toolType");
        Objects.requireNonNull(trigger, "trigger");
        if (toolLevel < 1) {
            throw new IllegalArgumentException("toolLevel must be positive");
        }
        attributes = Map.copyOf(attributes);
    }
}
