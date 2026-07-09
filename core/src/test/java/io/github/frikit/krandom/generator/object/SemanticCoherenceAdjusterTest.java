/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.Size;
import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.locale.LocaleDataBundle;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SemanticCoherenceAdjuster")
class SemanticCoherenceAdjusterTest {

    @Test
    @DisplayName("utility methods normalize strings and URLs")
    void utilityMethodsNormalizeStringsAndUrls() throws Exception {
        assertNull(invokeStatic("normalizeDomain", new Class<?>[] { String.class }, (Object) null));
        assertEquals("example.com", invokeStatic("normalizeDomain", new Class<?>[] { String.class }, " WWW.Example.Com "));
        assertNull(invokeStatic("normalizeDomain", new Class<?>[] { String.class }, " www. "));
        assertNull(invokeStatic("emailDomain", new Class<?>[] { String.class }, (Object) null));
        assertNull(invokeStatic("emailDomain", new Class<?>[] { String.class }, "missing-at"));
        assertNull(invokeStatic("emailDomain", new Class<?>[] { String.class }, "user@"));
        assertEquals("example.com", invokeStatic("emailDomain", new Class<?>[] { String.class }, "user@example.com"));
        assertNull(invokeStatic("urlHost", new Class<?>[] { String.class }, (Object) null));
        assertEquals("www.example.com", invokeStatic("urlHost", new Class<?>[] { String.class }, "https://www.example.com/path"));
        assertNull(invokeStatic("urlHost", new Class<?>[] { String.class }, "not a uri"));
        assertNull(invokeStatic("urlHost", new Class<?>[] { String.class }, "mailto:test@example.com"));
        assertNull(invokeStatic("slugFragment", new Class<?>[] { String.class }, (Object) null));
        assertEquals("alicesmith", invokeStatic("slugFragment", new Class<?>[] { String.class }, "Alice Smith"));
        assertNull(invokeStatic("slugFragment", new Class<?>[] { String.class }, "ÄÖÜ"));
        assertEquals("alice.smith1@example.com",
                     invokeStatic("uniquifyString", new Class<?>[] { String.class, int.class }, "alice.smith@example.com", 1));
        assertEquals("fullName1", invokeStatic("uniquifyString", new Class<?>[] { String.class, int.class }, "fullName", 1));
    }

    @Test
    @DisplayName("utility methods derive local parts and convert temporal types")
    void utilityMethodsDeriveLocalPartsAndConvertTemporalTypes() throws Exception {
        ManualNameHolder nameHolder = new ManualNameHolder();
        nameHolder.firstName = "Alice";
        nameHolder.lastName = "Smith";
        assertEquals("alice.smith", invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                                 slotMap(nameHolder, "firstName", "lastName")));

