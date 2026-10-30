/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import io.github.frikit.krandom.generator.core.model.Address;
import io.github.frikit.krandom.generator.core.model.Person;
import io.github.frikit.krandom.generator.core.model.PersonRecord;
import io.github.frikit.krandom.generator.core.model.PersonWithArrays;
import io.github.frikit.krandom.generator.core.model.PersonWithCollections;
import io.github.frikit.krandom.generator.core.model.PersonWithDateTimes;
import io.github.frikit.krandom.generator.core.model.Status;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@DisplayName("ObjectGenerator")
class ObjectGeneratorTest {

    private static final int SAMPLES = 50;

    static class PreInitializedFields {

        private String presetName = "PRESET";
        private String unsetName;
        private int presetAge = 42;
        private int defaultAge;

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

    static class ContactWithPresetEmail {

        private String firstName;
        private String lastName;
        private String email = "preset@example.com";
    }

    static class Ticket {

        private String code;
        private final String codeSeenByConstructor;

        Ticket(String code) {
            this.code = code;
            this.codeSeenByConstructor = code;
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
        Address address = new ObjectGenerator<>(
                        Address.class, ObjectGeneratorConfig.builder().build().toGeneratorConfig())
                .generate();
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

        private String label;
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
        private String name;
    }

    static class RootAdvancedConfigHolder {

        private String name;
        private String password;
        private Integer score;
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
            assertFalse(
                    seen.contains(Status.PENDING),
                    "PENDING should be filtered out by active/status coherence. Seen: " + seen);
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
            GeneratorConfig config =
                    GeneratorConfig.builder().stringLength(12, 12).build();

            GenericStrings value = new ObjectGenerator<>(GenericStrings.class, config).generate();

            assertEquals(12, value.getToken().length());
            assertEquals(12, value.getLabel().length());
        }

        @Test
        @DisplayName("shared collection-size defaults flow into lists and arrays")
        void generatorConfigAppliesCollectionSizes() {
            GeneratorConfig config =
                    GeneratorConfig.builder().collectionSize(4, 4).build();

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
            GeneratorConfig config =
                    GeneratorConfig.builder().seed(11L).stringLength(10, 10).build();

            ObjectGenerator<Person> left = new ObjectGenerator<>(Person.class, config);
            ObjectGenerator<Person> right = new ObjectGenerator<>(Person.class, config);

            Person leftFirst = left.generate();
            Person leftSecond = left.generate();
            Person rightFirst = right.generate();
            Person rightSecond = right.generate();

            assertEquals(leftFirst.getFirstName(), rightFirst.getFirstName());
            assertEquals(leftFirst.getLastName(), rightFirst.getLastName());
            assertEquals(
                    leftFirst.getAddress().getStreet(), rightFirst.getAddress().getStreet());
            assertEquals(leftSecond.getFirstName(), rightSecond.getFirstName());
            assertEquals(leftSecond.getLastName(), rightSecond.getLastName());
            assertEquals(
                    leftSecond.getAddress().getStreet(),
                    rightSecond.getAddress().getStreet());
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
            RootIgnoreErrorsHolder ignoreErrorsHolder =
                    new ObjectGenerator<>(RootIgnoreErrorsHolder.class, config).generate();
            PreInitializedFields preInitialized = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            PersonWithDateTimes dated = new ObjectGenerator<>(PersonWithDateTimes.class, config).generate();

            assertNotNull(depthOuter.middle, "first nested object should still be generated");
            assertNull(depthOuter.middle.inner, "deeper nested object should honor root maxDepth");
            assertNull(ignoreErrorsHolder.nested, "throwing nested type should be swallowed by root ignoreErrors");
            assertNotNull(ignoreErrorsHolder.name, "other fields should still be generated");
            assertEquals(
                    8, preInitialized.getPresetName().length(), "preset string should be overwritten by root config");
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
            GeneratorConfig config =
                    GeneratorConfig.builder().objectNullProbability(1.0).build();

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
                assertNotEquals("Alice", p.getLastName(), "Field override bled into lastName");
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
            ObjectGeneratorConfig config =
                    ObjectGeneratorConfig.builder().maxDepth(1).build();

            Person p = new ObjectGenerator<>(Person.class, config).generate();
            // Address itself is generated (depth-1 is within maxDepth=1)
            assertNotNull(p.getAddress());
            // Primitive/String fields on Address are always populated (BUILTINS bypass depth guard)
            assertNotNull(p.getAddress().getStreet());
        }

        @Test
        @DisplayName("ignoreErrors=true swallows population failures gracefully")
        void ignoreErrorsSwallowsFailures() {
            ObjectGeneratorConfig config =
                    ObjectGeneratorConfig.builder().ignoreErrors(true).build();
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
                GeneratorConfig rootConfig =
                        GeneratorConfig.builder().objectIgnoreErrors(true).build();
                RootIgnoreErrorsHolder holder =
                        new ObjectGenerator<>(RootIgnoreErrorsHolder.class, rootConfig).generate();
                assertNull(holder.nested, "throwing nested type should be swallowed");
                assertTrue(
                        appender.list.stream()
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
        @DisplayName("a field override replaces a non-default initializer while initializers are preserved")
        void fieldOverrideReplacesInitializedValue() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .overrideDefaultInitialization(false)
                    .override(PreInitializedFields.class, "presetName", () -> "RULED")
                    .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals("RULED", value.getPresetName(), "a field override should replace the initializer");
            assertEquals(42, value.getPresetAge(), "a field without its own override keeps its initializer");
        }

        @Test
        @DisplayName("a contextual field override replaces a non-default initializer")
        void contextualFieldOverrideReplacesInitializedValue() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(PreInitializedFields.class, "presetName", context -> "CTX-" + context.getFieldName())
                    .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals("CTX-presetName", value.getPresetName());
            assertEquals(42, value.getPresetAge());
        }

        @Test
        @DisplayName("a predicate field override replaces a non-default initializer")
        void predicateFieldOverrideReplacesInitializedValue() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(field -> field.getName().equals("presetAge"), () -> 7)
                    .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals(7, value.getPresetAge());
            assertEquals("PRESET", value.getPresetName());
        }

        @Test
        @DisplayName("a contextual predicate field override replaces a non-default initializer")
        void contextualPredicateFieldOverrideReplacesInitializedValue() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(field -> field.getName().equals("presetAge"), context -> 9)
                    .build();

            PreInitializedFields value = new ObjectGenerator<>(PreInitializedFields.class, config).generate();
            assertEquals(9, value.getPresetAge());
            assertEquals("PRESET", value.getPresetName());
        }

        @Test
        @DisplayName("populate(existing) overwrites a set field only when it has a field override")
        void populateOverwritesOnlyFieldsWithFieldOverride() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(PreInitializedFields.class, "presetName", () -> "RULED")
                    .build();
            PreInitializedFields existing = new PreInitializedFields();

            PreInitializedFields populated =
                    new ObjectGenerator<>(PreInitializedFields.class, config).populate(existing);

