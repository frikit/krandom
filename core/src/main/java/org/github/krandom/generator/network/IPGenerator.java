/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates IP addresses by randomly choosing IPv4 or IPv6 per call.
 */
public final class IPGenerator implements Generator<String> {

    private final Random        random;
    private final IPv4Generator ipv4Generator;
    private final IPv6Generator ipv6Generator;

    public IPGenerator() {
        this(GeneratorConfig.defaults());
    }

    public IPGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.ipv4Generator = new IPv4Generator(config);
        this.ipv6Generator = new IPv6Generator(config);
    }

    @Override
    public String generate() {
        return random.nextBoolean() ? ipv4Generator.generate() : ipv6Generator.generate();
    }

    public String generateIPv4() {
        return ipv4Generator.generate();
    }

    public String generateIPv6() {
        return ipv6Generator.generate();
    }
}
