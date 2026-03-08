package org.github.krandom.examples;

import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaMavenUsageTest {

    @Test
    void javaApiCanGenerateFixtureData() {
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
