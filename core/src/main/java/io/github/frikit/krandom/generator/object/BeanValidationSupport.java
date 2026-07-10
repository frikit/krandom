/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.base.RegexGenerator;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigInteger;
import java.math.BigDecimal;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

/**
 * Derives a constrained {@link Generator} from Bean Validation annotations on a field.
 */
final class BeanValidationSupport {

    private static final int SIZE_UNBOUNDED_CAP = 255;
    private static final int TEXT_GENERATION_ATTEMPTS = 256;
    private static final BigDecimal DECIMAL_DEFAULT_SPAN = new BigDecimal("1000000");
    private static final java.util.regex.Pattern EMAIL_SHAPE =
        java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Comparator<NumericBound> LOWER_BOUND_ORDER =
        Comparator.comparing(NumericBound::value).thenComparing(bound -> !bound.inclusive());
    private static final Comparator<NumericBound> UPPER_BOUND_ORDER =
        Comparator.comparing(NumericBound::value).thenComparing(NumericBound::inclusive);

    private BeanValidationSupport() {
    }

    record ConstraintModel(
        boolean constrained,
        boolean nullOnly,
        boolean required,
        SizeRange size,
        NumericRange numeric,
        Boolean assertion,
        TemporalConstraint temporal,
        TextConstraint text
    ) {

        private static final ConstraintModel NONE =
            new ConstraintModel(false, false, false, null, null, null, null, null);
    }

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
     * @param element the annotated field or record component
     * @param rawType the field's erasure
     * @return a constraint-respecting generator, or {@code null}
     */
    static Generator<?> constraintGeneratorFor(AnnotatedElement element, Class<?> rawType) {
        return constraintGeneratorFor(element, rawType, new Random(), Clock.systemDefaultZone());
    }

    static Generator<?> constraintGeneratorFor(AnnotatedElement element, Class<?> rawType, Random random) {
        return constraintGeneratorFor(element, rawType, random, Clock.systemDefaultZone());
    }

    static Generator<?> constraintGeneratorFor(AnnotatedElement element,
                                               Class<?> rawType,
                                               Random random,
                                               Clock clock) {
        ConstraintModel constraints = constraintModelFor(element, rawType);
        return constraintGeneratorFor(element, rawType, random, clock, constraints);
    }

