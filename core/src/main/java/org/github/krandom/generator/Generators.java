/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.algorithms.FibonacciGenerator;
import org.github.krandom.generator.algorithms.LuhnGenerator;
import org.github.krandom.generator.base.BigDecimalGenerator;
import org.github.krandom.generator.base.BigIntegerGenerator;
import org.github.krandom.generator.base.BooleanGenerator;
import org.github.krandom.generator.base.ByteGenerator;
import org.github.krandom.generator.base.CharGenerator;
import org.github.krandom.generator.base.DigitGenerator;
import org.github.krandom.generator.base.DoubleGenerator;
import org.github.krandom.generator.base.FloatGenerator;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.base.LongGenerator;
import org.github.krandom.generator.base.NaturalNumberGenerator;
import org.github.krandom.generator.base.NormalDistributionGenerator;
import org.github.krandom.generator.base.NullableBooleanGenerator;
import org.github.krandom.generator.base.NumberWithFormatGenerator;
import org.github.krandom.generator.base.PrimeGenerator;
import org.github.krandom.generator.base.PyDecimalGenerator;
import org.github.krandom.generator.base.ShortGenerator;
import org.github.krandom.generator.base.StringGenerator;
import org.github.krandom.generator.color.ColorGenerator;
import org.github.krandom.generator.commerce.CommerceGenerator;
import org.github.krandom.generator.commerce.ProductInfoGenerator;
import org.github.krandom.generator.database.DatabaseGenerator;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.DurationGenerator;
import org.github.krandom.generator.datetime.InstantGenerator;
import org.github.krandom.generator.datetime.LocalDateTimeGenerator;
import org.github.krandom.generator.datetime.TimezoneGenerator;
import org.github.krandom.generator.datetime.ZonedDateTimeGenerator;
import org.github.krandom.generator.file.DirPathGenerator;
import org.github.krandom.generator.file.FileExtensionGenerator;
import org.github.krandom.generator.file.FileNameGenerator;
import org.github.krandom.generator.file.FilePathGenerator;
import org.github.krandom.generator.file.MimeTypeGenerator;
import org.github.krandom.generator.file.SemverGenerator;
import org.github.krandom.generator.finance.AbaRoutingGenerator;
import org.github.krandom.generator.finance.BankAccountGenerator;
import org.github.krandom.generator.finance.BankInfoGenerator;
import org.github.krandom.generator.finance.BankCountryGenerator;
import org.github.krandom.generator.finance.BankNameGenerator;
import org.github.krandom.generator.finance.BankTypeGenerator;
import org.github.krandom.generator.finance.BbanGenerator;
import org.github.krandom.generator.finance.BicGenerator;
import org.github.krandom.generator.finance.CardExpirationGenerator;
import org.github.krandom.generator.finance.CreditCardGenerator;
import org.github.krandom.generator.finance.CreditCardInfoGenerator;
import org.github.krandom.generator.finance.CryptoAddressGenerator;
import org.github.krandom.generator.finance.CurrencyGenerator;
import org.github.krandom.generator.finance.CurrencyPairGenerator;
import org.github.krandom.generator.finance.CusipGenerator;
import org.github.krandom.generator.finance.EinGenerator;
import org.github.krandom.generator.finance.IbanGenerator;
import org.github.krandom.generator.finance.IsinGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.games.coin.CoinGenerator;
import org.github.krandom.generator.games.dice.DiceGenerator;
import org.github.krandom.generator.games.dice.DiceType;
import org.github.krandom.generator.identifier.EanGenerator;
import org.github.krandom.generator.identifier.HashGenerator;
import org.github.krandom.generator.identifier.IdentifierMaskGenerator;
import org.github.krandom.generator.identifier.IsbnGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.identifier.UpcGenerator;
import org.github.krandom.generator.location.AddressInfoGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.PostalCodeGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.DomainGenerator;
import org.github.krandom.generator.network.HostnameGenerator;
import org.github.krandom.generator.network.HttpMethodGenerator;
import org.github.krandom.generator.network.HttpStatusCodeGenerator;
import org.github.krandom.generator.network.IPGenerator;
import org.github.krandom.generator.network.IPv4Generator;
import org.github.krandom.generator.network.IPv6Generator;
import org.github.krandom.generator.network.MacAddressGenerator;
import org.github.krandom.generator.network.PortGenerator;
import org.github.krandom.generator.network.SlugGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.network.UriGenerator;
import org.github.krandom.generator.network.UserAgentGenerator;
import org.github.krandom.generator.object.ObjectFaker;
import org.github.krandom.generator.object.ObjectGenerator;
import org.github.krandom.generator.object.ObjectGeneratorConfig;
import org.github.krandom.generator.provider.ProviderHub;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.FieldLookup;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.github.krandom.generator.selection.PickGenerator;
import org.github.krandom.generator.selection.PickSetGenerator;
import org.github.krandom.generator.selection.RepeatGenerator;
import org.github.krandom.generator.selection.ShuffleGenerator;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.selection.WeightedGenerator;
import org.github.krandom.generator.system.ExceptionPayloadGenerator;
import org.github.krandom.generator.system.PlatformIdGenerator;
import org.github.krandom.generator.system.VersionGenerator;
import org.github.krandom.generator.text.LoremIpsumGenerator;
import org.github.krandom.generator.text.ParagraphGenerator;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.SyllableGenerator;
import org.github.krandom.generator.text.TemplateStringGenerator;
import org.github.krandom.generator.text.TextGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.AvatarUrlGenerator;
import org.github.krandom.generator.user.CompanyBuzzwordGenerator;
import org.github.krandom.generator.user.CompanyCatchPhraseGenerator;
import org.github.krandom.generator.user.CompanyEmailGenerator;
import org.github.krandom.generator.user.CompanyInfoGenerator;
import org.github.krandom.generator.user.CompanyNameGenerator;
import org.github.krandom.generator.user.CompanyUrlGenerator;
import org.github.krandom.generator.user.ContactInfoGenerator;
import org.github.krandom.generator.user.EducationalAttainmentGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.IndustryGenerator;
import org.github.krandom.generator.user.JobFieldGenerator;
import org.github.krandom.generator.user.JobInfoGenerator;
import org.github.krandom.generator.user.JobTypeGenerator;
import org.github.krandom.generator.user.MaritalStatusGenerator;
import org.github.krandom.generator.user.MiddleNameGenerator;
import org.github.krandom.generator.user.PasswordGenerator;
import org.github.krandom.generator.user.PersonInfoGenerator;
import org.github.krandom.generator.user.PositionGenerator;
import org.github.krandom.generator.user.ProfessionGenerator;
import org.github.krandom.generator.user.ProfileGenerator;
import org.github.krandom.generator.user.SeniorityGenerator;
import org.github.krandom.generator.user.SimpleProfileGenerator;
import org.github.krandom.generator.user.SocialHandleGenerator;
import org.github.krandom.generator.user.SocialProfileGenerator;
import org.github.krandom.generator.user.UsernameGenerator;
import org.github.krandom.generator.user.nationalid.NationalIdGenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Static factory for all built-in base-type generators.
 *
 * <p>Every {@code of*()} method has two forms:
 * <ul>
 *   <li>No-arg — default range / default character set, uses {@link java.security.SecureRandom}.</li>
 *   <li>With bounds (and optional seed) — fully configured.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   int          value  = Generators.ofInt().generate();
 *   int          roll   = Generators.ofInt(1, 7).generate();          // die [1..6]
 *   List<String> names  = Generators.ofString().generateList(20);
 *   List<Long>   ids    = Generators.ofLong(1L, 1_000_000L).generateList(100);
 *
 *   // Generic lookup by Class
 *   Generator<Integer> g = Generators.forType(Integer.class);
 * }</pre>
 *
 * <p>All generators returned by this class are independent instances — they do not share state.
 */
