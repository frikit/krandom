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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * Derives a constrained {@link Generator} from Bean Validation annotations on a field.
 */
final class BeanValidationSupport {

    private static final int SIZE_UNBOUNDED_CAP = 255;
    private static final BigDecimal DECIMAL_DEFAULT_SPAN = new BigDecimal("1000000");
    private static final BigDecimal DECIMAL_STEP = new BigDecimal("0.01");
    private static final BigDecimal BIG_DECIMAL_LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal BIG_DECIMAL_LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    private BeanValidationSupport() {
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
        return constraintGeneratorFor(element, rawType, new Random());
    }

    static Generator<?> constraintGeneratorFor(AnnotatedElement element, Class<?> rawType, Random random) {
        Objects.requireNonNull(random, "random must not be null");

        if (element == null) {
            return null;
        }
        if (annotation(element, Null.class) != null && !rawType.isPrimitive()) {
            return () -> null;
        }
        if (rawType == boolean.class || rawType == Boolean.class) {
            if (annotation(element, AssertTrue.class) != null) {
                return () -> Boolean.TRUE;
            }
            if (annotation(element, AssertFalse.class) != null) {
                return () -> Boolean.FALSE;
            }
        }
        if (rawType == String.class) {
            if (annotation(element, Email.class) != null) {
                return new RegexGenerator("[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)");
            }
            Pattern pattern = annotation(element, Pattern.class);
            if (pattern != null) {
                return new RegexGenerator(pattern.regexp());
            }
            Generator<String> numericStringGenerator = numericStringGeneratorFor(element, random);
            if (numericStringGenerator != null) {
                return numericStringGenerator;
            }
            SizeRange size = sizeRangeFor(element);
            if (size != null || annotation(element, NotBlank.class) != null) {
                return stringGeneratorFor(size, annotation(element, NotBlank.class) != null, random);
            }
        }
        Generator<?> numericGenerator = numericGeneratorFor(element, rawType, random);
        if (numericGenerator != null) {
            return numericGenerator;
        }
        return temporalGeneratorFor(element, rawType, random);
    }

    static boolean hasNullConstraint(AnnotatedElement element) {
        return element != null && annotation(element, Null.class) != null;
    }

    static boolean hasSizeConstraint(AnnotatedElement element) {
        return sizeRangeFor(element) != null;
    }

