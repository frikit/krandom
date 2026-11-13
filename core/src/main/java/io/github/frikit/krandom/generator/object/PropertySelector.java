/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.io.Serializable;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Serializable, type-safe reference to a JavaBean getter or record accessor.
 *
 * <p>Use an unbound method reference such as {@code User::getEmail} or
 * {@code UserRecord::email}. Arbitrary lambda expressions are rejected because they do not
 * identify one stable property. The property may be nullable, as in
 * {@code PropertySelector<User, @Nullable String>} for a getter that may return {@code null}.
 *
 * @param <T> owning type
 * @param <V> property value type, which may be nullable
 */
@FunctionalInterface
public interface PropertySelector<T, V extends @Nullable Object> extends Function<T, V>, Serializable {}
