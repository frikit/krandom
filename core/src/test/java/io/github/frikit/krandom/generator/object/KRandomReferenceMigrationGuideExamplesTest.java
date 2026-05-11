/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("k-random migration guide examples")
class KRandomReferenceMigrationGuideExamplesTest {

    @Test
    @DisplayName("basic object generation example stays runnable")
    void basicObjectGenerationExampleStaysRunnable() {
        ObjectGenerator<User> users = Generators.ofObject(User.class);

        User user = users.generate();
        List<User> batch = users.generateList(10);

        assertAll(
            () -> assertNotNull(user),
            () -> assertNotNull(user.username),
            () -> assertEquals(10, batch.size()),
            () -> assertTrue(batch.stream().allMatch(value -> value.username != null))
        );
    }

    @Test
    @DisplayName("seeded generation example is repeatable")
    void seededGenerationExampleIsRepeatable() {
        GeneratorConfig firstConfig = GeneratorConfig.builder()
                                                     .seed(42L)
                                                     .build();
        GeneratorConfig secondConfig = GeneratorConfig.builder()
                                                      .seed(42L)
                                                      .build();

        UserRecord first = Generators.ofObject(UserRecord.class, firstConfig).generate();
        UserRecord second = Generators.ofObject(UserRecord.class, secondConfig).generate();

        assertEquals(first, second);
    }

    @Test
    @DisplayName("field override example stays runnable")
    void fieldOverrideExampleStaysRunnable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(User.class, "email", () -> "user@example.com")
                                                .build();

        User user = Generators.ofObject(User.class, config).generate();

        assertEquals("user@example.com", user.email);
    }

    @Test
    @DisplayName("type override example stays runnable")
    void typeOverrideExampleStaysRunnable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(PaymentMethod.class, CardPayment::new)
                                                .build();

        Checkout checkout = Generators.ofObject(Checkout.class, config).generate();

        assertInstanceOf(CardPayment.class, checkout.paymentMethod);
    }

    @Test
    @DisplayName("exclusion example stays runnable")
    void exclusionExampleStaysRunnable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectExcludeField("password")
                                                .build();

        SecretUser user = Generators.ofObject(SecretUser.class, config).generate();

        assertAll(
            () -> assertNotNull(user.username),
            () -> assertNull(user.password)
        );
    }

    @Test
    @DisplayName("Bean Validation example stays runnable")
    void beanValidationExampleStaysRunnable() {
        Account account = Generators.ofObject(Account.class).generate();

        assertAll(
            () -> assertNotNull(account.username),
            () -> assertFalse(account.username.isBlank()),
            () -> assertTrue(account.username.length() >= 3),
            () -> assertTrue(account.username.length() <= 16),
            () -> assertNotNull(account.email),
            () -> assertTrue(account.email.contains("@")),
            () -> assertNotNull(account.scores),
            () -> assertTrue(account.scores.size() >= 2),
            () -> assertTrue(account.scores.size() <= 4),
            () -> assertTrue(account.expiresAt.isAfter(Instant.now()))
        );
    }

    @Test
    @DisplayName("faker and domain generator example stays runnable")
    void fakerAndDomainGeneratorExampleStaysRunnable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(20260511L)
                                                .build();

        String firstName = Generators.person(config).firstName().generate();
        String email = Generators.ofEmail(config).generate();
        String city = Generators.location(config).city().generate();

        assertAll(
            () -> assertFalse(firstName.isBlank()),
            () -> assertTrue(email.contains("@")),
            () -> assertFalse(city.isBlank())
        );
    }

    static class User {
        String username;
        String email;
    }

    record UserRecord(String username, int age) {}

    interface PaymentMethod {}

    record CardPayment() implements PaymentMethod {}

    static class Checkout {
        PaymentMethod paymentMethod;
    }

    static class SecretUser {
        String username;
        String password;
    }

    static class Account {
        @NotBlank
        @Size(min = 3, max = 16)
        String username;

        @Email
        String email;

        @Size(min = 2, max = 4)
        List<Integer> scores;

        @Future
        Instant expiresAt;
    }
}
