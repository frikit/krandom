/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.Size;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.finance.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("ObjectGenerator typed semantics")
class ObjectGeneratorTypedSemanticTest {

    @Test
    @DisplayName("typed business fields use semantic defaults")
    void typedBusinessFieldsUseSemanticDefaults() {
        BusinessSemantics value = new ObjectGenerator<>(BusinessSemantics.class).generate();

        LocalDate today = LocalDate.now();
        assertTrue(!value.dateOfBirth.isBefore(today.minusYears(90)));
        assertTrue(!value.dateOfBirth.isAfter(today.minusYears(18)));
        assertTrue(value.ageYears >= 18 && value.ageYears <= 90);

        assertTrue(!value.created_at.toLocalDate().isBefore(today.minusYears(10)));
        assertTrue(!value.created_at.toLocalDate().isAfter(today));

        Instant latestAllowed = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        assertTrue(!value.updatedTimestamp.isBefore(today.minusYears(10).atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertTrue(value.updatedTimestamp.isBefore(latestAllowed));

        assertNotNull(value.totalAmount);
        assertEquals(2, value.totalAmount.scale());
        assertTrue(value.totalAmount.compareTo(BigDecimal.ZERO) >= 0);

        assertNotNull(value.accountBalance);
        assertEquals(2, value.accountBalance.scale());
        assertTrue(value.accountBalance.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(value.accountBalance.compareTo(value.totalAmount) >= 0);

        assertTrue(value.unitPrice >= 0.0);
        assertTrue(value.unitPrice < 10000.0);
        assertTrue(value.totalAmount.doubleValue() + 0.0001d >= value.unitPrice);
        assertEquals(Currency.USD, value.currencyCode);
        assertTrue(value.accountId > 0);
        assertTrue(value.lat >= 24.5 && value.lat <= 49.0);
        assertTrue(value.lon >= -125.0 && value.lon <= -66.0);
        assertTrue(value.isEnabled
                   ? Set.of("ACTIVE", "ENABLED").contains(value.status)
                   : Set.of("INACTIVE", "DISABLED", "SUSPENDED").contains(value.status));
    }

    @Test
    @DisplayName("semantic id aliases stay unique across one generator sequence by default")
    void semanticIdAliasesStayUniqueAcrossOneGeneratorSequence() {
        ObjectGenerator<BusinessSemantics> generator = new ObjectGenerator<>(BusinessSemantics.class);
        Set<Long> accountIds = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            assertTrue(accountIds.add(generator.generate().accountId));
        }
    }

    @Test
    @DisplayName("structural-only mode disables typed semantic defaults")
    void structuralOnlyModeDisablesTypedSemanticDefaults() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(42L)
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .build();

        BusinessSemantics value = new ObjectGenerator<>(BusinessSemantics.class, config).generate();

        assertTrue(value.lat >= 0.0 && value.lat < 1.0);
        assertTrue(value.lon >= 0.0 && value.lon < 1.0);
    }

    @Test
    @DisplayName("configured date ranges override semantic temporal defaults")
    void configuredDateRangesOverrideSemanticTemporalDefaults() {
        LocalDate min = LocalDate.of(2021, 1, 1);
        LocalDate max = LocalDate.of(2021, 12, 31);

        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .dateRange(min, max)
                                                            .build();

        BusinessSemantics value = new ObjectGenerator<>(BusinessSemantics.class, config).generate();

        assertFalse(value.dateOfBirth.isBefore(min));
        assertFalse(value.dateOfBirth.isAfter(max));
        assertFalse(value.created_at.toLocalDate().isBefore(min));
        assertFalse(value.created_at.toLocalDate().isAfter(max));
        assertFalse(value.updatedTimestamp.atOffset(ZoneOffset.UTC).toLocalDate().isBefore(min));
        assertFalse(value.updatedTimestamp.atOffset(ZoneOffset.UTC).toLocalDate().isAfter(max));
    }

    @Test
    @DisplayName("status enums prefer common business lifecycle constants")
    void statusEnumsPreferCommonLifecycleConstants() {
        EnumBackedStatusHolder value = new ObjectGenerator<>(EnumBackedStatusHolder.class).generate();
        assertTrue(Set.of(LifecycleStatus.ACTIVE, LifecycleStatus.PENDING, LifecycleStatus.SUSPENDED).contains(value.status));
    }

    @Test
    @DisplayName("unsupported locale currency semantics fall back to generated codes")
    void unsupportedLocaleCurrencySemanticsFallBack() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.ROOT)
                                                .seed(17L)
                                                .build();

