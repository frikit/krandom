/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * Central reflective boundary for schema record conversion.
 */
final class SchemaRecordAccess {

    private SchemaRecordAccess() {
    }

    static Object read(Object record, RecordComponent component, String path) {
        Method accessor = component.getAccessor();
        try {
            accessor.trySetAccessible();
            return accessor.invoke(record);
        } catch (InvocationTargetException ex) {
            throw failure(record, component, path, ex.getTargetException());
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
            throw failure(record, component, path, ex);
        }
    }

    private static SchemaGenerationException failure(Object record,
                                                     RecordComponent component,
                                                     String path,
                                                     Throwable cause) {
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.REFLECTION,
            GenerationOperation.READ,
            path,
            record.getClass(),
            component.getGenericType().getTypeName(),
            -1,
            -1);
        return new SchemaGenerationException(context, cause);
    }
}
