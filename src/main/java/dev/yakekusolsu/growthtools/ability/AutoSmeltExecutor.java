package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.service.DropTransformationContext;
import dev.yakekusolsu.growthtools.service.DropTransformationPipeline;
import dev.yakekusolsu.growthtools.service.DropTransformationResult;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public final class AutoSmeltExecutor implements AbilityExecutor {
    private final DropTransformationPipeline transformations;

    public AutoSmeltExecutor(DropTransformationPipeline transformations) {
        this.transformations = transformations;
    }

    @Override
    public AbilityResult execute(PaperAbilityExecutionContext context, AbilityDefinition definition) {
        if (!(context.payload() instanceof List<?> payload)) {
            return AbilityResult.skipped(AbilityResult.Status.FAILED, "missing drop payload");
        }
        List<Item> entities = new ArrayList<>();
        List<ItemStack> drops = new ArrayList<>();
        for (Object value : payload) {
            if (value instanceof Item entity) {
                entities.add(entity);
                drops.add(entity.getItemStack());
            }
        }
        DropTransformationResult result = transformations.transform(
                new DropTransformationContext(context.domain().toolType(),
                        context.domain().toolLevel(), context.tool()),
                drops);
        for (int index = 0; index < entities.size(); index++) {
            entities.get(index).setItemStack(result.drops().get(index));
        }
        int transformed = result.transformedBy(AutoSmeltDropTransformer.ID);
        return transformed == 0
                ? AbilityResult.skipped(AbilityResult.Status.NO_VALID_TARGET, "no furnace recipe")
                : AbilityResult.success(transformed, 0);
    }
}
