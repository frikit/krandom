/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.algorithms.FibonacciGenerator;
import io.github.frikit.krandom.generator.algorithms.LuhnGenerator;
import io.github.frikit.krandom.generator.base.BooleanGenerator;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.CharGenerator;
import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.base.DoubleGenerator;
import io.github.frikit.krandom.generator.base.FloatGenerator;
import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.NaturalNumberGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.base.NullableBooleanGenerator;
import io.github.frikit.krandom.generator.base.NumberWithFormatGenerator;
import io.github.frikit.krandom.generator.base.PrimeGenerator;
import io.github.frikit.krandom.generator.base.PyDecimalGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.color.ColorGenerator;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.DurationGenerator;
import io.github.frikit.krandom.generator.datetime.InstantGenerator;
import io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.TimezoneGenerator;
import io.github.frikit.krandom.generator.datetime.ZonedDateTimeGenerator;
import io.github.frikit.krandom.generator.file.DirPathGenerator;
import io.github.frikit.krandom.generator.file.FileExtensionGenerator;
import io.github.frikit.krandom.generator.file.FileNameGenerator;
import io.github.frikit.krandom.generator.file.FilePathGenerator;
import io.github.frikit.krandom.generator.file.MimeTypeGenerator;
import io.github.frikit.krandom.generator.file.SemverGenerator;
import io.github.frikit.krandom.generator.finance.AbaRoutingGenerator;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.BankCountryGenerator;
import io.github.frikit.krandom.generator.finance.BankNameGenerator;
import io.github.frikit.krandom.generator.finance.BankTypeGenerator;
import io.github.frikit.krandom.generator.finance.BbanGenerator;
import io.github.frikit.krandom.generator.finance.BicGenerator;
import io.github.frikit.krandom.generator.finance.CardExpirationGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.krandom.generator.finance.PaymentInfoGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyPairGenerator;
import io.github.frikit.krandom.generator.finance.CusipGenerator;
import io.github.frikit.krandom.generator.finance.EinGenerator;
import io.github.frikit.krandom.generator.finance.IbanGenerator;
import io.github.frikit.krandom.generator.finance.IsinGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.games.coin.CoinGenerator;
import io.github.frikit.krandom.generator.games.dice.DiceGenerator;
import io.github.frikit.krandom.generator.games.dice.DiceType;
import io.github.frikit.krandom.generator.identifier.EanGenerator;
import io.github.frikit.krandom.generator.identifier.HashGenerator;
import io.github.frikit.krandom.generator.identifier.IdentifierMaskGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.identifier.UpcGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.HttpMethodGenerator;
import io.github.frikit.krandom.generator.network.HttpStatusCodeGenerator;
import io.github.frikit.krandom.generator.network.IPGenerator;
import io.github.frikit.krandom.generator.network.IPv4Generator;
import io.github.frikit.krandom.generator.network.IPv6Generator;
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.network.UriGenerator;
import io.github.frikit.krandom.generator.network.UserAgentGenerator;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.FieldLookup;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import io.github.frikit.krandom.generator.selection.PickGenerator;
import io.github.frikit.krandom.generator.selection.PickSetGenerator;
import io.github.frikit.krandom.generator.selection.RepeatGenerator;
import io.github.frikit.krandom.generator.selection.ShuffleGenerator;
import io.github.frikit.krandom.generator.selection.UniqueGenerator;
import io.github.frikit.krandom.generator.selection.WeightedGenerator;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.SyllableGenerator;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyEmailGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.user.MiddleNameGenerator;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.ProfessionGenerator;
import io.github.frikit.krandom.generator.user.ProfileGenerator;
import io.github.frikit.krandom.generator.user.SimpleProfileGenerator;
import io.github.frikit.krandom.generator.user.SocialHandleGenerator;
import io.github.frikit.krandom.generator.user.SocialProfileGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Generators factory")
class GeneratorsTest {

    // ── Byte ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofByte() returns ByteGenerator")
    void ofByte() {
        assertInstanceOf(ByteGenerator.class, Generators.ofByte());
    }

    @Test
    @DisplayName("ofByte(min, max) returns ByteGenerator")
    void ofByteRange() {
        assertInstanceOf(ByteGenerator.class, Generators.ofByte((byte) 0, (byte) 10));
    }

    @Test
    @DisplayName("ofByte(min, max, seed) returns ByteGenerator")
    void ofByteSeeded() {
        assertInstanceOf(ByteGenerator.class, Generators.ofByte((byte) 0, (byte) 10, 1L));
    }

    // ── Short ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofShort() returns ShortGenerator")
    void ofShort() {
        assertInstanceOf(ShortGenerator.class, Generators.ofShort());
    }

    @Test
    @DisplayName("ofShort(min, max) returns ShortGenerator")
    void ofShortRange() {
        assertInstanceOf(ShortGenerator.class, Generators.ofShort((short) 0, (short) 100));
    }

