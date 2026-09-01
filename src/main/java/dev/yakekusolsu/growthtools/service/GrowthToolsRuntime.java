package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.GrowthToolsPlugin;
import dev.yakekusolsu.growthtools.command.GrowthToolsCommand;
import dev.yakekusolsu.growthtools.ability.AbilityExperienceBoostModifier;
import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.ability.CooldownService;
import dev.yakekusolsu.growthtools.ability.AbilityService;
import dev.yakekusolsu.growthtools.ability.AreaBreakExecutor;
import dev.yakekusolsu.growthtools.ability.AutoSmeltExecutor;
import dev.yakekusolsu.growthtools.ability.AutoSmeltDropTransformer;
import dev.yakekusolsu.growthtools.ability.BuiltinAbilities;
import dev.yakekusolsu.growthtools.ability.VeinMinerExecutor;
import dev.yakekusolsu.growthtools.ability.ExperienceModifierPipeline;
import dev.yakekusolsu.growthtools.config.PluginConfiguration;
import dev.yakekusolsu.growthtools.data.GrowthToolDataMigrationService;
import dev.yakekusolsu.growthtools.listener.BlockExperienceListener;
import dev.yakekusolsu.growthtools.listener.BlockAbilityListener;
import dev.yakekusolsu.growthtools.listener.AutoSmeltListener;
import dev.yakekusolsu.growthtools.listener.BowExperienceListener;
import dev.yakekusolsu.growthtools.listener.FishingExperienceListener;
import dev.yakekusolsu.growthtools.listener.PlacedBlockLifecycleListener;
import dev.yakekusolsu.growthtools.listener.PlacedBlockListener;
import dev.yakekusolsu.growthtools.listener.ToolLifecycleListener;
import dev.yakekusolsu.growthtools.listener.ToolTransformationListener;
import dev.yakekusolsu.growthtools.listener.AddonLifecycleListener;
import dev.yakekusolsu.growthtools.api.GrowthToolsAPI;
import dev.yakekusolsu.growthtools.api.internal.*;
import dev.yakekusolsu.growthtools.integration.IntegrationManager;
import dev.yakekusolsu.growthtools.storage.AuditLogRepository;
import dev.yakekusolsu.growthtools.storage.GrowthToolKeys;
import dev.yakekusolsu.growthtools.storage.GrowthToolRepository;
import dev.yakekusolsu.growthtools.storage.PersistentPlacedBlockTracker;
import dev.yakekusolsu.growthtools.storage.PlacedBlockRepository;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import dev.yakekusolsu.growthtools.storage.database.DatabaseExecutor;
import dev.yakekusolsu.growthtools.storage.database.DatabaseProvider;
import dev.yakekusolsu.growthtools.storage.database.DatabaseRuntime;
import dev.yakekusolsu.growthtools.storage.database.InitialSchemaMigration;
import dev.yakekusolsu.growthtools.storage.database.SQLiteAuditLogRepository;
import dev.yakekusolsu.growthtools.storage.database.SQLiteDatabaseProvider;
import dev.yakekusolsu.growthtools.storage.database.SQLiteGrowthToolRepository;
import dev.yakekusolsu.growthtools.storage.database.SQLitePlacedBlockRepository;
import dev.yakekusolsu.growthtools.storage.database.SchemaMigrationService;
import java.nio.file.Path;
import java.util.List;
import org.bukkit.event.Listener;

/** Owns Phase 4 service wiring and lifecycle without becoming a static singleton. */
public final class GrowthToolsRuntime implements AutoCloseable {
    private final GrowthToolsCommand command;
    private final List<Listener> listeners;
    private final DatabaseRuntime database;
    private final IntegrationManager integrations;
    private final ApiLifecycle apiLifecycle;
    private final GrowthToolsAPI api;

