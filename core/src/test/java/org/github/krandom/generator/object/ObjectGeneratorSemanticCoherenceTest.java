/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.location.AddressInfo;
import org.github.krandom.generator.location.AddressInfoGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator semantic coherence")
class ObjectGeneratorSemanticCoherenceTest {

    @Test
    @DisplayName("mutable classes align related semantic fields")
    void mutableClassesAlignRelatedSemanticFields() {
        CoherentPerson value = new ObjectGenerator<>(CoherentPerson.class).generate();

        assertEquals(value.firstName + " " + value.lastName, value.fullName);
        assertTrue(value.email.startsWith(slug(value.firstName) + "." + slug(value.lastName) + "@"));
        assertEquals(value.domain, emailDomain(value.email));
        assertEquals("https://www." + value.domain, value.url);
        assertFalse(value.createdAt.toInstant(ZoneOffset.UTC).isAfter(value.updatedAt));
    }

    @Test
    @DisplayName("records align related semantic fields")
    void recordsAlignRelatedSemanticFields() {
        CoherentPersonRecord value = new ObjectGenerator<>(CoherentPersonRecord.class).generate();

        assertEquals(value.firstName() + " " + value.lastName(), value.fullName());
        assertTrue(value.email().startsWith(slug(value.firstName()) + "." + slug(value.lastName()) + "@"));
        assertEquals(value.domain(), emailDomain(value.email()));
        assertEquals("https://www." + value.domain(), value.url());
        assertFalse(value.createdAt().toInstant(ZoneOffset.UTC).isAfter(value.updatedAt()));
    }

    @Test
    @DisplayName("explicit field overrides still win over semantic coherence")
    void explicitFieldOverridesStillWinOverSemanticCoherence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(CoherentPerson.class, "email",
                                                                      () -> "custom@example.org")
                                                            .build();

        CoherentPerson value = new ObjectGenerator<>(CoherentPerson.class, config).generate();

