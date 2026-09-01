package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.config.AbilitySettings;
import dev.yakekusolsu.growthtools.service.AbilitySettingsService;
import dev.yakekusolsu.growthtools.service.DropTransformationContext;
import dev.yakekusolsu.growthtools.service.DropTransformer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/** Auto Smelt as a drop-only modifier shared by normal and additional breaks. */
public final class AutoSmeltDropTransformer implements DropTransformer {
    public static final String ID = "growthtools:auto_smelt";

    private final Supplier<AbilitySettings> settings;
    private final Function<ItemStack, Optional<ItemStack>> recipes;
    private final Predicate<ItemStack> silkTouch;

    public AutoSmeltDropTransformer(AbilitySettingsService settings) {
        this(settings::current, AutoSmeltDropTransformer::furnaceResult,
                tool -> tool.containsEnchantment(Enchantment.SILK_TOUCH));
    }

    AutoSmeltDropTransformer(Supplier<AbilitySettings> settings,
            Function<ItemStack, Optional<ItemStack>> recipes,
            Predicate<ItemStack> silkTouch) {
        this.settings = settings;
        this.recipes = recipes;
        this.silkTouch = silkTouch;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int transform(DropTransformationContext context, List<ItemStack> drops) {
        AbilitySettings.AutoSmelt autoSmelt = settings.get().autoSmelt();
        if (!autoSmelt.enabled()
                || context.toolLevel() < autoSmelt.unlockLevel()
                || !BuiltinAbilities.AUTO_SMELT_TOOL_TYPES.contains(context.toolType())
                || autoSmelt.disableWithSilkTouch()
                        && silkTouch.test(context.tool())) {
            return 0;
        }
        int transformed = 0;
        for (int index = 0; index < drops.size(); index++) {
            ItemStack input = drops.get(index);
            Optional<ItemStack> result = recipes.apply(input);
            if (result.isEmpty()) {
                continue;
            }
            ItemStack replacement = result.get().clone();
            long amount = (long) replacement.getAmount() * input.getAmount();
            replacement.setAmount((int) Math.min(amount, Integer.MAX_VALUE));
            drops.set(index, replacement);
            transformed++;
        }
        return transformed;
    }

    private static Optional<ItemStack> furnaceResult(ItemStack input) {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        FurnaceRecipe best = null;
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe instanceof FurnaceRecipe furnace && furnace.getInputChoice().test(input)
                    && (best == null || key(furnace).compareTo(key(best)) < 0)) {
                best = furnace;
            }
        }
        return best == null ? Optional.empty() : Optional.of(best.getResult());
    }

    private static String key(FurnaceRecipe recipe) {
        return recipe instanceof Keyed keyed ? keyed.getKey().toString() : "minecraft:unknown";
    }
}
