/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one constructor argument for a {@link Randomizer}-declared generator.
 *
 * <p>Arguments are resolved in source order using the provided {@link #type()} and
 * parsed from {@link #value()}.
 */
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RandomizerArguments.class)
@Documented
public @interface RandomizerArgument {

    /**
     * Constructor parameter type at the same position as this annotation.
     */
    Class<?> type();

    /**
     * Raw string value that will be converted to {@link #type()}.
     */
    String value();
}
