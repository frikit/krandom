/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pins the kRandom seed for a test method or test class.
 *
 * <p>Annotating a test (or its class) registers {@link KrandomExtension} automatically, so
 * {@code @KrandomSeed} alone is enough — no separate {@code @ExtendWith} required. A
 * method-level annotation overrides a class-level one.
 *
 * <p>Exactly one of {@link #value()} (numeric seed) or {@link #text()} (string seed, derived
 * with the same {@code fnv1a64-v1} algorithm as {@link GeneratorConfig#deriveSeed(String)})
 * must be specified; setting both or neither is a configuration error.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   @KrandomSeed(42L)
 *   @Test
 *   void reproducible(GeneratorConfig config) {
 *       User user = Generators.ofObject(User.class, config).generate();
 *       // same user on every run
 *   }
 * }</pre>
 *
 * <p>Tests without {@code @KrandomSeed} (but extended with {@link KrandomExtension}) run with a
 * random per-test seed; when such a test fails, the extension reports the seed so the failing
 * run can be reproduced by pinning it with this annotation.
 *
 * <p>{@link Long#MIN_VALUE} is reserved as the "unset" sentinel for {@link #value()} and cannot
 * be used as a numeric seed; use a {@link #text()} seed if that exact value is ever needed.
 *
 * @see KrandomExtension
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(KrandomExtension.class)
public @interface KrandomSeed {

    /** Sentinel meaning "no numeric seed specified". */
    long UNSET = Long.MIN_VALUE;

    /**
     * Numeric seed to pin, mutually exclusive with {@link #text()}.
     *
     * @return the numeric seed, or {@link #UNSET} when not specified
     */
    long value() default UNSET;

    /**
     * String seed to pin, mutually exclusive with {@link #value()}; derived to a numeric seed
     * via {@link GeneratorConfig#deriveSeed(String)} and must not be blank.
     *
     * @return the string seed, or an empty string when not specified
     */
    String text() default "";
}
