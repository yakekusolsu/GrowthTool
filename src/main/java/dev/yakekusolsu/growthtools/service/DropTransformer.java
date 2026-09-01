package dev.yakekusolsu.growthtools.service;

import java.util.List;
import org.bukkit.inventory.ItemStack;

/** One ordered, internal transformation of already-computed block drops. */
public interface DropTransformer {
    String id();

    int transform(DropTransformationContext context, List<ItemStack> drops);
}