    @Test
    @DisplayName("ofShort(min, max, seed) returns ShortGenerator")
    void ofShortSeeded() {
        assertInstanceOf(ShortGenerator.class, Generators.ofShort((short) 0, (short) 100, 1L));
    }

    // ── Int ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofInt() returns IntGenerator")
    void ofInt() {
        assertInstanceOf(IntGenerator.class, Generators.ofInt());
    }

    @Test
    @DisplayName("ofInt(min, max) returns IntGenerator")
    void ofIntRange() {
        assertInstanceOf(IntGenerator.class, Generators.ofInt(0, 100));
    }

    @Test
    @DisplayName("ofInt(min, max, seed) returns IntGenerator")
    void ofIntSeeded() {
        assertInstanceOf(IntGenerator.class, Generators.ofInt(0, 100, 1L));
    }

    // ── Long ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofLong() returns LongGenerator")
    void ofLong() {
        assertInstanceOf(LongGenerator.class, Generators.ofLong());
    }

    @Test
    @DisplayName("ofLong(min, max) returns LongGenerator")
    void ofLongRange() {
        assertInstanceOf(LongGenerator.class, Generators.ofLong(0L, 100L));
    }

    @Test
    @DisplayName("ofLong(min, max, seed) returns LongGenerator")
    void ofLongSeeded() {
        assertInstanceOf(LongGenerator.class, Generators.ofLong(0L, 100L, 1L));
    }

    // ── Float ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofFloat() returns FloatGenerator")
    void ofFloat() {
        assertInstanceOf(FloatGenerator.class, Generators.ofFloat());
    }

    @Test
    @DisplayName("ofFloat(min, max) returns FloatGenerator")
    void ofFloatRange() {
        assertInstanceOf(FloatGenerator.class, Generators.ofFloat(0f, 1f));
    }

    @Test
    @DisplayName("ofFloat(min, max, seed) returns FloatGenerator")
    void ofFloatSeeded() {
        assertInstanceOf(FloatGenerator.class, Generators.ofFloat(0f, 1f, 1L));
    }

    // ── Double ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofDouble() returns DoubleGenerator")
    void ofDouble() {
        assertInstanceOf(DoubleGenerator.class, Generators.ofDouble());
    }

    @Test
    @DisplayName("ofDouble(min, max) returns DoubleGenerator")
    void ofDoubleRange() {
        assertInstanceOf(DoubleGenerator.class, Generators.ofDouble(0.0, 1.0));
    }

    @Test
    @DisplayName("ofDouble(min, max, seed) returns DoubleGenerator")
    void ofDoubleSeeded() {
        assertInstanceOf(DoubleGenerator.class, Generators.ofDouble(0.0, 1.0, 1L));
    }

