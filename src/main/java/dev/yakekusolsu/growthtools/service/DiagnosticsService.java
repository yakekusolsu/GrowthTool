package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.api.ApiVersion;
import dev.yakekusolsu.growthtools.integration.IntegrationManager;
import dev.yakekusolsu.growthtools.storage.GrowthToolKeys;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import dev.yakekusolsu.growthtools.storage.database.DatabaseRuntime;
import dev.yakekusolsu.growthtools.storage.database.SchemaMigrationService;
import dev.yakekusolsu.growthtools.config.PluginConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.Plugin;

public final class DiagnosticsService {
    private final Plugin plugin;
    private final PluginConfiguration configuration;
    private final DatabaseRuntime database;
    private final PlacedBlockTracker placedBlocks;
    private final AbilityRegistry abilities;
    private final IntegrationManager integrations;

    public DiagnosticsService(Plugin plugin, PluginConfiguration configuration,
            DatabaseRuntime database, PlacedBlockTracker placedBlocks,
            AbilityRegistry abilities, IntegrationManager integrations) {
        this.plugin = plugin; this.configuration = configuration; this.database = database;
        this.placedBlocks = placedBlocks; this.abilities = abilities; this.integrations = integrations;
    }

    public CompletableFuture<String> report() {
        int abilityCount = abilities.getAll().size();
        return database.stats().thenApply(stats -> {
            StringBuilder text = new StringBuilder();
            line(text, "PASS", "GrowthTools", plugin.getPluginMeta().getVersion());
            line(text, "PASS", "API", ApiVersion.CURRENT.toString());
            line(text, "PASS", "Paper", plugin.getServer().getVersion());
            line(text, "PASS", "Java", System.getProperty("java.version"));
            line(text, configuration.validationReport().hasErrors() ? "FAIL"
                    : configuration.validationReport().warningCount() > 0 ? "WARN" : "PASS",
                    "Config", "v" + configuration.configVersion() + ", warnings="
                            + configuration.validationReport().warningCount());
            line(text, stats.connected() ? "PASS" : "FAIL", "Database",
                    "connected=" + stats.connected() + ", tools=" + stats.registeredTools()
                            + ", placed=" + stats.trackedPlacedBlocks()
                            + ", bytes=" + stats.databaseFileSize());
            line(text, stats.duplicateTools() == 0 ? "PASS" : "WARN", "Registry",
                    "duplicates=" + stats.duplicateTools());
            line(text, stats.schemaVersion() == SchemaMigrationService.CURRENT_SCHEMA_VERSION
                    ? "PASS" : "FAIL", "Schema", Integer.toString(stats.schemaVersion()));
            line(text, "PASS", "PDC", Integer.toString(GrowthToolKeys.CURRENT_DATA_VERSION));
            line(text, placedBlocks.isReady() ? "PASS" : "FAIL", "Placed blocks",
                    placedBlocks.isReady() ? "ready" : "degraded; block EXP disabled");
            line(text, "PASS", "Abilities", Integer.toString(abilityCount));
            var integrationSnapshots = integrations.snapshots();
            integrationSnapshots.forEach(value -> line(text,
                    switch (value.state()) {
                        case AVAILABLE, UNAVAILABLE, DISABLED -> "PASS";
                        case ERROR -> "WARN";
                    }, "Integration " + value.id(), value.state() + " (" + value.detail() + ')'));
            boolean integrationError = integrationSnapshots.stream()
                    .anyMatch(value -> value.state()
                            == dev.yakekusolsu.growthtools.integration.IntegrationState.ERROR);
            boolean degraded = !stats.connected() || !placedBlocks.isReady() || integrationError;
            line(text, degraded ? "WARN" : "PASS", "Degraded mode",
                    Boolean.toString(degraded));
            return text.toString().stripTrailing();
        });
    }

    public CompletableFuture<Path> export() {
        return report().thenApply(report -> {
            Path directory = plugin.getDataFolder().toPath().resolve("diagnostics");
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                    .withZone(java.time.ZoneOffset.UTC).format(Instant.now());
            Path file = directory.resolve("growthtools-doctor-" + timestamp + ".txt");
            try {
                Files.createDirectories(directory);
                Files.writeString(file, "Generated: " + Instant.now() + System.lineSeparator()
                        + report + System.lineSeparator(), StandardCharsets.UTF_8);
                return file;
            } catch (IOException exception) {
                throw new java.util.concurrent.CompletionException(exception);
            }
        });
    }

    private static void line(StringBuilder text, String status, String name, String value) {
        text.append(status).append(" | ").append(name).append(": ").append(value)
                .append(System.lineSeparator());
    }
}
