package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.data.GrowthToolDataMigrationService;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.model.RepairOutcome;
import dev.yakekusolsu.growthtools.storage.GrowthToolKeys;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/** Converts between ItemStacks and validated, versioned GrowthTool domain data. */
public final class GrowthToolItemService {
    private final GrowthToolKeys keys;
    private final MaterialToolTypeMapper typeMapper;
    private final GrowthToolLoreRenderer loreRenderer;
    private final Logger logger;
    private final Clock clock;
    private final GrowthToolDataMigrationService migrationService;
    private LevelingService levelingService;

    public GrowthToolItemService(
            GrowthToolKeys keys,
            MaterialToolTypeMapper typeMapper,
            GrowthToolLoreRenderer loreRenderer,
            LevelingService levelingService,
            GrowthToolDataMigrationService migrationService,
            Logger logger) {
        this(keys, typeMapper, loreRenderer, levelingService, migrationService,
                logger, Clock.systemUTC());
    }

    GrowthToolItemService(
            GrowthToolKeys keys,
            MaterialToolTypeMapper typeMapper,
            GrowthToolLoreRenderer loreRenderer,
            LevelingService levelingService,
            GrowthToolDataMigrationService migrationService,
            Logger logger,
            Clock clock) {
        this.keys = keys;
        this.typeMapper = typeMapper;
        this.loreRenderer = loreRenderer;
        this.levelingService = levelingService;
        this.migrationService = migrationService;
        this.logger = logger;
        this.clock = clock;
    }

    public boolean isGrowthTool(ItemStack item) {
        return read(item).isPresent();
    }

    public boolean hasGrowthToolMarker(ItemStack item) {
        return isUsableItem(item)
                && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                        .has(keys.id(), PersistentDataType.STRING);
    }

