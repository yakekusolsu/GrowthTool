package dev.yakekusolsu.growthtools.api.internal;

import dev.yakekusolsu.growthtools.api.tool.GrowthToolSnapshot;
import dev.yakekusolsu.growthtools.api.tool.ToolManager;
import dev.yakekusolsu.growthtools.service.ExperienceService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public final class ToolManagerImpl implements ToolManager {
    private final ApiLifecycle lifecycle;
    private final GrowthToolItemService items;
    private final ExperienceService experience;
    public ToolManagerImpl(ApiLifecycle lifecycle, GrowthToolItemService items,
            ExperienceService experience) {
        this.lifecycle = lifecycle; this.items = items; this.experience = experience;
    }
    @Override public boolean isGrowthTool(ItemStack item) {
        lifecycle.ensureActive(); MainThreadGuard.check(); return items.isGrowthTool(item);
    }
    @Override public Optional<GrowthToolSnapshot> getTool(ItemStack item) {
        lifecycle.ensureActive(); MainThreadGuard.check();
        return items.read(item).map(data -> ToolSnapshotMapper.map(data, experience.maximumLevel()));
    }
}
