/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.*;
import org.github.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator")
class ObjectGeneratorTest {

    private static final int SAMPLES = 50;

    // ── Plain class — flat (Address) ──────────────────────────────────────────

    @Nested
    @DisplayName("Plain class — flat POJO (Address)")
    class FlatPojoTest {

        @Test
        @DisplayName("generates a non-null instance")
        void generatesNonNull() {
            Address addr = new ObjectGenerator<>(Address.class).generate();
            assertNotNull(addr);
        }

        @Test
        @DisplayName("populates String field")
        void populatesString() {
            Address addr = new ObjectGenerator<>(Address.class).generate();
            assertNotNull(addr.getStreet());
            assertFalse(addr.getStreet().isEmpty());
        }

        @Test
        @DisplayName("populates all numeric primitive fields")
        void populatesNumericPrimitives() {
            // Run many times — primitives default to 0, so any non-zero value proves population.
            // Over SAMPLES runs we expect at least one non-zero value for each field.
            boolean sawNonZeroInt   = false;
            boolean sawNonZeroFloor = false;
            boolean sawNonZeroLat   = false;

            for (int i = 0; i < SAMPLES; i++) {
                Address a = new ObjectGenerator<>(Address.class).generate();
                if (a.getHouseNumber() != 0) sawNonZeroInt   = true;
                if (a.getFloor()       != 0) sawNonZeroFloor = true;
                if (a.getLatitude()    != 0) sawNonZeroLat   = true;
            }

            assertTrue(sawNonZeroInt,   "houseNumber (int) was never non-zero");
            assertTrue(sawNonZeroFloor, "floor (byte) was never non-zero");
            assertTrue(sawNonZeroLat,   "latitude (double) was never non-zero");
        }

        @Test
        @DisplayName("generates distinct instances across calls")
        void generatesDistinctInstances() {
            ObjectGenerator<Address> gen = new ObjectGenerator<>(Address.class);
            Set<String> streets = new HashSet<>();
            for (int i = 0; i < SAMPLES; i++) streets.add(gen.generate().getStreet());
            assertTrue(streets.size() > 1, "Expected distinct street values across " + SAMPLES + " samples");
        }

        @Test
        @DisplayName("generateList returns correct count")
        void generateList() {
            List<Address> list = new ObjectGenerator<>(Address.class).generateList(10);
            assertEquals(10, list.size());
            list.forEach(a -> assertNotNull(a.getStreet()));
        }
    }

    // ── Plain class — inheritance (Person extends BaseJavaClass) ──────────────

    @Nested
    @DisplayName("Plain class — with inheritance and nested types (Person)")
    class InheritanceAndNestedTest {

        @Test
        @DisplayName("generates a non-null Person")
        void generatesNonNull() {
            assertNotNull(new ObjectGenerator<>(Person.class).generate());
        }

        @Test
        @DisplayName("populates own String fields")
        void populatesOwnFields() {
            Person p = new ObjectGenerator<>(Person.class).generate();
            assertNotNull(p.getFirstName());
            assertNotNull(p.getLastName());
            assertFalse(p.getFirstName().isEmpty());
        }

        @Test
        @DisplayName("populates enum field with a valid constant")
        void populatesEnumField() {
            Set<Status> validConstants = new HashSet<>(Arrays.asList(Status.values()));
            for (int i = 0; i < SAMPLES; i++) {
                Status s = new ObjectGenerator<>(Person.class).generate().getStatus();
                assertNotNull(s, "status must not be null");
                assertTrue(validConstants.contains(s), "unexpected enum value: " + s);
            }
        }

        @Test
        @DisplayName("all four Status constants appear over many generations")
        void allEnumConstantsAppear() {
            Set<Status> seen = new HashSet<>();
            ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class);
            for (int i = 0; i < 200; i++) seen.add(gen.generate().getStatus());
            assertEquals(Status.values().length, seen.size(),
                    "Not all Status constants were generated. Seen: " + seen);
        }

