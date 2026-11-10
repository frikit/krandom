/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import io.github.frikit.krandom.generator.schema.Schema;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Jackson 3 ({@code tools.jackson}) module for krandom types: it serializes a {@link Schema} as
 * the JSON Schema document of {@link Schema#toJsonSchema()}.
 *
 * <p>Jackson 3 mappers are immutable, so the module is registered while a mapper is built, for
 * example with {@code JsonMapper.builder().addModule(new KrandomJacksonModule()).build()} or
 * {@link KrandomJackson#configure}. Spring Boot 4 registers every {@code JacksonModule} bean with
 * the {@code JsonMapper} it auto-configures, so a {@code KrandomJacksonModule} bean is enough there.
 * It is not a Jackson 2 ({@code com.fasterxml.jackson.databind}) module and cannot be registered
 * with a Jackson 2 {@code ObjectMapper}.
 */
public final class KrandomJacksonModule extends SimpleModule {

    /** Creates a module with serializers for krandom public types. */
    public KrandomJacksonModule() {
        super("krandom-jackson");
        addSerializer(Schema.class, new SchemaSerializer());
    }

    private static final class SchemaSerializer extends StdSerializer<Schema> {

        private SchemaSerializer() {
            super(Schema.class);
        }

        @Override
        public void serialize(Schema value, JsonGenerator gen, SerializationContext ctxt) {
            ctxt.writeValue(gen, value.toJsonSchema());
        }
    }
}
