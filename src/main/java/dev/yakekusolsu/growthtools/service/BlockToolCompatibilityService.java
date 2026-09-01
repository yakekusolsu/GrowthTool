package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

/**
 * Resolves the natural block categories for GrowthTools.
 *
 * <p>Vanilla tags are preferred, but server data packs and Paper versions can change their
 * contents. Naming conventions and small fallback groups keep the core categories stable without
 * duplicating every vanilla block in code.</p>
 */
public final class BlockToolCompatibilityService {
    private static final Set<Material> PICKAXE_FALLBACKS = EnumSet.of(
            Material.STONE,
            Material.COBBLESTONE,
            Material.DEEPSLATE,
            Material.COBBLED_DEEPSLATE,
            Material.ANDESITE,
            Material.DIORITE,
            Material.GRANITE,
            Material.TUFF,
            Material.CALCITE,
            Material.BLACKSTONE,
            Material.BASALT,
            Material.NETHERRACK,
            Material.END_STONE,
            Material.OBSIDIAN,
            Material.CRYING_OBSIDIAN,
            Material.BRICKS,
            Material.NETHER_BRICKS,
            Material.ANCIENT_DEBRIS);

    private static final Set<Material> AXE_FALLBACKS = EnumSet.of(
            Material.BAMBOO_BLOCK,
            Material.STRIPPED_BAMBOO_BLOCK,
            Material.MANGROVE_ROOTS,
            Material.MUDDY_MANGROVE_ROOTS);

    private static final Set<Material> SHOVEL_FALLBACKS = EnumSet.of(
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.COARSE_DIRT,
            Material.ROOTED_DIRT,
            Material.PODZOL,
            Material.MYCELIUM,
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL,
            Material.CLAY,
            Material.SOUL_SAND,
            Material.SOUL_SOIL,
            Material.SNOW,
            Material.SNOW_BLOCK,
            Material.POWDER_SNOW,
            Material.MUD);

    private static final Set<Material> AGEABLE_CROPS = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART);

    private static final Set<Material> HOE_FALLBACKS = EnumSet.of(
            Material.MELON,
            Material.PUMPKIN,
            Material.HAY_BLOCK,
            Material.MOSS_BLOCK,
            Material.SCULK,
            Material.SCULK_VEIN,
            Material.SCULK_CATALYST,
            Material.SCULK_SENSOR,
            Material.SCULK_SHRIEKER);

    public boolean isCompatible(GrowthToolType type, Material blockMaterial) {
        return switch (type) {
            case PICKAXE -> isTagged(Tag.MINEABLE_PICKAXE, blockMaterial)
                    || followsPickaxeNaming(blockMaterial)
                    || PICKAXE_FALLBACKS.contains(blockMaterial);
            case AXE -> isTagged(Tag.MINEABLE_AXE, blockMaterial)
                    || followsAxeNaming(blockMaterial)
                    || AXE_FALLBACKS.contains(blockMaterial);
            case SHOVEL -> isTagged(Tag.MINEABLE_SHOVEL, blockMaterial)
                    || followsShovelNaming(blockMaterial)
                    || SHOVEL_FALLBACKS.contains(blockMaterial);
            case HOE -> isTagged(Tag.MINEABLE_HOE, blockMaterial)
                    || isTagged(Tag.CROPS, blockMaterial)
                    || followsHoeNaming(blockMaterial)
                    || AGEABLE_CROPS.contains(blockMaterial)
                    || HOE_FALLBACKS.contains(blockMaterial);
            case FISHING_ROD, BOW -> false;
        };
    }

    /** Returns whether an ageable crop is mature enough to award block EXP. */
    public boolean isMatureForExperience(GrowthToolType type, Block block) {
        if (type != GrowthToolType.HOE || !AGEABLE_CROPS.contains(block.getType())) {
            return true;
        }
        return block.getBlockData() instanceof Ageable ageable
                && ageable.getAge() == ageable.getMaximumAge();
    }

    /**
     * Mature crops may award EXP despite being player planted. Other placed blocks stay protected
     * from repeatable EXP farming.
     */
    public boolean permitsPlacedBlockExperience(GrowthToolType type, Block block) {
        return type == GrowthToolType.HOE
                && AGEABLE_CROPS.contains(block.getType())
                && isMatureForExperience(type, block);
    }

    private static boolean isTagged(Tag<Material> tag, Material material) {
        return tag != null && tag.isTagged(material);
    }

    private static boolean followsPickaxeNaming(Material material) {
        String name = material.name();
        return name.endsWith("_ORE")
                || name.contains("PRISMARINE")
                || name.endsWith("STONE_BRICKS")
                || name.endsWith("DEEPSLATE_BRICKS")
                || name.endsWith("DEEPSLATE_TILES");
    }

    private static boolean followsAxeNaming(Material material) {
        String name = material.name();
        return name.endsWith("_LOG")
                || name.endsWith("_WOOD")
                || name.endsWith("_STEM")
                || name.endsWith("_HYPHAE")
                || name.endsWith("_PLANKS");
    }

    private static boolean followsShovelNaming(Material material) {
        String name = material.name();
        return name.endsWith("_DIRT")
                || name.endsWith("_SAND")
                || name.startsWith("SNOW");
    }

    private static boolean followsHoeNaming(Material material) {
        String name = material.name();
        return name.endsWith("_LEAVES")
                || name.startsWith("SCULK");
    }
}
