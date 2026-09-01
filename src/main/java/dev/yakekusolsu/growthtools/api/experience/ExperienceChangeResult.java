package dev.yakekusolsu.growthtools.api.experience;

import dev.yakekusolsu.growthtools.api.tool.GrowthToolSnapshot;

/** Read-only result after the normal PDC, lore, registry and event pipeline. */
public record ExperienceChangeResult(
        GrowthToolSnapshot before, GrowthToolSnapshot after, long requested, long added) { }
