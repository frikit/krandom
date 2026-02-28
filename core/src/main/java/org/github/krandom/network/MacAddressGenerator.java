/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

/**
 * Backward-compatible alias for {@link org.github.krandom.generator.network.MacAddressGenerator}.
 *
 * @deprecated Use {@link org.github.krandom.generator.network.MacAddressGenerator} instead.
 */
@Deprecated
public final class MacAddressGenerator implements Generator<String> {

    private final org.github.krandom.generator.network.MacAddressGenerator delegate;

    public MacAddressGenerator() {
        this.delegate = new org.github.krandom.generator.network.MacAddressGenerator();
    }

    public MacAddressGenerator(GeneratorConfig config) {
        this.delegate = new org.github.krandom.generator.network.MacAddressGenerator(config);
    }

    @Override
    public String generate() {
        return delegate.generate();
    }

    public String generate(char separator) {
        return delegate.generate(separator);
    }

    public String generateLowercase() {
        return delegate.generateLowercase();
    }

    public String generateLowercase(char separator) {
        return delegate.generateLowercase(separator);
    }
}
