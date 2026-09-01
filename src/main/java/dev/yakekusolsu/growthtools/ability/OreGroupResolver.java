package dev.yakekusolsu.growthtools.ability;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;

public final class OreGroupResolver {
    public Optional<Set<Material>> groupFor(Material material, Map<String, Set<Material>> groups) {
        return groups.values().stream().filter(group -> group.contains(material)).findFirst();
    }
}
