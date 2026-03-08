package org.github.krandom.examples;

import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaGradleUsageTest {

    @Test
    void javaApiCanGenerateFixtureData() {
        String name = Generators.ofFullName().generate();
        String email = Generators.ofEmail().generate();
        String country = Generators.ofCountry().generate();

        UserFixture fixture = new UserFixture(name, email, country);

        assertNotNull(fixture.name());
        assertTrue(fixture.email().contains("@"));
        assertFalse(fixture.country().isBlank());
    }

    @Test
    void seededGenerationIsDeterministic() {
        int a = Generators.ofInt(1, 100, 42L).generate();
        int b = Generators.ofInt(1, 100, 42L).generate();
        assertEquals(a, b);
    }
}
