package dev.yakekusolsu.growthtools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.api.GrowthToolsProvider;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.listener.BlockExperienceListener;
import dev.yakekusolsu.growthtools.service.BlockToolCompatibilityService;
import dev.yakekusolsu.growthtools.service.GrowthToolsRuntime;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;

class BlockExperienceIntegrationTest {
    private ServerMock server;
    private GrowthToolsPlugin plugin;
    private PlayerMock player;
    private World world;

    @BeforeEach
    void setUp() throws InterruptedException {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("block-exp-test");
        plugin = MockBukkit.load(GrowthToolsPlugin.class);
        player = server.addPlayer("BlockExpTester");
        player.setOp(true);
        awaitPlacedBlockTracker();
    }

    @AfterEach
    void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void compatibilityIncludesStandardBlocksAcrossAllToolTypes() {
        BlockToolCompatibilityService compatibility = new BlockToolCompatibilityService();

        assertCompatible(compatibility, GrowthToolType.PICKAXE, List.of(
                Material.STONE,
                Material.COBBLESTONE,
                Material.DEEPSLATE,
                Material.COBBLED_DEEPSLATE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.TUFF,
                Material.CALCITE,
                Material.BLACKSTONE,
                Material.BASALT,
                Material.COAL_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.COPPER_ORE,
                Material.DEEPSLATE_COPPER_ORE,
                Material.IRON_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.GOLD_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.REDSTONE_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.DIAMOND_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.NETHER_GOLD_ORE,
                Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS,
                Material.NETHERRACK,
                Material.END_STONE,
                Material.OBSIDIAN,
                Material.CRYING_OBSIDIAN,
                Material.PRISMARINE,
                Material.PRISMARINE_BRICKS,
                Material.DARK_PRISMARINE,
                Material.BRICKS,
                Material.NETHER_BRICKS,
                Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS));

        assertCompatible(compatibility, GrowthToolType.AXE, List.of(
                Material.OAK_LOG,
                Material.SPRUCE_LOG,
                Material.MANGROVE_LOG,
                Material.STRIPPED_OAK_LOG,
                Material.OAK_WOOD,
                Material.STRIPPED_OAK_WOOD,
                Material.OAK_PLANKS,
                Material.BAMBOO_BLOCK,
                Material.STRIPPED_BAMBOO_BLOCK,
                Material.MANGROVE_ROOTS,
                Material.MUDDY_MANGROVE_ROOTS));

        assertCompatible(compatibility, GrowthToolType.SHOVEL, List.of(
                Material.DIRT,
                Material.GRASS_BLOCK,
                Material.COARSE_DIRT,
                Material.ROOTED_DIRT,
                Material.PODZOL,
                Material.MYCELIUM,
                Material.SAND,
                Material.RED_SAND,
                Material.GRAVEL,
                Material.CLAY,
                Material.SOUL_SAND,
                Material.SOUL_SOIL,
                Material.SNOW,
                Material.SNOW_BLOCK,
                Material.POWDER_SNOW,
                Material.MUD));

        assertCompatible(compatibility, GrowthToolType.HOE, List.of(
                Material.WHEAT,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS,
                Material.NETHER_WART,
                Material.MELON,
                Material.PUMPKIN,
                Material.OAK_LEAVES,
                Material.HAY_BLOCK,
                Material.MOSS_BLOCK,
                Material.SCULK,
                Material.SCULK_SENSOR));

        assertFalse(compatibility.isCompatible(GrowthToolType.PICKAXE, Material.OAK_LOG));
        assertFalse(compatibility.isCompatible(GrowthToolType.AXE, Material.DIAMOND_ORE));
        assertFalse(compatibility.isCompatible(GrowthToolType.SHOVEL, Material.REDSTONE_ORE));
        assertFalse(compatibility.isCompatible(GrowthToolType.HOE, Material.STONE));
    }

    @Test
    void defaultConfigContainsPositiveExperienceForDocumentedBlocks() {
        for (Material material : List.of(
                Material.STONE,
                Material.COAL_ORE,
                Material.COPPER_ORE,
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS,
                Material.OBSIDIAN,
                Material.OAK_LOG,
                Material.OAK_PLANKS,
                Material.BAMBOO_BLOCK,
                Material.DIRT,
                Material.SAND,
                Material.MUD,
                Material.WHEAT,
                Material.CARROTS,
                Material.OAK_LEAVES,
                Material.HAY_BLOCK,
                Material.MOSS_BLOCK)) {
            assertTrue(plugin.getConfig().getLong("experience.blocks." + material.name()) > 0,
                    () -> "Default EXP should be positive for " + material);
        }
    }

