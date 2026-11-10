/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

/**
 * Derives a constrained {@link Generator} from Bean Validation annotations on a field.
 *
 * <p>A field also reads its accessors' constraints, and a constructor parameter reads those of the
 * field it initializes, wherever it declares none of the same type: see {@link #initializedField}.
 *
 * <p>The Jakarta Validation API is a compile-only dependency of core. When it is absent from the
 * runtime class path, {@link #isAvailable()} is {@code false}, every lookup reports no constraints,
 * and no {@code jakarta.validation} class is ever touched.
 */
final class BeanValidationSupport {

    private static final boolean AVAILABLE = detectJakartaValidation();
    private static final int SIZE_UNBOUNDED_CAP = 255;
    private static final int TEXT_GENERATION_ATTEMPTS = 256;
    /** The length of "@" and ".test" in a generated reserved address. */
    private static final int TEST_EMAIL_AFFIX_LENGTH = 6;

    private static final BigDecimal DECIMAL_DEFAULT_SPAN = new BigDecimal("1000000");
    private static final java.util.regex.Pattern MANGLED_NAME_SUFFIX = java.util.regex.Pattern.compile("-.*");
    private static final Comparator<NumericBound> LOWER_BOUND_ORDER =
            Comparator.comparing(NumericBound::value).thenComparing(bound -> !bound.inclusive());
    private static final Comparator<NumericBound> UPPER_BOUND_ORDER =
            Comparator.comparing(NumericBound::value).thenComparing(NumericBound::inclusive);
    private static final ClassValue<ConcurrentMap<ConstraintCacheKey, ConstraintModel>> CONSTRAINT_MODELS =
            new ClassValue<>() {
                @Override
                protected ConcurrentMap<ConstraintCacheKey, ConstraintModel> computeValue(Class<?> type) {
                    return new ConcurrentHashMap<>();
                }
            };
    private static final ClassValue<AccessorMethods> ACCESSOR_METHODS = new ClassValue<>() {
        @Override
        protected AccessorMethods computeValue(Class<?> type) {
            return buildAccessorMethods(type);
        }
    };
    private static final ClassValue<ConcurrentMap<Parameter, Optional<Field>>> INITIALIZED_FIELDS = new ClassValue<>() {
        @Override
        protected ConcurrentMap<Parameter, Optional<Field>> computeValue(Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };
    /** Read by name, so that no constraint class is loaded when the API is absent. */
    private static final String CONSTRAINT_PACKAGE = "jakarta.validation.constraints";

    private BeanValidationSupport() {}

    /**
     * Whether the Jakarta Validation API can be loaded by this class's loader.
     */
    static boolean isAvailable() {
        return AVAILABLE;
    }

    private static boolean detectJakartaValidation() {
        try {
            Class.forName(
                    "jakarta.validation.constraints.NotNull", false, BeanValidationSupport.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError absent) {
            return false;
        }
    }

    record ConstraintModel(
            boolean constrained,
            boolean nullOnly,
            boolean required,
            @Nullable SizeRange size,
            @Nullable NumericRange numeric,
            @Nullable Boolean assertion,
            @Nullable TemporalConstraint temporal,
            @Nullable TextConstraint text) {

        private static final ConstraintModel NONE =
                new ConstraintModel(false, false, false, null, null, null, null, null);
    }

    /**
     * A field, a record component's accessor, or a parameter, with the type it is generated as. A record
     * component is keyed by its accessor, because each lookup of the components returns new objects.
     */
    private record ConstraintCacheKey(Object member, Class<?> rawType) {}

    private record AccessorMethods(Map<String, List<Method>> declared, Map<String, List<Method>> interfaces) {}

    static final class ConstraintConflictException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        ConstraintConflictException(String message) {
            super(message);
        }
    }

    /**
     * Returns a constrained {@link Generator} for the given element and raw type,
     * or {@code null} if no relevant Bean Validation annotation is present.
     *
     * @param element the annotated field, record component, parameter, or type node
     * @param rawType the field's erasure
     * @return a constraint-respecting generator, or {@code null}
     */
    static @Nullable Generator<?> constraintGeneratorFor(@Nullable AnnotatedElement element, Class<?> rawType) {
        return constraintGeneratorFor(element, rawType, new Random(), Clock.systemDefaultZone());
    }

    static @Nullable Generator<?> constraintGeneratorFor(
            @Nullable AnnotatedElement element, Class<?> rawType, Random random) {
        return constraintGeneratorFor(element, rawType, random, Clock.systemDefaultZone());
    }

    static @Nullable Generator<?> constraintGeneratorFor(
            @Nullable AnnotatedElement element, Class<?> rawType, Random random, Clock clock) {
        ConstraintModel constraints = constraintModelFor(element, rawType);
        return constraintGeneratorFor(
                element, rawType, random, clock, constraints, EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS);
    }

    static @Nullable Generator<?> constraintGeneratorFor(
            @Nullable AnnotatedElement element,
            Class<?> rawType,
            Random random,
            Clock clock,
            ConstraintModel constraints,
            EmailDomainPolicy emailDomainPolicy) {
        Objects.requireNonNull(random, "random must not be null");
        Objects.requireNonNull(clock, "clock must not be null");
        Objects.requireNonNull(constraints, "constraints must not be null");

        if (element == null) {
            return null;
        }
        if (constraints.nullOnly()) {
            return () -> null;
        }
        if (constraints.assertion() != null) {
            return () -> constraints.assertion();
        }
        if (constraints.text() != null) {
            return textGeneratorFor(constraints.text(), random, emailDomainPolicy);
        }
        Generator<?> numericGenerator = numericGeneratorFor(constraints.numeric(), rawType, random);
        if (numericGenerator != null) {
            return numericGenerator;
        }
        return temporalGeneratorFor(constraints.temporal(), rawType, random, clock);
    }

    static ConstraintModel constraintModelFor(@Nullable AnnotatedElement element, Class<?> rawType) {
        if (element == null || !AVAILABLE) {
            return ConstraintModel.NONE;
        }
        Objects.requireNonNull(rawType, "rawType must not be null");
        if (element instanceof Field field) {
            return cachedConstraintModel(field.getDeclaringClass(), field, element, rawType);
        }
        if (element instanceof RecordComponent component) {
            return cachedConstraintModel(component.getDeclaringRecord(), component.getAccessor(), element, rawType);
        }
        if (element instanceof Parameter parameter) {
            Class<?> owner = parameter.getDeclaringExecutable().getDeclaringClass();
            return cachedConstraintModel(owner, parameter, element, rawType);
        }
        return buildConstraintModel(element, rawType);
    }

    private static ConstraintModel cachedConstraintModel(
            Class<?> owner, Object member, AnnotatedElement element, Class<?> rawType) {
        return CONSTRAINT_MODELS
                .get(owner)
                .computeIfAbsent(
                        new ConstraintCacheKey(member, rawType), ignored -> buildConstraintModel(element, rawType));
    }

    private static ConstraintModel buildConstraintModel(AnnotatedElement element, Class<?> rawType) {
        boolean nullOnly = annotation(element, Null.class) != null;
        boolean notNull = annotation(element, NotNull.class) != null;
        boolean notEmpty = annotation(element, NotEmpty.class) != null;
        boolean notBlank = annotation(element, NotBlank.class) != null;
        boolean requiredByAnnotation = notNull || notEmpty || notBlank;

        if (nullOnly && (rawType.isPrimitive() || requiredByAnnotation)) {
            throw conflict("@Null cannot be combined with a primitive or required constraint");
        }
        if ((annotation(element, Size.class) != null || notEmpty) && !supportsSize(rawType)) {
            throw conflict("@Size and @NotEmpty require a string, array, collection, or map target");
        }
        if (notBlank && rawType != String.class) {
            throw conflict("@NotBlank requires a supported string target");
        }

        SizeRange size = sizeRangeFor(element);
        NumericRange numeric = numericRangeFor(element, rawType);
        Boolean assertion = assertionFor(element, rawType);
        TemporalConstraint temporal = temporalConstraintFor(element, rawType);
        TextConstraint text =
                textConstraintFor(element, rawType, size == null ? null : size.forText(), notBlank, numeric);
        boolean constrained = nullOnly
                || requiredByAnnotation
                || size != null
                || numeric != null
                || assertion != null
                || temporal != null
                || text != null;
        return new ConstraintModel(
                constrained,
                nullOnly,
                rawType.isPrimitive() || requiredByAnnotation,
                size,
                numeric,
                assertion,
                temporal,
                text);
    }

    /**
     * The size that {@code @Size}, {@code @NotEmpty} and {@code @NotBlank} allow together, or {@code null}
     * when none is declared. With no declared maximum, the range reaches {@link Integer#MAX_VALUE}.
     */
    static @Nullable SizeRange sizeRangeFor(AnnotatedElement element) {
        if (!AVAILABLE) {
            return null;
        }
        Size size = annotation(element, Size.class);
        boolean requiresContent =
                annotation(element, NotEmpty.class) != null || annotation(element, NotBlank.class) != null;
        if (size == null && !requiresContent) {
            return null;
        }
        int declaredMin = size == null ? 0 : Math.max(0, size.min());
        int min = requiresContent ? Math.max(1, declaredMin) : declaredMin;
        int max = size == null ? Integer.MAX_VALUE : size.max();
        if (max < min) {
            throw conflict("size constraints have an empty intersection: min " + min + " exceeds max " + max);
        }
        return new SizeRange(min, max);
    }

    private static ConstraintConflictException conflict(String message) {
        return new ConstraintConflictException(message);
    }

    private static boolean supportsSize(Class<?> rawType) {
        return rawType == String.class
                || rawType.isArray()
                || Collection.class.isAssignableFrom(rawType)
                || java.util.Map.class.isAssignableFrom(rawType);
    }

    private static @Nullable TextConstraint textConstraintFor(
            AnnotatedElement element,
            Class<?> rawType,
            @Nullable SizeRange size,
            boolean nonBlank,
            @Nullable NumericRange numeric) {
        Email email = annotation(element, Email.class);
        List<Pattern> patternAnnotations = annotations(element, Pattern.class);
        boolean hasTextAnnotation = email != null || !patternAnnotations.isEmpty();
        if (rawType != String.class) {
            if (hasTextAnnotation) {
                throw conflict("text constraints require a supported string target");
            }
            return null;
        }
        if (!hasTextAnnotation && size == null && numeric == null) {
            return null;
        }

        // The flag conversion stays inline: a helper taking Pattern.Flag[] would put a Jakarta type
        // into a method signature, which breaks reflection over this class when the API is absent.
        List<TextPattern> patterns = new ArrayList<>();
        for (Pattern pattern : patternAnnotations) {
            int compiledFlags = 0;
            for (Pattern.Flag flag : pattern.flags()) {
                compiledFlags |= flag.getValue();
            }
            patterns.add(compileTextPattern(pattern.regexp(), compiledFlags, true));
        }
        if (email != null) {
            int compiledFlags = 0;
            for (Pattern.Flag flag : email.flags()) {
                compiledFlags |= flag.getValue();
            }
            boolean usefulSource = !".*".equals(email.regexp());
            patterns.add(compileTextPattern(email.regexp(), compiledFlags, usefulSource));
        }
        return new TextConstraint(email != null, List.copyOf(patterns), size, nonBlank, numeric);
    }

    private static TextPattern compileTextPattern(String expression, int compiledFlags, boolean source) {
        try {
            return new TextPattern(expression, java.util.regex.Pattern.compile(expression, compiledFlags), source);
        } catch (java.util.regex.PatternSyntaxException invalidPattern) {
            throw conflict("text constraint contains an invalid regular expression");
        }
    }

    private static void addEmailSources(
            List<Generator<String>> sources,
            @Nullable SizeRange size,
            EmailDomainPolicy emailDomainPolicy,
            Random random) {
        if (emailDomainPolicy != EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS) {
            sources.add(regexGenerator("[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)", random));
            return;
        }
        sources.add(regexGenerator("[a-z]{4,8}@example\\.(com|net|org)", random));
        if (size == null) {
            return;
        }
        // For a size an example.* address cannot meet: the labels share the length left beside "@" and
        // ".test", so every candidate fits it. A size that leaves too little or too much has no source.
        int minLabels = Math.max(2, size.min() - TEST_EMAIL_AFFIX_LENGTH);
        int maxLabels = Math.min(16, size.max() - TEST_EMAIL_AFFIX_LENGTH);
        if (minLabels <= maxLabels) {
            sources.add(regexGenerator(
                    "[a-z]{" + (minLabels + 1) / 2 + "," + (maxLabels + 1) / 2 + "}@[a-z]{" + minLabels / 2 + ","
                            + maxLabels / 2 + "}\\.test",
                    random));
        }
    }

    private static Generator<String> textGeneratorFor(
            TextConstraint constraints, Random random, EmailDomainPolicy emailDomainPolicy) {
        List<Generator<String>> sources = new ArrayList<>();
        // A pattern that cannot be a source is still enforced on each candidate, so another source can meet it.
        StringBuilder unsupported = new StringBuilder();
        for (TextPattern pattern : constraints.patterns()) {
            if (pattern.source()) {
                try {
                    sources.add(
                            regexGenerator(pattern.expression(), constraints.patternMinLength(), random.nextLong()));
                } catch (IllegalArgumentException unsupportedPattern) {
                    unsupported.append("; ").append(unsupportedPattern.getMessage());
                }
            }
        }
        if (constraints.email()) {
            addEmailSources(sources, constraints.size(), emailDomainPolicy, random);
        }
        Generator<String> numericSource = numericStringGeneratorFor(constraints.numeric(), random);
        if (numericSource != null) {
            sources.add(numericSource);
        }
        if (constraints.size() != null) {
            sources.add(stringGeneratorFor(constraints.size(), constraints.nonBlank(), random));
        }
        if (sources.isEmpty()) {
            throw conflict("text constraint uses unsupported regular-expression syntax" + unsupported);
        }

        return () -> {
            for (int attempt = 0; attempt < TEXT_GENERATION_ATTEMPTS; attempt++) {
                String candidate = sources.get(attempt % sources.size()).generate();
                if (constraints.accepts(candidate)) {
                    return candidate;
                }
            }
            throw conflict("text constraints did not produce a value in the bounded search budget" + unsupported);
        };
    }

    private static RegexGenerator regexGenerator(String expression, Random random) {
        return regexGenerator(expression, 1, random.nextLong());
    }

    // Each regex source replays from its own seed, drawn from the engine's replayable stream. The minimum
    // length lets its open-ended repetitions reach what @Size asks for.
    private static RegexGenerator regexGenerator(String expression, int minLength, long seed) {
        int length = Math.clamp(minLength, 1, Integer.MAX_VALUE - 1);
        return new RegexGenerator(
                expression,
                GeneratorConfig.builder()
                        .seed(seed)
                        .stringLength(length, length)
                        .build());
    }

    record TextPattern(String expression, java.util.regex.Pattern compiled, boolean source) {}

    record TextConstraint(
            boolean email,
            List<TextPattern> patterns,
            @Nullable SizeRange size,
            boolean nonBlank,
            @Nullable NumericRange numeric) {

        /** The length a pattern source starts from, so its open-ended repetitions reach {@code @Size}. */
        int patternMinLength() {
            return size == null ? 1 : size.min();
        }

        boolean accepts(String candidate) {
            if (size != null && (candidate.length() < size.min() || candidate.length() > size.max())) {
                return false;
            }
            if (nonBlank && candidate.isBlank()) {
                return false;
            }
            if (email && !EmailShape.accepts(candidate)) {
                return false;
            }
            for (TextPattern pattern : patterns) {
                if (!pattern.compiled().matcher(candidate).matches()) {
                    return false;
                }
            }
            return numeric == null || numericAccepts(numeric, candidate);
        }
    }

    private static boolean numericAccepts(NumericRange range, String candidate) {
        final BigDecimal value;
        try {
            value = new BigDecimal(candidate);
        } catch (NumberFormatException notNumeric) {
            return false;
        }
        return numericAccepts(range, value);
    }

    private static boolean numericAccepts(NumericRange range, BigDecimal value) {
        if (range.lower() != null) {
            int comparison = value.compareTo(range.lower().value());
            if (comparison < 0 || (comparison == 0 && !range.lower().inclusive())) {
                return false;
            }
        }
        if (range.upper() != null) {
            int comparison = value.compareTo(range.upper().value());
            if (comparison > 0 || (comparison == 0 && !range.upper().inclusive())) {
                return false;
            }
        }
        return true;
    }

    /**
     * The size of a collection, array or map: the configured range, kept within the declared size where
     * there is one.
     *
     * @param declared the declared size, or {@code null} when none is declared
     * @param random the stream to draw from
     * @param configuredMin the configured minimum collection size
     * @param configuredMax the configured maximum collection size
     * @return the size to generate
     */
    static int sizeFor(@Nullable SizeRange declared, Random random, int configuredMin, int configuredMax) {
        SizeRange range = declared == null
                ? new SizeRange(configuredMin, configuredMax)
                : declared.within(configuredMin, configuredMax);
        return range.next(random);
    }

    record SizeRange(int min, int max) {

        /** Text with no declared maximum has at most 255 characters. */
        SizeRange forText() {
            return max == Integer.MAX_VALUE ? new SizeRange(min, Math.max(min, SIZE_UNBOUNDED_CAP)) : this;
        }

        /**
         * A configured range kept within this one: each configured bound moves to the nearest size this
         * range allows, so a configured range outside this one gives its nearest bound.
         */
        SizeRange within(int configuredMin, int configuredMax) {
            return new SizeRange(Math.clamp(configuredMin, min, max), Math.clamp(configuredMax, min, max));
        }

        int next(Random random) {
            return min == max ? min : random.nextInt(min, max + 1);
        }
    }

    private static Generator<String> stringGeneratorFor(SizeRange size, boolean notBlank, Random random) {
        int min = Math.max(notBlank ? 1 : 0, size.min());
        int max = Math.max(min, size.max());
        int lower = min;
        int upper = max;
        return () -> {
            int length = lower == upper ? lower : random.nextInt(lower, upper + 1);
            if (length == 0) {
                return "";
            }
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                builder.append((char) ('a' + random.nextInt(26)));
            }
            return builder.toString();
        };
    }

