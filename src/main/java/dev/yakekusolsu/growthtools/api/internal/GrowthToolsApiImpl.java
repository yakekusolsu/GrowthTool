package dev.yakekusolsu.growthtools.api.internal;

import dev.yakekusolsu.growthtools.api.*;
import dev.yakekusolsu.growthtools.api.ability.AbilityManager;
import dev.yakekusolsu.growthtools.api.experience.ExperienceManager;
import dev.yakekusolsu.growthtools.api.tool.ToolManager;

public final class GrowthToolsApiImpl implements GrowthToolsAPI {
    private final ApiLifecycle lifecycle;
    private final ToolManager tools;
    private final AbilityManager abilities;
    private final ExperienceManager experience;
    public GrowthToolsApiImpl(ApiLifecycle lifecycle, ToolManager tools,
            AbilityManager abilities, ExperienceManager experience) {
        this.lifecycle = lifecycle; this.tools = tools; this.abilities = abilities;
        this.experience = experience;
    }
    @Override public ToolManager tools() { lifecycle.ensureActive(); return tools; }
    @Override public AbilityManager abilities() { lifecycle.ensureActive(); return abilities; }
    @Override public ExperienceManager experience() { lifecycle.ensureActive(); return experience; }
    @Override public ApiVersion version() { lifecycle.ensureActive(); return ApiVersion.CURRENT; }
}