    @Test
    void configuredNaturalBlocksAwardExperienceThroughBlockBreakEvent() {
        give("pickaxe");

        Map<Material, Long> gains = Map.ofEntries(
                Map.entry(Material.COAL_ORE, 3L),
                Map.entry(Material.COPPER_ORE, 3L),
                Map.entry(Material.REDSTONE_ORE, 5L),
                Map.entry(Material.LAPIS_ORE, 6L),
                Map.entry(Material.EMERALD_ORE, 20L),
                Map.entry(Material.DEEPSLATE_IRON_ORE, 6L),
                Map.entry(Material.NETHER_QUARTZ_ORE, 5L),
                Map.entry(Material.ANCIENT_DEBRIS, 30L),
                Map.entry(Material.OBSIDIAN, 10L));
        long expected = 0L;
        int x = 0;
        for (Map.Entry<Material, Long> entry : gains.entrySet()) {
            expected += entry.getValue();
            breakNatural(entry.getKey(), x++);
            assertEquals(expected, experience(),
                    () -> entry.getKey() + " should award configured EXP");
        }
    }

    @Test
    void axeShovelAndHoeAcceptRepresentativeConfiguredMaterials() {
        give("axe");
        breakNatural(Material.OAK_LOG, 0);
        assertEquals(2L, experience());

        give("shovel");
        breakNatural(Material.DIRT, 1);
        assertEquals(1L, experience());

        give("hoe");
        breakNatural(Material.OAK_LEAVES, 2);
        assertEquals(1L, experience());
    }

    @Test
    void wrongToolsDoNotAwardConfiguredBlockExperience() {
        give("pickaxe");
        breakNatural(Material.OAK_LOG, 0);
        assertEquals(0L, experience());

        give("axe");
        breakNatural(Material.DIAMOND_ORE, 1);
        assertEquals(0L, experience());

        give("shovel");
        breakNatural(Material.REDSTONE_ORE, 2);
        assertEquals(0L, experience());
    }

    @Test
    void playerPlacedConfiguredOreDoesNotAwardExperience() {
        give("pickaxe");
        Block block = world.getBlockAt(0, 64, 0);
        new PlayerSimulation(player).simulateBlockPlace(Material.IRON_ORE, block.getLocation());

        assertTrue(player.breakBlock(block));
        assertEquals(0L, experience(), "Player-placed ore must remain farm-protected");
    }

    @Test
    void onlyMatureAgeableCropsPassCropAndPlacedChecks() {
        BlockToolCompatibilityService compatibility = new BlockToolCompatibilityService();
        Block mature = ageableBlock(Material.WHEAT, 7, 7);
        Block immature = ageableBlock(Material.WHEAT, 0, 7);

        assertTrue(compatibility.isMatureForExperience(GrowthToolType.HOE, mature));
        assertTrue(compatibility.permitsPlacedBlockExperience(GrowthToolType.HOE, mature));
        assertFalse(compatibility.isMatureForExperience(GrowthToolType.HOE, immature));
        assertFalse(compatibility.permitsPlacedBlockExperience(GrowthToolType.HOE, immature));
        assertFalse(compatibility.permitsPlacedBlockExperience(GrowthToolType.PICKAXE, mature));
    }

    @Test
    void reloadPreservesCustomBlockExperienceValues() {
        plugin.getConfig().set("experience.blocks.COAL_ORE", 99L);
        plugin.saveConfig();
        assertTrue(server.executeConsole("gt", "reload").hasSucceeded());

        give("pickaxe");
        breakNatural(Material.COAL_ORE, 0);
        assertEquals(99L, experience());
    }

    private static void assertCompatible(BlockToolCompatibilityService compatibility,
            GrowthToolType type, List<Material> materials) {
        for (Material material : materials) {
            assertTrue(compatibility.isCompatible(type, material),
                    () -> type + " should be compatible with " + material);
        }
    }

    private void give(String type) {
        player.getInventory().clear();
        assertTrue(server.executeConsole("gt", "give", player.getName(), type).hasSucceeded());
        ItemStack tool = java.util.Arrays.stream(player.getInventory().getContents())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
        player.getInventory().setItemInMainHand(tool);
    }

    private void breakNatural(Material material, int x) {
        Block block = world.getBlockAt(x, 64, 0);
        block.setType(material);
        assertTrue(player.breakBlock(block), () -> "Mock player could not break " + material);
    }

    private static Block ageableBlock(Material material, int age, int maximumAge) {
        Ageable data = (Ageable) Proxy.newProxyInstance(
                Ageable.class.getClassLoader(),
                new Class<?>[] {Ageable.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getAge" -> age;
                    case "getMaximumAge" -> maximumAge;
                    default -> defaultValue(method.getReturnType());
                });
        return (Block) Proxy.newProxyInstance(
                Block.class.getClassLoader(),
                new Class<?>[] {Block.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getType" -> material;
                    case "getBlockData" -> data;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return 0;
    }

    private long experience() {
        return GrowthToolsProvider.get().tools()
                .getTool(player.getInventory().getItemInMainHand())
                .orElseThrow()
                .totalExperience();
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
