/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationFailureListener;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.locale.LocaleDataBundle;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.object.ObjectGeneratorSemanticCoherenceTest.TemporalPairs;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@DisplayName("SemanticCoherenceAdjuster")
class SemanticCoherenceAdjusterTest {

    private static final LocalDate LEAP_DAY = LocalDate.of(2024, 2, 29);
    private static final ObjectGeneratorConfig LEAP_DAY_RANGE =
            ObjectGeneratorConfig.builder().dateRange(LEAP_DAY, LEAP_DAY).build();

    @Test
    @DisplayName("utility methods normalize strings and URLs")
    void utilityMethodsNormalizeStringsAndUrls() throws Exception {
        assertNull(SemanticCoherenceAdjuster.normalizeDomain(null));
        assertEquals("example.com", SemanticCoherenceAdjuster.normalizeDomain(" WWW.Example.Com "));
        assertNull(SemanticCoherenceAdjuster.normalizeDomain(" www. "));
        assertNull(SemanticCoherenceAdjuster.emailDomain(null));
        assertNull(SemanticCoherenceAdjuster.emailDomain("missing-at"));
        assertNull(SemanticCoherenceAdjuster.emailDomain("user@"));
        assertEquals("example.com", SemanticCoherenceAdjuster.emailDomain("user@example.com"));
        assertNull(SemanticCoherenceAdjuster.urlHost(null));
        assertEquals("www.example.com", SemanticCoherenceAdjuster.urlHost("https://www.example.com/path"));
        assertNull(SemanticCoherenceAdjuster.urlHost("not a uri"));
        assertNull(SemanticCoherenceAdjuster.urlHost("mailto:test@example.com"));
        assertNull(SemanticCoherenceAdjuster.slugFragment(null));
        assertEquals("alicesmith", SemanticCoherenceAdjuster.slugFragment("Alice Smith"));
        assertNull(SemanticCoherenceAdjuster.slugFragment("ÄÖÜ"));
        assertEquals(
                "alice.smith1@example.com", SemanticCoherenceAdjuster.uniquifyString("alice.smith@example.com", 1));
        assertEquals("fullName1", SemanticCoherenceAdjuster.uniquifyString("fullName", 1));
    }

