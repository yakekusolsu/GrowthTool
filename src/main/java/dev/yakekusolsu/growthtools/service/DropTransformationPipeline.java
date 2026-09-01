package dev.yakekusolsu.growthtools.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Applies ordered drop-only modifiers without triggering another block ability. */
public final class DropTransformationPipeline {
    private final List<DropTransformer> transformers;

    public DropTransformationPipeline(List<DropTransformer> transformers) {
        this.transformers = List.copyOf(transformers);
        long uniqueIds = this.transformers.stream().map(DropTransformer::id).distinct().count();
        if (uniqueIds != this.transformers.size()) {
            throw new IllegalArgumentException("Duplicate drop transformer id");
        }
    }

    public DropTransformationResult transform(
            DropTransformationContext context, Collection<ItemStack> initialDrops) {
        Objects.requireNonNull(context, "context");
        List<ItemStack> drops = new ArrayList<>(initialDrops.size());
        for (ItemStack drop : initialDrops) {
            drops.add(Objects.requireNonNull(drop, "drop").clone());
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (DropTransformer transformer : transformers) {
            int transformed = transformer.transform(context, drops);
            if (transformed < 0) {
                throw new IllegalStateException(
                        "Drop transformer returned a negative count: " + transformer.id());
            }
            counts.put(transformer.id(), transformed);
        }
        return new DropTransformationResult(drops, counts);
    }
}
