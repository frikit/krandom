/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.frikit.krandom.generator.schema.Schema;

import java.io.IOException;

/**
 * Jackson module for krandom integration.
 */
public final class KrandomJacksonModule extends SimpleModule {

    public KrandomJacksonModule() {
        super("krandom-jackson");
        addSerializer(Schema.class, new SchemaSerializer());
    }

    private static final class SchemaSerializer extends StdSerializer<Schema> {

        private SchemaSerializer() {
            super(Schema.class);
        }

        @Override
        public void serialize(Schema value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(value.toJsonSchema());
        }
    }
}
