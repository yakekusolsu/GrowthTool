package dev.yakekusolsu.growthtools.api.ability;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable ability metadata and activation policy. */
public record AbilityDefinition(
        AbilityId id,
        String displayName,
        String description,
        AbilityTrigger trigger,
        boolean enabled,
        int unlockLevel,
        Duration cooldown,
        Set<GrowthToolType> supportedToolTypes,
        List<AbilityCondition> conditions,
        Map<String, String> settings) {
    private static final int MAX_DISPLAY_NAME_LENGTH = 128;
    private static final int MAX_DESCRIPTION_LENGTH = 512;
    private static final int MAX_CONDITIONS = 32;
    private static final int MAX_SETTINGS = 64;
    private static final int MAX_SETTING_KEY_LENGTH = 128;
    private static final int MAX_SETTING_VALUE_LENGTH = 512;

    public AbilityDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(cooldown, "cooldown");
        Objects.requireNonNull(supportedToolTypes, "supportedToolTypes");
        Objects.requireNonNull(conditions, "conditions");
        Objects.requireNonNull(settings, "settings");
        if (displayName.isBlank() || displayName.length() > MAX_DISPLAY_NAME_LENGTH
                || description.isBlank() || description.length() > MAX_DESCRIPTION_LENGTH
                || unlockLevel < 1 || cooldown.isNegative() || supportedToolTypes.isEmpty()) {
            throw new IllegalArgumentException("Invalid unlock level or cooldown");
        }
        if (conditions.size() > MAX_CONDITIONS || settings.size() > MAX_SETTINGS
                || settings.entrySet().stream().anyMatch(entry -> entry.getKey() == null
                        || entry.getValue() == null || entry.getKey().isBlank()
                        || entry.getKey().length() > MAX_SETTING_KEY_LENGTH
                        || entry.getValue().length() > MAX_SETTING_VALUE_LENGTH)) {
            throw new IllegalArgumentException("Ability conditions or settings exceed safe limits");
        }
        supportedToolTypes = Set.copyOf(supportedToolTypes);
        conditions = List.copyOf(conditions);
        settings = Map.copyOf(settings);
    }

    public boolean canActivate(AbilityContext context) {
        return enabled
                && context.trigger() == trigger
                && context.toolLevel() >= unlockLevel
                && supportedToolTypes.contains(context.toolType())
                && conditions.stream().allMatch(condition -> condition.test(context));
    }
}
