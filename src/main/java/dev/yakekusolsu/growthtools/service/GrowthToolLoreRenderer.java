package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

/** Renders display-only lore from domain data. PDC remains the source of truth. */
public final class GrowthToolLoreRenderer {
    private final MessageService messages;
    private final AbilityRegistry abilityRegistry;
    private final AbilitySettingsService abilitySettings;

    public GrowthToolLoreRenderer(MessageService messages) {
        this(messages, null, null);
    }

    public GrowthToolLoreRenderer(MessageService messages, AbilityRegistry abilityRegistry,
            AbilitySettingsService abilitySettings) {
        this.messages = messages;
        this.abilityRegistry = abilityRegistry;
        this.abilitySettings = abilitySettings;
    }

    public void render(ItemMeta meta, GrowthToolData data, LevelingService levelingService) {
        Component title = messages.component("lore.title", Map.of());
        Component level = messages.component(
                "lore.level", Map.of("level", Integer.toString(data.level())));

        Component experience;
        if (data.level() >= levelingService.getMaximumLevel()) {
            experience = messages.component("lore.experience-max", Map.of());
        } else {
            long progress = levelingService.getExperienceWithinLevel(data.experience());
            long required = levelingService.getRequiredExperienceForLevel(data.level());
            experience = messages.component("lore.experience", Map.of(
                    "experience", Long.toString(progress),
                    "required", Long.toString(required)));
        }
        List<Component> lore = new ArrayList<>(List.of(title, level, experience));
        appendAbilities(lore, data);
        meta.lore(lore);
    }

    private void appendAbilities(List<Component> lore, GrowthToolData data) {
        if (abilityRegistry == null || abilitySettings == null
                || !abilitySettings.current().loreEnabled()) {
            return;
        }
        List<AbilityDefinition> unlocked = abilityRegistry.getAll().stream()
                .filter(AbilityDefinition::enabled)
                .filter(definition -> definition.unlockLevel() <= data.level())
                .filter(definition -> definition.supportedToolTypes().contains(data.type()))
                .sorted(java.util.Comparator.comparingInt(AbilityDefinition::unlockLevel))
                .limit(abilitySettings.current().loreMaximumEntries())
                .toList();
        if (unlocked.isEmpty()) {
            return;
        }
        lore.add(messages.component("lore.abilities-title", Map.of()));
        unlocked.forEach(definition -> lore.add(messages.component("lore.ability", Map.of(
                "ability", definition.displayName(),
                "level", Integer.toString(definition.unlockLevel())))));
    }
}
