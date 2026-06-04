/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.system;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates lightweight exception payloads useful for logs/fixtures.
 */
public final class ExceptionPayloadGenerator implements Generator<Map<String, String>> {

    private static final String[] TYPES    = {
        "IllegalStateException", "IllegalArgumentException", "RuntimeException",
        "NullPointerException", "UnsupportedOperationException"
    };
    private static final String[] MESSAGES = {
        "Invalid state transition",
        "Unexpected null value",
        "Configuration value out of range",
        "Operation is not supported",
        "Input validation failed"
    };

    private final Random random;
    private final Clock  clock;

    public ExceptionPayloadGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ExceptionPayloadGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.clock = config.getClock();
    }

    @Override
    public Map<String, String> generate() {
        String type = TYPES[random.nextInt(TYPES.length)];
        String message = MESSAGES[random.nextInt(MESSAGES.length)];
        int code = 1000 + random.nextInt(9000);
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("message", message);
        payload.put("code", "ERR-" + code);
        payload.put("timestamp", clock.instant().toString());
        return payload;
    }
}