    private static @Nullable Generator<String> numericStringGeneratorFor(@Nullable NumericRange range, Random random) {
        if (range == null) {
            return null;
        }
        if (range.decimal()) {
            return () -> randomBigDecimal(random, range).stripTrailingZeros().toPlainString();
        }
        IntegralRange integral = integralRangeFor(range, String.class);
        return () -> randomBigInteger(random, integral.min(), integral.max()).toString();
    }

    private static @Nullable Generator<?> numericGeneratorFor(
            @Nullable NumericRange range, Class<?> rawType, Random random) {
        if (range == null) {
            return null;
        }
        if (rawType == byte.class || rawType == Byte.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () ->
                    randomBigInteger(random, integral.min(), integral.max()).byteValueExact();
        }
        if (rawType == short.class || rawType == Short.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () ->
                    randomBigInteger(random, integral.min(), integral.max()).shortValueExact();
        }
        if (rawType == int.class || rawType == Integer.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () ->
                    randomBigInteger(random, integral.min(), integral.max()).intValueExact();
        }
        if (rawType == long.class || rawType == Long.class || rawType == Number.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomLongInclusive(
                    random, integral.min().longValueExact(), integral.max().longValueExact());
        }
        if (rawType == float.class || rawType == Float.class) {
            FloatingRange floating = floatingRangeFor(range, true);
            return () -> (float) randomDouble(random, floating.min(), floating.max());
        }
        if (rawType == double.class || rawType == Double.class) {
            FloatingRange floating = floatingRangeFor(range, false);
            return () -> randomDouble(random, floating.min(), floating.max());
        }
        if (rawType == BigInteger.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomBigInteger(random, integral.min(), integral.max());
        }
        return () -> randomBigDecimal(random, range);
    }

