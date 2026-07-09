/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.core.model.Address;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — Optional support")
class ObjectGeneratorOptionalTest {

    @Test
    @DisplayName("typed Optional fields are populated as Optional values")
    void typedOptionalsAreGenerated() {
        OptionalHolder value = new ObjectGenerator<>(OptionalHolder.class).generate();
        assertNotNull(value.getName());
        assertNotNull(value.getAge());
        assertNotNull(value.getAddress());
        assertTrue(value.getName().isPresent(), "Optional<String> should be present for built-in String generation");
        assertTrue(value.getAge().isPresent(), "Optional<Integer> should be present for built-in Integer generation");
        assertTrue(value.getAddress().isPresent(), "Optional<Address> should be present for nested object generation");
        assertNotNull(value.getAddress().orElseThrow().getStreet());
    }

    @Test
    @DisplayName("Optional becomes empty when nested generated value is null")
    void optionalEmptyWhenNestedValueNull() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(String.class, () -> null)
                                                            .build();
        OptionalHolder value = new ObjectGenerator<>(OptionalHolder.class, config).generate();
        assertNotNull(value.getName());
        assertTrue(value.getName().isEmpty());
    }

    @Test
    @DisplayName("raw Optional type fails with unsupported-type context")
    void rawOptionalFailsWithContext() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(RawOptionalHolder.class).generate());
        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("RawOptionalHolder.raw", context.path());
        assertEquals(Optional.class.getTypeName(), context.declaredType());
    }

    @Test
    @DisplayName("optionalEmptyProbability(1.0) forces Optional.empty for typed fields")
    void optionalEmptyProbabilityForcesEmpty() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .optionalEmptyProbability(1.0)
                                                            .build();
        OptionalHolder value = new ObjectGenerator<>(OptionalHolder.class, config).generate();
        assertTrue(value.getName().isEmpty());
        assertTrue(value.getAge().isEmpty());
        assertTrue(value.getAddress().isEmpty());
    }


    static class OptionalHolder {

        private Optional<String>  name;
        private Optional<Integer> age;
        private Optional<Address> address;

        Optional<String> getName() {
            return name;
        }

        Optional<Integer> getAge() {
            return age;
        }

        Optional<Address> getAddress() {
            return address;
        }

    }

    static class RawOptionalHolder {

        @SuppressWarnings("rawtypes")
        private Optional raw;
    }
}
