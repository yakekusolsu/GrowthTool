package dev.yakekusolsu.growthtools.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GrowthToolDataTest {
    private static final UUID TOOL_ID = UUID.fromString("e44b42e7-fd99-4ef8-a010-b1a029be5a7d");

    @Test
    void preservesValidatedImmutableState() {
        GrowthToolData data = new GrowthToolData(
                TOOL_ID, GrowthToolType.AXE, 3, 300, 1_700_000_000_000L, 1);

        assertEquals(TOOL_ID, data.toolId());
        assertEquals(GrowthToolType.AXE, data.type());
        assertEquals(3, data.level());
        assertEquals(300, data.experience());

        GrowthToolData updated = data.withExperience(600, 4);
        assertNotEquals(data, updated);
        assertEquals(300, data.experience());
        assertEquals(TOOL_ID, updated.toolId());
        assertEquals(data.createdAt(), updated.createdAt());
    }

    @Test
    void rejectsInvalidState() {
        assertThrows(NullPointerException.class,
                () -> new GrowthToolData(null, GrowthToolType.AXE, 1, 0, 0, 1));
        assertThrows(NullPointerException.class,
                () -> new GrowthToolData(TOOL_ID, null, 1, 0, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GrowthToolData(TOOL_ID, GrowthToolType.AXE, 0, 0, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GrowthToolData(TOOL_ID, GrowthToolType.AXE, 1, -1, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GrowthToolData(TOOL_ID, GrowthToolType.AXE, 1, 0, -1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GrowthToolData(TOOL_ID, GrowthToolType.AXE, 1, 0, 0, 0));
    }
}
