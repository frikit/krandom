/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a generator class to use for a specific field or record component.
 *
 * <p>The referenced generator must have either:
 * <ul>
 *   <li>a no-arg constructor, or</li>
 *   <li>a constructor that matches the declared {@link RandomizerArgument} list.</li>
 * </ul>
 */
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Randomizer {

    /**
     * Generator class used to produce the field/component value.
     */
    Class<? extends Generator<?>> value();
}
