/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.core.model.CircularNode;
import io.github.frikit.krandom.generator.core.model.Person;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("object generator configuration mapping")
class ObjectGeneratorConfigurationMappingTest {

    @Test
    @DisplayName("seed(long) configures repeatable object generation")
    void seedConfiguresRepeatableObjectGeneration() {
        GeneratorConfig firstConfig = GeneratorConfig.builder().seed(42L).build();
        GeneratorConfig secondConfig = GeneratorConfig.builder().seed(42L).build();
        GeneratorConfig differentConfig = GeneratorConfig.builder().seed(43L).build();

        Person first = Generators.ofObject(Person.class, firstConfig).generate();
        Person second = Generators.ofObject(Person.class, secondConfig).generate();
        Person different = Generators.ofObject(Person.class, differentConfig).generate();

        assertEquals(personSnapshot(first), personSnapshot(second));
        assertNotEquals(personSnapshot(first), personSnapshot(different));
    }

    @Test
    @DisplayName("charset and string length constrain generated strings")
    void charsetAndStringLengthConstrainStringFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .charset(StandardCharsets.US_ASCII)
                                                .stringLength(8, 8)
                                                .seed(123L)
                                                .build();

        StringHolder holder = Generators.ofObject(StringHolder.class, config).generate();

        assertNotNull(holder.randomText);
        assertEquals(8, holder.randomText.length());
        assertFalse(holder.randomText.isBlank());
        holder.randomText.chars()
                         .forEach(ch -> assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode((char) ch)));
    }

    @Test
    @DisplayName("collection size controls arrays, collections, and maps")
    void collectionSizeControlsContainerFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .collectionSize(1, 1)
                                                .stringLength(4, 4)
                                                .seed(99L)
                                                .build();

        ContainerHolder holder = Generators.ofObject(ContainerHolder.class, config).generate();

        assertNotNull(holder.tags);
        assertEquals(1, holder.tags.length);
        assertNotNull(holder.tags[0]);
        assertNotNull(holder.values);
        assertEquals(1, holder.values.size());
        assertNotNull(holder.values.getFirst());
        assertNotNull(holder.attributes);
        assertEquals(1, holder.attributes.size());
    }

    @Test
    @DisplayName("object pool and max depth control recursive object graphs")
    void objectPoolAndDepthControlObjectGraphs() {
        GeneratorConfig poolConfig = GeneratorConfig.builder()
                                                    .objectPoolSize(0)
                                                    .build();
        CircularNode node = assertDoesNotThrow(() -> Generators.ofObject(CircularNode.class, poolConfig).generate());
        assertNotNull(node);

        GeneratorConfig depthConfig = GeneratorConfig.builder()
                                                     .objectMaxDepth(1)
                                                     .build();
        DepthRoot root = Generators.ofObject(DepthRoot.class, depthConfig).generate();
        assertNotNull(root.middle);
        assertNull(root.middle.leaf);
    }

    @Test
    @DisplayName("object date range constrains generated date fields")
    void objectDateRangeConstrainsDateFields() {
        LocalDate exactDate = LocalDate.of(2024, 5, 10);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectDateRange(exactDate, exactDate)
                                                .seed(7L)
                                                .build();

        DateHolder holder = Generators.ofObject(DateHolder.class, config).generate();

        assertEquals(exactDate, holder.calendarDate);
    }

    @Test
    @DisplayName("LocalTime fields can use explicit overrides")
    void localTimeFieldUsesExplicitOverride() {
        LocalTime exactTime = LocalTime.of(9, 30);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(LocalTime.class, () -> exactTime)
                                                .build();

        TimeHolder holder = Generators.ofObject(TimeHolder.class, config).generate();

        assertEquals(exactTime, holder.wakeUpTime);
    }

    @Test
    @DisplayName("ignore errors flag suppresses generation failures")
    void ignoreErrorsFlagSuppressesGenerationFailures() {
        GeneratorConfig ignoreConfig = GeneratorConfig.builder()
                                                      .objectIgnoreErrors(true)
                                                      .stringLength(5, 5)
                                                      .build();

        ErrorHolder holder = Generators.ofObject(ErrorHolder.class, ignoreConfig).generate();
        assertNull(holder.nested);
        assertNotNull(holder.randomText);

        GeneratorConfig strictConfig = GeneratorConfig.builder()
                                                      .objectIgnoreErrors(false)
                                                      .build();
        assertThrows(ObjectGenerationException.class,
                     () -> Generators.ofObject(ErrorHolder.class, strictConfig).generate());
    }

    @Test
    @DisplayName("default initialization setting controls existing field values")
    void defaultInitializationSettingControlsExistingFieldValues() {
        GeneratorConfig preserveConfig = GeneratorConfig.builder()
                                                        .stringLength(4, 4)
                                                        .objectOverrideDefaultInitialization(false)
                                                        .build();
        InitializedHolder preserved = Generators.ofObject(InitializedHolder.class, preserveConfig).generate();
        assertEquals("preset-value", preserved.randomText);
        assertNotNull(preserved.blankText);

        GeneratorConfig overwriteConfig = GeneratorConfig.builder()
                                                         .stringLength(4, 4)
                                                         .objectOverrideDefaultInitialization(true)
                                                         .build();
        InitializedHolder overwritten = Generators.ofObject(InitializedHolder.class, overwriteConfig).generate();
        assertNotEquals("preset-value", overwritten.randomText);
        assertEquals(4, overwritten.randomText.length());
    }

    @Test
    @DisplayName("direct-field object population bypasses setters")
    void directFieldObjectPopulationBypassesSetters() {
        SetterTrap.setterCalled = false;
        GeneratorConfig config = GeneratorConfig.builder()
                                                .stringLength(3, 3)
                                                .build();

        SetterTrap trap = Generators.ofObject(SetterTrap.class, config).generate();

        assertFalse(SetterTrap.setterCalled);
        assertNotNull(trap.value);
        assertEquals(3, trap.value.length());
    }

    @Test
    @DisplayName("abstract fields use explicit type overrides")
    void abstractFieldsUseExplicitTypeOverride() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(PaymentMethod.class, CardPayment::new)
                                                .build();

        AbstractTypeHolder holder = Generators.ofObject(AbstractTypeHolder.class, config).generate();

        assertInstanceOf(CardPayment.class, holder.paymentMethod);
    }

    private List<Object> personSnapshot(Person person) {
        return List.of(
            person.getFirstName(),
            person.getLastName(),
            person.getAge(),
            person.getSalary(),
            person.isActive(),
            person.getStatus(),
            person.getAddress().getStreet(),
            person.getAddress().getHouseNumber());
    }

    static class StringHolder {

        String randomText;
    }

    static class ContainerHolder {

        String[]             tags;
        List<String>         values;
        Map<String, Integer> attributes;
    }

    static class DepthRoot {

        DepthMiddle middle;
    }

    static class DepthMiddle {

        DepthLeaf leaf;
    }

    static class DepthLeaf {

        String randomText;
    }

    static class DateHolder {

        LocalDate calendarDate;
    }

    static class TimeHolder {

        LocalTime wakeUpTime;
    }

    static class ErrorHolder {

        ExplodingNested nested;
        String          randomText;
    }

    static class ExplodingNested {

        ExplodingNested() {
            throw new IllegalStateException("boom");
        }
    }

    static class InitializedHolder {

        String randomText = "preset-value";
        String blankText;
    }

    static class SetterTrap {

        static boolean setterCalled;
        String         value;

        public void setValue(String value) {
            setterCalled = true;
            this.value = "setter-" + value;
        }
    }

    interface PaymentMethod {
    }

    static class CardPayment implements PaymentMethod {
    }

    static class AbstractTypeHolder {

        PaymentMethod paymentMethod;
    }
}
