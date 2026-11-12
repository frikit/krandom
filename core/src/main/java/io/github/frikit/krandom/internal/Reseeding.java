/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * The generators whose reseed leaves them as a new one built from their configuration with that seed would be,
 * so that a Schema can build one for each column and reseed it for each value rather than build one for each
 * value.
 *
 * <p>Each such class registers its reseed, a method of its own package, from its static initializer, so the
 * reseed adds no public member to the class and does not make it {@code Seedable}: object generation reseeds a
 * {@code Seedable} {@code @Randomizer}, which these classes must not become. This package is not exported, so
 * this is not public API.
 */
public final class Reseeding {

    /**
     * Resets a generator to what a new build from its configuration with a seed would be.
     *
     * @param <G> generator type
     */
    @FunctionalInterface
    public interface Reseeder<G> {

        /**
         * Reseeds a generator.
         *
         * @param generator generator to reset
         * @param seed      seed of the new build it then equals
         */
        void reseed(G generator, long seed);
    }

    private static final Map<Class<?>, Reseeder<?>> RESEEDERS = new ConcurrentHashMap<>();

    private Reseeding() {}

    /**
     * Registers the reseed of a class; each class does so once, when it is initialized.
     *
     * @param type     the generator class, which must be final
     * @param reseeder its reseed
     * @param <G>      generator type
     * @throws IllegalStateException if the class has a reseed already
     */
    public static <G> void register(Class<G> type, Reseeder<G> reseeder) {
        Objects.requireNonNull(reseeder, "reseeder must not be null");
        if (RESEEDERS.putIfAbsent(Objects.requireNonNull(type, "type must not be null"), reseeder) != null) {
            throw new IllegalStateException("A reseed is registered for " + type.getName() + " already");
        }
    }

    /**
     * The reseed of a generator's class, which is initialized, as the generator exists.
     *
     * @param generator generator to look up
     * @param <G>       generator type
     * @return its reseed, or null when its class has none
     */
    @SuppressWarnings("unchecked")
    public static <G> @Nullable Reseeder<G> reseederOf(G generator) {
        return (Reseeder<G>) RESEEDERS.get(generator.getClass());
    }

    /** The classes registered so far, for the test that checks each of them. */
    static java.util.Set<Class<?>> registered() {
        return java.util.Set.copyOf(RESEEDERS.keySet());
    }

    /**
     * The seed of the child stream {@code streamName} of a configuration like {@code config} seeded {@code seed}:
     * what {@code forChildStream} gives the child, its parent's seed under {@link ChildStreamPolicy#LEGACY}.
     *
     * @param config     configuration whose child-stream policy applies
     * @param seed       seed of the parent
     * @param streamName name of the child stream
     * @return the child's seed
     */
    public static long childSeed(GeneratorConfig config, long seed, String streamName) {
        return config.getChildStreamPolicy() == ChildStreamPolicy.LEGACY
                ? seed
                : GenerationRecipe.deriveChildSeed(seed, streamName);
    }
}
