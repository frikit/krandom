/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectFaker typed rules")
class ObjectFakerTypedRuleTest {

    @Test
    @DisplayName("typed root and nested paths apply generator rules")
    void typedPathsApplyGeneratorRules() {
        User generated = new ObjectFaker<>(User.class)
            .ruleFor(User::getFirstName, () -> "Ada")
            .ruleFor(PropertyPath.of(User::getAddress).then(Address::getCity), () -> "London")
            .generate();

        assertEquals("Ada", generated.firstName);
        assertEquals("London", generated.address.city);
    }

    @Test
    @DisplayName("typed dependent rules express correlated assignments")
    void typedDependentRulesExpressAssignments() {
        User generated = new ObjectFaker<>(User.class)
            .ruleFor(User::getFirstName, () -> "Ada")
            .ruleFor(User::getLastName, () -> "Lovelace")
            .ruleFor(User::getEmail,
                user -> user.firstName.toLowerCase() + "." + user.lastName.toLowerCase() + "@example.com")
            .generate();

        assertEquals("ada.lovelace@example.com", generated.email);
    }

    @Test
    @DisplayName("typed contextual rules receive root and nested field context")
    void typedContextualRulesReceiveFieldContext() {
        User generated = new ObjectFaker<>(User.class)
            .ruleForContext(User::getFirstName, context -> context.getFieldName())
            .ruleForContext(
                PropertyPath.of(User::getAddress).then(Address::getCity),
                context -> context.getOwnerType().getSimpleName() + "." + context.getFieldName())
            .ignore(User::getEmail)
            .generate();

        assertEquals("firstName", generated.firstName);
        assertEquals("Address.city", generated.address.city);
        assertNull(generated.email);
    }

    @Test
    @DisplayName("typed nested ignore clears only the selected property")
    void typedNestedIgnoreClearsSelectedProperty() {
        User generated = new ObjectFaker<>(User.class)
            .ignore(PropertyPath.of(User::getAddress).then(Address::getCity))
            .generate();

        assertNull(generated.address.city);
    }

    @Test
    @DisplayName("strict mode validates the complete rule configuration before generation")
    void strictModeValidatesBeforeGeneration() {
        ObjectFaker<User> incomplete = new ObjectFaker<>(User.class)
            .ruleFor(User::getFirstName, () -> "Ada")
            .strict();

        assertThrows(IllegalStateException.class, incomplete::generate);
    }

    static final class User {
        String firstName;
        String lastName;
        String email;
        Address address;

        String getFirstName() {
            return firstName;
        }

        String getLastName() {
            return lastName;
        }

        String getEmail() {
            return email;
        }

        Address getAddress() {
            return address;
        }
    }

    static final class Address {
        String city;
        String postalCode;

        String getCity() {
            return city;
        }

        String getPostalCode() {
            return postalCode;
        }
    }
}