    public Optional<GrowthToolData> read(ItemStack item) {
        if (!isUsableItem(item) || !item.hasItemMeta()) {
            return Optional.empty();
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String idValue = container.get(keys.id(), PersistentDataType.STRING);
        if (idValue == null) {
            return Optional.empty();
        }

        String typeValue = container.get(keys.type(), PersistentDataType.STRING);
        Integer level = container.get(keys.level(), PersistentDataType.INTEGER);
        Long experience = container.get(keys.experience(), PersistentDataType.LONG);
        Long createdAt = container.get(keys.createdAt(), PersistentDataType.LONG);
        Integer dataVersion = container.get(keys.dataVersion(), PersistentDataType.INTEGER);
        if (typeValue == null || level == null || experience == null
                || createdAt == null || dataVersion == null) {
            warnInvalid("GrowthTool PDC is incomplete", idValue);
            return Optional.empty();
        }
        if (dataVersion > GrowthToolKeys.CURRENT_DATA_VERSION || dataVersion < 1) {
            warnInvalid("Unsupported GrowthTool data version " + dataVersion, idValue);
            return Optional.empty();
        }

        Optional<GrowthToolType> storedType = GrowthToolType.parse(typeValue);
        Optional<GrowthToolType> materialType = typeMapper.fromMaterial(item.getType());
        if (storedType.isEmpty() || materialType.isEmpty() || storedType.get() != materialType.get()) {
            warnInvalid("GrowthTool type does not match its Material", idValue);
            return Optional.empty();
        }

        try {
            UUID toolId = UUID.fromString(idValue);
            int calculatedLevel = levelingService.calculateLevel(experience);
            long maximumExperience = levelingService.getTotalExperienceForLevel(
                    levelingService.getMaximumLevel());
            if (experience > maximumExperience) {
                warnInvalid("Stored experience exceeds the configured maximum", idValue);
                return Optional.empty();
            }
            GrowthToolData data = new GrowthToolData(
                    toolId, storedType.get(), calculatedLevel, experience, createdAt, dataVersion);
            if (dataVersion < GrowthToolKeys.CURRENT_DATA_VERSION) {
                data = migrationService.migrate(data);
                logger.warning("Migrated GrowthTool PDC from version " + dataVersion + " to "
                        + data.dataVersion() + " (id=" + idValue + ").");
                write(item, data);
            }
            if (level != calculatedLevel) {
                logger.warning("Repaired GrowthTool cached level " + level + " to "
                        + calculatedLevel + " from total EXP (id=" + idValue + ").");
                write(item, data);
            }
            return Optional.of(data);
        } catch (IllegalArgumentException exception) {
            warnInvalid("GrowthTool PDC contains invalid values", idValue);
            return Optional.empty();
        }
    }

    /** Creates fresh domain data and immediately persists it to the supplied ItemStack. */
    public GrowthToolData create(ItemStack item) {
        GrowthToolType type = requireSupportedType(item);
        GrowthToolData data = new GrowthToolData(
                UUID.randomUUID(),
                type,
                1,
                0,
                clock.millis(),
                GrowthToolKeys.CURRENT_DATA_VERSION);
        write(item, data);
        return data;
    }

    public void write(ItemStack item, GrowthToolData data) {
        GrowthToolType materialType = requireSupportedType(item);
        if (materialType != data.type()) {
            throw new IllegalArgumentException("GrowthTool type does not match the ItemStack Material");
        }
        if (data.dataVersion() != GrowthToolKeys.CURRENT_DATA_VERSION) {
            throw new IllegalArgumentException(
                    "Cannot write unsupported data version " + data.dataVersion());
        }
        long maximumExperience = levelingService.getTotalExperienceForLevel(
                levelingService.getMaximumLevel());
        if (data.experience() > maximumExperience) {
            throw new IllegalArgumentException("GrowthTool experience exceeds the configured maximum");
        }
        int calculatedLevel = levelingService.calculateLevel(data.experience());
        if (data.level() != calculatedLevel) {
            throw new IllegalArgumentException("GrowthTool level must match total experience");
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(keys.id(), PersistentDataType.STRING, data.toolId().toString());
        container.set(keys.type(), PersistentDataType.STRING, data.type().name());
        container.set(keys.level(), PersistentDataType.INTEGER, data.level());
        container.set(keys.experience(), PersistentDataType.LONG, data.experience());
        container.set(keys.createdAt(), PersistentDataType.LONG, data.createdAt());
        container.set(keys.dataVersion(), PersistentDataType.INTEGER, data.dataVersion());
        loreRenderer.render(meta, data, levelingService);
        item.setItemMeta(meta);
    }

    public void updateLevelingService(LevelingService newLevelingService) {
        levelingService = newLevelingService;
    }

    public RepairOutcome repair(ItemStack item) {
        if (!hasGrowthToolMarker(item)) {
            return new RepairOutcome(
                    RepairOutcome.Status.NOT_A_GROWTH_TOOL,
                    Optional.empty(),
                    "The item has no GrowthTool PDC marker.");
        }
        Optional<GrowthToolData> data = read(item);
        if (data.isEmpty()) {
            return new RepairOutcome(
                    RepairOutcome.Status.UNREPAIRABLE,
                    Optional.empty(),
                    "Critical PDC values are invalid or unsupported; the item was not changed.");
        }
        write(item, data.get());
        return new RepairOutcome(
                RepairOutcome.Status.REPAIRED,
                data,
                "Level cache and lore were synchronized from total EXP.");
    }

    public Optional<GrowthToolData> regenerateId(ItemStack item) {
        Optional<GrowthToolData> current = read(item);
        if (current.isEmpty()) {
            return Optional.empty();
        }
        GrowthToolData oldData = current.get();
        GrowthToolData replacement = new GrowthToolData(
                UUID.randomUUID(),
                oldData.type(),
                oldData.level(),
                oldData.experience(),
                oldData.createdAt(),
                oldData.dataVersion());
        write(item, replacement);
        return Optional.of(replacement);
    }

    /** Preserves GrowthTools PDC across a server-provided equipment transformation result. */
    public boolean preserveData(ItemStack source, ItemStack result) {
        if (!hasGrowthToolMarker(source) || !isUsableItem(result)) {
            return false;
        }
        Optional<GrowthToolData> sourceData = read(source);
        if (sourceData.isEmpty()
                || typeMapper.fromMaterial(result.getType()).orElse(null) != sourceData.get().type()) {
            return false;
        }
        if (!hasGrowthToolMarker(result)) {
            ItemMeta resultMeta = result.getItemMeta();
            source.getItemMeta().getPersistentDataContainer().copyTo(
                    resultMeta.getPersistentDataContainer(), false);
            result.setItemMeta(resultMeta);
        }
        return read(result).isPresent();
    }

    private GrowthToolType requireSupportedType(ItemStack item) {
        if (!isUsableItem(item)) {
            throw new IllegalArgumentException("ItemStack must not be null or air");
        }
        return typeMapper.fromMaterial(item.getType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported GrowthTool Material: " + item.getType()));
    }

    private static boolean isUsableItem(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    private void warnInvalid(String reason, String id) {
        logger.warning(reason + " (id=" + id + "). The item was left unchanged.");
    }
}
