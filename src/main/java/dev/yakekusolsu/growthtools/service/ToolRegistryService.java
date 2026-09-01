package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.RegisteredTool;
import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import dev.yakekusolsu.growthtools.storage.AuditLogRepository;
import dev.yakekusolsu.growthtools.storage.GrowthToolRepository;
import dev.yakekusolsu.growthtools.storage.database.DatabaseRuntime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Maintains an async audit registry while leaving portable state in item PDC. */
public final class ToolRegistryService {
    private final GrowthToolRepository repository;
    private final AuditLogRepository auditRepository;
    private final DatabaseRuntime database;
    private final GrowthToolItemService itemService;
    private final Logger logger;
    private final boolean auditEnabled;
    private final DuplicateDetectionService duplicateDetection = new DuplicateDetectionService();
    private final Map<UUID, UUID> recentOwners = new ConcurrentHashMap<>();

    public ToolRegistryService(
            GrowthToolRepository repository,
            AuditLogRepository auditRepository,
            DatabaseRuntime database,
            GrowthToolItemService itemService,
            Logger logger,
            boolean auditEnabled) {
        this.repository = repository;
        this.auditRepository = auditRepository;
        this.database = database;
        this.itemService = itemService;
        this.logger = logger;
        this.auditEnabled = auditEnabled;
    }

    public void observe(GrowthToolData data, Player owner, boolean scanForDuplicates) {
        int sameOwnerCount = scanForDuplicates ? countInInventory(owner, data.toolId()) : 0;
        UUID previousOwner = recentOwners.put(data.toolId(), owner.getUniqueId());
        boolean previousOwnerStillHasTool = false;
        if (previousOwner != null && !previousOwner.equals(owner.getUniqueId())) {
            Player previous = owner.getServer().getPlayer(previousOwner);
            previousOwnerStillHasTool = previous != null
                    && countInInventory(previous, data.toolId()) > 0;
        }
        boolean duplicate = duplicateDetection.isDuplicate(
                sameOwnerCount, previousOwnerStillHasTool);

        long now = System.currentTimeMillis();
        ToolRegistryStatus status = duplicate
                ? ToolRegistryStatus.DUPLICATE : ToolRegistryStatus.ACTIVE;
        RegisteredTool registered = new RegisteredTool(
                data.toolId(),
                data.type(),
                data.level(),
                data.experience(),
                data.dataVersion(),
                now,
                now,
                owner.getUniqueId(),
                status);
        database.run(() -> repository.upsert(registered), "update GrowthTool registry");

        if (duplicate) {
            logger.warning("Duplicate GrowthTool UUID detected: " + data.toolId()
                    + " (observed owner=" + owner.getName() + ")");
            audit("DUPLICATE_DETECTED", data.toolId(), owner.getUniqueId(),
                    "Duplicate UUID observed for player " + owner.getName(), now, true);
        }
    }

    public void observeInventory(Player player) {
        Map<UUID, GrowthToolData> tools = new HashMap<>();
        Map<UUID, Integer> counts = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            itemService.read(item).ifPresent(data -> {
                tools.putIfAbsent(data.toolId(), data);
                counts.merge(data.toolId(), Math.max(1, item.getAmount()), Integer::sum);
            });
        }
        tools.forEach((id, data) -> observe(data, player, counts.getOrDefault(id, 0) > 1));
    }

    public CompletableFuture<Optional<RegisteredTool>> find(UUID toolId) {
        return database.supply(() -> repository.find(toolId), "read GrowthTool registry");
    }

    public void markStatus(UUID toolId, ToolRegistryStatus status, UUID actorId, String details) {
        long now = System.currentTimeMillis();
        database.run(() -> repository.updateStatus(toolId, status, now),
                "update GrowthTool status");
        audit("STATUS_" + status.name(), toolId, actorId, details, now, true);
    }

    public void registerReplacement(
            GrowthToolData oldData, GrowthToolData newData, Player player) {
        long now = System.currentTimeMillis();
        RegisteredTool replaced = new RegisteredTool(
                oldData.toolId(),
                oldData.type(),
                oldData.level(),
                oldData.experience(),
                oldData.dataVersion(),
                now,
                now,
                player.getUniqueId(),
                ToolRegistryStatus.REPLACED);
        database.run(() -> repository.upsert(replaced), "retain replaced GrowthTool registry row");
        recentOwners.remove(oldData.toolId());
        observe(newData, player, true);
        audit("UUID_REGENERATED", newData.toolId(), player.getUniqueId(),
                "Old UUID " + oldData.toolId() + ", new UUID " + newData.toolId(),
                now, true);
    }

    public void audit(
            String operation,
            UUID toolId,
            UUID actorId,
            String details,
            long timestamp,
            boolean important) {
        if (!important && !auditEnabled) {
            return;
        }
        database.run(() -> auditRepository.record(
                operation, toolId, actorId, details, timestamp), "write audit log");
    }

    private int countInInventory(Player player, UUID toolId) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            Optional<GrowthToolData> data = itemService.read(item);
            if (data.isPresent() && data.get().toolId().equals(toolId)) {
                count += Math.max(1, item.getAmount());
            }
        }
        return count;
    }
}
