/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.identifier.*;

/**
 * Fluent namespace for identifier-related generators.
 *
 * <p>Usage: {@code Generators.identifier().uuid().generate()}
 */
public final class IdentifierGenerators {

    private final GeneratorConfig config;

    public IdentifierGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public IdentifierGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public UUIDGenerator uuid() { return new UUIDGenerator(); }

    public HashGenerator hash() { return new HashGenerator(); }

    public IsbnGenerator isbn() { return new IsbnGenerator(); }

    public IsbnGenerator isbn(IsbnGenerator.IsbnType type) { return new IsbnGenerator(type); }

    public EanGenerator ean() { return new EanGenerator(); }

    public UpcGenerator upc() { return new UpcGenerator(); }

    public IdentifierMaskGenerator mask() { return new IdentifierMaskGenerator(); }
}
