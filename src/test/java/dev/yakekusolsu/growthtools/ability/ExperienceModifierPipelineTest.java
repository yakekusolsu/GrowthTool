package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.model.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExperienceModifierPipelineTest {
    private final ExperienceModificationContext context = new ExperienceModificationContext(
            new GrowthToolData(UUID.randomUUID(), GrowthToolType.PICKAXE, 1, 0, 1, 1),
            ExperienceSource.BLOCK_BREAK);

    @Test void appliesOrderedMultipliersWithFlooring() {
        ExperienceModifierPipeline pipeline = new ExperienceModifierPipeline(List.of(
                modifier("first", 1.25), modifier("second", 2.0)));
        assertEquals(7, pipeline.apply(context, 3).amount());
    }

    @Test void ignoresNanNegativeAndInfinity() {
        ExperienceModifierPipeline pipeline = new ExperienceModifierPipeline(List.of(
                modifier("nan", Double.NaN), modifier("negative", -1),
                modifier("infinite", Double.POSITIVE_INFINITY)));
        assertEquals(10, pipeline.apply(context, 10).amount());
    }

    @Test void saturatesOverflow() {
        ExperienceModifierPipeline pipeline = new ExperienceModifierPipeline(
                List.of(modifier("large", 100)));
        assertEquals(Long.MAX_VALUE, pipeline.apply(context, Long.MAX_VALUE).amount());
    }

    private static ExperienceModifier modifier(String id, double multiplier) {
        return new ExperienceModifier() {
            @Override public AbilityId id() { return AbilityId.parse("test:" + id); }
            @Override public double multiplier(ExperienceModificationContext ignored) {
                return multiplier;
            }
        };
    }
}