    static Generator<?> constraintGeneratorFor(AnnotatedElement element,
                                               Class<?> rawType,
                                               Random random,
                                               Clock clock,
                                               ConstraintModel constraints) {
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
            return textGeneratorFor(constraints.text(), random);
        }
        Generator<?> numericGenerator = numericGeneratorFor(constraints.numeric(), rawType, random);
        if (numericGenerator != null) {
            return numericGenerator;
        }
        return temporalGeneratorFor(constraints.temporal(), rawType, random, clock);
    }

    static ConstraintModel constraintModelFor(AnnotatedElement element, Class<?> rawType) {
        if (element == null) {
            return ConstraintModel.NONE;
        }
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
        TextConstraint text = textConstraintFor(element, rawType, size, notBlank, numeric);
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

    static SizeRange sizeRangeFor(AnnotatedElement element) {
        Size size = annotation(element, Size.class);
        boolean requiresContent = annotation(element, NotEmpty.class) != null
                                  || annotation(element, NotBlank.class) != null;
        if (size == null && !requiresContent) {
            return null;
        }
        int declaredMin = size == null ? 0 : Math.max(0, size.min());
        int min = requiresContent ? Math.max(1, declaredMin) : declaredMin;
        int declaredMax = size == null ? Integer.MAX_VALUE : size.max();
        int max = declaredMax == Integer.MAX_VALUE ? Math.max(min, SIZE_UNBOUNDED_CAP) : declaredMax;
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

    private static TextConstraint textConstraintFor(AnnotatedElement element,
                                                    Class<?> rawType,
                                                    SizeRange size,
                                                    boolean nonBlank,
                                                    NumericRange numeric) {
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

        List<TextPattern> patterns = new ArrayList<>();
        for (Pattern pattern : patternAnnotations) {
            patterns.add(compileTextPattern(pattern.regexp(), pattern.flags(), true));
        }
        if (email != null) {
            boolean usefulSource = !".*".equals(email.regexp());
            patterns.add(compileTextPattern(email.regexp(), email.flags(), usefulSource));
        }
        return new TextConstraint(email != null, List.copyOf(patterns), size, nonBlank, numeric);
    }

    private static TextPattern compileTextPattern(String expression,
                                                  Pattern.Flag[] flags,
                                                  boolean source) {
        int compiledFlags = 0;
        for (Pattern.Flag flag : flags) {
            compiledFlags |= flag.getValue();
        }
        try {
            return new TextPattern(
                expression,
                java.util.regex.Pattern.compile(expression, compiledFlags),
                source);
        } catch (java.util.regex.PatternSyntaxException invalidPattern) {
            throw conflict("text constraint contains an invalid regular expression");
        }
    }

    private static Generator<String> textGeneratorFor(TextConstraint constraints, Random random) {
        List<Generator<String>> sources = new ArrayList<>();
        for (TextPattern pattern : constraints.patterns()) {
            if (pattern.source()) {
                try {
                    sources.add(new RegexGenerator(pattern.expression(), random.nextLong()));
                } catch (IllegalArgumentException unsupportedPattern) {
                    throw conflict("text constraint uses unsupported regular-expression syntax");
                }
            }
        }
        if (constraints.email()) {
            sources.add(new RegexGenerator(
                "[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)", random.nextLong()));
        }
        Generator<String> numericSource = numericStringGeneratorFor(constraints.numeric(), random);
        if (numericSource != null) {
            sources.add(numericSource);
        }
        if (constraints.size() != null) {
            sources.add(stringGeneratorFor(constraints.size(), constraints.nonBlank(), random));
        }

        return () -> {
            for (int attempt = 0; attempt < TEXT_GENERATION_ATTEMPTS; attempt++) {
                String candidate = sources.get(attempt % sources.size()).generate();
                if (constraints.accepts(candidate)) {
                    return candidate;
                }
            }
            throw conflict("text constraints did not produce a value in the bounded search budget");
        };
    }

    record TextPattern(String expression, java.util.regex.Pattern compiled, boolean source) {}

    record TextConstraint(boolean email,
                          List<TextPattern> patterns,
                          SizeRange size,
                          boolean nonBlank,
                          NumericRange numeric) {

        boolean accepts(String candidate) {
            if (size != null && (candidate.length() < size.min() || candidate.length() > size.max())) {
                return false;
            }
            if (nonBlank && candidate.isBlank()) {
                return false;
            }
            if (email && !EMAIL_SHAPE.matcher(candidate).matches()) {
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

    static int sizeFor(AnnotatedElement element, Random random, int defaultMin, int defaultMax) {
        SizeRange range = sizeRangeFor(element);
        if (range == null) {
            return defaultMin == defaultMax ? defaultMin : random.nextInt(defaultMin, defaultMax + 1);
        }
        return range.next(random);
    }

    record SizeRange(int min, int max) {
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

    private static Generator<String> numericStringGeneratorFor(NumericRange range, Random random) {
        if (range == null) {
            return null;
        }
        if (range.decimal()) {
            return () -> randomBigDecimal(random, range).stripTrailingZeros().toPlainString();
        }
        IntegralRange integral = integralRangeFor(range, String.class);
        return () -> randomBigInteger(random, integral.min(), integral.max()).toString();
    }

    private static Generator<?> numericGeneratorFor(NumericRange range, Class<?> rawType, Random random) {
        if (range == null) {
            return null;
        }
        if (rawType == byte.class || rawType == Byte.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomBigInteger(random, integral.min(), integral.max()).byteValueExact();
        }
        if (rawType == short.class || rawType == Short.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomBigInteger(random, integral.min(), integral.max()).shortValueExact();
        }
        if (rawType == int.class || rawType == Integer.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomBigInteger(random, integral.min(), integral.max()).intValueExact();
        }
        if (rawType == long.class || rawType == Long.class || rawType == Number.class) {
            IntegralRange integral = integralRangeFor(range, rawType);
            return () -> randomLongInclusive(random, integral.min().longValueExact(), integral.max().longValueExact());
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

    private static Generator<?> temporalGeneratorFor(TemporalConstraint constraint,
                                                     Class<?> rawType,
                                                     Random random,
                                                     Clock clock) {
        if (constraint == null) {
            return null;
        }
        return () -> temporalValueFor(rawType, constraint, random, clock);
    }

    private static Object temporalValueFor(Class<?> rawType,
                                           TemporalConstraint constraint,
                                           Random random,
                                           Clock clock) {
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
        if (rawType == Calendar.class) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(clock.getZone()));
            calendar.setTime(Date.from(instant));
            return calendar;
        }
        if (rawType == LocalTime.class) return localTimeFor(future, clock);
        if (rawType == OffsetTime.class) return localTimeFor(future, clock).atOffset(OffsetDateTime.now(clock).getOffset());
        if (rawType == Year.class) return future ? Year.now(clock).plusYears(1) : Year.now(clock).minusYears(1);
        if (rawType == YearMonth.class) return future ? YearMonth.now(clock).plusMonths(1) : YearMonth.now(clock).minusMonths(1);
        return monthDayFor(future, clock);
    }

    private static Object presentTemporalValueFor(Class<?> rawType, Clock clock) {
        Instant instant = clock.instant();
        LocalDate date = LocalDate.now(clock);
        if (rawType == Instant.class) return instant;
        if (rawType == LocalDate.class) return date;
        if (rawType == LocalDateTime.class) return LocalDateTime.now(clock);
        if (rawType == ZonedDateTime.class) return ZonedDateTime.now(clock);
        if (rawType == OffsetDateTime.class) return OffsetDateTime.now(clock);
        if (rawType == Date.class) return Date.from(instant);
        if (rawType == java.sql.Date.class) return new java.sql.Date(instant.toEpochMilli());
        if (rawType == Timestamp.class) return Timestamp.from(instant);
        if (rawType == Calendar.class) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(clock.getZone()));
            calendar.setTime(Date.from(instant));
            return calendar;
        }
        if (rawType == LocalTime.class) return LocalTime.now(clock);
        if (rawType == OffsetTime.class) return OffsetTime.now(clock);
        if (rawType == Year.class) return Year.now(clock);
        if (rawType == YearMonth.class) return YearMonth.now(clock);
        return MonthDay.now(clock);
    }

    private static LocalTime localTimeFor(boolean future, Clock clock) {
        LocalTime now = LocalTime.now(clock);
        if (future && now.equals(LocalTime.MAX)) {
            throw conflict("@Future has no representable LocalTime after the configured clock");
        }
        if (!future && now.equals(LocalTime.MIN)) {
            throw conflict("@Past has no representable LocalTime before the configured clock");
        }
        return future ? LocalTime.MAX : LocalTime.MIN;
    }

    private static MonthDay monthDayFor(boolean future, Clock clock) {
        MonthDay today = MonthDay.now(clock);
        MonthDay boundary = future ? MonthDay.of(12, 31) : MonthDay.of(1, 1);
        if (today.equals(boundary)) {
            throw conflict("temporal constraint has no representable MonthDay at the configured clock");
        }
        return boundary;
    }

    private static TemporalConstraint temporalConstraintFor(AnnotatedElement element, Class<?> rawType) {
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
        TemporalDirection direction = allowsFuture
                                      ? TemporalDirection.FUTURE
                                      : allowsPast ? TemporalDirection.PAST : TemporalDirection.PRESENT;
        return new TemporalConstraint(direction);
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
               || rawType == LocalTime.class
               || rawType == OffsetTime.class
               || rawType == Year.class
               || rawType == YearMonth.class
               || rawType == MonthDay.class;
    }

    private enum TemporalDirection { PAST, PRESENT, FUTURE }

    record TemporalConstraint(TemporalDirection direction) {}

    private static Boolean assertionFor(AnnotatedElement element, Class<?> rawType) {
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

    private static NumericRange numericRangeFor(AnnotatedElement element, Class<?> rawType) {
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
            || (comparison == 0 && (!range.lower().inclusive() || !range.upper().inclusive()))) {
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

    private static IntegralRange integralRangeFor(NumericRange range, Class<?> rawType) {
        BigInteger domainMin = null;
        BigInteger domainMax = null;
        if (rawType == byte.class || rawType == Byte.class) {
            domainMin = BigInteger.valueOf(Byte.MIN_VALUE);
            domainMax = BigInteger.valueOf(Byte.MAX_VALUE);
        } else if (rawType == short.class || rawType == Short.class) {
            domainMin = BigInteger.valueOf(Short.MIN_VALUE);
            domainMax = BigInteger.valueOf(Short.MAX_VALUE);
        } else if (rawType == int.class || rawType == Integer.class) {
            domainMin = BigInteger.valueOf(Integer.MIN_VALUE);
            domainMax = BigInteger.valueOf(Integer.MAX_VALUE);
        } else if (rawType != BigInteger.class) {
            domainMin = BigInteger.valueOf(Long.MIN_VALUE);
            domainMax = BigInteger.valueOf(Long.MAX_VALUE);
        }

        BigInteger min = smallestInteger(range.lower());
        BigInteger max = largestInteger(range.upper());
        if (domainMin != null) {
            min = min == null ? domainMin : min.max(domainMin);
            max = max == null ? domainMax : max.min(domainMax);
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

    private static BigInteger smallestInteger(NumericBound lower) {
        if (lower == null) {
            return null;
        }
        BigInteger value = lower.value().setScale(0, RoundingMode.CEILING).toBigIntegerExact();
        if (!lower.inclusive() && lower.value().compareTo(new BigDecimal(value)) == 0) {
            return value.add(BigInteger.ONE);
        }
        return value;
    }

    private static BigInteger largestInteger(NumericBound upper) {
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

    private static Double floatingLower(NumericBound lower, boolean singlePrecision, double magnitude) {
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
        double candidate = singlePrecision ? lower.value().floatValue() : lower.value().doubleValue();
        BigDecimal represented = representedFloating(candidate, singlePrecision);
        if (represented.compareTo(lower.value()) < 0
            || (!lower.inclusive() && represented.compareTo(lower.value()) == 0)) {
            candidate = singlePrecision ? Math.nextUp((float) candidate) : Math.nextUp(candidate);
        }
        return candidate;
    }

    private static Double floatingUpper(NumericBound upper, boolean singlePrecision, double magnitude) {
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
        double candidate = singlePrecision ? upper.value().floatValue() : upper.value().doubleValue();
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

    record NumericRange(NumericBound lower, NumericBound upper, boolean decimal) {}

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
        BigDecimal offset = DECIMAL_DEFAULT_SPAN.multiply(
            BigDecimal.valueOf(0.25d + random.nextDouble() * 0.5d));
        return lower != null ? lower.value().add(offset) : Objects.requireNonNull(upper).value().subtract(offset);
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

    private static <A extends java.lang.annotation.Annotation> A annotation(AnnotatedElement element, Class<A> type) {
        if (element == null) {
            return null;
        }
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
        return null;
    }

    private static <A extends java.lang.annotation.Annotation> List<A> annotations(AnnotatedElement element,
                                                                                   Class<A> type) {
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
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> List<A> accessorAnnotations(Field field,
                                                                                            Class<A> type) {
        String capitalized = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        String[] names = field.getType() == boolean.class || field.getType() == Boolean.class
                         ? new String[] { field.getName(), "is" + capitalized, "get" + capitalized }
                         : new String[] { field.getName(), "get" + capitalized };
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
        Class<?> owner,
        String name,
        Class<A> type) {
        for (Class<?> interfaceType : owner.getInterfaces()) {
            try {
                Method method = interfaceType.getMethod(name);
                List<A> found = List.of(method.getAnnotationsByType(type));
                if (!found.isEmpty()) {
                    return found;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next interface.
            }
        }
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> List<A> declaredMethodAnnotations(
        Class<?> owner,
        String name,
        Class<A> type) {
        Class<?> current = owner;
        while (current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(name);
                method.setAccessible(true);
                List<A> found = List.of(method.getAnnotationsByType(type));
                if (!found.isEmpty()) {
                    return found;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next superclass.
            }
            current = current.getSuperclass();
        }
        return List.of();
    }

    private static <A extends java.lang.annotation.Annotation> A accessorAnnotation(Field field, Class<A> type) {
        String capitalized = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        String[] names = field.getType() == boolean.class || field.getType() == Boolean.class
                         ? new String[] { field.getName(), "is" + capitalized, "get" + capitalized }
                         : new String[] { field.getName(), "get" + capitalized };
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

    private static <A extends java.lang.annotation.Annotation> A interfaceMethodAnnotation(Class<?> owner,
                                                                                           String name,
                                                                                           Class<A> type) {
        for (Class<?> interfaceType : owner.getInterfaces()) {
            try {
                Method method = interfaceType.getMethod(name);
                A annotation = method.getAnnotation(type);
                if (annotation != null) {
                    return annotation;
                }
            } catch (NoSuchMethodException ignored) {
                // Try parent interfaces below.
            }
        }
        return null;
    }

    private static <A extends java.lang.annotation.Annotation> A declaredMethodAnnotation(Class<?> owner,
                                                                                          String name,
                                                                                          Class<A> type) {
        Class<?> current = owner;
        while (current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(name);
                method.setAccessible(true);
                A annotation = method.getAnnotation(type);
                if (annotation != null) {
                    return annotation;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next superclass.
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