        BusinessSemantics value = new ObjectGenerator<>(BusinessSemantics.class, config).generate();
        assertNotNull(value.currencyCode);
    }

    @Test
    @DisplayName("typed semantic variants cover string ids, java currency, offset timestamps, and coordinate wrappers")
    void typedSemanticVariantsCoverStringIdsJavaCurrencyOffsetTimestampsAndCoordinateWrappers() {
        ExtendedTypedSemantics value = new ObjectGenerator<>(ExtendedTypedSemantics.class).generate();

        assertNotNull(value.accountId);
        assertTrue(value.accountId.matches("\\d+"));
        assertNotNull(value.currencyCode);
        assertEquals("USD", value.currencyCode.getCurrencyCode());
        assertNotNull(value.createdAt);

        LocalDate today = LocalDate.now();
        LocalDate createdDate = value.createdAt.toLocalDate();
        assertFalse(createdDate.isBefore(today.minusYears(10)));
        assertFalse(createdDate.isAfter(today));

        assertNotNull(value.latitude);
        assertTrue(value.latitude >= 24.5f && value.latitude <= 49.0f);
        assertNotNull(value.longitude);
        assertTrue(value.longitude.compareTo(BigDecimal.valueOf(-125.0)) >= 0);
        assertTrue(value.longitude.compareTo(BigDecimal.valueOf(-66.0)) <= 0);
        assertEquals(6, value.longitude.scale());
    }

    @Test
    @DisplayName("typed age variants cover primitive, wrapper, and string registrations")
    void typedAgeVariantsCoverPrimitiveWrapperAndStringRegistrations() {
        PrimitiveLongAgeSemantics primitiveLong = new ObjectGenerator<>(PrimitiveLongAgeSemantics.class).generate();
        WrapperLongAgeSemantics wrapperLong = new ObjectGenerator<>(WrapperLongAgeSemantics.class).generate();
        PrimitiveShortAgeSemantics primitiveShort = new ObjectGenerator<>(PrimitiveShortAgeSemantics.class).generate();
        WrapperShortAgeSemantics wrapperShort = new ObjectGenerator<>(WrapperShortAgeSemantics.class).generate();
        StringAgeSemantics stringAge = new ObjectGenerator<>(StringAgeSemantics.class).generate();

        assertTrue(primitiveLong.age >= 18L && primitiveLong.age <= 90L);
        assertNotNull(wrapperLong.age);
        assertTrue(wrapperLong.age >= 18L && wrapperLong.age <= 90L);
        assertTrue(primitiveShort.yearsOld >= 18 && primitiveShort.yearsOld <= 90);
        assertNotNull(wrapperShort.yearsOld);
        assertTrue(wrapperShort.yearsOld >= 18 && wrapperShort.yearsOld <= 90);
        assertNotNull(stringAge.age);
        assertTrue(Integer.parseInt(stringAge.age) >= 18 && Integer.parseInt(stringAge.age) <= 90);
    }

    @Test
    @DisplayName("business profile string fields use company semantics and stay coherent")
    void businessProfileStringFieldsUseCompanySemanticsAndStayCoherent() {
        BusinessProfileSemantics value = new ObjectGenerator<>(BusinessProfileSemantics.class).generate();

        assertNotNull(value.companyName);
        assertNotNull(value.industry);
        assertTrue(value.companyEmail.contains("@"));
        assertTrue(value.companyUrl.startsWith("https://www."));

        String host = URI.create(value.companyUrl).getHost();
        assertNotNull(host);
        String domain = host.startsWith("www.") ? host.substring(4) : host;
        assertEquals(domain, value.companyEmail.substring(value.companyEmail.indexOf('@') + 1));
    }

    @Test
    @DisplayName("bean validation constraints suppress relaxed semantic string generators")
    void beanValidationConstraintsSuppressRelaxedSemanticStringGenerators() {
        ValidatedSemanticString value = new ObjectGenerator<>(ValidatedSemanticString.class).generate();

        assertNotNull(value.username);
        assertEquals(40, value.username.length());
    }

    @Test
    @DisplayName("status enums without preferred constants fall back to enum generation")
    void statusEnumsWithoutPreferredConstantsFallBackToEnumGeneration() {
        NonSemanticStatusHolder value = new ObjectGenerator<>(NonSemanticStatusHolder.class).generate();
        assertTrue(Set.of(WeirdStatus.DRAFT, WeirdStatus.QUEUED).contains(value.status));
    }

    static class BusinessSemantics {

        LocalDate     dateOfBirth;
        int           ageYears;
        LocalDateTime created_at;
        Instant       updatedTimestamp;
        BigDecimal    totalAmount;
        BigDecimal    accountBalance;
        double        unitPrice;
        Currency      currencyCode;
        long          accountId;
        double        lat;
        double        lon;
        boolean       isEnabled;
        String        status;
    }

    static class EnumBackedStatusHolder {

        LifecycleStatus status;
    }

    static class ExtendedTypedSemantics {

        String             accountId;
        java.util.Currency currencyCode;
        OffsetDateTime     createdAt;
        Float              latitude;
        BigDecimal         longitude;
    }

    static class PrimitiveLongAgeSemantics {

        long age;
    }

    static class WrapperLongAgeSemantics {

        Long age;
    }

    static class PrimitiveShortAgeSemantics {

        short yearsOld;
    }

    static class WrapperShortAgeSemantics {

        Short yearsOld;
    }

    static class StringAgeSemantics {

        String age;
    }

    static class NonSemanticStatusHolder {

        WeirdStatus status;
    }

    static class BusinessProfileSemantics {

        String companyName;
        String industry;
        String companyEmail;
        String companyUrl;
    }

    static class ValidatedSemanticString {

        @Size(min = 40, max = 40)
        String username;
    }

    enum LifecycleStatus {
        ACTIVE,
        PENDING,
        SUSPENDED,
        UNKNOWN
    }

    enum WeirdStatus {
        DRAFT,
        QUEUED
    }
}