            assertSame(existing, populated);
            assertEquals("RULED", populated.getPresetName());
            assertEquals(42, populated.getPresetAge());
            assertNotNull(populated.getUnsetName());
        }

        @Test
        @DisplayName("semantic coherence keeps a field override that replaced an initializer")
        void coherenceKeepsFieldOverrideOfInitializedField() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(ContactWithPresetEmail.class, "email", () -> "rule@example.org")
                    .build();

            ContactWithPresetEmail value = new ObjectGenerator<>(ContactWithPresetEmail.class, config).generate();
            assertEquals("rule@example.org", value.email);
            assertNotNull(value.firstName);
            assertNotNull(value.lastName);
        }

        @Test
        @DisplayName("a by-name constructor override runs once and is not regenerated for its field")
        void constructorOverrideRunsOnceForItsField() {
            AtomicInteger calls = new AtomicInteger();
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(Ticket.class, "code", () -> "T" + calls.incrementAndGet())
                    .build();

            Ticket ticket = new ObjectGenerator<>(Ticket.class, config).generate();

            assertEquals("T1", ticket.codeSeenByConstructor);
            assertEquals("T1", ticket.code);
            assertEquals(1, calls.get());
        }

        @Test
        @DisplayName("a contextual by-name constructor override runs once for its field")
        void contextualConstructorOverrideRunsOnceForItsField() {
            AtomicInteger calls = new AtomicInteger();
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(Ticket.class, "code", context -> "C" + calls.incrementAndGet())
                    .build();

            Ticket ticket = new ObjectGenerator<>(Ticket.class, config).generate();

            assertEquals("C1", ticket.code);
            assertEquals(1, calls.get());
        }

        @Test
        @DisplayName("a predicate override replaces a field its constructor argument initialized")
        void predicateOverrideReplacesConstructorInitializedField() {
            ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                    .override(field -> field.getName().equals("code"), () -> "PRED")
                    .build();

            Ticket ticket = new ObjectGenerator<>(Ticket.class, config).generate();

            assertEquals("PRED", ticket.code);
            assertNotEquals("PRED", ticket.codeSeenByConstructor, "predicates do not apply to constructor arguments");
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
            var gen = new ObjectGenerator<>(Person.class).map(p -> p.getFirstName() + " " + p.getLastName());
            String name = gen.generate();
            assertNotNull(name);
            assertTrue(name.contains(" "), "Expected 'First Last' format, got: " + name);
        }

        @Test
        @DisplayName("stream() produces on-demand instances")
        void streamUsage() {
            List<Person> people =
                    new ObjectGenerator<>(Person.class).stream().limit(30).toList();
            assertEquals(30, people.size());
            people.forEach(p -> assertNotNull(p.getFirstName()));
        }

        @Test
        @DisplayName("filter() retains only ACTIVE persons")
        void filterByStatus() {
            var gen = new ObjectGenerator<>(Person.class).filter(p -> p.getStatus() == Status.ACTIVE);
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
            NoDefaultCtor result = assertDoesNotThrow(() -> new ObjectGenerator<>(NoDefaultCtor.class).generate());
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
            assertThrows(IllegalArgumentException.class, () -> new ObjectGenerator<>(Address.class).generateList(-1));
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
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(ThrowingCtor.class).generate());

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
            ObjectGeneratorConfig cfg =
                    ObjectGeneratorConfig.builder().maxDepth(1).build();
            Outer outer = new ObjectGenerator<>(Outer.class, cfg).generate();
            assertNotNull(outer.middle, "Middle should be generated (depth 0 → 1 is within maxDepth=1)");
            assertNull(outer.middle.inner, "Inner should be null: depth guard fires at depth 1 >= maxDepth 1");
        }

        @Test
        @DisplayName("ignoreErrors=true swallows ObjectGenerationException from nested type with throwing ctor")
        void ignoreErrorsSwallowsNestedOGE() {
            ObjectGeneratorConfig cfg =
                    ObjectGeneratorConfig.builder().ignoreErrors(true).build();
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
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(OuterWithInner.class, cfg).generate());

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
                        throw new ObjectGenerationException("personal-looking-value");
                    })
                    .build();
            ObjectGenerationException error = assertThrows(
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(OuterWithInner.class, cfg).generate());

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
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(ForeignContextRoot.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
            assertEquals("ForeignContextRoot.child.External.path", context.path());
            assertEquals(ForeignContextGenerator.class, context.ownerType());
            assertEquals(7, context.depth());
            assertNull(error.getCause());
        }

        @Test
        @DisplayName("a nested type root path collapses to its parent field path")
        void nestedTypeRootFailurePathCollapsesToParent() {
            ObjectGenerationException error = assertThrows(
                    ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(NestedRootContextRoot.class).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
            assertEquals("NestedRootContextRoot.child", context.path());
            assertEquals(NestedRootContextGenerator.class, context.ownerType());
        }

        private void assertUnsupportedTypeFailure(Class<?> ownerType, String fieldName, Class<?> declaredType) {
            ObjectGenerationException error =
                    assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(ownerType).generate());

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals(ownerType.getSimpleName() + "." + fieldName, context.path());
            assertEquals(ownerType, context.ownerType());
            assertEquals(declaredType.getTypeName(), context.declaredType());
            assertEquals(0, context.depth());
            assertTrue(error.getCause() instanceof UnsupportedOperationException);
        }

        private void assertUnsupportedTypeFallback(Class<?> ownerType, String fieldName)
                throws ReflectiveOperationException {
            GeneratorConfig config =
                    GeneratorConfig.builder().objectIgnoreErrors(true).build();
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

        abstract static class AbstractBase {}

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
            final int finalVal = 1;
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
            String name;
        }

        // For Exception-catch branch in resolveAndGenerate (non-OGE from nested generation)
        static class InnerWithString {

            String value;
        }

        static class OuterWithInner {

            InnerWithString inner;
            int num;
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

        static class NestedRootContextRoot {

            NestedRootContextChild child;
        }

        static class NestedRootContextChild {

            @Randomizer(NestedRootContextGenerator.class)
            String value;
        }

        public static class NestedRootContextGenerator implements Generator<String> {

            @Override
            public String generate() {
                GenerationFailureContext context = new GenerationFailureContext(
                        GenerationFailureCategory.ASSIGNMENT,
                        GenerationOperation.ASSIGN,
                        NestedRootContextChild.class.getSimpleName(),
                        NestedRootContextGenerator.class,
                        String.class.getName(),
                        2,
                        -1);
                throw new ObjectGenerationException("nested root failure", context, null);
            }
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
                        .anyMatch(event -> event.getFormattedMessage()
                                .contains("'Address.houseNumber' (declared type int, depth 0")));
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
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(Address.class, cfg).generate());

            assertEquals("Could not set field 'Address.houseNumber' (declared type int, depth 0)", error.getMessage());
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

    // ── Required members at the depth limit ───────────────────────────────────

    @Nested
    @DisplayName("required members at objectMaxDepth")
    class RequiredMemberAtDepthLimitTest {

        record Leaf(String value) {}

        record Mid(@NotNull Leaf leaf) {}

        record Top(@NotNull Mid mid) {}

        record Outermost(@NotNull Top top) {}

        record AccessorOwner(Leaf leaf) {
            @Override
            @NotNull public Leaf leaf() {
                return leaf;
            }
        }

        record AccessorTop(AccessorOwner mid) {}

        static class FieldOwner {
            @NotNull Leaf leaf;
        }

        record FieldTop(FieldOwner mid) {}

        static class InheritingOwner extends FieldOwner {}

        record InheritingTop(InheritingOwner mid) {}

        record ElementTop(List<Mid> mids) {}

        static class GetterOwner {
            Leaf leaf;

            @NotNull public Leaf getLeaf() {
                return leaf;
            }
        }

        record GetterTop(GetterOwner mid) {}

        interface HasLeaf {
            @NotNull Leaf getLeaf();
        }

        static class InterfaceOwner implements HasLeaf {
            Leaf leaf;

            @Override
            public Leaf getLeaf() {
                return leaf;
            }
        }

        record InterfaceTop(InterfaceOwner mid) {}

        static class ParameterOwner {
            final Leaf leaf;

            ParameterOwner(@NotNull Leaf leaf) {
                this.leaf = leaf;
            }
        }

        record ParameterTop(ParameterOwner mid) {}

        static class AssignedOwner {
            @NotNull Leaf leaf;

            AssignedOwner(Leaf leaf) {
                this.leaf = leaf;
            }
        }

        record AssignedTop(AssignedOwner mid) {}

        static class InitializedOwner {
            @NotNull Leaf leaf = new Leaf("preset");
        }

        record InitializedTop(InitializedOwner mid) {}

        static class Bag extends AbstractCollection<String> {
            @Override
            public Iterator<String> iterator() {
                return List.<String>of().iterator();
            }

            @Override
            public int size() {
                return 0;
            }
        }

        record RequiredBagOwner(@NotEmpty Bag bag) {}

        record RequiredBagTop(RequiredBagOwner mid) {}

        record NullableOwner(Leaf leaf, Collection<String> values, Bag bag, Shape shape, Object anything) {}

        record NullableTop(NullableOwner mid) {}

        record SizedOwner(
                @Size(min = 1) Collection<String> values,
                @Size(min = 1) Bag bag) {}

        record SizedTop(SizedOwner mid) {}

        interface Shape {}

        record Circle(int radius) implements Shape {}

        record RequiredShapeOwner(@NotNull Shape shape) {}

        record RequiredShapeTop(RequiredShapeOwner mid) {}

        record RequiredInterfaceCollectionOwner(@NotNull Collection<String> values) {}

        record RequiredInterfaceCollectionTop(RequiredInterfaceCollectionOwner mid) {}

        record NotEmptyInterfaceCollectionOwner(@NotEmpty Collection<String> values) {}

        record NotEmptyInterfaceCollectionTop(NotEmptyInterfaceCollectionOwner mid) {}

        record RequiredObjectOwner(@NotNull Object anything) {}

        record RequiredObjectTop(RequiredObjectOwner mid) {}

        record ContainerOwner(
                @NotEmpty List<Leaf> list,
                @NotEmpty Set<Leaf> set,
                @NotEmpty Map<String, Leaf> byName,
                Map<Leaf, String> byLeaf,
                List<@NotNull Leaf> requiredElements,
                Set<@NotNull Leaf> requiredSetElements,
                Map<String, @NotNull Leaf> requiredValues,
                Map<@NotNull Leaf, String> requiredKeys,
                Optional<@NotNull Leaf> requiredValue,
                @NotNull Optional<Leaf> optional,
                @NotNull Leaf[] array) {}

        record ContainerTop(ContainerOwner mid) {}

        record NotEmptyLeafKeysOwner(@NotEmpty Map<Leaf, String> byLeaf) {}

        record NotEmptyLeafKeysTop(NotEmptyLeafKeysOwner mid) {}

        private static GeneratorConfig limitedTo(int maxDepth) {
            return GeneratorConfig.builder()
                    .objectMaxDepth(maxDepth)
                    .collectionSize(2, 2)
                    .build();
        }

        private static GenerationFailureContext assertDepthFailure(
                Class<?> root, GeneratorConfig config, String path, Class<?> owner, int limit) {
            ObjectGenerationException error =
                    assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(root, config).generate());
            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(path, context.path());
            assertEquals(owner, context.ownerType());
            assertEquals(limit, context.depth());
            BeanValidationSupport.ConstraintConflictException cause =
                    assertInstanceOf(BeanValidationSupport.ConstraintConflictException.class, error.getCause());
            assertEquals(
                    "required member would be null at objectMaxDepth " + limit
                            + "; raise objectMaxDepth or override the member",
                    cause.getMessage());
            return context;
        }

        private static void assertUnsupportedFailure(Class<?> root, String path, String declaredType) {
            ObjectGenerationException error = assertThrows(
                    ObjectGenerationException.class, () -> new ObjectGenerator<>(root, limitedTo(1)).generate());
            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(path, context.path());
            assertEquals(declaredType, context.declaredType());
            assertInstanceOf(UnsupportedOperationException.class, error.getCause());
        }

        @Test
        @DisplayName("a @NotNull record component fails with its path, instead of being left null")
        void requiredRecordComponentFails() {
            GenerationFailureContext context =
                    assertDepthFailure(Top.class, limitedTo(1), "Top.mid.leaf", Mid.class, 1);

            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals(Leaf.class.getTypeName(), context.declaredType());
            assertEquals(-1, context.recordIndex());
        }

        @Test
        @DisplayName("the failure names the whole path and the limit when the limit is deeper")
        void deeperLimitNamesTheWholePath() {
            assertDepthFailure(Outermost.class, limitedTo(2), "Outermost.top.mid.leaf", Mid.class, 2);
        }

        @Test
        @DisplayName("a larger objectMaxDepth generates the member")
        void largerLimitGeneratesTheMember() {
            Top top = new ObjectGenerator<>(Top.class, limitedTo(2)).generate();

            assertNotNull(top.mid().leaf().value());
        }

        @Test
        @DisplayName("a @NotNull class field fails")
        void requiredFieldFails() {
            assertDepthFailure(FieldTop.class, limitedTo(1), "FieldTop.mid.leaf", FieldOwner.class, 1);
        }

        @Test
        @DisplayName("an inherited @NotNull field fails at the member's path, not its declaring class")
        void requiredInheritedFieldFailsAtMemberPath() {
            assertDepthFailure(InheritingTop.class, limitedTo(1), "InheritingTop.mid.leaf", FieldOwner.class, 1);
        }

        @Test
        @DisplayName("a @NotNull member of a list element fails at the element's path")
        void requiredMemberOfListElementFailsAtElementPath() {
            assertDepthFailure(ElementTop.class, limitedTo(1), "ElementTop.mids[].leaf", Mid.class, 1);
        }

        @Test
        @DisplayName("a field whose getter is @NotNull fails")
        void requiredGetterFails() {
            assertDepthFailure(GetterTop.class, limitedTo(1), "GetterTop.mid.leaf", GetterOwner.class, 1);
        }

        @Test
        @DisplayName("a field whose interface accessor is @NotNull fails")
        void requiredInterfaceAccessorFails() {
            assertDepthFailure(InterfaceTop.class, limitedTo(1), "InterfaceTop.mid.leaf", InterfaceOwner.class, 1);
        }

        @Test
        @DisplayName("a record component whose accessor is @NotNull fails")
        void requiredRecordAccessorFails() {
            assertDepthFailure(AccessorTop.class, limitedTo(1), "AccessorTop.mid.leaf", AccessorOwner.class, 1);
        }

        @Test
        @DisplayName("a @NotNull constructor parameter fails")
        void requiredConstructorParameterFails() {
            assertDepthFailure(ParameterTop.class, limitedTo(1), "ParameterTop.mid.leaf", ParameterOwner.class, 1);
        }

        @Test
        @DisplayName("a @NotNull field fails after an unconstrained constructor parameter left it null")
        void requiredFieldAssignedFromPlainParameterFails() {
            assertDepthFailure(AssignedTop.class, limitedTo(1), "AssignedTop.mid.leaf", AssignedOwner.class, 1);
        }

        @Test
        @DisplayName("@NotEmpty on a collection class reaches the limit and fails")
        void notEmptyCollectionClassFails() {
            assertDepthFailure(RequiredBagTop.class, limitedTo(1), "RequiredBagTop.mid.bag", RequiredBagOwner.class, 1);
        }

        @Test
        @DisplayName("a field that already has a value is not generated, so it keeps its initializer")
        void initializedRequiredFieldKeepsItsValue() {
            InitializedTop top = new ObjectGenerator<>(InitializedTop.class, limitedTo(1)).generate();

            assertEquals("preset", top.mid().leaf.value());
        }

        @Test
        @DisplayName("members with no required constraint stay null")
        void nullableMembersStayNull() {
            NullableTop top = new ObjectGenerator<>(NullableTop.class, limitedTo(1)).generate();

            assertNull(top.mid().leaf());
            assertNull(top.mid().values());
            assertNull(top.mid().bag());
            assertNull(top.mid().shape());
            assertNull(top.mid().anything());
        }

        @Test
        @DisplayName("@Size alone stays null, because Bean Validation accepts null for it")
        void sizedMembersStayNull() {
            SizedTop top = new ObjectGenerator<>(SizedTop.class, limitedTo(1)).generate();

            assertNull(top.mid().values());
            assertNull(top.mid().bag());
        }

        @Test
        @DisplayName("a required member that no depth could generate fails as an unsupported type")
        void requiredUnsupportedMembersFailAsUnsupported() {
            assertUnsupportedFailure(RequiredShapeTop.class, "RequiredShapeTop.mid.shape", Shape.class.getTypeName());
            assertUnsupportedFailure(
                    RequiredInterfaceCollectionTop.class,
                    "RequiredInterfaceCollectionTop.mid.values",
                    "java.util.Collection<java.lang.String>");
            assertUnsupportedFailure(
                    NotEmptyInterfaceCollectionTop.class,
                    "NotEmptyInterfaceCollectionTop.mid.values",
                    "java.util.Collection<java.lang.String>");
            assertUnsupportedFailure(RequiredObjectTop.class, "RequiredObjectTop.mid.anything", "java.lang.Object");
        }

        @Test
        @DisplayName("a subtype mapping makes a required interface member a depth failure")
        void mappedRequiredInterfaceFailsOnDepth() {
            GeneratorConfig mapped = limitedTo(1).toBuilder()
                    .objectSubtype(Shape.class, Circle.class)
                    .build();

            assertDepthFailure(
                    RequiredShapeTop.class, mapped, "RequiredShapeTop.mid.shape", RequiredShapeOwner.class, 1);
        }

        @Test
        @DisplayName("containers are not cut off, and a constraint on a type argument leaves their contents alone")
        void containersKeepTruncatedContents() {
            ContainerOwner owner = new ObjectGenerator<>(ContainerTop.class, limitedTo(1))
                    .generate()
                    .mid();

            assertFalse(owner.list().isEmpty());
            assertTrue(owner.list().stream().allMatch(Objects::isNull));
            assertEquals(1, owner.set().size());
            assertTrue(owner.set().contains(null));
            assertFalse(owner.byName().isEmpty());
            assertTrue(owner.byName().values().stream().allMatch(Objects::isNull));
            assertTrue(owner.byLeaf().isEmpty());
            assertEquals(Arrays.asList(null, null), owner.requiredElements());
            assertEquals(1, owner.requiredSetElements().size());
            assertTrue(owner.requiredSetElements().contains(null));
            assertEquals(2, owner.requiredValues().size());
            assertTrue(owner.requiredValues().values().stream().allMatch(Objects::isNull));
            assertTrue(owner.requiredKeys().isEmpty());
            assertEquals(Optional.empty(), owner.requiredValue());
            assertEquals(Optional.empty(), owner.optional());
            assertEquals(2, owner.array().length);
            assertNull(owner.array()[0]);
        }

        @Test
        @DisplayName("@NotEmpty on a map with nested keys still fails on its size")
        void notEmptyMapWithNestedKeysFailsOnSize() {
            ObjectGenerationException error = assertThrows(
                    ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(NotEmptyLeafKeysTop.class, limitedTo(1)).generate());

            assertEquals(
                    "NotEmptyLeafKeysTop.mid.byLeaf",
                    error.getContext().orElseThrow().path());
            assertInstanceOf(BeanValidationSupport.ConstraintConflictException.class, error.getCause());
            assertTrue(error.getCause().getMessage().contains("declared minimum size of 1"));
        }

        @Test
        @DisplayName("objectIgnoreErrors keeps the null and reports the failure to the listener")
        void lenientModeKeepsNullAndNotifiesListener() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            GeneratorConfig lenient = limitedTo(1).toBuilder()
                    .objectIgnoreErrors(true)
                    .generationFailureListener(diagnostics::add)
                    .build();

            Top top = new ObjectGenerator<>(Top.class, lenient).generate();

            assertNull(top.mid().leaf());
            assertEquals(1, diagnostics.size());
            GenerationFailureDiagnostic diagnostic = diagnostics.getFirst();
            assertEquals("Top.mid.leaf", diagnostic.context().path());
            assertEquals(
                    GenerationFailureCategory.UNSUPPORTED_TYPE,
                    diagnostic.context().category());
            assertEquals(Mid.class, diagnostic.context().ownerType());
            assertEquals(1, diagnostic.context().depth());
            assertEquals(BeanValidationSupport.ConstraintConflictException.class.getName(), diagnostic.causeType());
        }

        @Test
        @DisplayName("objectIgnoreErrors also keeps the null for a required member no depth could generate")
        void lenientModeKeepsNullForRequiredUnsupportedMember() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            GeneratorConfig lenient = limitedTo(1).toBuilder()
                    .objectIgnoreErrors(true)
                    .generationFailureListener(diagnostics::add)
                    .build();

            RequiredShapeTop top = new ObjectGenerator<>(RequiredShapeTop.class, lenient).generate();

            assertNull(top.mid().shape());
            assertEquals(1, diagnostics.size());
            assertEquals(
                    "RequiredShapeOwner.shape", diagnostics.getFirst().context().path());
            assertEquals(
                    UnsupportedOperationException.class.getName(),
                    diagnostics.getFirst().causeType());
        }

        @Test
        @DisplayName("strict mode reports the failure to the listener and throws")
        void strictModeNotifiesListener() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            GeneratorConfig strict = limitedTo(1).toBuilder()
                    .generationFailureListener(diagnostics::add)
                    .build();

            assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(Top.class, strict).generate());

            assertEquals(
                    List.of("Top.mid.leaf", "Top.mid.leaf"),
                    diagnostics.stream().map(d -> d.context().path()).toList());
        }

        @Test
        @DisplayName("field and type overrides supply the member before the limit applies")
        void overridesRunBeforeTheLimit() {
            GeneratorConfig fieldOverride = limitedTo(1).toBuilder()
                    .objectOverride(Mid.class, "leaf", () -> new Leaf("field"))
                    .build();
            GeneratorConfig typeOverride = limitedTo(1).toBuilder()
                    .objectOverride(Leaf.class, () -> new Leaf("type"))
                    .build();

            assertEquals(
                    "field",
                    new ObjectGenerator<>(Top.class, fieldOverride)
                            .generate()
                            .mid()
                            .leaf()
                            .value());
            assertEquals(
                    "type",
                    new ObjectGenerator<>(Top.class, typeOverride)
                            .generate()
                            .mid()
                            .leaf()
                            .value());
        }

        @Test
        @DisplayName("an excluded member stays null, because it is never generated")
        void excludedMemberStaysNull() {
            GeneratorConfig excluded =
                    limitedTo(1).toBuilder().objectExcludeField("leaf").build();

            assertNull(
                    new ObjectGenerator<>(Top.class, excluded).generate().mid().leaf());
        }
    }

    @Nested
    @DisplayName("unique fields")
    class UniqueFieldTest {

        enum Tier {
            BRONZE,
            SILVER,
            GOLD
        }

        record BoundedId(@Min(1) @Max(3) int id) {}

        record OneId(@Min(1) @Max(1) int id) {}

        record Flag(boolean flag) {}

        record Tiered(Tier tier) {}

        record Toggle(boolean active) {}

        record Code(String code) {}

        record Contact(String firstName, String email) {}

        record FakeRanged(@FakeRange(min = 1, max = 4) int code) {}

        record FakeFlag(@Fake("active") boolean code) {}

        record Randomized(@Randomizer(ThreeValues.class) String code) {}

        record ConstantlyRandomized(
                @Randomizer(ConstantValue.class) String code) {}

        record SeedablyRandomized(
                @Randomizer(SeedablePick.class) int code) {}

        record FailinglyRandomized(
                @Randomizer(FailingValue.class) String code) {}

        record AccountRef(@Min(1) @Max(2) int accountId) {}

        record BooleanRef(boolean accountId) {}

        record TierRef(Tier accountId) {}

        record TwoIds(
                @Min(1) @Max(2) int id, @Min(1) @Max(2) int accountId) {}

        record OrderRef(long orderId, long customerId) {}

        record Buyer(long id) {}

        record Purchase(long id, Buyer buyer) {}

        record SiblingIds(LeftId left, RightId right) {}

        record Inner(@Min(1) @Max(1) int id) {}

        record Outer(Inner mid) {}

        record InheritedIdHolder(DerivedId mid) {}

        record Mixed(@Min(1) @Max(5) int id, int count, String name) {}

        record Impossible(
                @Size(min = 3, max = 3) @Pattern(regexp = "[a-z]{5}") String id) {}

        record NullOnly(@Null String id) {}

        record Ids(@Size(min = 2, max = 2) List<@Min(1) @Max(3) Integer> id) {}

        static class MutableId {
            @Min(1) @Max(3) int id;
        }

        static class BaseId {
            @Min(1) @Max(1) int id;
        }

        static class DerivedId extends BaseId {}

        static class ConstructorId {
            final int id;

            ConstructorId(@Min(1) @Max(2) int id) {
                this.id = id;
            }
        }

        static class MutableContact {
            String email;

            MutableContact(String email) {
                this.email = email;
            }
        }

        static class DefaultValuedId {
            int id;

            DefaultValuedId(int id) {
                this.id = id;
            }
        }

        static class DroppedArgument {
            String email;

            DroppedArgument(String email) {
                Objects.requireNonNull(email);
            }
        }

        static class StringCode {
            String code;
        }

        static class IntCodeChild extends StringCode {
            IntCodeChild(int code) {
                Objects.checkIndex(code, 2);
            }
        }

        static class BooleanConstructorId {
            boolean id;

            BooleanConstructorId(boolean id) {
                this.id = id;
            }
        }

        static class ReplacedId {
            @Min(1) @Max(2) int id;

            ReplacedId(@Min(1) @Max(2) int id) {
                this.id = id;
            }
        }

        static class OptionalConstructorId {
            Optional<String> id;

            OptionalConstructorId(Optional<String> id) {
                this.id = id;
            }
        }

        static class ShadowedId {
            int id;
        }

        static class ShadowingId extends ShadowedId {
            int id;

            ShadowingId(int id) {
                Objects.checkIndex(id, 2);
            }
        }

        record CaseVariants(int id, int ID) {}

        static class SharedId {
            @Min(1) @Max(2) int id;
        }

        static class LeftId extends SharedId {}

        static class RightId extends SharedId {}

        public static class ThreeValues implements Generator<String> {
            private int next;

            @Override
            public String generate() {
                return List.of("a", "b", "c").get(next++ % 3);
            }
        }

        public static class ConstantValue implements Generator<String> {
            @Override
            public String generate() {
                return "fixed";
            }
        }

        public static class SeedablePick implements Generator<Integer>, Seedable {
            private Random random = new Random(0L);

            @Override
            public Integer generate() {
                return random.nextInt(3);
            }

            @Override
            public void reseed(long seed) {
                random = new Random(seed);
            }
        }

        public static class FailingValue implements Generator<String> {
            @Override
            public String generate() {
                throw new IllegalStateException("boom");
            }
        }

        private GeneratorConfig.Builder unique(String fieldName) {
            return GeneratorConfig.builder().seed(42L).objectUniqueField(fieldName);
        }

        /** Draws the domain's size, which must give every value once, then one more, which must fail. */
        private <T> void assertDistinctThenFails(
                Class<T> type, GeneratorConfig config, Function<T, Object> field, Set<?> domain, String path) {
            ObjectGenerator<T> generator = new ObjectGenerator<>(type, config);
            Set<Object> values = new HashSet<>();
            for (int i = 0; i < domain.size(); i++) {
                values.add(field.apply(generator.generate()));
            }
            assertEquals(domain, values);
            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
            assertEquals(path, error.getContext().orElseThrow().path());
            assertTrue(error.getMessage().contains("unique value at '" + path + "'"), error.getMessage());
        }

        @Test
        @DisplayName("a boolean has two values")
        void booleanHasTwoValues() {
            assertDistinctThenFails(Flag.class, unique("flag").build(), Flag::flag, Set.of(true, false), "Flag.flag");
        }

        @Test
        @DisplayName("an enum has its constants")
        void enumHasItsConstants() {
            assertDistinctThenFails(
                    Tiered.class, unique("tier").build(), Tiered::tier, Set.of(Tier.values()), "Tiered.tier");
        }

        @Test
        @DisplayName("a Bean Validation range has its values")
        void beanValidationRangeHasItsValues() {
            assertDistinctThenFails(
                    BoundedId.class, unique("id").build(), BoundedId::id, Set.of(1, 2, 3), "BoundedId.id");
            assertDistinctThenFails(OneId.class, unique("id").build(), OneId::id, Set.of(1), "OneId.id");
        }

        @Test
        @DisplayName("a semantic field has its values")
        void semanticFieldHasItsValues() {
            assertDistinctThenFails(
                    Toggle.class, unique("active").build(), Toggle::active, Set.of(true, false), "Toggle.active");
        }

        @Test
        @DisplayName("a mutable field and a populated instance share one uniqueness scope")
        void mutableFieldsAndPopulationAreTracked() {
            assertDistinctThenFails(
                    MutableId.class, unique("id").build(), value -> value.id, Set.of(1, 2, 3), "MutableId.id");

            ObjectGenerator<MutableId> generator =
                    new ObjectGenerator<>(MutableId.class, unique("id").build());
            Set<Integer> ids = new HashSet<>();
            ids.add(generator.generate().id);
            ids.add(generator.populate(new MutableId()).id);
            ids.add(generator.populate(new MutableId()).id);
            assertEquals(Set.of(1, 2, 3), ids);
            assertThrows(ObjectGenerationException.class, () -> generator.populate(new MutableId()));
        }

        @Test
        @DisplayName("a constructor argument is tracked")
        void constructorArgumentIsTracked() {
            assertDistinctThenFails(
                    ConstructorId.class, unique("id").build(), value -> value.id, Set.of(1, 2), "ConstructorId.id");
        }

        @Test
        @DisplayName("a constant override is no repeat of its own constructor argument, whatever the mode")
        void constantOverrideIsNoRepeatOfItsOwnConstructorArgument() {
            for (boolean overrideDefaultInitialization : List.of(false, true)) {
                List<GeneratorConfig.Builder> overrides = List.of(
                        GeneratorConfig.builder()
                                .objectOverride(MutableContact.class, "email", () -> "owner@example.test"),
                        GeneratorConfig.builder().objectOverride(String.class, () -> "owner@example.test"));

                for (GeneratorConfig.Builder override : overrides) {
                    assertDistinctThenFails(
                            MutableContact.class,
                            override.objectOverrideDefaultInitialization(overrideDefaultInitialization)
                                    .build(),
                            value -> value.email,
                            Set.of("owner@example.test"),
                            "MutableContact.email");
                }
            }
        }

        @Test
        @DisplayName("a constructor override that returns the default is no repeat of itself")
        void constructorOverrideReturningTheDefaultIsNoRepeatOfItself() {
            GeneratorConfig config = unique("id")
                    .objectOverride(DefaultValuedId.class, "id", () -> 0)
                    .build();

            assertDistinctThenFails(DefaultValuedId.class, config, value -> value.id, Set.of(0), "DefaultValuedId.id");
        }

        @Test
        @DisplayName("an override the constructor drops still reaches the field")
        void overrideDroppedByTheConstructorReachesTheField() {
            GeneratorConfig config = GeneratorConfig.builder()
                    .objectOverride(DroppedArgument.class, "email", () -> "owner@example.test")
                    .build();

            assertDistinctThenFails(
                    DroppedArgument.class,
                    config,
                    value -> value.email,
                    Set.of("owner@example.test"),
                    "DroppedArgument.email");
        }

        @Test
        @DisplayName("a higher-priority override still wins for the field of a constructor argument")
        void higherPriorityOverrideWinsForTheFieldOfAConstructorArgument() {
            GeneratorConfig config = GeneratorConfig.builder()
                    .objectOverride(DroppedArgument.class, "email", () -> "named@example.test")
                    .objectOverride(FieldPredicates.named("email"), context -> "predicate@example.test")
                    .build();

            assertEquals(
                    "predicate@example.test", new ObjectGenerator<>(DroppedArgument.class, config).generate().email);
        }

        @Test
        @DisplayName("a constructor override does not reach a same-named field it does not target")
        void constructorOverrideDoesNotReachAnUntargetedField() {
            GeneratorConfig config = GeneratorConfig.builder()
                    .objectOverride(IntCodeChild.class, "code", () -> 1)
                    .build();

            assertNotNull(new ObjectGenerator<>(IntCodeChild.class, config).generate().code);
        }

        @Test
        @DisplayName("a generated constructor argument its field replaces gives its value back")
        void generatedConstructorArgumentGivesItsValueBack() {
            assertDistinctThenFails(
                    BooleanConstructorId.class,
                    unique("id").build(),
                    value -> value.id,
                    Set.of(true, false),
                    "BooleanConstructorId.id");
            assertDistinctThenFails(
                    ReplacedId.class,
                    unique("id").objectOverrideDefaultInitialization(true).build(),
                    value -> value.id,
                    Set.of(1, 2),
                    "ReplacedId.id");
        }

        @Test
        @DisplayName("a constructor argument that was never tracked gives nothing back")
        void untrackedConstructorArgumentGivesNothingBack() {
            GeneratorConfig config = GeneratorConfig.builder()
                    .objectOverride(String.class, () -> "fixed")
                    .objectOverride(FieldPredicates.named("id"), () -> Optional.of("fixed"))
                    .build();

            assertDistinctThenFails(
                    OptionalConstructorId.class,
                    config,
                    value -> value.id,
                    Set.of(Optional.of("fixed")),
                    "OptionalConstructorId.id");
        }

        @Test
        @DisplayName("a lenient constructor argument's fallback gives nothing back")
        void lenientConstructorArgumentFallbackGivesNothingBack() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            ObjectGenerator<BooleanConstructorId> generator = new ObjectGenerator<>(
                    BooleanConstructorId.class,
                    unique("id")
                            .objectIgnoreErrors(true)
                            .generationFailureListener(diagnostics::add)
                            .build());
            generator.generateList(2);
            assertTrue(diagnostics.isEmpty());

            assertFalse(generator.generate().id);

            // The argument fails, then so does the field: the fallback did not free the earlier false.
            assertEquals(2, diagnostics.size());
        }

        @Test
        @DisplayName("a constructor argument is given back once, by the field of the same type and name")
        void constructorArgumentIsGivenBackOnlyByItsOwnField() {
            AtomicInteger inherited = new AtomicInteger();
            GeneratorConfig config = GeneratorConfig.builder()
                    .objectOverride(ShadowingId.class, "id", () -> 1)
                    .objectOverride(ShadowedId.class, "id", inherited::incrementAndGet)
                    .build();

            assertDistinctThenFails(ShadowingId.class, config, value -> value.id, Set.of(1), "ShadowingId.id");
        }

        @Test
        @DisplayName("names that differ only by case are different fields")
        void namesDifferingByCaseAreDifferentFields() {
            ObjectGenerator<CaseVariants> generator = new ObjectGenerator<>(
                    CaseVariants.class,
                    GeneratorConfig.builder()
                            .objectOverride(CaseVariants.class, "id", () -> 1)
                            .objectOverride(CaseVariants.class, "ID", () -> 1)
                            .build());

            assertEquals(new CaseVariants(1, 1), generator.generate());
            assertEquals(
                    "CaseVariants.id",
                    assertThrows(ObjectGenerationException.class, generator::generate)
                            .getContext()
                            .orElseThrow()
                            .path());
        }

        @Test
        @DisplayName("@FakeRange has the values of its range")
        void fakeRangeHasItsValues() {
            assertDistinctThenFails(
                    FakeRanged.class, unique("code").build(), FakeRanged::code, Set.of(1, 2, 3), "FakeRanged.code");
        }

        @Test
        @DisplayName("@Fake is tracked under the field's own name")
        void fakeIsTracked() {
            assertDistinctThenFails(
                    FakeFlag.class, unique("code").build(), FakeFlag::code, Set.of(true, false), "FakeFlag.code");
        }

        @Test
        @DisplayName("@Randomizer is drawn again on a repeat, and a constant one fails on the next object")
        void randomizerIsDrawnAgainOnARepeat() {
            assertDistinctThenFails(
                    Randomized.class,
                    unique("code").build(),
                    Randomized::code,
                    Set.of("a", "b", "c"),
                    "Randomized.code");
            assertDistinctThenFails(
                    ConstantlyRandomized.class,
                    unique("code").build(),
                    ConstantlyRandomized::code,
                    Set.of("fixed"),
                    "ConstantlyRandomized.code");
        }

        @Test
        @DisplayName("a seedable @Randomizer keeps drawing when it is retried")
        void seedableRandomizerKeepsDrawingWhenRetried() {
            assertDistinctThenFails(
                    SeedablyRandomized.class,
                    unique("code").build(),
                    SeedablyRandomized::code,
                    Set.of(0, 1, 2),
                    "SeedablyRandomized.code");
        }

        @Test
        @DisplayName("every kind of override fails on the next object when it returns a constant")
        void everyOverrideKindFailsOnARepeatedConstant() {
            List<GeneratorConfig.Builder> overrides = List.of(
                    unique("code").objectOverride(Code.class, "code", () -> "fixed"),
                    unique("code").objectOverride(Code.class, "code", context -> "fixed"),
                    unique("code").objectOverride(FieldPredicates.named("code"), () -> "fixed"),
                    unique("code").objectOverride(FieldPredicates.named("code"), context -> "fixed"),
                    unique("code").objectOverride(String.class, () -> "fixed"),
                    unique("code").objectOverride(String.class, context -> "fixed"));

            for (GeneratorConfig.Builder override : overrides) {
                assertDistinctThenFails(Code.class, override.build(), Code::code, Set.of("fixed"), "Code.code");
            }
        }

        @Test
        @DisplayName("an override is drawn again on a repeat")
        void overrideIsDrawnAgainOnARepeat() {
            Iterator<String> draws = List.of("a", "a", "b", "a", "b", "c").iterator();
            ObjectGenerator<Code> generator = new ObjectGenerator<>(
                    Code.class,
                    unique("code")
                            .objectOverride(Code.class, "code", () -> draws.next())
                            .build());

            assertEquals(
                    List.of("a", "b", "c"),
                    generator.generateList(3).stream().map(Code::code).toList());
        }

        @Test
        @DisplayName("a null is no repeat")
        void nullIsNoRepeat() {
            ObjectGenerator<NullOnly> constrained =
                    new ObjectGenerator<>(NullOnly.class, unique("id").build());
            ObjectGenerator<Code> overridden = new ObjectGenerator<>(
                    Code.class,
                    unique("code")
                            .objectOverride(Code.class, "code", () -> null)
                            .build());

            for (int i = 0; i < 3; i++) {
                assertNull(constrained.generate().id());
                assertNull(overridden.generate().code());
            }
        }

        @Test
        @DisplayName("a constant override on a name that is unique by default fails unless the name is left out")
        void constantOverrideOnDefaultUniqueName() {
            GeneratorConfig.Builder pinned =
                    GeneratorConfig.builder().objectOverride(Contact.class, "email", () -> "owner@example.test");

            assertDistinctThenFails(
                    Contact.class, pinned.build(), Contact::email, Set.of("owner@example.test"), "Contact.email");

            // A new generator is a new scope.
            assertEquals(
                    "owner@example.test",
                    new ObjectGenerator<>(Contact.class, pinned.build())
                            .generate()
                            .email());

            List<Contact> repeated = new ObjectGenerator<>(
                            Contact.class, pinned.objectUniqueFields().build())
                    .generateList(3);
            assertEquals(
                    List.of("owner@example.test", "owner@example.test", "owner@example.test"),
                    repeated.stream().map(Contact::email).toList());
        }

        @Test
        @DisplayName("a semantic alias of a unique name is unique on every route")
        void semanticAliasIsUniqueOnEveryRoute() {
            GeneratorConfig defaults = GeneratorConfig.builder().seed(42L).build();

            assertDistinctThenFails(
                    AccountRef.class, defaults, AccountRef::accountId, Set.of(1, 2), "AccountRef.accountId");
            assertDistinctThenFails(
                    BooleanRef.class, defaults, BooleanRef::accountId, Set.of(true, false), "BooleanRef.accountId");
            assertDistinctThenFails(
                    TierRef.class, defaults, TierRef::accountId, Set.of(Tier.values()), "TierRef.accountId");
        }

        @Test
        @DisplayName("each field has its own values, whether a semantic alias or the same name in a nested type")
        void eachFieldHasItsOwnValues() {
            ObjectGenerator<TwoIds> ranges = new ObjectGenerator<>(
                    TwoIds.class, GeneratorConfig.builder().seed(42L).build());
            List<TwoIds> both = ranges.generateList(2);
            assertEquals(Set.of(1, 2), both.stream().map(TwoIds::id).collect(Collectors.toSet()));
            assertEquals(Set.of(1, 2), both.stream().map(TwoIds::accountId).collect(Collectors.toSet()));
            assertThrows(ObjectGenerationException.class, ranges::generate);

            ObjectGenerator<OrderRef> pinnedAliases = new ObjectGenerator<>(
                    OrderRef.class,
                    GeneratorConfig.builder()
                            .objectOverride(OrderRef.class, "orderId", () -> 1L)
                            .objectOverride(OrderRef.class, "customerId", () -> 1L)
                            .build());
            assertEquals(new OrderRef(1L, 1L), pinnedAliases.generate());
            assertEquals(
                    "OrderRef.orderId",
                    assertThrows(ObjectGenerationException.class, pinnedAliases::generate)
                            .getContext()
                            .orElseThrow()
                            .path());

            ObjectGenerator<Purchase> pinnedNested = new ObjectGenerator<>(
                    Purchase.class,
                    GeneratorConfig.builder()
                            .objectOverride(Purchase.class, "id", () -> 1L)
                            .objectOverride(Buyer.class, "id", () -> 1L)
                            .build());
            assertEquals(new Purchase(1L, new Buyer(1L)), pinnedNested.generate());
            assertEquals(
                    "Purchase.id",
                    assertThrows(ObjectGenerationException.class, pinnedNested::generate)
                            .getContext()
                            .orElseThrow()
                            .path());
        }

        @Test
        @DisplayName("an inherited field is one field, however many subclasses reach it")
        void inheritedFieldIsOneField() {
            ObjectGenerator<SiblingIds> generator =
                    new ObjectGenerator<>(SiblingIds.class, unique("id").build());

            SiblingIds first = generator.generate();
            assertNotEquals(first.left().id, first.right().id);
            assertEquals(
                    "SiblingIds.left.id",
                    assertThrows(ObjectGenerationException.class, generator::generate)
                            .getContext()
                            .orElseThrow()
                            .path());
        }

        @Test
        @DisplayName("structural-only generation has no aliases, so only the field's own name counts")
        void structuralOnlyGenerationHasNoAliases() {
            GeneratorConfig structural = GeneratorConfig.builder()
                    .seed(42L)
                    .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                    .build();

            Set<Integer> accountIds = new HashSet<>();
            ObjectGenerator<AccountRef> refs = new ObjectGenerator<>(AccountRef.class, structural);
            for (int i = 0; i < SAMPLES; i++) {
                accountIds.add(refs.generate().accountId());
            }
            assertEquals(Set.of(1, 2), accountIds);

            assertDistinctThenFails(BoundedId.class, structural, BoundedId::id, Set.of(1, 2, 3), "BoundedId.id");
        }

        @Test
        @DisplayName("the elements of a collection are tracked under the field's name")
        void collectionElementsAreTracked() {
            ObjectGenerator<Ids> generator =
                    new ObjectGenerator<>(Ids.class, unique("id").build());

            List<Integer> first = generator.generate().id();
            assertEquals(2, new HashSet<>(first).size());
            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
            assertEquals("Ids.id[]", error.getContext().orElseThrow().path());
        }

        @Test
        @DisplayName("an exhausted domain fails with the path and the reason, and tells the listener")
        void exhaustedDomainFailsWithPathAndReason() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            ObjectGenerator<OneId> generator = new ObjectGenerator<>(
                    OneId.class,
                    unique("id").generationFailureListener(diagnostics::add).build());
            generator.generate();
            assertTrue(diagnostics.isEmpty());

            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);

            GenerationFailureContext context = error.getContext().orElseThrow();
            assertEquals("OneId.id", context.path());
            assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
            assertEquals(GenerationOperation.GENERATE, context.operation());
            assertEquals(OneId.class, context.ownerType());
            assertEquals("int", context.declaredType());
            assertEquals(0, context.depth());
            assertEquals(
                    "Could not generate a unique value at 'OneId.id' (declared type int, depth 0)", error.getMessage());
            assertTrue(error.getCause().getMessage().contains("OneId.id' after 256 attempts"));
            assertTrue(error.getCause().getMessage().contains("objectUniquenessMaxAttempts"));
            assertTrue(error.getCause().getMessage().contains("objectUniqueFields"));
            assertEquals(1, diagnostics.size());
            assertEquals(context, diagnostics.getFirst().context());
            assertEquals(
                    error.getCause().getClass().getName(),
                    diagnostics.getFirst().causeType());
            assertTrue(diagnostics.getFirst().causeType().endsWith("UniqueFieldTracker$ExhaustedException"));
        }

        @Test
        @DisplayName("objectUniquenessMaxAttempts is the number of draws for one value")
        void maxAttemptsIsTheNumberOfDrawsForOneValue() {
            AtomicInteger draws = new AtomicInteger();
            ObjectGenerator<Code> generator = new ObjectGenerator<>(
                    Code.class,
                    unique("code")
                            .objectUniquenessMaxAttempts(5)
                            .objectOverride(Code.class, "code", () -> {
                                draws.incrementAndGet();
                                return "fixed";
                            })
                            .build());
            generator.generate();
            assertEquals(1, draws.get());

            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);

            assertEquals(6, draws.get());
            assertTrue(error.getCause().getMessage().contains("after 5 attempts"));
        }

        @Test
        @DisplayName("a nested failure keeps the whole path and the reason")
        void nestedFailureKeepsPathAndReason() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            ObjectGenerator<Outer> generator = new ObjectGenerator<>(
                    Outer.class,
                    unique("id").generationFailureListener(diagnostics::add).build());
            generator.generate();

            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);

            assertEquals("Outer.mid.id", error.getContext().orElseThrow().path());
            assertTrue(error.getCause().getMessage().contains("Inner.id' after 256 attempts"));
            assertEquals(
                    List.of("Outer.mid.id", "Outer.mid.id"),
                    diagnostics.stream().map(d -> d.context().path()).toList());
        }

        @Test
        @DisplayName("an inherited field's failure is named by the path the object was reached on")
        void inheritedFieldFailureUsesTheMemberPath() {
            ObjectGenerator<InheritedIdHolder> generator =
                    new ObjectGenerator<>(InheritedIdHolder.class, unique("id").build());
            generator.generate();

            GenerationFailureContext context = assertThrows(ObjectGenerationException.class, generator::generate)
                    .getContext()
                    .orElseThrow();

            assertEquals("InheritedIdHolder.mid.id", context.path());
            assertEquals(BaseId.class, context.ownerType());
        }

        @Test
        @DisplayName("objectIgnoreErrors gives the default for the type and tells the listener each time")
        void lenientModeGivesTheDefaultAndNotifiesListener() {
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            ObjectGenerator<BoundedId> generator = new ObjectGenerator<>(
                    BoundedId.class,
                    unique("id")
                            .objectIgnoreErrors(true)
                            .generationFailureListener(diagnostics::add)
                            .build());

            assertEquals(
                    Set.of(1, 2, 3),
                    generator.generateList(3).stream().map(BoundedId::id).collect(Collectors.toSet()));
            assertTrue(diagnostics.isEmpty());

            assertEquals(0, generator.generate().id());
            assertEquals(0, generator.generate().id());

            assertEquals(
                    List.of("BoundedId.id", "BoundedId.id"),
                    diagnostics.stream().map(d -> d.context().path()).toList());
            assertTrue(diagnostics.getFirst().causeType().endsWith("UniqueFieldTracker$ExhaustedException"));

            ObjectGenerator<Code> overridden = new ObjectGenerator<>(
                    Code.class,
                    unique("code")
                            .objectIgnoreErrors(true)
                            .objectOverride(Code.class, "code", () -> "fixed")
                            .build());
            assertEquals("fixed", overridden.generate().code());
            assertNull(overridden.generate().code());
        }

        @Test
        @DisplayName("the same seed gives the same values, retries included, on every stream policy")
        void sameSeedGivesTheSameValuesAcrossRetries() {
            for (ObjectFieldStreamPolicy policy : ObjectFieldStreamPolicy.values()) {
                GeneratorConfig config =
                        unique("id").objectFieldStreamPolicy(policy).build();
                ObjectGenerator<Mixed> first = new ObjectGenerator<>(Mixed.class, config);
                ObjectGenerator<Mixed> second = new ObjectGenerator<>(Mixed.class, config);

                List<Mixed> values = first.generateList(5);

                assertEquals(values, second.generateList(5), policy.name());
                assertEquals(
                        Set.of(1, 2, 3, 4, 5),
                        values.stream().map(Mixed::id).collect(Collectors.toSet()),
                        policy.name());
                assertThrows(ObjectGenerationException.class, first::generate, policy.name());
            }
        }

        @Test
        @DisplayName("a failing @Randomizer reports its own failure, not exhaustion")
        void failingRandomizerReportsItsOwnFailure() {
            ObjectGenerator<FailinglyRandomized> generator = new ObjectGenerator<>(
                    FailinglyRandomized.class, unique("code").build());

            ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);

            assertEquals(
                    GenerationFailureCategory.CUSTOM_GENERATOR,
                    error.getContext().orElseThrow().category());
            assertInstanceOf(IllegalStateException.class, error.getCause());
        }

        @Test
        @DisplayName("an unsatisfiable constraint stays a constraint failure, once per object")
        void unsatisfiableConstraintStaysAConstraintFailure() {
            ObjectGenerationException error = assertThrows(
                    ObjectGenerationException.class,
                    () -> new ObjectGenerator<>(Impossible.class, unique("id").build()).generate());

            assertTrue(
                    error.getMessage().startsWith("Unsatisfiable constraints at 'Impossible.id'"), error.getMessage());
            assertInstanceOf(BeanValidationSupport.ConstraintConflictException.class, error.getCause());

            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            ObjectGenerator<Impossible> lenient = new ObjectGenerator<>(
                    Impossible.class,
                    unique("id")
                            .objectIgnoreErrors(true)
                            .generationFailureListener(diagnostics::add)
                            .build());
            assertNull(lenient.generate().id());
            assertNull(lenient.generate().id());
            assertEquals(2, diagnostics.size());
        }
    }
}
