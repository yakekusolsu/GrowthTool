package dev.yakekusolsu.growthtools.model;

import java.util.Locale;
import java.util.Optional;

/** A Paper-independent category of item supported by GrowthTools. */
public enum GrowthToolType {
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    FISHING_ROD,
    BOW;

    public static Optional<GrowthToolType> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
