/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GenerationFailureContext")
class GenerationFailureContextTest {

    @Test
    @DisplayName("stores sanitized machine-readable context")
    void storesContext() {
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.ASSIGNMENT,
            GenerationOperation.ASSIGN,
            "Order.customer",
            String.class,
            "java.lang.String",
            2,
            -1);

        assertEquals("Order.customer", context.path());
        assertEquals(String.class, context.ownerType());
        assertEquals("java.lang.String", context.declaredType());
        assertEquals(2, context.depth());
        assertEquals(-1, context.recordIndex());
    }

    @Test
    @DisplayName("is serializable with its owning exception")
    void serializable() throws Exception {
        GenerationFailureContext context = context(
            GenerationFailureCategory.SCHEMA_VALUE,
            GenerationOperation.GENERATE,
            "email",
            "java.lang.String",
            -1,
            3);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(context);
        }

        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertEquals(context, input.readObject());
        }
    }

    @Test
    @DisplayName("rejects missing and invalid context dimensions")
    void validatesContext() {
        assertThrows(NullPointerException.class, () -> context(null, GenerationOperation.GENERATE, "field", null, 0, -1));
        assertThrows(NullPointerException.class, () -> context(GenerationFailureCategory.PROVIDER, null, "field", null, 0, -1));
        assertThrows(NullPointerException.class, () -> context(GenerationFailureCategory.PROVIDER,
                                                               GenerationOperation.GENERATE,
                                                               null,
                                                               null,
                                                               0,
                                                               -1));
        assertThrows(IllegalArgumentException.class, () -> context(GenerationFailureCategory.PROVIDER,
                                                                  GenerationOperation.GENERATE,
                                                                  " ",
                                                                  null,
                                                                  0,
                                                                  -1));
        assertThrows(IllegalArgumentException.class, () -> context(GenerationFailureCategory.PROVIDER,
                                                                  GenerationOperation.GENERATE,
                                                                  "field",
                                                                  " ",
                                                                  0,
                                                                  -1));
        assertThrows(IllegalArgumentException.class, () -> context(GenerationFailureCategory.PROVIDER,
                                                                  GenerationOperation.GENERATE,
                                                                  "field",
                                                                  null,
                                                                  -2,
                                                                  -1));
        assertThrows(IllegalArgumentException.class, () -> context(GenerationFailureCategory.PROVIDER,
                                                                  GenerationOperation.GENERATE,
                                                                  "field",
                                                                  null,
                                                                  -1,
                                                                  -2));
    }

    private static GenerationFailureContext context(GenerationFailureCategory category,
                                                    GenerationOperation operation,
                                                    String path,
                                                    String declaredType,
                                                    int depth,
                                                    int recordIndex) {
        return new GenerationFailureContext(category, operation, path, null, declaredType, depth, recordIndex);
    }
}