    private static @Nullable Generator<?> temporalGeneratorFor(
            @Nullable TemporalConstraint constraint, Class<?> rawType, Random random, Clock clock) {
        if (constraint == null) {
            return null;
        }
        return () -> temporalValueFor(rawType, constraint, random, clock);
    }

    private static Object temporalValueFor(
            Class<?> rawType, TemporalConstraint constraint, Random random, Clock clock) {
        if (constraint.direction() == TemporalDirection.PRESENT) {
            return presentTemporalValueFor(rawType, clock);
        }
        boolean future = constraint.direction() == TemporalDirection.FUTURE;
        long seconds = random.nextLong(60, 3650L * 24L * 60L * 60L + 1L);
        Instant instantNow = clock.instant();
        Instant instant = future ? instantNow.plusSeconds(seconds) : instantNow.minusSeconds(seconds);
        LocalDate today = LocalDate.now(clock);
        LocalDate date = future ? today.plusDays(1) : today.minusDays(1);
        LocalDateTime dateTimeNow = LocalDateTime.now(clock);
        LocalDateTime dateTime = future ? dateTimeNow.plusSeconds(seconds) : dateTimeNow.minusSeconds(seconds);
        if (rawType == Instant.class) return instant;
        if (rawType == LocalDate.class) return date;
        if (rawType == LocalDateTime.class) return dateTime;
        if (rawType == ZonedDateTime.class) return ZonedDateTime.ofInstant(instant, clock.getZone());
        if (rawType == OffsetDateTime.class) return OffsetDateTime.ofInstant(instant, clock.getZone());
        if (rawType == Date.class) return Date.from(instant);
        if (rawType == java.sql.Date.class) return new java.sql.Date(instant.toEpochMilli());
        if (rawType == Timestamp.class) return Timestamp.from(instant);
        if (rawType == Calendar.class || rawType == GregorianCalendar.class) {
            return FieldGeneratorResolver.calendarOf(ZonedDateTime.ofInstant(instant, clock.getZone()));
        }
        if (rawType == LocalTime.class) return localTimeFor(future, constraint.allowsPresent(), clock);
        if (rawType == OffsetTime.class)
            return localTimeFor(future, constraint.allowsPresent(), clock)
                    .atOffset(OffsetDateTime.now(clock).getOffset());
        if (rawType == Year.class)
            return future ? Year.now(clock).plusYears(1) : Year.now(clock).minusYears(1);
        if (rawType == YearMonth.class)
            return future
                    ? YearMonth.now(clock).plusMonths(1)
                    : YearMonth.now(clock).minusMonths(1);
        return monthDayFor(future, constraint.allowsPresent(), clock);
    }

