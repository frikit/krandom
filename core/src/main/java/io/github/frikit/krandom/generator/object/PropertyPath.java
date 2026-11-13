/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable, type-safe property path used by {@link ObjectFaker}.
 *
 * <p>Root paths use {@link #of(PropertySelector)}. Nested paths compose without string literals:
 *
 * <pre>{@code
 * PropertyPath<User, String> city =
 *     PropertyPath.of(User::getAddress).then(Address::getCity);
 * }</pre>
 *
 * <p>A selected value may be nullable, as in {@code PropertyPath<User, @Nullable Address>}, and a path
 * continues past a nullable value with an accessor of its non-null type.
 *
 * @param <T> root object type
 * @param <V> selected value type, which may be nullable
 */
public final class PropertyPath<T, V extends @Nullable Object> {

    private final List<String> segments;
    private final String path;

    private PropertyPath(List<String> segments) {
        this.segments = List.copyOf(segments);
        this.path = String.join(".", this.segments);
    }

    /**
     * Creates a root property path from a getter or record accessor method reference.
     *
     * @param selector unbound accessor method reference
     * @param <T> root type
     * @param <V> property type, which may be nullable
     * @return immutable root path
     */
    public static <T, V extends @Nullable Object> PropertyPath<T, V> of(PropertySelector<T, V> selector) {
        return new PropertyPath<>(List.of(propertyName(selector)));
    }

    /**
     * Appends a nested getter or record accessor to this path.
     *
     * @param selector accessor on the current value type, or on its non-null type where it is nullable
     * @param <N> nested value type, which may be nullable
     * @return a new composed path
     */
    public <N extends @Nullable Object> PropertyPath<T, N> then(PropertySelector<@NonNull V, N> selector) {
        List<String> composed = new ArrayList<>(segments.size() + 1);
        composed.addAll(segments);
        composed.add(propertyName(selector));
        return new PropertyPath<>(composed);
    }

    /**
     * Returns the validated dot-separated path consumed by object generation.
     *
     * @return property path
     */
    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PropertyPath<?, ?> that && segments.equals(that.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    private static String propertyName(PropertySelector<?, ?> selector) {
        Objects.requireNonNull(selector, "selector must not be null");
        SerializedLambda lambda = serializedLambda(selector);
        int methodKind = lambda.getImplMethodKind();
        if (methodKind != MethodHandleInfo.REF_invokeVirtual && methodKind != MethodHandleInfo.REF_invokeInterface) {
            throw invalidSelector(lambda.getImplMethodName());
        }

        String methodName = lambda.getImplMethodName();
        if (methodName.startsWith("lambda$")) {
            throw invalidSelector(methodName);
        }
        if (isBeanAccessor(methodName, "get")) {
            return decapitalize(methodName.substring(3));
        }
        if (isBeanAccessor(methodName, "is")) {
            return decapitalize(methodName.substring(2));
        }
        if (methodName.indexOf('$') >= 0) {
            throw invalidSelector(methodName);
        }
        return methodName;
    }

    private static boolean isBeanAccessor(String methodName, String prefix) {
        return methodName.length() > prefix.length()
                && methodName.startsWith(prefix)
                && Character.isUpperCase(methodName.charAt(prefix.length()));
    }

    private static String decapitalize(String value) {
        if (value.length() > 1 && Character.isUpperCase(value.charAt(1))) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static SerializedLambda serializedLambda(PropertySelector<?, ?> selector) {
        try {
            Method replacement = selector.getClass().getDeclaredMethod("writeReplace");
            replacement.setAccessible(true);
            Object serialized = replacement.invoke(selector);
            if (serialized instanceof SerializedLambda lambda) {
                return lambda;
            }
            throw new IllegalArgumentException("Property selector must be an unbound accessor method reference");
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("Property selector must be an unbound accessor method reference", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Could not inspect property selector method reference", e.getTargetException());
        }
    }

    private static IllegalArgumentException invalidSelector(String methodName) {
        return new IllegalArgumentException(
                "Property selector must be an unbound getter or record accessor method reference, got '" + methodName
                        + "'");
    }
}