    static SizeRange sizeRangeFor(AnnotatedElement element) {
        Size size = annotation(element, Size.class);
        if (size == null) {
            return null;
        }
        int min = Math.max(0, size.min());
        int max = size.max() == Integer.MAX_VALUE ? Math.max(min, SIZE_UNBOUNDED_CAP) : size.max();
        return new SizeRange(min, Math.max(min, max));
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
        int min = notBlank ? 1 : 0;
        int max = SIZE_UNBOUNDED_CAP;
        if (size != null) {
            min = Math.max(min, size.min());
            max = Math.max(min, size.max());
        }
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

    private static Generator<String> numericStringGeneratorFor(AnnotatedElement element, Random random) {
        if (!hasNumericConstraint(element)) {
            return null;
        }
        BigDecimalBounds bounds = decimalBoundsFor(element);
        if (hasDecimalConstraint(element)) {
            return () -> randomBigDecimal(random, bounds).stripTrailingZeros().toPlainString();
        }
        long min = clampToLong(bounds.min().setScale(0, RoundingMode.CEILING), Long.MIN_VALUE, Long.MAX_VALUE);
        long max = clampToLong(bounds.max().setScale(0, RoundingMode.FLOOR), Long.MIN_VALUE, Long.MAX_VALUE);
        return () -> Long.toString(randomLongInclusive(random, min, Math.max(min, max)));
    }

    private static Generator<?> numericGeneratorFor(AnnotatedElement element, Class<?> rawType, Random random) {
        if (!hasNumericConstraint(element)) {
            return null;
        }
        BigDecimalBounds bounds = decimalBoundsFor(element);
        if (rawType == byte.class || rawType == Byte.class) {
            long min = clampToLong(bounds.min().setScale(0, RoundingMode.CEILING), Byte.MIN_VALUE, Byte.MAX_VALUE);
            long max = clampToLong(bounds.max().setScale(0, RoundingMode.FLOOR), Byte.MIN_VALUE, Byte.MAX_VALUE);
            return () -> (byte) randomLongInclusive(random, min, Math.max(min, max));
        }
        if (rawType == short.class || rawType == Short.class) {
            long min = clampToLong(bounds.min().setScale(0, RoundingMode.CEILING), Short.MIN_VALUE, Short.MAX_VALUE);
            long max = clampToLong(bounds.max().setScale(0, RoundingMode.FLOOR), Short.MIN_VALUE, Short.MAX_VALUE);
            return () -> (short) randomLongInclusive(random, min, Math.max(min, max));
        }
        if (rawType == int.class || rawType == Integer.class) {
            long min = clampToLong(bounds.min().setScale(0, RoundingMode.CEILING), Integer.MIN_VALUE, Integer.MAX_VALUE);
            long max = clampToLong(bounds.max().setScale(0, RoundingMode.FLOOR), Integer.MIN_VALUE, Integer.MAX_VALUE);
            return () -> (int) randomLongInclusive(random, min, Math.max(min, max));
        }
        if (rawType == long.class || rawType == Long.class || rawType == Number.class) {
            long min = clampToLong(bounds.min().setScale(0, RoundingMode.CEILING), Long.MIN_VALUE, Long.MAX_VALUE);
            long max = clampToLong(bounds.max().setScale(0, RoundingMode.FLOOR), Long.MIN_VALUE, Long.MAX_VALUE);
            return () -> randomLongInclusive(random, min, Math.max(min, max));
        }
        if (rawType == float.class || rawType == Float.class) {
            double min = Math.max(bounds.min().doubleValue(), -Float.MAX_VALUE);
            double max = Math.min(bounds.max().doubleValue(), Float.MAX_VALUE);
            return () -> (float) randomDouble(random, min, Math.max(min, max));
        }
        if (rawType == double.class || rawType == Double.class) {
            double min = bounds.min().max(BigDecimal.valueOf(-Double.MAX_VALUE)).doubleValue();
            double max = bounds.max().min(BigDecimal.valueOf(Double.MAX_VALUE)).doubleValue();
            return () -> randomDouble(random, min, Math.max(min, max));
        }
        if (rawType == BigInteger.class) {
            BigInteger min = bounds.min().setScale(0, RoundingMode.CEILING).toBigInteger();
            BigInteger max = bounds.max().setScale(0, RoundingMode.FLOOR).toBigInteger();
            return () -> randomBigInteger(random, min, max);
        }
        if (rawType == BigDecimal.class) {
            return () -> randomBigDecimal(random, bounds);
        }
        return null;
    }

    private static Generator<?> temporalGeneratorFor(AnnotatedElement element, Class<?> rawType, Random random) {
        TemporalConstraint constraint = temporalConstraintFor(element);
        if (constraint == null) {
            return null;
        }
        return () -> temporalValueFor(rawType, constraint, random);
    }

    private static Object temporalValueFor(Class<?> rawType, TemporalConstraint constraint, Random random) {
        long seconds = random.nextLong(60, 3650L * 24L * 60L * 60L + 1L);
        Instant instantNow = Instant.now();
        Instant instant = constraint.future() ? instantNow.plusSeconds(seconds) : instantNow.minusSeconds(seconds);
        LocalDate today = LocalDate.now();
        LocalDate date = constraint.future()
                         ? today.plusDays(random.nextLong(1, 3651))
                         : today.minusDays(random.nextLong(1, 3651));
        LocalDateTime dateTimeNow = LocalDateTime.now();
        LocalDateTime dateTime = constraint.future()
                                 ? dateTimeNow.plusSeconds(seconds)
                                 : dateTimeNow.minusSeconds(seconds);
        if (rawType == Instant.class) return instant;
        if (rawType == LocalDate.class) return date;
        if (rawType == LocalDateTime.class) return dateTime;
        if (rawType == ZonedDateTime.class) return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        if (rawType == OffsetDateTime.class) return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
        if (rawType == Date.class) return Date.from(instant);
        if (rawType == java.sql.Date.class) return java.sql.Date.valueOf(date);
        if (rawType == Timestamp.class) return Timestamp.from(instant);
        if (rawType == Calendar.class) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        }
        if (rawType == LocalTime.class) return localTimeFor(constraint);
        if (rawType == OffsetTime.class) return localTimeFor(constraint).atOffset(OffsetDateTime.now().getOffset());
        if (rawType == Year.class) return constraint.future() ? Year.now().plusYears(1) : Year.now().minusYears(1);
        if (rawType == YearMonth.class) return constraint.future() ? YearMonth.now().plusMonths(1) : YearMonth.now().minusMonths(1);
        if (rawType == MonthDay.class) {
            return MonthDay.from(date);
        }
        return null;
    }

