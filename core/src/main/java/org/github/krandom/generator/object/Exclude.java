/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as excluded from {@link ObjectGenerator} random population.
 *
 * <p>When {@link ObjectGenerator} encounters a field annotated with {@code @Exclude},
 * it skips population entirely, leaving the field at its initialized value
 * ({@code null} for reference types, {@code 0}/{@code false} for primitives).
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   public class Person {
 *       private String username;
 *
 *       @Exclude
 *       private String password;   // never populated by ObjectGenerator
 *   }
 *
 *   Person p = new ObjectGenerator<>(Person.class).generate();
 *   assertNotNull(p.getUsername());
 *   assertNull(p.getPassword());   // excluded
 * }</pre>
 *
 * <p>Exclusions can also be registered programmatically via
 * {@link ObjectGeneratorConfig.Builder#exclude(java.util.function.Predicate)},
 * {@link ObjectGeneratorConfig.Builder#excludeField(String)}, and
 * {@link ObjectGeneratorConfig.Builder#excludeType(Class)}.
 *
 * @see ObjectGeneratorConfig
 * @see FieldPredicates
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Exclude {
}
