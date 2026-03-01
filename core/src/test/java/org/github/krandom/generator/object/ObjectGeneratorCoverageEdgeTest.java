/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator edge coverage")
class ObjectGeneratorCoverageEdgeTest {

    @Test
    @DisplayName("hasNonDefaultValue wraps IllegalAccessException")
    void hasNonDefaultValueIllegalAccessBranch() throws Exception {
        ObjectGenerator<PrivateFieldHolder> generator = new ObjectGenerator<>(PrivateFieldHolder.class);
        Method method = ObjectGenerator.class.getDeclaredMethod("hasNonDefaultValue", Object.class, Field.class);
        method.setAccessible(true);

        Field field = PrivateFieldHolder.class.getDeclaredField("value");
        // Intentionally do not set accessible(true) to trigger IllegalAccessException in field.get(instance).
        InvocationTargetException ex = assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(generator, new PrivateFieldHolder(), field)
        );
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    private static final class PrivateFieldHolder {
        @SuppressWarnings("unused")
        private String value = "x";
    }
}
