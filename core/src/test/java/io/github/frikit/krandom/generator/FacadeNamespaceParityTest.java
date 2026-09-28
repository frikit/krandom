/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the {@link Generators} facade and the fluent namespaces ({@code Generators.person()},
 * {@code Generators.finance()}, …) in step: every namespace generator has a facade factory, and
 * locale-aware namespace factories have a locale-aware facade overload.
 */
@DisplayName("Generators facade and namespaces stay in step")
class FacadeNamespaceParityTest {

    /**
     * Namespace methods whose facade factory intentionally uses a different, more specific name.
     * Keys are {@code NamespaceSimpleName#method}; values are the facade method names.
     */
    private static final Map<String, String> FACADE_ALIASES = Map.of(
        "CommerceGenerators#product", "ofProductInfo",
        "CommerceGenerators#order", "ofOrderInfo",
        "CommerceGenerators#shipment", "ofShipmentInfo",
        "IdentifierGenerators#mask", "ofIdentifierMask",
        "NetworkGenerators#ip", "ofIP",
        "NetworkGenerators#ipv4", "ofIPv4",
        "NetworkGenerators#ipv6", "ofIPv6"
    );

    /**
     * Every namespace type exposed by the facade, discovered from {@code Generators}' public
     * static no-arg methods that return a type from the {@code namespace} package.
     */
    static List<Class<?>> namespaceTypes() {
        return Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getParameterCount() == 0)
            .map(Method::getReturnType)
            .filter(type -> type.getPackageName().endsWith(".generator.namespace"))
            .distinct()
            .sorted((left, right) -> left.getSimpleName().compareTo(right.getSimpleName()))
            .toList();
    }

    private static Set<String> facadeMethods(Class<?>... parameterTypes) {
        return Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> Arrays.equals(method.getParameterTypes(), parameterTypes))
            .map(Method::getName)
            .collect(Collectors.toSet());
    }

    private static List<Method> namespaceMethods(Class<?> namespace) {
        return Arrays.stream(namespace.getMethods())
            .filter(method -> method.getDeclaringClass() == namespace)
            .filter(method -> !Modifier.isStatic(method.getModifiers()))
            .filter(method -> !method.isAnnotationPresent(Deprecated.class))
            .toList();
    }

    private static String facadeName(Class<?> namespace, String method) {
        String alias = FACADE_ALIASES.get(namespace.getSimpleName() + "#" + method);
        return alias != null ? alias : "of" + Character.toUpperCase(method.charAt(0)) + method.substring(1);
    }

    @Test
    @DisplayName("the facade exposes all eight namespaces")
    void discoversNamespaces() {
        assertEquals(List.of("CommerceGenerators", "DateTimeGenerators", "FinanceGenerators",
                             "IdentifierGenerators", "LocationGenerators", "NetworkGenerators",
                             "PersonGenerators", "TextGenerators"),
                     namespaceTypes().stream().map(Class::getSimpleName).toList());
    }

    @Test
    @DisplayName("every namespace generator has a facade factory")
    void everyNamespaceMethodHasFacadeFactory() {
        Set<String> facade = Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());
        List<String> missing = new ArrayList<>();
        for (Class<?> namespace : namespaceTypes()) {
            for (Method method : namespaceMethods(namespace)) {
                String expected = facadeName(namespace, method.getName());
                if (!facade.contains(expected)) {
                    missing.add(namespace.getSimpleName() + "#" + method.getName() + " -> Generators." + expected);
                }
            }
        }
        assertTrue(missing.isEmpty(), "facade factories missing for " + missing);
    }

    @Test
    @DisplayName("locale-aware namespace factories have locale-aware facade overloads")
    void localeOverloadsStayInStep() {
        Set<String> facadeLocale = facadeMethods(Locale.class);
        Set<String> missing = new LinkedHashSet<>();
        for (Class<?> namespace : namespaceTypes()) {
            for (Method method : namespaceMethods(namespace)) {
                if (Arrays.equals(method.getParameterTypes(), new Class<?>[] {Locale.class})) {
                    String expected = facadeName(namespace, method.getName());
                    if (!facadeLocale.contains(expected)) {
                        missing.add("Generators." + expected + "(Locale)");
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "locale overloads missing for " + missing);
    }

    @Test
    @DisplayName("aliases only cover namespace methods that exist")
    void aliasesAreCurrent() {
        Set<String> declared = namespaceTypes().stream()
            .flatMap(namespace -> namespaceMethods(namespace).stream()
                .map(method -> namespace.getSimpleName() + "#" + method.getName()))
            .collect(Collectors.toSet());
        assertTrue(declared.containsAll(FACADE_ALIASES.keySet()),
                   "stale aliases: " + FACADE_ALIASES.keySet().stream().filter(key -> !declared.contains(key)).toList());
    }
}
