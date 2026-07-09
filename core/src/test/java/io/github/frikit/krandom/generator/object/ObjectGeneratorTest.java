/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.Address;
import io.github.frikit.krandom.generator.core.model.Person;
import io.github.frikit.krandom.generator.core.model.PersonRecord;
import io.github.frikit.krandom.generator.core.model.PersonWithArrays;
import io.github.frikit.krandom.generator.core.model.PersonWithCollections;
import io.github.frikit.krandom.generator.core.model.PersonWithDateTimes;
import io.github.frikit.krandom.generator.core.model.Status;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator")
class ObjectGeneratorTest {

    private static final int SAMPLES = 50;


    static class PreInitializedFields {

        private String presetName = "PRESET";
        private String unsetName;
        private int    presetAge  = 42;
        private int    defaultAge;

        String getPresetName() {
            return presetName;
        }

        String getUnsetName() {
            return unsetName;
        }

        int getPresetAge() {
            return presetAge;
        }

        int getDefaultAge() {
            return defaultAge;
        }
    }


    static class SetterTrap {

        private String value;

        String getValue() {
            return value;
        }

        public void setValue(String value) {
            throw new IllegalStateException("Setter must not be called");
        }
    }

    @Test
    @DisplayName("ObjectGeneratorConfig can project into the public GeneratorConfig path")
    void objectConfigProjectsIntoPublicGeneratorConfigPath() {
        Address address =
            new ObjectGenerator<>(Address.class, ObjectGeneratorConfig.builder().build().toGeneratorConfig()).generate();
        assertNotNull(address);
    }


    static class GenericStrings {

        private String token;
        private String label;

        String getToken() {
            return token;
        }

        String getLabel() {
            return label;
        }
    }


    static class SemanticProfile {

        private String firstName;
        private String lastName;
        private String email;
        private String username;
        private String phoneNumber;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String companyName;
        private String url;
        private String domain;
        private String uuid;
    }

    static class NullableReferences {

        private String  label;
        private Address address;

        String getLabel() {
            return label;
        }

        Address getAddress() {
            return address;
        }
    }

    static class DepthOuter {

        private DepthMiddle middle;
    }

    static class DepthMiddle {

        private DepthInner inner;
    }

    static class DepthInner {

        private String value;
    }

    static class RootIgnoreErrorsHolder {

        private RootThrowingNested nested;
        private String             name;
    }

    static class RootAdvancedConfigHolder {

        private String    name;
        private String    password;
        private Integer   score;
        private LocalDate createdAt;
    }

    static class RootThrowingNested {

        RootThrowingNested() {
            throw new IllegalStateException("boom");
        }
    }

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
            boolean sawNonZeroInt = false;
            boolean sawNonZeroFloor = false;
            boolean sawNonZeroLat = false;

            for (int i = 0; i < SAMPLES; i++) {
                Address a = new ObjectGenerator<>(Address.class).generate();
                if (a.getHouseNumber() != 0) sawNonZeroInt = true;
                if (a.getFloor() != 0) sawNonZeroFloor = true;
                if (a.getLatitude() != 0) sawNonZeroLat = true;
            }

            assertTrue(sawNonZeroInt, "houseNumber (int) was never non-zero");
            assertTrue(sawNonZeroFloor, "floor (byte) was never non-zero");
            assertTrue(sawNonZeroLat, "latitude (double) was never non-zero");
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
        @DisplayName("status field semantics keep Person lifecycle enums in the business subset")
        void statusFieldUsesBusinessLifecycleSubset() {
            Set<Status> seen = new HashSet<>();
            ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class);
            for (int i = 0; i < 200; i++) seen.add(gen.generate().getStatus());
            assertTrue(seen.contains(Status.ACTIVE), "ACTIVE should still appear. Seen: " + seen);
            assertFalse(seen.contains(Status.PENDING), "PENDING should be filtered out by active/status coherence. Seen: " + seen);
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
            assertNotNull(r.lastName(), "lastName component is null");
            assertNotNull(r.status(), "status component is null");
            assertNotNull(r.address(), "address component is null");
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

    @Nested
    @DisplayName("Shared GeneratorConfig integration")
    class SharedGeneratorConfigTest {

