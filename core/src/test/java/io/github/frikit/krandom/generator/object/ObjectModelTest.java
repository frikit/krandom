/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectModel")
class ObjectModelTest {

    @Test
    @DisplayName("replays reusable configuration into fresh fakers")
    void replaysConfigurationIntoFreshFakers() {
        ObjectModel<User> model = ObjectModel.of(User.class)
            .configure(faker -> faker
                .ruleFor(User::getFirstName, () -> "Ada")
                .ruleFor(User::getLastName, () -> "Lovelace")
                .ruleFor(User::getEmail,
                    user -> user.firstName.toLowerCase() + "." + user.lastName.toLowerCase() + "@example.com"));

        assertEquals("ada.lovelace@example.com", model.generate().email);
        assertEquals("ada.lovelace@example.com", model.generate().email);
    }

    @Test
    @DisplayName("configuration returns a new model and leaves the source unchanged")
    void configurationIsImmutable() {
        ObjectModel<User> base = ObjectModel.of(User.class);
        ObjectModel<User> configured = base.configure(faker -> faker.ruleFor(User::getFirstName, () -> "Ada"));

        assertNotNull(base.generate().firstName);
        assertEquals("Ada", configured.generate().firstName);
    }

    @Test
    @DisplayName("models compose in order")
    void modelsComposeInOrder() {
        ObjectModel<User> names = ObjectModel.of(User.class)
            .configure(faker -> faker
                .ruleFor(User::getFirstName, () -> "Ada")
                .ruleFor(User::getLastName, () -> "Lovelace"));
        ObjectModel<User> email = ObjectModel.of(User.class)
            .configure(faker -> faker.ruleFor(
                User::getEmail,
                user -> user.firstName.toLowerCase() + "@example.com"));

        User generated = names.and(email).generate();

        assertEquals("Ada", generated.firstName);
        assertEquals("ada@example.com", generated.email);
    }

    @Test
    @DisplayName("typed include is retained by a reusable model")
    void typedIncludeIsRetained() {
        User generated = ObjectModel.of(User.class)
            .configure(faker -> faker.include(User::getFirstName))
            .generate();

        assertNotNull(generated.firstName);
        assertNull(generated.lastName);
        assertNull(generated.email);
    }

    @Test
    @DisplayName("accepts explicit generator configuration")
    void acceptsExplicitGeneratorConfiguration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        ObjectModel<User> model = ObjectModel.of(User.class)
            .configure(faker -> faker.ruleFor(User::getFirstName, () -> "Ada"));

        assertEquals("Ada", model.faker(config).generate().firstName);
        assertEquals("Ada", model.generate(config).firstName);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @DisplayName("rejects composition across different object types")
    void rejectsCompositionAcrossTypes() {
        ObjectModel<User> users = ObjectModel.of(User.class);
        ObjectModel other = ObjectModel.of(String.class);

        assertThrows(IllegalArgumentException.class, () -> users.and(other));
    }

    static final class User {
        String firstName;
        String lastName;
        String email;

        String getFirstName() {
            return firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getEmail() {
            return email;
        }
    }
}
