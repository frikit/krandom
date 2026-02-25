/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.base.BigDecimalGenerator;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.base.LongGenerator;
import org.github.krandom.generator.base.RegexGenerator;
import org.github.krandom.generator.base.StringGenerator;

import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;

/**
 * Derives a constrained {@link Generator} from Bean Validation annotations on a field.
 */
final class BeanValidationSupport {

    private BeanValidationSupport() { }

    /**
     * Returns a constrained {@link Generator} for the given element and raw type,
     * or {@code null} if no relevant Bean Validation annotation is present.
     *
     * @param element the annotated field or record component
     * @param rawType the field's erasure
     * @return a constraint-respecting generator, or {@code null}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static Generator<?> constraintGeneratorFor(AnnotatedElement element, Class<?> rawType) {

        // ── @Size on String ───────────────────────────────────────────────────
        if (rawType == String.class) {
            Size size = element.getAnnotation(Size.class);
            if (size != null) {
                int lo = Math.max(1, size.min());
                int hi = Math.max(lo, size.max());
                return StringGenerator.letters(lo, hi);
            }
            Email email = element.getAnnotation(Email.class);
            if (email != null) {
                return new RegexGenerator("[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)");
            }
            Pattern pattern = element.getAnnotation(Pattern.class);
            if (pattern != null) {
                return new RegexGenerator(pattern.regexp());
            }
        }

        // ── @Min / @Max on int / Integer ──────────────────────────────────────
        if (rawType == int.class || rawType == Integer.class) {
            return intGeneratorFor(element);
        }

        // ── @Min / @Max on long / Long ────────────────────────────────────────
        if (rawType == long.class || rawType == Long.class) {
            return longGeneratorFor(element);
        }

        // ── @DecimalMin / @DecimalMax on BigDecimal ───────────────────────────
        if (rawType == BigDecimal.class) {
            DecimalMin dmin = element.getAnnotation(DecimalMin.class);
            DecimalMax dmax = element.getAnnotation(DecimalMax.class);
            if (dmin != null && dmax != null) {
                return new BigDecimalGenerator(new BigDecimal(dmin.value()), new BigDecimal(dmax.value()));
            }
        }

        return null;
    }

    private static Generator<Integer> intGeneratorFor(AnnotatedElement element) {
        Min  min  = element.getAnnotation(Min.class);
        Max  max  = element.getAnnotation(Max.class);
        Positive         pos  = element.getAnnotation(Positive.class);
        PositiveOrZero   poz  = element.getAnnotation(PositiveOrZero.class);
        Negative         neg  = element.getAnnotation(Negative.class);
        NegativeOrZero   noz  = element.getAnnotation(NegativeOrZero.class);

        if (pos  != null) return new IntGenerator(1, Integer.MAX_VALUE);
        if (poz  != null) return new IntGenerator(0, Integer.MAX_VALUE);
        if (neg  != null) return new IntGenerator(Integer.MIN_VALUE, -1);
        if (noz  != null) return new IntGenerator(Integer.MIN_VALUE, 0);
        if (min != null && max != null) {
            return new IntGenerator((int) min.value(), (int) max.value());
        }
        if (min != null) return new IntGenerator((int) min.value(), Integer.MAX_VALUE);
        if (max != null) return new IntGenerator(Integer.MIN_VALUE, (int) max.value());
        return null;
    }

    private static Generator<Long> longGeneratorFor(AnnotatedElement element) {
        Min  min  = element.getAnnotation(Min.class);
        Max  max  = element.getAnnotation(Max.class);
        Positive         pos  = element.getAnnotation(Positive.class);
        PositiveOrZero   poz  = element.getAnnotation(PositiveOrZero.class);
        Negative         neg  = element.getAnnotation(Negative.class);
        NegativeOrZero   noz  = element.getAnnotation(NegativeOrZero.class);

        if (pos  != null) return new LongGenerator(1L, Long.MAX_VALUE);
        if (poz  != null) return new LongGenerator(0L, Long.MAX_VALUE);
        if (neg  != null) return new LongGenerator(Long.MIN_VALUE, -1L);
        if (noz  != null) return new LongGenerator(Long.MIN_VALUE, 0L);
        if (min != null && max != null) {
            return new LongGenerator(min.value(), max.value());
        }
        if (min != null) return new LongGenerator(min.value(), Long.MAX_VALUE);
        if (max != null) return new LongGenerator(Long.MIN_VALUE, max.value());
        return null;
    }
}