    @Test
    @DisplayName("utility methods derive local parts and convert temporal types")
    void utilityMethodsDeriveLocalPartsAndConvertTemporalTypes() throws Exception {
        ManualNameHolder nameHolder = new ManualNameHolder();
        nameHolder.firstName = "Alice";
        nameHolder.lastName = "Smith";
        assertEquals(
                "alice.smith", SemanticCoherenceAdjuster.emailLocalPart(slotMap(nameHolder, "firstName", "lastName")));

        ManualNameHolder prince = new ManualNameHolder();
        prince.fullName = "Prince";
        assertEquals("prince", SemanticCoherenceAdjuster.emailLocalPart(slotMap(prince, "fullName")));

        ManualNameHolder ada = new ManualNameHolder();
        ada.fullName = "Ada Lovelace";
        assertEquals("ada.lovelace", SemanticCoherenceAdjuster.emailLocalPart(slotMap(ada, "fullName")));

        ManualNameHolder invalidFirst = new ManualNameHolder();
        invalidFirst.firstName = "ÄÖÜ";
        invalidFirst.lastName = "Smith";
        assertNull(SemanticCoherenceAdjuster.emailLocalPart(slotMap(invalidFirst, "firstName", "lastName")));

        ManualNameHolder invalidLast = new ManualNameHolder();
        invalidLast.firstName = "Ada";
        invalidLast.lastName = "ÄÖÜ";
        assertNull(SemanticCoherenceAdjuster.emailLocalPart(slotMap(invalidLast, "firstName", "lastName")));

        ManualNameHolder invalidFull = new ManualNameHolder();
        invalidFull.fullName = "ÄÖÜ Smith";
        assertNull(SemanticCoherenceAdjuster.emailLocalPart(slotMap(invalidFull, "fullName")));

        ManualNameHolder invalidFullLast = new ManualNameHolder();
        invalidFullLast.fullName = "Ada ÄÖÜ";
        assertNull(SemanticCoherenceAdjuster.emailLocalPart(slotMap(invalidFullLast, "fullName")));

        assertNull(SemanticCoherenceAdjuster.emailLocalPart(Map.of()));

        Instant instant = Instant.parse("2026-04-20T12:34:56Z");
        assertSame(instant, SemanticCoherenceAdjuster.fromInstant(instant, Instant.class));
        assertEquals(LocalDate.of(2026, 4, 20), SemanticCoherenceAdjuster.fromInstant(instant, LocalDate.class));
        assertEquals(
                LocalDateTime.of(2026, 4, 20, 12, 34, 56),
                SemanticCoherenceAdjuster.fromInstant(instant, LocalDateTime.class));
        assertEquals(
                OffsetDateTime.ofInstant(instant, ZoneOffset.UTC),
                SemanticCoherenceAdjuster.fromInstant(instant, OffsetDateTime.class));
        assertEquals(
                ZonedDateTime.ofInstant(instant, ZoneOffset.UTC),
                SemanticCoherenceAdjuster.fromInstant(instant, ZonedDateTime.class));
        assertEquals(
                java.util.Date.from(instant), SemanticCoherenceAdjuster.fromInstant(instant, java.util.Date.class));
        assertEquals(
                java.sql.Date.valueOf(LocalDate.of(2026, 4, 20)),
                SemanticCoherenceAdjuster.fromInstant(instant, java.sql.Date.class));
        assertEquals(
                java.sql.Timestamp.from(instant),
                SemanticCoherenceAdjuster.fromInstant(instant, java.sql.Timestamp.class));

        assertEquals(instant, SemanticCoherenceAdjuster.toInstant(instant));
        assertEquals(
                Instant.parse("2026-04-20T00:00:00Z"), SemanticCoherenceAdjuster.toInstant(LocalDate.of(2026, 4, 20)));
        assertEquals(instant, SemanticCoherenceAdjuster.toInstant(LocalDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, SemanticCoherenceAdjuster.toInstant(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, SemanticCoherenceAdjuster.toInstant(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, SemanticCoherenceAdjuster.toInstant(java.util.Date.from(instant)));
        assertNull(SemanticCoherenceAdjuster.toInstant(123L));

        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.of(2026, 1, 2));
        assertEquals(
                LocalDate.of(2026, 1, 2).atStartOfDay().toInstant(ZoneOffset.UTC),
                SemanticCoherenceAdjuster.toInstant(sqlDate));
        assertNull(SemanticCoherenceAdjuster.toInstant(java.sql.Time.valueOf("10:15:30")));

        assertNull(SemanticCoherenceAdjuster.fromInstant(instant, String.class));
    }

    @Test
    @DisplayName("a value with an offset or a zone reads its own local date, and every other type its UTC date")
    void valueWithAnOffsetOrAZoneReadsItsOwnLocalDate() {
        // 2000-06-04T10:30Z and 2000-06-05T10:30Z: each local date is a day from the one UTC reads.
        ZonedDateTime kiritimati = ZonedDateTime.parse("2000-06-05T00:30+14:00[Pacific/Kiritimati]");
        ZonedDateTime pagoPago = ZonedDateTime.parse("2000-06-04T23:30-11:00[Pacific/Pago_Pago]");
        LocalDate fourth = LocalDate.of(2000, 6, 4);
        LocalDate fifth = LocalDate.of(2000, 6, 5);

        assertEquals(fifth, SemanticCoherenceAdjuster.toLocalDate(kiritimati.toOffsetDateTime()));
        assertEquals(fourth, SemanticCoherenceAdjuster.toLocalDate(pagoPago.toOffsetDateTime()));
        assertEquals(fifth, SemanticCoherenceAdjuster.toLocalDate(kiritimati));
        assertEquals(fourth, SemanticCoherenceAdjuster.toLocalDate(pagoPago));
        assertEquals(fifth, SemanticCoherenceAdjuster.toLocalDate(java.util.GregorianCalendar.from(kiritimati)));
        assertEquals(fourth, SemanticCoherenceAdjuster.toLocalDate(java.util.GregorianCalendar.from(pagoPago)));
        // Before 1970 too, where the epoch second is negative.
        assertEquals(
                LocalDate.of(1950, 6, 5),
                SemanticCoherenceAdjuster.toLocalDate(OffsetDateTime.parse("1950-06-05T00:30+14:00")));
        assertEquals(
                LocalDate.of(1950, 6, 4),
                SemanticCoherenceAdjuster.toLocalDate(OffsetDateTime.parse("1950-06-04T23:30-12:00")));

        // An Instant, a LocalDateTime and a legacy date hold no offset, and read their UTC date.
        assertEquals(fourth, SemanticCoherenceAdjuster.toLocalDate(kiritimati.toInstant()));
        assertEquals(fifth, SemanticCoherenceAdjuster.toLocalDate(java.util.Date.from(pagoPago.toInstant())));
        assertEquals(fifth, SemanticCoherenceAdjuster.toLocalDate(java.sql.Timestamp.from(pagoPago.toInstant())));
        assertEquals(fourth, SemanticCoherenceAdjuster.toLocalDate(LocalDateTime.of(2000, 6, 4, 23, 30)));
    }

    @Test
    @DisplayName("age and status utility methods cover conversion and fallback branches")
    void ageAndStatusUtilityMethodsCoverConversionAndFallbackBranches() throws Exception {
        Instant instant = Instant.parse("2026-04-20T12:34:56Z");
        assertEquals(LocalDate.of(2026, 4, 20), SemanticCoherenceAdjuster.toLocalDate(LocalDate.of(2026, 4, 20)));
        assertEquals(LocalDate.of(2026, 4, 20), SemanticCoherenceAdjuster.toLocalDate(java.util.Date.from(instant)));
        assertNull(SemanticCoherenceAdjuster.toLocalDate("bad-date"));

        assertEquals(42, SemanticCoherenceAdjuster.toInteger(42));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger(42L));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger((short) 42));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger(" 42 "));
        assertNull(SemanticCoherenceAdjuster.toInteger(Long.MIN_VALUE));
        assertNull(SemanticCoherenceAdjuster.toInteger(Long.MAX_VALUE));
        assertNull(SemanticCoherenceAdjuster.toInteger("forty-two"));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger((byte) 42));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger(42.9));
        assertEquals(42, SemanticCoherenceAdjuster.toInteger(42.5f));
        assertNull(SemanticCoherenceAdjuster.toInteger(Double.NaN));
        assertNull(SemanticCoherenceAdjuster.toInteger(1e10));
        assertNull(SemanticCoherenceAdjuster.toInteger(new BigDecimal("42")));

        assertEquals(Boolean.TRUE, SemanticCoherenceAdjuster.toBoolean(Boolean.TRUE));
        assertNull(SemanticCoherenceAdjuster.toBoolean("true"));

        assertEquals(42, SemanticCoherenceAdjuster.fromAge(42, int.class));
        assertEquals(42L, SemanticCoherenceAdjuster.fromAge(42, long.class));
        assertEquals(42L, SemanticCoherenceAdjuster.fromAge(42, Long.class));
        assertEquals((short) 42, SemanticCoherenceAdjuster.fromAge(42, short.class));
        assertEquals((short) 42, SemanticCoherenceAdjuster.fromAge(42, Short.class));
        assertEquals("42", SemanticCoherenceAdjuster.fromAge(42, String.class));

        assertEquals(42.0, SemanticCoherenceAdjuster.fromAge(42, Double.class));
        assertEquals(42.0f, SemanticCoherenceAdjuster.fromAge(42, float.class));
        assertEquals((byte) 42, SemanticCoherenceAdjuster.fromAge(42, byte.class));
        assertNull(SemanticCoherenceAdjuster.fromAge(200, Byte.class));
        assertEquals(Byte.MIN_VALUE, SemanticCoherenceAdjuster.fromAge(Byte.MIN_VALUE, byte.class));
        assertNull(SemanticCoherenceAdjuster.fromAge(-129, byte.class));
        assertEquals(Short.MIN_VALUE, SemanticCoherenceAdjuster.fromAge(Short.MIN_VALUE, short.class));
        assertNull(SemanticCoherenceAdjuster.fromAge(Short.MAX_VALUE + 1, short.class));
        assertNull(SemanticCoherenceAdjuster.fromAge(Short.MIN_VALUE - 1, Short.class));
        assertNull(SemanticCoherenceAdjuster.fromAge(42, java.math.BigDecimal.class));

        assertNull(SemanticCoherenceAdjuster.activeFromStatus((Object) null));
        assertEquals(Boolean.TRUE, SemanticCoherenceAdjuster.activeFromStatus("ENABLED"));
        assertEquals(Boolean.FALSE, SemanticCoherenceAdjuster.activeFromStatus("archived"));
        assertNull(SemanticCoherenceAdjuster.activeFromStatus("pending"));

        assertEquals("ACTIVE", SemanticCoherenceAdjuster.statusValueFor(true, String.class));
        assertEquals("INACTIVE", SemanticCoherenceAdjuster.statusValueFor(false, String.class));
        assertEquals(
                ManualLifecycleState.ACTIVE,
                SemanticCoherenceAdjuster.statusValueFor(true, ManualLifecycleState.class));
        assertEquals(
                ManualLifecycleState.DISABLED,
                SemanticCoherenceAdjuster.statusValueFor(false, ManualLifecycleState.class));
        assertNull(SemanticCoherenceAdjuster.statusValueFor(true, Integer.class));
        assertNull(SemanticCoherenceAdjuster.statusValueFor(true, PendingOnlyLifecycleState.class));
    }

    @Test
    @DisplayName("money and currency utility methods cover parsing and formatting branches")
    void moneyAndCurrencyUtilityMethodsCoverParsingAndFormattingBranches() throws Exception {
        assertEquals(new BigDecimal("12.35"), SemanticCoherenceAdjuster.moneyValue("USD 12.345"));
        assertEquals(new BigDecimal("42.00"), SemanticCoherenceAdjuster.moneyValue(42));
        assertEquals(new BigDecimal("42.00"), SemanticCoherenceAdjuster.moneyValue(BigInteger.valueOf(42)));
        assertEquals(new BigDecimal("7.50"), SemanticCoherenceAdjuster.moneyValue(7.5d));
        assertNull(SemanticCoherenceAdjuster.moneyValue("not-money"));
        assertNull(SemanticCoherenceAdjuster.moneyValue(LocalDate.now()));

        assertEquals(
                "USD", SemanticCoherenceAdjuster.currencyCode(io.github.frikit.krandom.generator.finance.Currency.USD));
        assertEquals("EUR", SemanticCoherenceAdjuster.currencyCode(java.util.Currency.getInstance("EUR")));
        assertEquals("SAR", SemanticCoherenceAdjuster.currencyCode("sar"));
        assertEquals("BHD", SemanticCoherenceAdjuster.currencyCode("bhd"));
        assertNull(SemanticCoherenceAdjuster.currencyCode(123));
        assertNull(SemanticCoherenceAdjuster.currencyCode("   "));
        assertNull(SemanticCoherenceAdjuster.currencyCode("not-a-currency"));

        assertEquals(
                "USD 12.35", SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), String.class, "USD"));
        assertEquals("12.35", SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), String.class, null));
        assertEquals(
                new BigDecimal("12.35"),
                SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), BigDecimal.class, "USD"));
        assertEquals(
                BigInteger.valueOf(12),
                SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), BigInteger.class, "USD"));
        assertEquals((byte) 12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), byte.class, "USD"));
        assertEquals((byte) 12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Byte.class, "USD"));
        assertEquals((short) 12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Short.class, "USD"));
        assertEquals((short) 12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), short.class, "USD"));
        assertEquals(12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Integer.class, "USD"));
        assertEquals(12, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), int.class, "USD"));
        assertEquals(12L, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Long.class, "USD"));
        assertEquals(12L, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), long.class, "USD"));
        assertEquals(12.35f, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Float.class, "USD"));
        assertEquals(12.35f, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), float.class, "USD"));
        assertEquals(12.35d, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), Double.class, "USD"));
        assertEquals(12.35d, SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), double.class, "USD"));
        assertNull(SemanticCoherenceAdjuster.moneyValueFor(new BigDecimal("12.345"), LocalDate.class, "USD"));
        assertEquals(
                new BigDecimal("1.00"), SemanticCoherenceAdjuster.assignMoney(null, new BigDecimal("1.00"), "USD"));

        ManualMoneyHolder nullMoneyHolder = new ManualMoneyHolder();
        SemanticCoherenceAdjuster.Slot priceSlot = rawSlot(nullMoneyHolder, "price");
        assertNull(SemanticCoherenceAdjuster.assignMoney(priceSlot, null, "USD"));

        UnsupportedMoneyTarget unsupportedMoneyTarget = new UnsupportedMoneyTarget();
        SemanticCoherenceAdjuster.Slot unsupportedSlot = rawSlot(unsupportedMoneyTarget, "value");
        assertEquals(
                new BigDecimal("2.00"),
                SemanticCoherenceAdjuster.assignMoney(unsupportedSlot, new BigDecimal("2.00"), "USD"));
        assertNull(unsupportedMoneyTarget.value);

        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualStringMoneyHolder stringHolder = new ManualStringMoneyHolder();
        SemanticCoherenceAdjuster.Slot stringPriceSlot = rawSlot(stringHolder, "price");
        assertFalse((Boolean) adjuster.shouldFormatMoneyString(new BigDecimal("1.00"), null, true, "USD"));
        assertFalse((Boolean) adjuster.shouldFormatMoneyString(null, stringPriceSlot, true, "USD"));
        assertFalse((Boolean) adjuster.shouldFormatMoneyString(
                new BigDecimal("1.00"), rawSlot(new ManualMoneyHolder(), "price"), true, "USD"));

        assertFalse((Boolean) SemanticCoherenceAdjuster.isLessThan(new BigDecimal("1.00"), null));
    }

    @Test
    @DisplayName("structural-only mode leaves class instances and record args untouched")
    void structuralOnlyModeLeavesClassInstancesAndRecordArgsUntouched() throws Exception {
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .semanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                        .build(),
                new UniqueFieldTracker());

        ManualNameHolder instance = new ManualNameHolder();
        instance.firstName = "Ada";
        instance.lastName = "Lovelace";
        instance.fullName = "unchanged";
        adjuster.adjustInstance(ManualNameHolder.class, instance, declaredFields(ManualNameHolder.class), true);
        assertEquals("unchanged", instance.fullName);

        ManualRecord record = new ManualRecord("Ada", "Lovelace", "unchanged");
        Object[] args = {record.firstName(), record.lastName(), record.fullName()};
        Field[] fields = {
            ManualRecord.class.getDeclaredField("firstName"),
            ManualRecord.class.getDeclaredField("lastName"),
            ManualRecord.class.getDeclaredField("fullName")
        };
        adjuster.adjustRecordArguments(ManualRecord.class, ManualRecord.class.getRecordComponents(), fields, args);
        assertEquals("unchanged", args[2]);
    }

    @Test
    @DisplayName("manual instances derive domain, url, email, and full name")
    void manualInstancesDeriveDomainUrlEmailAndFullName() throws Exception {
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualCoherenceHolder company = new ManualCoherenceHolder();
        company.companyName = "Acme Labs";
        adjuster.adjustInstance(
                ManualCoherenceHolder.class, company, declaredFields(ManualCoherenceHolder.class), true);
        assertEquals("acmelabs.test", company.domain);
        assertEquals("https://www.acmelabs.test", company.url);
        assertEquals("hello@acmelabs.test", company.companyEmail);
        assertEquals("https://www.acmelabs.test", company.companyUrl);

        SemanticCoherenceAdjuster emailAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualEmailHolder fromEmail = new ManualEmailHolder();
        fromEmail.firstName = "Ada";
        fromEmail.lastName = "Lovelace";
        fromEmail.email = "ignored@widgets.test";
        emailAdjuster.adjustInstance(ManualEmailHolder.class, fromEmail, declaredFields(ManualEmailHolder.class), true);
        assertEquals("ada.lovelace@widgets.test", fromEmail.email);

        SemanticCoherenceAdjuster urlAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualEmailHolder fromUrl = new ManualEmailHolder();
        fromUrl.firstName = "Ada";
        fromUrl.lastName = "Lovelace";
        fromUrl.url = "https://www.widgets.test/products";
        urlAdjuster.adjustInstance(ManualEmailHolder.class, fromUrl, declaredFields(ManualEmailHolder.class), true);
        assertEquals("ada.lovelace@widgets.test", fromUrl.email);

        SemanticCoherenceAdjuster fallbackAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualEmailHolder fallback = new ManualEmailHolder();
        fallback.firstName = "Ada";
        fallback.lastName = "Lovelace";
        fallbackAdjuster.adjustInstance(
                ManualEmailHolder.class, fallback, declaredFields(ManualEmailHolder.class), true);
        assertEquals("ada.lovelace@example.com", fallback.email);

        SemanticCoherenceAdjuster nameAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualNameHolder fromNames = new ManualNameHolder();
        fromNames.firstName = "Ada";
        fromNames.lastName = "Lovelace";
        nameAdjuster.adjustInstance(ManualNameHolder.class, fromNames, declaredFields(ManualNameHolder.class), true);
        assertEquals("Ada Lovelace", fromNames.fullName);
    }

    @Test
    @DisplayName("manual instances align locale country and monetary fields")
    void manualInstancesAlignLocaleCountryAndMonetaryFields() throws Exception {
        long generationSeed = 41L;
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker(), generationSeed);
        AddressInfo expectedAddress = new AddressInfoGenerator(
                        GeneratorConfig.builder().seed(generationSeed).build())
                .generate();

        ManualAddressHolder address = new ManualAddressHolder();
        address.city = "Dallas";
        address.state = "Texas";
        address.postalCode = "75201";
        address.country = "France";
        adjuster.adjustInstance(ManualAddressHolder.class, address, declaredFields(ManualAddressHolder.class), true);
        assertEquals(expectedAddress.city(), address.city);
        assertEquals(expectedAddress.state(), address.state);
        assertEquals(expectedAddress.zip(), address.postalCode);
        assertEquals(expectedAddress.country(), address.country);

        ManualMoneyHolder money = new ManualMoneyHolder();
        money.currencyCode = "usd";
        money.price = new BigDecimal("80.00");
        money.amount = new BigDecimal("50.00");
        money.balance = new BigDecimal("40.00");
        adjuster.adjustInstance(ManualMoneyHolder.class, money, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("80.00"), money.price);
        assertEquals(new BigDecimal("80.00"), money.amount);
        assertEquals(new BigDecimal("80.00"), money.balance);

        ManualStringMoneyHolder stringMoney = new ManualStringMoneyHolder();
        stringMoney.currencyCode = "usd";
        stringMoney.price = "7.5";
        adjuster.adjustInstance(
                ManualStringMoneyHolder.class, stringMoney, declaredFields(ManualStringMoneyHolder.class), true);
        assertEquals("USD 7.50", stringMoney.price);
        assertEquals("USD 7.50", stringMoney.amount);
        assertEquals("USD 7.50", stringMoney.balance);
    }

    @Test
    @DisplayName("company contact fallback paths derive from self, companion fields, and company fallback inputs")
    void companyContactFallbackPathsDeriveFromSelfCompanionFieldsAndCompanyFallbackInputs() throws Exception {
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualCompanyContactHolder fromOwnEmail = new ManualCompanyContactHolder();
        fromOwnEmail.companyEmail = "legacy@widgets.test";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class, fromOwnEmail, declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("hello@widgets.test", fromOwnEmail.companyEmail);

        ManualCompanyContactHolder fromCompanyUrl = new ManualCompanyContactHolder();
        fromCompanyUrl.companyUrl = "https://www.widgets.test/path";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class,
                fromCompanyUrl,
                declaredFields(ManualCompanyContactHolder.class),
                true);
        assertEquals("hello@widgets.test", fromCompanyUrl.companyEmail);
        assertEquals("https://www.widgets.test", fromCompanyUrl.companyUrl);

        ManualCompanyContactHolder fromOwnUrl = new ManualCompanyContactHolder();
        fromOwnUrl.companyUrl = "https://www.widgets.test/old";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class, fromOwnUrl, declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("https://www.widgets.test", fromOwnUrl.companyUrl);

        ManualCompanyContactHolder fromCompanyEmail = new ManualCompanyContactHolder();
        fromCompanyEmail.companyEmail = "hello@widgets.test";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class,
                fromCompanyEmail,
                declaredFields(ManualCompanyContactHolder.class),
                true);
        assertEquals("https://www.widgets.test", fromCompanyEmail.companyUrl);

        ManualCompanyContactHolder unresolved = new ManualCompanyContactHolder();
        unresolved.companyName = "株式会社";
        unresolved.companyEmail = "";
        unresolved.companyUrl = "";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class, unresolved, declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("", unresolved.companyEmail);
        assertEquals("", unresolved.companyUrl);
    }

    @Test
    @DisplayName("generic email falls back to company name when full-name slugs are unavailable")
    void genericEmailFallsBackToCompanyNameWhenFullNameSlugsAreUnavailable() throws Exception {
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualCompanyFallbackEmailHolder singleWord = new ManualCompanyFallbackEmailHolder();
        singleWord.fullName = "株式会社";
        singleWord.companyName = "Acme Labs";
        adjuster.adjustInstance(
                ManualCompanyFallbackEmailHolder.class,
                singleWord,
                declaredFields(ManualCompanyFallbackEmailHolder.class),
                true);
        assertEquals("hello@acmelabs.test", singleWord.email);

        ManualCompanyFallbackEmailHolder multiWord = new ManualCompanyFallbackEmailHolder();
        multiWord.fullName = "株式会社 株式会社";
        multiWord.companyName = "Widgets Co";
        adjuster.adjustInstance(
                ManualCompanyFallbackEmailHolder.class,
                multiWord,
                declaredFields(ManualCompanyFallbackEmailHolder.class),
                true);
        assertEquals("hello@widgetsco.test", multiWord.email);
    }

    @Test
    @DisplayName("generated emails take a real sibling domain only when an override supplied it")
    void generatedEmailsTakeARealSiblingDomainOnlyWhenAnOverrideSuppliedIt() throws Exception {
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualCoherenceHolder generatedUrl = new ManualCoherenceHolder();
        generatedUrl.url = "https://www.widgets.com/path";
        adjuster.adjustInstance(
                ManualCoherenceHolder.class, generatedUrl, declaredFields(ManualCoherenceHolder.class), true);
        assertNull(generatedUrl.companyEmail);

        ManualCompanyContactHolder generatedCompanyUrl = new ManualCompanyContactHolder();
        generatedCompanyUrl.companyUrl = "https://www.widgets.com/path";
        adjuster.adjustInstance(
                ManualCompanyContactHolder.class,
                generatedCompanyUrl,
                declaredFields(ManualCompanyContactHolder.class),
                true);
        assertNull(generatedCompanyUrl.companyEmail);

        ManualCoherenceHolder suppliedUrl = new ManualCoherenceHolder();
        suppliedUrl.url = "https://www.widgets.com/path";
        new SemanticCoherenceAdjuster(
                        ObjectGeneratorConfig.builder()
                                .override(ManualCoherenceHolder.class, "url", () -> "unused")
                                .build(),
                        new UniqueFieldTracker())
                .adjustInstance(
                        ManualCoherenceHolder.class, suppliedUrl, declaredFields(ManualCoherenceHolder.class), true);
        assertEquals("hello@widgets.com", suppliedUrl.companyEmail);

        ManualCompanyContactHolder suppliedCompanyUrl = new ManualCompanyContactHolder();
        suppliedCompanyUrl.companyUrl = "https://www.widgets.com/path";
        new SemanticCoherenceAdjuster(
                        ObjectGeneratorConfig.builder()
                                .override(ManualCompanyContactHolder.class, "companyUrl", () -> "unused")
                                .build(),
                        new UniqueFieldTracker())
                .adjustInstance(
                        ManualCompanyContactHolder.class,
                        suppliedCompanyUrl,
                        declaredFields(ManualCompanyContactHolder.class),
                        true);
        assertEquals("hello@widgets.com", suppliedCompanyUrl.companyEmail);
    }

    @Test
    @DisplayName("manual money holders backfill missing amount from price")
    void manualMoneyHoldersBackfillMissingAmountFromPrice() throws Exception {
        SemanticCoherenceAdjuster adjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualMoneyHolder money = new ManualMoneyHolder();
        money.currencyCode = "usd";
        money.price = new BigDecimal("19.99");
        adjuster.adjustInstance(ManualMoneyHolder.class, money, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("19.99"), money.amount);
    }

    @Test
    @DisplayName("protected company contact fields are left untouched")
    void protectedCompanyContactFieldsAreLeftUntouched() throws Exception {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(ManualCompanyContactHolder.class, "companyEmail", () -> "ignored@example.test")
                .override(ManualCompanyContactHolder.class, "companyUrl", () -> "https://ignored.example.test")
                .build();
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(config, new UniqueFieldTracker());

        ManualCompanyContactHolder holder = new ManualCompanyContactHolder();
        holder.companyName = "Acme Labs";
        holder.companyEmail = "legacy@widgets.test";
        holder.companyUrl = "https://portal.widgets.test";

        adjuster.adjustInstance(
                ManualCompanyContactHolder.class, holder, declaredFields(ManualCompanyContactHolder.class), true);

        assertEquals("legacy@widgets.test", holder.companyEmail);
        assertEquals("https://portal.widgets.test", holder.companyUrl);
    }

    @Test
    @DisplayName("protected amount slots skip price backfill")
    void protectedAmountSlotsSkipPriceBackfill() throws Exception {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(ManualMoneyHolder.class, "amount", () -> new BigDecimal("1.00"))
                .build();
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(config, new UniqueFieldTracker());

        ManualMoneyHolder money = new ManualMoneyHolder();
        money.currencyCode = "usd";
        money.price = new BigDecimal("19.99");

        adjuster.adjustInstance(ManualMoneyHolder.class, money, declaredFields(ManualMoneyHolder.class), true);

        assertNull(money.amount);
        assertEquals(new BigDecimal("19.99"), money.price);
    }

    @Test
    @DisplayName("address and money coherence cover null-locale and protected fallback branches")
    void addressAndMoneyCoherenceCoverNullLocaleAndProtectedFallbackBranches() throws Exception {
        SemanticCoherenceAdjuster defaultAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualAddressHolder noAddressSignals = new ManualAddressHolder();
        noAddressSignals.country = "France";
        defaultAdjuster.adjustInstance(
                ManualAddressHolder.class, noAddressSignals, declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", noAddressSignals.country);

        StreetOnlyAddressHolder streetOnly = new StreetOnlyAddressHolder();
        streetOnly.streetAddress = "1 Main Street";
        streetOnly.country = "France";
        defaultAdjuster.adjustInstance(
                StreetOnlyAddressHolder.class, streetOnly, declaredFields(StreetOnlyAddressHolder.class), true);
        assertEquals("United States", streetOnly.country);

        SemanticCoherenceAdjuster languageOnlyAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .generatorConfig(
                                GeneratorConfig.builder().locale(Locale.ENGLISH).build())
                        .build(),
                new UniqueFieldTracker());
        ManualAddressHolder languageOnly = new ManualAddressHolder();
        languageOnly.city = "London";
        languageOnly.country = "France";
        languageOnlyAdjuster.adjustInstance(
                ManualAddressHolder.class, languageOnly, declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", languageOnly.country);

        SemanticCoherenceAdjuster unregisteredLocaleAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .generatorConfig(GeneratorConfig.builder()
                                .locale(Locale.of("zz", "US"))
                                .build())
                        .build(),
                new UniqueFieldTracker());
        ManualAddressHolder unregisteredLocale = new ManualAddressHolder();
        unregisteredLocale.city = "Nowhere";
        unregisteredLocale.country = "France";
        unregisteredLocaleAdjuster.adjustInstance(
                ManualAddressHolder.class, unregisteredLocale, declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", unregisteredLocale.country);

        ManualMoneyHolder derivedPrice = new ManualMoneyHolder();
        derivedPrice.amount = new BigDecimal("12.50");
        defaultAdjuster.adjustInstance(
                ManualMoneyHolder.class, derivedPrice, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("12.50"), derivedPrice.price);
        assertEquals(new BigDecimal("12.50"), derivedPrice.amount);
        assertEquals(new BigDecimal("12.50"), derivedPrice.balance);

        SemanticCoherenceAdjuster protectedAmountAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualMoneyHolder.class, "amount", () -> new BigDecimal("50.00"))
                        .build(),
                new UniqueFieldTracker());
        ManualMoneyHolder protectedAmount = new ManualMoneyHolder();
        protectedAmount.price = new BigDecimal("80.00");
        protectedAmount.amount = new BigDecimal("50.00");
        protectedAmountAdjuster.adjustInstance(
                ManualMoneyHolder.class, protectedAmount, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("50.00"), protectedAmount.price);
        assertEquals(new BigDecimal("50.00"), protectedAmount.amount);

        SemanticCoherenceAdjuster protectedBalanceAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualMoneyHolder.class, "balance", () -> new BigDecimal("40.00"))
                        .build(),
                new UniqueFieldTracker());
        ManualMoneyHolder protectedBalance = new ManualMoneyHolder();
        protectedBalance.amount = new BigDecimal("80.00");
        protectedBalance.balance = new BigDecimal("40.00");
        protectedBalanceAdjuster.adjustInstance(
                ManualMoneyHolder.class, protectedBalance, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("40.00"), protectedBalance.amount);
        assertEquals(new BigDecimal("40.00"), protectedBalance.balance);

        SemanticCoherenceAdjuster protectedMissingAmountAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualMoneyHolder.class, "amount", () -> new BigDecimal("25.00"))
                        .build(),
                new UniqueFieldTracker());
        ManualMoneyHolder protectedMissingAmount = new ManualMoneyHolder();
        protectedMissingAmount.price = new BigDecimal("25.00");
        protectedMissingAmountAdjuster.adjustInstance(
                ManualMoneyHolder.class, protectedMissingAmount, declaredFields(ManualMoneyHolder.class), true);
        assertNull(protectedMissingAmount.amount);

        SemanticCoherenceAdjuster fullyProtectedPriceAmountAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualMoneyHolder.class, "price", () -> new BigDecimal("80.00"))
                        .override(ManualMoneyHolder.class, "amount", () -> new BigDecimal("50.00"))
                        .build(),
                new UniqueFieldTracker());
        ManualMoneyHolder fullyProtectedPriceAmount = new ManualMoneyHolder();
        fullyProtectedPriceAmount.price = new BigDecimal("80.00");
        fullyProtectedPriceAmount.amount = new BigDecimal("50.00");
        fullyProtectedPriceAmountAdjuster.adjustInstance(
                ManualMoneyHolder.class, fullyProtectedPriceAmount, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("80.00"), fullyProtectedPriceAmount.price);
        assertEquals(new BigDecimal("50.00"), fullyProtectedPriceAmount.amount);

        SemanticCoherenceAdjuster fullyProtectedAmountBalanceAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualMoneyHolder.class, "amount", () -> new BigDecimal("80.00"))
                        .override(ManualMoneyHolder.class, "balance", () -> new BigDecimal("40.00"))
                        .build(),
                new UniqueFieldTracker());
        ManualMoneyHolder fullyProtectedAmountBalance = new ManualMoneyHolder();
        fullyProtectedAmountBalance.amount = new BigDecimal("80.00");
        fullyProtectedAmountBalance.balance = new BigDecimal("40.00");
        fullyProtectedAmountBalanceAdjuster.adjustInstance(
                ManualMoneyHolder.class, fullyProtectedAmountBalance, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("80.00"), fullyProtectedAmountBalance.amount);
        assertEquals(new BigDecimal("40.00"), fullyProtectedAmountBalance.balance);
    }

    @Test
    @DisplayName("address coherence caches one snapshot and skips blank generated components")
    void addressCoherenceCachesOneSnapshotAndSkipsBlankGeneratedComponents() throws Exception {
        Locale locale = Locale.of("qa", "US");
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .registerLocaleData(LocaleDataBundle.builder(locale)
                        .countries("United States")
                        .streetAddress(new String[] {"Elm"}, new String[] {"St"}, new String[] {"Street"})
                        .build())
                .build();
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .locale(locale)
                        .registryContext(context)
                        .build())
                .build();
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(config, new UniqueFieldTracker(), 77L);

        ManualAddressHolder first = new ManualAddressHolder();
        first.city = "Keep City";
        first.state = "Keep State";
        first.postalCode = "11111";
        first.country = "France";
        adjuster.adjustInstance(ManualAddressHolder.class, first, declaredFields(ManualAddressHolder.class), true);

        ManualAddressHolder second = new ManualAddressHolder();
        second.city = "Keep City Two";
        second.state = "Keep State Two";
        second.postalCode = "22222";
        second.country = "Canada";
        adjuster.adjustInstance(ManualAddressHolder.class, second, declaredFields(ManualAddressHolder.class), true);

        assertEquals("Keep City", first.city);
        assertEquals("Keep State", first.state);
        assertEquals("Keep City Two", second.city);
        assertEquals("Keep State Two", second.state);
        assertEquals("United States", first.country);
        assertEquals(first.country, second.country);
        assertEquals(first.postalCode, second.postalCode);
    }

    @Test
    @DisplayName("overwrite rules and protections are respected")
    void overwriteRulesAndProtectionsAreRespected() throws Exception {
        SemanticCoherenceAdjuster defaultAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualNameHolder blankOnly = new ManualNameHolder();
        blankOnly.firstName = "Ada";
        blankOnly.lastName = "Lovelace";
        blankOnly.fullName = "   ";
        defaultAdjuster.adjustInstance(
                ManualNameHolder.class, blankOnly, declaredFields(ManualNameHolder.class), false);
        assertEquals("Ada Lovelace", blankOnly.fullName);

        ManualNameHolder nullOnly = new ManualNameHolder();
        nullOnly.firstName = "Ada";
        nullOnly.lastName = "Lovelace";
        defaultAdjuster.adjustInstance(ManualNameHolder.class, nullOnly, declaredFields(ManualNameHolder.class), false);
        assertEquals("Ada Lovelace", nullOnly.fullName);

        ManualNameHolder protectedExisting = new ManualNameHolder();
        protectedExisting.firstName = "Ada";
        protectedExisting.lastName = "Lovelace";
        protectedExisting.fullName = "Custom";
        defaultAdjuster.adjustInstance(
                ManualNameHolder.class, protectedExisting, declaredFields(ManualNameHolder.class), false);
        assertEquals("Custom", protectedExisting.fullName);

        ManualNameHolder missingFirst = new ManualNameHolder();
        missingFirst.lastName = "Lovelace";
        defaultAdjuster.adjustInstance(
                ManualNameHolder.class, missingFirst, declaredFields(ManualNameHolder.class), true);
        assertNull(missingFirst.fullName);

        ManualNameHolder missingLast = new ManualNameHolder();
        missingLast.firstName = "Ada";
        defaultAdjuster.adjustInstance(
                ManualNameHolder.class, missingLast, declaredFields(ManualNameHolder.class), true);
        assertNull(missingLast.fullName);

        ManualTemporalHolder temporal = new ManualTemporalHolder();
        temporal.createdAt = LocalDateTime.of(2026, 4, 20, 12, 0);
        temporal.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(
                ManualTemporalHolder.class, temporal, declaredFields(ManualTemporalHolder.class), false);
        assertEquals(LocalDateTime.of(2026, 4, 20, 12, 0), temporal.createdAt);
        assertEquals(Instant.parse("2026-04-20T10:00:00Z"), temporal.updatedAt);

        ManualTemporalHolder missingCreated = new ManualTemporalHolder();
        missingCreated.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(
                ManualTemporalHolder.class, missingCreated, declaredFields(ManualTemporalHolder.class), true);
        assertNull(missingCreated.createdAt);

        ManualTemporalHolder missingUpdated = new ManualTemporalHolder();
        missingUpdated.createdAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        defaultAdjuster.adjustInstance(
                ManualTemporalHolder.class, missingUpdated, declaredFields(ManualTemporalHolder.class), true);
        assertNull(missingUpdated.updatedAt);

        ManualTemporalHolder alreadyOrdered = new ManualTemporalHolder();
        alreadyOrdered.createdAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        alreadyOrdered.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(
                ManualTemporalHolder.class, alreadyOrdered, declaredFields(ManualTemporalHolder.class), true);
        assertEquals(LocalDateTime.of(2026, 4, 20, 9, 0), alreadyOrdered.createdAt);

        SemanticCoherenceAdjuster fieldOverrideAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualNameHolder.class, "fullName", () -> "fixed")
                        .build(),
                new UniqueFieldTracker());
        ManualNameHolder fieldOverride = new ManualNameHolder();
        fieldOverride.firstName = "Ada";
        fieldOverride.lastName = "Lovelace";
        fieldOverrideAdjuster.adjustInstance(
                ManualNameHolder.class, fieldOverride, declaredFields(ManualNameHolder.class), true);
        assertNull(fieldOverride.fullName);

        SemanticCoherenceAdjuster typeOverrideAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(String.class, () -> "fixed")
                        .build(),
                new UniqueFieldTracker());
        ManualEmailHolder typeOverride = new ManualEmailHolder();
        typeOverride.firstName = "Ada";
        typeOverride.lastName = "Lovelace";
        typeOverrideAdjuster.adjustInstance(
                ManualEmailHolder.class, typeOverride, declaredFields(ManualEmailHolder.class), true);
        assertNull(typeOverride.email);

        SemanticCoherenceAdjuster contextualTypeOverrideAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(String.class, ctx -> "fixed")
                        .build(),
                new UniqueFieldTracker());
        ManualEmailHolder contextualTypeOverride = new ManualEmailHolder();
        contextualTypeOverride.firstName = "Ada";
        contextualTypeOverride.lastName = "Lovelace";
        contextualTypeOverrideAdjuster.adjustInstance(
                ManualEmailHolder.class, contextualTypeOverride, declaredFields(ManualEmailHolder.class), true);
        assertNull(contextualTypeOverride.email);

        SemanticCoherenceAdjuster predicateOverrideAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(FieldPredicates.named("fullName"), () -> "fixed")
                        .build(),
                new UniqueFieldTracker());
        ManualNameHolder predicateOverride = new ManualNameHolder();
        predicateOverride.firstName = "Ada";
        predicateOverride.lastName = "Lovelace";
        predicateOverride.fullName = "RAW";
        predicateOverrideAdjuster.adjustInstance(
                ManualNameHolder.class, predicateOverride, declaredFields(ManualNameHolder.class), true);
        assertEquals("RAW", predicateOverride.fullName);

        UrlOnlyHolder urlOnly = new UrlOnlyHolder();
        defaultAdjuster.adjustInstance(UrlOnlyHolder.class, urlOnly, declaredFields(UrlOnlyHolder.class), true);
        assertNull(urlOnly.url);
    }

    @Test
    @DisplayName("age and status coherence cover protected, missing, and unsupported fallback paths")
    void ageAndStatusCoherenceCoverProtectedMissingAndUnsupportedFallbackPaths() throws Exception {
        SemanticCoherenceAdjuster defaultAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualAgeHolder derivedAge = new ManualAgeHolder();
        derivedAge.birthDate = LocalDate.now().minusYears(33);
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, derivedAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(33, derivedAge.age);

        ManualAgeHolder noOverwriteConflict = new ManualAgeHolder();
        noOverwriteConflict.birthDate = LocalDate.now().minusYears(29);
        noOverwriteConflict.age = 17;
        defaultAdjuster.adjustInstance(
                ManualAgeHolder.class, noOverwriteConflict, declaredFields(ManualAgeHolder.class), false);
        assertEquals(17, noOverwriteConflict.age);
        assertEquals(LocalDate.now().minusYears(29), noOverwriteConflict.birthDate);

        SemanticCoherenceAdjuster protectedNullAgeAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualAgeHolder.class, "age", () -> 41)
                        .build(),
                new UniqueFieldTracker());
        ManualAgeHolder protectedNullAge = new ManualAgeHolder();
        protectedNullAge.birthDate = LocalDate.now().minusYears(31);
        protectedNullAgeAdjuster.adjustInstance(
                ManualAgeHolder.class, protectedNullAge, declaredFields(ManualAgeHolder.class), true);
        assertNull(protectedNullAge.age);
        assertEquals(LocalDate.now().minusYears(31), protectedNullAge.birthDate);

        ManualAgeHolder matchingAge = new ManualAgeHolder();
        matchingAge.birthDate = LocalDate.now().minusYears(29);
        matchingAge.age = 29;
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, matchingAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(29, matchingAge.age);

        SemanticCoherenceAdjuster protectedAgeAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualAgeHolder.class, "age", () -> 42)
                        .build(),
                new UniqueFieldTracker());
        ManualAgeHolder protectedAge = new ManualAgeHolder();
        protectedAge.birthDate = LocalDate.now().minusYears(20);
        protectedAge.age = 42;
        protectedAgeAdjuster.adjustInstance(
                ManualAgeHolder.class, protectedAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(42, protectedAge.age);
        assertEquals(LocalDate.now().minusYears(42), protectedAge.birthDate);

        ManualAgeHolder missingBirthDate = new ManualAgeHolder();
        missingBirthDate.age = 27;
        defaultAdjuster.adjustInstance(
                ManualAgeHolder.class, missingBirthDate, declaredFields(ManualAgeHolder.class), true);
        assertEquals(LocalDate.now().minusYears(27), missingBirthDate.birthDate);

        SemanticCoherenceAdjuster protectedBirthDateAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualAgeHolder.class, "birthDate", () -> LocalDate.EPOCH)
                        .build(),
                new UniqueFieldTracker());
        ManualAgeHolder protectedBirthDate = new ManualAgeHolder();
        protectedBirthDate.age = 36;
        protectedBirthDateAdjuster.adjustInstance(
                ManualAgeHolder.class, protectedBirthDate, declaredFields(ManualAgeHolder.class), true);
        assertNull(protectedBirthDate.birthDate);
        assertEquals(36, protectedBirthDate.age);

        ManualAgeHolder missingBoth = new ManualAgeHolder();
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, missingBoth, declaredFields(ManualAgeHolder.class), true);
        assertNull(missingBoth.birthDate);
        assertNull(missingBoth.age);

        ManualStatusHolder alignedStatus = new ManualStatusHolder();
        alignedStatus.active = true;
        alignedStatus.status = "ENABLED";
        defaultAdjuster.adjustInstance(
                ManualStatusHolder.class, alignedStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("ENABLED", alignedStatus.status);

        ManualStatusHolder rewrittenStatus = new ManualStatusHolder();
        rewrittenStatus.active = true;
        rewrittenStatus.status = "SUSPENDED";
        defaultAdjuster.adjustInstance(
                ManualStatusHolder.class, rewrittenStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("ACTIVE", rewrittenStatus.status);

        SemanticCoherenceAdjuster protectedStatusAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualStatusHolder.class, "status", () -> "fixed")
                        .build(),
                new UniqueFieldTracker());
        ManualStatusHolder protectedStatus = new ManualStatusHolder();
        protectedStatus.active = true;
        protectedStatus.status = "SUSPENDED";
        protectedStatusAdjuster.adjustInstance(
                ManualStatusHolder.class, protectedStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("SUSPENDED", protectedStatus.status);
        assertFalse(protectedStatus.active);

        ManualStatusHolder protectedStatusWithoutOverwrite = new ManualStatusHolder();
        protectedStatusWithoutOverwrite.active = true;
        protectedStatusWithoutOverwrite.status = "SUSPENDED";
        protectedStatusAdjuster.adjustInstance(
                ManualStatusHolder.class,
                protectedStatusWithoutOverwrite,
                declaredFields(ManualStatusHolder.class),
                false);
        assertTrue(protectedStatusWithoutOverwrite.active);
        assertEquals("SUSPENDED", protectedStatusWithoutOverwrite.status);

        ManualNullableStatusHolder derivedActive = new ManualNullableStatusHolder();
        derivedActive.status = "ACTIVE";
        defaultAdjuster.adjustInstance(
                ManualNullableStatusHolder.class,
                derivedActive,
                declaredFields(ManualNullableStatusHolder.class),
                true);
        assertEquals(Boolean.TRUE, derivedActive.active);

        ManualNullableStatusHolder unknownStatus = new ManualNullableStatusHolder();
        unknownStatus.status = "PENDING";
        defaultAdjuster.adjustInstance(
                ManualNullableStatusHolder.class,
                unknownStatus,
                declaredFields(ManualNullableStatusHolder.class),
                true);
        assertNull(unknownStatus.active);

        SemanticCoherenceAdjuster protectedActiveAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualNullableStatusHolder.class, "active", () -> Boolean.FALSE)
                        .build(),
                new UniqueFieldTracker());
        ManualNullableStatusHolder protectedActive = new ManualNullableStatusHolder();
        protectedActive.status = "ACTIVE";
        protectedActiveAdjuster.adjustInstance(
                ManualNullableStatusHolder.class,
                protectedActive,
                declaredFields(ManualNullableStatusHolder.class),
                true);
        assertNull(protectedActive.active);

        ManualEnumStatusHolder enumStatus = new ManualEnumStatusHolder();
        enumStatus.active = false;
        defaultAdjuster.adjustInstance(
                ManualEnumStatusHolder.class, enumStatus, declaredFields(ManualEnumStatusHolder.class), true);
        assertEquals(ManualLifecycleState.DISABLED, enumStatus.status);

        ManualOpaqueStatusHolder opaqueStatus = new ManualOpaqueStatusHolder();
        opaqueStatus.active = true;
        opaqueStatus.status = 7;
        defaultAdjuster.adjustInstance(
                ManualOpaqueStatusHolder.class, opaqueStatus, declaredFields(ManualOpaqueStatusHolder.class), true);
        assertEquals(7, opaqueStatus.status);
        assertTrue(opaqueStatus.active);
    }

    @Test
    @DisplayName(
            "relaxed mode protects annotated and validated fields, and strict mode overwrites them within their constraints")
    void relaxedModeProtectsAnnotatedAndValidatedFieldsAndStrictModeOverwritesThemWithinTheirConstraints()
            throws Exception {
        ProtectedSemanticHolder relaxed = new ProtectedSemanticHolder();
        relaxed.firstName = "Ada";
        relaxed.lastName = "Lovelace";
        relaxed.fullName = "RAW";
        relaxed.email = "ANNOTATED";

        SemanticCoherenceAdjuster relaxedAdjuster =
                new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        relaxedAdjuster.adjustInstance(
                ProtectedSemanticHolder.class, relaxed, declaredFields(ProtectedSemanticHolder.class), true);
        assertEquals("RAW", relaxed.fullName);
        assertEquals("ANNOTATED", relaxed.email);

        ProtectedSemanticHolder strict = new ProtectedSemanticHolder();
        strict.firstName = "Ada";
        strict.lastName = "Lovelace";
        strict.fullName = "RAW";
        strict.email = "ANNOTATED";

        SemanticCoherenceAdjuster strictAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .semanticMode(ObjectGenerationSemanticMode.STRICT)
                        .build(),
                new UniqueFieldTracker());
        strictAdjuster.adjustInstance(
                ProtectedSemanticHolder.class, strict, declaredFields(ProtectedSemanticHolder.class), true);
        // "Ada Lovelace" breaks @Size(min = 3, max = 3), so the full name keeps the value that met it.
        assertEquals("RAW", strict.fullName);
        assertEquals("ada.lovelace@example.com", strict.email);
    }

    @Test
    @DisplayName("in strict mode a derived string is written only where the field's own constraints allow it")
    void strictDerivedStringIsWrittenOnlyWhereItsConstraintsAllowIt() throws Exception {
        ConstrainedContact contact = new ConstrainedContact();
        contact.firstName = "Ada";
        contact.lastName = "Lovelace";
        contact.fullName = "RAW";
        contact.email = "a@b.test";
        contact.domain = "x.test";
        contact.url = "https://x";
        contact.companyName = "Acme";
        contact.companyEmail = "c@x.test";
        contact.companyUrl = "https://y";

        strictAdjuster()
                .adjustInstance(ConstrainedContact.class, contact, declaredFields(ConstrainedContact.class), true);

        // "Ada Lovelace" fits @Size(max = 20), and the domain keeps its own value, which fits too.
        assertEquals("Ada Lovelace", contact.fullName);
        assertEquals("x.test", contact.domain);
        // "ada.lovelace@x.test", "hello@x.test" and "https://www.x.test" are each too long for their fields.
        assertEquals("a@b.test", contact.email);
        assertEquals("c@x.test", contact.companyEmail);
        assertEquals("https://x", contact.url);
        assertEquals("https://y", contact.companyUrl);
    }

    @Test
    @DisplayName(
            "in strict mode a domain that its constraints keep from the derived one stays, and its siblings use that one")
    void strictDomainThatItsConstraintsKeepFromTheDerivedOneStays() throws Exception {
        // The domain's own value, read as acme.test, would no longer match its pattern.
        PatternedDomain patterned = new PatternedDomain();
        patterned.domain = "WWW.ACME.TEST";
        patterned.url = "https://x";
        strictAdjuster().adjustInstance(PatternedDomain.class, patterned, declaredFields(PatternedDomain.class), true);
        assertEquals("WWW.ACME.TEST", patterned.domain);
        assertEquals("https://www.acme.test", patterned.url);

        // The domain the email gives is too long for the field, which keeps its own value.
        ShortDomain shortDomain = new ShortDomain();
        shortDomain.email = "a@example.com";
        shortDomain.domain = "abc";
        strictAdjuster().adjustInstance(ShortDomain.class, shortDomain, declaredFields(ShortDomain.class), true);
        assertEquals("abc", shortDomain.domain);
        assertEquals("a@example.com", shortDomain.email);
    }

    @Test
    @DisplayName("in strict mode a bounded amount keeps its value, and price follows it down")
    void strictBoundedAmountKeepsItsValueAndPriceFollowsIt() throws Exception {
        ConstrainedMoney money = new ConstrainedMoney();
        money.price = new BigDecimal("500.00");
        money.amount = new BigDecimal("50.00");
        money.balance = new BigDecimal("70.00");
        strictAdjuster().adjustInstance(ConstrainedMoney.class, money, declaredFields(ConstrainedMoney.class), true);
        // 500.00 breaks the amount's @Max(100), so the price moves down to the amount instead.
        assertEquals(new BigDecimal("50.00"), money.price);
        assertEquals(new BigDecimal("50.00"), money.amount);
        assertEquals(new BigDecimal("70.00"), money.balance);

        // A balance capped below the amount keeps its value, and the amount follows it down.
        CappedBalance capped = new CappedBalance();
        capped.price = new BigDecimal("20.00");
        capped.amount = new BigDecimal("90.00");
        capped.balance = new BigDecimal("60.00");
        strictAdjuster().adjustInstance(CappedBalance.class, capped, declaredFields(CappedBalance.class), true);
        assertEquals(new BigDecimal("60.00"), capped.amount);
        assertEquals(new BigDecimal("60.00"), capped.balance);
        assertEquals(new BigDecimal("20.00"), capped.price);

        // A price above the amount it came down to follows it down too, where its own constraints allow that.
        CappedBalance above = new CappedBalance();
        above.price = new BigDecimal("80.00");
        above.amount = new BigDecimal("90.00");
        above.balance = new BigDecimal("60.00");
        strictAdjuster().adjustInstance(CappedBalance.class, above, declaredFields(CappedBalance.class), true);
        assertEquals(new BigDecimal("60.00"), above.price);
        assertEquals(new BigDecimal("60.00"), above.amount);
        assertEquals(new BigDecimal("60.00"), above.balance);
        FlooredPrice floored = new FlooredPrice();
        floored.price = new BigDecimal("80.00");
        floored.amount = new BigDecimal("90.00");
        floored.balance = new BigDecimal("60.00");
        strictAdjuster().adjustInstance(FlooredPrice.class, floored, declaredFields(FlooredPrice.class), true);
        assertEquals(new BigDecimal("80.00"), floored.price);
        assertEquals(new BigDecimal("60.00"), floored.amount);

        // A float of 0.10 is 0.1f, which Hibernate Validator reads as 0.10000000149, above @DecimalMax("0.1").
        FloatAmount floatAmount = new FloatAmount();
        floatAmount.price = new BigDecimal("0.10");
        floatAmount.amount = 0.05f;
        strictAdjuster().adjustInstance(FloatAmount.class, floatAmount, declaredFields(FloatAmount.class), true);
        assertEquals(0.05f, floatAmount.amount);
        assertEquals(new BigDecimal("0.05"), floatAmount.price);

        // 1E40 is infinite as a float, which @Max(100) rejects, rather than fail to read it.
        FloatAmount infinite = new FloatAmount();
        infinite.price = new BigDecimal("1E40");
        infinite.amount = 0.05f;
        strictAdjuster().adjustInstance(FloatAmount.class, infinite, declaredFields(FloatAmount.class), true);
        assertEquals(0.05f, infinite.amount);
        assertEquals(new BigDecimal("0.05"), infinite.price);

        // @Max(16777219) is 16777220f to Hibernate Validator, so a float balance can take 16777220.
        LargeFloatBalance large = new LargeFloatBalance();
        large.amount = new BigDecimal("16777220.00");
        large.balance = 0f;
        strictAdjuster().adjustInstance(LargeFloatBalance.class, large, declaredFields(LargeFloatBalance.class), true);
        assertEquals(16_777_220f, large.balance);
        assertEquals(new BigDecimal("16777220.00"), large.amount);

        // A price that the amount would break is not backfilled from it.
        CappedPrice backfill = new CappedPrice();
        backfill.amount = new BigDecimal("50.00");
        strictAdjuster().adjustInstance(CappedPrice.class, backfill, declaredFields(CappedPrice.class), true);
        assertNull(backfill.price);

        // A price string that its pattern keeps from the currency code stays as it is.
        PatternedPrice patterned = new PatternedPrice();
        patterned.price = "12.34";
        patterned.currency = "EUR";
        strictAdjuster().adjustInstance(PatternedPrice.class, patterned, declaredFields(PatternedPrice.class), true);
        assertEquals("12.34", patterned.price);
    }

    @Test
    @DisplayName(
            "in strict mode a status or an active flag keeps a value its constraints require, and the other follows")
    void strictStatusOrActiveFlagKeepsAValueItsConstraintsRequire() throws Exception {
        // "INACTIVE" is too long for @Size(max = 6), so the status stays, and the flag follows it.
        ConstrainedStatus status = new ConstrainedStatus();
        status.active = false;
        status.status = "ACTIVE";
        strictAdjuster().adjustInstance(ConstrainedStatus.class, status, declaredFields(ConstrainedStatus.class), true);
        assertEquals("ACTIVE", status.status);
        assertTrue(status.active);

        // An @AssertTrue flag cannot follow a status pinned at SUSPENDED, so both keep their values.
        AssertedActive asserted = new AssertedActive();
        asserted.active = true;
        asserted.status = "SUSPENDED";
        new SemanticCoherenceAdjuster(
                        ObjectGeneratorConfig.builder()
                                .semanticMode(ObjectGenerationSemanticMode.STRICT)
                                .override(AssertedActive.class, "status", () -> "SUSPENDED")
                                .build(),
                        new UniqueFieldTracker())
                .adjustInstance(AssertedActive.class, asserted, declaredFields(AssertedActive.class), true);
        assertTrue(asserted.active);
        assertEquals("SUSPENDED", asserted.status);
    }

    @Test
    @DisplayName("in strict mode a unique derived string that numbering takes past its constraints is not written")
    void strictUniqueDerivedStringNumberedPastItsConstraintsIsNotWritten() throws Exception {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .generatorConfig(GeneratorConfig.builder()
                                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                .objectUniqueField("email")
                                .objectUniquenessMaxAttempts(3)
                                .build())
                        .build(),
                tracker);
        // "alice.smith@example.com" has the 23 characters that @Size(max = 23) allows, and its numbered form has 24.
        ExactLengthEmail first = new ExactLengthEmail();
        first.firstName = "Alice";
        first.lastName = "Smith";
        first.email = "first@example.com";
        adjuster.adjustInstance(ExactLengthEmail.class, first, declaredFields(ExactLengthEmail.class), true);
        assertEquals("alice.smith@example.com", first.email);

        // Each numbered form it rejects is given back, so that three attempts serve any number of objects.
        String fieldKey = UniqueFieldTracker.fieldKey(ExactLengthEmail.class, "email");
        for (int i = 0; i < 6; i++) {
            ExactLengthEmail next = new ExactLengthEmail();
            next.firstName = "Alice";
            next.lastName = "Smith";
            next.email = "object" + i + "@example.com";
            adjuster.adjustInstance(ExactLengthEmail.class, next, declaredFields(ExactLengthEmail.class), true);
            assertEquals("object" + i + "@example.com", next.email);
        }
        assertEquals(
                "alice.smith1@example.com",
                tracker.nextUnique(fieldKey, () -> "alice.smith1@example.com", 1),
                "a rejected numbered form is still reserved");
    }

    @Test
    @DisplayName(
            "relaxed mode leaves a @Fake field alone, as it does @Randomizer and @FakeRange, and strict mode does not")
    void relaxedModeLeavesAFakeFieldAlone() throws Exception {
        FakeNamedHolder relaxed = new FakeNamedHolder();
        relaxed.firstName = "Ada";
        relaxed.lastName = "Lovelace";
        relaxed.fullName = "Grace Hopper";
        new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker())
                .adjustInstance(FakeNamedHolder.class, relaxed, declaredFields(FakeNamedHolder.class), true);
        assertEquals("Grace Hopper", relaxed.fullName);

        FakeNamedHolder strict = new FakeNamedHolder();
        strict.firstName = "Ada";
        strict.lastName = "Lovelace";
        strict.fullName = "Grace Hopper";
        strictAdjuster().adjustInstance(FakeNamedHolder.class, strict, declaredFields(FakeNamedHolder.class), true);
        assertEquals("Ada Lovelace", strict.fullName);
    }

    private static SemanticCoherenceAdjuster strictAdjuster() {
        return new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .semanticMode(ObjectGenerationSemanticMode.STRICT)
                        .build(),
                new UniqueFieldTracker());
    }

    @Test
    @DisplayName("an aligned updatedAt reads UTC while that date is in the configured range")
    void alignedUpdatedAtReadsUtcWhileThatDateIsInTheRange() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2024-02-29T12:00+05:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2024-02-29T01:00+05:00");

        Object[] offset = aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToOffset.class, createdAt, updatedAt);
        assertEquals(createdAt, offset[0]);
        assertEquals(OffsetDateTime.parse("2024-02-29T07:00Z"), offset[1]);

        Object[] zoned = aligned(
                LEAP_DAY_RANGE, TemporalPairs.OffsetToZoned.class, createdAt, ZonedDateTime.parse("2024-02-29T01:00Z"));
        assertEquals(ZonedDateTime.parse("2024-02-29T07:00Z"), zoned[1]);
    }

    @Test
    @DisplayName("an updatedAt that holds an offset reads the nearest to UTC that gives a date in the range")
    void updatedAtReadsTheOffsetNearestUtcThatGivesADateInTheRange() {
        // 2024-03-01T11:00Z, past the range at UTC, and -11:00 still reads the next day.
        OffsetDateTime late = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        ZonedDateTime lateZoned = ZonedDateTime.parse("2024-02-29T23:00-12:00[Etc/GMT+12]");
        OffsetDateTime earlier = OffsetDateTime.parse("2024-02-29T08:00+03:00");
        ZonedDateTime earlierZoned = ZonedDateTime.parse("2024-02-29T08:00+03:00[Europe/Moscow]");
        OffsetDateTime offset = OffsetDateTime.parse("2024-02-29T23:45-11:15");
        ZonedDateTime zoned = ZonedDateTime.parse("2024-02-29T23:45-11:15");

        Object[] offsetPair = aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToOffset.class, late, earlier);
        assertEquals(late, offsetPair[0]);
        assertEquals(offset, offsetPair[1]);
        assertEquals(offset, aligned(LEAP_DAY_RANGE, TemporalPairs.ZonedToOffset.class, lateZoned, earlier)[1]);
        assertEquals(zoned, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToZoned.class, late, earlierZoned)[1]);
        assertEquals(zoned, aligned(LEAP_DAY_RANGE, TemporalPairs.ZonedToZoned.class, lateZoned, earlierZoned)[1]);

        // The last moment of the range, which -11:00 still reads, with or without a fraction of a second.
        OffsetDateTime lastSecond = OffsetDateTime.parse("2024-02-29T23:59:59-11:00");
        assertEquals(lastSecond, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToOffset.class, lastSecond, earlier)[1]);
        OffsetDateTime lastFraction = OffsetDateTime.parse("2024-02-29T23:59:59.5-11:00");
        assertEquals(
                lastFraction, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToOffset.class, lastFraction, earlier)[1]);

        // 2024-02-28T11:00Z, before the range at UTC, where +13:00 reads the first moment of the range.
        OffsetDateTime earliest = OffsetDateTime.parse("2024-02-29T00:30+14:00");
        Object[] firstMoment = aligned(
                LEAP_DAY_RANGE,
                TemporalPairs.OffsetToOffset.class,
                OffsetDateTime.parse("2024-02-29T01:00+14:00"),
                earliest);
        assertEquals(OffsetDateTime.parse("2024-02-29T00:00+13:00"), firstMoment[1]);
        assertEquals(
                OffsetDateTime.parse("2024-02-29T00:05+13:00"),
                aligned(
                        LEAP_DAY_RANGE,
                        TemporalPairs.OffsetToOffset.class,
                        OffsetDateTime.parse("2024-02-29T01:05+14:00"),
                        earliest)[1]);
    }

    @Test
    @DisplayName("createdAt moves to an updatedAt that cannot reach it inside the range")
    void createdAtMovesToAnUpdatedAtThatCannotReachItInsideTheRange() {
        OffsetDateTime late = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        ZonedDateTime lateZoned = ZonedDateTime.parse("2024-02-29T23:00-12:00[Etc/GMT+12]");
        Instant morning = Instant.parse("2024-02-29T10:00:00Z");
        OffsetDateTime morningUtc = OffsetDateTime.parse("2024-02-29T10:00Z");
        OffsetDateTime midnightUtc = OffsetDateTime.parse("2024-02-29T00:00Z");

        Object[] instant = aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToInstant.class, late, morning);
        assertEquals(morningUtc, instant[0]);
        assertEquals(morning, instant[1]);

        Object[] zoned = aligned(LEAP_DAY_RANGE, TemporalPairs.ZonedToInstant.class, lateZoned, morning);
        assertEquals(ZonedDateTime.parse("2024-02-29T10:00Z"), zoned[0]);
        assertEquals(morning, zoned[1]);

        LocalDateTime localDateTime = LocalDateTime.of(2024, 2, 29, 10, 0);
        assertEquals(
                morningUtc, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToLocalDateTime.class, late, localDateTime)[0]);
        java.util.Date utilDate = java.util.Date.from(morning);
        assertEquals(morningUtc, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToUtilDate.class, late, utilDate)[0]);
        java.sql.Timestamp timestamp = java.sql.Timestamp.from(morning);
        assertEquals(morningUtc, aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToTimestamp.class, late, timestamp)[0]);

        Object[] localDate = aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToLocalDate.class, late, LEAP_DAY);
        assertEquals(midnightUtc, localDate[0]);
        assertEquals(LEAP_DAY, localDate[1]);
        java.sql.Date sqlDate = java.sql.Date.valueOf(LEAP_DAY);
        Object[] date = aligned(LEAP_DAY_RANGE, TemporalPairs.OffsetToSqlDate.class, late, sqlDate);
        assertEquals(midnightUtc, date[0]);
        assertEquals(sqlDate, date[1]);
    }

    @Test
    @DisplayName("an OffsetDateTime keeps to the UTC-offset contract, whatever offset its sibling holds")
    void offsetDateTimeKeepsToTheOffsetContractWhateverOffsetItsSiblingHolds() {
        LocalDate day = LocalDate.of(1880, 6, 15);
        // 1880-06-16T08:59:36Z, an offset with seconds, as Anchorage's rules give in 1880.
        ZonedDateTime offTheGrid = ZonedDateTime.parse("1880-06-15T23:00-09:59:36");
        OffsetDateTime morning = OffsetDateTime.parse("1880-06-15T08:00+03:00");
        OffsetDateTime onTheGrid = OffsetDateTime.parse("1880-06-15T23:59:36-09:00");

        Object[] movable = aligned(
                ObjectGeneratorConfig.builder().dateRange(day, day).build(),
                TemporalPairs.ZonedToOffset.class,
                offTheGrid,
                morning);
        assertEquals(offTheGrid, movable[0]);
        assertEquals(onTheGrid, movable[1]);

        Object[] protectedSibling = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(day, day)
                        .override(TemporalPairs.ZonedToOffset.class, "createdAt", () -> offTheGrid)
                        .build(),
                TemporalPairs.ZonedToOffset.class,
                offTheGrid,
                morning);
        assertEquals(offTheGrid, protectedSibling[0]);
        assertEquals(onTheGrid, protectedSibling[1]);

        // 2024-02-28T18:00Z, at an offset past the contract's +14:00, which +06:00 also reads in the range.
        OffsetDateTime pastTheBounds = OffsetDateTime.parse("2024-02-29T12:00+18:00");
        Object[] bounded = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(LEAP_DAY, LEAP_DAY)
                        .override(TemporalPairs.OffsetToOffset.class, "createdAt", () -> pastTheBounds)
                        .build(),
                TemporalPairs.OffsetToOffset.class,
                pastTheBounds,
                OffsetDateTime.parse("2024-02-29T00:00+14:00"));
        assertEquals(pastTheBounds, bounded[0]);
        assertEquals(OffsetDateTime.parse("2024-02-29T00:00+06:00"), bounded[1]);
    }

    @Test
    @DisplayName(
            "createdAt moves where the contract's bounds stop an OffsetDateTime updatedAt, and a ZonedDateTime need not")
    void createdAtMovesWhereTheContractsBoundsStopAnOffsetDateTimeUpdatedAt() {
        LocalDate day = LocalDate.of(1800, 6, 15);
        ObjectGeneratorConfig range =
                ObjectGeneratorConfig.builder().dateRange(day, day).build();
        // 1800-06-16T14:56Z. Only an offset below -14:56 reads the range, as Manila's rules give in 1800.
        ZonedDateTime late = ZonedDateTime.parse("1800-06-15T23:00-15:56");

        OffsetDateTime morning = OffsetDateTime.parse("1800-06-15T08:00+03:00");
        Object[] readsUtc = aligned(range, TemporalPairs.ZonedToOffset.class, late, morning);
        assertEquals(ZonedDateTime.parse("1800-06-15T05:00Z"), readsUtc[0]);
        assertEquals(morning, readsUtc[1]);

        // 1800-06-14T11:00Z, before the range at UTC, so createdAt needs an offset as well.
        OffsetDateTime early = OffsetDateTime.parse("1800-06-15T01:00+14:00");
        Object[] readsAnOffset = aligned(range, TemporalPairs.ZonedToOffset.class, late, early);
        assertEquals(ZonedDateTime.parse("1800-06-15T00:00+13:00"), readsAnOffset[0]);
        assertEquals(early, readsAnOffset[1]);

        // The contract's bounds are for an OffsetDateTime. A ZonedDateTime holds any offset, so it can move.
        ZonedDateTime zonedMorning = ZonedDateTime.parse("1800-06-15T08:00+03:00");
        Object[] zoned = aligned(range, TemporalPairs.ZonedToZoned.class, late, zonedMorning);
        assertEquals(late, zoned[0]);
        assertEquals(ZonedDateTime.parse("1800-06-15T23:56-15:00"), zoned[1]);

        Object[] followsOutOfTheRange = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(day, day)
                        .override(TemporalPairs.ZonedToOffset.class, "createdAt", () -> late)
                        .build(),
                TemporalPairs.ZonedToOffset.class,
                late,
                morning);
        assertEquals(OffsetDateTime.parse("1800-06-16T14:56Z"), followsOutOfTheRange[1]);
    }

    @Test
    @DisplayName(
            "a field beside a protected sibling near the range stays in the range, and follows one too far for that")
    void fieldBesideAProtectedSiblingStaysInTheRangeUnlessItIsTooFarAway() {
        Instant nextMorning = Instant.parse("2024-03-01T05:00:00Z");
        assertEquals(
                OffsetDateTime.parse("2024-02-29T23:45-05:15"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.InstantToOffset.class, "createdAt", () -> nextMorning)
                                .build(),
                        TemporalPairs.InstantToOffset.class,
                        nextMorning,
                        OffsetDateTime.parse("2024-02-29T08:00+03:00"))[1]);

        Instant farLater = Instant.parse("2030-01-01T00:00:00Z");
        assertEquals(
                OffsetDateTime.parse("2030-01-01T00:00Z"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.InstantToOffset.class, "createdAt", () -> farLater)
                                .build(),
                        TemporalPairs.InstantToOffset.class,
                        farLater,
                        OffsetDateTime.parse("2024-02-29T08:00+03:00"))[1]);
        assertEquals(
                ZonedDateTime.parse("2030-01-01T00:00Z"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.InstantToZoned.class, "createdAt", () -> farLater)
                                .build(),
                        TemporalPairs.InstantToZoned.class,
                        farLater,
                        ZonedDateTime.parse("2024-02-29T08:00+03:00"))[1]);

        Instant farEarlier = Instant.parse("2000-01-01T00:00:00Z");
        assertEquals(
                OffsetDateTime.parse("2000-01-01T00:00Z"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.OffsetToInstant.class, "updatedAt", () -> farEarlier)
                                .build(),
                        TemporalPairs.OffsetToInstant.class,
                        OffsetDateTime.parse("2024-02-29T08:00+03:00"),
                        farEarlier)[0]);
        assertEquals(
                ZonedDateTime.parse("2000-01-01T00:00Z"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.ZonedToInstant.class, "updatedAt", () -> farEarlier)
                                .build(),
                        TemporalPairs.ZonedToInstant.class,
                        ZonedDateTime.parse("2024-02-29T08:00+03:00"),
                        farEarlier)[0]);
    }

    @Test
    @DisplayName("a field follows a protected sibling out of the range where no date in it keeps them in order")
    void fieldFollowsAProtectedSiblingOutOfTheRangeWhereNoDateInItKeepsThemInOrder() {
        OffsetDateTime lateCreatedAt = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        Instant updatedAt = Instant.parse("2024-02-29T10:00:00Z");
        Object[] updatedFollows = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(LEAP_DAY, LEAP_DAY)
                        .override(TemporalPairs.OffsetToInstant.class, "createdAt", () -> lateCreatedAt)
                        .build(),
                TemporalPairs.OffsetToInstant.class,
                lateCreatedAt,
                updatedAt);
        assertEquals(lateCreatedAt, updatedFollows[0]);
        assertEquals(Instant.parse("2024-03-01T11:00:00Z"), updatedFollows[1]);

        OffsetDateTime earlyUpdatedAt = OffsetDateTime.parse("2024-02-29T01:00+14:00");
        LocalDateTime createdAt = LocalDateTime.of(2024, 2, 29, 5, 0);
        Object[] createdFollows = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(LEAP_DAY, LEAP_DAY)
                        .override(TemporalPairs.LocalDateTimeToOffset.class, "updatedAt", () -> earlyUpdatedAt)
                        .build(),
                TemporalPairs.LocalDateTimeToOffset.class,
                createdAt,
                earlyUpdatedAt);
        assertEquals(LocalDateTime.of(2024, 2, 28, 11, 0), createdFollows[0]);
        assertEquals(earlyUpdatedAt, createdFollows[1]);
    }

    @Test
    @DisplayName("a field following a protected sibling reads an offset that stays in the range")
    void fieldFollowingAProtectedSiblingReadsAnOffsetThatStaysInTheRange() {
        OffsetDateTime lateCreatedAt = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2024-02-29T08:00+03:00");
        assertEquals(
                OffsetDateTime.parse("2024-02-29T23:45-11:15"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.OffsetToOffset.class, "createdAt", () -> lateCreatedAt)
                                .build(),
                        TemporalPairs.OffsetToOffset.class,
                        lateCreatedAt,
                        updatedAt)[1]);

        OffsetDateTime earlyUpdatedAt = OffsetDateTime.parse("2024-02-29T01:00+14:00");
        OffsetDateTime createdAt = OffsetDateTime.parse("2024-02-29T05:00Z");
        assertEquals(
                OffsetDateTime.parse("2024-02-29T00:00+13:00"),
                aligned(
                        ObjectGeneratorConfig.builder()
                                .dateRange(LEAP_DAY, LEAP_DAY)
                                .override(TemporalPairs.OffsetToOffset.class, "updatedAt", () -> earlyUpdatedAt)
                                .build(),
                        TemporalPairs.OffsetToOffset.class,
                        createdAt,
                        earlyUpdatedAt)[0]);
    }

    @Test
    @DisplayName("updatedAt moves when neither value can stay in the range")
    void updatedAtMovesWhenNeitherValueCanStayInTheRange() {
        Instant createdAt = Instant.parse("2030-01-02T00:00:00Z");
        Instant updatedAt = Instant.parse("2030-01-01T00:00:00Z");

        Object[] aligned = aligned(LEAP_DAY_RANGE, TemporalPairs.InstantToInstant.class, createdAt, updatedAt);

        assertEquals(createdAt, aligned[0]);
        assertEquals(createdAt, aligned[1]);
    }

    @Test
    @DisplayName("with no configured range an aligned value reads UTC, whatever date that is")
    void withNoConfiguredRangeAnAlignedValueReadsUtc() {
        ObjectGeneratorConfig noRange = ObjectGeneratorConfig.builder().build();
        OffsetDateTime late = OffsetDateTime.parse("2024-02-29T23:00-12:00");

        assertEquals(
                OffsetDateTime.parse("2024-03-01T11:00Z"),
                aligned(
                        noRange,
                        TemporalPairs.OffsetToOffset.class,
                        late,
                        OffsetDateTime.parse("2024-02-29T08:00+03:00"))[1]);
        assertEquals(
                Instant.parse("2024-03-01T11:00:00Z"),
                aligned(noRange, TemporalPairs.OffsetToInstant.class, late, Instant.parse("2024-02-29T10:00:00Z"))[1]);
        assertEquals(
                LocalDate.of(2024, 3, 1), aligned(noRange, TemporalPairs.OffsetToLocalDate.class, late, LEAP_DAY)[1]);
    }

    @Test
    @DisplayName("a member type the pass cannot write keeps its value under a configured range")
    void memberTypeThePassCannotWriteKeepsItsValueUnderAConfiguredRange() {
        Instant updatedAt = Instant.parse("2024-02-29T10:00:00Z");
        Instant late = Instant.parse("2024-02-29T11:00:00Z");
        Object[] unwritableUpdatedAt = aligned(LEAP_DAY_RANGE, TemporalUpdatedAt.class, late, updatedAt);
        assertEquals(late, unwritableUpdatedAt[0]);
        assertEquals(updatedAt, unwritableUpdatedAt[1]);

        // A createdAt that cannot be written cannot move, so updatedAt follows it, even out of the range.
        OffsetDateTime outOfReach = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        Object[] unwritableCreatedAt = aligned(LEAP_DAY_RANGE, TemporalCreatedAt.class, outOfReach, updatedAt);
        assertEquals(outOfReach, unwritableCreatedAt[0]);
        assertEquals(Instant.parse("2024-03-01T11:00:00Z"), unwritableCreatedAt[1]);
    }

    @Test
    @DisplayName("in strict mode a constrained timestamp moves only within its constraints, and its sibling follows it")
    void strictTimestampMovesOnlyWithinItsConstraints() {
        Instant now = Instant.parse("2024-02-29T12:00:00Z");
        ObjectGeneratorConfig strict = strictAt(now).build();
        Instant hourAgo = now.minusSeconds(3_600);
        Instant twoHoursAgo = now.minusSeconds(7_200);
        Instant hourLater = now.plusSeconds(3_600);
        Instant twoHoursLater = now.plusSeconds(7_200);

        // updatedAt moves on while it stays before the clock, and cannot pass it, so createdAt moves back instead.
        assertArrayEquals(
                new Object[] {hourAgo, hourAgo},
                aligned(strict, BoundedPairs.PastUpdatedAt.class, hourAgo, twoHoursAgo));
        assertArrayEquals(
                new Object[] {twoHoursAgo, twoHoursAgo},
                aligned(strict, BoundedPairs.PastUpdatedAt.class, hourLater, twoHoursAgo));
        // @PastOrPresent allows the clock's instant itself, and @Past does not.
        assertArrayEquals(
                new Object[] {now, now}, aligned(strict, BoundedPairs.PastOrPresentUpdatedAt.class, now, twoHoursAgo));
        assertArrayEquals(
                new Object[] {twoHoursAgo, twoHoursAgo},
                aligned(strict, BoundedPairs.PastUpdatedAt.class, now, twoHoursAgo));

        // A createdAt held in the future follows a pinned updatedAt that is still in the future, but not one in the
        // past.
        ObjectGeneratorConfig pinnedLater = strictAt(now)
                .override(BoundedPairs.FutureOrPresentCreatedAt.class, "updatedAt", () -> hourLater)
                .build();
        assertArrayEquals(
                new Object[] {hourLater, hourLater},
                aligned(pinnedLater, BoundedPairs.FutureOrPresentCreatedAt.class, twoHoursLater, hourLater));
        ObjectGeneratorConfig pinnedEarlier = strictAt(now)
                .override(BoundedPairs.FutureOrPresentCreatedAt.class, "updatedAt", () -> hourAgo)
                .build();
        assertArrayEquals(
                new Object[] {twoHoursLater, hourAgo},
                aligned(pinnedEarlier, BoundedPairs.FutureOrPresentCreatedAt.class, twoHoursLater, hourAgo));

        // Neither can move, so both keep their values, out of order.
        assertArrayEquals(
                new Object[] {hourLater, hourAgo}, aligned(strict, BoundedPairs.Apart.class, hourLater, hourAgo));
    }

    @Test
    @DisplayName("in strict mode updatedAt leaves the range where createdAt cannot move into it within its constraint")
    void strictUpdatedAtLeavesTheRangeWhereCreatedAtCannotMoveIntoIt() {
        // 2024-03-01T11:00Z, past the range at UTC, which updatedAt cannot reach inside the range. createdAt could
        // move back to 10:00Z inside it, but that is before the clock, so updatedAt follows it out of the range.
        OffsetDateTime late = OffsetDateTime.parse("2024-02-29T23:00-12:00");
        Instant morning = Instant.parse("2024-02-29T10:00:00Z");
        ObjectGeneratorConfig config = strictAt(Instant.parse("2024-02-29T10:30:00Z"))
                .dateRange(LEAP_DAY, LEAP_DAY)
                .build();

        Object[] aligned = aligned(config, BoundedPairs.FutureOrPresentOffsetCreatedAt.class, late, morning);

        assertEquals(late, aligned[0]);
        assertEquals(Instant.parse("2024-03-01T11:00:00Z"), aligned[1]);
    }

    @Test
    @DisplayName("in strict mode an age its constraints reject keeps its value, and birthDate follows it")
    void strictAgeItsConstraintsRejectKeepsItsValue() {
        Instant now = Instant.parse("2024-02-29T12:00:00Z");
        ObjectGeneratorConfig strict = strictAt(now).build();

        // 20 is outside @Min(30) @Max(40), so the age stays 35 and the birth date follows it.
        assertArrayEquals(
                new Object[] {LEAP_DAY.minusYears(35), 35},
                aligned(strict, BoundedPairs.RangedAge.class, LEAP_DAY.minusYears(20), 35));
        // 33 is inside, so the age follows the birth date, as it always has.
        assertArrayEquals(
                new Object[] {LEAP_DAY.minusYears(33), 33},
                aligned(strict, BoundedPairs.RangedAge.class, LEAP_DAY.minusYears(33), 35));
        // A @Null age takes no value, and leaves the birth date nothing to follow.
        assertArrayEquals(
                new Object[] {LEAP_DAY.minusYears(20), null},
                aligned(strict, BoundedPairs.NullAge.class, LEAP_DAY.minusYears(20), null));
        // A @Past birth date cannot be today, which an overridden age of 0 gives, so it keeps its value.
        ObjectGeneratorConfig ageZero = strictAt(now)
                .override(BoundedPairs.PastBirthDate.class, "age", () -> 0)
                .build();
        assertArrayEquals(
                new Object[] {LEAP_DAY.minusDays(1), 0},
                aligned(ageZero, BoundedPairs.PastBirthDate.class, LEAP_DAY.minusDays(1), 0));
    }

    @Test
    @DisplayName("an age that no birth date of its type can hold leaves birthDate as it is, rather than fail")
    void ageNoBirthDateCanHoldLeavesBirthDateAsItIs() {
        // Beyond the years a LocalDate holds.
        ObjectGeneratorConfig hugeAge = ObjectGeneratorConfig.builder()
                .override(AgedPairs.LocalBirthDate.class, "age", () -> Integer.MAX_VALUE)
                .build();
        assertArrayEquals(
                new Object[] {LEAP_DAY, Integer.MAX_VALUE},
                aligned(hugeAge, AgedPairs.LocalBirthDate.class, LEAP_DAY, Integer.MAX_VALUE));

        // Within them, but beyond the milliseconds a Calendar holds.
        java.util.GregorianCalendar calendar =
                java.util.GregorianCalendar.from(ZonedDateTime.parse("2000-06-04T12:00Z"));
        ObjectGeneratorConfig calendarAge = ObjectGeneratorConfig.builder()
                .override(AgedPairs.CalendarBirthDate.class, "age", () -> 500_000_000)
                .build();
        assertArrayEquals(
                new Object[] {calendar, 500_000_000},
                aligned(calendarAge, AgedPairs.CalendarBirthDate.class, calendar, 500_000_000));

        // A java.sql.Date and, on JDK 21, a Timestamp would wrap such a date round, to a year after 84,000,000.
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.of(2000, 6, 4));
        ObjectGeneratorConfig sqlDateAge = ObjectGeneratorConfig.builder()
                .override(AgedPairs.SqlDateBirthDate.class, "age", () -> 500_000_000)
                .build();
        assertArrayEquals(
                new Object[] {sqlDate, 500_000_000},
                aligned(sqlDateAge, AgedPairs.SqlDateBirthDate.class, sqlDate, 500_000_000));

        // In strict mode, an age that its constraints keep from a @Future birth date's leaves both as they are,
        // and a wrapped Timestamp in the future must not take the place of one.
        ObjectGeneratorConfig strict =
                strictAt(Instant.parse("2024-02-29T12:00:00Z")).build();
        LocalDate tomorrow = LEAP_DAY.plusDays(1);
        assertArrayEquals(
                new Object[] {tomorrow, Integer.MAX_VALUE},
                aligned(strict, AgedPairs.UnreachableBirthDate.class, tomorrow, Integer.MAX_VALUE));
        java.sql.Timestamp later = java.sql.Timestamp.from(Instant.parse("2024-03-01T00:00:00Z"));
        assertArrayEquals(
                new Object[] {later, 500_000_000},
                aligned(strict, AgedPairs.FutureTimestampBirthDate.class, later, 500_000_000));
        java.sql.Date laterDate = java.sql.Date.valueOf(LocalDate.of(2024, 3, 1));
        assertArrayEquals(
                new Object[] {laterDate, 292_277_078},
                aligned(strict, AgedPairs.FutureSqlDateBirthDate.class, laterDate, 292_277_078));
        // An age of 2,024 gives 0000-02-29, which a java.sql.Date cannot hold, although its milliseconds are past.
        java.sql.Date earlierDate = java.sql.Date.valueOf(LocalDate.of(2024, 2, 28));
        assertArrayEquals(
                new Object[] {earlierDate, 2_024},
                aligned(strict, AgedPairs.PastSqlDateBirthDate.class, earlierDate, 2_024));
    }

    @Test
    @DisplayName("a birth date derived from an age gives that age, on 29 February too")
    void birthDateDerivedFromAnAgeGivesThatAgeOnALeapDay() {
        Instant now = Instant.parse("2024-02-29T12:00:00Z");
        // A year back, 29 February becomes the 28th, which still gives a whole year to the leap day.
        assertEquals(LocalDate.of(2023, 2, 28), birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 1));
        assertEquals(LocalDate.of(2020, 2, 29), birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 4));
        // A year ahead, the 28th would be a day short of the year that an age of -1 needs, so it is 1 March.
        assertEquals(LocalDate.of(2025, 3, 1), birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, -1));
        assertEquals(LocalDate.of(2028, 2, 29), birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, -4));

        // So too where, in strict mode, a constrained age keeps its value and a @Future birth date follows it.
        LocalDate tomorrow = LEAP_DAY.plusDays(1);
        assertArrayEquals(
                new Object[] {LocalDate.of(2025, 3, 1), -1},
                aligned(strictAt(now).build(), AgedPairs.FutureLocalBirthDate.class, tomorrow, -1));
    }

    @Test
    @DisplayName("a birth date in the first or last year its type holds is derived, and one a year beyond is not")
    void birthDateInTheFirstOrLastYearItsTypeHoldsIsDerived() {
        Instant now = Instant.parse("2024-02-29T12:00:00Z");
        // A LocalDate holds the years from -999,999,999 to 999,999,999. Ahead of the leap day, a negative age gives
        // 1 March, as above.
        assertEquals(
                LocalDate.of(-999_999_999, 2, 28),
                birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 2_024 + 999_999_999));
        assertEquals(LEAP_DAY, birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 2_024 + 1_000_000_000));
        assertEquals(
                LocalDate.of(999_999_999, 3, 1),
                birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 2_024 - 999_999_999));
        assertEquals(LEAP_DAY, birthDateBeside(now, AgedPairs.LocalBirthDate.class, LEAP_DAY, 2_024 - 1_000_000_000));

        // A Calendar holds in full the years from -292,275,054 to 292,278,993, which a long of milliseconds reaches.
        java.util.GregorianCalendar calendar =
                java.util.GregorianCalendar.from(ZonedDateTime.parse("2000-06-04T12:00Z"));
        assertEquals(
                LocalDate.of(-292_275_054, 2, 28).atStartOfDay().toInstant(ZoneOffset.UTC),
                ((java.util.Calendar)
                                birthDateBeside(now, AgedPairs.CalendarBirthDate.class, calendar, 2_024 + 292_275_054))
                        .toInstant());
        assertSame(calendar, birthDateBeside(now, AgedPairs.CalendarBirthDate.class, calendar, 2_024 + 292_275_055));
        assertEquals(
                LocalDate.of(292_278_993, 3, 1).atStartOfDay().toInstant(ZoneOffset.UTC),
                ((java.util.Calendar)
                                birthDateBeside(now, AgedPairs.CalendarBirthDate.class, calendar, 2_024 - 292_278_993))
                        .toInstant());
        assertSame(calendar, birthDateBeside(now, AgedPairs.CalendarBirthDate.class, calendar, 2_024 - 292_278_994));

        // A java.sql.Date holds the years of the common era, from 1: valueOf drops the era of an earlier year, so
        // that year 0 reads back as year 1, and 0000-02-29 does not read back at all. Further back, in the first
        // year a Calendar holds, valueOf wraps round. In each case the birth date stays.
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.of(2000, 6, 4));
        assertEquals(
                java.sql.Date.valueOf(LocalDate.of(1, 2, 28)),
                birthDateBeside(now, AgedPairs.SqlDateBirthDate.class, sqlDate, 2_024 - 1));
        assertSame(sqlDate, birthDateBeside(now, AgedPairs.SqlDateBirthDate.class, sqlDate, 2_024));
        assertSame(sqlDate, birthDateBeside(now, AgedPairs.SqlDateBirthDate.class, sqlDate, 2_024 + 292_275_054));
        // A date in the Julian calendar before 1582, or after it, is derived as valueOf gives it.
        assertEquals(
                java.sql.Date.valueOf(LocalDate.of(1000, 2, 28)),
                birthDateBeside(now, AgedPairs.SqlDateBirthDate.class, sqlDate, 1_024));
        assertEquals(
                java.sql.Date.valueOf(LocalDate.of(1964, 2, 29)),
                birthDateBeside(now, AgedPairs.SqlDateBirthDate.class, sqlDate, 60));
        // valueOf moves a date that the change from the Julian calendar skipped, 1582-10-05 to 1582-10-14, on by
        // ten days, so such a birth date stays too.
        assertSame(
                sqlDate,
                birthDateBeside(Instant.parse("2024-10-10T12:00:00Z"), AgedPairs.SqlDateBirthDate.class, sqlDate, 442));
    }

    /** The birth date the pass leaves beside an overridden age, under a clock fixed at this instant. */
    private static Object birthDateBeside(Instant now, Class<?> pair, Object birthDate, int age) {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .clock(Clock.fixed(now, ZoneOffset.UTC))
                        .build())
                .override(pair, "age", () -> age)
                .build();
        return aligned(config, pair, birthDate, age)[0];
    }

    @Test
    @DisplayName("the range check reads a value no date can hold without failing")
    void rangeCheckReadsAValueNoDateCanHoldWithoutFailing() {
        Object[] aligned = aligned(
                ObjectGeneratorConfig.builder()
                        .dateRange(LEAP_DAY, LEAP_DAY)
                        .override(TemporalPairs.InstantToInstant.class, "createdAt", () -> Instant.MAX)
                        .build(),
                TemporalPairs.InstantToInstant.class,
                Instant.MAX,
                Instant.parse("2024-02-29T10:00:00Z"));

        assertEquals(Instant.MAX, aligned[1]);
    }

    @Test
    @DisplayName("timestamp fallback and uniqueness branches are exercised")
    void timestampFallbackAndUniquenessBranchesAreExercised() throws Exception {
        SemanticCoherenceAdjuster updatedProtectedAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder()
                        .override(ManualTemporalHolder.class, "updatedAt", () -> Instant.EPOCH)
                        .build(),
                new UniqueFieldTracker());
        ManualTemporalHolder temporal = new ManualTemporalHolder();
        temporal.createdAt = LocalDateTime.of(2026, 4, 20, 12, 0);
        temporal.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        updatedProtectedAdjuster.adjustInstance(
                ManualTemporalHolder.class, temporal, declaredFields(ManualTemporalHolder.class), true);
        assertEquals(LocalDateTime.ofInstant(temporal.updatedAt, ZoneOffset.UTC), temporal.createdAt);

        SemanticCoherenceAdjuster emailAliasAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder().uniqueFields("emailaddress").build(), new UniqueFieldTracker());
        EmailAliasHolder firstAlias = new EmailAliasHolder();
        firstAlias.firstName = "Ada";
        firstAlias.lastName = "Lovelace";
        emailAliasAdjuster.adjustInstance(
                EmailAliasHolder.class, firstAlias, declaredFields(EmailAliasHolder.class), true);
        EmailAliasHolder secondAlias = new EmailAliasHolder();
        secondAlias.firstName = "Ada";
        secondAlias.lastName = "Lovelace";
        emailAliasAdjuster.adjustInstance(
                EmailAliasHolder.class, secondAlias, declaredFields(EmailAliasHolder.class), true);
        assertEquals("ada.lovelace@example.com", firstAlias.emailAddress);
        assertEquals("ada.lovelace1@example.com", secondAlias.emailAddress);

        SemanticCoherenceAdjuster uniqueFullNameAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder().uniqueFields("fullname").build(), new UniqueFieldTracker());
        ManualNameHolder firstName = new ManualNameHolder();
        firstName.firstName = "Ada";
        firstName.lastName = "Lovelace";
        uniqueFullNameAdjuster.adjustInstance(
                ManualNameHolder.class, firstName, declaredFields(ManualNameHolder.class), true);
        ManualNameHolder secondName = new ManualNameHolder();
        secondName.firstName = "Ada";
        secondName.lastName = "Lovelace";
        uniqueFullNameAdjuster.adjustInstance(
                ManualNameHolder.class, secondName, declaredFields(ManualNameHolder.class), true);
        assertEquals("Ada Lovelace", firstName.fullName);
        assertEquals("Ada Lovelace1", secondName.fullName);
    }

    @Test
    @DisplayName("internal uniqueness helpers cover null and alias branches")
    void internalUniquenessHelpersCoverNullAndAliasBranches() throws Exception {
        SemanticCoherenceAdjuster aliasAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder().uniqueFields("emailaddress").build(), new UniqueFieldTracker());
        assertTrue((Boolean) aliasAdjuster.isUniqueField("email", "email"));

        SemanticCoherenceAdjuster semanticAdjuster = new SemanticCoherenceAdjuster(
                ObjectGeneratorConfig.builder().uniqueFields("email").build(), new UniqueFieldTracker());
        assertTrue((Boolean) semanticAdjuster.isUniqueField("customField", "email"));

        SemanticCoherenceAdjuster.Slot slot =
                slotMap(new ManualEmailHolder(), "email").get("email");
        assertNull(semanticAdjuster.applyUniqueness(slot, "email", null));
    }

    @Test
    @DisplayName("reflection slots wrap read and write failures")
    void reflectionSlotsWrapReadAndWriteFailures() throws Exception {
        PrivateValueHolder privateHolder = new PrivateValueHolder();
        Field privateField = PrivateValueHolder.class.getDeclaredField("value");
        SemanticCoherenceAdjuster.Slot slot = new SemanticCoherenceAdjuster.ReflectionSlot(
                PrivateValueHolder.class, privateField, privateHolder, false, 3);

        ObjectGenerationException readException = assertThrows(ObjectGenerationException.class, slot::getValue);
        GenerationFailureContext readContext = readException.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, readContext.category());
        assertEquals(GenerationOperation.READ, readContext.operation());
        assertEquals("PrivateValueHolder.value", readContext.path());
        assertEquals(String.class.getTypeName(), readContext.declaredType());
        assertEquals(3, readContext.depth());

        SemanticCoherenceAdjuster.Slot lenientReadSlot = new SemanticCoherenceAdjuster.ReflectionSlot(
                PrivateValueHolder.class, privateField, privateHolder, true, 3);
        assertNull(lenientReadSlot.getValue());

        Field publicField = PublicValueHolder.class.getDeclaredField("value");
        SemanticCoherenceAdjuster.Slot strictSlot = new SemanticCoherenceAdjuster.ReflectionSlot(
                PublicValueHolder.class, publicField, new PublicValueHolder(), false, 4);
        ObjectGenerationException writeException =
                assertThrows(ObjectGenerationException.class, () -> strictSlot.setValue(123));
        GenerationFailureContext writeContext = writeException.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.ASSIGNMENT, writeContext.category());
        assertEquals(GenerationOperation.ALIGN_SEMANTICS, writeContext.operation());
        assertEquals("PublicValueHolder.value", writeContext.path());
        assertEquals(String.class.getTypeName(), writeContext.declaredType());
        assertEquals(4, writeContext.depth());

        PublicValueHolder ignored = new PublicValueHolder();
        SemanticCoherenceAdjuster.Slot lenientSlot =
                new SemanticCoherenceAdjuster.ReflectionSlot(PublicValueHolder.class, publicField, ignored, true, 4);
        assertDoesNotThrow(() -> lenientSlot.setValue(123));
        assertEquals("ok", ignored.value);
    }

    @Test
    @DisplayName("lenient reflection diagnostics contain context but no field values")
    void lenientReflectionDiagnosticsAreSanitized() throws Exception {
        List<GenerationFailureDiagnostic> diagnostics = new java.util.ArrayList<>();
        GenerationFailureListener listener = diagnostics::add;
        Field privateField = PrivateValueHolder.class.getDeclaredField("value");
        SemanticCoherenceAdjuster.Slot readSlot = new SemanticCoherenceAdjuster.ReflectionSlot(
                PrivateValueHolder.class, privateField, new PrivateValueHolder(), true, 3, listener);

        Field publicField = PublicValueHolder.class.getDeclaredField("value");
        SemanticCoherenceAdjuster.Slot writeSlot = new SemanticCoherenceAdjuster.ReflectionSlot(
                PublicValueHolder.class, publicField, new PublicValueHolder(), true, 4, listener);

        Logger logger = (Logger) LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            assertNull(readSlot.getValue());
            assertDoesNotThrow(() -> writeSlot.setValue(123));

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();
            assertTrue(messages.stream().anyMatch(message -> message.contains("PrivateValueHolder.value")));
            assertTrue(messages.stream().anyMatch(message -> message.contains("PublicValueHolder.value")));
            assertFalse(messages.stream()
                    .anyMatch(message ->
                            message.contains("hidden") || message.contains("ok") || message.contains("123")));
            assertEquals(
                    List.of("PrivateValueHolder.value", "PublicValueHolder.value"),
                    diagnostics.stream()
                            .map(diagnostic -> diagnostic.context().path())
                            .toList());
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
        }
    }

    /**
     * Runs the pass over a two-member record, such as one with a createdAt and an updatedAt, and returns what it
     * leaves in the members.
     */
    private static Object[] aligned(ObjectGeneratorConfig config, Class<?> pair, Object first, Object second) {
        Object[] args = {first, second};
        new SemanticCoherenceAdjuster(config, new UniqueFieldTracker())
                .adjustRecordArguments(pair, pair.getRecordComponents(), pair.getDeclaredFields(), args);
        return args;
    }

    /** A strict configuration whose clock is fixed at this instant, in UTC. */
    private static ObjectGeneratorConfig.Builder strictAt(Instant now) {
        return ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .clock(Clock.fixed(now, ZoneOffset.UTC))
                        .build())
                .semanticMode(ObjectGenerationSemanticMode.STRICT);
    }

    /** Pairs whose members carry the constraints that the strict pass keeps them within. */
    static final class BoundedPairs {

        record PastUpdatedAt(Instant createdAt, @Past Instant updatedAt) {}

        record PastOrPresentUpdatedAt(
                Instant createdAt, @PastOrPresent Instant updatedAt) {}

        record FutureOrPresentCreatedAt(@FutureOrPresent Instant createdAt, Instant updatedAt) {}

        record FutureOrPresentOffsetCreatedAt(
                @FutureOrPresent OffsetDateTime createdAt, Instant updatedAt) {}

        record Apart(@Future Instant createdAt, @Past Instant updatedAt) {}

        record RangedAge(
                LocalDate birthDate, @Min(30) @Max(40) int age) {}

        record NullAge(LocalDate birthDate, @Null Integer age) {}

        record PastBirthDate(@Past LocalDate birthDate, int age) {}
    }

    /** Pairs of a birth date and an age, for ages that no birth date of its type can hold. */
    static final class AgedPairs {

        record LocalBirthDate(LocalDate birthDate, int age) {}

        record CalendarBirthDate(java.util.Calendar birthDate, int age) {}

        record SqlDateBirthDate(java.sql.Date birthDate, int age) {}

        record UnreachableBirthDate(
                @Future LocalDate birthDate,
                @Min(Integer.MAX_VALUE) int age) {}

        record FutureTimestampBirthDate(
                @Future java.sql.Timestamp birthDate,
                @Min(500_000_000) int age) {}

        record FutureSqlDateBirthDate(
                @Future java.sql.Date birthDate,
                @Min(292_277_078) int age) {}

        record PastSqlDateBirthDate(
                @Past java.sql.Date birthDate,
                @Min(2_024) @Max(2_024) int age) {}

        record FutureLocalBirthDate(
                @Future LocalDate birthDate,
                @Min(-1) @Max(-1) int age) {}
    }

    private static List<Field> declaredFields(Class<?> type) {
        return List.of(type.getDeclaredFields());
    }

    private static Map<String, SemanticCoherenceAdjuster.Slot> slotMap(Object instance, String... fieldNames)
            throws Exception {
        Map<String, SemanticCoherenceAdjuster.Slot> slots = new java.util.LinkedHashMap<>();
        for (String fieldName : fieldNames) {
            String semanticKey = FieldGeneratorResolver.semanticKeyForFieldName(fieldName);
            slots.put(semanticKey, rawSlot(instance, fieldName));
        }
        return slots;
    }

    private static SemanticCoherenceAdjuster.Slot rawSlot(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return new SemanticCoherenceAdjuster.ReflectionSlot(instance.getClass(), field, instance, false);
    }

    static class ManualCoherenceHolder {

        String companyName;
        String domain;
        String url;
        String companyEmail;
        String companyUrl;
    }

    static class ManualEmailHolder {

        String firstName;
        String lastName;
        String email;
        String url;
    }

    static class ManualNameHolder {

        String firstName;
        String lastName;
        String fullName;
    }

    record TemporalUpdatedAt(Instant createdAt, Temporal updatedAt) {}

    record TemporalCreatedAt(Temporal createdAt, Instant updatedAt) {}

    static class ManualTemporalHolder {

        LocalDateTime createdAt;
        Instant updatedAt;
    }

    static class ManualAgeHolder {

        LocalDate birthDate;
        Integer age;
    }

    static class EmailAliasHolder {

        String firstName;
        String lastName;
        String emailAddress;
    }

    static class UrlOnlyHolder {

        String url;
    }

    static class ManualStatusHolder {

        boolean active;
        String status;
    }

    static class ManualNullableStatusHolder {

        Boolean active;
        String status;
    }

    static class ManualEnumStatusHolder {

        boolean active;
        ManualLifecycleState status;
    }

    static class ManualOpaqueStatusHolder {

        boolean active;
        Integer status;
    }

    static class ProtectedSemanticHolder {

        String firstName;
        String lastName;

        @Size(min = 3, max = 3) String fullName;

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    static class ConstrainedContact {

        String firstName;
        String lastName;

        @Size(max = 20) String fullName;

        @Size(max = 12) String email;

        @Size(max = 10) String domain;

        @Size(max = 10) String url;

        String companyName;

        @Size(max = 10) String companyEmail;

        @Size(max = 10) String companyUrl;
    }

    static class PatternedDomain {

        @Pattern(regexp = "WWW\\.[A-Z]+\\.TEST") String domain;

        String url;
    }

    static class ShortDomain {

        String email;

        @Size(max = 5) String domain;
    }

    static class ConstrainedMoney {

        BigDecimal price;

        @Max(100) BigDecimal amount;

        BigDecimal balance;
    }

    static class CappedBalance {

        BigDecimal price;
        BigDecimal amount;

        @Max(60) BigDecimal balance;
    }

    static class CappedPrice {

        @Max(10) BigDecimal price;

        BigDecimal amount;
    }

    static class FlooredPrice {

        @Min(70) BigDecimal price;

        BigDecimal amount;

        @Max(60) BigDecimal balance;
    }

    static class FloatAmount {

        BigDecimal price;

        @DecimalMax("0.1") float amount;
    }

    static class LargeFloatBalance {

        BigDecimal amount;

        @Max(16_777_219) float balance;
    }

    static class PatternedPrice {

        @Pattern(regexp = "\\d+\\.\\d{2}") String price;

        String currency;
    }

    static class ConstrainedStatus {

        boolean active;

        @Size(max = 6) String status;
    }

    static class AssertedActive {

        @AssertTrue boolean active;

        String status;
    }

    static class ExactLengthEmail {

        String firstName;
        String lastName;

        @Size(max = 23) String email;
    }

    static class FakeNamedHolder {

        String firstName;
        String lastName;

        @Fake("fullName")
        String fullName;
    }

    record ManualRecord(String firstName, String lastName, String fullName) {}

    static class PrivateValueHolder {

        private String value = "hidden";
    }

    static class PublicValueHolder {

        public String value = "ok";
    }

    public static class AnnotatedValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }

    enum ManualLifecycleState {
        ACTIVE,
        ENABLED,
        DISABLED,
        SUSPENDED
    }

    enum PendingOnlyLifecycleState {
        PENDING
    }

    static class ManualAddressHolder {

        String city;
        String state;
        String postalCode;
        String country;
    }

    static class StreetOnlyAddressHolder {

        String streetAddress;
        String country;
    }

    static class ManualMoneyHolder {

        String currencyCode;
        BigDecimal price;
        BigDecimal amount;
        BigDecimal balance;
    }

    static class ManualStringMoneyHolder {

        String currencyCode;
        String price;
        String amount;
        String balance;
    }

    static class ManualCompanyContactHolder {

        String companyName;
        String companyEmail;
        String companyUrl;
    }

    static class ManualCompanyFallbackEmailHolder {

        String fullName;
        String companyName;
        String email;
    }

    private static ObjectGeneratorConfig defaultObjectConfig() {
        return ObjectGeneratorConfig.builder().build();
    }

    static class UnsupportedMoneyTarget {

        LocalDate value;
    }
}
