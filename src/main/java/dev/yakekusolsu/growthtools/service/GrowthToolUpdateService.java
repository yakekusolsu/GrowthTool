package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.api.event.GrowthToolExperienceGainEvent;
import dev.yakekusolsu.growthtools.api.event.GrowthToolLevelUpEvent;
import dev.yakekusolsu.growthtools.api.event.GrowthToolAbilityUnlockEvent;
import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.ability.AbilityUnlockService;
import dev.yakekusolsu.growthtools.ability.ExperienceModificationContext;
import dev.yakekusolsu.growthtools.ability.ExperienceModificationResult;
import dev.yakekusolsu.growthtools.ability.ExperienceModifierPipeline;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.experience.ExperienceSourceId;
import dev.yakekusolsu.growthtools.model.ExperienceGain;
import dev.yakekusolsu.growthtools.model.ExperienceResult;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Applies one complete PDC, EXP, level, lore, event, and notification update. */
public final class GrowthToolUpdateService {
    private final Plugin plugin;
    private final GrowthToolItemService itemService;
    private final ExperienceService experienceService;
    private final MessageService messages;
    private final ToolRegistryService registryService;
    private final AbilityRegistry abilityRegistry;
    private final AbilityUnlockService unlockService;
    private final ExperienceModifierPipeline modifierPipeline;
    private final AbilityEventPublisher abilityEvents;

    public GrowthToolUpdateService(
            Plugin plugin,
            GrowthToolItemService itemService,
            ExperienceService experienceService,
            MessageService messages,
            ToolRegistryService registryService,
            AbilityRegistry abilityRegistry,
            ExperienceModifierPipeline modifierPipeline,
            AbilityEventPublisher abilityEvents) {
        this.plugin = plugin;
        this.itemService = itemService;
        this.experienceService = experienceService;
        this.messages = messages;
        this.registryService = registryService;
        this.abilityRegistry = abilityRegistry;
        this.unlockService = new AbilityUnlockService(abilityRegistry);
        this.modifierPipeline = modifierPipeline;
        this.abilityEvents = abilityEvents;
    }

    public Optional<ExperienceResult> addExperience(
            Player player, ItemStack item, ExperienceSource source, long amount) {
        return addExperience(player, item, source,
                new ExperienceSourceId("growthtools", source.name().toLowerCase(java.util.Locale.ROOT)),
                amount);
    }

    public Optional<ExperienceResult> addExperience(
            Player player, ItemStack item, ExperienceSource source,
            ExperienceSourceId sourceId, long amount) {
        Optional<GrowthToolData> current = itemService.read(item);
        if (current.isEmpty()) {
            return Optional.empty();
        }

        GrowthToolData data = current.get();
        ExperienceModificationResult modification = modifierPipeline.apply(
                new ExperienceModificationContext(data, source), amount);
        ExperienceGain gain = new ExperienceGain(
                data.toolId(), data.type(), source, modification.amount());
        ExperienceResult result = applyExperience(player, item, data, gain, sourceId);
        if (result.experienceAdded() > 0 && modification.amount() > amount) {
            modification.appliedAbilities().stream()
                    .map(abilityRegistry::get)
                    .flatMap(Optional::stream)
                    .forEach(definition -> abilityEvents.activated(player, item,
                            result.newData(), definition,
                            AbilityResult.success(0, modification.amount() - amount), false));
        }
        return Optional.of(result);
    }

    /** Raises a held GrowthTool by exact level boundaries without gameplay EXP modifiers. */
    public Optional<ExperienceResult> addLevelsForDebug(
            Player player, ItemStack item, int requestedLevels) {
        if (requestedLevels <= 0) {
            throw new IllegalArgumentException("requestedLevels must be positive");
        }
        Optional<GrowthToolData> current = itemService.read(item);
        if (current.isEmpty()) {
            return Optional.empty();
        }

        GrowthToolData data = current.get();
        int targetLevel = (int) Math.min(
                experienceService.maximumLevel(),
                (long) data.level() + requestedLevels);
        long targetExperience = experienceService.totalExperienceForLevel(targetLevel);
        long amount = targetExperience - data.experience();
        if (amount <= 0) {
            return Optional.empty();
        }

        ExperienceGain gain = new ExperienceGain(
                data.toolId(), data.type(), ExperienceSource.API, amount);
        return Optional.of(applyExperience(
                player,
                item,
                data,
                gain,
                new ExperienceSourceId("growthtools", "debug_level")));
    }

    private ExperienceResult applyExperience(
            Player player,
            ItemStack item,
            GrowthToolData data,
            ExperienceGain gain,
            ExperienceSourceId sourceId) {
        ExperienceResult result = experienceService.addExperience(data, gain);
        if (result.experienceAdded() > 0) {
            itemService.write(item, result.newData());
            registryService.observe(result.newData(), player, false);
            plugin.getServer().getPluginManager().callEvent(new GrowthToolExperienceGainEvent(
                    player,
                    item,
                    result.newData().toolId(),
                    result.newData().type(),
                    sourceId,
                    gain.amount(),
                    result.experienceAdded(),
                    result.oldData().experience(),
                    result.newData().experience()));
        }
        if (result.leveledUp()) {
            notifyLevelUp(player, item, result);
        }
        return result;
    }

    private void notifyLevelUp(Player player, ItemStack item, ExperienceResult result) {
        GrowthToolData data = result.newData();
        plugin.getServer().getPluginManager().callEvent(new GrowthToolLevelUpEvent(
                player,
                item,
                data.toolId(),
                data.type(),
                result.oldLevel(),
                result.newLevel()));

        Map<String, String> placeholders = Map.of(
                "type", data.type().id(),
                "level", Integer.toString(result.newLevel()),
                "levels", Integer.toString(result.levelsGained()));
        messages.send(
                player,
                result.reachedMaximumLevel() ? "level-up.maximum" : "level-up.message",
                placeholders);
        for (var definition : unlockService.unlockedBetween(
                data.type(), result.oldLevel(), result.newLevel())) {
            plugin.getServer().getPluginManager().callEvent(new GrowthToolAbilityUnlockEvent(
                    player, item, data.toolId(), data.type(), definition.id(), result.newLevel()));
            messages.send(player, "ability.unlocked", Map.of(
                    "ability", definition.displayName(),
                    "level", Integer.toString(definition.unlockLevel())));
        }
    }
}