    private static Object presentTemporalValueFor(Class<?> rawType, Clock clock) {
        Instant instant = clock.instant();
        LocalDate date = LocalDate.now(clock);
        if (rawType == Instant.class) return instant;
        if (rawType == LocalDate.class) return date;
        if (rawType == LocalDateTime.class) return LocalDateTime.now(clock);
        if (rawType == ZonedDateTime.class) return ZonedDateTime.now(clock);
        if (rawType == OffsetDateTime.class) return OffsetDateTime.now(clock);
        if (rawType == Date.class) return Date.from(presentMillisecond(instant, rawType));
        if (rawType == java.sql.Date.class)
            return new java.sql.Date(presentMillisecond(instant, rawType).toEpochMilli());
        if (rawType == Timestamp.class) return Timestamp.from(presentMillisecond(instant, rawType));
        if (rawType == Calendar.class || rawType == GregorianCalendar.class) {
            return FieldGeneratorResolver.calendarOf(
                    ZonedDateTime.ofInstant(presentMillisecond(instant, rawType), clock.getZone()));
        }
        if (rawType == LocalTime.class) return LocalTime.now(clock);
        if (rawType == OffsetTime.class) return OffsetTime.now(clock);
        if (rawType == Year.class) return Year.now(clock);
        if (rawType == YearMonth.class) return YearMonth.now(clock);
        return MonthDay.now(clock);
    }

    /**
     * The clock's instant as a {@code Date}, a {@code Timestamp} or a {@code Calendar} holds it, to the millisecond.
     * Hibernate Validator compares that millisecond with the clock's whole instant, so where the clock is finer
     * no value of the type is the present: the millisecond before it is past and the one after it future.
     */
    private static Instant presentMillisecond(Instant instant, Class<?> rawType) {
        if (instant.getNano() % 1_000_000 != 0) {
            throw conflict("@PastOrPresent and @FutureOrPresent have no representable " + rawType.getSimpleName()
                    + " at the configured clock, which is finer than the millisecond it holds; configure a clock"
                    + " at a whole millisecond");
        }
        return instant;
    }

    /**
     * The last time of day for the future, or the first for the past. At that end of the day the clock's own
     * time is the only value left, which an inclusive bound allows and a strict one does not.
     */
    private static LocalTime localTimeFor(boolean future, boolean allowsPresent, Clock clock) {
        LocalTime now = LocalTime.now(clock);
        if (future && now.equals(LocalTime.MAX) && !allowsPresent) {
            throw conflict("@Future has no representable LocalTime after the configured clock");
        }
        if (!future && now.equals(LocalTime.MIN) && !allowsPresent) {
            throw conflict("@Past has no representable LocalTime before the configured clock");
        }
        return future ? LocalTime.MAX : LocalTime.MIN;
    }

    /**
     * The last day of the year for the future, or the first for the past. On that day it is the clock's own
     * day, which an inclusive bound allows and a strict one does not.
     */
    private static MonthDay monthDayFor(boolean future, boolean allowsPresent, Clock clock) {
        MonthDay today = MonthDay.now(clock);
        MonthDay boundary = future ? MonthDay.of(12, 31) : MonthDay.of(1, 1);
        if (today.equals(boundary) && !allowsPresent) {
            throw conflict("temporal constraint has no representable MonthDay at the configured clock");
        }
        return boundary;
    }

    private static @Nullable TemporalConstraint temporalConstraintFor(AnnotatedElement element, Class<?> rawType) {
        boolean futureOnly = annotation(element, Future.class) != null;
        boolean futureOrPresent = annotation(element, FutureOrPresent.class) != null;
        boolean pastOnly = annotation(element, Past.class) != null;
        boolean pastOrPresent = annotation(element, PastOrPresent.class) != null;
        if (!futureOnly && !futureOrPresent && !pastOnly && !pastOrPresent) {
            return null;
        }
        if (!supportsTemporal(rawType)) {
            throw conflict("temporal constraints require a supported date or time target");
        }

        boolean allowsPast = !futureOnly && !futureOrPresent;
        boolean allowsPresent = !futureOnly && !pastOnly;
        boolean allowsFuture = !pastOnly && !pastOrPresent;
        if (!allowsPast && !allowsPresent && !allowsFuture) {
            throw conflict("temporal constraints have an empty intersection");
        }
        return new TemporalConstraint(allowsPast, allowsPresent, allowsFuture);
    }

    private static boolean supportsTemporal(Class<?> rawType) {
        return rawType == Instant.class
                || rawType == LocalDate.class
                || rawType == LocalDateTime.class
                || rawType == ZonedDateTime.class
                || rawType == OffsetDateTime.class
                || rawType == Date.class
                || rawType == java.sql.Date.class
                || rawType == Timestamp.class
                || rawType == Calendar.class
                || rawType == GregorianCalendar.class
                || rawType == LocalTime.class
                || rawType == OffsetTime.class
                || rawType == Year.class
                || rawType == YearMonth.class
                || rawType == MonthDay.class;
    }

    private enum TemporalDirection {
        PAST,
        PRESENT,
        FUTURE
    }

    /** The times a temporal constraint allows: before, at, and after the configured clock's present. */
    record TemporalConstraint(boolean allowsPast, boolean allowsPresent, boolean allowsFuture) {

        /** A generated value lies in the future where that is allowed, else in the past, else at the present. */
        TemporalDirection direction() {
            return allowsFuture
                    ? TemporalDirection.FUTURE
                    : allowsPast ? TemporalDirection.PAST : TemporalDirection.PRESENT;
        }

