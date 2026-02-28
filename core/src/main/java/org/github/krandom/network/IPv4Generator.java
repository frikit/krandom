/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

/**
 * Backward-compatible alias for {@link org.github.krandom.generator.network.IPv4Generator}.
 *
 * @deprecated Use {@link org.github.krandom.generator.network.IPv4Generator} instead.
 */
@Deprecated
public final class IPv4Generator implements Generator<String> {

    private final org.github.krandom.generator.network.IPv4Generator delegate;

    public IPv4Generator() {
        this.delegate = new org.github.krandom.generator.network.IPv4Generator();
    }

    public IPv4Generator(GeneratorConfig config) {
        this.delegate = new org.github.krandom.generator.network.IPv4Generator(config);
    }

    @Override
    public String generate() {
        return delegate.generate();
    }
}