        assertEquals("custom@example.org", value.email);
        assertEquals(value.firstName + " " + value.lastName, value.fullName);
    }

    @Test
    @DisplayName("relaxed mode keeps annotated email fields untouched during coherence")
    void relaxedModeKeepsAnnotatedEmailFieldsUntouchedDuringCoherence() {
        AnnotatedEmailPerson value = new ObjectGenerator<>(AnnotatedEmailPerson.class).generate();

        assertEquals("ANNOTATED", value.email);
        assertEquals(value.firstName + " " + value.lastName, value.fullName);
    }

    @Test
    @DisplayName("derived emails stay unique across one generator sequence")
    void derivedEmailsStayUniqueAcrossOneGeneratorSequence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(FixedNamePerson.class, "firstName", () -> "Alice")
                                                            .override(FixedNamePerson.class, "lastName", () -> "Smith")
                                                            .override(FixedNamePerson.class, "domain", () -> "example.com")
                                                            .build();

        ObjectGenerator<FixedNamePerson> generator = new ObjectGenerator<>(FixedNamePerson.class, config);

        String first = generator.generate().email;
        String second = generator.generate().email;
        String third = generator.generate().email;

        assertEquals("alice.smith@example.com", first);
        assertEquals("alice.smith1@example.com", second);
        assertEquals("alice.smith2@example.com", third);
    }

    @Test
    @DisplayName("createdAt never lands after updatedAt across seeded sequences")
    void createdAtNeverLandsAfterUpdatedAtAcrossSeededSequences() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        ObjectGenerator<TemporalCoherenceHolder> generator = new ObjectGenerator<>(TemporalCoherenceHolder.class, config);

        for (int i = 0; i < 40; i++) {
            TemporalCoherenceHolder value = generator.generate();
            assertFalse(value.createdAt.toInstant(ZoneOffset.UTC).isAfter(value.updatedAt));
        }
    }

    @Test
    @DisplayName("strict mode can still overwrite annotated targets through semantic coherence")
    void strictModeCanStillOverwriteAnnotatedTargetsThroughSemanticCoherence() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                .build();

        AnnotatedEmailPerson value = new ObjectGenerator<>(AnnotatedEmailPerson.class, config).generate();

        assertNotEquals("ANNOTATED", value.email);
        assertTrue(value.email.startsWith(slug(value.firstName) + "." + slug(value.lastName) + "@"));
    }

    @Test
    @DisplayName("birthDate and age stay aligned")
    void birthDateAndAgeStayAligned() {
        AgeCoherenceHolder value = new ObjectGenerator<>(AgeCoherenceHolder.class).generate();

        assertEquals((int) ChronoUnit.YEARS.between(value.dateOfBirth, LocalDate.now()), value.ageYears);
    }

    @Test
    @DisplayName("birthDate is rebuilt from overridden age when age is protected")
    void birthDateIsRebuiltFromProtectedAge() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(AgeCoherenceHolder.class, "ageYears", () -> 42)
                                                            .build();

        AgeCoherenceHolder value = new ObjectGenerator<>(AgeCoherenceHolder.class, config).generate();

        assertEquals(42, value.ageYears);
        assertEquals(42, (int) ChronoUnit.YEARS.between(value.dateOfBirth, LocalDate.now()));
    }

    @Test
    @DisplayName("active and string status stay aligned")
    void activeAndStringStatusStayAligned() {
        LifecycleCoherenceHolder value = new ObjectGenerator<>(LifecycleCoherenceHolder.class).generate();

        assertTrue(value.isEnabled
                   ? java.util.Set.of("ACTIVE", "ENABLED").contains(value.status)
                   : java.util.Set.of("INACTIVE", "DISABLED", "SUSPENDED").contains(value.status));
    }

    @Test
    @DisplayName("active and enum status stay aligned")
    void activeAndEnumStatusStayAligned() {
        EnumLifecycleCoherenceHolder value = new ObjectGenerator<>(EnumLifecycleCoherenceHolder.class).generate();

        assertEquals(value.isEnabled, value.status == LifecycleState.ACTIVE || value.status == LifecycleState.ENABLED);
    }

    @Test
    @DisplayName("locale-backed address fields align to one seeded coherent address cluster")
    void localeBackedAddressFieldsAlignToOneSeededCoherentAddressCluster() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.GERMANY).seed(17L).build();
        long generationSeed = new java.util.Random(17L).nextLong();
        AddressInfo expected = new AddressInfoGenerator(
            GeneratorConfig.builder().locale(Locale.GERMANY).seed(generationSeed).build()).generate();

        AddressCoherenceHolder value = new ObjectGenerator<>(AddressCoherenceHolder.class, config).generate();

        assertEquals(expected.city(), value.city);
        assertEquals(expected.state(), value.state);
        assertEquals(expected.zip(), value.postalCode);
        assertEquals(expected.country(), value.country);
    }

    @Test
    @DisplayName("explicit country overrides still win over address coherence")
    void explicitCountryOverridesStillWinOverAddressCoherence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(AddressCoherenceHolder.class, "country", () -> "Canada")
                                                            .build();

        AddressCoherenceHolder value = new ObjectGenerator<>(AddressCoherenceHolder.class, config).generate();

        assertEquals("Canada", value.country);
    }

    @Test
    @DisplayName("money fields stay ordered and string money fields derive formatted values")
    void moneyFieldsStayOrderedAndStringMoneyFieldsDeriveFormattedValues() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(StringMoneyHolder.class, "price", () -> "7.50")
                                                            .override(StringMoneyHolder.class, "currencyCode", () -> "USD")
                                                            .build();

        StringMoneyHolder stringMoney = new ObjectGenerator<>(StringMoneyHolder.class, config).generate();
        OrderedMoneyHolder orderedMoney = new ObjectGenerator<>(OrderedMoneyHolder.class).generate();

        assertEquals("7.50", stringMoney.price);
        assertEquals("USD 7.50", stringMoney.amount);
        assertEquals("USD 7.50", stringMoney.balance);

        assertNotNull(orderedMoney.price);
        assertNotNull(orderedMoney.amount);
        assertNotNull(orderedMoney.balance);
        assertTrue(orderedMoney.amount.compareTo(orderedMoney.price) >= 0);
        assertTrue(orderedMoney.balance.compareTo(orderedMoney.amount) >= 0);
    }

    private static String slug(String value) {
        StringBuilder normalized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch <= 127 && Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    private static String emailDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }

    static class CoherentPerson {

        String        firstName;
        String        lastName;
        String        fullName;
        String        email;
        String        domain;
        String        url;
        LocalDateTime createdAt;
        Instant       updatedAt;
    }

    record CoherentPersonRecord(String firstName,
                                String lastName,
                                String fullName,
                                String email,
                                String domain,
                                String url,
                                LocalDateTime createdAt,
                                Instant updatedAt) {
    }

    static class FixedNamePerson {

        String firstName;
        String lastName;
        String domain;
        String email;
    }

    static class TemporalCoherenceHolder {

        LocalDateTime createdAt;
        Instant       updatedAt;
    }

    static class AgeCoherenceHolder {

        LocalDate dateOfBirth;
        int       ageYears;
    }

    static class LifecycleCoherenceHolder {

        boolean isEnabled;
        String  status;
    }

    static class EnumLifecycleCoherenceHolder {

        boolean        isEnabled;
        LifecycleState status;
    }

    static class AddressCoherenceHolder {

        String city;
        String state;
        String postalCode;
        String country;
    }

    static class StringMoneyHolder {

        String currencyCode;
        String price;
        String amount;
        String balance;
    }

    static class OrderedMoneyHolder {

        BigDecimal price;
        BigDecimal amount;
        BigDecimal balance;
    }

    static class AnnotatedEmailPerson {

        String firstName;
        String lastName;
        String fullName;

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    public static class AnnotatedValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }

    enum LifecycleState {
        ACTIVE,
        ENABLED,
        INACTIVE,
        DISABLED,
        PENDING
    }
}