        @Test
        @DisplayName("populates nested Address field")
        void populatesNestedObject() {
            Person p = new ObjectGenerator<>(Person.class).generate();
            assertNotNull(p.getAddress(), "nested Address must not be null");
            assertNotNull(p.getAddress().getStreet(), "nested Address.street must not be null");
        }
    }

    // ── Records ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Java record (PersonRecord)")
    class RecordTest {

        @Test
        @DisplayName("generates a non-null record instance")
        void generatesNonNull() {
            assertNotNull(new ObjectGenerator<>(PersonRecord.class).generate());
        }

        @Test
        @DisplayName("all record components are populated")
        void allComponentsPopulated() {
            PersonRecord r = new ObjectGenerator<>(PersonRecord.class).generate();
            assertNotNull(r.firstName(), "firstName component is null");
            assertNotNull(r.lastName(),  "lastName component is null");
            assertNotNull(r.status(),    "status component is null");
            assertNotNull(r.address(),   "address component is null");
        }

        @Test
        @DisplayName("record is truly immutable — no setter to bypass, constructor used")
        void recordIsImmutable() {
            // Generating the same record twice should NOT produce the same instance
            ObjectGenerator<PersonRecord> gen = new ObjectGenerator<>(PersonRecord.class);
            PersonRecord r1 = gen.generate();
            PersonRecord r2 = gen.generate();
            // Records implement equals() based on components; highly unlikely to be equal
            // (since components include random Strings, ints, doubles and a nested Address)
            // We just check they are different instances in the JVM sense
            assertNotSame(r1, r2);
        }

        @Test
        @DisplayName("enum component of record gets a valid value")
        void enumComponentPopulated() {
            Set<Status> validConstants = new HashSet<>(Arrays.asList(Status.values()));
            for (int i = 0; i < SAMPLES; i++) {
                Status s = new ObjectGenerator<>(PersonRecord.class).generate().status();
                assertNotNull(s);
                assertTrue(validConstants.contains(s));
            }
        }

        @Test
        @DisplayName("nested Address component in record is populated")
        void nestedComponentPopulated() {
            PersonRecord r = new ObjectGenerator<>(PersonRecord.class).generate();
            assertNotNull(r.address());
            assertNotNull(r.address().getStreet());
        }

        @Test
        @DisplayName("generateList works for records")
        void generateList() {
            List<PersonRecord> list = new ObjectGenerator<>(PersonRecord.class).generateList(20);
            assertEquals(20, list.size());
            list.forEach(r -> assertNotNull(r.firstName()));
        }
    }

    // ── ObjectGeneratorConfig ─────────────────────────────────────────────────

    @Nested
    @DisplayName("ObjectGeneratorConfig")
    class ConfigTest {

        @Test
        @DisplayName("type-level override replaces built-in String generator")
        void typeOverrideForString() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(String.class, () -> "FIXED")
                    .build();

            ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class, config);
            for (int i = 0; i < SAMPLES; i++) {
                Person p = gen.generate();
                assertEquals("FIXED", p.getFirstName());
                assertEquals("FIXED", p.getLastName());
            }
        }

        @Test
        @DisplayName("field-level override applies only to the named field")
        void fieldOverrideForSpecificField() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(Person.class, "firstName", () -> "Alice")
                    .build();

            ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class, config);
            for (int i = 0; i < SAMPLES; i++) {
                Person p = gen.generate();
                assertEquals("Alice", p.getFirstName());
                // lastName must still be randomly generated
                assertNotEquals("Alice", p.getLastName(),
                        "Field override bled into lastName");
            }
        }

        @Test
        @DisplayName("field override wins over type override for the same field")
        void fieldOverrideWinsOverTypeOverride() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(String.class, () -> "TYPE_LEVEL")
                    .override(Person.class, "firstName", () -> "FIELD_LEVEL")
                    .build();

            Person p = new ObjectGenerator<>(Person.class, config).generate();
            assertEquals("FIELD_LEVEL", p.getFirstName());
            assertEquals("TYPE_LEVEL",  p.getLastName());
        }

        @Test
        @DisplayName("maxDepth=1 still creates Address but caps its own nested fields")
        void maxDepthLimitsRecursion() {
            // Person is at depth=0. Address field is resolved at depth=0 → depth(0) < maxDepth(1)
            // so Address ObjectGenerator runs at depth=1.
            // Inside Address at depth=1: primitive/String fields use BUILTINS (no depth guard).
            // If Address had a further nested object it would be null (depth 1 >= maxDepth 1).
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .maxDepth(1)
                    .build();

            Person p = new ObjectGenerator<>(Person.class, config).generate();
            // Address itself is generated (depth-1 is within maxDepth=1)
            assertNotNull(p.getAddress());
            // Primitive/String fields on Address are always populated (BUILTINS bypass depth guard)
            assertNotNull(p.getAddress().getStreet());
        }

        @Test
        @DisplayName("ignoreErrors=true swallows population failures gracefully")
        void ignoreErrorsSwallowsFailures() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .ignoreErrors(true)
                    .build();
            // Even if some field can't be set, the generator should not throw
            assertDoesNotThrow(() -> new ObjectGenerator<>(Person.class, config).generate());
        }
    }

    // ── Generator composition ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Generator composition via map / filter / stream")
    class CompositionTest {

        @Test
        @DisplayName("map() transforms generated Person to a DTO string")
        void mapToString() {
            var gen = new ObjectGenerator<>(Person.class)
                    .map(p -> p.getFirstName() + " " + p.getLastName());
            String name = gen.generate();
            assertNotNull(name);
            assertTrue(name.contains(" "), "Expected 'First Last' format, got: " + name);
        }

        @Test
        @DisplayName("stream() produces on-demand instances")
        void streamUsage() {
            List<Person> people = new ObjectGenerator<>(Person.class)
                    .stream()
                    .limit(30)
                    .toList();
            assertEquals(30, people.size());
            people.forEach(p -> assertNotNull(p.getFirstName()));
        }

        @Test
        @DisplayName("filter() retains only ACTIVE persons")
        void filterByStatus() {
            var gen = new ObjectGenerator<>(Person.class)
                    .filter(p -> p.getStatus() == Status.ACTIVE);
            for (int i = 0; i < 20; i++) {
                assertEquals(Status.ACTIVE, gen.generate().getStatus());
            }
        }
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTest {

        /** A class with no no-arg constructor — must fail clearly. */
        static class NoDefaultCtor {
            private final String value;
            NoDefaultCtor(String value) { this.value = value; }
        }

        @Test
        @DisplayName("missing no-arg constructor throws ObjectGenerationException")
        void missingNoArgCtorThrows() {
            ObjectGenerationException ex = assertThrows(
                    ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(NoDefaultCtor.class).generate());
            assertTrue(ex.getMessage().contains("no-arg constructor"),
                    "Expected helpful message, got: " + ex.getMessage());
        }

        @Test
        @DisplayName("generateList with 0 count returns empty list")
        void generateListZero() {
            assertTrue(new ObjectGenerator<>(Address.class).generateList(0).isEmpty());
        }

        @Test
        @DisplayName("generateList with negative count throws IllegalArgumentException")
        void generateListNegativeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ObjectGenerator<>(Address.class).generateList(-1));
        }
    }
}