public final class Generators {

    private static final Map<Class<?>, Supplier<? extends Generator<?>>> REGISTRY;

    // ── Byte ──────────────────────────────────────────────────────────────────

    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put(Byte.class, ByteGenerator::new);
        REGISTRY.put(byte.class, ByteGenerator::new);
        REGISTRY.put(Short.class, ShortGenerator::new);
        REGISTRY.put(short.class, ShortGenerator::new);
        REGISTRY.put(Integer.class, IntGenerator::new);
        REGISTRY.put(int.class, IntGenerator::new);
        REGISTRY.put(Long.class, LongGenerator::new);
        REGISTRY.put(long.class, LongGenerator::new);
        REGISTRY.put(Float.class, FloatGenerator::new);
        REGISTRY.put(float.class, FloatGenerator::new);
        REGISTRY.put(Double.class, DoubleGenerator::new);
        REGISTRY.put(double.class, DoubleGenerator::new);
        REGISTRY.put(Character.class, CharGenerator::letters);
        REGISTRY.put(char.class, CharGenerator::letters);
        REGISTRY.put(Boolean.class, BooleanGenerator::new);
        REGISTRY.put(boolean.class, BooleanGenerator::new);
        REGISTRY.put(String.class, StringGenerator::letters);
    }

    private Generators() { /* static utility */ }

    public static ByteGenerator ofByte() {
        return new ByteGenerator();
    }

    // ── Short ─────────────────────────────────────────────────────────────────

    public static ByteGenerator ofByte(byte min, byte max) {
        return new ByteGenerator(min, max);
    }

    public static ByteGenerator ofByte(byte min, byte max, long seed) {
        return new ByteGenerator(min, max, seed);
    }

    public static ShortGenerator ofShort() {
        return new ShortGenerator();
    }

    // ── Int ───────────────────────────────────────────────────────────────────

    public static ShortGenerator ofShort(short min, short max) {
        return new ShortGenerator(min, max);
    }

    public static ShortGenerator ofShort(short min, short max, long seed) {
        return new ShortGenerator(min, max, seed);
    }

    public static IntGenerator ofInt() {
        return new IntGenerator();
    }

    // ── Natural Number ────────────────────────────────────────────────────────

    public static IntGenerator ofInt(int min, int max) {
        return new IntGenerator(min, max);
    }

    public static IntGenerator ofInt(int min, int max, long seed) {
        return new IntGenerator(min, max, seed);
    }

    public static NaturalNumberGenerator ofNaturalNumber() {
        return new NaturalNumberGenerator();
    }

    // ── Long ──────────────────────────────────────────────────────────────────

    public static NaturalNumberGenerator ofNaturalNumber(int min, int max) {
        return new NaturalNumberGenerator(min, max);
    }

    public static NaturalNumberGenerator ofNaturalNumber(int min, int max, long seed) {
        return new NaturalNumberGenerator(min, max, seed);
    }

    public static LongGenerator ofLong() {
        return new LongGenerator();
    }

    // ── Float ─────────────────────────────────────────────────────────────────

    public static LongGenerator ofLong(long min, long max) {
        return new LongGenerator(min, max);
    }

    public static LongGenerator ofLong(long min, long max, long seed) {
        return new LongGenerator(min, max, seed);
    }

    public static FloatGenerator ofFloat() {
        return new FloatGenerator();
    }

    // ── Double ────────────────────────────────────────────────────────────────

    public static FloatGenerator ofFloat(float min, float max) {
        return new FloatGenerator(min, max);
    }

    public static FloatGenerator ofFloat(float min, float max, long seed) {
        return new FloatGenerator(min, max, seed);
    }

    public static DoubleGenerator ofDouble() {
        return new DoubleGenerator();
    }

    // ── Normal Distribution ───────────────────────────────────────────────────

    public static DoubleGenerator ofDouble(double min, double max) {
        return new DoubleGenerator(min, max);
    }

    public static DoubleGenerator ofDouble(double min, double max, long seed) {
        return new DoubleGenerator(min, max, seed);
    }

    public static NormalDistributionGenerator ofNormal() {
        return new NormalDistributionGenerator();
    }

    // ── Prime ─────────────────────────────────────────────────────────────────

    public static NormalDistributionGenerator ofNormal(double mean, double standardDeviation) {
        return new NormalDistributionGenerator(mean, standardDeviation);
    }

    public static NormalDistributionGenerator ofNormal(double mean, double standardDeviation, long seed) {
        return new NormalDistributionGenerator(mean, standardDeviation, seed);
    }

    public static PrimeGenerator ofPrime() {
        return new PrimeGenerator();
    }

    // ── Char ──────────────────────────────────────────────────────────────────

    public static PrimeGenerator ofPrime(int min, int max) {
        return new PrimeGenerator(min, max);
    }

    public static PrimeGenerator ofPrime(int min, int max, long seed) {
        return new PrimeGenerator(min, max, seed);
    }

    // ── Boolean ───────────────────────────────────────────────────────────────

    /**
     * Letters (upper + lower).
     */
    public static CharGenerator ofChar() {
        return CharGenerator.letters();
    }

    /**
     * Custom character pool via a {@link CharGenerator.Builder}.
     */
    public static CharGenerator.Builder ofChar(CharGenerator.Builder builder) {
        return Objects.requireNonNull(builder, "builder");
    }

    // ── String ────────────────────────────────────────────────────────────────

    public static BooleanGenerator ofBoolean() {
        return new BooleanGenerator();
    }

    public static BooleanGenerator ofBoolean(long seed) {
        return new BooleanGenerator(seed);
    }

    /**
     * Letters only, length 5–20.
     */
    public static StringGenerator ofString() {
        return StringGenerator.letters();
    }

    /**
     * Full control via a pre-configured builder.
     */
    public static StringGenerator ofString(StringGenerator.Builder builder) {
        return Objects.requireNonNull(builder, "builder").build();
    }

    /**
     * Returns a generator that produces single decimal digits as strings.
     */
    public static DigitGenerator ofDigit() {
        return new DigitGenerator();
    }

    /**
     * Returns a generator that produces numbers from '#' placeholder formats.
     */
    public static NumberWithFormatGenerator ofNumberWithFormat() {
        return new NumberWithFormatGenerator();
    }

    // ── Algorithms ────────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces decimal values similar to Faker pydecimal.
     */
    public static PyDecimalGenerator ofPyDecimal() {
        return new PyDecimalGenerator();
    }

    /**
     * Returns a generator that produces nullable booleans (true/false/null).
     */
    public static NullableBooleanGenerator ofNullableBoolean() {
        return new NullableBooleanGenerator();
    }

    // ── Games ─────────────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces random Fibonacci numbers.
     */
    public static FibonacciGenerator ofFibonacci() {
        return new FibonacciGenerator();
    }

    /**
     * Returns a generator that produces 10-digit Luhn-valid number strings.
     */
    public static LuhnGenerator ofLuhn() {
        return new LuhnGenerator();
    }

    // ── Network ───────────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces random coin-flip results ({@code HEAD} or {@code TAIL}).
     */
    public static CoinGenerator ofCoin() {
        return new CoinGenerator();
    }

    /**
     * Returns a generator for the given die type (results in {@code [1, sides]}).
     */
    public static DiceGenerator ofDice(DiceType type) {
        return new DiceGenerator(type);
    }

    /**
     * Returns a generator that produces random IPv4 addresses (RFC 791, unicast range).
     */
    public static IPv4Generator ofIPv4() {
        return new IPv4Generator();
    }

    /**
     * Returns a generator that produces random IPv6 addresses (RFC 4291 / RFC 5952).
     */
    public static IPv6Generator ofIPv6() {
        return new IPv6Generator();
    }

    /**
     * Returns a generator that produces either IPv4 or IPv6 addresses per call.
     */
    public static IPGenerator ofIP() {
        return new IPGenerator();
    }

    /**
     * Returns a generator that produces TCP/UDP ports in [1, 65535].
     */
    public static PortGenerator ofPort() {
        return new PortGenerator();
    }

    /**
     * Returns a generator that produces URL-friendly slugs.
     */
    public static SlugGenerator ofSlug() {
        return new SlugGenerator();
    }

    /**
     * Returns a generator that produces browser and bot user-agent strings.
     */
    public static UserAgentGenerator ofUserAgent() {
        return new UserAgentGenerator();
    }

    /**
     * Returns a generator that produces standard HTTP status codes.
     */
    public static HttpStatusCodeGenerator ofHttpStatusCode() {
        return new HttpStatusCodeGenerator();
    }

    /**
     * Returns a generator that produces HTTP methods.
     */
    public static HttpMethodGenerator ofHttpMethod() {
        return new HttpMethodGenerator();
    }

    /**
     * Returns a generator that produces domain names.
     */
    public static DomainGenerator ofDomain() {
        return new DomainGenerator();
    }

    /**
     * Returns a generator that produces hostnames.
     */
    public static HostnameGenerator ofHostname() {
        return new HostnameGenerator();
    }

    // ── BigDecimal ────────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces URL strings.
     */
    public static URLGenerator ofUrl() {
        return new URLGenerator();
    }

    /**
     * Returns a generator that produces URI strings.
     */
    public static UriGenerator ofUri() {
        return new UriGenerator();
    }

    // ── BigInteger ────────────────────────────────────────────────────────────

    /**
     * Returns a generator producing random {@link BigDecimal} values ([0, 1&nbsp;000&nbsp;000], scale 2).
     */
    public static BigDecimalGenerator ofBigDecimal() {
        return new BigDecimalGenerator();
    }

    /**
     * Returns a generator producing random {@link BigDecimal} values in [min, max] with scale 2.
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (inclusive)
     */
    public static BigDecimalGenerator ofBigDecimal(BigDecimal min, BigDecimal max) {
        return new BigDecimalGenerator(min, max);
    }

    /**
     * Returns a generator producing random {@link BigInteger} values ([0, {@link Long#MAX_VALUE}]).
     */
    public static BigIntegerGenerator ofBigInteger() {
        return new BigIntegerGenerator();
    }

    // ── Date / Time ───────────────────────────────────────────────────────────

    /**
     * Returns a generator producing random {@link BigInteger} values in [min, max].
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (inclusive)
     */
    public static BigIntegerGenerator ofBigInteger(BigInteger min, BigInteger max) {
        return new BigIntegerGenerator(min, max);
    }

    /**
     * Returns a generator that produces color values and color names.
     */
    public static ColorGenerator ofColor() {
        return new ColorGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.time.LocalDate} values (1970–2100).
     */
    public static DateGenerator ofLocalDate() {
        return new DateGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.time.LocalDateTime} values (1970–2100).
     */
    public static LocalDateTimeGenerator ofLocalDateTime() {
        return new LocalDateTimeGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.time.Instant} values (1970–2100 at UTC midnight).
     */
    public static InstantGenerator ofInstant() {
        return new InstantGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.time.ZonedDateTime} values (1970–2100).
     */
    public static ZonedDateTimeGenerator ofZonedDateTime() {
        return new ZonedDateTimeGenerator();
    }

    // ── Full name ─────────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces random {@link java.time.Duration} values.
     */
    public static DurationGenerator ofDuration() {
        return new DurationGenerator();
    }

    /**
     * Returns a generator that produces timezone identifiers.
     */
    public static TimezoneGenerator ofTimezone() {
        return new TimezoneGenerator();
    }

    /**
     * Returns a generator that produces random full names (first + last) in {@link java.util.Locale#US}.
     */
    public static FullNameGenerator ofFullName() {
        return new FullNameGenerator();
    }

    /**
     * Returns a generator that produces locale-aware middle names.
     */
    public static MiddleNameGenerator ofMiddleName() {
        return new MiddleNameGenerator();
    }

    /**
     * Returns a generator that produces locale-aware middle names for a specific locale.
     */
    public static MiddleNameGenerator ofMiddleName(Locale locale) {
        return new MiddleNameGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware middle names with explicit configuration.
     */
    public static MiddleNameGenerator ofMiddleName(GeneratorConfig config) {
        return new MiddleNameGenerator(config);
    }

    /**
     * Returns a generator that produces email addresses.
     */
    public static EmailGenerator ofEmail() {
        return new EmailGenerator();
    }

    /**
     * Returns a generator that produces structured contact payloads.
     */
    public static ContactInfoGenerator ofContactInfo() {
        return new ContactInfoGenerator();
    }

    /**
     * Returns a generator that produces structured contact payloads for a specific locale.
     */
    public static ContactInfoGenerator ofContactInfo(Locale locale) {
        return new ContactInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured contact payloads with explicit configuration.
     */
    public static ContactInfoGenerator ofContactInfo(GeneratorConfig config) {
        return new ContactInfoGenerator(config);
    }

    /**
     * Returns a generator that produces company email addresses.
     */
    public static CompanyEmailGenerator ofCompanyEmail() {
        return new CompanyEmailGenerator();
    }

    /**
     * Returns a generator that produces structured company payloads.
     */
    public static CompanyInfoGenerator ofCompanyInfo() {
        return new CompanyInfoGenerator();
    }

    /**
     * Returns a generator that produces structured company payloads for a specific locale.
     */
    public static CompanyInfoGenerator ofCompanyInfo(Locale locale) {
        return new CompanyInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured company payloads with explicit configuration.
     */
    public static CompanyInfoGenerator ofCompanyInfo(GeneratorConfig config) {
        return new CompanyInfoGenerator(config);
    }

    // ── Street address ────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces simple user profiles.
     */
    public static SimpleProfileGenerator ofSimpleProfile() {
        return new SimpleProfileGenerator();
    }

    /**
     * Returns a generator that produces extended user profiles.
     */
    public static ProfileGenerator ofProfile() {
        return new ProfileGenerator();
    }

    /**
     * Returns a generator that produces structured job payloads.
     */
    public static JobInfoGenerator ofJobInfo() {
        return new JobInfoGenerator();
    }

    /**
     * Returns a generator that produces structured job payloads for a specific locale.
     */
    public static JobInfoGenerator ofJobInfo(Locale locale) {
        return new JobInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured job payloads with explicit configuration.
     */
    public static JobInfoGenerator ofJobInfo(GeneratorConfig config) {
        return new JobInfoGenerator(config);
    }

    /**
     * Returns a generator that produces structured person payloads.
     */
    public static PersonInfoGenerator ofPersonInfo() {
        return new PersonInfoGenerator();
    }

    /**
     * Returns a generator that produces structured person payloads for a specific locale.
     */
    public static PersonInfoGenerator ofPersonInfo(Locale locale) {
        return new PersonInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured person payloads with explicit configuration.
     */
    public static PersonInfoGenerator ofPersonInfo(GeneratorConfig config) {
        return new PersonInfoGenerator(config);
    }

    /**
     * Returns a generator that produces social-media style handles.
     */
    public static SocialHandleGenerator ofSocialHandle() {
        return new SocialHandleGenerator();
    }

    /**
     * Returns a generator that produces social-media style profiles.
     */
    public static SocialProfileGenerator ofSocialProfile() {
        return new SocialProfileGenerator();
    }

    /**
     * Returns a generator that produces random US-style street addresses (e.g. {@code "123 Oak Ave"}).
     */
    public static StreetAddressGenerator ofStreetAddress() {
        return new StreetAddressGenerator();
    }

    /**
     * Returns a generator that produces structured address payloads.
     */
    public static AddressInfoGenerator ofAddressInfo() {
        return new AddressInfoGenerator();
    }

    /**
     * Returns a generator that produces structured address payloads for a specific locale.
     */
    public static AddressInfoGenerator ofAddressInfo(Locale locale) {
        return new AddressInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured address payloads with explicit configuration.
     */
    public static AddressInfoGenerator ofAddressInfo(GeneratorConfig config) {
        return new AddressInfoGenerator(config);
    }

    /**
     * Returns a generator that produces city names.
     */
    public static CityGenerator ofCity() {
        return new CityGenerator();
    }

    /**
     * Returns a generator that produces state/province names.
     */
    public static StateGenerator ofState() {
        return new StateGenerator();
    }

    /**
     * Returns a generator that produces postal codes.
     */
    public static PostalCodeGenerator ofPostalCode() {
        return new PostalCodeGenerator();
    }

    // ── Company name ──────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces country names and country codes.
     */
    public static CountryGenerator ofCountry() {
        return new CountryGenerator();
    }

    /**
     * Returns a generator that produces locale-aware phone numbers.
     */
    public static PhoneNumberGenerator ofPhoneNumber() {
        return new PhoneNumberGenerator();
    }

    /**
     * Returns a generator that produces random company names including a legal-form suffix.
     */
    public static CompanyNameGenerator ofCompanyName() {
        return new CompanyNameGenerator();
    }

    /**
     * Returns a generator that produces company website URLs.
     */
    public static CompanyUrlGenerator ofCompanyUrl() {
        return new CompanyUrlGenerator();
    }

    /**
     * Returns a generator that produces company buzzword phrases.
     */
    public static CompanyBuzzwordGenerator ofCompanyBuzzword() {
        return new CompanyBuzzwordGenerator();
    }

    /**
     * Returns a generator that produces company catch phrases.
     */
    public static CompanyCatchPhraseGenerator ofCompanyCatchPhrase() {
        return new CompanyCatchPhraseGenerator();
    }

    /**
     * Returns a generator that produces industry values.
     */
    public static IndustryGenerator ofIndustry() {
        return new IndustryGenerator();
    }

    /**
     * Returns a generator that produces job field categories.
     */
    public static JobFieldGenerator ofJobField() {
        return new JobFieldGenerator();
    }

    /**
     * Returns a generator that produces employment type values.
     */
    public static JobTypeGenerator ofJobType() {
        return new JobTypeGenerator();
    }

    /**
     * Returns a generator that produces job seniority labels.
     */
    public static SeniorityGenerator ofSeniority() {
        return new SeniorityGenerator();
    }

    /**
     * Returns a generator that produces position/job titles.
     */
    public static PositionGenerator ofPosition() {
        return new PositionGenerator();
    }

    /**
     * Returns a generator that produces educational attainment values.
     */
    public static EducationalAttainmentGenerator ofEducationalAttainment() {
        return new EducationalAttainmentGenerator();
    }

    /**
     * Returns a generator that produces marital status values.
     */
    public static MaritalStatusGenerator ofMaritalStatus() {
        return new MaritalStatusGenerator();
    }

    /**
     * Returns a generator that produces file extensions (for example: {@code "png"}, {@code "pdf"}).
     */
    public static FileExtensionGenerator ofFileExtension() {
        return new FileExtensionGenerator();
    }

    /**
     * Returns a generator that produces file names and file names with extensions.
     */
    public static FileNameGenerator ofFileName() {
        return new FileNameGenerator();
    }

    /**
     * Returns a generator that produces locale-aware directory paths.
     */
    public static DirPathGenerator ofDirPath() {
        return new DirPathGenerator();
    }

    /**
     * Returns a generator that produces locale-aware file paths.
     */
    public static FilePathGenerator ofFilePath() {
        return new FilePathGenerator();
    }

    /**
     * Returns a generator that produces MIME content types.
     */
    public static MimeTypeGenerator ofMimeType() {
        return new MimeTypeGenerator();
    }

    /**
     * Returns a generator that produces semantic version strings.
     */
    public static SemverGenerator ofSemver() {
        return new SemverGenerator();
    }

    /**
     * Returns a generator that produces commerce products/departments/prices.
     */
    public static CommerceGenerator ofCommerce() {
        return new CommerceGenerator();
    }

    /**
     * Returns a generator that produces structured commerce product payloads.
     */
    public static ProductInfoGenerator ofProductInfo() {
        return new ProductInfoGenerator();
    }

    /**
     * Returns a generator that produces structured commerce product payloads for a specific locale.
     */
    public static ProductInfoGenerator ofProductInfo(Locale locale) {
        return new ProductInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured commerce product payloads with explicit configuration.
     */
    public static ProductInfoGenerator ofProductInfo(GeneratorConfig config) {
        return new ProductInfoGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware profession/job-title values.
     */
    public static ProfessionGenerator ofProfession() {
        return new ProfessionGenerator();
    }

    /**
     * Returns a generator that produces SWIFT/BIC codes.
     */
    public static BicGenerator ofBic() {
        return new BicGenerator();
    }

    /**
     * Returns a generator that produces BBAN values.
     */
    public static BbanGenerator ofBban() {
        return new BbanGenerator();
    }

    /**
     * Returns a generator that produces IBAN values.
     */
    public static IbanGenerator ofIban() {
        return new IbanGenerator();
    }

    /**
     * Returns a generator that produces ABA routing numbers.
     */
    public static AbaRoutingGenerator ofAbaRouting() {
        return new AbaRoutingGenerator();
    }

    /**
     * Returns a generator that produces bank-country alpha-2 codes.
     */
    public static BankCountryGenerator ofBankCountry() {
        return new BankCountryGenerator();
    }

    /**
     * Returns a generator that produces account numbers/names/transaction types.
     */
    public static BankAccountGenerator ofBankAccount() {
        return new BankAccountGenerator();
    }

    /**
     * Returns a generator that produces structured bank payloads.
     */
    public static BankInfoGenerator ofBankInfo() {
        return new BankInfoGenerator();
    }

    /**
     * Returns a generator that produces structured bank payloads for a specific locale.
     */
    public static BankInfoGenerator ofBankInfo(Locale locale) {
        return new BankInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured bank payloads with explicit configuration.
     */
    public static BankInfoGenerator ofBankInfo(GeneratorConfig config) {
        return new BankInfoGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware bank names.
     */
    public static BankNameGenerator ofBankName() {
        return new BankNameGenerator();
    }

    /**
     * Returns a generator that produces locale-aware bank type labels.
     */
    public static BankTypeGenerator ofBankType() {
        return new BankTypeGenerator();
    }

    /**
     * Returns a generator that produces currency codes and metadata.
     */
    public static CurrencyGenerator ofCurrency() {
        return new CurrencyGenerator();
    }

    /**
     * Returns a generator that produces currency pairs.
     */
    public static CurrencyPairGenerator ofCurrencyPair() {
        return new CurrencyPairGenerator();
    }

    /**
     * Returns a generator that produces currency pairs with explicit configuration.
     */
    public static CurrencyPairGenerator ofCurrencyPair(GeneratorConfig config) {
        return new CurrencyPairGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware price-tag strings.
     */
    public static MoneyGenerator ofMoney() {
        return new MoneyGenerator();
    }

    /**
     * Returns a generator that produces card expiration values.
     */
    public static CardExpirationGenerator ofCardExpiration() {
        return new CardExpirationGenerator();
    }

    /**
     * Returns a generator that produces credit card values.
     */
    public static CreditCardGenerator ofCreditCard() {
        return new CreditCardGenerator();
    }

    /**
     * Returns a generator that produces structured credit-card payloads.
     */
    public static CreditCardInfoGenerator ofCreditCardInfo() {
        return new CreditCardInfoGenerator();
    }

    /**
     * Returns a generator that produces structured credit-card payloads for a specific locale.
     */
    public static CreditCardInfoGenerator ofCreditCardInfo(Locale locale) {
        return new CreditCardInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured credit-card payloads with explicit configuration.
     */
    public static CreditCardInfoGenerator ofCreditCardInfo(GeneratorConfig config) {
        return new CreditCardInfoGenerator(config);
    }

    /**
     * Returns a generator that produces valid ISIN codes.
     */
    public static IsinGenerator ofIsin() {
        return new IsinGenerator();
    }

    /**
     * Returns a generator that produces CUSIP values with valid check digits.
     */
    public static CusipGenerator ofCusip() {
        return new CusipGenerator();
    }

    /**
     * Returns a generator that produces US EIN values.
     */
    public static EinGenerator ofEin() {
        return new EinGenerator();
    }

    /**
     * Returns a generator that produces crypto wallet addresses.
     */
    public static CryptoAddressGenerator ofCryptoAddress() {
        return new CryptoAddressGenerator();
    }

    /**
     * Returns a locale-aware national-id generator.
     */
    public static NationalIdGenerator ofNationalId(Locale locale) {
        return new NationalIdGenerator(locale);
    }

    /**
     * Returns a locale-aware seeded national-id generator.
     */
    public static NationalIdGenerator ofNationalId(Locale locale, long seed) {
        return new NationalIdGenerator(locale, seed);
    }

    /**
     * Returns a national-id generator with explicit configuration.
     */
    public static NationalIdGenerator ofNationalId(GeneratorConfig config) {
        return new NationalIdGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware usernames.
     */
    public static UsernameGenerator ofUsername() {
        return new UsernameGenerator();
    }

    /**
     * Returns a generator that produces random passwords with configurable length ranges.
     */
    public static PasswordGenerator ofPassword() {
        return new PasswordGenerator();
    }

    /**
     * Returns a generator that produces avatar image URLs.
     */
    public static AvatarUrlGenerator ofAvatarUrl() {
        return new AvatarUrlGenerator();
    }

    /**
     * Returns a generator that produces semantic version strings (system provider).
     */
    public static VersionGenerator ofVersion() {
        return new VersionGenerator();
    }

    /**
     * Returns a generator that produces platform identifiers.
     */
    public static PlatformIdGenerator ofPlatformId() {
        return new PlatformIdGenerator();
    }

    // ── Lorem Ipsum ───────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces exception-style payload maps.
     */
    public static ExceptionPayloadGenerator ofExceptionPayload() {
        return new ExceptionPayloadGenerator();
    }

    /**
     * Returns a generator that produces database column/type values.
     */
    public static DatabaseGenerator ofDatabase() {
        return new DatabaseGenerator();
    }

    /**
     * Returns a generator that produces Lorem Ipsum sentences (default {@link LoremIpsumGenerator.Mode#SENTENCE}).
     */
    public static LoremIpsumGenerator ofLoremIpsum() {
        return new LoremIpsumGenerator();
    }

    /**
     * Returns a generator that produces Lorem Ipsum text in the specified mode.
     *
     * @param mode {@link LoremIpsumGenerator.Mode#WORD}, {@link LoremIpsumGenerator.Mode#SENTENCE},
     *             or {@link LoremIpsumGenerator.Mode#PARAGRAPH}; must not be {@code null}
     */
    public static LoremIpsumGenerator ofLoremIpsum(LoremIpsumGenerator.Mode mode) {
        return new LoremIpsumGenerator(mode);
    }

    /**
     * Returns a generator that produces natural-looking pseudo-words.
     */
    public static WordGenerator ofWord() {
        return new WordGenerator();
    }

    /**
     * Returns a generator that produces locale-aware pronounceable syllables.
     */
    public static SyllableGenerator ofSyllable() {
        return new SyllableGenerator();
    }

    /**
     * Returns a generator that produces locale-aware pseudo-natural sentences.
     */
    public static SentenceGenerator ofSentence() {
        return new SentenceGenerator();
    }

    /**
     * Returns a generator that produces locale-aware pseudo-natural paragraphs.
     */
    public static ParagraphGenerator ofParagraph() {
        return new ParagraphGenerator();
    }

    /**
     * Returns a generator that produces char-limited text blocks.
     */
    public static TextGenerator ofText() {
        return new TextGenerator();
    }

    // ── MAC address ───────────────────────────────────────────────────────────

    /**
     * Returns a template generator supporting DataFaker-style {@code #} and {@code ?} placeholders.
     */
    public static TemplateStringGenerator ofTemplate(String template) {
        return new TemplateStringGenerator(template);
    }

    // ── ISBN ──────────────────────────────────────────────────────────────────

    /**
     * Returns a seeded template generator supporting DataFaker-style {@code #} and {@code ?} placeholders.
     */
    public static TemplateStringGenerator ofTemplate(String template, long seed) {
        return new TemplateStringGenerator(template, seed);
    }

    /**
     * Returns a generator that produces random MAC addresses ({@code "XX:XX:XX:XX:XX:XX"}).
     */
    public static MacAddressGenerator ofMacAddress() {
        return new MacAddressGenerator();
    }

    /**
     * Returns a generator that produces random ISBN-13 numbers.
     */
    public static IsbnGenerator ofIsbn() {
        return new IsbnGenerator();
    }

    /**
     * Returns a generator that produces random ISBN numbers in the specified format.
     *
     * @param type {@link IsbnGenerator.IsbnType#ISBN_10} or {@link IsbnGenerator.IsbnType#ISBN_13};
     *             must not be {@code null}
     */
    public static IsbnGenerator ofIsbn(IsbnGenerator.IsbnType type) {
        return new IsbnGenerator(type);
    }

    /**
     * Returns a generator that produces UUID values.
     */
    public static UUIDGenerator ofUuid() {
        return new UUIDGenerator();
    }

    /**
     * Returns a generator that produces random hash strings and algorithm digests.
     */
    public static HashGenerator ofHash() {
        return new HashGenerator();
    }

    /**
     * Returns a generator that produces masked identifier strings.
     */
    public static IdentifierMaskGenerator ofIdentifierMask() {
        return new IdentifierMaskGenerator();
    }

    // ── Schema / Field (Mimesis-style bulk generation) ──────────────────────

    /**
     * Returns a generator that produces EAN barcodes.
     */
    public static EanGenerator ofEan() {
        return new EanGenerator();
    }

    /**
     * Returns a generator that produces UPC-A values.
     */
    public static UpcGenerator ofUpc() {
        return new UpcGenerator();
    }

    /**
     * Returns a field resolver with default configuration.
     */
    public static Field ofField() {
        return new Field();
    }

    /**
     * Returns a field resolver for the provided locale.
     */
    public static Field ofField(Locale locale) {
        return new Field(locale);
    }

    /**
     * Returns a field resolver with explicit configuration.
     */
    public static Field ofField(GeneratorConfig config) {
        return new Field(config);
    }

    /**
     * Returns a field resolver backed by an existing lookup registry.
     */
    public static Field ofField(FieldLookup lookup) {
        return new Field(lookup);
    }

    /**
     * Returns an object generator for the given type with default configuration.
     */
    public static <T> ObjectGenerator<T> ofObject(Class<T> type) {
        return new ObjectGenerator<>(type);
    }

    /**
     * Returns an object generator for the given type with shared root configuration.
     */
    public static <T> ObjectGenerator<T> ofObject(Class<T> type, GeneratorConfig config) {
        return new ObjectGenerator<>(type, config);
    }

    /**
     * Returns an object generator for the given type with object-scoped configuration overrides.
     */
    public static <T> ObjectGenerator<T> ofObject(Class<T> type, ObjectGeneratorConfig config) {
        return new ObjectGenerator<>(type, config);
    }

    /**
     * Returns a fluent object faker for the given type with default configuration.
     */
    public static <T> ObjectFaker<T> ofObjectFaker(Class<T> type) {
        return new ObjectFaker<>(type);
    }

    /**
     * Returns a fluent object faker for the given type with shared root configuration.
     */
    public static <T> ObjectFaker<T> ofObjectFaker(Class<T> type, GeneratorConfig config) {
        return new ObjectFaker<>(type, config);
    }

    /**
     * Returns a fluent object faker for the given type with object-scoped configuration overrides.
     */
    public static <T> ObjectFaker<T> ofObjectFaker(Class<T> type, ObjectGeneratorConfig config) {
        return new ObjectFaker<>(type, config);
    }

    /**
     * Returns a schema generator with default configuration.
     */
    public static Schema ofSchema(Map<String, SchemaValueProvider> fields) {
        return new Schema(fields);
    }

    // ── Provider hub (Mimesis-style generic providers) ──────────────────────

    /**
     * Returns a locale-aware schema generator.
     */
    public static Schema ofSchema(Locale locale, Map<String, SchemaValueProvider> fields) {
        return new Schema(locale, fields);
    }

    /**
     * Returns a schema generator with explicit configuration.
     */
    public static Schema ofSchema(GeneratorConfig config, Map<String, SchemaValueProvider> fields) {
        return new Schema(config, fields);
    }

    /**
     * Returns a generic provider hub with default configuration.
     */
    public static ProviderHub ofProviderHub() {
        return new ProviderHub();
    }

    // ── Selection / helper-style generators ──────────────────────────────────

    /**
     * Returns a locale-aware generic provider hub.
     */
    public static ProviderHub ofProviderHub(Locale locale) {
        return new ProviderHub(locale);
    }

    /**
     * Returns a generic provider hub with explicit configuration.
     */
    public static ProviderHub ofProviderHub(GeneratorConfig config) {
        return new ProviderHub(config);
    }

    /**
     * Returns a generic provider hub configured from a named profile.
     */
    public static ProviderHub ofProviderHub(GeneratorProfile profile) {
        return new ProviderHub(profile);
    }

    /**
     * Returns a locale-aware provider hub configured from a named profile.
     */
    public static ProviderHub ofProviderHub(Locale locale, GeneratorProfile profile) {
        return new ProviderHub(locale, profile);
    }

    /**
     * Returns a provider hub with explicit config and profile metadata.
     */
    public static ProviderHub ofProviderHub(GeneratorConfig config, GeneratorProfile profile) {
        return new ProviderHub(config, profile);
    }

    /**
     * Returns a generator that picks one random element from the given source list.
     */
    public static <T> PickGenerator<T> pickFrom(List<T> source) {
        return new PickGenerator<>(source);
    }

    /**
     * Chance-style alias for {@link #pickFrom(List)}.
     */
    public static <T> PickGenerator<T> pick(List<T> source) {
        return pickFrom(source);
    }

    /**
     * Returns a generator that picks {@code count} distinct elements without replacement.
     */
    public static <T> PickSetGenerator<T> pickSetFrom(List<T> source, int count) {
        return new PickSetGenerator<>(source, count);
    }

    /**
     * Chance-style alias for {@link #pickSetFrom(List, int)}.
     */
    public static <T> PickSetGenerator<T> pickset(List<T> source, int count) {
        return pickSetFrom(source, count);
    }

    /**
     * Returns a generator that returns a shuffled copy of the given list.
     */
    public static <T> ShuffleGenerator<T> shuffleOf(List<T> source) {
        return new ShuffleGenerator<>(source);
    }

    /**
     * Chance-style alias for {@link #shuffleOf(List)}.
     */
    public static <T> ShuffleGenerator<T> shuffle(List<T> source) {
        return shuffleOf(source);
    }

    /**
     * Returns a weighted generator that selects values according to positive integer weights.
     */
    public static <T> WeightedGenerator<T> weighted(List<T> values, List<Integer> weights) {
        return new WeightedGenerator<>(values, weights);
    }

    /**
     * Returns a unique-value decorator using {@link Objects#equals(Object, Object)} semantics.
     */
    public static <T> UniqueGenerator<T> unique(Generator<T> source) {
        return new UniqueGenerator<>(source);
    }

    /**
     * DataFaker-style alias for {@link #unique(Generator)}.
     */
    public static <T> UniqueGenerator<T> uniqueValues(Generator<T> source) {
        return unique(source);
    }

    /**
     * Returns a unique-value decorator with bounded attempts for each generated value.
     */
    public static <T> UniqueGenerator<T> unique(Generator<T> source, int maxAttempts) {
        return new UniqueGenerator<>(source, maxAttempts);
    }

    // ── Generic lookup by type ────────────────────────────────────────────────

    /**
     * Returns a unique-value decorator with a custom equality comparator.
     */
    public static <T> UniqueGenerator<T> unique(Generator<T> source, BiPredicate<T, T> comparator) {
        return new UniqueGenerator<>(source, comparator);
    }

    // ── Internal registry ─────────────────────────────────────────────────────

    /**
     * Returns a generator that invokes the given source generator {@code count} times per call.
     */
    public static <T> RepeatGenerator<T> repeat(Generator<T> source, int count) {
        return new RepeatGenerator<>(source, count);
    }

    /**
     * Return a default {@link Generator} for the given Java primitive wrapper class.
     *
     * <p>Supported types: {@code Byte}, {@code Short}, {@code Integer}, {@code Long},
     * {@code Float}, {@code Double}, {@code Character}, {@code Boolean}, {@code String}.
     *
     * @param type the wrapper class; must not be {@code null}
     * @throws IllegalArgumentException if the type has no built-in generator
     */
    @SuppressWarnings("unchecked")
    public static <T> Generator<T> forType(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        Supplier<?> factory = REGISTRY.get(type);
        if (factory == null) {
            throw new IllegalArgumentException(
                "No built-in generator for type: " + type.getName() +
                ". Register a custom Generator or use one of the of*() methods.");
        }
        return (Generator<T>) factory.get();
    }
}