        boolean accepts(Object value, Clock clock) {
            int comparison = compareWithClock(value, clock);
            return comparison < 0 ? allowsPast : comparison == 0 ? allowsPresent : allowsFuture;
        }
    }

    /**
     * Compares a value with the clock's present as Hibernate Validator does: a {@code java.time} value with the
     * {@code now(clock)} of its type, and a {@code Calendar} or {@code Date} by the instant of its epoch
     * millisecond with the clock's instant, which can be finer. These are the types the coherence pass writes,
     * and {@code java.sql.Date} and {@code Timestamp} are {@code Date}s.
     */
    private static int compareWithClock(Object value, Clock clock) {
        if (value instanceof Instant instant) {
            return instant.compareTo(clock.instant());
        }
        if (value instanceof LocalDate date) {
            return date.compareTo(LocalDate.now(clock));
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.compareTo(LocalDateTime.now(clock));
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.compareTo(OffsetDateTime.now(clock));
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.compareTo(ZonedDateTime.now(clock));
        }
        if (value instanceof Calendar calendar) {
            return calendar.toInstant().compareTo(clock.instant());
        }
        return Instant.ofEpochMilli(((Date) value).getTime()).compareTo(clock.instant());
    }

    /**
     * Whether a value the coherence pass would write meets the constraints of the element: no value for
     * {@code @Null}, the value {@code @AssertTrue} or {@code @AssertFalse} asks for, a date or time against the
     * clock as Hibernate Validator compares it, a number within its bounds, and a string as its text
     * constraints require. The pass writes no value that another constraint bounds.
     *
     * @param element the field the value is for
     * @param rawType the field's erasure
     * @param value a boolean, a date, a time, a number or a string of that type
     * @param clock the configured clock
     * @return whether the element's constraints allow the value
     */
    static boolean accepts(AnnotatedElement element, Class<?> rawType, Object value, Clock clock) {
        ConstraintModel constraints = constraintModelFor(element, rawType);
        if (constraints.nullOnly()) {
            return false;
        }
        if (constraints.assertion() != null) {
            return constraints.assertion().equals(value);
        }
        if (constraints.temporal() != null) {
            return constraints.temporal().accepts(value, clock);
        }
        if (constraints.text() != null) {
            return constraints.text().accepts(value.toString());
        }
        if (constraints.numeric() == null) {
            return true;
        }
        if (value instanceof Float number) {
            return floatingAccepts(element, number.doubleValue(), true);
        }
        if (value instanceof Double number) {
            return floatingAccepts(element, number, false);
        }
        return numericAccepts(constraints.numeric(), new BigDecimal(value.toString()));
    }

    /**
     * Whether a float or a double meets each numeric constraint of the element as Hibernate Validator reads it:
     * {@code @Min} and {@code @Max} in the value's own type, with the bound converted to it, so that
     * {@code 16777220f} meets {@code @Max(16777219)}; {@code @DecimalMin} and {@code @DecimalMax} against the double
     * the value is, so that {@code 0.1f}, which is {@code 0.10000000149011612}, breaks {@code @DecimalMax("0.1")};
     * and a sign by comparison with zero, so that {@code -0.0} is negative. An infinity lies beyond every bound.
     */
    private static boolean floatingAccepts(AnnotatedElement element, double value, boolean singlePrecision) {
        Min min = annotation(element, Min.class);
        if (min != null && floatingCompare(value, min.value(), singlePrecision) < 0) {
            return false;
        }
        Max max = annotation(element, Max.class);
        if (max != null && floatingCompare(value, max.value(), singlePrecision) > 0) {
            return false;
        }
        DecimalMin decimalMin = annotation(element, DecimalMin.class);
        if (decimalMin != null && !meetsLower(decimalCompare(value, decimalMin.value()), decimalMin.inclusive())) {
            return false;
        }
        DecimalMax decimalMax = annotation(element, DecimalMax.class);
        if (decimalMax != null && !meetsUpper(decimalCompare(value, decimalMax.value()), decimalMax.inclusive())) {
            return false;
        }
        int sign = Double.compare(value, 0.0);
        boolean signAllowed = (annotation(element, Positive.class) == null || sign > 0)
                && (annotation(element, PositiveOrZero.class) == null || sign >= 0)
                && (annotation(element, Negative.class) == null || sign < 0);
        return signAllowed && (annotation(element, NegativeOrZero.class) == null || sign <= 0);
    }

    private static int floatingCompare(double value, long bound, boolean singlePrecision) {
        return singlePrecision ? Float.compare((float) value, (float) bound) : Double.compare(value, (double) bound);
    }

    private static int decimalCompare(double value, String bound) {
        if (Double.isInfinite(value)) {
            return value > 0 ? 1 : -1;
        }
        return BigDecimal.valueOf(value).compareTo(new BigDecimal(bound));
    }

    private static boolean meetsLower(int comparison, boolean inclusive) {
        return comparison > 0 || (comparison == 0 && inclusive);
    }

    private static boolean meetsUpper(int comparison, boolean inclusive) {
        return comparison < 0 || (comparison == 0 && inclusive);
    }

    private static @Nullable Boolean assertionFor(AnnotatedElement element, Class<?> rawType) {
        boolean assertedTrue = annotation(element, AssertTrue.class) != null;
        boolean assertedFalse = annotation(element, AssertFalse.class) != null;
        if (!assertedTrue && !assertedFalse) {
            return null;
        }
        if (rawType != boolean.class && rawType != Boolean.class) {
            throw conflict("boolean assertions require a boolean target");
        }
        if (assertedTrue && assertedFalse) {
            throw conflict("@AssertTrue and @AssertFalse have an empty intersection");
        }
        return assertedTrue;
    }

    private static boolean hasNumericConstraint(AnnotatedElement element) {
        return annotation(element, Min.class) != null
                || annotation(element, Max.class) != null
                || annotation(element, DecimalMin.class) != null
                || annotation(element, DecimalMax.class) != null
                || annotation(element, Positive.class) != null
                || annotation(element, PositiveOrZero.class) != null
                || annotation(element, Negative.class) != null
                || annotation(element, NegativeOrZero.class) != null;
    }

