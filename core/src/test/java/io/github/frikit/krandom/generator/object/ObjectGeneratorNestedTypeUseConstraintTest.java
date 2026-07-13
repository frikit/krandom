/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — nested type-use constraints")
class ObjectGeneratorNestedTypeUseConstraintTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                                  .seed(3084L)
                                                                  .collectionSize(2, 2)
                                                                  .build();

    @Test
    @DisplayName("type-use constraints apply to optional, collection, and map children")
    void typeUseConstraintsApplyToContainerChildren() {
        NestedTypeUseFixture value = new ObjectGenerator<>(NestedTypeUseFixture.class, CONFIG).generate();

        assertTrue(value.optionalScore.orElseThrow() >= 100);
        for (String code : value.codes) {
            assertEquals(6, code.length());
        }
        value.labels.forEach(label -> assertEquals(4, label.length()));
        value.nestedLabels.forEach(labels -> labels.forEach(label -> assertEquals(7, label.length())));
        value.scores.forEach((label, score) -> {
            assertEquals(3, label.length());
            assertTrue(score >= 100);
        });
    }

    @Test
    @DisplayName("record component type-use constraints follow the same container path")
    void recordComponentTypeUseConstraintsApplyToContainerChildren() {
        NestedTypeUseRecord value = new ObjectGenerator<>(NestedTypeUseRecord.class, CONFIG).generate();

        value.labels().forEach(label -> assertEquals(5, label.length()));
    }

    @Test
    @DisplayName("an incompatible type-use constraint reports the nested child path")
    void incompatibleTypeUseConstraintReportsNestedChildPath() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(IncompatibleTypeUseFixture.class, CONFIG).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("IncompatibleTypeUseFixture.values[]", context.path());
        assertEquals(Integer.class.getTypeName(), context.declaredType());
    }

    static final class NestedTypeUseFixture {

        Optional<@Min(100) Integer> optionalScore;
        @Size(min = 6, max = 6) String[] codes;
        List<@Size(min = 4, max = 4) String> labels;
        List<List<@Size(min = 7, max = 7) String>> nestedLabels;
        Map<@Size(min = 3, max = 3) String, @Min(100) Integer> scores;
    }

    record NestedTypeUseRecord(List<@Size(min = 5, max = 5) String> labels) {
    }

    static final class IncompatibleTypeUseFixture {

        List<@Size(min = 1) Integer> values;
    }
}
