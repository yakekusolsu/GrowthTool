package dev.yakekusolsu.growthtools.storage;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/** Central registry for the versioned GrowthTools PDC schema. */
public final class GrowthToolKeys {
    public static final int CURRENT_DATA_VERSION = 1;

    private final NamespacedKey id;
    private final NamespacedKey type;
    private final NamespacedKey level;
    private final NamespacedKey experience;
    private final NamespacedKey createdAt;
    private final NamespacedKey dataVersion;
    private final NamespacedKey bowToolId;
    private final NamespacedKey bowProcessed;
    private final NamespacedKey fishingToolId;
    private final NamespacedKey fishingProcessed;

    public GrowthToolKeys(Plugin plugin) {
        id = new NamespacedKey(plugin, "id");
        type = new NamespacedKey(plugin, "type");
        level = new NamespacedKey(plugin, "level");
        experience = new NamespacedKey(plugin, "experience");
        createdAt = new NamespacedKey(plugin, "created_at");
        dataVersion = new NamespacedKey(plugin, "data_version");
        bowToolId = new NamespacedKey(plugin, "bow_tool_id");
        bowProcessed = new NamespacedKey(plugin, "bow_processed");
        fishingToolId = new NamespacedKey(plugin, "fishing_tool_id");
        fishingProcessed = new NamespacedKey(plugin, "fishing_processed");
    }

    public NamespacedKey id() { return id; }
    public NamespacedKey type() { return type; }
    public NamespacedKey level() { return level; }
    public NamespacedKey experience() { return experience; }
    public NamespacedKey createdAt() { return createdAt; }
    public NamespacedKey dataVersion() { return dataVersion; }
    public NamespacedKey bowToolId() { return bowToolId; }
    public NamespacedKey bowProcessed() { return bowProcessed; }
    public NamespacedKey fishingToolId() { return fishingToolId; }
    public NamespacedKey fishingProcessed() { return fishingProcessed; }
}
