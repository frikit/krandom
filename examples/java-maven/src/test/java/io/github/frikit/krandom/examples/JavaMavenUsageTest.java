package io.github.frikit.krandom.examples;

import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaMavenUsageTest {

    @Test
    void coreCanGenerateFixtureData() {
        UserFixture fixture = new UserFixture(
                Generators.ofFullName().generate(),
                Generators.ofEmail().generate(),
                Generators.ofCountry().generate()
        );

        assertNotNull(fixture.name());
        assertTrue(fixture.email().contains("@"));
        assertFalse(fixture.country().isBlank());
    }
}
