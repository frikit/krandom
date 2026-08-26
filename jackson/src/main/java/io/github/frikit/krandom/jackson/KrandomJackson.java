/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Jackson utility for krandom integrations.
 */
public final class KrandomJackson {

    private KrandomJackson() {
    }

    /**
     * Registers krandom's Jackson module with an existing mapper.
     *
     * @param mapper mapper to configure
     * @return the same mapper instance
     */
    public static ObjectMapper configure(ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        mapper.registerModule(new KrandomJacksonModule());
        return mapper;
    }

    /**
     * Creates a new mapper configured with krandom's Jackson module.
     *
     * @return the configured mapper
     */
    public static ObjectMapper newObjectMapper() {
        return configure(new ObjectMapper());
    }
}
