package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Mutable registration boundary; exposed definitions remain immutable. */
public final class AbilityRegistry {
    private final Map<AbilityId, AbilityDefinition> definitions = new LinkedHashMap<>();

    public void register(AbilityDefinition definition) {
        if (definitions.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException("Duplicate ability ID: " + definition.id());
        }
    }

    public Optional<AbilityDefinition> get(AbilityId id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public boolean contains(AbilityId id) {
        return definitions.containsKey(id);
    }

    public Optional<AbilityDefinition> unregister(AbilityId id) {
        return Optional.ofNullable(definitions.remove(id));
    }

    public Collection<AbilityDefinition> getAll() {
        return List.copyOf(definitions.values());
    }

}
