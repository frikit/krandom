/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Internal recursive representation of a declared Java type.
 */
record ResolvedType(
    Type declaredType,
    Kind kind,
    @Nullable Class<?> rawClass,
    List<ResolvedType> arguments,
    @Nullable ResolvedType componentType,
    @Nullable ResolvedType effectiveType,
    @Nullable String unresolvedReason
) {

    enum Kind {
        CLASS,
        PARAMETERIZED,
        GENERIC_ARRAY,
        WILDCARD,
        TYPE_VARIABLE,
        UNKNOWN
    }

    ResolvedType {
        Objects.requireNonNull(declaredType, "declaredType must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
        arguments = List.copyOf(arguments);
    }

    static ResolvedType resolve(Type type) {
        return resolve(type, Map.of());
    }

    static ResolvedType resolve(Type type, Map<? extends TypeVariable<?>, ? extends Type> bindings) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(bindings, "bindings must not be null");
        return resolve(type, Map.copyOf(bindings), new HashSet<>());
    }

    private static ResolvedType resolve(Type type,
                                        Map<? extends TypeVariable<?>, ? extends Type> bindings,
                                        Set<TypeVariable<?>> visiting) {
        if (type instanceof Class<?> rawClass) {
            ResolvedType component = rawClass.isArray()
                                     ? resolve(rawClass.getComponentType(), bindings, visiting)
                                     : null;
            return new ResolvedType(type, Kind.CLASS, rawClass, List.of(), component, null, null);
        }
        if (type instanceof ParameterizedType parameterized) {
            List<ResolvedType> arguments = new ArrayList<>();
            for (Type argument : parameterized.getActualTypeArguments()) {
                arguments.add(resolve(argument, bindings, visiting));
            }
            Class<?> rawClass = parameterized.getRawType() instanceof Class<?> candidate ? candidate : null;
            String reason = rawClass == null ? "parameterized type has no concrete raw class" : null;
            return new ResolvedType(type, Kind.PARAMETERIZED, rawClass, arguments, null, null, reason);
        }
        if (type instanceof GenericArrayType genericArray) {
            ResolvedType component = resolve(genericArray.getGenericComponentType(), bindings, visiting);
            Class<?> rawClass = component.rawClass() == null
                                ? null
                                : Array.newInstance(component.rawClass(), 0).getClass();
            String reason = rawClass == null ? "generic array component has no concrete runtime class" : null;
            return new ResolvedType(type, Kind.GENERIC_ARRAY, rawClass, List.of(), component, null, reason);
        }
        if (type instanceof WildcardType wildcard) {
            return resolveWildcard(wildcard, bindings, visiting);
        }
        if (type instanceof TypeVariable<?> variable) {
            return resolveVariable(variable, bindings, visiting);
        }
        return unresolved(type, Kind.UNKNOWN, "unsupported Type implementation: " + type.getClass().getName());
    }

    private static ResolvedType resolveWildcard(WildcardType wildcard,
                                                Map<? extends TypeVariable<?>, ? extends Type> bindings,
                                                Set<TypeVariable<?>> visiting) {
        Type[] lowerBounds = wildcard.getLowerBounds();
        if (lowerBounds.length == 1) {
            return withEffectiveType(wildcard, Kind.WILDCARD, resolve(lowerBounds[0], bindings, visiting));
        }
        Type[] upperBounds = wildcard.getUpperBounds();
        if (upperBounds.length == 1) {
            if (upperBounds[0] != Object.class) {
                return withEffectiveType(wildcard, Kind.WILDCARD, resolve(upperBounds[0], bindings, visiting));
            }
            return unresolved(wildcard, Kind.WILDCARD, "unbounded wildcard has no generation type");
        }
        return unresolved(wildcard, Kind.WILDCARD, "wildcard must declare exactly one upper bound");
    }

    private static ResolvedType resolveVariable(TypeVariable<?> variable,
                                                Map<? extends TypeVariable<?>, ? extends Type> bindings,
                                                Set<TypeVariable<?>> visiting) {
        if (!visiting.add(variable)) {
            return unresolved(variable, Kind.TYPE_VARIABLE, "recursive type bound for " + variable.getName());
        }
        try {
            Type bound = bindings.get(variable);
            if (bound != null && bound != variable) {
                return withEffectiveType(variable, Kind.TYPE_VARIABLE, resolve(bound, bindings, visiting));
            }
            Type[] bounds = variable.getBounds();
            if (bounds.length == 1) {
                if (bounds[0] != Object.class) {
                    return withEffectiveType(variable, Kind.TYPE_VARIABLE, resolve(bounds[0], bindings, visiting));
                }
                return unresolved(variable, Kind.TYPE_VARIABLE,
                                  "unbounded type variable has no generation type: " + variable.getName());
            }
            return unresolved(variable, Kind.TYPE_VARIABLE,
                              "multiple type-variable bounds are unsupported: " + variable.getName());
        } finally {
            visiting.remove(variable);
        }
    }

    private static ResolvedType withEffectiveType(Type declaredType, Kind kind, ResolvedType effectiveType) {
        return new ResolvedType(
            declaredType,
            kind,
            effectiveType.rawClass(),
            List.of(),
            null,
            effectiveType,
            effectiveType.unresolvedReason());
    }

    private static ResolvedType unresolved(Type type, Kind kind, String reason) {
        return new ResolvedType(type, kind, null, List.of(), null, null, reason);
    }

    String signature() {
        return declaredType.getTypeName();
    }

    ResolvedType argument(int index) {
        return arguments.get(index);
    }

    boolean isResolved() {
        if (rawClass == null) {
            return false;
        }
        if (componentType != null && !componentType.isResolved()) {
            return false;
        }
        if (effectiveType != null && !effectiveType.isResolved()) {
            return false;
        }
        return arguments.stream().allMatch(ResolvedType::isResolved);
    }
}