    public GrowthToolsRuntime(GrowthToolsPlugin plugin) {
        PluginConfiguration configuration = new PluginConfiguration(plugin);
        configuration.reload();
        MessageService messages = new MessageService(configuration);
        LevelingService leveling = configuration.createLevelingService();
        AbilityRegistry abilityRegistry = new AbilityRegistry();
        AbilitySettingsService abilitySettings = new AbilitySettingsService(
                configuration, abilityRegistry);
        GrowthToolKeys keys = new GrowthToolKeys(plugin);
        MaterialToolTypeMapper typeMapper = new MaterialToolTypeMapper();
        GrowthToolItemService itemService = new GrowthToolItemService(
                keys,
                typeMapper,
                new GrowthToolLoreRenderer(messages, abilityRegistry, abilitySettings),
                leveling,
                new GrowthToolDataMigrationService(List.of()),
                plugin.getLogger());

        Path databasePath = plugin.getDataFolder().toPath().resolve(configuration.databaseFileName());
        DatabaseProvider provider = new SQLiteDatabaseProvider(databasePath);
        DatabaseExecutor databaseExecutor = new DatabaseExecutor();
        SchemaMigrationService schemaMigrations = new SchemaMigrationService(
                List.of(new InitialSchemaMigration()));
        GrowthToolRepository toolRepository = new SQLiteGrowthToolRepository(provider);
        PlacedBlockRepository blockRepository = new SQLitePlacedBlockRepository(provider);
        AuditLogRepository auditRepository = new SQLiteAuditLogRepository(provider);
        database = new DatabaseRuntime(
                provider,
                databaseExecutor,
                schemaMigrations,
                toolRepository,
                blockRepository,
                plugin.getLogger());
        database.start();

        PlacedBlockTracker placedBlocks = new PersistentPlacedBlockTracker(
                blockRepository, database, plugin.getLogger());
        ExperienceService experienceService = new ExperienceService(leveling);
        ExperienceSettingsService settingsService = new ExperienceSettingsService(configuration);
        ToolRegistryService registryService = new ToolRegistryService(
                toolRepository,
                auditRepository,
                database,
                itemService,
                plugin.getLogger(),
                configuration.auditLoggingEnabled());
        AbilityEventPublisher abilityEvents = new AbilityEventPublisher(plugin, messages);
        ExperienceModifierPipeline modifierPipeline = new ExperienceModifierPipeline(List.of(
                new AbilityExperienceBoostModifier(abilityRegistry, abilitySettings)));
        GrowthToolUpdateService updateService = new GrowthToolUpdateService(
                plugin, itemService, experienceService, messages, registryService,
                abilityRegistry, modifierPipeline, abilityEvents);
        GrowthToolInventoryService inventoryService = new GrowthToolInventoryService(itemService);
        GameplayEntityTracker entityTracker = new GameplayEntityTracker(keys);
        ToolAdministrationService administrationService = new ToolAdministrationService(
                plugin, itemService, registryService, updateService, experienceService,
                database, messages, plugin.getLogger());

        BlockToolCompatibilityService compatibility = new BlockToolCompatibilityService();
        CooldownService cooldowns = new CooldownService();
        AbilityService abilities = new AbilityService(
                abilityRegistry, cooldowns, plugin.getLogger());
        integrations = new IntegrationManager(plugin, configuration);
        integrations.initialize(itemService, experienceService, abilityRegistry);
        PluginReloadService reloadService = new PluginReloadService(
                configuration, itemService, experienceService, settingsService, abilitySettings,
                integrations::refreshAll);
        DropTransformationPipeline dropTransformations = new DropTransformationPipeline(
                List.of(new AutoSmeltDropTransformer(abilitySettings)));
        AdditionalBlockBreakService abilityBreaker = new AdditionalBlockBreakService(
                plugin, placedBlocks, compatibility, integrations, dropTransformations);
        abilities.registerExecutor(BuiltinAbilities.VEIN_MINER,
                new VeinMinerExecutor(abilitySettings, settingsService, placedBlocks,
                        abilityBreaker, updateService));
        abilities.registerExecutor(BuiltinAbilities.AREA_BREAK,
                new AreaBreakExecutor(abilitySettings, settingsService,
                        abilityBreaker, updateService));
        abilities.registerExecutor(BuiltinAbilities.AUTO_SMELT,
                new AutoSmeltExecutor(dropTransformations));
        apiLifecycle = new ApiLifecycle();
        AbilityManagerImpl publicAbilities = new AbilityManagerImpl(
                apiLifecycle, abilityRegistry, abilities);
        List.of(BuiltinAbilities.VEIN_MINER, BuiltinAbilities.AUTO_SMELT,
                BuiltinAbilities.AREA_BREAK, BuiltinAbilities.EXPERIENCE_BOOST)
                .forEach(id -> publicAbilities.trackBuiltin(plugin, id));
        api = new GrowthToolsApiImpl(apiLifecycle,
                new ToolManagerImpl(apiLifecycle, itemService, experienceService),
                publicAbilities,
                new ExperienceManagerImpl(apiLifecycle, itemService, updateService,
                        experienceService));
        DiagnosticsService diagnostics = new DiagnosticsService(plugin, configuration,
                database, placedBlocks, abilityRegistry, integrations);
        command = new GrowthToolsCommand(
                plugin,
                messages,
                reloadService,
                itemService,
                new InitialToolMaterialProvider(),
                registryService,
                administrationService,
                abilityRegistry,
                abilities,
                integrations,
                diagnostics);
        listeners = List.of(
                new PlacedBlockListener(placedBlocks),
                new PlacedBlockLifecycleListener(placedBlocks),
                new BlockAbilityListener(
                        abilities, abilitySettings, itemService, placedBlocks, abilityEvents),
                new AutoSmeltListener(abilities, abilitySettings, itemService, abilityEvents),
                new BlockExperienceListener(
                        settingsService,
                        typeMapper,
                        compatibility,
                        updateService,
                        placedBlocks,
                        configuration::auditLoggingEnabled,
                        plugin.getLogger()),
                new FishingExperienceListener(
                        settingsService, itemService, inventoryService, entityTracker, updateService),
                new BowExperienceListener(
                        settingsService, itemService, inventoryService, entityTracker, updateService),
                new ToolLifecycleListener(itemService, registryService),
                new ToolTransformationListener(itemService),
                new AddonLifecycleListener(publicAbilities, integrations));
    }

    public GrowthToolsCommand command() {
        return command;
    }

    public List<Listener> listeners() {
        return listeners;
    }

    public GrowthToolsAPI api() { return api; }

    @Override
    public void close() {
        apiLifecycle.deactivate();
        integrations.close();
        database.close();
    }
}