        ManualNameHolder prince = new ManualNameHolder();
        prince.fullName = "Prince";
        assertEquals("prince", invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                            slotMap(prince, "fullName")));

        ManualNameHolder ada = new ManualNameHolder();
        ada.fullName = "Ada Lovelace";
        assertEquals("ada.lovelace", invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                                  slotMap(ada, "fullName")));

        ManualNameHolder invalidFirst = new ManualNameHolder();
        invalidFirst.firstName = "ÄÖÜ";
        invalidFirst.lastName = "Smith";
        assertNull(invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                slotMap(invalidFirst, "firstName", "lastName")));

        ManualNameHolder invalidLast = new ManualNameHolder();
        invalidLast.firstName = "Ada";
        invalidLast.lastName = "ÄÖÜ";
        assertNull(invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                slotMap(invalidLast, "firstName", "lastName")));

        ManualNameHolder invalidFull = new ManualNameHolder();
        invalidFull.fullName = "ÄÖÜ Smith";
        assertNull(invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                slotMap(invalidFull, "fullName")));

        ManualNameHolder invalidFullLast = new ManualNameHolder();
        invalidFullLast.fullName = "Ada ÄÖÜ";
        assertNull(invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class },
                                slotMap(invalidFullLast, "fullName")));

        assertNull(invokeStatic("emailLocalPart", new Class<?>[] { java.util.Map.class }, Map.of()));

        Instant instant = Instant.parse("2026-04-20T12:34:56Z");
        assertSame(instant, invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, Instant.class));
        assertEquals(LocalDate.of(2026, 4, 20),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, LocalDate.class));
        assertEquals(LocalDateTime.of(2026, 4, 20, 12, 34, 56),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, LocalDateTime.class));
        assertEquals(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, OffsetDateTime.class));
        assertEquals(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, ZonedDateTime.class));
        assertEquals(java.util.Date.from(instant),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, java.util.Date.class));
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 4, 20)),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, java.sql.Date.class));
        assertEquals(java.sql.Timestamp.from(instant),
                     invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class }, instant, java.sql.Timestamp.class));

        assertEquals(instant, invokeStatic("toInstant", new Class<?>[] { Object.class }, instant));
        assertEquals(Instant.parse("2026-04-20T00:00:00Z"),
                     invokeStatic("toInstant", new Class<?>[] { Object.class }, LocalDate.of(2026, 4, 20)));
        assertEquals(instant, invokeStatic("toInstant", new Class<?>[] { Object.class }, LocalDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, invokeStatic("toInstant", new Class<?>[] { Object.class }, OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, invokeStatic("toInstant", new Class<?>[] { Object.class }, ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)));
        assertEquals(instant, invokeStatic("toInstant", new Class<?>[] { Object.class }, java.util.Date.from(instant)));
        assertNull(invokeStatic("toInstant", new Class<?>[] { Object.class }, 123L));

        ObjectGenerationException exception = assertThrows(ObjectGenerationException.class,
                                                           () -> invokeStatic("fromInstant", new Class<?>[] { Instant.class, Class.class },
                                                                              instant, String.class));
        assertTrue(exception.getMessage().contains("Unsupported semantic timestamp type"));
    }

    @Test
    @DisplayName("age and status utility methods cover conversion and fallback branches")
    void ageAndStatusUtilityMethodsCoverConversionAndFallbackBranches() throws Exception {
        Instant instant = Instant.parse("2026-04-20T12:34:56Z");
        assertEquals(LocalDate.of(2026, 4, 20),
                     invokeStatic("toLocalDate", new Class<?>[] { Object.class }, LocalDate.of(2026, 4, 20)));
        assertEquals(LocalDate.of(2026, 4, 20),
                     invokeStatic("toLocalDate", new Class<?>[] { Object.class }, java.util.Date.from(instant)));
        assertNull(invokeStatic("toLocalDate", new Class<?>[] { Object.class }, "bad-date"));

        assertEquals(42, invokeStatic("toInteger", new Class<?>[] { Object.class }, 42));
        assertEquals(42, invokeStatic("toInteger", new Class<?>[] { Object.class }, 42L));
        assertEquals(42, invokeStatic("toInteger", new Class<?>[] { Object.class }, (short) 42));
        assertEquals(42, invokeStatic("toInteger", new Class<?>[] { Object.class }, " 42 "));
        assertNull(invokeStatic("toInteger", new Class<?>[] { Object.class }, Long.MIN_VALUE));
        assertNull(invokeStatic("toInteger", new Class<?>[] { Object.class }, Long.MAX_VALUE));
        assertNull(invokeStatic("toInteger", new Class<?>[] { Object.class }, "forty-two"));
        assertNull(invokeStatic("toInteger", new Class<?>[] { Object.class }, 42.0));

        assertEquals(Boolean.TRUE, invokeStatic("toBoolean", new Class<?>[] { Object.class }, Boolean.TRUE));
        assertNull(invokeStatic("toBoolean", new Class<?>[] { Object.class }, "true"));

        assertEquals(42, invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, int.class));
        assertEquals(42L, invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, long.class));
        assertEquals(42L, invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, Long.class));
        assertEquals((short) 42, invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, short.class));
        assertEquals((short) 42, invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, Short.class));
        assertEquals("42", invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, String.class));

        ObjectGenerationException ageException =
            assertThrows(ObjectGenerationException.class,
                         () -> invokeStatic("fromAge", new Class<?>[] { int.class, Class.class }, 42, Double.class));
        assertTrue(ageException.getMessage().contains("Unsupported semantic age type"));

        assertNull(invokeStatic("activeFromStatus", new Class<?>[] { Object.class }, (Object) null));
        assertEquals(Boolean.TRUE, invokeStatic("activeFromStatus", new Class<?>[] { Object.class }, "ENABLED"));
        assertEquals(Boolean.FALSE, invokeStatic("activeFromStatus", new Class<?>[] { Object.class }, "archived"));
        assertNull(invokeStatic("activeFromStatus", new Class<?>[] { Object.class }, "pending"));

        assertEquals("ACTIVE", invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, true, String.class));
        assertEquals("INACTIVE", invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, false, String.class));
        assertEquals(ManualLifecycleState.ACTIVE,
                     invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, true,
                                  ManualLifecycleState.class));
        assertEquals(ManualLifecycleState.DISABLED,
                     invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, false,
                                  ManualLifecycleState.class));
        assertNull(invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, true, Integer.class));
        assertNull(invokeStatic("statusValueFor", new Class<?>[] { boolean.class, Class.class }, true,
                                PendingOnlyLifecycleState.class));
    }

    @Test
    @DisplayName("money and currency utility methods cover parsing and formatting branches")
    void moneyAndCurrencyUtilityMethodsCoverParsingAndFormattingBranches() throws Exception {
        assertEquals(new BigDecimal("12.35"),
                     invokeStatic("moneyValue", new Class<?>[] { Object.class }, "USD 12.345"));
        assertEquals(new BigDecimal("42.00"),
                     invokeStatic("moneyValue", new Class<?>[] { Object.class }, 42));
        assertEquals(new BigDecimal("42.00"),
                     invokeStatic("moneyValue", new Class<?>[] { Object.class }, BigInteger.valueOf(42)));
        assertEquals(new BigDecimal("7.50"),
                     invokeStatic("moneyValue", new Class<?>[] { Object.class }, 7.5d));
        assertNull(invokeStatic("moneyValue", new Class<?>[] { Object.class }, "not-money"));
        assertNull(invokeStatic("moneyValue", new Class<?>[] { Object.class }, LocalDate.now()));

        assertEquals("USD",
                     invokeStatic("currencyCode", new Class<?>[] { Object.class },
                                  io.github.frikit.krandom.generator.finance.Currency.USD));
        assertEquals("EUR",
                     invokeStatic("currencyCode", new Class<?>[] { Object.class }, java.util.Currency.getInstance("EUR")));
        assertEquals("SAR", invokeStatic("currencyCode", new Class<?>[] { Object.class }, "sar"));
        assertEquals("BHD", invokeStatic("currencyCode", new Class<?>[] { Object.class }, "bhd"));
        assertNull(invokeStatic("currencyCode", new Class<?>[] { Object.class }, 123));
        assertNull(invokeStatic("currencyCode", new Class<?>[] { Object.class }, "   "));
        assertNull(invokeStatic("currencyCode", new Class<?>[] { Object.class }, "not-a-currency"));

        assertEquals("USD 12.35",
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), String.class, "USD"));
        assertEquals("12.35",
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), String.class, null));
        assertEquals(new BigDecimal("12.35"),
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), BigDecimal.class, "USD"));
        assertEquals(BigInteger.valueOf(12),
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), BigInteger.class, "USD"));
        assertEquals((byte) 12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), byte.class, "USD"));
        assertEquals((byte) 12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Byte.class, "USD"));
        assertEquals((short) 12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Short.class, "USD"));
        assertEquals((short) 12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), short.class, "USD"));
        assertEquals(12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Integer.class, "USD"));
        assertEquals(12,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), int.class, "USD"));
        assertEquals(12L,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Long.class, "USD"));
        assertEquals(12L,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), long.class, "USD"));
        assertEquals(12.35f,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Float.class, "USD"));
        assertEquals(12.35f,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), float.class, "USD"));
        assertEquals(12.35d,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), Double.class, "USD"));
        assertEquals(12.35d,
                     invokeStatic("moneyValueFor",
                                  new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                  new BigDecimal("12.345"), double.class, "USD"));
        assertNull(invokeStatic("moneyValueFor",
                                new Class<?>[] { BigDecimal.class, Class.class, String.class },
                                new BigDecimal("12.345"), LocalDate.class, "USD"));

        Class<?> slotType = Class.forName("io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$Slot");
        assertEquals(new BigDecimal("1.00"),
                     invokeStatic("assignMoney",
                                  new Class<?>[] { slotType, BigDecimal.class, String.class },
                                  null, new BigDecimal("1.00"), "USD"));

        ManualMoneyHolder nullMoneyHolder = new ManualMoneyHolder();
        Object priceSlot = rawSlot(nullMoneyHolder, "price");
        assertNull(invokeStatic("assignMoney",
                                new Class<?>[] { slotType, BigDecimal.class, String.class },
                                priceSlot, null, "USD"));

        UnsupportedMoneyTarget unsupportedMoneyTarget = new UnsupportedMoneyTarget();
        Object unsupportedSlot = rawSlot(unsupportedMoneyTarget, "value");
        assertEquals(new BigDecimal("2.00"),
                     invokeStatic("assignMoney",
                                  new Class<?>[] { slotType, BigDecimal.class, String.class },
                                  unsupportedSlot, new BigDecimal("2.00"), "USD"));
        assertNull(unsupportedMoneyTarget.value);

        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        ManualStringMoneyHolder stringHolder = new ManualStringMoneyHolder();
        Object stringPriceSlot = rawSlot(stringHolder, "price");
        assertFalse((Boolean) invokeInstance(adjuster, "shouldFormatMoneyString",
                                             new Class<?>[] { BigDecimal.class, slotType, boolean.class },
                                             new BigDecimal("1.00"), null, true));
        assertFalse((Boolean) invokeInstance(adjuster, "shouldFormatMoneyString",
                                             new Class<?>[] { BigDecimal.class, slotType, boolean.class },
                                             null, stringPriceSlot, true));
        assertFalse((Boolean) invokeInstance(adjuster, "shouldFormatMoneyString",
                                             new Class<?>[] { BigDecimal.class, slotType, boolean.class },
                                             new BigDecimal("1.00"), rawSlot(new ManualMoneyHolder(), "price"), true));

        assertFalse((Boolean) invokeStatic("isLessThan",
                                           new Class<?>[] { BigDecimal.class, BigDecimal.class },
                                           new BigDecimal("1.00"), null));
    }

    @Test
    @DisplayName("structural-only mode leaves class instances and record args untouched")
    void structuralOnlyModeLeavesClassInstancesAndRecordArgsUntouched() throws Exception {
        SemanticCoherenceAdjuster adjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
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
        Object[] args = { record.firstName(), record.lastName(), record.fullName() };
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
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualCoherenceHolder company = new ManualCoherenceHolder();
        company.companyName = "Acme Labs";
        adjuster.adjustInstance(ManualCoherenceHolder.class, company, declaredFields(ManualCoherenceHolder.class), true);
        assertEquals("acmelabs.com", company.domain);
        assertEquals("https://www.acmelabs.com", company.url);
        assertEquals("hello@acmelabs.com", company.companyEmail);
        assertEquals("https://www.acmelabs.com", company.companyUrl);

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
        fallbackAdjuster.adjustInstance(ManualEmailHolder.class, fallback, declaredFields(ManualEmailHolder.class), true);
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
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(),
                                                                           new UniqueFieldTracker(),
                                                                           generationSeed);
        AddressInfo expectedAddress = new AddressInfoGenerator(
            GeneratorConfig.builder().seed(generationSeed).build()).generate();

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
        adjuster.adjustInstance(ManualStringMoneyHolder.class, stringMoney, declaredFields(ManualStringMoneyHolder.class), true);
        assertEquals("USD 7.50", stringMoney.price);
        assertEquals("USD 7.50", stringMoney.amount);
        assertEquals("USD 7.50", stringMoney.balance);
    }

    @Test
    @DisplayName("company contact fallback paths derive from self, companion fields, and company fallback inputs")
    void companyContactFallbackPathsDeriveFromSelfCompanionFieldsAndCompanyFallbackInputs() throws Exception {
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(),
                                                                           new UniqueFieldTracker());

        ManualCompanyContactHolder fromOwnEmail = new ManualCompanyContactHolder();
        fromOwnEmail.companyEmail = "legacy@widgets.test";
        adjuster.adjustInstance(ManualCompanyContactHolder.class, fromOwnEmail,
                                declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("hello@widgets.test", fromOwnEmail.companyEmail);

        ManualCompanyContactHolder fromCompanyUrl = new ManualCompanyContactHolder();
        fromCompanyUrl.companyUrl = "https://www.widgets.test/path";
        adjuster.adjustInstance(ManualCompanyContactHolder.class, fromCompanyUrl,
                                declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("hello@widgets.test", fromCompanyUrl.companyEmail);
        assertEquals("https://www.widgets.test", fromCompanyUrl.companyUrl);

        ManualCompanyContactHolder fromOwnUrl = new ManualCompanyContactHolder();
        fromOwnUrl.companyUrl = "https://www.widgets.test/old";
        adjuster.adjustInstance(ManualCompanyContactHolder.class, fromOwnUrl,
                                declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("https://www.widgets.test", fromOwnUrl.companyUrl);

        ManualCompanyContactHolder fromCompanyEmail = new ManualCompanyContactHolder();
        fromCompanyEmail.companyEmail = "hello@widgets.test";
        adjuster.adjustInstance(ManualCompanyContactHolder.class, fromCompanyEmail,
                                declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("https://www.widgets.test", fromCompanyEmail.companyUrl);

        ManualCompanyContactHolder unresolved = new ManualCompanyContactHolder();
        unresolved.companyName = "株式会社";
        unresolved.companyEmail = "";
        unresolved.companyUrl = "";
        adjuster.adjustInstance(ManualCompanyContactHolder.class, unresolved,
                                declaredFields(ManualCompanyContactHolder.class), true);
        assertEquals("", unresolved.companyEmail);
        assertEquals("", unresolved.companyUrl);
    }

    @Test
    @DisplayName("generic email falls back to company name when full-name slugs are unavailable")
    void genericEmailFallsBackToCompanyNameWhenFullNameSlugsAreUnavailable() throws Exception {
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(),
                                                                           new UniqueFieldTracker());

        ManualCompanyFallbackEmailHolder singleWord = new ManualCompanyFallbackEmailHolder();
        singleWord.fullName = "株式会社";
        singleWord.companyName = "Acme Labs";
        adjuster.adjustInstance(ManualCompanyFallbackEmailHolder.class, singleWord,
                                declaredFields(ManualCompanyFallbackEmailHolder.class), true);
        assertEquals("hello@acmelabs.com", singleWord.email);

        ManualCompanyFallbackEmailHolder multiWord = new ManualCompanyFallbackEmailHolder();
        multiWord.fullName = "株式会社 株式会社";
        multiWord.companyName = "Widgets Co";
        adjuster.adjustInstance(ManualCompanyFallbackEmailHolder.class, multiWord,
                                declaredFields(ManualCompanyFallbackEmailHolder.class), true);
        assertEquals("hello@widgetsco.com", multiWord.email);
    }

    @Test
    @DisplayName("manual money holders backfill missing amount from price")
    void manualMoneyHoldersBackfillMissingAmountFromPrice() throws Exception {
        SemanticCoherenceAdjuster adjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(),
                                                                           new UniqueFieldTracker());

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

        adjuster.adjustInstance(ManualCompanyContactHolder.class, holder,
                                declaredFields(ManualCompanyContactHolder.class), true);

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
        SemanticCoherenceAdjuster defaultAdjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(),
                                                                                  new UniqueFieldTracker());

        ManualAddressHolder noAddressSignals = new ManualAddressHolder();
        noAddressSignals.country = "France";
        defaultAdjuster.adjustInstance(ManualAddressHolder.class, noAddressSignals,
                                       declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", noAddressSignals.country);

        StreetOnlyAddressHolder streetOnly = new StreetOnlyAddressHolder();
        streetOnly.streetAddress = "1 Main Street";
        streetOnly.country = "France";
        defaultAdjuster.adjustInstance(StreetOnlyAddressHolder.class, streetOnly,
                                       declaredFields(StreetOnlyAddressHolder.class), true);
        assertEquals("United States", streetOnly.country);

        SemanticCoherenceAdjuster languageOnlyAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .generatorConfig(GeneratorConfig.builder()
                                                                                               .locale(Locale.ENGLISH)
                                                                                               .build())
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualAddressHolder languageOnly = new ManualAddressHolder();
        languageOnly.city = "London";
        languageOnly.country = "France";
        languageOnlyAdjuster.adjustInstance(ManualAddressHolder.class, languageOnly,
                                            declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", languageOnly.country);

        SemanticCoherenceAdjuster unregisteredLocaleAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .generatorConfig(GeneratorConfig.builder()
                                                                                               .locale(Locale.of("zz", "US"))
                                                                                               .build())
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualAddressHolder unregisteredLocale = new ManualAddressHolder();
        unregisteredLocale.city = "Nowhere";
        unregisteredLocale.country = "France";
        unregisteredLocaleAdjuster.adjustInstance(ManualAddressHolder.class, unregisteredLocale,
                                                  declaredFields(ManualAddressHolder.class), true);
        assertEquals("France", unregisteredLocale.country);

        ManualMoneyHolder derivedPrice = new ManualMoneyHolder();
        derivedPrice.amount = new BigDecimal("12.50");
        defaultAdjuster.adjustInstance(ManualMoneyHolder.class, derivedPrice, declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("12.50"), derivedPrice.price);
        assertEquals(new BigDecimal("12.50"), derivedPrice.amount);
        assertEquals(new BigDecimal("12.50"), derivedPrice.balance);

        SemanticCoherenceAdjuster protectedAmountAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualMoneyHolder.class, "amount",
                                                                         () -> new BigDecimal("50.00"))
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualMoneyHolder protectedAmount = new ManualMoneyHolder();
        protectedAmount.price = new BigDecimal("80.00");
        protectedAmount.amount = new BigDecimal("50.00");
        protectedAmountAdjuster.adjustInstance(ManualMoneyHolder.class, protectedAmount,
                                               declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("50.00"), protectedAmount.price);
        assertEquals(new BigDecimal("50.00"), protectedAmount.amount);

        SemanticCoherenceAdjuster protectedBalanceAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualMoneyHolder.class, "balance",
                                                                         () -> new BigDecimal("40.00"))
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualMoneyHolder protectedBalance = new ManualMoneyHolder();
        protectedBalance.amount = new BigDecimal("80.00");
        protectedBalance.balance = new BigDecimal("40.00");
        protectedBalanceAdjuster.adjustInstance(ManualMoneyHolder.class, protectedBalance,
                                                declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("40.00"), protectedBalance.amount);
        assertEquals(new BigDecimal("40.00"), protectedBalance.balance);

        SemanticCoherenceAdjuster protectedMissingAmountAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualMoneyHolder.class, "amount",
                                                                         () -> new BigDecimal("25.00"))
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualMoneyHolder protectedMissingAmount = new ManualMoneyHolder();
        protectedMissingAmount.price = new BigDecimal("25.00");
        protectedMissingAmountAdjuster.adjustInstance(ManualMoneyHolder.class, protectedMissingAmount,
                                                      declaredFields(ManualMoneyHolder.class), true);
        assertNull(protectedMissingAmount.amount);

        SemanticCoherenceAdjuster fullyProtectedPriceAmountAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualMoneyHolder.class, "price",
                                                                         () -> new BigDecimal("80.00"))
                                                               .override(ManualMoneyHolder.class, "amount",
                                                                         () -> new BigDecimal("50.00"))
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualMoneyHolder fullyProtectedPriceAmount = new ManualMoneyHolder();
        fullyProtectedPriceAmount.price = new BigDecimal("80.00");
        fullyProtectedPriceAmount.amount = new BigDecimal("50.00");
        fullyProtectedPriceAmountAdjuster.adjustInstance(ManualMoneyHolder.class, fullyProtectedPriceAmount,
                                                         declaredFields(ManualMoneyHolder.class), true);
        assertEquals(new BigDecimal("80.00"), fullyProtectedPriceAmount.price);
        assertEquals(new BigDecimal("50.00"), fullyProtectedPriceAmount.amount);

        SemanticCoherenceAdjuster fullyProtectedAmountBalanceAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualMoneyHolder.class, "amount",
                                                                         () -> new BigDecimal("80.00"))
                                                               .override(ManualMoneyHolder.class, "balance",
                                                                         () -> new BigDecimal("40.00"))
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualMoneyHolder fullyProtectedAmountBalance = new ManualMoneyHolder();
        fullyProtectedAmountBalance.amount = new BigDecimal("80.00");
        fullyProtectedAmountBalance.balance = new BigDecimal("40.00");
        fullyProtectedAmountBalanceAdjuster.adjustInstance(ManualMoneyHolder.class, fullyProtectedAmountBalance,
                                                           declaredFields(ManualMoneyHolder.class), true);
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
                                                                                             .streetAddress(new String[] { "Elm" },
                                                                                                            new String[] { "St" },
                                                                                                            new String[] { "Street" })
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
        SemanticCoherenceAdjuster defaultAdjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualNameHolder blankOnly = new ManualNameHolder();
        blankOnly.firstName = "Ada";
        blankOnly.lastName = "Lovelace";
        blankOnly.fullName = "   ";
        defaultAdjuster.adjustInstance(ManualNameHolder.class, blankOnly, declaredFields(ManualNameHolder.class), false);
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
        defaultAdjuster.adjustInstance(ManualNameHolder.class, protectedExisting, declaredFields(ManualNameHolder.class), false);
        assertEquals("Custom", protectedExisting.fullName);

        ManualNameHolder missingFirst = new ManualNameHolder();
        missingFirst.lastName = "Lovelace";
        defaultAdjuster.adjustInstance(ManualNameHolder.class, missingFirst, declaredFields(ManualNameHolder.class), true);
        assertNull(missingFirst.fullName);

        ManualNameHolder missingLast = new ManualNameHolder();
        missingLast.firstName = "Ada";
        defaultAdjuster.adjustInstance(ManualNameHolder.class, missingLast, declaredFields(ManualNameHolder.class), true);
        assertNull(missingLast.fullName);

        ManualTemporalHolder temporal = new ManualTemporalHolder();
        temporal.createdAt = LocalDateTime.of(2026, 4, 20, 12, 0);
        temporal.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(ManualTemporalHolder.class, temporal, declaredFields(ManualTemporalHolder.class), false);
        assertEquals(LocalDateTime.of(2026, 4, 20, 12, 0), temporal.createdAt);
        assertEquals(Instant.parse("2026-04-20T10:00:00Z"), temporal.updatedAt);

        ManualTemporalHolder missingCreated = new ManualTemporalHolder();
        missingCreated.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(ManualTemporalHolder.class, missingCreated, declaredFields(ManualTemporalHolder.class), true);
        assertNull(missingCreated.createdAt);

        ManualTemporalHolder missingUpdated = new ManualTemporalHolder();
        missingUpdated.createdAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        defaultAdjuster.adjustInstance(ManualTemporalHolder.class, missingUpdated, declaredFields(ManualTemporalHolder.class), true);
        assertNull(missingUpdated.updatedAt);

        ManualTemporalHolder alreadyOrdered = new ManualTemporalHolder();
        alreadyOrdered.createdAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        alreadyOrdered.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        defaultAdjuster.adjustInstance(ManualTemporalHolder.class, alreadyOrdered, declaredFields(ManualTemporalHolder.class), true);
        assertEquals(LocalDateTime.of(2026, 4, 20, 9, 0), alreadyOrdered.createdAt);

        SemanticCoherenceAdjuster fieldOverrideAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualNameHolder.class, "fullName", () -> "fixed")
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualNameHolder fieldOverride = new ManualNameHolder();
        fieldOverride.firstName = "Ada";
        fieldOverride.lastName = "Lovelace";
        fieldOverrideAdjuster.adjustInstance(ManualNameHolder.class, fieldOverride, declaredFields(ManualNameHolder.class), true);
        assertNull(fieldOverride.fullName);

        SemanticCoherenceAdjuster typeOverrideAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().override(String.class, () -> "fixed").build(),
                                          new UniqueFieldTracker());
        ManualEmailHolder typeOverride = new ManualEmailHolder();
        typeOverride.firstName = "Ada";
        typeOverride.lastName = "Lovelace";
        typeOverrideAdjuster.adjustInstance(ManualEmailHolder.class, typeOverride, declaredFields(ManualEmailHolder.class), true);
        assertNull(typeOverride.email);

        SemanticCoherenceAdjuster contextualTypeOverrideAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(String.class, ctx -> "fixed")
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualEmailHolder contextualTypeOverride = new ManualEmailHolder();
        contextualTypeOverride.firstName = "Ada";
        contextualTypeOverride.lastName = "Lovelace";
        contextualTypeOverrideAdjuster.adjustInstance(ManualEmailHolder.class, contextualTypeOverride,
                                                      declaredFields(ManualEmailHolder.class), true);
        assertNull(contextualTypeOverride.email);

        SemanticCoherenceAdjuster predicateOverrideAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(FieldPredicates.named("fullName"), () -> "fixed")
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualNameHolder predicateOverride = new ManualNameHolder();
        predicateOverride.firstName = "Ada";
        predicateOverride.lastName = "Lovelace";
        predicateOverride.fullName = "RAW";
        predicateOverrideAdjuster.adjustInstance(ManualNameHolder.class, predicateOverride,
                                                 declaredFields(ManualNameHolder.class), true);
        assertEquals("RAW", predicateOverride.fullName);

        UrlOnlyHolder urlOnly = new UrlOnlyHolder();
        defaultAdjuster.adjustInstance(UrlOnlyHolder.class, urlOnly, declaredFields(UrlOnlyHolder.class), true);
        assertNull(urlOnly.url);
    }

    @Test
    @DisplayName("age and status coherence cover protected, missing, and unsupported fallback paths")
    void ageAndStatusCoherenceCoverProtectedMissingAndUnsupportedFallbackPaths() throws Exception {
        SemanticCoherenceAdjuster defaultAdjuster = new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());

        ManualAgeHolder derivedAge = new ManualAgeHolder();
        derivedAge.birthDate = LocalDate.now().minusYears(33);
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, derivedAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(33, derivedAge.age);

        ManualAgeHolder noOverwriteConflict = new ManualAgeHolder();
        noOverwriteConflict.birthDate = LocalDate.now().minusYears(29);
        noOverwriteConflict.age = 17;
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, noOverwriteConflict, declaredFields(ManualAgeHolder.class), false);
        assertEquals(17, noOverwriteConflict.age);
        assertEquals(LocalDate.now().minusYears(29), noOverwriteConflict.birthDate);

        SemanticCoherenceAdjuster protectedNullAgeAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().override(ManualAgeHolder.class, "age", () -> 41).build(),
                                          new UniqueFieldTracker());
        ManualAgeHolder protectedNullAge = new ManualAgeHolder();
        protectedNullAge.birthDate = LocalDate.now().minusYears(31);
        protectedNullAgeAdjuster.adjustInstance(ManualAgeHolder.class, protectedNullAge, declaredFields(ManualAgeHolder.class), true);
        assertNull(protectedNullAge.age);
        assertEquals(LocalDate.now().minusYears(31), protectedNullAge.birthDate);

        ManualAgeHolder matchingAge = new ManualAgeHolder();
        matchingAge.birthDate = LocalDate.now().minusYears(29);
        matchingAge.age = 29;
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, matchingAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(29, matchingAge.age);

        SemanticCoherenceAdjuster protectedAgeAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().override(ManualAgeHolder.class, "age", () -> 42).build(),
                                          new UniqueFieldTracker());
        ManualAgeHolder protectedAge = new ManualAgeHolder();
        protectedAge.birthDate = LocalDate.now().minusYears(20);
        protectedAge.age = 42;
        protectedAgeAdjuster.adjustInstance(ManualAgeHolder.class, protectedAge, declaredFields(ManualAgeHolder.class), true);
        assertEquals(42, protectedAge.age);
        assertEquals(LocalDate.now().minusYears(42), protectedAge.birthDate);

        ManualAgeHolder missingBirthDate = new ManualAgeHolder();
        missingBirthDate.age = 27;
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, missingBirthDate, declaredFields(ManualAgeHolder.class), true);
        assertEquals(LocalDate.now().minusYears(27), missingBirthDate.birthDate);

        SemanticCoherenceAdjuster protectedBirthDateAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualAgeHolder.class, "birthDate", () -> LocalDate.EPOCH)
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualAgeHolder protectedBirthDate = new ManualAgeHolder();
        protectedBirthDate.age = 36;
        protectedBirthDateAdjuster.adjustInstance(ManualAgeHolder.class, protectedBirthDate,
                                                  declaredFields(ManualAgeHolder.class), true);
        assertNull(protectedBirthDate.birthDate);
        assertEquals(36, protectedBirthDate.age);

        ManualAgeHolder missingBoth = new ManualAgeHolder();
        defaultAdjuster.adjustInstance(ManualAgeHolder.class, missingBoth, declaredFields(ManualAgeHolder.class), true);
        assertNull(missingBoth.birthDate);
        assertNull(missingBoth.age);

        ManualStatusHolder alignedStatus = new ManualStatusHolder();
        alignedStatus.active = true;
        alignedStatus.status = "ENABLED";
        defaultAdjuster.adjustInstance(ManualStatusHolder.class, alignedStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("ENABLED", alignedStatus.status);

        ManualStatusHolder rewrittenStatus = new ManualStatusHolder();
        rewrittenStatus.active = true;
        rewrittenStatus.status = "SUSPENDED";
        defaultAdjuster.adjustInstance(ManualStatusHolder.class, rewrittenStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("ACTIVE", rewrittenStatus.status);

        SemanticCoherenceAdjuster protectedStatusAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().override(ManualStatusHolder.class, "status", () -> "fixed").build(),
                                          new UniqueFieldTracker());
        ManualStatusHolder protectedStatus = new ManualStatusHolder();
        protectedStatus.active = true;
        protectedStatus.status = "SUSPENDED";
        protectedStatusAdjuster.adjustInstance(ManualStatusHolder.class, protectedStatus, declaredFields(ManualStatusHolder.class), true);
        assertEquals("SUSPENDED", protectedStatus.status);
        assertFalse(protectedStatus.active);

        ManualStatusHolder protectedStatusWithoutOverwrite = new ManualStatusHolder();
        protectedStatusWithoutOverwrite.active = true;
        protectedStatusWithoutOverwrite.status = "SUSPENDED";
        protectedStatusAdjuster.adjustInstance(ManualStatusHolder.class, protectedStatusWithoutOverwrite,
                                              declaredFields(ManualStatusHolder.class), false);
        assertTrue(protectedStatusWithoutOverwrite.active);
        assertEquals("SUSPENDED", protectedStatusWithoutOverwrite.status);

        ManualNullableStatusHolder derivedActive = new ManualNullableStatusHolder();
        derivedActive.status = "ACTIVE";
        defaultAdjuster.adjustInstance(ManualNullableStatusHolder.class, derivedActive, declaredFields(ManualNullableStatusHolder.class), true);
        assertEquals(Boolean.TRUE, derivedActive.active);

        ManualNullableStatusHolder unknownStatus = new ManualNullableStatusHolder();
        unknownStatus.status = "PENDING";
        defaultAdjuster.adjustInstance(ManualNullableStatusHolder.class, unknownStatus, declaredFields(ManualNullableStatusHolder.class), true);
        assertNull(unknownStatus.active);

        SemanticCoherenceAdjuster protectedActiveAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().override(ManualNullableStatusHolder.class, "active", () -> Boolean.FALSE)
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualNullableStatusHolder protectedActive = new ManualNullableStatusHolder();
        protectedActive.status = "ACTIVE";
        protectedActiveAdjuster.adjustInstance(ManualNullableStatusHolder.class, protectedActive,
                                               declaredFields(ManualNullableStatusHolder.class), true);
        assertNull(protectedActive.active);

        ManualEnumStatusHolder enumStatus = new ManualEnumStatusHolder();
        enumStatus.active = false;
        defaultAdjuster.adjustInstance(ManualEnumStatusHolder.class, enumStatus, declaredFields(ManualEnumStatusHolder.class), true);
        assertEquals(ManualLifecycleState.DISABLED, enumStatus.status);

        ManualOpaqueStatusHolder opaqueStatus = new ManualOpaqueStatusHolder();
        opaqueStatus.active = true;
        opaqueStatus.status = 7;
        defaultAdjuster.adjustInstance(ManualOpaqueStatusHolder.class, opaqueStatus, declaredFields(ManualOpaqueStatusHolder.class), true);
        assertEquals(7, opaqueStatus.status);
        assertTrue(opaqueStatus.active);
    }

    @Test
    @DisplayName("relaxed mode protects annotated and validated fields while strict mode does not")
    void relaxedModeProtectsAnnotatedAndValidatedFieldsWhileStrictModeDoesNot() throws Exception {
        ProtectedSemanticHolder relaxed = new ProtectedSemanticHolder();
        relaxed.firstName = "Ada";
        relaxed.lastName = "Lovelace";
        relaxed.fullName = "RAW";
        relaxed.email = "ANNOTATED";

        SemanticCoherenceAdjuster relaxedAdjuster =
            new SemanticCoherenceAdjuster(defaultObjectConfig(), new UniqueFieldTracker());
        relaxedAdjuster.adjustInstance(ProtectedSemanticHolder.class, relaxed, declaredFields(ProtectedSemanticHolder.class), true);
        assertEquals("RAW", relaxed.fullName);
        assertEquals("ANNOTATED", relaxed.email);

        ProtectedSemanticHolder strict = new ProtectedSemanticHolder();
        strict.firstName = "Ada";
        strict.lastName = "Lovelace";
        strict.fullName = "RAW";
        strict.email = "ANNOTATED";

        SemanticCoherenceAdjuster strictAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .semanticMode(ObjectGenerationSemanticMode.STRICT)
                                                               .build(),
                                          new UniqueFieldTracker());
        strictAdjuster.adjustInstance(ProtectedSemanticHolder.class, strict, declaredFields(ProtectedSemanticHolder.class), true);
        assertEquals("Ada Lovelace", strict.fullName);
        assertEquals("ada.lovelace@example.com", strict.email);
    }

    @Test
    @DisplayName("timestamp fallback and uniqueness branches are exercised")
    void timestampFallbackAndUniquenessBranchesAreExercised() throws Exception {
        SemanticCoherenceAdjuster updatedProtectedAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder()
                                                               .override(ManualTemporalHolder.class, "updatedAt", () -> Instant.EPOCH)
                                                               .build(),
                                          new UniqueFieldTracker());
        ManualTemporalHolder temporal = new ManualTemporalHolder();
        temporal.createdAt = LocalDateTime.of(2026, 4, 20, 12, 0);
        temporal.updatedAt = Instant.parse("2026-04-20T10:00:00Z");
        updatedProtectedAdjuster.adjustInstance(ManualTemporalHolder.class, temporal, declaredFields(ManualTemporalHolder.class), true);
        assertEquals(LocalDateTime.ofInstant(temporal.updatedAt, ZoneOffset.UTC), temporal.createdAt);

        SemanticCoherenceAdjuster emailAliasAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().uniqueFields("emailaddress").build(),
                                          new UniqueFieldTracker());
        EmailAliasHolder firstAlias = new EmailAliasHolder();
        firstAlias.firstName = "Ada";
        firstAlias.lastName = "Lovelace";
        emailAliasAdjuster.adjustInstance(EmailAliasHolder.class, firstAlias, declaredFields(EmailAliasHolder.class), true);
        EmailAliasHolder secondAlias = new EmailAliasHolder();
        secondAlias.firstName = "Ada";
        secondAlias.lastName = "Lovelace";
        emailAliasAdjuster.adjustInstance(EmailAliasHolder.class, secondAlias, declaredFields(EmailAliasHolder.class), true);
        assertEquals("ada.lovelace@example.com", firstAlias.emailAddress);
        assertEquals("ada.lovelace1@example.com", secondAlias.emailAddress);

        SemanticCoherenceAdjuster uniqueFullNameAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().uniqueFields("fullname").build(),
                                          new UniqueFieldTracker());
        ManualNameHolder firstName = new ManualNameHolder();
        firstName.firstName = "Ada";
        firstName.lastName = "Lovelace";
        uniqueFullNameAdjuster.adjustInstance(ManualNameHolder.class, firstName, declaredFields(ManualNameHolder.class), true);
        ManualNameHolder secondName = new ManualNameHolder();
        secondName.firstName = "Ada";
        secondName.lastName = "Lovelace";
        uniqueFullNameAdjuster.adjustInstance(ManualNameHolder.class, secondName, declaredFields(ManualNameHolder.class), true);
        assertEquals("Ada Lovelace", firstName.fullName);
        assertEquals("Ada Lovelace1", secondName.fullName);
    }

    @Test
    @DisplayName("internal uniqueness helpers cover null and alias branches")
    void internalUniquenessHelpersCoverNullAndAliasBranches() throws Exception {
        SemanticCoherenceAdjuster aliasAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().uniqueFields("emailaddress").build(),
                                          new UniqueFieldTracker());
        assertTrue((Boolean) invokeInstance(aliasAdjuster, "isUniqueField",
                                            new Class<?>[] { String.class, String.class }, "email", "email"));

        SemanticCoherenceAdjuster semanticAdjuster =
            new SemanticCoherenceAdjuster(ObjectGeneratorConfig.builder().uniqueFields("email").build(),
                                          new UniqueFieldTracker());
        assertTrue((Boolean) invokeInstance(semanticAdjuster, "isUniqueField",
                                            new Class<?>[] { String.class, String.class }, "customField", "email"));

        Object slot = slotMap(new ManualEmailHolder(), "email").get("email");
        Class<?> slotType = Class.forName("io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$Slot");
        assertNull(invokeInstance(semanticAdjuster, "applyUniqueness",
                                  new Class<?>[] { slotType, String.class, String.class }, slot, "email", null));
    }

    @Test
    @DisplayName("reflection slots wrap read and write failures")
    void reflectionSlotsWrapReadAndWriteFailures() throws Exception {
        Constructor<?> constructor = Class.forName("io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$ReflectionSlot")
                                         .getDeclaredConstructor(
                                             Class.class, Field.class, Object.class, boolean.class, int.class);
        constructor.setAccessible(true);

        PrivateValueHolder privateHolder = new PrivateValueHolder();
        Field privateField = PrivateValueHolder.class.getDeclaredField("value");
        Object slot = constructor.newInstance(PrivateValueHolder.class, privateField, privateHolder, false, 3);

        Method getValue = slot.getClass().getDeclaredMethod("getValue");
        getValue.setAccessible(true);
        ObjectGenerationException readException =
            assertThrows(ObjectGenerationException.class, () -> invokeMethod(slot, getValue));
        GenerationFailureContext readContext = readException.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, readContext.category());
        assertEquals(GenerationOperation.READ, readContext.operation());
        assertEquals("PrivateValueHolder.value", readContext.path());
        assertEquals(String.class.getTypeName(), readContext.declaredType());
        assertEquals(3, readContext.depth());

        Object lenientReadSlot = constructor.newInstance(
            PrivateValueHolder.class, privateField, privateHolder, true, 3);
        assertNull(invokeMethod(lenientReadSlot, getValue));

        Field publicField = PublicValueHolder.class.getDeclaredField("value");
        Object strictSlot = constructor.newInstance(
            PublicValueHolder.class, publicField, new PublicValueHolder(), false, 4);
        Method setValue = strictSlot.getClass().getDeclaredMethod("setValue", Object.class);
        setValue.setAccessible(true);
        ObjectGenerationException writeException =
            assertThrows(ObjectGenerationException.class, () -> invokeMethod(strictSlot, setValue, 123));
        GenerationFailureContext writeContext = writeException.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.ASSIGNMENT, writeContext.category());
        assertEquals(GenerationOperation.ALIGN_SEMANTICS, writeContext.operation());
        assertEquals("PublicValueHolder.value", writeContext.path());
        assertEquals(String.class.getTypeName(), writeContext.declaredType());
        assertEquals(4, writeContext.depth());

        PublicValueHolder ignored = new PublicValueHolder();
        Object lenientSlot = constructor.newInstance(PublicValueHolder.class, publicField, ignored, true, 4);
        assertDoesNotThrow(() -> invokeMethod(lenientSlot, setValue, 123));
        assertEquals("ok", ignored.value);
    }

    @Test
    @DisplayName("lenient reflection diagnostics contain context but no field values")
    void lenientReflectionDiagnosticsAreSanitized() throws Exception {
        Constructor<?> constructor = Class.forName(
            "io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$ReflectionSlot")
                                              .getDeclaredConstructor(
                                                  Class.class, Field.class, Object.class, boolean.class, int.class);
        constructor.setAccessible(true);
        Field privateField = PrivateValueHolder.class.getDeclaredField("value");
        Object readSlot = constructor.newInstance(
            PrivateValueHolder.class, privateField, new PrivateValueHolder(), true, 3);
        Method getValue = readSlot.getClass().getDeclaredMethod("getValue");
        getValue.setAccessible(true);

        Field publicField = PublicValueHolder.class.getDeclaredField("value");
        Object writeSlot = constructor.newInstance(
            PublicValueHolder.class, publicField, new PublicValueHolder(), true, 4);
        Method setValue = writeSlot.getClass().getDeclaredMethod("setValue", Object.class);
        setValue.setAccessible(true);

        Logger logger = (Logger) LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            assertNull(invokeMethod(readSlot, getValue));
            assertDoesNotThrow(() -> invokeMethod(writeSlot, setValue, 123));

            List<String> messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
            assertTrue(messages.stream().anyMatch(message -> message.contains("PrivateValueHolder.value")));
            assertTrue(messages.stream().anyMatch(message -> message.contains("PublicValueHolder.value")));
            assertFalse(messages.stream().anyMatch(message -> message.contains("hidden")
                                                               || message.contains("ok")
                                                               || message.contains("123")));
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
        }
    }

    private static Object invokeStatic(String name, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = SemanticCoherenceAdjuster.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return invokeMethod(null, method, args);
    }

    private static Object invokeInstance(Object target, String name, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = SemanticCoherenceAdjuster.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return invokeMethod(target, method, args);
    }

    private static Object invokeMethod(Object target, Method method, Object... args) throws Exception {
        try {
            return method.invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw e;
        }
    }

    private static List<Field> declaredFields(Class<?> type) {
        return List.of(type.getDeclaredFields());
    }

    private static Map<String, Object> slotMap(Object instance, String... fieldNames) throws Exception {
        Map<String, Object> slots = new java.util.LinkedHashMap<>();
        for (String fieldName : fieldNames) {
            String semanticKey = FieldGeneratorResolver.semanticKeyForFieldName(fieldName);
            slots.put(semanticKey, rawSlot(instance, fieldName));
        }
        return slots;
    }

    private static Object rawSlot(Object instance, String fieldName) throws Exception {
        Constructor<?> constructor = Class.forName("io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$ReflectionSlot")
                                         .getDeclaredConstructor(Class.class, Field.class, Object.class, boolean.class);
        constructor.setAccessible(true);
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return constructor.newInstance(instance.getClass(), field, instance, false);
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

    static class ManualTemporalHolder {

        LocalDateTime createdAt;
        Instant       updatedAt;
    }

    static class ManualAgeHolder {

        LocalDate birthDate;
        Integer   age;
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
        String  status;
    }

    static class ManualNullableStatusHolder {

        Boolean active;
        String  status;
    }

    static class ManualEnumStatusHolder {

        boolean              active;
        ManualLifecycleState status;
    }

    static class ManualOpaqueStatusHolder {

        boolean active;
        Integer status;
    }

    static class ProtectedSemanticHolder {

        String firstName;
        String lastName;

        @Size(min = 3, max = 3)
        String fullName;

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    record ManualRecord(String firstName, String lastName, String fullName) {
    }

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

        String     currencyCode;
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