    private static @Nullable NumericRange numericRangeFor(AnnotatedElement element, Class<?> rawType) {
        if (!hasNumericConstraint(element)) {
            return null;
        }
        List<NumericBound> lowerBounds = new ArrayList<>();
        List<NumericBound> upperBounds = new ArrayList<>();
        Min integralMin = annotation(element, Min.class);
        if (integralMin != null) {
            lowerBounds.add(new NumericBound(BigDecimal.valueOf(integralMin.value()), true));
        }
        Max integralMax = annotation(element, Max.class);
        if (integralMax != null) {
            upperBounds.add(new NumericBound(BigDecimal.valueOf(integralMax.value()), true));
        }
        DecimalMin decimalMin = annotation(element, DecimalMin.class);
        DecimalMax decimalMax = annotation(element, DecimalMax.class);
        try {
            if (decimalMin != null) {
                lowerBounds.add(new NumericBound(new BigDecimal(decimalMin.value()), decimalMin.inclusive()));
            }
            if (decimalMax != null) {
                upperBounds.add(new NumericBound(new BigDecimal(decimalMax.value()), decimalMax.inclusive()));
            }
        } catch (NumberFormatException invalidBound) {
            throw conflict("decimal constraint contains an invalid numeric bound");
        }
        if (annotation(element, Positive.class) != null) {
            lowerBounds.add(new NumericBound(BigDecimal.ZERO, false));
        }
        if (annotation(element, PositiveOrZero.class) != null) {
            lowerBounds.add(new NumericBound(BigDecimal.ZERO, true));
        }
        if (annotation(element, Negative.class) != null) {
            upperBounds.add(new NumericBound(BigDecimal.ZERO, false));
        }
        if (annotation(element, NegativeOrZero.class) != null) {
            upperBounds.add(new NumericBound(BigDecimal.ZERO, true));
        }

        NumericBound lower = lowerBounds.stream().max(LOWER_BOUND_ORDER).orElse(null);
        NumericBound upper = upperBounds.stream().min(UPPER_BOUND_ORDER).orElse(null);
        NumericRange range = new NumericRange(lower, upper, decimalMin != null || decimalMax != null);
        validateContinuousRange(range);
        validateNumericTarget(range, rawType);
        return range;
    }

    private static void validateContinuousRange(NumericRange range) {
        if (range.lower() == null || range.upper() == null) {
            return;
        }
        int comparison = range.lower().value().compareTo(range.upper().value());
        if (comparison > 0
                || (comparison == 0
                        && (!range.lower().inclusive() || !range.upper().inclusive()))) {
            throw conflict("numeric constraints have an empty intersection");
        }
    }

    private static void validateNumericTarget(NumericRange range, Class<?> rawType) {
        if (isIntegralTarget(rawType) || (rawType == String.class && !range.decimal())) {
            integralRangeFor(range, rawType);
            return;
        }
        if (rawType == float.class || rawType == Float.class) {
            floatingRangeFor(range, true);
            return;
        }
        if (rawType == double.class || rawType == Double.class) {
            floatingRangeFor(range, false);
            return;
        }
        if (rawType != BigDecimal.class && rawType != String.class) {
            throw conflict("numeric constraints require a supported numeric or numeric-string target");
        }
    }

    private static boolean isIntegralTarget(Class<?> rawType) {
        return rawType == byte.class
                || rawType == Byte.class
                || rawType == short.class
                || rawType == Short.class
                || rawType == int.class
                || rawType == Integer.class
                || rawType == long.class
                || rawType == Long.class
                || rawType == Number.class
                || rawType == BigInteger.class;
    }

    /** The value domain of a bounded integral target type; {@code null} for {@link BigInteger}. */
    private record IntegralDomain(BigInteger min, BigInteger max) {}

    private static @Nullable IntegralDomain integralDomainFor(Class<?> rawType) {
        if (rawType == byte.class || rawType == Byte.class) {
            return new IntegralDomain(BigInteger.valueOf(Byte.MIN_VALUE), BigInteger.valueOf(Byte.MAX_VALUE));
        }
        if (rawType == short.class || rawType == Short.class) {
            return new IntegralDomain(BigInteger.valueOf(Short.MIN_VALUE), BigInteger.valueOf(Short.MAX_VALUE));
        }
        if (rawType == int.class || rawType == Integer.class) {
            return new IntegralDomain(BigInteger.valueOf(Integer.MIN_VALUE), BigInteger.valueOf(Integer.MAX_VALUE));
        }
        if (rawType != BigInteger.class) {
            return new IntegralDomain(BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(Long.MAX_VALUE));
        }
        return null;
    }

    private static IntegralRange integralRangeFor(NumericRange range, Class<?> rawType) {
        IntegralDomain domain = integralDomainFor(rawType);
        BigInteger min = smallestInteger(range.lower());
        BigInteger max = largestInteger(range.upper());
        if (domain != null) {
            min = min == null ? domain.min() : min.max(domain.min());
            max = max == null ? domain.max() : max.min(domain.max());
        } else if (min == null) {
            min = Objects.requireNonNull(max).subtract(DECIMAL_DEFAULT_SPAN.toBigIntegerExact());
        } else if (max == null) {
            max = min.add(DECIMAL_DEFAULT_SPAN.toBigIntegerExact());
        }
        if (min.compareTo(max) > 0) {
            throw conflict("numeric constraints have no value in the target integral domain");
        }
        return new IntegralRange(min, max);
    }

    private static @Nullable BigInteger smallestInteger(@Nullable NumericBound lower) {
        if (lower == null) {
            return null;
        }
        BigInteger value = lower.value().setScale(0, RoundingMode.CEILING).toBigIntegerExact();
        if (!lower.inclusive() && lower.value().compareTo(new BigDecimal(value)) == 0) {
            return value.add(BigInteger.ONE);
        }
        return value;
    }

    private static @Nullable BigInteger largestInteger(@Nullable NumericBound upper) {
        if (upper == null) {
            return null;
        }
        BigInteger value = upper.value().setScale(0, RoundingMode.FLOOR).toBigIntegerExact();
        if (!upper.inclusive() && upper.value().compareTo(new BigDecimal(value)) == 0) {
            return value.subtract(BigInteger.ONE);
        }
        return value;
    }

    private static FloatingRange floatingRangeFor(NumericRange range, boolean singlePrecision) {
        double magnitude = singlePrecision ? Float.MAX_VALUE : Double.MAX_VALUE;
        Double min = floatingLower(range.lower(), singlePrecision, magnitude);
        Double max = floatingUpper(range.upper(), singlePrecision, magnitude);
        if (min == null) {
            min = Math.max(-magnitude, Objects.requireNonNull(max) - DECIMAL_DEFAULT_SPAN.doubleValue());
        }
        if (max == null) {
            max = Math.min(magnitude, min + DECIMAL_DEFAULT_SPAN.doubleValue());
        }
        if (Double.compare(min, max) > 0) {
            throw conflict("numeric constraints have no value in the target floating-point domain");
        }
        return new FloatingRange(min, max);
    }

    private static @Nullable Double floatingLower(
            @Nullable NumericBound lower, boolean singlePrecision, double magnitude) {
        if (lower == null) {
            return null;
        }
        BigDecimal domainMax = BigDecimal.valueOf(magnitude);
        BigDecimal domainMin = domainMax.negate();
        if (lower.value().compareTo(domainMax) > 0) {
            throw conflict("numeric lower bound exceeds the target floating-point domain");
        }
        if (lower.value().compareTo(domainMin) < 0) {
            return -magnitude;
        }
        double candidate =
                singlePrecision ? lower.value().floatValue() : lower.value().doubleValue();
        BigDecimal represented = representedFloating(candidate, singlePrecision);
        if (represented.compareTo(lower.value()) < 0
                || (!lower.inclusive() && represented.compareTo(lower.value()) == 0)) {
            candidate = singlePrecision ? Math.nextUp((float) candidate) : Math.nextUp(candidate);
        }
        return candidate;
    }

