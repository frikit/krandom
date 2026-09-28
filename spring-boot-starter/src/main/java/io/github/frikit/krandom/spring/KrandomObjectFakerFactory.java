/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GenerationRecipe;
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
 *
 * <p><b>Seeded configurations.</b> Every {@link #faker(Class)} and {@link #generator(Class)} call
 * starts a new instance on the configuration's root seed, so with {@code krandom.seed} set each
 * call restarts the same sequence and the first generated object is identical across calls. Reuse
 * one instance for a sequence of distinct objects, or pass a stream name to
 * {@link #generator(Class, String)} or {@link #faker(Class, String)} to derive an independent,
 * reproducible child stream per request or entity.
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

    /**
     * Creates a new {@link ObjectGenerator} for the given type on a named child stream of the
     * auto-configured {@link GeneratorConfig}.
     *
     * <p>With a seeded configuration the generator uses the child seed
     * {@code GenerationRecipe.deriveChildSeed(seed, streamName)} and keeps every other setting: each
     * stream name yields its own reproducible sequence, so per-request or per-entity names avoid the
     * identical objects that {@link #generator(Class)} returns when it restarts the root sequence on
     * every call. An unseeded configuration stays unseeded and ignores the name.
     *
     * @param type       the class to generate
     * @param streamName non-blank, single-line name of the child stream, e.g. {@code "order-42"}
     * @param <T>        the type
     * @return a new {@code ObjectGenerator} instance
     * @throws IllegalArgumentException if {@code streamName} is blank or spans several lines
     */
    public <T> ObjectGenerator<T> generator(Class<T> type, String streamName) {
        return new ObjectGenerator<>(type, childConfig(streamName));
    }

    /**
     * Creates a new {@link ObjectFaker} for the given type on a named child stream of the
     * auto-configured {@link GeneratorConfig}, with the same child-seed derivation as
     * {@link #generator(Class, String)}.
     *
     * @param type       the class to generate
     * @param streamName non-blank, single-line name of the child stream, e.g. {@code "order-42"}
     * @param <T>        the type
     * @return a new {@code ObjectFaker} instance
     * @throws IllegalArgumentException if {@code streamName} is blank or spans several lines
     */
    public <T> ObjectFaker<T> faker(Class<T> type, String streamName) {
        return new ObjectFaker<>(type, childConfig(streamName));
    }

    private GeneratorConfig childConfig(String streamName) {
        Objects.requireNonNull(streamName, "streamName must not be null");
        if (streamName.isBlank() || streamName.indexOf('\n') >= 0 || streamName.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("streamName must be a non-blank single-line value");
        }
        if (config.getSeed().isEmpty()) {
            return config;
        }
        return config.toBuilder()
                     .seed(GenerationRecipe.deriveChildSeed(config.getSeed().getAsLong(), streamName))
                     .build();
    }
}
