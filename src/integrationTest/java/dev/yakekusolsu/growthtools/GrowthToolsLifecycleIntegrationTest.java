package dev.yakekusolsu.growthtools;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.GrowthToolsAPI;
import dev.yakekusolsu.growthtools.api.GrowthToolsProvider;
import dev.yakekusolsu.growthtools.api.ability.*;
import dev.yakekusolsu.growthtools.api.event.GrowthToolExperienceGainEvent;
import dev.yakekusolsu.growthtools.api.experience.ExperienceSourceId;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class GrowthToolsLifecycleIntegrationTest {
    private ServerMock server;
    private GrowthToolsPlugin plugin;

    @BeforeEach void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(GrowthToolsPlugin.class);
    }

    @AfterEach void tearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock();
    }

    @Test void completeLifecycleUsesThePublicPipeline() {
        assertTrue(plugin.isEnabled());
        assertTrue(GrowthToolsProvider.isAvailable());
        assertTrue(plugin.getConfig().contains("integrations.worldguard.enabled"));

        var player = server.addPlayer("PhaseSixTester");
        player.setOp(true);
        assertTrue(server.executeConsole("gt", "give", player.getName(), "pickaxe")
                .hasSucceeded());
        ItemStack tool = java.util.Arrays.stream(player.getInventory().getContents())
                .filter(java.util.Objects::nonNull).findFirst().orElseThrow();
        player.getInventory().setItemInMainHand(tool);

        GrowthToolsAPI api = GrowthToolsProvider.get();
        assertEquals(1, api.tools().getTool(tool).orElseThrow().dataVersion());
        var observedSource = new java.util.concurrent.atomic.AtomicReference<ExperienceSourceId>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler public void onExperience(GrowthToolExperienceGainEvent event) {
                observedSource.set(event.getSource());
            }
        }, plugin);
        api.experience().addExperience(player, tool, 7,
                new ExperienceSourceId("integrationtest", "reward")).orElseThrow();
        assertEquals("integrationtest:reward", observedSource.get().toString());
        assertEquals(7, api.tools().getTool(tool).orElseThrow().totalExperience());
        var asyncFailure = assertThrows(java.util.concurrent.CompletionException.class,
                () -> java.util.concurrent.CompletableFuture.runAsync(
                        () -> api.tools().getTool(tool)).join());
        assertInstanceOf(IllegalStateException.class, asyncFailure.getCause());

        Plugin addon = MockBukkit.createMockPlugin("IntegrationTest");
        assertThrows(SecurityException.class, () -> api.abilities().register(addon,
                new AbilityDefinition(new AbilityId("growthtools", "spoofed"),
                        "Spoofed", "Must be rejected", AbilityTrigger.BLOCK_BREAK,
                        true, 1, Duration.ZERO, Set.of(GrowthToolType.PICKAXE),
                        List.of(), Map.of()),
                (context, definition) -> AbilityResult.success(0, 0)));
        AbilityRegistration registration = api.abilities().register(addon,
                new AbilityDefinition(new AbilityId("integrationtest", "sample"),
                        "Sample", "Integration test ability", AbilityTrigger.BLOCK_BREAK,
                        true, 1, Duration.ZERO, Set.of(GrowthToolType.PICKAXE),
                        List.of(), Map.of()),
                (context, definition) -> AbilityResult.success(0, 0));
        assertTrue(registration.isRegistered());

        assertTrue(server.executeConsole("gt", "reload").hasSucceeded());
        assertTrue(registration.isRegistered(), "reload must retain addon registrations");
        server.getPluginManager().disablePlugin(addon);
        assertFalse(registration.isRegistered());
        assertFalse(registration.unregister(), "unregister must be idempotent");

        server.getPluginManager().disablePlugin(plugin);
        assertFalse(GrowthToolsProvider.isAvailable());
        assertThrows(IllegalStateException.class, api::tools);
    }

    @Test void administratorCanAddExactLevelsToHeldGrowthTool() {
        var player = server.addPlayer("LevelDebugTester");
        player.setOp(true);
        assertTrue(server.executeConsole("gt", "give", player.getName(), "pickaxe")
                .hasSucceeded());
        ItemStack tool = java.util.Arrays.stream(player.getInventory().getContents())
                .filter(java.util.Objects::nonNull).findFirst().orElseThrow();
        player.getInventory().setItemInMainHand(tool);

        var observedSource = new java.util.concurrent.atomic.AtomicReference<ExperienceSourceId>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler public void onExperience(GrowthToolExperienceGainEvent event) {
                observedSource.set(event.getSource());
            }
        }, plugin);

        player.setOp(false);
        assertTrue(server.execute("gt", player, "debug", "add-level", "1").hasSucceeded());
        assertEquals(1, GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand()).orElseThrow().level());
        player.setOp(true);

        assertTrue(server.execute("gt", player, "debug", "add-level", "5").hasSucceeded());
        var levelSix = GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand()).orElseThrow();
        assertEquals(6, levelSix.level());
        assertEquals(1_500L, levelSix.totalExperience());
        assertEquals("growthtools:debug_level", observedSource.get().toString());

        assertTrue(server.execute("gt", player, "debug", "add-level", "0").hasSucceeded());
        assertEquals(6, GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand()).orElseThrow().level());

        assertTrue(server.execute("gt", player, "debug", "add-level", "1000").hasSucceeded());
        var maximum = GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand()).orElseThrow();
        assertEquals(500, maximum.level());
        assertEquals(12_475_000L, maximum.totalExperience());

        assertTrue(server.execute("gt", player, "debug", "add-level", "1").hasSucceeded());
        assertEquals(500, GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand()).orElseThrow().level());
    }
}
