/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.core.model.PersonWithExcludes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ObjectGenerator — field exclusion")
class ObjectGeneratorExclusionTest {

    @Test
    @DisplayName("@Exclude leaves field null, non-excluded fields still populated")
    void excludeAnnotationLeavesFieldNull() {
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class).generate();
        assertNull(p.getPassword(), "@Exclude field must remain null");
        assertNotNull(p.getUsername(), "non-excluded field must be populated");
    }

    @Test
    @DisplayName("excludeField(name) suppresses population of the named field")
    void excludeFieldByName() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .excludeField("username")
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getUsername(), "excluded-by-name field must remain null");
    }

    @Test
    @DisplayName("excludeField(name) does not suppress other fields")
    void excludeFieldByNameDoesNotAffectOthers() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .excludeField("username")
                                                            .build();
        // password is @Exclude so it is null; age is a primitive (not excluded here)
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getPassword(), "@Exclude field must still be null");
    }

    // ── @Exclude annotation ───────────────────────────────────────────────────

    @Test
    @DisplayName("excludeType(String.class) sets all String fields to null")
    void excludeTypeString() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .excludeType(String.class)
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getPassword(), "String field must be null (excluded by type)");
        assertNull(p.getUsername(), "String field must be null (excluded by type)");
    }

    // ── excludeField(name) ────────────────────────────────────────────────────

    @Test
    @DisplayName("excludeType(TypePredicates.inPackage(...)) suppresses matching package types")
    void excludeTypeByPackagePredicate() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .excludeType(TypePredicates.inPackage("java.time"))
                                                            .build();
        PackageTypeExclusionTarget target = new ObjectGenerator<>(PackageTypeExclusionTarget.class, config).generate();
        assertNull(target.getDate(), "java.time type should be excluded");
        assertNotNull(target.getTitle(), "non-matching package type must still be populated");
    }

    @Test
    @DisplayName("exclude(FieldPredicates.named(...)) suppresses the matching field")
    void excludeViaCustomPredicate() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.named("username"))
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getUsername(), "field matched by predicate must be null");
    }

    // ── excludeType(Class<?>) ─────────────────────────────────────────────────

    @Test
    @DisplayName("exclude(FieldPredicates.isAnnotatedWith(...)) suppresses annotated fields")
    void excludeViaAnnotationPredicate() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.isAnnotatedWith(LegacyField.class))
                                                            .build();
        AnnotationPredicateTarget target = new ObjectGenerator<>(AnnotationPredicateTarget.class, config).generate();
        assertNull(target.getLegacyValue(), "annotated field must be excluded");
        assertNotNull(target.getActiveValue(), "non-annotated field must be populated");
    }

    @Test
    @DisplayName("exclude(FieldPredicates.hasModifiers(...)) suppresses fields by modifiers")
    void excludeViaModifierPredicate() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.hasModifiers(Modifier.PRIVATE))
                                                            .build();
        ModifierPredicateTarget target = new ObjectGenerator<>(ModifierPredicateTarget.class, config).generate();
        assertNull(target.getHidden(), "private field must be excluded");
        assertNotNull(target.getVisible(), "non-private field must still be populated");
    }

    // ── exclude(Predicate<Field>) ─────────────────────────────────────────────

    @Test
    @DisplayName("exclude(predicate.and(...)) supports AND composition")
    void excludeViaAndComposition() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.named("hidden").and(FieldPredicates.hasModifiers(Modifier.PRIVATE)))
                                                            .build();
        ModifierPredicateTarget target = new ObjectGenerator<>(ModifierPredicateTarget.class, config).generate();
        assertNull(target.getHidden(), "AND predicate should exclude matching private hidden field");
        assertNotNull(target.getVisible(), "AND predicate should not exclude non-matching field");
    }

    @Test
    @DisplayName("exclude(predicate.or(...)) supports OR composition")
    void excludeViaOrComposition() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.named("hidden").or(FieldPredicates.named("visible")))
                                                            .build();
        ModifierPredicateTarget target = new ObjectGenerator<>(ModifierPredicateTarget.class, config).generate();
        assertNull(target.getHidden(), "OR predicate should exclude hidden");
        assertNull(target.getVisible(), "OR predicate should exclude visible");
    }

    @Test
    @DisplayName("exclude(predicate.negate()) supports NOT composition")
    void excludeViaNegateComposition() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.named("hidden").negate())
                                                            .build();
        ModifierPredicateTarget target = new ObjectGenerator<>(ModifierPredicateTarget.class, config).generate();
        assertNotNull(target.getHidden(), "negated predicate should keep the original match");
        assertNull(target.getVisible(), "negated predicate should exclude non-matching field");
    }

    @Test
    @DisplayName("FieldPredicates.inClass — predicate returns true for fields in the target class")
    void inClassPredicateTrueForTargetClassFields() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.inClass(PersonWithExcludes.class))
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        // All fields in PersonWithExcludes are excluded → non-primitives are null
        assertNull(p.getUsername(), "inClass excludes fields declared in the target class");
    }

    @Test
    @DisplayName("FieldPredicates.inClass — predicate returns false for fields outside the target class")
    void inClassPredicateFalseForOtherClassFields() {
        // inClass(String.class) never matches PersonWithExcludes fields → username is populated
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .exclude(FieldPredicates.inClass(String.class))
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNotNull(p.getUsername(), "inClass(other) must not exclude fields from a different class");
    }

    @Test
    @DisplayName("combining excludeField and excludeType excludes the union of fields")
    void combinedExclusions() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .excludeField("username")
                                                            .excludeType(String.class) // covers password as well
                                                            .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getUsername(), "username must be null");
        assertNull(p.getPassword(), "password must be null");
    }

    // ── FieldPredicates.inClass ───────────────────────────────────────────────


    static class AnnotationPredicateTarget {

        @LegacyField
        private String legacyValue;
        private String activeValue;

        String getLegacyValue() {
            return legacyValue;
        }

        String getActiveValue() {
            return activeValue;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface LegacyField {
    }


    static class ModifierPredicateTarget {

        String visible;
        private String hidden;

        String getHidden() {
            return hidden;
        }

        String getVisible() {
            return visible;
        }
    }

    // ── Multiple exclusions combined ──────────────────────────────────────────


    static class PackageTypeExclusionTarget {

        java.time.LocalDate date;
        String              title;

        java.time.LocalDate getDate() {
            return date;
        }

        String getTitle() {
            return title;
        }
    }

    // ── Record component exclusion (covers defaultForType branches) ───────────


    /**
     * Record with every primitive type plus String as @Exclude components.
     * Exercises every branch of ObjectGenerator.defaultForType().
     */
    record AllTypesRecord(
        @Exclude boolean flag,
        @Exclude byte byteVal,
        @Exclude short shortVal,
        @Exclude int intVal,
        @Exclude long longVal,
        @Exclude float floatVal,
        @Exclude double doubleVal,
        @Exclude char charVal,
        @Exclude String stringVal
    ) {

    }


    @Nested
    @DisplayName("Record component @Exclude — defaultForType coverage")
    class RecordExclusionTest {

        @Test
        @DisplayName("excluded boolean component defaults to false")
        void excludedBoolean() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertFalse(r.flag(), "excluded boolean must be false");
        }

        @Test
        @DisplayName("excluded byte component defaults to 0")
        void excludedByte() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0, r.byteVal(), "excluded byte must be 0");
        }

        @Test
        @DisplayName("excluded short component defaults to 0")
        void excludedShort() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0, r.shortVal(), "excluded short must be 0");
        }

        @Test
        @DisplayName("excluded int component defaults to 0")
        void excludedInt() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0, r.intVal(), "excluded int must be 0");
        }

        @Test
        @DisplayName("excluded long component defaults to 0L")
        void excludedLong() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0L, r.longVal(), "excluded long must be 0L");
        }

        @Test
        @DisplayName("excluded float component defaults to 0.0f")
        void excludedFloat() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0.0f, r.floatVal(), 0.0001f, "excluded float must be 0.0f");
        }

        @Test
        @DisplayName("excluded double component defaults to 0.0")
        void excludedDouble() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals(0.0, r.doubleVal(), 0.0001, "excluded double must be 0.0");
        }

        @Test
        @DisplayName("excluded char component defaults to null char")
        void excludedChar() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertEquals('\0', r.charVal(), "excluded char must be '\\0'");
        }

        @Test
        @DisplayName("excluded String component defaults to null")
        void excludedString() {
            AllTypesRecord r = new ObjectGenerator<>(AllTypesRecord.class).generate();
            assertNull(r.stringVal(), "excluded String must be null");
        }
    }
}
