package dev.yakekusolsu.growthtools.api.experience;

import dev.yakekusolsu.growthtools.api.ability.AbilityId;

/** Namespaced provenance for API-granted EXP. */
public record ExperienceSourceId(String namespace, String key) {
    public static final ExperienceSourceId API = new ExperienceSourceId("growthtools", "api");
    public ExperienceSourceId {
        AbilityId validated = new AbilityId(namespace, key);
        namespace = validated.namespace();
        key = validated.key();
    }
    @Override public String toString() { return namespace + ':' + key; }
}