    // ── Natural ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofNaturalNumber() returns NaturalNumberGenerator")
    void ofNaturalNumber() {
        assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber());
    }

    @Test
    @DisplayName("ofNaturalNumber(min, max) returns NaturalNumberGenerator")
    void ofNaturalNumberRange() {
        assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber(0, 100));
    }

    @Test
    @DisplayName("ofNaturalNumber(min, max, seed) returns NaturalNumberGenerator")
    void ofNaturalNumberSeeded() {
        assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber(0, 100, 1L));
    }

    // ── Normal Distribution ───────────────────────────────────────────────────

    @Test
    @DisplayName("ofNormal() returns NormalDistributionGenerator")
    void ofNormal() {
        assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal());
    }

    @Test
    @DisplayName("ofNormal(mean, stdDev) returns NormalDistributionGenerator")
    void ofNormalParams() {
        assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal(0.0, 1.0));
    }

    @Test
    @DisplayName("ofNormal(mean, stdDev, seed) returns NormalDistributionGenerator")
    void ofNormalSeeded() {
        assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal(0.0, 1.0, 1L));
    }

    // ── Prime ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofPrime() returns PrimeGenerator")
    void ofPrime() {
        assertInstanceOf(PrimeGenerator.class, Generators.ofPrime());
    }

    @Test
    @DisplayName("ofPrime(min, max) returns PrimeGenerator")
    void ofPrimeRange() {
        assertInstanceOf(PrimeGenerator.class, Generators.ofPrime(2, 100));
    }

    @Test
    @DisplayName("ofPrime(min, max, seed) returns PrimeGenerator")
    void ofPrimeSeeded() {
        assertInstanceOf(PrimeGenerator.class, Generators.ofPrime(2, 100, 1L));
    }

    // ── Char ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofChar() returns CharGenerator")
    void ofChar() {
        assertInstanceOf(CharGenerator.class, Generators.ofChar());
    }

    @Test
    @DisplayName("ofChar(builder) returns the same builder")
    void ofCharBuilder() {
        CharGenerator.Builder builder = CharGenerator.builder().uppercase();
        assertSame(builder, Generators.ofChar(builder));
    }

    // ── Boolean ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofBoolean() returns BooleanGenerator")
    void ofBoolean() {
        assertInstanceOf(BooleanGenerator.class, Generators.ofBoolean());
    }

    @Test
    @DisplayName("ofBoolean(seed) returns BooleanGenerator")
    void ofBooleanSeeded() {
        assertInstanceOf(BooleanGenerator.class, Generators.ofBoolean(1L));
    }

    // ── String ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofString() returns StringGenerator")
    void ofString() {
        assertInstanceOf(StringGenerator.class, Generators.ofString());
    }

    @Test
    @DisplayName("ofString(builder) returns StringGenerator")
    void ofStringBuilder() {
        assertInstanceOf(StringGenerator.class,
                         Generators.ofString(StringGenerator.builder().length(5)));
    }

    @Test
    @DisplayName("ofDigit() returns DigitGenerator")
    void ofDigit() {
        assertInstanceOf(DigitGenerator.class, Generators.ofDigit());
    }

    @Test
    @DisplayName("ofNumberWithFormat() returns NumberWithFormatGenerator")
    void ofNumberWithFormat() {
        assertInstanceOf(NumberWithFormatGenerator.class, Generators.ofNumberWithFormat());
    }

    @Test
    @DisplayName("ofNullableBoolean() returns NullableBooleanGenerator")
    void ofNullableBoolean() {
        assertInstanceOf(NullableBooleanGenerator.class, Generators.ofNullableBoolean());
    }

    @Test
    @DisplayName("ofPyDecimal() returns PyDecimalGenerator")
    void ofPyDecimal() {
        assertInstanceOf(PyDecimalGenerator.class, Generators.ofPyDecimal());
    }

    // ── Algorithms ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofFibonacci() returns FibonacciGenerator")
    void ofFibonacci() {
        assertInstanceOf(FibonacciGenerator.class, Generators.ofFibonacci());
    }

    @Test
    @DisplayName("ofLuhn() returns LuhnGenerator")
    void ofLuhn() {
        assertInstanceOf(LuhnGenerator.class, Generators.ofLuhn());
    }

    // ── Selection / helper-style generators ──────────────────────────────────

    @Test
    @DisplayName("pickFrom(source) returns PickGenerator")
    void pickFrom() {
        assertInstanceOf(PickGenerator.class, Generators.pickFrom(List.of("a", "b")));
    }

    @Test
    @DisplayName("pick(source) returns PickGenerator alias")
    void pickAlias() {
        assertInstanceOf(PickGenerator.class, Generators.pick(List.of("a", "b")));
    }

    @Test
    @DisplayName("pickSetFrom(source, count) returns PickSetGenerator")
    void pickSetFrom() {
        assertInstanceOf(PickSetGenerator.class, Generators.pickSetFrom(List.of(1, 2, 3), 2));
    }

    @Test
    @DisplayName("pickset(source, count) returns PickSetGenerator alias")
    void pickSetAlias() {
        assertInstanceOf(PickSetGenerator.class, Generators.pickset(List.of(1, 2, 3), 2));
    }

    @Test
    @DisplayName("shuffleOf(source) returns ShuffleGenerator")
    void shuffleOf() {
        assertInstanceOf(ShuffleGenerator.class, Generators.shuffleOf(List.of(1, 2, 3)));
    }

    @Test
    @DisplayName("shuffle(source) returns ShuffleGenerator alias")
    void shuffleAlias() {
        assertInstanceOf(ShuffleGenerator.class, Generators.shuffle(List.of(1, 2, 3)));
    }

    @Test
    @DisplayName("weighted(values, weights) returns WeightedGenerator")
    void weighted() {
        assertInstanceOf(WeightedGenerator.class, Generators.weighted(List.of("h", "t"), List.of(7, 3)));
    }

    @Test
    @DisplayName("unique(source) returns UniqueGenerator")
    void unique() {
        assertInstanceOf(UniqueGenerator.class, Generators.unique(() -> 1));
    }

    @Test
    @DisplayName("unique(source, maxAttempts) returns UniqueGenerator")
    void uniqueMaxAttempts() {
        assertInstanceOf(UniqueGenerator.class, Generators.unique(() -> 1, 5));
    }

    @Test
    @DisplayName("unique(source, comparator) returns UniqueGenerator")
    void uniqueWithComparator() {
        assertInstanceOf(UniqueGenerator.class, Generators.unique(() -> "a", String::equalsIgnoreCase));
    }

    @Test
    @DisplayName("repeat(source, count) returns RepeatGenerator")
    void repeat() {
        assertInstanceOf(RepeatGenerator.class, Generators.repeat(() -> 1, 3));
    }

    @Test
    @DisplayName("ofProfession() returns ProfessionGenerator")
    void ofProfession() {
        assertInstanceOf(ProfessionGenerator.class, Generators.ofProfession());
    }

    @Test
    @DisplayName("ofBic() returns BicGenerator")
    void ofBic() {
        assertInstanceOf(BicGenerator.class, Generators.ofBic());
    }

    @Test
    @DisplayName("ofIsin() returns IsinGenerator")
    void ofIsin() {
        assertInstanceOf(IsinGenerator.class, Generators.ofIsin());
    }

    @Test
    @DisplayName("ofWord() returns WordGenerator")
    void ofWord() {
        assertInstanceOf(WordGenerator.class, Generators.ofWord());
    }

    @Test
    @DisplayName("ofSyllable() returns SyllableGenerator")
    void ofSyllable() {
        assertInstanceOf(SyllableGenerator.class, Generators.ofSyllable());
    }

    @Test
    @DisplayName("ofSentence() returns SentenceGenerator")
    void ofSentence() {
        assertInstanceOf(SentenceGenerator.class, Generators.ofSentence());
    }

    @Test
    @DisplayName("ofParagraph() returns ParagraphGenerator")
    void ofParagraph() {
        assertInstanceOf(ParagraphGenerator.class, Generators.ofParagraph());
    }

    @Test
    @DisplayName("ofFileExtension() returns FileExtensionGenerator")
    void ofFileExtension() {
        assertInstanceOf(FileExtensionGenerator.class, Generators.ofFileExtension());
    }

    @Test
    @DisplayName("ofFileName() returns FileNameGenerator")
    void ofFileName() {
        assertInstanceOf(FileNameGenerator.class, Generators.ofFileName());
    }

    @Test
    @DisplayName("ofDirPath() returns DirPathGenerator")
    void ofDirPath() {
        assertInstanceOf(DirPathGenerator.class, Generators.ofDirPath());
    }

    @Test
    @DisplayName("ofFilePath() returns FilePathGenerator")
    void ofFilePath() {
        assertInstanceOf(FilePathGenerator.class, Generators.ofFilePath());
    }

    @Test
    @DisplayName("ofMimeType() returns MimeTypeGenerator")
    void ofMimeType() {
        assertInstanceOf(MimeTypeGenerator.class, Generators.ofMimeType());
    }

    @Test
    @DisplayName("ofSemver() returns SemverGenerator")
    void ofSemver() {
        assertInstanceOf(SemverGenerator.class, Generators.ofSemver());
    }

    @Test
    @DisplayName("ofColor() returns ColorGenerator")
    void ofColor() {
        assertInstanceOf(ColorGenerator.class, Generators.ofColor());
    }

    @Test
    @DisplayName("date/time factories return corresponding generators")
    void dateTimeFactories() {
        assertInstanceOf(DateGenerator.class, Generators.ofLocalDate());
        assertInstanceOf(LocalDateTimeGenerator.class, Generators.ofLocalDateTime());
        assertInstanceOf(InstantGenerator.class, Generators.ofInstant());
        assertInstanceOf(ZonedDateTimeGenerator.class, Generators.ofZonedDateTime());
        assertInstanceOf(DurationGenerator.class, Generators.ofDuration());
        assertInstanceOf(OffsetDateTimeGenerator.class, Generators.ofOffsetDateTime());
        assertInstanceOf(TimezoneGenerator.class, Generators.ofTimezone());
    }

    @Test
    @DisplayName("ofJobType() returns JobTypeGenerator")
    void ofJobType() {
        assertInstanceOf(JobTypeGenerator.class, Generators.ofJobType());
    }

    // ── Games ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofCoin() returns CoinGenerator")
    void ofCoin() {
        assertInstanceOf(CoinGenerator.class, Generators.ofCoin());
    }

    @Test
    @DisplayName("ofDice(D6) returns DiceGenerator")
    void ofDice() {
        assertInstanceOf(DiceGenerator.class, Generators.ofDice(DiceType.D6));
    }

    // ── Network ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ofIPv4() returns IPv4Generator")
    void ofIPv4() {
        assertInstanceOf(IPv4Generator.class, Generators.ofIPv4());
    }

    @Test
    @DisplayName("ofIPv6() returns IPv6Generator")
    void ofIPv6() {
        assertInstanceOf(IPv6Generator.class, Generators.ofIPv6());
    }

    @Test
    @DisplayName("ofIP() returns IPGenerator")
    void ofIP() {
        assertInstanceOf(IPGenerator.class, Generators.ofIP());
    }

    @Test
    @DisplayName("ofHttpStatusCode() returns HttpStatusCodeGenerator")
    void ofHttpStatusCode() {
        assertInstanceOf(HttpStatusCodeGenerator.class, Generators.ofHttpStatusCode());
    }

    @Test
    @DisplayName("ofHttpMethod() returns HttpMethodGenerator")
    void ofHttpMethod() {
        assertInstanceOf(HttpMethodGenerator.class, Generators.ofHttpMethod());
    }

    @Test
    @DisplayName("ofDomain() returns DomainGenerator")
    void ofDomain() {
        assertInstanceOf(DomainGenerator.class, Generators.ofDomain());
    }

    @Test
    @DisplayName("ofHostname() returns HostnameGenerator")
    void ofHostname() {
        assertInstanceOf(HostnameGenerator.class, Generators.ofHostname());
    }

    @Test
    @DisplayName("ofUrl() returns URLGenerator")
    void ofUrl() {
        assertInstanceOf(URLGenerator.class, Generators.ofUrl());
    }

    @Test
    @DisplayName("ofUri() returns UriGenerator")
    void ofUri() {
        assertInstanceOf(UriGenerator.class, Generators.ofUri());
    }

    @Test
    @DisplayName("ofUserAgent() returns UserAgentGenerator")
    void ofUserAgent() {
        assertInstanceOf(UserAgentGenerator.class, Generators.ofUserAgent());
    }

    @Test
    @DisplayName("ofEmail() returns EmailGenerator")
    void ofEmail() {
        assertInstanceOf(EmailGenerator.class, Generators.ofEmail());
    }

    @Test
    @DisplayName("locale and config overloads exist for common locale-aware facade factories")
    void localeAndConfigOverloadsForCommonFacadeFactories() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.GERMANY).seed(11L).build();

        assertInstanceOf(FullNameGenerator.class, Generators.ofFullName());
        assertInstanceOf(FullNameGenerator.class, Generators.ofFullName(Locale.GERMANY));
        assertInstanceOf(FullNameGenerator.class, Generators.ofFullName(config));
        assertInstanceOf(EmailGenerator.class, Generators.ofEmail(Locale.GERMANY));
        assertInstanceOf(EmailGenerator.class, Generators.ofEmail(config));
        assertInstanceOf(CompanyEmailGenerator.class, Generators.ofCompanyEmail(Locale.GERMANY));
        assertInstanceOf(CompanyEmailGenerator.class, Generators.ofCompanyEmail(config));
        assertInstanceOf(SimpleProfileGenerator.class, Generators.ofSimpleProfile(Locale.GERMANY));
        assertInstanceOf(SimpleProfileGenerator.class, Generators.ofSimpleProfile(config));
        assertInstanceOf(ProfileGenerator.class, Generators.ofProfile(Locale.GERMANY));
        assertInstanceOf(ProfileGenerator.class, Generators.ofProfile(config));
        assertInstanceOf(SocialHandleGenerator.class, Generators.ofSocialHandle(Locale.GERMANY));
        assertInstanceOf(SocialHandleGenerator.class, Generators.ofSocialHandle(config));
        assertInstanceOf(SocialProfileGenerator.class, Generators.ofSocialProfile(Locale.GERMANY));
        assertInstanceOf(SocialProfileGenerator.class, Generators.ofSocialProfile(config));
        assertInstanceOf(StreetAddressGenerator.class, Generators.ofStreetAddress(Locale.GERMANY));
        assertInstanceOf(StreetAddressGenerator.class, Generators.ofStreetAddress(config));
        assertInstanceOf(CityGenerator.class, Generators.ofCity(Locale.GERMANY));
        assertInstanceOf(CityGenerator.class, Generators.ofCity(config));
        assertInstanceOf(StateGenerator.class, Generators.ofState(Locale.GERMANY));
        assertInstanceOf(StateGenerator.class, Generators.ofState(config));
        assertInstanceOf(PostalCodeGenerator.class, Generators.ofPostalCode(Locale.GERMANY));
        assertInstanceOf(PostalCodeGenerator.class, Generators.ofPostalCode(config));
        assertInstanceOf(CountryGenerator.class, Generators.ofCountry(Locale.GERMANY));
        assertInstanceOf(CountryGenerator.class, Generators.ofCountry(config));
        assertInstanceOf(PhoneNumberGenerator.class, Generators.ofPhoneNumber(Locale.GERMANY));
        assertInstanceOf(PhoneNumberGenerator.class, Generators.ofPhoneNumber(config));
        assertInstanceOf(CompanyNameGenerator.class, Generators.ofCompanyName(Locale.GERMANY));
        assertInstanceOf(CompanyNameGenerator.class, Generators.ofCompanyName(config));
        assertInstanceOf(CompanyUrlGenerator.class, Generators.ofCompanyUrl(Locale.GERMANY));
        assertInstanceOf(CompanyUrlGenerator.class, Generators.ofCompanyUrl(config));
        assertInstanceOf(ProfessionGenerator.class, Generators.ofProfession(Locale.GERMANY));
        assertInstanceOf(ProfessionGenerator.class, Generators.ofProfession(config));
        assertInstanceOf(MoneyGenerator.class, Generators.ofMoney(Locale.GERMANY));
        assertInstanceOf(MoneyGenerator.class, Generators.ofMoney(config));
        assertInstanceOf(UsernameGenerator.class, Generators.ofUsername(Locale.GERMANY));
        assertInstanceOf(UsernameGenerator.class, Generators.ofUsername(config));
    }

    @Test
    @DisplayName("contact/person info factories return structured payload generators")
    void structuredUserFactories() {
        assertInstanceOf(ContactInfoGenerator.class, Generators.ofContactInfo());
        assertInstanceOf(ContactInfoGenerator.class, Generators.ofContactInfo(Locale.US));
        assertInstanceOf(ContactInfoGenerator.class, Generators.ofContactInfo(GeneratorConfig.defaults()));
        assertInstanceOf(JobInfoGenerator.class, Generators.ofJobInfo());
        assertInstanceOf(JobInfoGenerator.class, Generators.ofJobInfo(Locale.US));
        assertInstanceOf(JobInfoGenerator.class, Generators.ofJobInfo(GeneratorConfig.defaults()));
        assertInstanceOf(PersonInfoGenerator.class, Generators.ofPersonInfo());
        assertInstanceOf(PersonInfoGenerator.class, Generators.ofPersonInfo(Locale.US));
        assertInstanceOf(PersonInfoGenerator.class, Generators.ofPersonInfo(GeneratorConfig.defaults()));
    }

    @Test
    @DisplayName("ofCompanyEmail() returns CompanyEmailGenerator")
    void ofCompanyEmail() {
        assertInstanceOf(CompanyEmailGenerator.class, Generators.ofCompanyEmail());
    }

    @Test
    @DisplayName("ofMiddleName() returns MiddleNameGenerator")
    void ofMiddleName() {
        assertInstanceOf(MiddleNameGenerator.class, Generators.ofMiddleName());
        assertInstanceOf(MiddleNameGenerator.class, Generators.ofMiddleName(Locale.US));
        assertInstanceOf(MiddleNameGenerator.class,
                         Generators.ofMiddleName(GeneratorConfig.builder().locale(Locale.US).seed(11L).build()));
    }

    @Test
    @DisplayName("ofSimpleProfile() returns SimpleProfileGenerator")
    void ofSimpleProfile() {
        assertInstanceOf(SimpleProfileGenerator.class, Generators.ofSimpleProfile());
    }

    @Test
    @DisplayName("ofProfile() returns ProfileGenerator")
    void ofProfile() {
        assertInstanceOf(ProfileGenerator.class, Generators.ofProfile());
    }

    @Test
    @DisplayName("ofStreetAddress() and ofAddressInfo() return location generators")
    void structuredLocationFactories() {
        assertInstanceOf(StreetAddressGenerator.class, Generators.ofStreetAddress());
        assertInstanceOf(AddressInfoGenerator.class, Generators.ofAddressInfo());
        assertInstanceOf(AddressInfoGenerator.class, Generators.ofAddressInfo(Locale.US));
        assertInstanceOf(AddressInfoGenerator.class, Generators.ofAddressInfo(GeneratorConfig.defaults()));
    }

    @Test
    @DisplayName("social factories return social generators")
    void socialFactories() {
        assertInstanceOf(SocialHandleGenerator.class, Generators.ofSocialHandle());
        assertInstanceOf(SocialProfileGenerator.class, Generators.ofSocialProfile());
    }

    @Test
    @DisplayName("ofCity() returns CityGenerator")
    void ofCity() {
        assertInstanceOf(CityGenerator.class, Generators.ofCity());
    }

    @Test
    @DisplayName("ofState() returns StateGenerator")
    void ofState() {
        assertInstanceOf(StateGenerator.class, Generators.ofState());
    }

    @Test
    @DisplayName("ofPostalCode() returns PostalCodeGenerator")
    void ofPostalCode() {
        assertInstanceOf(PostalCodeGenerator.class, Generators.ofPostalCode());
    }

    @Test
    @DisplayName("ofCountry() returns CountryGenerator")
    void ofCountry() {
        assertInstanceOf(CountryGenerator.class, Generators.ofCountry());
    }

    @Test
    @DisplayName("ofPhoneNumber() returns PhoneNumberGenerator")
    void ofPhoneNumber() {
        assertInstanceOf(PhoneNumberGenerator.class, Generators.ofPhoneNumber());
    }

    @Test
    @DisplayName("ofUuid() returns UUIDGenerator")
    void ofUuid() {
        assertInstanceOf(UUIDGenerator.class, Generators.ofUuid());
    }

    @Test
    @DisplayName("ofNationalId(locale) returns NationalIdGenerator")
    void ofNationalId() {
        assertInstanceOf(NationalIdGenerator.class, Generators.ofNationalId(Locale.US));
    }

    @Test
    @DisplayName("ofNationalId(locale,seed) is reproducible")
    void ofNationalIdSeeded() {
        NationalIdGenerator a = Generators.ofNationalId(Locale.US, 42L);
        NationalIdGenerator b = Generators.ofNationalId(Locale.US, 42L);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("ofNationalId(config) respects config locale")
    void ofNationalIdConfig() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.GERMANY).seed(11L).build();
        NationalIdGenerator generator = Generators.ofNationalId(config);
        assertEquals(Locale.GERMANY, generator.getLocale());
    }

    @Test
    @DisplayName("schema/field factories return corresponding types")
    void schemaFieldFactories() {
        assertInstanceOf(Field.class, Generators.ofField());
        Field field = Generators.ofField(Locale.US);
        assertInstanceOf(Field.class, field);
        assertInstanceOf(Field.class,
                         Generators.ofField(GeneratorConfig.builder().locale(Locale.CANADA).seed(2L).build()));
        assertInstanceOf(Field.class,
                         Generators.ofField(new FieldLookup(GeneratorConfig.builder().locale(Locale.FRANCE).build())));
        Map<String, SchemaValueProvider> fields = Map.of(
            "name", field.bind("person.full_name")
        );
        assertInstanceOf(Schema.class, Generators.ofSchema(fields));
        assertInstanceOf(Schema.class, Generators.ofSchema(Locale.US, fields));
        assertInstanceOf(Schema.class,
                         Generators.ofSchema(GeneratorConfig.builder().locale(Locale.US).seed(1L).build(), fields));
    }

    @Test
    @DisplayName("provider hub factories return corresponding type")
    void providerHubFactories() {
        assertInstanceOf(ProviderHub.class, Generators.ofProviderHub());
        assertInstanceOf(ProviderHub.class, Generators.ofProviderHub(Locale.US));
        assertInstanceOf(ProviderHub.class,
                         Generators.ofProviderHub(GeneratorConfig.builder().locale(Locale.US).seed(1L).build()));
        assertInstanceOf(ProviderHub.class, Generators.ofProviderHub(GeneratorProfile.FAST));
        assertInstanceOf(ProviderHub.class, Generators.ofProviderHub(Locale.US, GeneratorProfile.STRICT));
        assertInstanceOf(ProviderHub.class,
                         Generators.ofProviderHub(GeneratorConfig.builder().locale(Locale.US).build(),
                                                 GeneratorProfile.REALISTIC));
    }

    @Test
    @DisplayName("object factories return corresponding object generator")
    void objectFactories() {
        assertInstanceOf(ObjectGenerator.class, Generators.ofObject(SimplePojo.class));
        assertInstanceOf(ObjectGenerator.class,
                         Generators.ofObject(SimplePojo.class, GeneratorConfig.builder().seed(7L).build()));
        assertInstanceOf(ObjectFaker.class, Generators.ofObjectFaker(SimplePojo.class));
        assertInstanceOf(ObjectFaker.class,
                         Generators.ofObjectFaker(SimplePojo.class, GeneratorConfig.builder().seed(7L).build()));
    }

    @Test
    @DisplayName("finance factories return corresponding generators")
    void financeFactories() {
        assertInstanceOf(ProductInfoGenerator.class, Generators.ofProductInfo());
        assertInstanceOf(ProductInfoGenerator.class, Generators.ofProductInfo(Locale.US));
        assertInstanceOf(ProductInfoGenerator.class, Generators.ofProductInfo(GeneratorConfig.defaults()));
        assertInstanceOf(OrderInfoGenerator.class, Generators.ofOrderInfo());
        assertInstanceOf(OrderInfoGenerator.class, Generators.ofOrderInfo(Locale.US));
        assertInstanceOf(OrderInfoGenerator.class, Generators.ofOrderInfo(GeneratorConfig.defaults()));
        assertInstanceOf(OrderInfoGenerator.class,
                         Generators.ofOrderInfo(GeneratorConfig.defaults(),
                                                new AddressInfoGenerator(GeneratorConfig.defaults())));
        assertInstanceOf(ShipmentInfoGenerator.class, Generators.ofShipmentInfo());
        assertInstanceOf(ShipmentInfoGenerator.class, Generators.ofShipmentInfo(Locale.US));
        assertInstanceOf(ShipmentInfoGenerator.class, Generators.ofShipmentInfo(GeneratorConfig.defaults()));
        assertInstanceOf(CurrencyGenerator.class, Generators.ofCurrency());
        assertInstanceOf(CurrencyPairGenerator.class, Generators.ofCurrencyPair());
        assertInstanceOf(CurrencyPairGenerator.class,
                         Generators.ofCurrencyPair(GeneratorConfig.builder().seed(11L).build()));
        assertInstanceOf(MoneyGenerator.class, Generators.ofMoney());
        assertInstanceOf(CardExpirationGenerator.class, Generators.ofCardExpiration());
        assertInstanceOf(CreditCardGenerator.class, Generators.ofCreditCard());
        assertInstanceOf(CreditCardInfoGenerator.class, Generators.ofCreditCardInfo());
        assertInstanceOf(CreditCardInfoGenerator.class, Generators.ofCreditCardInfo(Locale.US));
        assertInstanceOf(CreditCardInfoGenerator.class, Generators.ofCreditCardInfo(GeneratorConfig.defaults()));
        assertInstanceOf(InvoiceInfoGenerator.class, Generators.ofInvoiceInfo());
        assertInstanceOf(InvoiceInfoGenerator.class, Generators.ofInvoiceInfo(Locale.US));
        assertInstanceOf(InvoiceInfoGenerator.class, Generators.ofInvoiceInfo(GeneratorConfig.defaults()));
        assertInstanceOf(PaymentInfoGenerator.class, Generators.ofPaymentInfo());
        assertInstanceOf(PaymentInfoGenerator.class, Generators.ofPaymentInfo(Locale.US));
        assertInstanceOf(PaymentInfoGenerator.class, Generators.ofPaymentInfo(GeneratorConfig.defaults()));
        assertInstanceOf(BbanGenerator.class, Generators.ofBban());
        assertInstanceOf(IbanGenerator.class, Generators.ofIban());
        assertInstanceOf(AbaRoutingGenerator.class, Generators.ofAbaRouting());
        assertInstanceOf(BankCountryGenerator.class, Generators.ofBankCountry());
        assertInstanceOf(BankInfoGenerator.class, Generators.ofBankInfo());
        assertInstanceOf(BankInfoGenerator.class, Generators.ofBankInfo(Locale.US));
        assertInstanceOf(BankInfoGenerator.class, Generators.ofBankInfo(GeneratorConfig.defaults()));
        assertInstanceOf(BankNameGenerator.class, Generators.ofBankName());
        assertInstanceOf(BankTypeGenerator.class, Generators.ofBankType());
        assertInstanceOf(CusipGenerator.class, Generators.ofCusip());
        assertInstanceOf(EinGenerator.class, Generators.ofEin());
    }

    @Test
    @DisplayName("extra text and identifier factories return corresponding generators")
    void extraFactories() {
        assertInstanceOf(CompanyBuzzwordGenerator.class, Generators.ofCompanyBuzzword());
        assertInstanceOf(CompanyCatchPhraseGenerator.class, Generators.ofCompanyCatchPhrase());
        assertInstanceOf(TextGenerator.class, Generators.ofText());
        assertInstanceOf(HashGenerator.class, Generators.ofHash());
        assertInstanceOf(IdentifierMaskGenerator.class, Generators.ofIdentifierMask());
        assertInstanceOf(EanGenerator.class, Generators.ofEan());
        assertInstanceOf(UpcGenerator.class, Generators.ofUpc());
    }

    // ── forType ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("forType(Byte.class) returns a generator")
    void forTypeByte() {
        assertNotNull(Generators.forType(Byte.class).generate());
    }

    private static final class SimplePojo {
        private String value;
    }

    @Test
    @DisplayName("forType(byte.class) returns a generator")
    void forTypeBytePrimitive() {
        assertNotNull(Generators.forType(byte.class).generate());
    }

    @Test
    @DisplayName("forType(Short.class) returns a generator")
    void forTypeShort() {
        assertNotNull(Generators.forType(Short.class).generate());
    }

    @Test
    @DisplayName("forType(short.class) returns a generator")
    void forTypeShortPrimitive() {
        assertNotNull(Generators.forType(short.class).generate());
    }

    @Test
    @DisplayName("forType(Integer.class) returns a generator")
    void forTypeInteger() {
        assertNotNull(Generators.forType(Integer.class).generate());
    }

    @Test
    @DisplayName("forType(int.class) returns a generator")
    void forTypeIntPrimitive() {
        assertNotNull(Generators.forType(int.class).generate());
    }

    @Test
    @DisplayName("forType(Long.class) returns a generator")
    void forTypeLong() {
        assertNotNull(Generators.forType(Long.class).generate());
    }

    @Test
    @DisplayName("forType(long.class) returns a generator")
    void forTypeLongPrimitive() {
        assertNotNull(Generators.forType(long.class).generate());
    }

    @Test
    @DisplayName("forType(Float.class) returns a generator")
    void forTypeFloat() {
        assertNotNull(Generators.forType(Float.class).generate());
    }

    @Test
    @DisplayName("forType(float.class) returns a generator")
    void forTypeFloatPrimitive() {
        assertNotNull(Generators.forType(float.class).generate());
    }

    @Test
    @DisplayName("forType(Double.class) returns a generator")
    void forTypeDouble() {
        assertNotNull(Generators.forType(Double.class).generate());
    }

    @Test
    @DisplayName("forType(double.class) returns a generator")
    void forTypeDoublePrimitive() {
        assertNotNull(Generators.forType(double.class).generate());
    }

    @Test
    @DisplayName("forType(Character.class) returns a generator")
    void forTypeCharacter() {
        assertNotNull(Generators.forType(Character.class).generate());
    }

    @Test
    @DisplayName("forType(char.class) returns a generator")
    void forTypeCharPrimitive() {
        assertNotNull(Generators.forType(char.class).generate());
    }

    @Test
    @DisplayName("forType(Boolean.class) returns a generator")
    void forTypeBoolean() {
        assertNotNull(Generators.forType(Boolean.class).generate());
    }

    @Test
    @DisplayName("forType(boolean.class) returns a generator")
    void forTypeBooleanPrimitive() {
        assertNotNull(Generators.forType(boolean.class).generate());
    }

    @Test
    @DisplayName("forType(String.class) returns a generator")
    void forTypeString() {
        assertNotNull(Generators.forType(String.class).generate());
    }

    @Test
    @DisplayName("forType(null) throws NullPointerException")
    void forTypeNullThrows() {
        assertThrows(NullPointerException.class, () -> Generators.forType(null));
    }

    @Test
    @DisplayName("forType(unknown type) throws IllegalArgumentException")
    void forTypeUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> Generators.forType(Object.class));
    }
}
