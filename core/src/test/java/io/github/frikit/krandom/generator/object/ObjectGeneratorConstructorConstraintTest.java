/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator — constraints on the field a constructor argument initializes")
class ObjectGeneratorConstructorConstraintTest {

    private static final int SAMPLES = 50;

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    private static final GeneratorConfig CONFIG =
            GeneratorConfig.builder().seed(9013L).collectionSize(2, 3).build();

    static final class Account {

        @Size(min = 3, max = 3) private final String code;

        @Min(100) @Max(200) private final int score;

        @Email private final String mailbox;

        private final List<@Size(min = 4, max = 4) String> tags;

        Account(String code, int score, String mailbox, List<String> tags) {
            this.code = code;
            this.score = score;
            this.mailbox = mailbox;
            this.tags = tags;
        }
    }

    static final class Badge {

        private final String label;

        Badge(String label) {
            this.label = label;
        }

        @Size(min = 5, max = 5) public String getLabel() {
            return label;
        }
    }

    static final class Ticket {

        @Size(min = 3, max = 3) @Pattern(regexp = "[0-9]+") private final String code;

        Ticket(@Size(min = 2, max = 2) String code) {
            this.code = code;
        }
    }

    static final class TaggedTicket {

        private final List<@Size(min = 4, max = 4) String> tags;

        TaggedTicket(List<@Size(min = 2, max = 2) String> tags) {
            this.tags = new ArrayList<>(tags);
        }
    }

    static final class NestedTaggedTicket {

        private final Map<String, List<@Size(min = 4, max = 4) String>> tags;

        NestedTaggedTicket(Map<String, List<@Size(min = 2, max = 2) String>> tags) {
            this.tags = new LinkedHashMap<>(tags);
        }
    }

    static final class NullableTaggedTicket {

        private final List<@Nullable @Size(min = 4, max = 4) String> tags;

        NullableTaggedTicket(List<@Nullable String> tags) {
            this.tags = new ArrayList<>(tags);
        }
    }

    static final class Converted {

        @Size(min = 3, max = 3) private final String count;

        Converted(@Min(1000) @Max(9999) int count) {
            this.count = Integer.toString(count);
        }
    }

    static final class Unstored {

        @Size(min = 30, max = 30) private static String code = "static";

        private final String stored;

        private final List<String> other;

        Unstored(String code, List<String> unrelated) {
            this.stored = code;
            this.other = unrelated;
        }
    }

    static class Base {

        @Size(min = 4, max = 4) protected final String code;

        Base(String code) {
            this.code = code;
        }
    }

    static final class Derived extends Base {

        Derived(String code) {
            super(code);
        }
    }

    static final class Mutable {

        @Size(min = 3, max = 3) private String code;

        Mutable(String code) {
            this.code = code;
        }

        void rename(String code) {
            this.code = code;
        }
    }

    record Pair(
            @Size(min = 3, max = 3) String code,
            @Min(100) @Max(200) int score) {}

    @Test
    @DisplayName("constraints on a final field apply to the constructor argument that fills it")
    void finalFieldConstraintsApplyToTheConstructorArgument() {
        ObjectGenerator<Account> generator = new ObjectGenerator<>(Account.class, CONFIG);

        for (int i = 0; i < SAMPLES; i++) {
            Account account = generator.generate();

            assertEquals(3, account.code.length(), account.code);
            assertTrue(account.score >= 100 && account.score <= 200, () -> "score " + account.score);
            assertFalse(account.tags.isEmpty());
            account.tags.forEach(tag -> assertEquals(4, tag.length(), tag));
            assertValid(account);
        }
    }

    @Test
    @DisplayName("constraints on the field's getter apply to the constructor argument too")
    void getterConstraintsApplyToTheConstructorArgument() {
        for (int i = 0; i < SAMPLES; i++) {
            Badge badge = new ObjectGenerator<>(Badge.class, CONFIG).generate();

            assertEquals(5, badge.getLabel().length(), badge.getLabel());
            assertValid(badge);
        }
    }

    @Test
    @DisplayName("a parameter's own constraint of a type wins, and the field adds the types it lacks")
    void parameterConstraintsComeFirst() {
        for (int i = 0; i < SAMPLES; i++) {
            Ticket ticket = new ObjectGenerator<>(Ticket.class, CONFIG).generate();

            assertTrue(ticket.code.matches("[0-9]{2}"), ticket.code);
        }
    }

