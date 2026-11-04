/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import java.util.Objects;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 ({@code tools.jackson}) utility for krandom integrations.
 *
 * <p>Jackson 3 mappers are immutable, so krandom's {@link KrandomJacksonModule} is added to a
 * mapper builder before the mapper is built. This module does not support Jackson 2
 * ({@code com.fasterxml.jackson.databind}).
 */
public final class KrandomJackson {

    private KrandomJackson() {}

    /**
     * Adds krandom's Jackson module to a mapper builder, such as {@code JsonMapper.builder()}.
     *
     * @param builder builder to configure
     * @param <B> the builder type
     * @return the same builder instance
     */
    public static <B extends MapperBuilder<?, B>> B configure(B builder) {
        Objects.requireNonNull(builder, "builder must not be null");
        return builder.addModule(new KrandomJacksonModule());
    }

    /**
     * Creates a new JSON mapper with krandom's Jackson module and Jackson 3's defaults otherwise.
     *
     * @return the configured mapper
     */
    public static JsonMapper newJsonMapper() {
        return configure(JsonMapper.builder()).build();
    }
}
