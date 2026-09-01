package dev.yakekusolsu.growthtools.integration.placeholderapi;

import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.integration.IntegrationAdapter;
import dev.yakekusolsu.growthtools.service.ExperienceService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderApiIntegration extends PlaceholderExpansion
        implements IntegrationAdapter {
    private static final int MAX_PARAMETER_LENGTH = 160;
    private final Plugin plugin;
    private final GrowthToolItemService items;
    private final ExperienceService experience;
    private final AbilityRegistry abilities;
    public PlaceholderApiIntegration(Plugin plugin, GrowthToolItemService items,
            ExperienceService experience, AbilityRegistry abilities) {
        this.plugin = plugin; this.items = items; this.experience = experience;
        this.abilities = abilities;
    }
    @Override public String id() { return "placeholderapi"; }
    @Override public String pluginName() { return "PlaceholderAPI"; }
    @Override public void initialize() {
        if (!register()) throw new IllegalStateException("Placeholder expansion registration failed");
    }
    @Override public void close() { unregister(); }
    @Override public @NotNull String getIdentifier() { return "growthtools"; }
    @Override public @NotNull String getAuthor() { return "yakekusolsu"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }
    @Override public @Nullable String onPlaceholderRequest(Player player, @NotNull String parameter) {
        if (player == null) return "";
        if (parameter.length() > MAX_PARAMETER_LENGTH) return null;
        var data = items.read(player.getInventory().getItemInMainHand());
        if (data.isEmpty()) return "";
        var tool = data.get();
        String normalized = parameter.toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "level" -> Integer.toString(tool.level());
            case "exp" -> Long.toString(tool.experience());
            case "exp_current" -> Long.toString(experience.experienceWithinLevel(tool.experience()));
            case "exp_required" -> Long.toString(experience.requiredExperience(tool.level()));
            case "type" -> tool.type().id();
            case "uuid" -> tool.toolId().toString();
            case "max_level" -> Integer.toString(experience.maximumLevel());
            case "is_max_level" -> Boolean.toString(tool.level() >= experience.maximumLevel());
            default -> abilityPlaceholder(normalized, tool.level(), tool.type());
        };
    }
    private String abilityPlaceholder(String parameter, int level,
            dev.yakekusolsu.growthtools.model.GrowthToolType type) {
        if (!parameter.startsWith("ability_") || !parameter.endsWith("_unlocked")) return null;
        String value = parameter.substring("ability_".length(),
                parameter.length() - "_unlocked".length());
        try {
            return abilities.get(AbilityId.parse(value))
                    .map(definition -> Boolean.toString(definition.enabled()
                            && definition.unlockLevel() <= level
                            && definition.supportedToolTypes().contains(type)))
                    .orElse("false");
        } catch (IllegalArgumentException exception) {
            return "false";
        }
    }
}
