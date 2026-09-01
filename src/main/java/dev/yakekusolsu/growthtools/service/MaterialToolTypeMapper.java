package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Optional;
import org.bukkit.Material;

/** Isolates the Paper Material naming rules from the domain tool type. */
public final class MaterialToolTypeMapper {
    public Optional<GrowthToolType> fromMaterial(Material material) {
        if (material == null || material.isAir()) {
            return Optional.empty();
        }

        String name = material.name();
        if (name.endsWith("_PICKAXE")) {
            return Optional.of(GrowthToolType.PICKAXE);
        }
        if (name.endsWith("_AXE")) {
            return Optional.of(GrowthToolType.AXE);
        }
        if (name.endsWith("_SHOVEL")) {
            return Optional.of(GrowthToolType.SHOVEL);
        }
        if (name.endsWith("_HOE")) {
            return Optional.of(GrowthToolType.HOE);
        }
        return switch (material) {
            case FISHING_ROD -> Optional.of(GrowthToolType.FISHING_ROD);
            case BOW -> Optional.of(GrowthToolType.BOW);
            default -> Optional.empty();
        };
    }
}
