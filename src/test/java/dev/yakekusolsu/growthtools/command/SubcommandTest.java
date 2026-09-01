package dev.yakekusolsu.growthtools.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SubcommandTest {
    @Test
    void parsesSubcommandsWithoutCaseSensitivity() {
        assertEquals(Subcommand.VERSION, Subcommand.parse("VeRsIoN").orElseThrow());
        assertEquals(Subcommand.RELOAD, Subcommand.parse("reload").orElseThrow());
        assertEquals(Subcommand.REGENERATE_ID,
                Subcommand.parse("regenerate-id").orElseThrow());
    }

    @Test
    void rejectsUnknownSubcommand() {
        assertTrue(Subcommand.parse("levels").isEmpty());
    }
}
