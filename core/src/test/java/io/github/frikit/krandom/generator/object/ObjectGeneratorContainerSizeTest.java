/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator — constrained array and collection sizes")
class ObjectGeneratorContainerSizeTest {

    private static final int SAMPLES = 50;

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    record Leaf(String value) {}

    static final class ConstrainedArrays {

        @NotEmpty Leaf[] leaves;

        @Size(min = 1, max = 2) Leaf[] sizedLeaves;

        @Size(min = 1, max = 4) byte[] bytes;

        @NotEmpty int[] numbers;

        @NotNull Leaf[] requiredLeaves;
    }

    record ArrayOwner(ConstrainedArrays arrays) {}

    static final class SizedStrings {

        @Size(min = 6, max = 6) String[] codes;

        List<@Size(min = 4, max = 4) String>[] groups;
    }

    static final class ConstrainedCollections {

        @NotEmpty List<String> notEmpty;

        @Size(min = 5) List<String> atLeastFive;

        @Size(max = 2) Set<Integer> atMostTwo;

        @Size(min = 2, max = 8) List<String> wide;

        @NotEmpty Map<String, Integer> notEmptyMap;

        @Size(min = 2, max = 3) String[] sizedArray;
    }

    static final class LargeCollection {

        @NotEmpty List<Integer> values;
    }

    private static GeneratorConfig sized(int min, int max) {
        return GeneratorConfig.builder().seed(4402L).collectionSize(min, max).build();
    }

    @Test
    @DisplayName("@NotEmpty and @Size on an array of objects or primitives constrain the array, not its elements")
    void arrayConstraintsApplyToTheArray() {
        for (int i = 0; i < SAMPLES; i++) {
            ConstrainedArrays arrays = new ObjectGenerator<>(ConstrainedArrays.class, sized(1, 10)).generate();

            assertTrue(arrays.leaves.length >= 1 && arrays.leaves.length <= 10);
            assertTrue(arrays.sizedLeaves.length >= 1 && arrays.sizedLeaves.length <= 2);
            assertTrue(arrays.bytes.length >= 1 && arrays.bytes.length <= 4);
            assertTrue(arrays.numbers.length >= 1);
            assertTrue(arrays.requiredLeaves.length >= 1);
            assertValid(arrays);
        }
    }

    @Test
    @DisplayName("a constrained array at objectMaxDepth holds null elements, as a list does")
    void constrainedArrayAtTheDepthLimitHoldsNullElements() {
        GeneratorConfig config = sized(2, 2).toBuilder().objectMaxDepth(1).build();

        ConstrainedArrays arrays =
                new ObjectGenerator<>(ArrayOwner.class, config).generate().arrays();

        assertEquals(2, arrays.leaves.length);
        assertNull(arrays.leaves[0]);
        assertEquals(2, arrays.sizedLeaves.length);
        assertNull(arrays.requiredLeaves[1]);
        assertValid(arrays);
    }

    @Test
    @DisplayName("@Size before a String[] sizes the array, and a type argument inside it still constrains")
    void sizeBeforeAStringArraySizesTheArray() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(4402L)
                .collectionSize(2, 2)
                .stringLength(2, 2)
                .build();

        SizedStrings strings = new ObjectGenerator<>(SizedStrings.class, config).generate();

        assertEquals(6, strings.codes.length);
        for (String code : strings.codes) {
            assertEquals(2, code.length(), code);
        }
        assertEquals(2, strings.groups.length);
        for (List<String> group : strings.groups) {
            group.forEach(label -> assertEquals(4, label.length(), label));
        }
    }

    @Test
    @DisplayName("a constrained size takes collectionSize within the declared bounds")
    void constrainedSizeTakesCollectionSizeWithinTheBounds() {
        Set<Integer> notEmptySizes = new HashSet<>();
        for (int i = 0; i < SAMPLES; i++) {
            ConstrainedCollections values = new ObjectGenerator<>(
                            ConstrainedCollections.class,
                            sized(3, 4).toBuilder().seed(i).build())
                    .generate();

            notEmptySizes.add(values.notEmpty.size());
            assertEquals(5, values.atLeastFive.size());
            assertEquals(2, values.atMostTwo.size());
            assertTrue(values.wide.size() >= 3 && values.wide.size() <= 4, () -> "wide " + values.wide.size());
            assertTrue(values.notEmptyMap.size() >= 3 && values.notEmptyMap.size() <= 4);
            assertEquals(3, values.sizedArray.length);
            assertValid(values);
        }
        assertEquals(Set.of(3, 4), notEmptySizes);
    }

    @Test
    @DisplayName("an empty collectionSize gives the smallest size a constraint allows")
    void emptyCollectionSizeGivesTheSmallestAllowedSize() {
        ConstrainedCollections values = new ObjectGenerator<>(ConstrainedCollections.class, sized(0, 0)).generate();

        assertEquals(1, values.notEmpty.size());
        assertEquals(5, values.atLeastFive.size());
        assertEquals(0, values.atMostTwo.size());
        assertEquals(2, values.wide.size());
        assertEquals(1, values.notEmptyMap.size());
        assertEquals(2, values.sizedArray.length);
    }

    @Test
    @DisplayName("an unbounded size follows a collectionSize above 255")
    void unboundedSizeFollowsALargeCollectionSize() {
        LargeCollection large = new ObjectGenerator<>(LargeCollection.class, sized(300, 300)).generate();

        assertEquals(300, large.values.size());
    }

    private static void assertValid(Object value) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(value);
        assertTrue(
                violations.isEmpty(),
                () -> violations.stream()
                        .map(violation -> violation.getPropertyPath() + "=" + violation.getInvalidValue())
                        .collect(Collectors.joining(", ")));
    }
}
