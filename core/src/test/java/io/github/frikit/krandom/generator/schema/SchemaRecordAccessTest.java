/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Schema record access")
class SchemaRecordAccessTest {

    @Test
    @DisplayName("receiver mismatches retain structured reflection context")
    void receiverMismatchRetainsStructuredContext() {
        RecordComponent component = ExpectedRecord.class.getRecordComponents()[0];

        SchemaGenerationException ex = assertThrows(
            SchemaGenerationException.class,
            () -> SchemaRecordAccess.read(new DifferentRecord("value"), component, "nested.value"));

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, context.category());
        assertEquals(GenerationOperation.READ, context.operation());
        assertEquals("nested.value", context.path());
        assertEquals(DifferentRecord.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    private record ExpectedRecord(String value) {
    }

    private record DifferentRecord(String value) {
    }
}
