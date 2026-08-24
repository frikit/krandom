/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Immutable, composable configuration model for creating fresh {@link ObjectFaker} instances.
 *
 * <p>A model stores configuration steps, not generated objects or mutable generator state. Each
 * call to {@link #faker()} or {@link #faker(GeneratorConfig)} creates and configures a new faker.
 * Configuration callbacks should therefore create any stateful field generators inside the
 * callback rather than capture and share them.
 *
 * @param <T> modeled object type
 */
public final class ObjectModel<T> {

    private final Class<T> type;
    private final List<Consumer<ObjectFaker<T>>> configurations;

    private ObjectModel(Class<T> type, List<Consumer<ObjectFaker<T>>> configurations) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.configurations = List.copyOf(configurations);
    }

    /**
     * Creates an empty model for a type.
     *
     * @param type modeled object type
     * @param <T> modeled type
     * @return empty immutable model
     */
    public static <T> ObjectModel<T> of(Class<T> type) {
        return new ObjectModel<>(type, List.of());
    }

    /**
     * Returns a new model with one configuration step appended.
     *
     * @param configuration configuration applied to each fresh faker
     * @return new immutable model
     */
    public ObjectModel<T> configure(Consumer<ObjectFaker<T>> configuration) {
        Objects.requireNonNull(configuration, "configuration must not be null");
        List<Consumer<ObjectFaker<T>>> updated = new ArrayList<>(configurations.size() + 1);
        updated.addAll(configurations);
        updated.add(configuration);
        return new ObjectModel<>(type, updated);
    }

    /**
     * Returns a new model applying this model followed by another model of the same type.
     *
     * @param other model appended after this model
     * @return composed immutable model
     */
    public ObjectModel<T> and(ObjectModel<T> other) {
        Objects.requireNonNull(other, "other must not be null");
        if (type != other.type) {
            throw new IllegalArgumentException(
                "Cannot compose model for " + type.getName() + " with model for " + other.type.getName());
        }
        List<Consumer<ObjectFaker<T>>> combined =
            new ArrayList<>(configurations.size() + other.configurations.size());
        combined.addAll(configurations);
        combined.addAll(other.configurations);
        return new ObjectModel<>(type, combined);
    }

    /**
     * Creates a fresh faker using default configuration.
     *
     * @return configured faker
     */
    public ObjectFaker<T> faker() {
        return configure(new ObjectFaker<>(type));
    }

    /**
     * Creates a fresh faker using explicit root configuration.
     *
     * @param config root generator configuration
     * @return configured faker
     */
    public ObjectFaker<T> faker(GeneratorConfig config) {
        return configure(new ObjectFaker<>(type, config));
    }

    /**
     * Generates one object using a fresh default-configured faker.
     *
     * @return generated object
     */
    public T generate() {
        return faker().generate();
    }

    /**
     * Generates one object using a fresh explicitly configured faker.
     *
     * @param config root generator configuration
     * @return generated object
     */
    public T generate(GeneratorConfig config) {
        return faker(config).generate();
    }

    private ObjectFaker<T> configure(ObjectFaker<T> faker) {
        for (Consumer<ObjectFaker<T>> configuration : configurations) {
            configuration.accept(faker);
        }
        return faker;
    }
}
