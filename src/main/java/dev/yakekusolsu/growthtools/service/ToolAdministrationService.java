package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.DatabaseStats;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.RegisteredTool;
import dev.yakekusolsu.growthtools.model.RepairOutcome;
import dev.yakekusolsu.growthtools.storage.database.DatabaseRuntime;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Keeps async registry/debug/repair orchestration out of command parsing. */
public final class ToolAdministrationService {
    private final Plugin plugin;
    private final GrowthToolItemService itemService;
    private final ToolRegistryService registryService;
    private final GrowthToolUpdateService updateService;
    private final ExperienceService experienceService;
    private final DatabaseRuntime database;
    private final MessageService messages;
    private final Logger logger;

    public ToolAdministrationService(
            Plugin plugin,
            GrowthToolItemService itemService,
            ToolRegistryService registryService,
            GrowthToolUpdateService updateService,
            ExperienceService experienceService,
            DatabaseRuntime database,
            MessageService messages,
            Logger logger) {
        this.plugin = plugin;
        this.itemService = itemService;
        this.registryService = registryService;
        this.updateService = updateService;
        this.experienceService = experienceService;
        this.database = database;
        this.messages = messages;
        this.logger = logger;
    }

    public void inspect(Player player, boolean debug) {
        ItemStack item = player.getInventory().getItemInMainHand();
        Optional<GrowthToolData> data = itemService.read(item);
        if (data.isEmpty()) {
            messages.send(player, "command.inspect-not-growth-tool");
            return;
        }
        GrowthToolData tool = data.get();
        registryService.observe(tool, player, true);
        registryService.find(tool.toolId()).whenComplete((registered, error) -> onMain(() -> {
            if (error != null) {
                sendToolDetails(player, item, tool, Optional.empty(), debug, "DATABASE_UNAVAILABLE");
            } else {
                sendToolDetails(player, item, tool, registered, debug, "NOT_REGISTERED");
            }
        }));
    }

    public void debugRegistry(CommandSender sender, UUID toolId) {
        registryService.find(toolId).whenComplete((registered, error) -> onMain(() -> {
            if (error != null) {
                messages.send(sender, "admin.database-unavailable");
            } else if (registered.isEmpty()) {
                messages.send(sender, "admin.registry-not-found", Map.of("id", toolId.toString()));
            } else {
                messages.send(sender, "admin.registry-result", registryPlaceholders(registered.get()));
            }
        }));
    }

    public void debugDatabase(CommandSender sender) {
        database.stats().thenAccept(stats -> onMain(() -> sendDatabaseStats(sender, stats)));
    }

    public void addDebugLevels(Player player, int requestedLevels) {
        ItemStack item = player.getInventory().getItemInMainHand();
        Optional<GrowthToolData> current = itemService.read(item);
        if (current.isEmpty()) {
            messages.send(player, "command.inspect-not-growth-tool");
            return;
        }
        GrowthToolData oldData = current.get();
        if (oldData.level() >= experienceService.maximumLevel()) {
            messages.send(player, "admin.debug-level-maximum", Map.of(
                    "level", Integer.toString(oldData.level())));
            return;
        }

        updateService.addLevelsForDebug(player, item, requestedLevels).ifPresent(result -> {
            GrowthToolData newData = result.newData();
            registryService.audit(
                    "DEBUG_LEVEL_INCREASE",
                    newData.toolId(),
                    player.getUniqueId(),
                    "Level " + result.oldLevel() + " -> " + result.newLevel(),
                    System.currentTimeMillis(),
                    true);
            messages.send(player, "admin.debug-level-success", Map.of(
                    "levels", Integer.toString(result.levelsGained()),
                    "old-level", Integer.toString(result.oldLevel()),
                    "new-level", Integer.toString(result.newLevel()),
                    "experience", Long.toString(newData.experience())));
        });
    }

    public void repair(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        RepairOutcome outcome = itemService.repair(item);
        if (outcome.status() == RepairOutcome.Status.REPAIRED) {
            GrowthToolData data = outcome.data().orElseThrow();
            registryService.observe(data, player, true);
            registryService.audit("TOOL_REPAIR", data.toolId(), player.getUniqueId(),
                    outcome.detail(), System.currentTimeMillis(), true);
            messages.send(player, "admin.repair-success", Map.of("detail", outcome.detail()));
        } else if (outcome.status() == RepairOutcome.Status.UNREPAIRABLE) {
            messages.send(player, "admin.repair-unsafe", Map.of("detail", outcome.detail()));
        } else {
            messages.send(player, "command.inspect-not-growth-tool");
        }
    }

