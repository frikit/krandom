/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.gofakeit;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GoFakeit phase-1 facade")
class GoFakeitGeneratorTest {

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultLocale() {
        GoFakeitGenerator generator = new GoFakeitGenerator();
        assertEquals(Locale.US, generator.getLocale());
    }

    @Test
    @DisplayName("locale constructor keeps configured locale")
    void localeCtor() {
        GoFakeitGenerator generator = new GoFakeitGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, generator.getLocale());
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfigRejected() {
        assertThrows(NullPointerException.class, () -> new GoFakeitGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("identity aliases return non-empty values")
    void identityAliases() {
        GoFakeitGenerator generator = seeded(Locale.US);
        assertFalse(generator.firstName().isBlank());
        assertFalse(generator.lastName().isBlank());
        assertFalse(generator.name().isBlank());
        assertFalse(generator.namePrefix().isBlank());
        assertFalse(generator.nameSuffix().isBlank());
        assertFalse(generator.gender().isBlank());
        assertTrue(generator.age() >= 1);
        assertFalse(generator.password().isBlank());
        assertFalse(generator.password(10, 12).isBlank());
    }

    @Test
    @DisplayName("password policy validates inputs and enforces enabled classes")
    void passwordPolicy() {
        GoFakeitGenerator generator = seeded(Locale.US);

        assertThrows(IllegalArgumentException.class,
                () -> generator.password(false, false, false, false, 8, 12));
        assertThrows(IllegalArgumentException.class,
                () -> generator.password(true, false, false, false, 0, 8));
        assertThrows(IllegalArgumentException.class,
                () -> generator.password(true, false, false, false, 10, 9));
        assertThrows(IllegalArgumentException.class,
                () -> generator.password(true, true, true, true, 2, 3));

        String value = generator.password(true, true, true, true, 12, 12);
        assertEquals(12, value.length());
        assertTrue(value.chars().anyMatch(Character::isLowerCase));
        assertTrue(value.chars().anyMatch(Character::isUpperCase));
        assertTrue(value.chars().anyMatch(Character::isDigit));
        assertTrue(value.chars().anyMatch(ch -> "!@#$%^&*()-_=+[]{};:,.?".indexOf(ch) >= 0));

        String lowerOnly = generator.password(true, false, false, false, 8, 8);
        assertTrue(lowerOnly.matches("[a-z]{8}"));
        String upperOnly = generator.password(false, true, false, false, 8, 8);
        assertTrue(upperOnly.matches("[A-Z]{8}"));
        String numericOnly = generator.password(false, false, true, false, 8, 8);
        assertTrue(numericOnly.matches("[0-9]{8}"));
        String specialOnly = generator.password(false, false, false, true, 8, 8);
        assertTrue(specialOnly.chars().allMatch(ch -> "!@#$%^&*()-_=+[]{};:,.?".indexOf(ch) >= 0));
    }

    @Test
    @DisplayName("address aliases and composite payload are populated")
    void addressAliasesAndComposite() {
        GoFakeitGenerator generator = seeded(Locale.US);
        assertFalse(generator.street().isBlank());
        assertFalse(generator.streetNumber().isBlank());
        assertFalse(generator.streetName().isBlank());
        assertFalse(generator.streetSuffix().isBlank());
        assertFalse(generator.streetPrefix().isBlank());
        assertFalse(generator.streetUnit().isBlank());
        assertFalse(generator.city().isBlank());
        assertFalse(generator.state().isBlank());
        assertFalse(generator.stateAbbr().isBlank());
        assertFalse(generator.zip().isBlank());
        assertFalse(generator.country().isBlank());
        assertFalse(generator.countryAbbr().isBlank());

        AddressInfo address = generator.address();
        assertNotNull(address);
        assertFalse(address.address().isBlank());
        assertFalse(address.street().isBlank());
        assertFalse(address.streetNumber().isBlank());
        assertFalse(address.streetName().isBlank());
        assertFalse(address.streetSuffix().isBlank());
        assertFalse(address.streetPrefix().isBlank());
        assertFalse(address.streetUnit().isBlank());
        assertFalse(address.city().isBlank());
        assertFalse(address.state().isBlank());
        assertFalse(address.stateAbbr().isBlank());
        assertFalse(address.zip().isBlank());
        assertFalse(address.country().isBlank());
        assertFalse(address.countryAbbr().isBlank());
    }

    @Test
    @DisplayName("contact and person composites are populated")
    void contactAndPersonComposite() {
        GoFakeitGenerator generator = seeded(Locale.US);

        assertFalse(generator.phone().isBlank());
        assertFalse(generator.phoneFormatted().isBlank());
        assertFalse(generator.email().isBlank());

        ContactInfo contact = generator.contact();
        assertNotNull(contact);
        assertFalse(contact.firstName().isBlank());
        assertFalse(contact.lastName().isBlank());
        assertFalse(contact.name().isBlank());
        assertFalse(contact.gender().isBlank());
        assertTrue(contact.age() >= 1);
        assertFalse(contact.phone().isBlank());
        assertFalse(contact.phoneFormatted().isBlank());
        assertFalse(contact.email().isBlank());

        PersonInfo person = generator.person();
        assertNotNull(person);
        assertNotNull(person.contact());
        assertNotNull(person.address());
        assertFalse(person.username().isBlank());
        assertFalse(person.password().isBlank());
    }

    @Test
    @DisplayName("network aliases are populated and status simplification covers categories")
    void networkAliases() {
        GoFakeitGenerator generator = seeded(Locale.US);
        assertFalse(generator.domainName().isBlank());
        assertFalse(generator.domainSuffix().isBlank());
        assertFalse(generator.url().isBlank());
        assertFalse(generator.urlSlug().isBlank());
        assertFalse(generator.macAddress().isBlank());
        int port = generator.port();
        assertTrue(port >= 1 && port <= 65535);
        assertFalse(generator.httpMethod().isBlank());
        int status = generator.httpStatusCode();
        assertTrue(status >= 100 && status <= 599);
        assertFalse(generator.userAgent().isBlank());

        assertEquals("informational", generator.httpStatusSimple(100));
        assertEquals("success", generator.httpStatusSimple(200));
        assertEquals("redirection", generator.httpStatusSimple(300));
        assertEquals("client_error", generator.httpStatusSimple(400));
        assertEquals("server_error", generator.httpStatusSimple(500));

        Set<String> categories = Set.of("informational", "success", "redirection", "client_error", "server_error");
        assertTrue(categories.contains(generator.httpStatusSimple()));
    }

    @Test
    @DisplayName("date/text aliases return expected shapes")
    void dateTextAliases() {
        GoFakeitGenerator generator = seeded(Locale.US);
        LocalDate date = generator.date();
        assertNotNull(date);

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);
        LocalDate inRange = generator.dateRange(from, to);
        assertFalse(inRange.isBefore(from));
        assertFalse(inRange.isAfter(to));

        assertTrue(generator.futureDate().isAfter(LocalDate.now()));
        assertTrue(generator.pastDate().isBefore(LocalDate.now()));

        assertFalse(generator.word().isBlank());
        assertFalse(generator.sentence().isBlank());
        assertTrue(generator.sentence().endsWith("."));
        assertFalse(generator.paragraph().isBlank());
        assertFalse(generator.lorem().isBlank());
    }

    @Test
    @DisplayName("template helpers apply numerify lexify bothify asciify")
    void templateHelpers() {
        GoFakeitGenerator generator = seeded(Locale.US);
        assertTrue(generator.numerify("###-###").matches("\\d\\d\\d-\\d\\d\\d"));
        assertTrue(generator.lexify("???-???").matches("[a-z]{3}-[a-z]{3}"));
        assertTrue(generator.bothify("??-##").matches("[a-z]{2}-\\d{2}"));

        String asciified = generator.asciify("A***Z");
        assertEquals(5, asciified.length());
        assertEquals('A', asciified.charAt(0));
        assertEquals('Z', asciified.charAt(4));
        for (int i = 1; i <= 3; i++) {
            int code = asciified.charAt(i);
            assertTrue(code >= 33 && code <= 126);
        }
        assertThrows(NullPointerException.class, () -> generator.asciify(null));
    }

    @Test
    @DisplayName("street prefix varies with locale")
    void streetPrefixLocaleAware() {
        assertTrue(Set.of("Nord", "Sud", "Ost", "West")
                .contains(seeded(Locale.GERMANY).streetPrefix()));
        assertTrue(Set.of("N", "S", "E", "W")
                .contains(seeded(Locale.US).streetPrefix()));
    }

    private static GoFakeitGenerator seeded(Locale locale) {
        return new GoFakeitGenerator(GeneratorConfig.builder().locale(locale).seed(1234L).build());
    }
}