    private static @Nullable Double floatingUpper(
            @Nullable NumericBound upper, boolean singlePrecision, double magnitude) {
        if (upper == null) {
            return null;
        }
        BigDecimal domainMax = BigDecimal.valueOf(magnitude);
        BigDecimal domainMin = domainMax.negate();
        if (upper.value().compareTo(domainMin) < 0) {
            throw conflict("numeric upper bound precedes the target floating-point domain");
        }
        if (upper.value().compareTo(domainMax) > 0) {
            return magnitude;
        }
        double candidate =
                singlePrecision ? upper.value().floatValue() : upper.value().doubleValue();
        BigDecimal represented = representedFloating(candidate, singlePrecision);
        if (represented.compareTo(upper.value()) > 0
                || (!upper.inclusive() && represented.compareTo(upper.value()) == 0)) {
            candidate = singlePrecision ? Math.nextDown((float) candidate) : Math.nextDown(candidate);
        }
        return candidate;
    }

    private static BigDecimal representedFloating(double value, boolean singlePrecision) {
        return new BigDecimal(singlePrecision ? Float.toString((float) value) : Double.toString(value));
    }

    record NumericBound(BigDecimal value, boolean inclusive) {}

    record NumericRange(
            @Nullable NumericBound lower, @Nullable NumericBound upper, boolean decimal) {}

    private record IntegralRange(BigInteger min, BigInteger max) {}

    private record FloatingRange(double min, double max) {}

    private static BigDecimal randomBigDecimal(Random random, NumericRange range) {
        NumericBound lower = range.lower();
        NumericBound upper = range.upper();
        if (lower != null && upper != null) {
            if (lower.value().compareTo(upper.value()) == 0) {
                return lower.value();
            }
            BigDecimal fraction = BigDecimal.valueOf(0.25d + random.nextDouble() * 0.5d);
            return lower.value().add(upper.value().subtract(lower.value()).multiply(fraction));
        }
        BigDecimal offset = DECIMAL_DEFAULT_SPAN.multiply(BigDecimal.valueOf(0.25d + random.nextDouble() * 0.5d));
        return lower != null
                ? lower.value().add(offset)
                : Objects.requireNonNull(upper).value().subtract(offset);
    }

    private static BigInteger randomBigInteger(Random random, BigInteger min, BigInteger max) {
        if (min.compareTo(max) >= 0) {
            return min;
        }
        BigInteger range = max.subtract(min);
        return min.add(new BigInteger(range.add(BigInteger.ONE).bitLength(), random).mod(range.add(BigInteger.ONE)));
    }

    private static long randomLongInclusive(Random random, long min, long max) {
        if (min >= max) {
            return min;
        }
        long bound = max == Long.MAX_VALUE ? Long.MAX_VALUE : max + 1;
        return random.nextLong(min, bound);
    }

    private static double randomDouble(Random random, double min, double max) {
        if (Double.compare(min, max) >= 0) {
            return min;
        }
        double value = min + random.nextDouble() * (max - min);
        return Math.min(value, max);
    }

    /**
     * The field a constructor argument initializes: the nearest field with the parameter's name, on the
     * constructor's class or a superclass, when it is an instance field of the parameter's declared type.
     * Parameter names are only known when the class was compiled with {@code -parameters}.
     *
     * <p>Where the parameter declares no constraint of a type, the argument takes the field's, and its
     * accessors', so a constraint on a final field reaches the value that fills it.
     *
     * @param parameter a constructor or method parameter
     * @return the initialized field, or {@code null} when there is none
     */
    static @Nullable Field initializedField(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        return INITIALIZED_FIELDS
                .get(executable.getDeclaringClass())
                .computeIfAbsent(parameter, BeanValidationSupport::findInitializedField)
                .orElse(null);
    }

    /**
     * Names a constructor parameter as a construction adapter does, after the property it fills, as Kotlin
     * metadata names it where the class was compiled without parameter names. The argument then meets the
     * constraints of the field with that name and the parameter's declared type too, as it does under a compiled
     * name. Constraints read for the parameter before are dropped, so that they are read again with the field.
     *
     * @param parameter a constructor parameter
     * @param name the name of the property it fills
     */
    static void nameConstructorParameter(Parameter parameter, String name) {
        Class<?> owner = parameter.getDeclaringExecutable().getDeclaringClass();
        Optional<Field> field = findInitializedField(parameter, name);
        if (!field.equals(INITIALIZED_FIELDS.get(owner).put(parameter, field))) {
            CONSTRAINT_MODELS.get(owner).keySet().removeIf(key -> key.member().equals(parameter));
        }
    }

    private static Optional<Field> findInitializedField(Parameter parameter) {
        return parameter.isNamePresent() ? findInitializedField(parameter, parameter.getName()) : Optional.empty();
    }

    private static Optional<Field> findInitializedField(Parameter parameter, String name) {
        Executable executable = parameter.getDeclaringExecutable();
        if (!(executable instanceof Constructor<?>)) {
            return Optional.empty();
        }
        for (Class<?> owner = executable.getDeclaringClass(); owner != Object.class; owner = owner.getSuperclass()) {
            try {
                Field field = owner.getDeclaredField(name);
                return !Modifier.isStatic(field.getModifiers())
                                && field.getGenericType().equals(parameter.getParameterizedType())
                        ? Optional.of(field)
                        : Optional.empty();
            } catch (NoSuchFieldException notDeclaredHere) {
                // Keep looking in the superclass.
            }
        }
        return Optional.empty();
    }

    /**
     * Whether the email domain policy chooses the domain of every value that a constraint generator for
     * these constraints produces: the value is an {@code @Email} address, and no {@code @Email} regexp or
     * {@code @Pattern} that the regex generator supports can supply it, with its domain as written instead.
     * A pattern it does not support only filters the policy's addresses. The resolver settles a
     * {@code @Null} member before it reports a constraint generator, so this is never asked of one.
     */
    static boolean followsEmailDomainPolicy(ConstraintModel constraints) {
        TextConstraint text = constraints.text();
        if (text == null || !text.email()) {
            return false;
        }
        for (TextPattern pattern : text.patterns()) {
            if (pattern.source() && suppliesValues(pattern, text.patternMinLength())) {
                return false;
            }
        }
        return true;
    }

    // The pattern sources of textGeneratorFor, built with a fixed seed so that the check draws nothing.
    private static boolean suppliesValues(TextPattern pattern, int minLength) {
        try {
            regexGenerator(pattern.expression(), minLength, 0L);
            return true;
        } catch (IllegalArgumentException unsupportedPattern) {
            return false;
        }
    }

