package dev.yakekusolsu.growthtools.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.ability.PaperAbilityExecutionContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.storage.InMemoryPlacedBlockTracker;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class AdditionalBlockBreakServiceIntegrationTest {
    private ServerMock server;
    private Plugin plugin;
    private PlayerMock player;
    private World world;
    private InMemoryPlacedBlockTracker placedBlocks;
    private AtomicInteger transformationCalls;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("AdditionalBreakTest");
        world = server.addSimpleWorld("additional-drop-test");
        world.getChunkAt(0, 0).load();
        player = server.addPlayer("AdditionalBreakTester");
        player.setGameMode(GameMode.CREATIVE);
        placedBlocks = new InMemoryPlacedBlockTracker();
        transformationCalls = new AtomicInteger();
    }

    @AfterEach
    void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void protectedAdditionalBlockIsNotBrokenOrTransformedOrDropped() {
        Block target = ironOre();
        AdditionalBlockBreakService service = service((ignoredPlayer, ignoredBlock) -> false);

        assertFalse(service.breakBlock(context(target), target));

        assertEquals(Material.IRON_ORE, target.getType());
        assertEquals(0, transformationCalls.get());
        assertTrue(droppedItems().isEmpty());
    }

    @Test
    void placedAdditionalBlockIsNotBrokenOrTransformedOrDropped() {
        Block target = ironOre();
        placedBlocks.markPlaced(target, player.getUniqueId());
        AdditionalBlockBreakService service = service((ignoredPlayer, ignoredBlock) -> true);

        assertFalse(service.breakBlock(context(target), target));

        assertEquals(Material.IRON_ORE, target.getType());
        assertEquals(0, transformationCalls.get());
        assertTrue(droppedItems().isEmpty());
    }

    @Test
    void successfulAdditionalBreakSpawnsOnlyPipelineFinalDrops() {
        Block target = ironOre();
        AdditionalBlockBreakService service = service((ignoredPlayer, ignoredBlock) -> true);

        assertTrue(service.breakBlock(context(target), target));

        assertEquals(Material.AIR, target.getType());
        assertEquals(1, transformationCalls.get());
        assertEquals(List.of(Material.IRON_INGOT), droppedItems());
    }

    private AdditionalBlockBreakService service(
            dev.yakekusolsu.growthtools.integration.BlockProtectionGate protection) {
        DropTransformer transformer = new DropTransformer() {
            @Override
            public String id() {
                return "test:replacement";
            }

            @Override
            public int transform(DropTransformationContext context, List<ItemStack> drops) {
                transformationCalls.incrementAndGet();
                drops.clear();
                drops.add(ItemStack.of(Material.IRON_INGOT));
                return 1;
            }
        };
        return new AdditionalBlockBreakService(
                plugin,
                placedBlocks,
                new BlockToolCompatibilityService(),
                protection,
                ignored -> { },
                new DropTransformationPipeline(List.of(transformer)),
                (block, tool, player) -> List.of(ItemStack.of(Material.RAW_IRON)));
    }

    private PaperAbilityExecutionContext context(Block origin) {
        ItemStack tool = ItemStack.of(Material.DIAMOND_PICKAXE);
        return new PaperAbilityExecutionContext(
                new AbilityContext(
                        UUID.randomUUID(), GrowthToolType.PICKAXE, 75,
                        AbilityTrigger.BLOCK_BREAK, Map.of()),
                player,
                tool,
                origin);
    }

    private Block ironOre() {
        Block block = world.getBlockAt(0, 64, 0);
        block.setType(Material.IRON_ORE);
        return block;
    }

    private List<Material> droppedItems() {
        return world.getEntities().stream()
                .filter(Item.class::isInstance)
                .map(Item.class::cast)
                .map(Item::getItemStack)
                .map(ItemStack::getType)
                .toList();
    }
}
