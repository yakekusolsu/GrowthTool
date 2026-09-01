package dev.yakekusolsu.growthtools.integration;

import dev.yakekusolsu.growthtools.config.PluginConfiguration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Isolates optional dependency loading and records independent health states. */
public final class IntegrationManager implements AutoCloseable, BlockProtectionGate {
    private final Plugin plugin;
    private final PluginConfiguration configuration;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private volatile List<IntegrationSnapshot> snapshotCache = List.of();
    private volatile BlockProtectionGate worldGuard = (player, block) -> true;
    private volatile EconomyService economy = new EconomyService() {
        @Override public boolean available() { return false; }
        @Override public double balance(java.util.UUID playerId) { return 0; }
    };
    private volatile BedrockPlayerService bedrock = playerId -> false;
    private final List<ExtraBlockRewardGuard> rewardGuards = new ArrayList<>();

    public IntegrationManager(Plugin plugin, PluginConfiguration configuration) {
        this.plugin = plugin; this.configuration = configuration;
    }

    public void initialize(dev.yakekusolsu.growthtools.service.GrowthToolItemService items,
            dev.yakekusolsu.growthtools.service.ExperienceService experience,
            dev.yakekusolsu.growthtools.ability.AbilityRegistry abilities) {
        entries.clear();
        add("placeholderapi", "PlaceholderAPI", () ->
                new dev.yakekusolsu.growthtools.integration.placeholderapi.PlaceholderApiIntegration(
                        plugin, items, experience, abilities));
        add("vault", "Vault", () ->
                new dev.yakekusolsu.growthtools.integration.vault.VaultEconomyBridge(plugin));
        add("mcmmo", "mcMMO", () ->
                new dev.yakekusolsu.growthtools.integration.mcmmo.McMmoIntegration(
                        configuration.abilityExtraBlockRewardsEnabled("mcmmo")));
        add("jobs", "Jobs", () ->
                new dev.yakekusolsu.growthtools.integration.jobs.JobsIntegration(
                        plugin, configuration.abilityExtraBlockRewardsEnabled("jobs")));
        add("worldguard", "WorldGuard", () ->
                new dev.yakekusolsu.growthtools.integration.worldguard.WorldGuardProtectionBridge());
        add("geyser", "Geyser-Spigot", () -> new DetectedIntegration("geyser", "Geyser-Spigot"));
        add("floodgate", "floodgate", () ->
                new dev.yakekusolsu.growthtools.integration.floodgate.FloodgateBedrockBridge());
        refreshAll();
    }

    private void add(String id, String pluginName, Supplier<IntegrationAdapter> factory) {
        entries.put(id, new Entry(id, pluginName, factory));
    }

    public void refreshAll() {
        entries.values().forEach(this::refresh);
        updateSnapshotCache();
    }

    public void refresh(String pluginName) {
        entries.values().stream().filter(entry ->
                entry.pluginName.equalsIgnoreCase(pluginName)).forEach(this::refresh);
        updateSnapshotCache();
    }

    private void refresh(Entry entry) {
        if (entry.adapter instanceof ExtraBlockRewardGuard guard) rewardGuards.remove(guard);
        if ("worldguard".equals(entry.id)) worldGuard = (player, block) -> true;
        if ("vault".equals(entry.id)) economy = unavailableEconomy();
        if ("floodgate".equals(entry.id)) bedrock = playerId -> false;
        entry.close();
        if (!configuration.integrationEnabled(entry.id)) {
            entry.state = IntegrationState.DISABLED; entry.detail = "disabled by config"; return;
        }
        Plugin dependency = plugin.getServer().getPluginManager().getPlugin(entry.pluginName);
        if (dependency == null || !dependency.isEnabled()) {
            entry.state = IntegrationState.UNAVAILABLE; entry.detail = "plugin not enabled"; return;
        }
        try {
            entry.adapter = entry.factory.get();
            entry.adapter.initialize();
            entry.state = IntegrationState.AVAILABLE;
            entry.detail = dependency.getPluginMeta().getVersion();
            if (entry.adapter instanceof BlockProtectionGate protection) worldGuard = protection;
            if (entry.adapter instanceof EconomyService economyService) economy = economyService;
            if (entry.adapter instanceof BedrockPlayerService bedrockService) bedrock = bedrockService;
            if (entry.adapter instanceof ExtraBlockRewardGuard guard) rewardGuards.add(guard);
        } catch (LinkageError | RuntimeException exception) {
            entry.state = IntegrationState.ERROR;
            entry.detail = exception.getClass().getSimpleName() + ": " + exception.getMessage();
            plugin.getLogger().log(Level.WARNING,
                    "Optional integration " + entry.id + " failed; core remains active.", exception);
            if ("worldguard".equals(entry.id)) worldGuard = (player, block) -> false;
        }
    }

    public List<IntegrationSnapshot> snapshots() {
        return snapshotCache;
    }

    private void updateSnapshotCache() {
        List<IntegrationSnapshot> result = new ArrayList<>();
        entries.values().forEach(entry -> result.add(new IntegrationSnapshot(
                entry.id, entry.pluginName, entry.state, entry.detail)));
        snapshotCache = List.copyOf(result);
    }

    public EconomyService economy() { return economy; }
    public BedrockPlayerService bedrock() { return bedrock; }
    public void markExtraBlock(Block block) {
        for (ExtraBlockRewardGuard guard : List.copyOf(rewardGuards)) {
            try { guard.markExtraBlock(block); }
            catch (RuntimeException | LinkageError exception) {
                plugin.getLogger().log(Level.WARNING, "Extra-block reward guard failed safely", exception);
            }
        }
    }
    @Override public boolean canBreak(Player player, Block block) {
        return worldGuard.canBreak(player, block);
    }

    @Override public void close() {
        entries.values().forEach(Entry::close); entries.clear();
        snapshotCache = List.of();
        rewardGuards.clear();
        worldGuard = (player, block) -> false; bedrock = id -> false;
        economy = unavailableEconomy();
    }

    private static EconomyService unavailableEconomy() {
        return new EconomyService() {
            @Override public boolean available() { return false; }
            @Override public double balance(java.util.UUID playerId) { return 0; }
        };
    }

    private static final class Entry {
        private final String id;
        private final String pluginName;
        private final Supplier<IntegrationAdapter> factory;
        private IntegrationState state = IntegrationState.UNAVAILABLE;
        private String detail = "not initialized";
        private IntegrationAdapter adapter;
        private Entry(String id, String pluginName, Supplier<IntegrationAdapter> factory) {
            this.id = id.toLowerCase(Locale.ROOT); this.pluginName = pluginName; this.factory = factory;
        }
        private void close() {
            if (adapter != null) {
                try { adapter.close(); } catch (RuntimeException ignored) { }
                adapter = null;
            }
        }
    }
}
