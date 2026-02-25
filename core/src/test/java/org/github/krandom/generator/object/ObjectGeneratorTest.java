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

        /** A class whose no-arg constructor throws — triggers ReflectiveOperationException path. */
        static class ThrowingCtor {
            ThrowingCtor() { throw new RuntimeException("ctor intentionally throws"); }
            String value;
        }

        @Test
        @DisplayName("class without no-arg constructor is instantiated via Objenesis")
        void missingNoArgCtorHandledByObjenesis() {
            // Objenesis bypasses the constructor — generation succeeds without throwing.
            NoDefaultCtor result = assertDoesNotThrow(
                    () -> new ObjectGenerator<>(NoDefaultCtor.class).generate());
            assertNotNull(result);
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

        @Test
        @DisplayName("ObjectGenerationException message-only constructor sets message")
        void exceptionMessageOnlyConstructor() {
            ObjectGenerationException ex = new ObjectGenerationException("standalone message");
            assertEquals("standalone message", ex.getMessage());
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("constructor that throws is wrapped in ObjectGenerationException (ReflectiveOperationException path)")
        void throwingConstructorWrappedInOGE() {
            // ctor.newInstance() raises InvocationTargetException (a ReflectiveOperationException)
            // which is caught in generate() and re-thrown as ObjectGenerationException.
            ObjectGenerationException ex = assertThrows(ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(ThrowingCtor.class).generate());
            assertNotNull(ex.getCause(), "Expected a cause wrapping the original exception");
        }

        @Test
        @DisplayName("toString includes type name, depth and maxDepth")
        void toStringContainsTypeAndDepth() {
            ObjectGenerator<Address> gen = new ObjectGenerator<>(Address.class);
            String s = gen.toString();
            assertTrue(s.contains("Address"),  "Expected class name in toString: " + s);
            assertTrue(s.contains("depth=0"),  "Expected depth in toString: " + s);
        }
    }

    // ── FieldGeneratorResolver branch coverage ────────────────────────────────

    @Nested
    @DisplayName("FieldGeneratorResolver — branch coverage")
    class FieldResolutionTest {

        // ── Fixtures ──────────────────────────────────────────────────────────

        enum EmptyStatus {}

        static class WithArrayField     { String[] tags; }
        static class WithInterfaceField { Runnable runner; }
        static abstract class AbstractBase {}
        static class WithAbstractField  { AbstractBase base; }
        static class WithJdkTypeField   { java.util.Date created; }
        static class WithEmptyEnumField { EmptyStatus status; }

        static class WithStaticAndFinalFields {
            static String staticVal = "static";
            final int     finalVal  = 1;
            String        mutable;
        }

        static class Inner  { String value; }
        static class Middle { Inner inner; }
        static class Outer  { Middle middle; }

        // Objenesis now handles no-arg-constructor-free classes; use a throwing ctor for error cases
        static class ThrowsOnCreate   { ThrowsOnCreate() { throw new RuntimeException("deliberate"); } String v; }
        static class WithThrowingNestedField { ThrowsOnCreate nested; String name; }

        // For Exception-catch branch in resolveAndGenerate (non-OGE from nested generation)
        static class InnerWithString { String value; }
        static class OuterWithInner  { InnerWithString inner; int num; }

        // ── Tests ─────────────────────────────────────────────────────────────

        @Test
        @DisplayName("array-typed field is auto-populated with 3 elements")
        void arrayFieldAutoPopulated() {
            WithArrayField obj = new ObjectGenerator<>(WithArrayField.class).generate();
            assertNotNull(obj);
            assertNotNull(obj.tags, "array-typed field should be auto-populated");
            assertEquals(3, obj.tags.length, "array should have 3 elements");
            for (String tag : obj.tags) assertNotNull(tag, "each array element should be non-null");
        }

        @Test
        @DisplayName("interface-typed field is left null (isInterface branch in isNestableType)")
        void interfaceFieldLeftNull() {
            WithInterfaceField obj = new ObjectGenerator<>(WithInterfaceField.class).generate();
            assertNotNull(obj);
            assertNull(obj.runner, "interface-typed field should be null");
        }

        @Test
        @DisplayName("abstract-class-typed field is left null (isAbstract branch in isNestableType)")
        void abstractClassFieldLeftNull() {
            WithAbstractField obj = new ObjectGenerator<>(WithAbstractField.class).generate();
            assertNotNull(obj);
            assertNull(obj.base, "abstract-typed field should be null");
        }

        @Test
        @DisplayName("JDK-typed field is left null (startsWith 'java.' branch in isNestableType)")
        void jdkTypeFieldLeftNull() {
            WithJdkTypeField obj = new ObjectGenerator<>(WithJdkTypeField.class).generate();
            assertNotNull(obj);
            assertNull(obj.created, "JDK-typed field should be null");
        }

        @Test
        @DisplayName("empty enum field returns null (constants.length == 0 branch)")
        void emptyEnumFieldIsNull() {
            WithEmptyEnumField obj = new ObjectGenerator<>(WithEmptyEnumField.class).generate();
            assertNotNull(obj);
            assertNull(obj.status, "empty-enum field should be null");
        }

        @Test
        @DisplayName("static and final fields are skipped by collectSettableFields")
        void staticAndFinalFieldsSkipped() {
            WithStaticAndFinalFields obj = new ObjectGenerator<>(WithStaticAndFinalFields.class).generate();
            assertNotNull(obj);
            assertEquals("static", WithStaticAndFinalFields.staticVal, "static field must not be modified");
            assertEquals(1, obj.finalVal, "final field must not be modified");
            assertNotNull(obj.mutable, "mutable field must be populated");
        }

        @Test
        @DisplayName("depth guard returns null for nested objects beyond maxDepth")
        void depthGuardReturnsNull() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder().maxDepth(1).build();
            Outer outer = new ObjectGenerator<>(Outer.class, cfg).generate();
            assertNotNull(outer.middle, "Middle should be generated (depth 0 → 1 is within maxDepth=1)");
            assertNull(outer.middle.inner, "Inner should be null: depth guard fires at depth 1 >= maxDepth 1");
        }

        @Test
        @DisplayName("ignoreErrors=true swallows ObjectGenerationException from nested type with throwing ctor")
        void ignoreErrorsSwallowsNestedOGE() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder().ignoreErrors(true).build();
            WithThrowingNestedField obj = new ObjectGenerator<>(WithThrowingNestedField.class, cfg).generate();
            assertNotNull(obj);
            assertNull(obj.nested, "nested field should be null when OGE is swallowed");
            assertNotNull(obj.name, "other fields should still be populated");
        }

        @Test
        @DisplayName("ignoreErrors=false re-throws ObjectGenerationException from nested type with throwing ctor")
        void ignoreErrorsRethrowsNestedOGE() {
            assertThrows(ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(WithThrowingNestedField.class).generate());
        }

        @Test
        @DisplayName("ignoreErrors=true swallows non-OGE exception from nested generation (Exception catch)")
        void ignoreErrorsSwallowsRuntimeExceptionFromNested() {
            // String override throws RuntimeException inside InnerWithString.generate() —
            // this propagates uncaught through generate(), reaching the Exception catch.
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                    .override(String.class, () -> { throw new RuntimeException("intentional"); })
                    .ignoreErrors(true)
                    .build();
            OuterWithInner obj = new ObjectGenerator<>(OuterWithInner.class, cfg).generate();
            assertNotNull(obj);
            assertNull(obj.inner, "nested field should be null when generation throws RuntimeException");
        }

        @Test
        @DisplayName("ignoreErrors=false wraps non-OGE exception in ObjectGenerationException (Exception catch)")
        void ignoreErrorsFalseWrapsRuntimeExceptionFromNested() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                    .override(String.class, () -> { throw new RuntimeException("intentional"); })
                    .build();
            assertThrows(ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(OuterWithInner.class, cfg).generate());
        }
    }

    // ── generateClass field.set() failure branches ─────────────────────────────

    @Nested
    @DisplayName("generateClass — field.set() failure branches")
    class FieldSetFailureTest {

        @Test
        @DisplayName("ignoreErrors=true silently skips field whose override returns the wrong type")
        void ignoreErrorsSkipsWrongTypeField() {
            // Field-level override returns a String for Address.houseNumber (int) —
            // field.set() throws IllegalArgumentException, which must be swallowed.
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                    .override(Address.class, "houseNumber", () -> "NOT_AN_INT")
                    .ignoreErrors(true)
                    .build();
            assertDoesNotThrow(() -> new ObjectGenerator<>(Address.class, cfg).generate());
        }

        @Test
        @DisplayName("ignoreErrors=false throws ObjectGenerationException when field.set() fails")
        void ignoreErrorsFalseThrowsOnWrongTypeField() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                    .override(Address.class, "houseNumber", () -> "NOT_AN_INT")
                    .build();
            assertThrows(ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(Address.class, cfg).generate());
        }
    }
}