    @Test
    @DisplayName("a parameter that constrains its own type arguments, at any depth, keeps them")
    void parameterTypeArgumentConstraintsComeFirst() {
        TaggedTicket ticket = new ObjectGenerator<>(TaggedTicket.class, CONFIG).generate();
        NestedTaggedTicket nested = new ObjectGenerator<>(NestedTaggedTicket.class, CONFIG).generate();

        assertFalse(ticket.tags.isEmpty());
        ticket.tags.forEach(tag -> assertEquals(2, tag.length(), tag));
        assertFalse(nested.tags.isEmpty());
        nested.tags.values().forEach(tags -> tags.forEach(tag -> assertEquals(2, tag.length(), tag)));
    }

    @Test
    @DisplayName("a type annotation that is not a constraint leaves the field's element constraints in force")
    void otherTypeAnnotationsKeepTheFieldElementConstraints() {
        NullableTaggedTicket ticket = new ObjectGenerator<>(NullableTaggedTicket.class, CONFIG).generate();

        assertFalse(ticket.tags.isEmpty());
        ticket.tags.forEach(tag -> assertEquals(4, tag.length(), tag));
    }

    @Test
    @DisplayName("a field of another type, a static field, or no field leaves the argument unconstrained")
    void otherFieldsDoNotConstrainTheArgument() {
        for (int i = 0; i < SAMPLES; i++) {
            Converted converted = new ObjectGenerator<>(Converted.class, CONFIG).generate();
            Unstored unstored = new ObjectGenerator<>(Unstored.class, CONFIG).generate();

            assertEquals(4, converted.count.length(), converted.count);
            assertNotEquals(30, unstored.stored.length(), unstored.stored);
            assertNotNull(unstored.other);
        }
    }

    @Test
    @DisplayName("an inherited field constrains the subclass constructor argument")
    void inheritedFieldConstrainsTheArgument() {
        for (int i = 0; i < SAMPLES; i++) {
            Derived derived = new ObjectGenerator<>(Derived.class, CONFIG).generate();

            assertEquals(4, derived.code.length(), derived.code);
        }
    }

    @Test
    @DisplayName("a mutable field keeps a constrained value from its constructor argument")
    void mutableFieldKeepsAConstrainedConstructorValue() {
        for (int i = 0; i < SAMPLES; i++) {
            Mutable mutable = new ObjectGenerator<>(Mutable.class, CONFIG).generate();

            assertEquals(3, mutable.code.length(), mutable.code);
            assertValid(mutable);
        }
    }

    @Test
    @DisplayName("record components keep their constraints")
    void recordComponentsKeepTheirConstraints() {
        for (int i = 0; i < SAMPLES; i++) {
            Pair pair = new ObjectGenerator<>(Pair.class, CONFIG).generate();

            assertEquals(3, pair.code().length(), pair.code());
            assertValid(pair);
        }
    }

    @Test
    @DisplayName("only a named constructor parameter reads the field it initializes")
    void onlyNamedConstructorParametersReadTheirField() throws Exception {
        Parameter constructorParameter =
                Mutable.class.getDeclaredConstructor(String.class).getParameters()[0];
        Parameter methodParameter =
                Mutable.class.getDeclaredMethod("rename", String.class).getParameters()[0];
        Parameter unnamedParameter =
                StringBuilder.class.getConstructor(String.class).getParameters()[0];

        assertEquals(
                Mutable.class.getDeclaredField("code"), BeanValidationSupport.initializedField(constructorParameter));
        assertNull(BeanValidationSupport.initializedField(methodParameter));
        assertFalse(unnamedParameter.isNamePresent());
        assertNull(BeanValidationSupport.initializedField(unnamedParameter));
        assertTrue(BeanValidationSupport.constraintModelFor(constructorParameter, String.class)
                .constrained());
        assertFalse(BeanValidationSupport.constraintModelFor(methodParameter, String.class)
                .constrained());
        assertSame(
                BeanValidationSupport.constraintModelFor(constructorParameter, String.class),
                BeanValidationSupport.constraintModelFor(constructorParameter, String.class));
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
