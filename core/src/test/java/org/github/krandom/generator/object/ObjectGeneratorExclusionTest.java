/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithExcludes;
import org.github.krandom.generator.object.Exclude;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — field exclusion")
class ObjectGeneratorExclusionTest {

    // ── @Exclude annotation ───────────────────────────────────────────────────

    @Test
    @DisplayName("@Exclude leaves field null, non-excluded fields still populated")
    void excludeAnnotationLeavesFieldNull() {
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class).generate();
        assertNull(p.getPassword(),  "@Exclude field must remain null");
        assertNotNull(p.getUsername(), "non-excluded field must be populated");
    }

    // ── excludeField(name) ────────────────────────────────────────────────────

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

    // ── excludeType(Class<?>) ─────────────────────────────────────────────────

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

    // ── exclude(Predicate<Field>) ─────────────────────────────────────────────

    @Test
    @DisplayName("exclude(FieldPredicates.named(...)) suppresses the matching field")
    void excludeViaCustomPredicate() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .exclude(FieldPredicates.named("username"))
                .build();
        PersonWithExcludes p = new ObjectGenerator<>(PersonWithExcludes.class, config).generate();
        assertNull(p.getUsername(), "field matched by predicate must be null");
    }

    // ── FieldPredicates.inClass ───────────────────────────────────────────────

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

    // ── Multiple exclusions combined ──────────────────────────────────────────

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

    // ── Record component exclusion (covers defaultForType branches) ───────────

    /**
     * Record with every primitive type plus String as @Exclude components.
     * Exercises every branch of ObjectGenerator.defaultForType().
     */
    record AllTypesRecord(
            @Exclude boolean  flag,
            @Exclude byte     byteVal,
            @Exclude short    shortVal,
            @Exclude int      intVal,
            @Exclude long     longVal,
            @Exclude float    floatVal,
            @Exclude double   doubleVal,
            @Exclude char     charVal,
            @Exclude String   stringVal
    ) {}

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
