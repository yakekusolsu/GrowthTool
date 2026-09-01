package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.storage.GrowthToolKeys;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/** Stores delayed fishing and bow attribution on the spawned entity itself. */
public final class GameplayEntityTracker {
    private static final byte PROCESSED = 1;

    private final GrowthToolKeys keys;

    public GameplayEntityTracker(GrowthToolKeys keys) {
        this.keys = keys;
    }

    public void trackBow(Entity projectile, UUID toolId) {
        projectile.getPersistentDataContainer().set(
                keys.bowToolId(), PersistentDataType.STRING, toolId.toString());
    }

    public Optional<UUID> claimBow(Entity projectile) {
        return claim(projectile, keys.bowToolId(), keys.bowProcessed());
    }

    public void trackFishing(Entity hook, UUID toolId) {
        hook.getPersistentDataContainer().set(
                keys.fishingToolId(), PersistentDataType.STRING, toolId.toString());
    }

    public Optional<UUID> claimFishing(Entity hook) {
        return claim(hook, keys.fishingToolId(), keys.fishingProcessed());
    }

    private Optional<UUID> claim(
            Entity entity,
            org.bukkit.NamespacedKey toolIdKey,
            org.bukkit.NamespacedKey processedKey) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (container.has(processedKey, PersistentDataType.BYTE)) {
            return Optional.empty();
        }
        String value = container.get(toolIdKey, PersistentDataType.STRING);
        if (value == null) {
            return Optional.empty();
        }
        container.set(processedKey, PersistentDataType.BYTE, PROCESSED);
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