        @Test
        @DisplayName("GeneratorConfig constructor applies configured string lengths")
        void generatorConfigAppliesStringLengthDefaults() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .stringLength(12, 12)
                                                    .build();

            GenericStrings value = new ObjectGenerator<>(GenericStrings.class, config).generate();

            assertEquals(12, value.getToken().length());
            assertEquals(12, value.getLabel().length());
        }

        @Test
        @DisplayName("shared collection-size defaults flow into lists and arrays")
        void generatorConfigAppliesCollectionSizes() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .collectionSize(4, 4)
                                                    .build();

            PersonWithCollections collections = new ObjectGenerator<>(PersonWithCollections.class, config).generate();
            PersonWithArrays arrays = new ObjectGenerator<>(PersonWithArrays.class, config).generate();

            assertEquals(4, collections.getHobbies().size());
            assertEquals(4, arrays.getTags().length);
            assertEquals(4, arrays.getScores().length);
            assertEquals(4, arrays.getAddresses().length);
        }

        @Test
        @DisplayName("seeded GeneratorConfig produces a repeatable sequence across generator instances")
        void seededGeneratorConfigProducesRepeatableSequence() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .seed(11L)
                                                    .stringLength(10, 10)
                                                    .build();

            ObjectGenerator<Person> left = new ObjectGenerator<>(Person.class, config);
            ObjectGenerator<Person> right = new ObjectGenerator<>(Person.class, config);

            Person leftFirst = left.generate();
            Person leftSecond = left.generate();
            Person rightFirst = right.generate();
            Person rightSecond = right.generate();

            assertEquals(leftFirst.getFirstName(), rightFirst.getFirstName());
            assertEquals(leftFirst.getLastName(), rightFirst.getLastName());
            assertEquals(leftFirst.getAddress().getStreet(), rightFirst.getAddress().getStreet());
            assertEquals(leftSecond.getFirstName(), rightSecond.getFirstName());
            assertEquals(leftSecond.getLastName(), rightSecond.getLastName());
            assertEquals(leftSecond.getAddress().getStreet(), rightSecond.getAddress().getStreet());
        }

        @Test
        @DisplayName("root object generation settings apply through GeneratorConfig directly")
        void generatorConfigAppliesObjectGenerationSettings() {
            LocalDate min = LocalDate.of(2021, 1, 1);
            LocalDate max = LocalDate.of(2021, 12, 31);
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .seed(19L)
                                                    .stringLength(8, 8)
                                                    .objectMaxDepth(1)
                                                    .objectIgnoreErrors(true)
                                                    .objectOverrideDefaultInitialization(true)
                                                    .objectDateRange(min, max)
                                                    .build();

            DepthOuter depthOuter = new ObjectGenerator<>(DepthOuter.class, config).generate();
            RootIgnoreErrorsHolder ignoreErrorsHolder = new ObjectGenerator<>(RootIgnoreErrorsHolder.class, config).generate();
            PreInitializedFields preInitialized = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            PersonWithDateTimes dated = new ObjectGenerator<>(PersonWithDateTimes.class, config).generate();

            assertNotNull(depthOuter.middle, "first nested object should still be generated");
            assertNull(depthOuter.middle.inner, "deeper nested object should honor root maxDepth");
            assertNull(ignoreErrorsHolder.nested, "throwing nested type should be swallowed by root ignoreErrors");
            assertNotNull(ignoreErrorsHolder.name, "other fields should still be generated");
            assertEquals(8, preInitialized.getPresetName().length(), "preset string should be overwritten by root config");
            assertNotEquals("PRESET", preInitialized.getPresetName());

            LocalDate dob = dated.getDob();
            LocalDate createdAt = dated.getCreatedAt().toLocalDate();
            LocalDate updatedAt = dated.getUpdatedAt().atOffset(ZoneOffset.UTC).toLocalDate();
            LocalDate scheduledAt = dated.getScheduledAt().toLocalDate();
            assertFalse(dob.isBefore(min));
            assertFalse(dob.isAfter(max));
            assertFalse(createdAt.isBefore(min));
            assertFalse(createdAt.isAfter(max));
            assertFalse(updatedAt.isBefore(min));
            assertFalse(updatedAt.isAfter(max));
            assertFalse(scheduledAt.isBefore(min));
            assertFalse(scheduledAt.isAfter(max));
        }

        @Test
        @DisplayName("root null probability can null out nullable reference fields")
        void generatorConfigAppliesNullProbability() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .objectNullProbability(1.0)
                                                    .build();

            NullableReferences value = new ObjectGenerator<>(NullableReferences.class, config).generate();

            assertNull(value.getLabel());
            assertNull(value.getAddress());
        }

        @Test
        @DisplayName("root GeneratorConfig can drive advanced object overrides and exclusions directly")
        void generatorConfigAppliesAdvancedObjectOverridesAndExclusions() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                    .objectOverride(String.class, () -> "root-type")
                                                    .objectOverride(RootAdvancedConfigHolder.class, "name", () -> "root-field")
                                                    .objectOverride(Integer.class, ctx -> 9)
                                                    .objectExcludeField("password")
                                                    .objectExcludeType(LocalDate.class)
                                                    .build();

            RootAdvancedConfigHolder value = new ObjectGenerator<>(RootAdvancedConfigHolder.class, config).generate();

            assertEquals("root-field", value.name);
            assertNull(value.password);
            assertEquals(9, value.score);
            assertNull(value.createdAt);
        }
    }

    @Nested
    @DisplayName("Semantic defaults")
    class SemanticDefaultsTest {

        @Test
        @DisplayName("common business field names use semantic generators by default")
        void commonBusinessFieldsUseSemanticGenerators() {
            SemanticProfile profile = new ObjectGenerator<>(SemanticProfile.class).generate();

            assertNotNull(profile.firstName);
            assertNotNull(profile.lastName);
            assertTrue(profile.email.contains("@"));
            assertFalse(profile.username.isBlank());
            assertFalse(profile.phoneNumber.isBlank());
            assertFalse(profile.city.isBlank());
            assertFalse(profile.state.isBlank());
            assertFalse(profile.postalCode.isBlank());
            assertFalse(profile.country.isBlank());
            assertFalse(profile.companyName.isBlank());
            assertTrue(profile.url.contains("://"));
            assertTrue(profile.domain.contains("."));
            assertDoesNotThrow(() -> UUID.fromString(profile.uuid));
        }

        @Test
        @DisplayName("default unique semantic fields stay unique across one generator sequence")
        void defaultUniqueSemanticFieldsStayUniqueAcrossSequence() {
            ObjectGenerator<SemanticProfile> generator = new ObjectGenerator<>(SemanticProfile.class);
            Set<String> emails = new HashSet<>();
            Set<String> usernames = new HashSet<>();
            Set<String> uuids = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                SemanticProfile profile = generator.generate();
                assertTrue(emails.add(profile.email), "duplicate email generated: " + profile.email);
                assertTrue(usernames.add(profile.username), "duplicate username generated: " + profile.username);
                assertTrue(uuids.add(profile.uuid), "duplicate uuid generated: " + profile.uuid);
            }
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
            assertEquals("TYPE_LEVEL", p.getLastName());
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

        @Test
        @DisplayName("ignoreErrors=true logs swallowed failures at DEBUG level")
        void ignoreErrorsLogsSwallowedFailuresAtDebugLevel() {
            Logger logger = (Logger) LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            Level previousLevel = logger.getLevel();
            logger.setLevel(Level.DEBUG);
            logger.addAppender(appender);
            try {
                GeneratorConfig rootConfig = GeneratorConfig.builder().objectIgnoreErrors(true).build();
                RootIgnoreErrorsHolder holder =
                    new ObjectGenerator<>(RootIgnoreErrorsHolder.class, rootConfig).generate();
                assertNull(holder.nested, "throwing nested type should be swallowed");
                assertTrue(appender.list.stream()
                                  .anyMatch(e -> e.getFormattedMessage().contains("RootIgnoreErrorsHolder.nested")),
                           "swallowed failure should be logged at DEBUG level");
            } finally {
                logger.detachAppender(appender);
                logger.setLevel(previousLevel);
            }
        }

        @Test
        @DisplayName("overrideDefaultInitialization=false preserves non-default initialized values")
        void doesNotOverrideInitializedValuesWhenDisabled() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                                .overrideDefaultInitialization(false)
                                                                .override(String.class, () -> "OVERRIDDEN")
                                                                .override(int.class, () -> 7)
                                                                .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals("PRESET", value.getPresetName(), "non-default String initializer should be preserved");
            assertEquals(42, value.getPresetAge(), "non-default primitive initializer should be preserved");
            assertEquals("OVERRIDDEN", value.getUnsetName(), "null field should still be generated");
            assertEquals(7, value.getDefaultAge(), "default primitive value should still be generated");
        }

        @Test
        @DisplayName("overrideDefaultInitialization=true overwrites initialized values")
        void overridesInitializedValuesWhenEnabled() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                                .overrideDefaultInitialization(true)
                                                                .override(String.class, () -> "OVERRIDDEN")
                                                                .override(int.class, () -> 7)
                                                                .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals("OVERRIDDEN", value.getPresetName());
            assertEquals("OVERRIDDEN", value.getUnsetName());
            assertEquals(7, value.getPresetAge());
            assertEquals(7, value.getDefaultAge());
        }

        @Test
        @DisplayName("population bypasses setters and writes fields directly")
        void populationBypassesSetters() {
            SetterTrap value = new ObjectGenerator<>(SetterTrap.class).generate();
            assertNotNull(value.getValue());
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
            assertTrue(ex.getContext().isEmpty());
        }

        @Test
        @DisplayName("constructor that throws reports sanitized root construction context")
        void throwingConstructorIsContextual() {
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(ThrowingCtor.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.CONSTRUCTION, context.category());
            assertEquals(GenerationOperation.CONSTRUCT, context.operation());
            assertEquals("ThrowingCtor", context.path());
            assertEquals(ThrowingCtor.class, context.ownerType());
            assertEquals(ThrowingCtor.class.getTypeName(), context.declaredType());
            assertEquals(0, context.depth());
            assertTrue(error.getCause() instanceof RuntimeException);
            assertFalse(error.getMessage().contains("personal-looking-value"));
        }

        @Test
        @DisplayName("toString includes type name, depth and maxDepth")
        void toStringContainsTypeAndDepth() {
            ObjectGenerator<Address> gen = new ObjectGenerator<>(Address.class);
            String s = gen.toString();
            assertTrue(s.contains("Address"), "Expected class name in toString: " + s);
            assertTrue(s.contains("depth=0"), "Expected depth in toString: " + s);
        }


        /**
         * A class with no no-arg constructor — must fail clearly.
         */
        static class NoDefaultCtor {

            private final String value;

            NoDefaultCtor(String value) {
                this.value = value;
            }
        }


        /**
         * A class whose no-arg constructor throws — triggers ReflectiveOperationException path.
         */
        static class ThrowingCtor {

            String value;

            ThrowingCtor() {
                throw new RuntimeException("personal-looking-value");
            }
        }
    }

    // ── FieldGeneratorResolver branch coverage ────────────────────────────────


    @Nested
    @DisplayName("FieldGeneratorResolver — branch coverage")
    class FieldResolutionTest {

        // ── Fixtures ──────────────────────────────────────────────────────────


        @Test
        @DisplayName("array-typed field size follows shared collection defaults")
        void arrayFieldAutoPopulated() {
            WithArrayField obj = new ObjectGenerator<>(WithArrayField.class).generate();
            assertNotNull(obj);
            assertNotNull(obj.tags, "array-typed field should be auto-populated");
            assertTrue(obj.tags.length >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
            assertTrue(obj.tags.length <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
            for (String tag : obj.tags) assertNotNull(tag, "each array element should be non-null");
        }

        @Test
        @DisplayName("interface-typed field reports unsupported type in strict mode")
        void interfaceFieldIsUnsupported() {
            assertUnsupportedTypeFailure(WithInterfaceField.class, "runner", Runnable.class);
        }

        @Test
        @DisplayName("abstract-class-typed field reports unsupported type in strict mode")
        void abstractClassFieldIsUnsupported() {
            assertUnsupportedTypeFailure(WithAbstractField.class, "base", AbstractBase.class);
        }

        @Test
        @DisplayName("unsupported JDK-typed field reports context in strict mode")
        void jdkTypeFieldIsUnsupported() {
            assertUnsupportedTypeFailure(WithJdkTypeField.class, "locale", java.util.Locale.class);
        }

        @Test
        @DisplayName("Object-typed field reports unsupported type in strict mode")
        void objectFieldIsUnsupported() {
            assertUnsupportedTypeFailure(WithObjectField.class, "value", Object.class);
        }

        @Test
        @DisplayName("explicit lenient mode leaves unsupported fields null")
        void lenientUnsupportedFieldsAreNull() throws ReflectiveOperationException {
            assertUnsupportedTypeFallback(WithInterfaceField.class, "runner");
            assertUnsupportedTypeFallback(WithAbstractField.class, "base");
            assertUnsupportedTypeFallback(WithJdkTypeField.class, "locale");
            assertUnsupportedTypeFallback(WithObjectField.class, "value");
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
        @DisplayName("strict nested constructor failure gains parent field context")
        void strictNestedConstructorFailureIsContextual() {
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(WithThrowingNestedField.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.CONSTRUCTION, context.category());
            assertEquals(GenerationOperation.CONSTRUCT, context.operation());
            assertEquals("WithThrowingNestedField.nested", context.path());
            assertEquals(ThrowsOnCreate.class.getTypeName(), context.declaredType());
            assertEquals(1, context.depth());
            assertTrue(error.getCause() instanceof RuntimeException);
        }

        @Test
        @DisplayName("ignoreErrors=true swallows non-OGE exception from nested generation (Exception catch)")
        void ignoreErrorsSwallowsRuntimeExceptionFromNested() {
            // String override throws RuntimeException inside InnerWithString.generate() —
            // this propagates uncaught through generate(), reaching the Exception catch.
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                                                             .override(String.class, () -> {
                                                                 throw new RuntimeException("intentional");
                                                             })
                                                             .ignoreErrors(true)
                                                             .build();
            OuterWithInner obj = new ObjectGenerator<>(OuterWithInner.class, cfg).generate();
            assertNotNull(obj);
            assertNull(obj.inner, "nested field should be null when generation throws RuntimeException");
        }

        @Test
        @DisplayName("strict nested runtime failure gains parent field context")
        void strictNestedRuntimeFailureIsContextual() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                                                             .override(String.class, () -> {
                                                                 throw new RuntimeException("intentional");
                                                             })
                                                             .build();
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(OuterWithInner.class, cfg).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.REFLECTION, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals("OuterWithInner.inner", context.path());
            assertTrue(error.getCause() instanceof RuntimeException);
        }

        @Test
        @DisplayName("legacy nested ObjectGenerationException gains parent context")
        void legacyNestedFailureGainsParentContext() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                                                             .override(String.class, () -> {
                                                                 throw new ObjectGenerationException(
                                                                     "personal-looking-value");
                                                             })
                                                             .build();
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(OuterWithInner.class, cfg).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.REFLECTION, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals("OuterWithInner.inner", context.path());
            assertEquals(InnerWithString.class.getTypeName(), context.declaredType());
            assertNull(error.getCause());
            assertFalse(error.getMessage().contains("personal-looking-value"));
        }

        @Test
        @DisplayName("structured nested failure composes a root-relative path once")
        void structuredNestedFailureComposesParentPath() {
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(NestedUnsupportedRoot.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals("NestedUnsupportedRoot.middle.child.task", context.path());
            assertEquals(NestedUnsupportedChild.class, context.ownerType());
            assertEquals(Runnable.class.getTypeName(), context.declaredType());
            assertEquals(2, context.depth());
            assertTrue(error.getCause() instanceof UnsupportedOperationException);
        }

        @Test
        @DisplayName("foreign structured nested path is retained below the parent path")
        void foreignNestedFailurePathIsRetained() {
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(ForeignContextRoot.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
            assertEquals("ForeignContextRoot.child.External.path", context.path());
            assertEquals(ForeignContextGenerator.class, context.ownerType());
            assertEquals(7, context.depth());
            assertNull(error.getCause());
        }

        private void assertUnsupportedTypeFailure(Class<?> ownerType,
                                                  String fieldName,
                                                  Class<?> declaredType) {
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(ownerType).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals(ownerType.getSimpleName() + "." + fieldName, context.path());
            assertEquals(ownerType, context.ownerType());
            assertEquals(declaredType.getTypeName(), context.declaredType());
            assertEquals(0, context.depth());
            assertTrue(error.getCause() instanceof UnsupportedOperationException);
        }

        private void assertUnsupportedTypeFallback(Class<?> ownerType,
                                                   String fieldName) throws ReflectiveOperationException {
            GeneratorConfig config = GeneratorConfig.builder().objectIgnoreErrors(true).build();
            Object generated = new ObjectGenerator<>(ownerType, config).generate();
            var field = ownerType.getDeclaredField(fieldName);
            field.setAccessible(true);

            assertNull(field.get(generated));
        }


        enum EmptyStatus {}


        static class WithArrayField {

            String[] tags;
        }


        static class WithInterfaceField {

            Runnable runner;
        }


        static abstract class AbstractBase {

        }

        // ── Tests ─────────────────────────────────────────────────────────────


        static class WithAbstractField {

            AbstractBase base;
        }


        static class WithJdkTypeField {

            java.util.Locale locale;
        }


        static class WithObjectField {

            Object value;
        }


        static class WithEmptyEnumField {

            EmptyStatus status;
        }


        static class WithStaticAndFinalFields {

            static String staticVal = "static";
            final  int    finalVal  = 1;
            String mutable;
        }


        static class Inner {

            String value;
        }


        static class Middle {

            Inner inner;
        }


        static class Outer {

            Middle middle;
        }


        // Objenesis now handles no-arg-constructor-free classes; use a throwing ctor for error cases
        static class ThrowsOnCreate {

            String v;

            ThrowsOnCreate() {
                throw new RuntimeException("deliberate");
            }
        }


        static class WithThrowingNestedField {

            ThrowsOnCreate nested;
            String         name;
        }


        // For Exception-catch branch in resolveAndGenerate (non-OGE from nested generation)
        static class InnerWithString {

            String value;
        }


        static class OuterWithInner {

            InnerWithString inner;
            int             num;
        }


        static class NestedUnsupportedRoot {

            NestedUnsupportedMiddle middle;
        }


        static class NestedUnsupportedMiddle {

            NestedUnsupportedChild child;
        }


        static class NestedUnsupportedChild {

            Runnable task;
        }


        static class ForeignContextRoot {

            ForeignContextChild child;
        }


        static class ForeignContextChild {

            @Randomizer(ForeignContextGenerator.class)
            String value;
        }


        public static class ForeignContextGenerator implements Generator<String> {

            @Override
            public String generate() {
                GenerationFailureContext context = new GenerationFailureContext(
                    GenerationFailureCategory.ASSIGNMENT,
                    GenerationOperation.ASSIGN,
                    "External.path",
                    ForeignContextGenerator.class,
                    String.class.getName(),
                    7,
                    -1);
                throw new ObjectGenerationException("foreign failure", context, null);
            }
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
            Logger logger = (Logger) LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            Level previousLevel = logger.getLevel();
            logger.setLevel(Level.DEBUG);
            logger.addAppender(appender);
            try {
                assertDoesNotThrow(() -> new ObjectGenerator<>(Address.class, cfg).generate());
                assertTrue(appender.list.stream()
                                        .anyMatch(event -> event.getFormattedMessage().contains(
                                            "'Address.houseNumber' (declared type int, depth 0")));
                assertFalse(appender.list.stream()
                                         .anyMatch(event -> event.getFormattedMessage().contains("NOT_AN_INT")));
            } finally {
                logger.detachAppender(appender);
                logger.setLevel(previousLevel);
            }
        }

        @Test
        @DisplayName("ignoreErrors=false throws ObjectGenerationException when field.set() fails")
        void ignoreErrorsFalseThrowsOnWrongTypeField() {
            ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                                                             .override(Address.class, "houseNumber", () -> "NOT_AN_INT")
                                                             .build();
            ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(Address.class, cfg).generate());

            assertEquals("Could not set field 'Address.houseNumber' (declared type int, depth 0)",
                         error.getMessage());
            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
            assertEquals(GenerationOperation.ASSIGN, context.operation());
            assertEquals("Address.houseNumber", context.path());
            assertEquals(Address.class, context.ownerType());
            assertEquals("int", context.declaredType());
            assertEquals(0, context.depth());
            assertEquals(-1, context.recordIndex());
            assertTrue(error.getCause() instanceof IllegalArgumentException);
        }
    }
}
