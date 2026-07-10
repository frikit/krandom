/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.algorithms.FibonacciGenerator;
import io.github.frikit.krandom.generator.algorithms.LuhnGenerator;
import io.github.frikit.krandom.generator.base.AtomicIntegerGenerator;
import io.github.frikit.krandom.generator.base.AtomicLongGenerator;
import io.github.frikit.krandom.generator.base.BigDecimalGenerator;
import io.github.frikit.krandom.generator.base.BigIntegerGenerator;
import io.github.frikit.krandom.generator.base.BooleanGenerator;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.CharGenerator;
import io.github.frikit.krandom.generator.base.ConstantGenerator;
import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.base.DoubleGenerator;
import io.github.frikit.krandom.generator.base.FloatGenerator;
import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.NaturalNumberGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.base.NullableBooleanGenerator;
import io.github.frikit.krandom.generator.base.NumberGenerator;
import io.github.frikit.krandom.generator.base.NumberWithFormatGenerator;
import io.github.frikit.krandom.generator.base.PrimeGenerator;
import io.github.frikit.krandom.generator.base.PyDecimalGenerator;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.color.ColorGenerator;
import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.database.DatabaseGenerator;
import io.github.frikit.krandom.generator.datetime.CalendarGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.DurationGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.InstantGenerator;
import io.github.frikit.krandom.generator.datetime.LegacyTimeZoneGenerator;
import io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.MonthDayGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetTimeGenerator;
import io.github.frikit.krandom.generator.datetime.PeriodGenerator;
import io.github.frikit.krandom.generator.datetime.SqlDateGenerator;
import io.github.frikit.krandom.generator.datetime.SqlTimeGenerator;
import io.github.frikit.krandom.generator.datetime.SqlTimestampGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.datetime.TimezoneGenerator;
import io.github.frikit.krandom.generator.datetime.UtilDateGenerator;
import io.github.frikit.krandom.generator.datetime.YearGenerator;
import io.github.frikit.krandom.generator.datetime.YearMonthGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneIdGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneOffsetGenerator;
import io.github.frikit.krandom.generator.datetime.ZonedDateTimeGenerator;
import io.github.frikit.krandom.generator.file.DirPathGenerator;
import io.github.frikit.krandom.generator.file.FileExtensionGenerator;
import io.github.frikit.krandom.generator.file.FileNameGenerator;
import io.github.frikit.krandom.generator.file.FilePathGenerator;
import io.github.frikit.krandom.generator.file.MimeTypeGenerator;
import io.github.frikit.krandom.generator.file.SemverGenerator;
import io.github.frikit.krandom.generator.finance.AbaRoutingGenerator;
import io.github.frikit.krandom.generator.finance.BankAccountGenerator;
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
import io.github.frikit.krandom.generator.finance.CryptoAddressGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyPairGenerator;
import io.github.frikit.krandom.generator.finance.CusipGenerator;
import io.github.frikit.krandom.generator.finance.EinGenerator;
import io.github.frikit.krandom.generator.finance.FinancialTermGenerator;
import io.github.frikit.krandom.generator.finance.IbanGenerator;
import io.github.frikit.krandom.generator.finance.IsinGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.games.coin.CoinGenerator;
import io.github.frikit.krandom.generator.games.dice.DiceGenerator;
import io.github.frikit.krandom.generator.games.dice.DiceType;
import io.github.frikit.krandom.generator.identifier.EanGenerator;
import io.github.frikit.krandom.generator.identifier.HashGenerator;
import io.github.frikit.krandom.generator.identifier.IdentifierMaskGenerator;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.identifier.UpcGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.GeohashGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.locale.RandomLocaleGenerator;
import io.github.frikit.krandom.generator.namespace.CommerceGenerators;
import io.github.frikit.krandom.generator.namespace.DateTimeGenerators;
import io.github.frikit.krandom.generator.namespace.FinanceGenerators;
import io.github.frikit.krandom.generator.namespace.IdentifierGenerators;
import io.github.frikit.krandom.generator.namespace.LocationGenerators;
import io.github.frikit.krandom.generator.namespace.NetworkGenerators;
import io.github.frikit.krandom.generator.namespace.PersonGenerators;
import io.github.frikit.krandom.generator.namespace.TextGenerators;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.HttpMethodGenerator;
import io.github.frikit.krandom.generator.network.HttpStatusCodeGenerator;
import io.github.frikit.krandom.generator.network.IPGenerator;
import io.github.frikit.krandom.generator.network.IPv4Generator;
import io.github.frikit.krandom.generator.network.IPv6Generator;
import io.github.frikit.krandom.generator.network.MacAddressGenerator;
import io.github.frikit.krandom.generator.network.PortGenerator;
import io.github.frikit.krandom.generator.network.SlugGenerator;
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.network.UriGenerator;
import io.github.frikit.krandom.generator.network.UserAgentGenerator;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.DataFakerExpressionGenerator;
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
import io.github.frikit.krandom.generator.system.ExceptionPayloadGenerator;
import io.github.frikit.krandom.generator.system.PlatformIdGenerator;
import io.github.frikit.krandom.generator.system.VersionGenerator;
import io.github.frikit.krandom.generator.text.LoremIpsumGenerator;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.ProviderTemplateGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.SyllableGenerator;
import io.github.frikit.krandom.generator.text.TemplateStringGenerator;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.AvatarUrlGenerator;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyEmailGenerator;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.EducationalAttainmentGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobFieldGenerator;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.commerce.CnpjGenerator;
import io.github.frikit.krandom.generator.tech.ProgrammingLanguageGenerator;
import io.github.frikit.krandom.generator.tech.AwsGenerator;
import io.github.frikit.krandom.generator.tech.AzureGenerator;
import io.github.frikit.krandom.generator.tech.ComputerGenerator;
import io.github.frikit.krandom.generator.text.NatoPhoneticGenerator;
import io.github.frikit.krandom.generator.user.HobbyGenerator;
import io.github.frikit.krandom.generator.user.MbtiGenerator;
import io.github.frikit.krandom.generator.vehicle.VehicleGenerator;
import io.github.frikit.krandom.generator.vehicle.VinGenerator;
import io.github.frikit.krandom.generator.measurement.MeasurementGenerator;
import io.github.frikit.krandom.generator.weather.WeatherGenerator;
import io.github.frikit.krandom.generator.user.BloodTypeGenerator;
import io.github.frikit.krandom.generator.user.ChineseZodiacGenerator;
import io.github.frikit.krandom.generator.user.PronounGenerator;
import io.github.frikit.krandom.generator.user.ZodiacGenerator;
import io.github.frikit.krandom.generator.user.MaritalStatusGenerator;
import io.github.frikit.krandom.generator.user.NationalityGenerator;
import io.github.frikit.krandom.generator.user.MiddleNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PassportGenerator;
import io.github.frikit.krandom.generator.user.DrivingLicenseGenerator;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.PositionGenerator;
import io.github.frikit.krandom.generator.user.ProfessionGenerator;
import io.github.frikit.krandom.generator.user.ProfileGenerator;
import io.github.frikit.krandom.generator.user.SeniorityGenerator;
import io.github.frikit.krandom.generator.user.SimpleProfileGenerator;
import io.github.frikit.krandom.generator.user.SocialHandleGenerator;
import io.github.frikit.krandom.generator.user.SocialProfileGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Static factory for all built-in base-type generators.
 *
 * <p>Every {@code of*()} method has two forms:
 * <ul>
 *   <li>No-arg — default range / default character set, uses the default fast PRNG.</li>
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
 *
 * <p><strong>Thread safety:</strong> Generators are <em>not thread-safe</em>. Each instance wraps a mutable
 * {@link java.util.Random} whose state changes on every call. Never share a generator
 * across threads without synchronization. Use {@link #threadLocal(java.util.function.Supplier)}
 * to get a thread-confined wrapper for concurrent scenarios.
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
        REGISTRY.put(Number.class, NumberGenerator::new);
        REGISTRY.put(BigDecimal.class, BigDecimalGenerator::new);
        REGISTRY.put(BigInteger.class, BigIntegerGenerator::new);
        REGISTRY.put(AtomicInteger.class, AtomicIntegerGenerator::new);
        REGISTRY.put(AtomicLong.class, AtomicLongGenerator::new);
        REGISTRY.put(Character.class, CharGenerator::letters);
        REGISTRY.put(char.class, CharGenerator::letters);
        REGISTRY.put(Boolean.class, BooleanGenerator::new);
        REGISTRY.put(boolean.class, BooleanGenerator::new);
        REGISTRY.put(String.class, StringGenerator::letters);
        REGISTRY.put(Calendar.class, CalendarGenerator::new);
        REGISTRY.put(GregorianCalendar.class, CalendarGenerator::new);
        REGISTRY.put(Locale.class, RandomLocaleGenerator::new);
        REGISTRY.put(UUID.class, UUIDGenerator::new);
        REGISTRY.put(URI.class, Generators::ofURI);
        REGISTRY.put(java.net.URL.class, Generators::ofURL);
        REGISTRY.put(java.util.Date.class, UtilDateGenerator::new);
        REGISTRY.put(java.sql.Date.class, SqlDateGenerator::new);
        REGISTRY.put(java.sql.Time.class, SqlTimeGenerator::new);
        REGISTRY.put(java.sql.Timestamp.class, SqlTimestampGenerator::new);
        REGISTRY.put(LocalDate.class, DateGenerator::new);
        REGISTRY.put(LocalTime.class, TimeGenerator::new);
        REGISTRY.put(LocalDateTime.class, LocalDateTimeGenerator::new);
        REGISTRY.put(Instant.class, InstantGenerator::new);
        REGISTRY.put(OffsetDateTime.class, OffsetDateTimeGenerator::new);
        REGISTRY.put(OffsetTime.class, OffsetTimeGenerator::new);
        REGISTRY.put(ZonedDateTime.class, ZonedDateTimeGenerator::new);
        REGISTRY.put(Year.class, YearGenerator::new);
        REGISTRY.put(YearMonth.class, YearMonthGenerator::new);
        REGISTRY.put(MonthDay.class, MonthDayGenerator::new);
        REGISTRY.put(Duration.class, DurationGenerator::new);
        REGISTRY.put(Period.class, PeriodGenerator::new);
        REGISTRY.put(ZoneId.class, ZoneIdGenerator::new);
        REGISTRY.put(ZoneOffset.class, ZoneOffsetGenerator::new);
        REGISTRY.put(TimeZone.class, LegacyTimeZoneGenerator::new);
    }

    private Generators() { /* static utility */ }

    /**
     * Returns a generator that always returns the same value.
     *
     * @param value value returned by every generated call; may be {@code null}
     * @param <T> generated value type
     * @return constant generator
     * @deprecated use {@link #ofConstant(Object)}; this compatibility alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static <T> ConstantGenerator<T> constant(T value) {
        return ofConstant(value);
    }

    /**
     * Returns a generator that always returns the same value.
     *
     * @param value value returned by every generated call; may be {@code null}
     * @param <T> generated value type
     * @return constant generator
     */
    public static <T> ConstantGenerator<T> ofConstant(T value) {
        return new ConstantGenerator<>(value);
    }

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

    public static NumberGenerator ofNumber() {
        return new NumberGenerator();
    }

    public static NumberGenerator ofNumber(long seed) {
        return new NumberGenerator(seed);
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

    public static AtomicIntegerGenerator ofAtomicInteger() {
        return new AtomicIntegerGenerator();
    }

    public static AtomicIntegerGenerator ofAtomicInteger(int min, int max) {
        return new AtomicIntegerGenerator(min, max);
    }

    public static AtomicIntegerGenerator ofAtomicInteger(int min, int max, long seed) {
        return new AtomicIntegerGenerator(min, max, seed);
    }

    public static AtomicLongGenerator ofAtomicLong() {
        return new AtomicLongGenerator();
    }

    public static AtomicLongGenerator ofAtomicLong(long min, long max) {
        return new AtomicLongGenerator(min, max);
    }

    public static AtomicLongGenerator ofAtomicLong(long min, long max, long seed) {
        return new AtomicLongGenerator(min, max, seed);
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

    /**
     * Returns a generator that produces strings matching the supported subset of regular expressions.
     */
    public static RegexGenerator ofRegex(String pattern) {
        return new RegexGenerator(pattern);
    }

    /**
     * Returns a deterministic generator that produces strings matching the supported subset of regular expressions.
     */
    public static RegexGenerator ofRegex(String pattern, long seed) {
        return new RegexGenerator(pattern, seed);
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

    /**
     * Returns a generator that produces {@link URI} values.
     */
    public static Generator<URI> ofURI() {
        UriGenerator generator = new UriGenerator();
        return () -> URI.create(generator.generate());
    }

    /**
     * Returns a generator that produces {@link URI} values using explicit configuration.
     */
    public static Generator<URI> ofURI(GeneratorConfig config) {
        UriGenerator generator = new UriGenerator(config);
        return () -> URI.create(generator.generate());
    }

    /**
     * Returns a generator that produces {@link java.net.URL} values.
     */
    public static Generator<java.net.URL> ofURL() {
        URLGenerator generator = new URLGenerator();
        return () -> toUrl(URI.create(generator.generate("https")));
    }

    /**
     * Returns a generator that produces {@link java.net.URL} values using explicit configuration.
     */
    public static Generator<java.net.URL> ofURL(GeneratorConfig config) {
        URLGenerator generator = new URLGenerator(config);
        return () -> toUrl(URI.create(generator.generate("https")));
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
     * Returns a generator that produces random {@link java.util.Date} values.
     */
    public static UtilDateGenerator ofUtilDate() {
        return new UtilDateGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.sql.Date} values.
     */
    public static SqlDateGenerator ofSqlDate() {
        return new SqlDateGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.sql.Time} values.
     */
    public static SqlTimeGenerator ofSqlTime() {
        return new SqlTimeGenerator();
    }

    /**
     * Returns a generator that produces random {@link java.sql.Timestamp} values.
     */
    public static SqlTimestampGenerator ofSqlTimestamp() {
        return new SqlTimestampGenerator();
    }

    /**
     * Returns a generator that produces random {@link LocalTime} values.
     */
    public static TimeGenerator ofLocalTime() {
        return new TimeGenerator();
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
     * Returns a generator that produces random {@link java.time.OffsetDateTime} values.
     */
    public static OffsetDateTimeGenerator ofOffsetDateTime() {
        return new OffsetDateTimeGenerator();
    }

    /**
     * Returns a generator that produces random {@link OffsetTime} values.
     */
    public static OffsetTimeGenerator ofOffsetTime() {
        return new OffsetTimeGenerator();
    }

    /**
     * Returns a generator that produces random {@link Year} values.
     */
    public static YearGenerator ofYear() {
        return new YearGenerator();
    }

    /**
     * Returns a generator that produces random {@link YearMonth} values.
     */
    public static YearMonthGenerator ofYearMonth() {
        return new YearMonthGenerator();
    }

    /**
     * Returns a generator that produces random {@link MonthDay} values.
     */
    public static MonthDayGenerator ofMonthDay() {
        return new MonthDayGenerator();
    }

    /**
     * Returns a generator that produces random {@link Period} values.
     */
    public static PeriodGenerator ofPeriod() {
        return new PeriodGenerator();
    }

    /**
     * Returns a generator that produces random {@link ZoneId} values.
     */
    public static ZoneIdGenerator ofZoneId() {
        return new ZoneIdGenerator();
    }

    /**
     * Returns a generator that produces random {@link ZoneOffset} values.
     */
    public static ZoneOffsetGenerator ofZoneOffset() {
        return new ZoneOffsetGenerator();
    }

    /**
     * Returns a generator that produces random legacy {@link TimeZone} values.
     */
    public static LegacyTimeZoneGenerator ofTimeZone() {
        return new LegacyTimeZoneGenerator();
    }

    /**
     * Returns a generator that produces timezone identifiers.
     */
    public static TimezoneGenerator ofTimezone() {
        return new TimezoneGenerator();
    }

    /**
     * Returns a generator that produces legacy {@link Calendar} values.
     */
    public static CalendarGenerator ofCalendar() {
        return new CalendarGenerator();
    }

    /**
     * Returns a deterministic generator that produces legacy {@link Calendar} values.
     */
    public static CalendarGenerator ofCalendar(long seed) {
        return new CalendarGenerator(seed);
    }

    /**
     * Returns a generator that produces legacy {@link Calendar} values using explicit configuration.
     */
    public static CalendarGenerator ofCalendar(GeneratorConfig config) {
        return new CalendarGenerator(config);
    }

    /**
     * Returns a generator that produces legacy {@link Calendar} values in the provided date range.
     */
    public static CalendarGenerator ofCalendar(LocalDate min, LocalDate max) {
        return new CalendarGenerator(min, max);
    }

    /**
     * Returns a generator that produces legacy {@link Calendar} values in the provided date range.
     */
    public static CalendarGenerator ofCalendar(LocalDate min, LocalDate max, GeneratorConfig config) {
        return new CalendarGenerator(min, max, config);
    }

    /**
     * Returns a generator that produces locales from the built-in supported locale catalog.
     */
    public static RandomLocaleGenerator ofLocale() {
        return new RandomLocaleGenerator();
    }

    /**
     * Returns a deterministic generator that produces locales from the built-in supported locale catalog.
     */
    public static RandomLocaleGenerator ofLocale(long seed) {
        return new RandomLocaleGenerator(seed);
    }

    /**
     * Returns a generator that produces locales from the built-in supported locale catalog.
     */
    public static RandomLocaleGenerator ofLocale(GeneratorConfig config) {
        return new RandomLocaleGenerator(config);
    }

    /**
     * Returns a generator that produces random full names (first + last) in {@link java.util.Locale#US}.
     */
    public static FullNameGenerator ofFullName() {
        return new FullNameGenerator();
    }

    /**
     * Returns a generator that produces locale-aware full names for a specific locale.
     */
    public static FullNameGenerator ofFullName(Locale locale) {
        return new FullNameGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware full names with explicit configuration.
     */
    public static FullNameGenerator ofFullName(GeneratorConfig config) {
        return new FullNameGenerator(config);
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
     * Returns a generator that produces locale-aware email addresses for a specific locale.
     */
    public static EmailGenerator ofEmail(Locale locale) {
        return new EmailGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware email addresses with explicit configuration.
     */
    public static EmailGenerator ofEmail(GeneratorConfig config) {
        return new EmailGenerator(config);
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
     * Returns a generator that produces company email addresses for a specific locale.
     */
    public static CompanyEmailGenerator ofCompanyEmail(Locale locale) {
        return new CompanyEmailGenerator(locale);
    }

    /**
     * Returns a generator that produces company email addresses with explicit configuration.
     */
    public static CompanyEmailGenerator ofCompanyEmail(GeneratorConfig config) {
        return new CompanyEmailGenerator(config);
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
     * Returns a generator that produces simple user profiles for a specific locale.
     */
    public static SimpleProfileGenerator ofSimpleProfile(Locale locale) {
        return new SimpleProfileGenerator(locale);
    }

    /**
     * Returns a generator that produces simple user profiles with explicit configuration.
     */
    public static SimpleProfileGenerator ofSimpleProfile(GeneratorConfig config) {
        return new SimpleProfileGenerator(config);
    }

    /**
     * Returns a generator that produces extended user profiles.
     */
    public static ProfileGenerator ofProfile() {
        return new ProfileGenerator();
    }

    /**
     * Returns a generator that produces extended user profiles for a specific locale.
     */
    public static ProfileGenerator ofProfile(Locale locale) {
        return new ProfileGenerator(locale);
    }

    /**
     * Returns a generator that produces extended user profiles with explicit configuration.
     */
    public static ProfileGenerator ofProfile(GeneratorConfig config) {
        return new ProfileGenerator(config);
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
     * Returns a generator that produces social-media style handles for a specific locale.
     */
    public static SocialHandleGenerator ofSocialHandle(Locale locale) {
        return new SocialHandleGenerator(locale);
    }

    /**
     * Returns a generator that produces social-media style handles with explicit configuration.
     */
    public static SocialHandleGenerator ofSocialHandle(GeneratorConfig config) {
        return new SocialHandleGenerator(config);
    }

    /**
     * Returns a generator that produces social-media style profiles.
     */
    public static SocialProfileGenerator ofSocialProfile() {
        return new SocialProfileGenerator();
    }

    /**
     * Returns a generator that produces social-media style profiles for a specific locale.
     */
    public static SocialProfileGenerator ofSocialProfile(Locale locale) {
        return new SocialProfileGenerator(locale);
    }

    /**
     * Returns a generator that produces social-media style profiles with explicit configuration.
     */
    public static SocialProfileGenerator ofSocialProfile(GeneratorConfig config) {
        return new SocialProfileGenerator(config);
    }

    /**
     * Returns a generator that produces random US-style street addresses (e.g. {@code "123 Oak Ave"}).
     */
    public static StreetAddressGenerator ofStreetAddress() {
        return new StreetAddressGenerator();
    }

    /**
     * Returns a generator that produces street addresses for a specific locale.
     */
    public static StreetAddressGenerator ofStreetAddress(Locale locale) {
        return new StreetAddressGenerator(locale);
    }

    /**
     * Returns a generator that produces street addresses with explicit configuration.
     */
    public static StreetAddressGenerator ofStreetAddress(GeneratorConfig config) {
        return new StreetAddressGenerator(config);
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
     * Returns a generator that produces city names for a specific locale.
     */
    public static CityGenerator ofCity(Locale locale) {
        return new CityGenerator(locale);
    }

    /**
     * Returns a generator that produces city names with explicit configuration.
     */
    public static CityGenerator ofCity(GeneratorConfig config) {
        return new CityGenerator(config);
    }

    /**
     * Returns a generator that produces state/province names.
     */
    public static StateGenerator ofState() {
        return new StateGenerator();
    }

    /**
     * Returns a generator that produces state/province names for a specific locale.
     */
    public static StateGenerator ofState(Locale locale) {
        return new StateGenerator(locale);
    }

    /**
     * Returns a generator that produces state/province names with explicit configuration.
     */
    public static StateGenerator ofState(GeneratorConfig config) {
        return new StateGenerator(config);
    }

    /**
     * Returns a generator that produces postal codes.
     */
    public static PostalCodeGenerator ofPostalCode() {
        return new PostalCodeGenerator();
    }

    /**
     * Returns a generator that produces postal codes for a specific locale.
     */
    public static PostalCodeGenerator ofPostalCode(Locale locale) {
        return new PostalCodeGenerator(locale);
    }

    /**
     * Returns a generator that produces postal codes with explicit configuration.
     */
    public static PostalCodeGenerator ofPostalCode(GeneratorConfig config) {
        return new PostalCodeGenerator(config);
    }

    // ── Company name ──────────────────────────────────────────────────────────

    /**
     * Returns a generator that produces country names and country codes.
     */
    public static CountryGenerator ofCountry() {
        return new CountryGenerator();
    }

    /**
     * Returns a generator that produces country names and country codes for a specific locale.
     */
    public static CountryGenerator ofCountry(Locale locale) {
        return new CountryGenerator(locale);
    }

    /**
     * Returns a generator that produces country names and country codes with explicit configuration.
     */
    public static CountryGenerator ofCountry(GeneratorConfig config) {
        return new CountryGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware phone numbers.
     */
    public static PhoneNumberGenerator ofPhoneNumber() {
        return new PhoneNumberGenerator();
    }

    /**
     * Returns a generator that produces locale-aware phone numbers for a specific locale.
     */
    public static PhoneNumberGenerator ofPhoneNumber(Locale locale) {
        return new PhoneNumberGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware phone numbers with explicit configuration.
     */
    public static PhoneNumberGenerator ofPhoneNumber(GeneratorConfig config) {
        return new PhoneNumberGenerator(config);
    }

    /**
     * Returns a generator that produces geohashes from locale-aware coordinates.
     */
    public static GeohashGenerator ofGeohash() {
        return new GeohashGenerator();
    }

    /**
     * Returns a generator that produces geohashes with explicit precision.
     */
    public static GeohashGenerator ofGeohash(int precision) {
        return new GeohashGenerator(precision);
    }

    /**
     * Returns a deterministic generator that produces geohashes with default precision.
     */
    public static GeohashGenerator ofGeohash(long seed) {
        return new GeohashGenerator(seed);
    }

    /**
     * Returns a deterministic generator that produces geohashes with explicit precision.
     */
    public static GeohashGenerator ofGeohash(int precision, long seed) {
        return new GeohashGenerator(precision, seed);
    }

    /**
     * Returns a generator that produces geohashes using explicit configuration.
     */
    public static GeohashGenerator ofGeohash(GeneratorConfig config) {
        return new GeohashGenerator(config);
    }

    /**
     * Returns a generator that produces geohashes with explicit precision and configuration.
     */
    public static GeohashGenerator ofGeohash(int precision, GeneratorConfig config) {
        return new GeohashGenerator(precision, config);
    }

    /**
     * Returns a generator that produces random company names including a legal-form suffix.
     */
    public static CompanyNameGenerator ofCompanyName() {
        return new CompanyNameGenerator();
    }

    /**
     * Returns a generator that produces company names for a specific locale configuration.
     */
    public static CompanyNameGenerator ofCompanyName(Locale locale) {
        return new CompanyNameGenerator(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Returns a generator that produces company names with explicit configuration.
     */
    public static CompanyNameGenerator ofCompanyName(GeneratorConfig config) {
        return new CompanyNameGenerator(config);
    }

    /**
     * Returns a generator that produces company website URLs.
     */
    public static CompanyUrlGenerator ofCompanyUrl() {
        return new CompanyUrlGenerator();
    }

    /**
     * Returns a generator that produces company website URLs for a specific locale.
     */
    public static CompanyUrlGenerator ofCompanyUrl(Locale locale) {
        return new CompanyUrlGenerator(locale);
    }

    /**
     * Returns a generator that produces company website URLs with explicit configuration.
     */
    public static CompanyUrlGenerator ofCompanyUrl(GeneratorConfig config) {
        return new CompanyUrlGenerator(config);
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
     * Returns a generator that produces locale-weighted blood types (e.g. {@code "O+"}).
     */
    public static BloodTypeGenerator ofBloodType() {
        return new BloodTypeGenerator();
    }

    /**
     * Returns a blood-type generator weighted by the given locale's distribution.
     */
    public static BloodTypeGenerator ofBloodType(Locale locale) {
        return new BloodTypeGenerator(locale);
    }

    /**
     * Returns a blood-type generator configured by the given {@link GeneratorConfig}.
     */
    public static BloodTypeGenerator ofBloodType(GeneratorConfig config) {
        return new BloodTypeGenerator(config);
    }

    /**
     * Returns a generator that produces Western zodiac signs (e.g. {@code "Scorpio"}).
     */
    public static ZodiacGenerator ofZodiac() {
        return new ZodiacGenerator();
    }

    /**
     * Returns a Western zodiac generator whose sign names are localized for the given locale.
     */
    public static ZodiacGenerator ofZodiac(Locale locale) {
        return new ZodiacGenerator(locale);
    }

    /**
     * Returns a Western zodiac generator configured by the given {@link GeneratorConfig}.
     */
    public static ZodiacGenerator ofZodiac(GeneratorConfig config) {
        return new ZodiacGenerator(config);
    }

    /**
     * Returns a generator that produces Chinese zodiac animals (e.g. {@code "Dragon"}).
     */
    public static ChineseZodiacGenerator ofChineseZodiac() {
        return new ChineseZodiacGenerator();
    }

    /**
     * Returns a Chinese zodiac generator whose animal names are localized for the given locale.
     */
    public static ChineseZodiacGenerator ofChineseZodiac(Locale locale) {
        return new ChineseZodiacGenerator(locale);
    }

    /**
     * Returns a Chinese zodiac generator configured by the given {@link GeneratorConfig}.
     */
    public static ChineseZodiacGenerator ofChineseZodiac(GeneratorConfig config) {
        return new ChineseZodiacGenerator(config);
    }

    /**
     * Returns a generator that produces NATO phonetic alphabet code words (e.g. {@code "Bravo"}).
     */
    public static NatoPhoneticGenerator ofNatoPhonetic() {
        return new NatoPhoneticGenerator();
    }

    /**
     * Returns a NATO phonetic generator configured by the given {@link GeneratorConfig}.
     */
    public static NatoPhoneticGenerator ofNatoPhonetic(GeneratorConfig config) {
        return new NatoPhoneticGenerator(config);
    }

    /**
     * Returns a generator that produces pronoun sets (e.g. {@code "they/them"}).
     */
    public static PronounGenerator ofPronoun() {
        return new PronounGenerator();
    }

    /**
     * Returns a pronoun generator whose sets are localized for the given locale.
     */
    public static PronounGenerator ofPronoun(Locale locale) {
        return new PronounGenerator(locale);
    }

    /**
     * Returns a pronoun generator configured by the given {@link GeneratorConfig}.
     */
    public static PronounGenerator ofPronoun(GeneratorConfig config) {
        return new PronounGenerator(config);
    }

    /**
     * Returns a CNPJ generator using the default configuration, which fails closed.
     *
     * <p>Pass a configuration with an explicit corporate tax-identifier safety policy to generate
     * an isolated compatibility fixture.
     */
    public static CnpjGenerator ofCnpj() {
        return new CnpjGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a CNPJ generator configured by the given {@link GeneratorConfig}.
     */
    public static CnpjGenerator ofCnpj(GeneratorConfig config) {
        return new CnpjGenerator(config);
    }

    /**
     * Returns a fail-closed generator for Brazilian individual tax IDs (CPF).
     *
     * <p>Use {@link #ofNationalId(GeneratorConfig)} with
     * {@code nationalIdSafetyPolicy(REALISTIC_UNCLASSIFIED)} only for isolated compatibility
     * fixtures.
     */
    public static NationalIdGenerator ofCpf() {
        return ofNationalId(GeneratorConfig.builder().locale(Locale.of("pt", "BR")).build());
    }

    /**
     * Returns a fail-closed seeded CPF generator.
     */
    public static NationalIdGenerator ofCpf(long seed) {
        return ofNationalId(GeneratorConfig.builder().locale(Locale.of("pt", "BR")).seed(seed).build());
    }

    /**
     * Returns a generator that produces 17-character VINs with a valid check digit.
     */
    public static VinGenerator ofVin() {
        return new VinGenerator();
    }

    /**
     * Returns a VIN generator configured by the given {@link GeneratorConfig}.
     */
    public static VinGenerator ofVin(GeneratorConfig config) {
        return new VinGenerator(config);
    }

    /**
     * Returns a generator that produces vehicle make/model pairs and license plates.
     */
    public static VehicleGenerator ofVehicle() {
        return new VehicleGenerator();
    }

    /**
     * Returns a vehicle generator configured by the given {@link GeneratorConfig}.
     */
    public static VehicleGenerator ofVehicle(GeneratorConfig config) {
        return new VehicleGenerator(config);
    }

    /**
     * Returns a generator that produces MBTI personality types (e.g. {@code "INTJ"}).
     */
    public static MbtiGenerator ofMbti() {
        return new MbtiGenerator();
    }

    /**
     * Returns an MBTI generator configured by the given {@link GeneratorConfig}.
     */
    public static MbtiGenerator ofMbti(GeneratorConfig config) {
        return new MbtiGenerator(config);
    }

    /**
     * Returns a generator that produces hobby names (e.g. {@code "Photography"}).
     */
    public static HobbyGenerator ofHobby() {
        return new HobbyGenerator();
    }

    /**
     * Returns a hobby generator whose names are localized for the given locale.
     */
    public static HobbyGenerator ofHobby(Locale locale) {
        return new HobbyGenerator(locale);
    }

    /**
     * Returns a hobby generator configured by the given {@link GeneratorConfig}.
     */
    public static HobbyGenerator ofHobby(GeneratorConfig config) {
        return new HobbyGenerator(config);
    }

    /**
     * Returns a generator that produces programming language names (e.g. {@code "Kotlin"}).
     */
    public static ProgrammingLanguageGenerator ofProgrammingLanguage() {
        return new ProgrammingLanguageGenerator();
    }

    /**
     * Returns a programming language generator configured by the given {@link GeneratorConfig}.
     */
    public static ProgrammingLanguageGenerator ofProgrammingLanguage(GeneratorConfig config) {
        return new ProgrammingLanguageGenerator(config);
    }

    /**
     * Returns a passport generator using the default configuration, which fails closed.
     *
     * <p>Pass a configuration with an explicit identity-document safety policy to generate an
     * isolated compatibility fixture.
     */
    public static PassportGenerator ofPassport() {
        return new PassportGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a passport generator configured by the given {@link GeneratorConfig}.
     */
    public static PassportGenerator ofPassport(GeneratorConfig config) {
        return new PassportGenerator(config);
    }

    /**
     * Returns a driving-license generator using the default configuration, which fails closed.
     *
     * <p>Pass a configuration with an explicit identity-document safety policy to generate an
     * isolated compatibility fixture.
     */
    public static DrivingLicenseGenerator ofDrivingLicense() {
        return new DrivingLicenseGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a driving-license generator configured by the given {@link GeneratorConfig}.
     */
    public static DrivingLicenseGenerator ofDrivingLicense(GeneratorConfig config) {
        return new DrivingLicenseGenerator(config);
    }

    /**
     * Returns a generator that produces AWS resource identifiers (regions, instance IDs, S3 buckets).
     */
    public static AwsGenerator ofAws() {
        return new AwsGenerator();
    }

    /**
     * Returns an AWS resource generator configured by the given {@link GeneratorConfig}.
     */
    public static AwsGenerator ofAws(GeneratorConfig config) {
        return new AwsGenerator(config);
    }

    /**
     * Returns a generator that produces Azure resource identifiers (regions, resource groups).
     */
    public static AzureGenerator ofAzure() {
        return new AzureGenerator();
    }

    /**
     * Returns an Azure resource generator configured by the given {@link GeneratorConfig}.
     */
    public static AzureGenerator ofAzure(GeneratorConfig config) {
        return new AzureGenerator(config);
    }

    /**
     * Returns a generator that produces computer values (operating systems, platforms, device types).
     */
    public static ComputerGenerator ofComputer() {
        return new ComputerGenerator();
    }

    /**
     * Returns a computer generator configured by the given {@link GeneratorConfig}.
     */
    public static ComputerGenerator ofComputer(GeneratorConfig config) {
        return new ComputerGenerator(config);
    }

    /**
     * Returns a generator that produces weather conditions (e.g. {@code "Sunny"}).
     */
    public static WeatherGenerator ofWeather() {
        return new WeatherGenerator();
    }

    /**
     * Returns a weather generator whose condition names are localized for the given locale.
     */
    public static WeatherGenerator ofWeather(Locale locale) {
        return new WeatherGenerator(locale);
    }

    /**
     * Returns a weather generator configured by the given {@link GeneratorConfig}.
     */
    public static WeatherGenerator ofWeather(GeneratorConfig config) {
        return new WeatherGenerator(config);
    }

    /**
     * Returns a generator that produces measurement-unit names (e.g. {@code "Kilometer"}).
     */
    public static MeasurementGenerator ofMeasurement() {
        return new MeasurementGenerator();
    }

    /**
     * Returns a measurement generator whose unit names are localized for the given locale.
     */
    public static MeasurementGenerator ofMeasurement(Locale locale) {
        return new MeasurementGenerator(locale);
    }

    /**
     * Returns a measurement generator configured by the given {@link GeneratorConfig}.
     */
    public static MeasurementGenerator ofMeasurement(GeneratorConfig config) {
        return new MeasurementGenerator(config);
    }

    /**
     * Returns a generator that produces financial-term names (e.g. {@code "Dividend"}).
     */
    public static FinancialTermGenerator ofFinancialTerm() {
        return new FinancialTermGenerator();
    }

    /**
     * Returns a financial-term generator whose term names are localized for the given locale.
     */
    public static FinancialTermGenerator ofFinancialTerm(Locale locale) {
        return new FinancialTermGenerator(locale);
    }

    /**
     * Returns a financial-term generator configured by the given {@link GeneratorConfig}.
     */
    public static FinancialTermGenerator ofFinancialTerm(GeneratorConfig config) {
        return new FinancialTermGenerator(config);
    }

    /**
     * Returns a generator that produces nationality demonyms (e.g. {@code "French"}).
     */
    public static NationalityGenerator ofNationality() {
        return new NationalityGenerator();
    }

    /**
     * Returns a nationality generator whose demonyms are localized for the given locale.
     */
    public static NationalityGenerator ofNationality(Locale locale) {
        return new NationalityGenerator(locale);
    }

    /**
     * Returns a nationality generator configured by the given {@link GeneratorConfig}.
     */
    public static NationalityGenerator ofNationality(GeneratorConfig config) {
        return new NationalityGenerator(config);
    }

    /**
     * Returns a generator that produces restaurant cuisine/type names (e.g. {@code "Italian"}).
     */
    public static RestaurantTypeGenerator ofRestaurantType() {
        return new RestaurantTypeGenerator();
    }

    /**
     * Returns a restaurant-type generator whose type names are localized for the given locale.
     */
    public static RestaurantTypeGenerator ofRestaurantType(Locale locale) {
        return new RestaurantTypeGenerator(locale);
    }

    /**
     * Returns a restaurant-type generator configured by the given {@link GeneratorConfig}.
     */
    public static RestaurantTypeGenerator ofRestaurantType(GeneratorConfig config) {
        return new RestaurantTypeGenerator(config);
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
     * Returns a generator that produces structured commerce order payloads.
     */
    public static OrderInfoGenerator ofOrderInfo() {
        return new OrderInfoGenerator();
    }

    /**
     * Returns a generator that produces structured commerce order payloads for a specific locale.
     */
    public static OrderInfoGenerator ofOrderInfo(Locale locale) {
        return new OrderInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured commerce order payloads with explicit configuration.
     */
    public static OrderInfoGenerator ofOrderInfo(GeneratorConfig config) {
        return new OrderInfoGenerator(config);
    }

    /**
     * Returns a generator that produces structured commerce order payloads with an independent shipping address.
     */
    public static OrderInfoGenerator ofOrderInfo(
        GeneratorConfig config,
        AddressInfoGenerator shippingAddressGenerator
    ) {
        return new OrderInfoGenerator(config, shippingAddressGenerator);
    }

    /**
     * Returns a generator that produces structured shipment payloads.
     */
    public static ShipmentInfoGenerator ofShipmentInfo() {
        return new ShipmentInfoGenerator();
    }

    /**
     * Returns a generator that produces structured shipment payloads for a specific locale.
     */
    public static ShipmentInfoGenerator ofShipmentInfo(Locale locale) {
        return new ShipmentInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured shipment payloads with explicit configuration.
     */
    public static ShipmentInfoGenerator ofShipmentInfo(GeneratorConfig config) {
        return new ShipmentInfoGenerator(config);
    }

    /**
     * Returns a generator that produces locale-aware profession/job-title values.
     */
    public static ProfessionGenerator ofProfession() {
        return new ProfessionGenerator();
    }

    /**
     * Returns a generator that produces profession/job-title values for a specific locale.
     */
    public static ProfessionGenerator ofProfession(Locale locale) {
        return new ProfessionGenerator(locale);
    }

    /**
     * Returns a generator that produces profession/job-title values with explicit configuration.
     */
    public static ProfessionGenerator ofProfession(GeneratorConfig config) {
        return new ProfessionGenerator(config);
    }

    /**
     * Returns a fail-closed generator for SWIFT/BIC codes.
     *
     * <p>Use the {@link BicGenerator#BicGenerator(GeneratorConfig)} constructor and explicitly
     * select the banking safety policy when compatibility fixtures require plausible output.
     */
    public static BicGenerator ofBic() {
        return new BicGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a fail-closed generator for BBAN values.
     */
    public static BbanGenerator ofBban() {
        return new BbanGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a fail-closed generator for IBAN values.
     */
    public static IbanGenerator ofIban() {
        return new IbanGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a fail-closed generator for ABA routing numbers.
     */
    public static AbaRoutingGenerator ofAbaRouting() {
        return new AbaRoutingGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a generator that produces bank-country alpha-2 codes.
     */
    public static BankCountryGenerator ofBankCountry() {
        return new BankCountryGenerator();
    }

    /**
     * Returns a fail-closed generator for account numbers, names, and transaction types.
     */
    public static BankAccountGenerator ofBankAccount() {
        return new BankAccountGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a fail-closed generator for structured bank payloads.
     */
    public static BankInfoGenerator ofBankInfo() {
        return new BankInfoGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns a fail-closed generator for structured bank payloads in a specific locale.
     */
    public static BankInfoGenerator ofBankInfo(Locale locale) {
        return new BankInfoGenerator(GeneratorConfig.builder().locale(locale).build());
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
     * Returns a generator that produces locale-aware price-tag strings for a specific locale.
     */
    public static MoneyGenerator ofMoney(Locale locale) {
        return new MoneyGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware price-tag strings with explicit configuration.
     */
    public static MoneyGenerator ofMoney(GeneratorConfig config) {
        return new MoneyGenerator(config);
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
     * Returns a generator that produces structured invoice payloads.
     */
    public static InvoiceInfoGenerator ofInvoiceInfo() {
        return new InvoiceInfoGenerator();
    }

    /**
     * Returns a generator that produces structured invoice payloads for a specific locale.
     */
    public static InvoiceInfoGenerator ofInvoiceInfo(Locale locale) {
        return new InvoiceInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured invoice payloads with explicit configuration.
     */
    public static InvoiceInfoGenerator ofInvoiceInfo(GeneratorConfig config) {
        return new InvoiceInfoGenerator(config);
    }

    /**
     * Returns a generator that produces structured payment payloads.
     */
    public static PaymentInfoGenerator ofPaymentInfo() {
        return new PaymentInfoGenerator();
    }

    /**
     * Returns a generator that produces structured payment payloads for a specific locale.
     */
    public static PaymentInfoGenerator ofPaymentInfo(Locale locale) {
        return new PaymentInfoGenerator(locale);
    }

    /**
     * Returns a generator that produces structured payment payloads with explicit configuration.
     */
    public static PaymentInfoGenerator ofPaymentInfo(GeneratorConfig config) {
        return new PaymentInfoGenerator(config);
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
     * Returns an EIN generator using the default configuration, which fails closed.
     *
     * <p>Pass a configuration with an explicit corporate tax-identifier safety policy to generate
     * an isolated compatibility fixture.
     */
    public static EinGenerator ofEin() {
        return new EinGenerator(GeneratorConfig.defaults());
    }

    /**
     * Returns an EIN generator configured by the given {@link GeneratorConfig}.
     */
    public static EinGenerator ofEin(GeneratorConfig config) {
        return new EinGenerator(config);
    }

    /**
     * Returns a generator that produces crypto wallet addresses.
     */
    public static CryptoAddressGenerator ofCryptoAddress() {
        return new CryptoAddressGenerator();
    }

    /**
     * Returns a fail-closed locale-aware national-ID generator.
     *
     * <p>Use {@link #ofNationalId(GeneratorConfig)} and explicitly select the national-ID safety
     * policy when compatibility fixtures require realistic-looking output.
     */
    public static NationalIdGenerator ofNationalId(Locale locale) {
        return new NationalIdGenerator(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Returns a fail-closed seeded national-ID generator.
     */
    public static NationalIdGenerator ofNationalId(Locale locale, long seed) {
        return new NationalIdGenerator(GeneratorConfig.builder().locale(locale).seed(seed).build());
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
     * Returns a generator that produces locale-aware usernames for a specific locale.
     */
    public static UsernameGenerator ofUsername(Locale locale) {
        return new UsernameGenerator(locale);
    }

    /**
     * Returns a generator that produces locale-aware usernames with explicit configuration.
     */
    public static UsernameGenerator ofUsername(GeneratorConfig config) {
        return new UsernameGenerator(config);
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
     * Returns a provider-template generator resolving tokens such as {@code "{firstname}"}.
     */
    public static ProviderTemplateGenerator ofProviderTemplate(String template) {
        return new ProviderTemplateGenerator(template);
    }

    /**
     * Returns a deterministic provider-template generator resolving tokens such as {@code "{firstname}"}.
     */
    public static ProviderTemplateGenerator ofProviderTemplate(String template, long seed) {
        return new ProviderTemplateGenerator(template, seed);
    }

    /**
     * Returns a provider-template generator resolving tokens using explicit configuration.
     */
    public static ProviderTemplateGenerator ofProviderTemplate(String template, GeneratorConfig config) {
        return new ProviderTemplateGenerator(template, config);
    }

    /**
     * Returns a generator resolving DataFaker-style {@code #{Provider.method}} expressions,
     * e.g. {@code "#{Name.firstName} <#{Internet.emailAddress}>"}. Migration-compatibility
     * adapter for DataFaker/JavaFaker users; unknown tokens fail fast at construction.
     */
    public static DataFakerExpressionGenerator ofDataFakerExpression(String expression) {
        return new DataFakerExpressionGenerator(expression);
    }

    /**
     * Returns a DataFaker-expression generator resolving providers with explicit configuration
     * (seed, locale).
     */
    public static DataFakerExpressionGenerator ofDataFakerExpression(String expression, GeneratorConfig config) {
        return new DataFakerExpressionGenerator(expression, config);
    }

    /**
     * Returns a provider-template generator resolving tokens through an existing field lookup.
     */
    public static ProviderTemplateGenerator ofProviderTemplate(String template, FieldLookup lookup) {
        return new ProviderTemplateGenerator(template, lookup);
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
     *
     * @deprecated use {@link #pick(List)}; this compatibility alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static <T> PickGenerator<T> pickFrom(List<T> source) {
        return pick(source);
    }

    /**
     * Returns a generator that picks one random element from the given source list.
     */
    public static <T> PickGenerator<T> pick(List<T> source) {
        return new PickGenerator<>(source);
    }

    /**
     * Returns a generator that picks {@code count} distinct elements without replacement.
     *
     * @deprecated use {@link #pickSet(List, int)}; this compatibility alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static <T> PickSetGenerator<T> pickSetFrom(List<T> source, int count) {
        return pickSet(source, count);
    }

    /**
     * Returns a generator that picks {@code count} distinct elements without replacement.
     *
     * @since 1.6
     */
    public static <T> PickSetGenerator<T> pickSet(List<T> source, int count) {
        return new PickSetGenerator<>(source, count);
    }

    /**
     * Chance-style compatibility alias for {@link #pickSet(List, int)}.
     *
     * @deprecated use {@link #pickSet(List, int)}; this incorrectly cased alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static <T> PickSetGenerator<T> pickset(List<T> source, int count) {
        return pickSet(source, count);
    }

    /**
     * Returns a generator that returns a shuffled copy of the given list.
     *
     * @deprecated use {@link #shuffle(List)}; this compatibility alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static <T> ShuffleGenerator<T> shuffleOf(List<T> source) {
        return shuffle(source);
    }

    /**
     * Returns a generator that returns a shuffled copy of the given list.
     */
    public static <T> ShuffleGenerator<T> shuffle(List<T> source) {
        return new ShuffleGenerator<>(source);
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
     *
     * @deprecated use {@link #unique(Generator)}; this compatibility alias is removed in v2
     */
    @Deprecated(since = "1.6", forRemoval = true)
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

    private static java.net.URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalStateException("Generated URI could not be converted to URL", e);
        }
    }

    /**
     * Returns a generator that invokes the given source generator {@code count} times per call.
     */
    public static <T> RepeatGenerator<T> repeat(Generator<T> source, int count) {
        return new RepeatGenerator<>(source, count);
    }

    /**
     * Return a default {@link Generator} for the given Java primitive wrapper or common Java type.
     *
     * <p>Supported types include primitives/wrappers, {@code String}, {@code Number}, big
     * numbers, atomics, UUID, locale, URI/URL, legacy date/time types, and common
     * {@code java.time} values.
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

    // ── Thread safety ─────────────────────────────────────────────────────────

    /**
     * Returns a {@link ThreadLocal}-backed wrapper around the given generator factory.
     *
     * <p>Each thread receives its own independent generator instance, eliminating the
     * need for external synchronization. The factory is invoked lazily on first access
     * per thread.
     *
     * <pre>{@code
     *   Generator<String> names = Generators.threadLocal(Generators::ofFullName);
     *   // safe to call names.generate() from any thread
     * }</pre>
     *
     * @param factory supplier of new generator instances (called once per thread)
     * @param <T> the generated value type
     * @return a thread-safe generator
     */
    public static <T> Generator<T> threadLocal(Supplier<? extends Generator<T>> factory) {
        Objects.requireNonNull(factory, "factory must not be null");
        ThreadLocal<Generator<T>> local = ThreadLocal.withInitial(factory::get);
        return () -> local.get().generate();
    }

    // ── Fluent domain namespaces ─────────────────────────────────────────────

    /**
     * Returns a fluent namespace for person-related generators (names, emails, professions, etc.).
     */
    public static PersonGenerators person() { return new PersonGenerators(); }

    /**
     * Returns a fluent namespace for person-related generators using the given config.
     */
    public static PersonGenerators person(GeneratorConfig config) { return new PersonGenerators(config); }

    /**
     * Returns a fluent namespace for finance-related generators (cards, currencies, banks, etc.).
     */
    public static FinanceGenerators finance() { return new FinanceGenerators(); }

    /**
     * Returns a fluent namespace for finance-related generators using the given config.
     */
    public static FinanceGenerators finance(GeneratorConfig config) { return new FinanceGenerators(config); }

    /**
     * Returns a fluent namespace for location-related generators (cities, countries, addresses, etc.).
     */
    public static LocationGenerators location() { return new LocationGenerators(); }

    /**
     * Returns a fluent namespace for location-related generators using the given config.
     */
    public static LocationGenerators location(GeneratorConfig config) { return new LocationGenerators(config); }

    /**
     * Returns a fluent namespace for network-related generators (IPs, URLs, MAC addresses, etc.).
     */
    public static NetworkGenerators network() { return new NetworkGenerators(); }

    /**
     * Returns a fluent namespace for network-related generators using the given config.
     */
    public static NetworkGenerators network(GeneratorConfig config) { return new NetworkGenerators(config); }

    /**
     * Returns a fluent namespace for text-related generators (lorem ipsum, words, sentences, etc.).
     */
    public static TextGenerators text() { return new TextGenerators(); }

    /**
     * Returns a fluent namespace for text-related generators using the given config.
     */
    public static TextGenerators text(GeneratorConfig config) { return new TextGenerators(config); }

    /**
     * Returns a fluent namespace for commerce-related generators (products, orders, shipments).
     */
    public static CommerceGenerators commerce() { return new CommerceGenerators(); }

    /**
     * Returns a fluent namespace for commerce-related generators using the given config.
     */
    public static CommerceGenerators commerce(GeneratorConfig config) { return new CommerceGenerators(config); }

    /**
     * Returns a fluent namespace for identifier generators (UUID, ISBN, EAN, hashes, etc.).
     */
    public static IdentifierGenerators identifier() { return new IdentifierGenerators(); }

    /**
     * Returns a fluent namespace for identifier generators using the given config.
     */
    public static IdentifierGenerators identifier(GeneratorConfig config) { return new IdentifierGenerators(config); }

    /**
     * Returns a fluent namespace for date/time-related generators.
     */
    public static DateTimeGenerators datetime() { return new DateTimeGenerators(); }

    /**
     * Returns a fluent namespace for date/time-related generators using the given config.
     */
    public static DateTimeGenerators datetime(GeneratorConfig config) { return new DateTimeGenerators(config); }
}
