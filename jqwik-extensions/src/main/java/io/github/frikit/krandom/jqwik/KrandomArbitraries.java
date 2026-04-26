/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jqwik;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Arbitraries;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Bridges krandom {@link Generator} instances to jqwik {@link Arbitrary} for
 * property-based testing.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   // From an existing generator
 *   Arbitrary<String> emails = KrandomArbitraries.fromGenerator(Generators.ofEmail());
 *
 *   // From a generator factory
 *   Arbitrary<String> names = KrandomArbitraries.fromFactory(Generators::ofFullName);
 *
 *   // Object generation
 *   Arbitrary<Person> people = KrandomArbitraries.forType(Person.class);
 * }</pre>
 */
public final class KrandomArbitraries {

    private KrandomArbitraries() {
        // utility class
    }

    /**
     * Wraps a krandom {@link Generator} as a jqwik {@link Arbitrary}.
     *
     * @param generator the krandom generator to bridge
     * @param <T>       the generated value type
     * @return a jqwik Arbitrary backed by the given generator
     */
    public static <T> Arbitrary<T> fromGenerator(Generator<T> generator) {
        Objects.requireNonNull(generator, "generator must not be null");
        return Arbitraries.randomValue(random -> generator.generate());
    }

    /**
     * Wraps a krandom {@link Generator} supplier as a jqwik {@link Arbitrary}.
     *
     * <p>The supplier is called once to create the generator instance.
     *
     * @param factory supplier that creates a krandom generator
     * @param <T>     the generated value type
     * @return a jqwik Arbitrary backed by the created generator
     */
    public static <T> Arbitrary<T> fromFactory(Supplier<Generator<T>> factory) {
        Objects.requireNonNull(factory, "factory must not be null");
        Generator<T> gen = factory.get();
        return Arbitraries.randomValue(random -> gen.generate());
    }

    /**
     * Creates a jqwik {@link Arbitrary} that generates random instances of the
     * given class using krandom's {@link ObjectGenerator}.
     *
     * @param type the class to generate
     * @param <T>  the generated type
     * @return a jqwik Arbitrary backed by ObjectGenerator
     */
    public static <T> Arbitrary<T> forType(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        ObjectGenerator<T> gen = new ObjectGenerator<>(type);
        return Arbitraries.randomValue(random -> gen.generate());
    }

    /**
     * Creates a jqwik {@link Arbitrary} that generates random instances of the
     * given class using krandom's {@link ObjectGenerator} with custom configuration.
     *
     * @param type   the class to generate
     * @param config the generator configuration
     * @param <T>    the generated type
     * @return a jqwik Arbitrary backed by ObjectGenerator
     */
    public static <T> Arbitrary<T> forType(Class<T> type, GeneratorConfig config) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(config, "config must not be null");
        ObjectGenerator<T> gen = new ObjectGenerator<>(type, config);
        return Arbitraries.randomValue(random -> gen.generate());
    }
}
