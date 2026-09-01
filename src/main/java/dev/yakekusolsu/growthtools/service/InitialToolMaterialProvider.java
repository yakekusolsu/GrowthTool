package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

/** Provides the initial item for admin-created tools and can later be backed by configuration. */
public final class InitialToolMaterialProvider {
    private final Map<GrowthToolType, Material> materials;

    public InitialToolMaterialProvider() {
        EnumMap<GrowthToolType, Material> defaults = new EnumMap<>(GrowthToolType.class);
        defaults.put(GrowthToolType.PICKAXE, Material.DIAMOND_PICKAXE);
        defaults.put(GrowthToolType.AXE, Material.DIAMOND_AXE);
        defaults.put(GrowthToolType.SHOVEL, Material.DIAMOND_SHOVEL);
        defaults.put(GrowthToolType.HOE, Material.DIAMOND_HOE);
        defaults.put(GrowthToolType.FISHING_ROD, Material.FISHING_ROD);
        defaults.put(GrowthToolType.BOW, Material.BOW);
        materials = Map.copyOf(defaults);
    }

    public Material get(GrowthToolType type) {
        Material material = materials.get(type);
        if (material == null) {
            throw new IllegalArgumentException("No initial material configured for " + type);
        }
        return material;
    }
}
