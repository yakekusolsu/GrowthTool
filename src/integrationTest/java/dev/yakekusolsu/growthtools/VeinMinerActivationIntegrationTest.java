package dev.yakekusolsu.growthtools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.ability.AbilityOriginContext;
import dev.yakekusolsu.growthtools.listener.BlockExperienceListener;
import dev.yakekusolsu.growthtools.service.GrowthToolsRuntime;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.lang.reflect.Field;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class VeinMinerActivationIntegrationTest {
    private ServerMock server;
    private GrowthToolsPlugin plugin;
    private PlayerMock player;
    private World world;

    @BeforeEach
    void setUp() throws InterruptedException {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("vein-recursion-test");
        plugin = MockBukkit.load(GrowthToolsPlugin.class);
        player = server.addPlayer("VeinRecursionTester");
        player.setOp(true);
        awaitPlacedBlockTracker();
        giveUnlockedPickaxe();
        player.setSneaking(true);
    }

    @AfterEach
    void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void bundledMiningAbilitiesRequireSneakingAndUseZeroCooldown() {
        assertEquals(0.0D,
                plugin.getConfig().getDouble("abilities.vein-miner.cooldown-seconds"));
        assertTrue(plugin.getConfig().getBoolean("abilities.vein-miner.require-sneak"));
        assertEquals(0.0D,
                plugin.getConfig().getDouble("abilities.area-break.cooldown-seconds"));
        assertTrue(plugin.getConfig().getBoolean("abilities.area-break.require-sneak"));
    }

    @Test
    void abilityOriginBlockBreakDoesNotRecursivelyTriggerVeinMiner() {
        world.getChunkAt(0, 0).load();
        Block origin = world.getBlockAt(0, 64, 0);
        Block adjacent = world.getBlockAt(1, 64, 0);
        origin.setType(Material.IRON_ORE);
        adjacent.setType(Material.IRON_ORE);

        AbilityOriginContext.Scope scope = AbilityOriginContext.enter();
        try {
            assertTrue(AbilityOriginContext.isAbilityOrigin());
            assertTrue(player.breakBlock(origin));
        } finally {
            scope.close();
        }

        assertEquals(Material.IRON_ORE, adjacent.getType());
    }

    @Test
    void abilityOriginBlockBreakDoesNotRecursivelyTriggerAreaBreak() {
        assertTrue(server.execute("gt", player, "debug", "add-level", "50").hasSucceeded());
        world.getChunkAt(0, 0).load();
        Block origin = world.getBlockAt(0, 64, 0);
        Block adjacent = world.getBlockAt(1, 64, 0);
        origin.setType(Material.STONE);
        adjacent.setType(Material.STONE);

        AbilityOriginContext.Scope scope = AbilityOriginContext.enter();
        try {
            assertTrue(AbilityOriginContext.isAbilityOrigin());
            assertTrue(player.breakBlock(origin));
        } finally {
            scope.close();
        }

        assertEquals(Material.STONE, adjacent.getType());
    }

    private void giveUnlockedPickaxe() {
        assertTrue(server.executeConsole("gt", "give", player.getName(), "pickaxe")
                .hasSucceeded());
        ItemStack tool = java.util.Arrays.stream(player.getInventory().getContents())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
        player.getInventory().setItemInMainHand(tool);
        assertTrue(server.execute("gt", player, "debug", "add-level", "24").hasSucceeded());
    }

    private void awaitPlacedBlockTracker() throws InterruptedException {
        PlacedBlockTracker tracker = placedBlockTracker();
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (tracker.isReady()) {
                return;
            }
            Thread.sleep(10L);
        }
        throw new IllegalStateException("Placed-block tracker did not become ready");
    }

    private PlacedBlockTracker placedBlockTracker() {
        try {
            Field runtimeField = GrowthToolsPlugin.class.getDeclaredField("runtime");
            runtimeField.setAccessible(true);
            GrowthToolsRuntime runtime = (GrowthToolsRuntime) runtimeField.get(plugin);
            BlockExperienceListener listener = runtime.listeners().stream()
                    .filter(BlockExperienceListener.class::isInstance)
                    .map(BlockExperienceListener.class::cast)
                    .findFirst()
                    .orElseThrow();
            Field trackerField = BlockExperienceListener.class
                    .getDeclaredField("placedBlockTracker");
            trackerField.setAccessible(true);
            return (PlacedBlockTracker) trackerField.get(listener);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not inspect test runtime", exception);
        }
    }
}
