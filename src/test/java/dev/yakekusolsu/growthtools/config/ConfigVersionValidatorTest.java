package dev.yakekusolsu.growthtools.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigVersionValidatorTest {
    private final ConfigVersionValidator validator = new ConfigVersionValidator(2);

    @Test
    void classifiesConfigVersions() {
        assertEquals(ConfigVersionValidator.Result.INVALID, validator.validate(-1));
        assertEquals(ConfigVersionValidator.Result.MISSING, validator.validate(0));
        assertEquals(ConfigVersionValidator.Result.OLDER, validator.validate(1));
        assertEquals(ConfigVersionValidator.Result.CURRENT, validator.validate(2));
        assertEquals(ConfigVersionValidator.Result.NEWER, validator.validate(3));
    }
}
