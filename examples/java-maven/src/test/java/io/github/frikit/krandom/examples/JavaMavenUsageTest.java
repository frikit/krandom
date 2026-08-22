package io.github.frikit.krandom.examples;

import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectModel;
import org.junit.jupiter.api.Test;

import java.util.Locale;

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

    @Test
    void publicTypedObjectModelApiCompilesAndGenerates() {
        ObjectModel<UserFixture> model = ObjectModel.of(UserFixture.class)
                .configure(faker -> faker
                        .ruleFor(UserFixture::name, () -> "Ada Lovelace")
                        .ruleFor(UserFixture::email, user -> user.name().toLowerCase(Locale.ROOT)
                                .replace(' ', '.') + "@example.com")
                        .ruleFor(UserFixture::country, () -> "United Kingdom")
                        .strict());

        assertTrue(model.generate().email().endsWith("@example.com"));
    }
}
