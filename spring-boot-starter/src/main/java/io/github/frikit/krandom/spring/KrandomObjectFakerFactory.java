/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.object.ObjectGenerator;

import java.util.Objects;

/**
 * Factory for creating typed {@link ObjectFaker} and {@link ObjectGenerator} instances
 * backed by the auto-configured {@link GeneratorConfig}.
 *
 * <p>Inject this bean and call {@link #faker(Class)} or {@link #generator(Class)} to get a
 * ready-to-use instance:
 *
 * <pre>{@code
 *   @Autowired
 *   KrandomObjectFakerFactory factory;
 *
 *   User user = factory.faker(User.class)
 *                      .ruleFor("email", () -> "test@example.com")
 *                      .generate();
 *
 *   Order order = factory.generator(Order.class).generate();
 * }</pre>
 */
public class KrandomObjectFakerFactory {

    private final GeneratorConfig config;

    /**
     * Creates a factory backed by the supplied generator configuration.
     *
     * @param config generator configuration shared by created instances
     */
    public KrandomObjectFakerFactory(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Creates a new {@link ObjectFaker} for the given type, using the auto-configured
     * {@link GeneratorConfig}.
     *
     * @param type the class to generate
     * @param <T>  the type
     * @return a new {@code ObjectFaker} instance
     */
    public <T> ObjectFaker<T> faker(Class<T> type) {
        return new ObjectFaker<>(type, config);
    }

    /**
     * Creates a new {@link ObjectGenerator} for the given type, using the auto-configured
     * {@link GeneratorConfig}.
     *
     * @param type the class to generate
     * @param <T>  the type
     * @return a new {@code ObjectGenerator} instance
     */
    public <T> ObjectGenerator<T> generator(Class<T> type) {
        return new ObjectGenerator<>(type, config);
    }
}