    /**
     * The simple names of the supported constraints that apply to an element as generation reads them:
     * declared on it, on a field's getter, or on the field that a constructor parameter initializes. A
     * generation report names them; without Bean Validation there are none.
     */
    static List<String> constraintNames(@Nullable AnnotatedElement element) {
        if (element == null || !AVAILABLE) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Class<? extends java.lang.annotation.Annotation> type : List.of(
                Null.class,
                NotNull.class,
                NotEmpty.class,
                NotBlank.class,
                Size.class,
                Min.class,
                Max.class,
                DecimalMin.class,
                DecimalMax.class,
                Positive.class,
                PositiveOrZero.class,
                Negative.class,
                NegativeOrZero.class,
                Past.class,
                PastOrPresent.class,
                Future.class,
                FutureOrPresent.class,
                AssertTrue.class,
                AssertFalse.class,
                Email.class,
                Pattern.class)) {
            // Generation reads a repeated @Pattern, and every other constraint once, as it does here.
            boolean present = type == Pattern.class
                    ? !annotations(element, Pattern.class).isEmpty()
                    : annotation(element, type) != null;
            if (present) {
                names.add(type.getSimpleName());
            }
        }
        return names;
    }

    private static <A extends java.lang.annotation.Annotation> @Nullable A annotation(
            AnnotatedElement element, Class<A> type) {
        if (element instanceof RecordComponent component) {
            return component.getAccessor().getAnnotation(type);
        }
        A direct = element.getAnnotation(type);
        if (direct != null) {
            return direct;
        }
        if (element instanceof Field field) {
            return accessorAnnotation(field, type);
        }
        if (element instanceof Parameter parameter) {
            Field field = initializedField(parameter);
            return field != null ? annotation(field, type) : null;
        }
        return null;
    }

    private static <A extends java.lang.annotation.Annotation> List<A> annotations(
            AnnotatedElement element, Class<A> type) {
        if (element instanceof RecordComponent component) {
            return List.of(component.getAccessor().getAnnotationsByType(type));
        }
        List<A> direct = List.of(element.getAnnotationsByType(type));
        if (!direct.isEmpty()) {
            return direct;
        }
        if (element instanceof Field field) {
            return accessorAnnotations(field, type);
        }
        if (element instanceof Parameter parameter) {
            Field field = initializedField(parameter);
            return field != null ? annotations(field, type) : List.of();
        }
        return List.of();
    }

    /**
     * Whether a Bean Validation constraint annotates a type argument within this type, at any depth and
     * inside array components, as in {@code List<@Email String>} or {@code List<@Email String>[]}. These
     * are the type nodes whose constraints apply. An array's component type itself does not count,
     * because its constraints do not apply, and nor do other type annotations, such as JSpecify's
     * {@code @Nullable}.
     */
    static boolean constrainsTypeArguments(AnnotatedType type) {
        if (type instanceof AnnotatedArrayType array) {
            return constrainsTypeArguments(array.getAnnotatedGenericComponentType());
        }
        if (!(type instanceof AnnotatedParameterizedType parameterized)) {
            return false;
        }
        for (AnnotatedType argument : parameterized.getAnnotatedActualTypeArguments()) {
            for (java.lang.annotation.Annotation annotation : argument.getAnnotations()) {
                if (annotation.annotationType().getPackageName().equals(CONSTRAINT_PACKAGE)) {
                    return true;
                }
            }
            if (constrainsTypeArguments(argument)) {
                return true;
            }
        }
        return false;
    }

    private static <A extends java.lang.annotation.Annotation> List<A> accessorAnnotations(Field field, Class<A> type) {
        String capitalized = Character.toUpperCase(field.getName().charAt(0))
                + field.getName().substring(1);
        String[] names = field.getType() == boolean.class || field.getType() == Boolean.class
                ? new String[] {field.getName(), "is" + capitalized, "get" + capitalized}
                : new String[] {field.getName(), "get" + capitalized};
        for (String name : names) {
            List<A> declared = declaredMethodAnnotations(field.getDeclaringClass(), name, type);
            if (!declared.isEmpty()) {
                return declared;
            }
            List<A> inherited = interfaceMethodAnnotations(field.getDeclaringClass(), name, type);
            if (!inherited.isEmpty()) {
                return inherited;
            }
        }
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> List<A> interfaceMethodAnnotations(
            Class<?> owner, String name, Class<A> type) {
        for (Method method : ACCESSOR_METHODS.get(owner).interfaces().getOrDefault(name, List.of())) {
            List<A> found = List.of(method.getAnnotationsByType(type));
            if (!found.isEmpty()) {
                return found;
            }
        }
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> List<A> declaredMethodAnnotations(
            Class<?> owner, String name, Class<A> type) {
        for (Method method : ACCESSOR_METHODS.get(owner).declared().getOrDefault(name, List.of())) {
            List<A> found = List.of(method.getAnnotationsByType(type));
            if (!found.isEmpty()) {
                return found;
            }
        }
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> @Nullable A accessorAnnotation(
            Field field, Class<A> type) {
        String capitalized = Character.toUpperCase(field.getName().charAt(0))
                + field.getName().substring(1);
        String[] names = field.getType() == boolean.class || field.getType() == Boolean.class
                ? new String[] {field.getName(), "is" + capitalized, "get" + capitalized}
                : new String[] {field.getName(), "get" + capitalized};
        for (String name : names) {
            A annotation = declaredMethodAnnotation(field.getDeclaringClass(), name, type);
            if (annotation != null) {
                return annotation;
            }
            annotation = interfaceMethodAnnotation(field.getDeclaringClass(), name, type);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static <A extends java.lang.annotation.Annotation> @Nullable A interfaceMethodAnnotation(
            Class<?> owner, String name, Class<A> type) {
        for (Method method : ACCESSOR_METHODS.get(owner).interfaces().getOrDefault(name, List.of())) {
            A annotation = method.getAnnotation(type);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static <A extends java.lang.annotation.Annotation> @Nullable A declaredMethodAnnotation(
            Class<?> owner, String name, Class<A> type) {
        for (Method method : ACCESSOR_METHODS.get(owner).declared().getOrDefault(name, List.of())) {
            A annotation = method.getAnnotation(type);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static AccessorMethods buildAccessorMethods(Class<?> owner) {
        Map<String, List<Method>> declared = new LinkedHashMap<>();
        Class<?> current = owner;
        while (current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0) {
                    declared.computeIfAbsent(accessorName(method), ignored -> new ArrayList<>())
                            .add(method);
                }
            }
            current = current.getSuperclass();
        }

        Map<String, List<Method>> interfaces = new LinkedHashMap<>();
        for (Class<?> interfaceType : owner.getInterfaces()) {
            for (Method method : interfaceType.getMethods()) {
                if (method.getParameterCount() == 0) {
                    interfaces
                            .computeIfAbsent(accessorName(method), ignored -> new ArrayList<>())
                            .add(method);
                }
            }
        }
        return new AccessorMethods(immutableMethodMap(declared), immutableMethodMap(interfaces));
    }

    /**
     * The name an accessor is looked up by. Kotlin mangles the name of a getter whose property has a
     * value class type, as in {@code getId-OLSiqns}, so the part from the hyphen, which no Java name
     * holds, is dropped.
     */
    private static String accessorName(Method method) {
        return MANGLED_NAME_SUFFIX.matcher(method.getName()).replaceFirst("");
    }

    private static Map<String, List<Method>> immutableMethodMap(Map<String, List<Method>> methods) {
        Map<String, List<Method>> immutable = new LinkedHashMap<>(methods.size());
        methods.forEach((name, values) -> immutable.put(name, List.copyOf(values)));
        return Map.copyOf(immutable);
    }
}