    private static LocalTime localTimeFor(TemporalConstraint constraint) {
        LocalTime now = LocalTime.now();
        return constraint.future() ? now.plusNanos(1) : now.minusNanos(1);
    }

    private static TemporalConstraint temporalConstraintFor(AnnotatedElement element) {
        if (annotation(element, Future.class) != null || annotation(element, FutureOrPresent.class) != null) {
            return new TemporalConstraint(true);
        }
        if (annotation(element, Past.class) != null || annotation(element, PastOrPresent.class) != null) {
            return new TemporalConstraint(false);
        }
        return null;
    }

    private record TemporalConstraint(boolean future) {}

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

    private static boolean hasDecimalConstraint(AnnotatedElement element) {
        return annotation(element, DecimalMin.class) != null || annotation(element, DecimalMax.class) != null;
    }

    private static BigDecimalBounds decimalBoundsFor(AnnotatedElement element) {
        BigDecimal min = BIG_DECIMAL_LONG_MIN;
        BigDecimal max = BIG_DECIMAL_LONG_MAX;

        Min integralMin = annotation(element, Min.class);
        if (integralMin != null) {
            min = min.max(BigDecimal.valueOf(integralMin.value()));
        }
        Max integralMax = annotation(element, Max.class);
        if (integralMax != null) {
            max = max.min(BigDecimal.valueOf(integralMax.value()));
        }
        DecimalMin decimalMin = annotation(element, DecimalMin.class);
        if (decimalMin != null) {
            BigDecimal value = new BigDecimal(decimalMin.value());
            min = min.max(decimalMin.inclusive() ? value : value.add(DECIMAL_STEP));
        }
        DecimalMax decimalMax = annotation(element, DecimalMax.class);
        if (decimalMax != null) {
            BigDecimal value = new BigDecimal(decimalMax.value());
            max = max.min(decimalMax.inclusive() ? value : value.subtract(DECIMAL_STEP));
        }
        if (annotation(element, Positive.class) != null) {
            min = min.max(DECIMAL_STEP);
        }
        if (annotation(element, PositiveOrZero.class) != null) {
            min = min.max(BigDecimal.ZERO);
        }
        if (annotation(element, Negative.class) != null) {
            max = max.min(DECIMAL_STEP.negate());
        }
        if (annotation(element, NegativeOrZero.class) != null) {
            max = max.min(BigDecimal.ZERO);
        }
        if (min.compareTo(max) > 0) {
            max = min;
        }
        if (decimalMin != null && decimalMax == null && max.equals(BIG_DECIMAL_LONG_MAX)) {
            max = min.add(DECIMAL_DEFAULT_SPAN);
        }
        if (decimalMax != null && decimalMin == null && min.equals(BIG_DECIMAL_LONG_MIN)) {
            min = max.subtract(DECIMAL_DEFAULT_SPAN);
        }
        return new BigDecimalBounds(min, max);
    }

    private record BigDecimalBounds(BigDecimal min, BigDecimal max) {}

    private static BigDecimal randomBigDecimal(Random random, BigDecimalBounds bounds) {
        BigDecimal min = bounds.min();
        BigDecimal max = bounds.max();
        if (min.compareTo(max) >= 0) {
            return min;
        }
        int scale = Math.max(2, Math.max(min.scale(), max.scale()));
        BigDecimal span = max.subtract(min);
        BigDecimal sample = BigDecimal.valueOf(random.nextDouble());
        BigDecimal value = min.add(span.multiply(sample));
        return value.setScale(scale, RoundingMode.DOWN);
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

    private static long clampToLong(BigDecimal value, long min, long max) {
        if (value.compareTo(BigDecimal.valueOf(min)) < 0) {
            return min;
        }
        if (value.compareTo(BigDecimal.valueOf(max)) > 0) {
            return max;
        }
        return value.longValue();
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