    public void regenerateId(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        Optional<GrowthToolData> old = itemService.read(item);
        if (old.isEmpty()) {
            messages.send(player, "admin.repair-unsafe",
                    Map.of("detail", "The main-hand PDC is not safely readable."));
            return;
        }
        Optional<GrowthToolData> replacement = itemService.regenerateId(item);
        if (replacement.isEmpty()) {
            messages.send(player, "admin.repair-unsafe",
                    Map.of("detail", "UUID regeneration failed without changing the item."));
            return;
        }
        long timestamp = System.currentTimeMillis();
        GrowthToolData newData = replacement.get();
        registryService.registerReplacement(old.get(), newData, player);
        logger.warning("GrowthTool UUID regenerated: old=" + old.get().toolId()
                + ", new=" + newData.toolId() + ", player=" + player.getName()
                + ", timestamp=" + timestamp);
        messages.send(player, "admin.regenerate-success", Map.of(
                "old-id", old.get().toolId().toString(),
                "new-id", newData.toolId().toString()));
    }

    private void sendToolDetails(
            CommandSender sender,
            ItemStack item,
            GrowthToolData data,
            Optional<RegisteredTool> registered,
            boolean debug,
            String missingStatus) {
        Map<String, String> values = new java.util.HashMap<>();
        values.put("id", data.toolId().toString());
        values.put("type", data.type().name());
        values.put("level", Integer.toString(data.level()));
        values.put("experience", Long.toString(data.experience()));
        values.put("data-version", Integer.toString(data.dataVersion()));
        values.put("created-at", Instant.ofEpochMilli(data.createdAt()).toString());
        values.put("material", item.getType().name());
        values.put("status", registered.map(value -> value.status().name()).orElse(missingStatus));
        values.put("last-owner", registered.map(value -> value.lastOwnerUuid() == null
                ? "NONE" : value.lastOwnerUuid().toString()).orElse("UNKNOWN"));
        values.put("last-seen", registered.map(value ->
                Instant.ofEpochMilli(value.lastSeenAt()).toString()).orElse("UNKNOWN"));
        values.put("duplicate", registered
                .map(value -> Boolean.toString(
                        value.status() == dev.yakekusolsu.growthtools.model.ToolRegistryStatus.DUPLICATE))
                .orElse("UNKNOWN"));
        messages.send(sender, debug ? "admin.debug-tool-result" : "command.inspect-result", values);
    }

    private void sendDatabaseStats(CommandSender sender, DatabaseStats stats) {
        messages.send(sender, "admin.database-result", Map.of(
                "connected", Boolean.toString(stats.connected()),
                "schema-version", Integer.toString(stats.schemaVersion()),
                "tools", Long.toString(stats.registeredTools()),
                "duplicates", Long.toString(stats.duplicateTools()),
                "blocks", Long.toString(stats.trackedPlacedBlocks()),
                "file-size", Long.toString(stats.databaseFileSize())));
    }

    private static Map<String, String> registryPlaceholders(RegisteredTool tool) {
        return Map.ofEntries(
                Map.entry("id", tool.toolId().toString()),
                Map.entry("type", tool.toolType().name()),
                Map.entry("level", Integer.toString(tool.lastKnownLevel())),
                Map.entry("experience", Long.toString(tool.lastKnownExperience())),
                Map.entry("data-version", Integer.toString(tool.dataVersion())),
                Map.entry("first-seen", Instant.ofEpochMilli(tool.firstSeenAt()).toString()),
                Map.entry("last-seen", Instant.ofEpochMilli(tool.lastSeenAt()).toString()),
                Map.entry("owner", tool.lastOwnerUuid() == null
                        ? "NONE" : tool.lastOwnerUuid().toString()),
                Map.entry("status", tool.status().name()));
    }

    private void onMain(Runnable action) {
        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTask(plugin, action);
        }
    }
}
