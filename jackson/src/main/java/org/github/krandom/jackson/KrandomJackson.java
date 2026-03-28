/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * Jackson utility for krandom integrations.
 */
public final class KrandomJackson {

    private KrandomJackson() {
    }

    public static ObjectMapper configure(ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        mapper.registerModule(new KrandomJacksonModule());
        return mapper;
    }

    public static ObjectMapper newObjectMapper() {
        return configure(new ObjectMapper());
    }
}
