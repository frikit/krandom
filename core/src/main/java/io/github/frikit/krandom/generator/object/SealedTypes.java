/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jspecify.annotations.Nullable;

/**
 * The classes object generation builds for a sealed interface or abstract sealed class.
 *
 * <p>Such a type is sealed when the construction adapter that supports it declares it so, as the
 * Kotlin adapter does for a Kotlin sealed class, or else when the JVM lists its permitted subclasses.
 * A concrete sealed class is generated as itself, and so is a sealed JDK type, which is not generated
 * reflectively.
 */
final class SealedTypes {

    private SealedTypes() {}

    /** Whether {@code type} is an interface or abstract class, and not an enum, which has constants. */
    static boolean isAbstract(Class<?> type) {
        return !type.isEnum() && (type.isInterface() || Modifier.isAbstract(type.getModifiers()));
    }

    /** Whether {@code type} is an interface or abstract class that is sealed. */
    static boolean isSealed(Class<?> type, UniqueFieldTracker tracker) {
        return directSubclasses(type, tracker) != null;
    }

    /**
     * The classes a value of sealed {@code type} is generated as, in binary-name order: a subclass with
     * a type override, the implementation an {@code objectSubtype} mapping names for a subclass, a
     * concrete subclass, an enum with constants, and the classes of a sealed subclass that is abstract,
     * at any depth.
     *
     * @throws UnsupportedOperationException when a subclass is abstract and none of these, or when no
     *     class remains
     * @throws IllegalStateException when an adapter lists a class that is not a subclass of the type
     */
    static List<Class<?>> concreteSubclasses(Class<?> type, ObjectGeneratorConfig config, UniqueFieldTracker tracker) {
        Set<Class<?>> classes = new TreeSet<>(Comparator.comparing(Class::getName));
        collect(type, type, config, tracker, classes);
        if (classes.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Sealed type " + type.getName() + " has no subclass to generate; register a type override for it");
        }
        return List.copyOf(classes);
    }

    private static void collect(
            Class<?> root,
            Class<?> sealedType,
            ObjectGeneratorConfig config,
            UniqueFieldTracker tracker,
            Set<Class<?>> classes) {
        for (Class<?> subclass : Objects.requireNonNull(directSubclasses(sealedType, tracker))) {
            // A proper subclass also rules out a cycle, so the walk ends.
            if (subclass == sealedType || !sealedType.isAssignableFrom(subclass)) {
                throw new IllegalStateException("Sealed type " + sealedType.getName() + " lists " + subclass.getName()
                        + ", which is not a subclass of it");
            }
            Class<?> mapped = config.resolveSubtype(subclass);
            if (config.getContextualTypeOverride(subclass).isPresent()
                    || config.getTypeOverride(subclass).isPresent()) {
                classes.add(subclass);
            } else if (mapped != subclass) {
                classes.add(mapped);
            } else if (subclass.isEnum()) {
                if (subclass.getEnumConstants().length > 0) {
                    classes.add(subclass);
                }
            } else if (isSealed(subclass, tracker)) {
                collect(root, subclass, config, tracker, classes);
            } else if (isAbstract(subclass)) {
                throw new UnsupportedOperationException("Sealed type " + root.getName() + " permits "
                        + subclass.getName() + ", which is abstract and not sealed; map it with objectSubtype or "
                        + "register a type override for it");
            } else {
                classes.add(subclass);
            }
        }
    }

    private static @Nullable List<Class<?>> directSubclasses(Class<?> type, UniqueFieldTracker tracker) {
        if (!isAbstract(type) || type.getClassLoader() == null) {
            return null;
        }
        Optional<List<Class<?>>> declared = tracker.constructionAdapter(type, ObjectGenerator::constructionAdapterFor)
                .flatMap(adapter -> adapter.sealedSubclasses(type));
        if (declared.isPresent()) {
            return List.copyOf(declared.get());
        }
        Class<?>[] permitted = type.getPermittedSubclasses();
        return permitted != null ? List.of(permitted) : null;
    }
}
