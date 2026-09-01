package dev.yakekusolsu.growthtools.command;

import java.util.Locale;
import java.util.Optional;

public enum Subcommand {
    VERSION,
    RELOAD,
    GIVE,
    INSPECT,
    DEBUG,
    REPAIR,
    REGENERATE_ID,
    ABILITY,
    INTEGRATIONS,
    DOCTOR;

    public static Optional<Subcommand> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.replace('-', '_').toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public String argument() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
