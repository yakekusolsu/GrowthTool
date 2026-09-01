package dev.yakekusolsu.growthtools.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import dev.yakekusolsu.growthtools.service.LevelingService;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import dev.yakekusolsu.growthtools.ability.AbilitySafetyLimits;
import java.time.Duration;

public final class PluginConfiguration {
    private static final String MESSAGES_FILE = "messages.yml";
    private static final long DEFAULT_EXPERIENCE_PER_LEVEL = 100L;
    private static final int DEFAULT_MAXIMUM_LEVEL = 500;
    private static final long DEFAULT_FISHING_EXPERIENCE = 5L;
    private static final long DEFAULT_BOW_EXPERIENCE = 3L;

    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private ConfigValidationReport validationReport = new ConfigValidationReport();

    public PluginConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        validationReport = new ConfigValidationReport();
        int configVersion = plugin.getConfig().getInt("config-version", 0);
        if (configVersion == 0) {
            validationReport.warning("config-version",
                    "Version was missing; existing values were preserved and stamped as version 1.");
        }
        ConfigMigrationService migrationService = new ConfigMigrationService(
                List.of(new ConfigVersionStampMigration()));
        try {
            if (migrationService.migrate(plugin.getConfig())) {
                plugin.saveConfig();
                plugin.getLogger().info("Migrated config.yml from version " + configVersion
                        + " to " + ConfigMigrationService.CURRENT_CONFIG_VERSION + ".");
            }
        } catch (IllegalStateException exception) {
            validationReport.error("config-version", exception.getMessage());
        }
        validateConfiguration();
        logValidationReport();
        if (validationReport.hasErrors()) {
            throw new IllegalStateException("Configuration contains unsupported critical errors");
        }
        File messagesFile = new File(plugin.getDataFolder(), MESSAGES_FILE);
        if (!messagesFile.isFile()) {
            plugin.saveResource(MESSAGES_FILE, false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        try (InputStream stream = plugin.getResource(MESSAGES_FILE)) {
            if (stream != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)));
            }
        } catch (java.io.IOException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not close the bundled messages resource", exception);
        }
    }

    public String message(String path) {
        return messages.getString(path, path);
    }

    public LevelingService createLevelingService() {
        long experiencePerLevel = plugin.getConfig()
                .getLong("leveling.experience-per-level", DEFAULT_EXPERIENCE_PER_LEVEL);
        int maximumLevel = plugin.getConfig()
                .getInt("leveling.maximum-level", DEFAULT_MAXIMUM_LEVEL);

        if (experiencePerLevel < 1) {
            experiencePerLevel = DEFAULT_EXPERIENCE_PER_LEVEL;
        }
        if (maximumLevel < 1) {
            maximumLevel = DEFAULT_MAXIMUM_LEVEL;
        }
        return new LevelingService(experiencePerLevel, maximumLevel);
    }

    public ExperienceSettings createExperienceSettings() {
        Map<Material, Long> blocks = new EnumMap<>(Material.class);
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("experience.blocks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null || !material.isBlock()) {
                    continue;
                }
                long amount = section.getLong(key, -1L);
                if (amount < 0) {
                    continue;
                }
                blocks.put(material, amount);
            }
        }

        long fishingExperience = nonNegativeSetting(
                "experience.fishing.default-exp", DEFAULT_FISHING_EXPERIENCE);
        long bowExperience = nonNegativeSetting(
                "experience.bow.hit-exp", DEFAULT_BOW_EXPERIENCE);
        return new ExperienceSettings(
                plugin.getConfig().getBoolean("experience.creative-mode", false),
                blocks,
                plugin.getConfig().getBoolean("experience.fishing.enabled", true),
                fishingExperience,
                plugin.getConfig().getBoolean("experience.bow.enabled", true),
                bowExperience);
    }

    public AbilitySettings createAbilitySettings() {
        Map<String, Set<Material>> oreGroups = new HashMap<>();
        ConfigurationSection groups = plugin.getConfig()
                .getConfigurationSection("abilities.vein-miner.ore-groups");
        if (groups != null) {
            for (String group : groups.getKeys(false)) {
                Set<Material> materials = new HashSet<>();
                for (String value : groups.getStringList(group)) {
                    Material material = Material.matchMaterial(value);
                    if (material != null && material.isBlock()) {
                        materials.add(material);
                    }
                }
                if (!materials.isEmpty()) {
                    oreGroups.put(group, materials);
                }
            }
        }
        AbilitySettings.VeinMiner vein = new AbilitySettings.VeinMiner(
                abilityBoolean("vein-miner", "enabled", true),
                abilityLevel("vein-miner", 25), abilityCooldown("vein-miner", 0.0D),
                abilityBoolean("vein-miner", "require-sneak", true),
                AbilitySafetyLimits.veinBlocks(plugin.getConfig()
                        .getInt("abilities.vein-miner.max-blocks", 16)),
                plugin.getConfig().getBoolean("abilities.vein-miner.diagonal", true),
                abilityMultiplier("vein-miner", "extra-block-exp-multiplier", 0.25D), oreGroups);
        AbilitySettings.AreaBreak area = new AbilitySettings.AreaBreak(
                abilityBoolean("area-break", "enabled", true),
                abilityLevel("area-break", 75), abilityCooldown("area-break", 0.0D),
                abilityBoolean("area-break", "require-sneak", true),
                AbilitySafetyLimits.areaRadius(plugin.getConfig()
                        .getInt("abilities.area-break.radius", 1)),
                abilityMultiplier("area-break", "extra-block-exp-multiplier", 0.25D));
        AbilitySettings.AutoSmelt smelt = new AbilitySettings.AutoSmelt(
                abilityBoolean("auto-smelt", "enabled", true),
                abilityLevel("auto-smelt", 50), abilityCooldown("auto-smelt", 0.0D),
                plugin.getConfig().getBoolean("abilities.auto-smelt.disable-with-silk-touch", true));
        AbilitySettings.ExperienceBoost boost = new AbilitySettings.ExperienceBoost(
                abilityBoolean("experience-boost", "enabled", true),
                abilityLevel("experience-boost", 100), abilityCooldown("experience-boost", 0.0D),
                abilityMultiplier("experience-boost", "multiplier", 1.25D));
        return new AbilitySettings(vein, area, smelt, boost,
                plugin.getConfig().getBoolean("abilities.creative-mode", false),
                plugin.getConfig().getBoolean("abilities.lore.enabled", true),
                Math.clamp(plugin.getConfig().getInt("abilities.lore.maximum-entries", 8), 0, 32));
    }

    private boolean abilityBoolean(String ability, String setting, boolean fallback) {
        return plugin.getConfig().getBoolean("abilities." + ability + '.' + setting, fallback);
    }

    private int abilityLevel(String ability, int fallback) {
        return Math.max(1, plugin.getConfig().getInt(
                "abilities." + ability + ".unlock-level", fallback));
    }

    private Duration abilityCooldown(String ability, double fallbackSeconds) {
        double seconds = plugin.getConfig().getDouble(
                "abilities." + ability + ".cooldown-seconds", fallbackSeconds);
        if (!Double.isFinite(seconds) || seconds < 0.0D) {
            seconds = fallbackSeconds;
        }
        return Duration.ofMillis((long) Math.min(seconds * 1000.0D, 86_400_000.0D));
    }

    private double abilityMultiplier(String ability, String setting, double fallback) {
        return AbilitySafetyLimits.experienceMultiplier(plugin.getConfig().getDouble(
                "abilities." + ability + '.' + setting, fallback), fallback);
    }

    private long nonNegativeSetting(String path, long fallback) {
        long value = plugin.getConfig().getLong(path, fallback);
        if (value < 0) {
            return fallback;
        }
        return value;
    }

    public String databaseFileName() {
        String value = plugin.getConfig().getString("database.file", "growthtools.db");
        if (value == null || value.isBlank() || value.contains("..")
                || new File(value).isAbsolute()) {
            return "growthtools.db";
        }
        return value;
    }

    public boolean auditLoggingEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    public boolean integrationEnabled(String id) {
        return plugin.getConfig().getBoolean("integrations." + id + ".enabled", true);
    }

    public boolean abilityExtraBlockRewardsEnabled(String id) {
        return plugin.getConfig().getBoolean(
                "integrations." + id + ".ability-extra-block-rewards", false);
    }

    public int configVersion() {
        return plugin.getConfig().getInt("config-version", 1);
    }

    public ConfigValidationReport validationReport() {
        return validationReport;
    }

    private void validateConfiguration() {
        if (plugin.getConfig().getLong("leveling.experience-per-level",
                DEFAULT_EXPERIENCE_PER_LEVEL) < 1) {
            validationReport.warning("leveling.experience-per-level",
                    "Value must be positive; using " + DEFAULT_EXPERIENCE_PER_LEVEL + ".");
        }
        if (plugin.getConfig().getInt("leveling.maximum-level", DEFAULT_MAXIMUM_LEVEL) < 1) {
            validationReport.warning("leveling.maximum-level",
                    "Value must be positive; using " + DEFAULT_MAXIMUM_LEVEL + ".");
        }
        validateNonNegative("experience.fishing.default-exp", DEFAULT_FISHING_EXPERIENCE);
        validateNonNegative("experience.bow.hit-exp", DEFAULT_BOW_EXPERIENCE);
        validateAbility("vein-miner", 25, 0.0D);
        validateAbility("auto-smelt", 50, 0.0D);
        validateAbility("area-break", 75, 0.0D);
        validateAbility("experience-boost", 100, 0.0D);
        validateRange("abilities.vein-miner.max-blocks", 1,
                AbilitySafetyLimits.MAX_VEIN_BLOCKS);
        validateRange("abilities.area-break.radius", 0,
                AbilitySafetyLimits.MAX_AREA_RADIUS);
        validateMultiplier("abilities.vein-miner.extra-block-exp-multiplier");
        validateMultiplier("abilities.area-break.extra-block-exp-multiplier");
        validateMultiplier("abilities.experience-boost.multiplier");
        validateOreGroups();

        ConfigurationSection blocks = plugin.getConfig()
                .getConfigurationSection("experience.blocks");
        if (blocks != null) {
            for (String key : blocks.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null || !material.isBlock()) {
                    validationReport.warning("experience.blocks." + key,
                            "Unknown block Material; entry ignored.");
                } else if (blocks.getLong(key, -1L) < 0) {
                    validationReport.warning("experience.blocks." + key,
                            "Negative EXP is forbidden; entry ignored.");
                }
            }
        }
        String databaseFile = plugin.getConfig().getString("database.file", "growthtools.db");
        if (databaseFile == null || databaseFile.isBlank() || databaseFile.contains("..")
                || new File(databaseFile).isAbsolute()) {
            validationReport.warning("database.file",
                    "Unsafe path; using growthtools.db inside the plugin data directory.");
        }
    }

    private void validateAbility(String ability, int fallbackLevel, double fallbackCooldown) {
        String prefix = "abilities." + ability;
        if (plugin.getConfig().getInt(prefix + ".unlock-level", fallbackLevel) < 1) {
            validationReport.warning(prefix + ".unlock-level", "Must be positive; using default.");
        }
        double cooldown = plugin.getConfig().getDouble(
                prefix + ".cooldown-seconds", fallbackCooldown);
        if (!Double.isFinite(cooldown) || cooldown < 0.0D || cooldown > 86_400.0D) {
            validationReport.warning(prefix + ".cooldown-seconds",
                    "Must be finite and between 0 and 86400; using a safe value.");
        }
    }

    private void validateRange(String path, int minimum, int maximum) {
        int value = plugin.getConfig().getInt(path);
        if (value < minimum || value > maximum) {
            validationReport.warning(path, "Outside safe range " + minimum + ".." + maximum
                    + "; value will be clamped.");
        }
    }

    private void validateMultiplier(String path) {
        double value = plugin.getConfig().getDouble(path);
        if (!Double.isFinite(value) || value <= 0.0D
                || value > AbilitySafetyLimits.MAX_EXPERIENCE_MULTIPLIER) {
            validationReport.warning(path, "Invalid multiplier; using a bounded safe value.");
        }
    }

    private void validateOreGroups() {
        ConfigurationSection groups = plugin.getConfig()
                .getConfigurationSection("abilities.vein-miner.ore-groups");
        if (groups != null) {
            for (String group : groups.getKeys(false)) {
                for (String materialName : groups.getStringList(group)) {
                    Material material = Material.matchMaterial(materialName);
                    if (material == null || !material.isBlock()) {
                        validationReport.warning("abilities.vein-miner.ore-groups." + group,
                                "Unknown block Material '" + materialName + "'; entry ignored.");
                    }
                }
            }
        }
    }

    private void validateNonNegative(String path, long fallback) {
        if (plugin.getConfig().getLong(path, fallback) < 0) {
            validationReport.warning(path,
                    "Negative EXP is forbidden; using " + fallback + ".");
        }
    }

    private void logValidationReport() {
        plugin.getLogger().info("Config validation: " + validationReport.warningCount()
                + " warning(s), " + validationReport.errorCount() + " error(s).");
        for (ConfigValidationIssue issue : validationReport.issues()) {
            Level level = issue.severity() == ConfigValidationIssue.Severity.ERROR
                    ? Level.SEVERE : Level.WARNING;
            plugin.getLogger().log(level,
                    issue.severity() + " " + issue.path() + ": " + issue.message());
        }
    }
}
