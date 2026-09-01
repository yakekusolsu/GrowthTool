package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.service.DropTransformationContext;
import dev.yakekusolsu.growthtools.service.DropTransformationPipeline;
import dev.yakekusolsu.growthtools.service.DropTransformationResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class AutoSmeltDropTransformerTest {
    @Test
    void veinMinerAdditionalIronDropBecomesIngotWhenUnlocked() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 50, tool(), ItemStack.of(Material.RAW_IRON));

        assertEquals(List.of(Material.IRON_INGOT), materials(result));
        assertEquals(1, result.transformedBy(AutoSmeltDropTransformer.ID));
    }

    @Test
    void lockedAutoSmeltLeavesAdditionalDropUnchanged() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 49, tool(), ItemStack.of(Material.RAW_IRON));

        assertEquals(List.of(Material.RAW_IRON), materials(result));
        assertEquals(0, result.transformedBy(AutoSmeltDropTransformer.ID));
    }

    @Test
    void silkTouchPolicyLeavesAdditionalDropUnchanged() {
        ItemStack silkTouch = tool();

        DropTransformationResult result = transform(
                settings(true, 50, true), 50, silkTouch, true,
                ItemStack.of(Material.RAW_IRON));

        assertEquals(List.of(Material.RAW_IRON), materials(result));
    }

    @Test
    void fortuneAdjustedQuantityIsPreservedByTransformation() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 50, tool(), ItemStack.of(Material.RAW_IRON, 3));

        assertEquals(Material.IRON_INGOT, result.drops().getFirst().getType());
        assertEquals(3, result.drops().getFirst().getAmount());
    }

    @Test
    void areaBreakAdditionalSmeltableDropUsesSameTransformer() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 75, tool(), ItemStack.of(Material.RAW_IRON));

        assertEquals(List.of(Material.IRON_INGOT), materials(result));
    }

    @Test
    void nonSmeltableAdditionalDropRemainsUnchanged() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 75, tool(), ItemStack.of(Material.COBBLESTONE));

        assertEquals(List.of(Material.COBBLESTONE), materials(result));
    }

    @Test
    void mixedAreaDropsTransformOnlySmeltableItems() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 75, tool(),
                ItemStack.of(Material.RAW_IRON), ItemStack.of(Material.COBBLESTONE));

        assertEquals(List.of(Material.IRON_INGOT, Material.COBBLESTONE), materials(result));
    }

    @Test
    void transformationReplacesRawDropInsteadOfAddingDoubleDrop() {
        DropTransformationResult result = transform(
                settings(true, 50, true), 50, tool(), ItemStack.of(Material.RAW_IRON));

        assertEquals(1, result.drops().size());
        assertEquals(Material.IRON_INGOT, result.drops().getFirst().getType());
        assertFalse(materials(result).contains(Material.RAW_IRON));
    }

    private static DropTransformationResult transform(
            AbilitySettings settings, int toolLevel, ItemStack tool, ItemStack... drops) {
        return transform(settings, toolLevel, tool, false, drops);
    }

    private static DropTransformationResult transform(
            AbilitySettings settings, int toolLevel, ItemStack tool,
            boolean silkTouch, ItemStack... drops) {
        AutoSmeltDropTransformer transformer = new AutoSmeltDropTransformer(
                () -> settings,
                input -> input.getType() == Material.RAW_IRON
                        ? Optional.of(ItemStack.of(Material.IRON_INGOT))
                        : Optional.empty(),
                ignored -> silkTouch);
        DropTransformationPipeline pipeline = new DropTransformationPipeline(
                List.of(transformer));
        return pipeline.transform(
                new DropTransformationContext(GrowthToolType.PICKAXE, toolLevel, tool),
                List.of(drops));
    }

    private static List<Material> materials(DropTransformationResult result) {
        return result.drops().stream().map(ItemStack::getType).toList();
    }

    private static ItemStack tool() {
        return ItemStack.of(Material.DIAMOND_PICKAXE);
    }

    private static AbilitySettings settings(
            boolean enabled, int unlockLevel, boolean disableWithSilkTouch) {
        return new AbilitySettings(
                new AbilitySettings.VeinMiner(
                        true, 25, Duration.ZERO, true, 16, true, 0.25D, Map.of()),
                new AbilitySettings.AreaBreak(true, 75, Duration.ZERO, true, 1, 0.25D),
                new AbilitySettings.AutoSmelt(
                        enabled, unlockLevel, Duration.ZERO, disableWithSilkTouch),
                new AbilitySettings.ExperienceBoost(true, 100, Duration.ZERO, 1.25D),
                false,
                true,
                8);
    }
}
