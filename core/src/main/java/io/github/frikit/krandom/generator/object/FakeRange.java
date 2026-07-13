/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constrains numeric field generation to a bounded range.
 *
 * <p>Supported field types: {@code int}/{@link Integer}, {@code long}/{@link Long},
 * {@code double}/{@link Double}, {@code float}/{@link Float},
 * {@code short}/{@link Short}, {@code byte}/{@link Byte}.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   public class Product {
 *       @FakeRange(min = 1, max = 100)
 *       private int quantity;
 *
 *       @FakeRange(min = 0, max = 10000)
 *       private double price;
 *   }
 *
 *   Product p = new ObjectGenerator<>(Product.class).generate();
 *   // p.quantity is in [1, 100)
 *   // p.price   is in [0.0, 10000.0)
 * }</pre>
 *
 * <p>This annotation takes precedence over the automatic semantic field-name resolution
 * and built-in type defaults, but is overridden by programmatic overrides registered via
 * {@link io.github.frikit.krandom.generator.GeneratorConfig.Builder}.
 *
 * @see Fake
 * @see Randomizer
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FakeRange {

    /**
     * Lower bound (inclusive).
     */
    long min() default 0;

    /**
     * Upper bound (exclusive).
     */
    long max() default Long.MAX_VALUE;
}
