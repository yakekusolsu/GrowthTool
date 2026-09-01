package dev.yakekusolsu.growthtools.api.ability;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Stable, Paper-independent namespaced identifier for an ability. */
public record AbilityId(String namespace, String key) implements Comparable<AbilityId> {
    private static final int MAX_PART_LENGTH = 64;
    private static final Pattern PART = Pattern.compile("[a-z0-9._-]+");

    public AbilityId {
        namespace = normalize(namespace, "namespace");
        key = normalize(key, "key");
    }

    public static AbilityId parse(String value) {
        Objects.requireNonNull(value, "value");
        String[] parts = value.split(":", -1);
        if (parts.length == 1) {
            return new AbilityId("growthtools", parts[0]);
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Ability ID must be namespace:key");
        }
        return new AbilityId(parts[0], parts[1]);
    }

    private static String normalize(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_PART_LENGTH || !PART.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid ability " + name + ": " + value);
        }
        return normalized;
    }

    @Override
    public String toString() {
        return namespace + ':' + key;
    }

    @Override
    public int compareTo(AbilityId other) {
        return toString().compareTo(other.toString());
    }
}
