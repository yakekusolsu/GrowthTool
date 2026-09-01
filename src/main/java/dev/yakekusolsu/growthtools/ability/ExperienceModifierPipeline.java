package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Ordered overflow-safe EXP modifiers. Invalid modifiers are ignored. */
public final class ExperienceModifierPipeline {
    private final List<ExperienceModifier> modifiers;

    public ExperienceModifierPipeline(List<ExperienceModifier> modifiers) {
        this.modifiers = List.copyOf(modifiers);
    }

    public ExperienceModificationResult apply(ExperienceModificationContext context, long baseAmount) {
        if (baseAmount <= 0) {
            return new ExperienceModificationResult(0, List.of());
        }
        BigDecimal value = BigDecimal.valueOf(baseAmount);
        List<AbilityId> applied = new ArrayList<>();
        for (ExperienceModifier modifier : modifiers) {
            double multiplier = modifier.multiplier(context);
            if (!Double.isFinite(multiplier) || multiplier < 0.0D) {
                continue;
            }
            if (Double.compare(multiplier, 1.0D) != 0) {
                value = value.multiply(BigDecimal.valueOf(multiplier));
                applied.add(modifier.id());
            }
            if (value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) >= 0) {
                return new ExperienceModificationResult(Long.MAX_VALUE, applied);
            }
        }
        return new ExperienceModificationResult(
                value.setScale(0, RoundingMode.FLOOR).longValue(), applied);
    }
}
