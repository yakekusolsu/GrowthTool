package dev.yakekusolsu.growthtools.api;

import dev.yakekusolsu.growthtools.api.ability.AbilityManager;
import dev.yakekusolsu.growthtools.api.experience.ExperienceManager;
import dev.yakekusolsu.growthtools.api.tool.ToolManager;

/**
 * Root of the experimental GrowthTools API.
 * Manager access and {@link #version()} are async-safe; manager methods document stricter rules.
 * Every method throws {@link IllegalStateException} after GrowthTools is disabled.
 */
public interface GrowthToolsAPI {
    /** Returns the read-only tool API. */
    ToolManager tools();
    /** Returns addon registration and ownership operations. */
    AbilityManager abilities();
    /** Returns the safe progression mutation API. */
    ExperienceManager experience();
    /** Returns a plugin-version-independent, non-null API version. */
    ApiVersion version();
}
