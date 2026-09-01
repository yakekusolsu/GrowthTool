package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.util.Map;

/** One entry point for translating gameplay signals to domain trigger contexts. */
public final class AbilityTriggerResolver {
    public AbilityContext resolve(
            GrowthToolData data, AbilityTrigger trigger, Map<String, String> attributes) {
        return new AbilityContext(data.toolId(), data.type(), data.level(), trigger, attributes);
    }
}
