package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.util.Objects;

public record ExperienceModificationContext(GrowthToolData tool, ExperienceSource source) {
    public ExperienceModificationContext {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(source, "source");
    }
}
