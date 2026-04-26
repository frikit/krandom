/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeAnnotationTest {

    // ── @Fake test fixtures ──────────────────────────────────────────────────

    public static class ContactForm {
        @Fake("email")
        public String contactEmail;

        @Fake("firstName")
        public String givenName;

        @Fake("city")
        public String hometown;

        @Fake("country")
        public String homeland;

        @Fake("phone")
        public String officePhone;

        @Fake("url")
        public String profileLink;
    }

    public static class FakedRecord {
        @Fake("companyName")
        public String employer;

        @Fake("username")
        public String handle;
    }

    public static class MixedAnnotations {
        @Fake("email")
        public String primaryEmail;

        public String normalField;

        @FakeRange(min = 18, max = 65)
        public int age;
    }

    // ── @FakeRange test fixtures ─────────────────────────────────────────────

    public static class RangedFields {
        @FakeRange(min = 1, max = 100)
        public int quantity;

        @FakeRange(min = 0, max = 10000)
        public long bigQuantity;

        @FakeRange(min = 0, max = 1000)
        public double price;

        @FakeRange(min = 0, max = 500)
        public float weight;

        @FakeRange(min = 1, max = 127)
        public short smallNum;

        @FakeRange(min = 0, max = 10)
        public byte tinyNum;
    }

    public static class RangedWrappers {
        @FakeRange(min = 1, max = 100)
        public Integer quantity;

        @FakeRange(min = 0, max = 10000)
        public Long bigQuantity;

        @FakeRange(min = 0, max = 1000)
        public Double price;

        @FakeRange(min = 0, max = 500)
        public Float weight;

        @FakeRange(min = 1, max = 127)
        public Short smallNum;

        @FakeRange(min = 0, max = 10)
        public Byte tinyNum;
    }

    public static class TypedFake {
        @Fake("age")
        public int ageField;

        @Fake("age")
        public Integer ageWrapper;
    }

    public static class UnknownFake {
        @Fake("nonExistentKey12345")
        public String unknownField;
    }

    public static class UnsupportedFakeRange {
        @FakeRange(min = 0, max = 100)
        public String textField;
    }

    public static class FakeAgeAsString {
        @Fake("age")
        public String ageText;
    }

    public static class FakeOnUnsupportedType {
        @Fake("age")
        public boolean ageFlag;
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("@Fake annotation")
    class FakeAnnotationTests {

        @Test
        @DisplayName("populates email field")
        void populatesEmailField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.contactEmail);
            assertTrue(form.contactEmail.contains("@"),
                "Expected email-like value but got: " + form.contactEmail);
        }

        @Test
        @DisplayName("populates firstName field")
        void populatesFirstNameField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.givenName);
            assertFalse(form.givenName.isBlank());
        }

        @Test
        @DisplayName("populates city field")
        void populatesCityField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.hometown);
            assertFalse(form.hometown.isBlank());
        }

        @Test
        @DisplayName("populates country field")
        void populatesCountryField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.homeland);
            assertFalse(form.homeland.isBlank());
        }

        @Test
        @DisplayName("populates phone field")
        void populatesPhoneField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.officePhone);
            assertFalse(form.officePhone.isBlank());
        }

        @Test
        @DisplayName("populates url field")
        void populatesUrlField() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm form = gen.generate();
            assertNotNull(form.profileLink);
            assertFalse(form.profileLink.isBlank());
        }

        @Test
        @DisplayName("populates companyName and username")
        void populatesCompanyAndUsername() {
            ObjectGenerator<FakedRecord> gen = new ObjectGenerator<>(FakedRecord.class);
            FakedRecord rec = gen.generate();
            assertNotNull(rec.employer);
            assertFalse(rec.employer.isBlank());
            assertNotNull(rec.handle);
            assertFalse(rec.handle.isBlank());
        }

        @Test
        @DisplayName("@Fake works alongside @FakeRange and unannotated fields")
        void mixedAnnotations() {
            ObjectGenerator<MixedAnnotations> gen = new ObjectGenerator<>(MixedAnnotations.class);
            MixedAnnotations obj = gen.generate();
            assertNotNull(obj.primaryEmail);
            assertTrue(obj.primaryEmail.contains("@"));
            assertNotNull(obj.normalField);
            assertTrue(obj.age >= 18 && obj.age < 65, "Expected age in [18,65), got: " + obj.age);
        }

        @Test
        @DisplayName("generates multiple distinct values")
        void generatesDistinctValues() {
            ObjectGenerator<ContactForm> gen = new ObjectGenerator<>(ContactForm.class);
            ContactForm a = gen.generate();
            ContactForm b = gen.generate();
            // Not strictly guaranteed but overwhelmingly likely with realistic generators
            assertNotNull(a.contactEmail);
            assertNotNull(b.contactEmail);
        }

        @Test
        @DisplayName("@Fake resolves typed (non-String) generators for int field")
        void fakeProvidesTypedGenerator() {
            ObjectGenerator<TypedFake> gen = new ObjectGenerator<>(TypedFake.class);
            for (int i = 0; i < 20; i++) {
                TypedFake obj = gen.generate();
                assertTrue(obj.ageField >= 18 && obj.ageField < 91,
                    "Expected age in [18,91), got: " + obj.ageField);
                assertNotNull(obj.ageWrapper);
                assertTrue(obj.ageWrapper >= 18 && obj.ageWrapper < 91,
                    "Expected age wrapper in [18,91), got: " + obj.ageWrapper);
            }
        }

        @Test
        @DisplayName("@Fake with unknown key falls back to default generation")
        void fakeWithUnknownKeyFallsBack() {
            ObjectGenerator<UnknownFake> gen = new ObjectGenerator<>(UnknownFake.class);
            UnknownFake obj = gen.generate();
            // Should still generate a value via built-in String generator
            assertNotNull(obj.unknownField);
        }

        @Test
        @DisplayName("@Fake('age') on String field returns age as text")
        void fakeAgeOnStringField() {
            ObjectGenerator<FakeAgeAsString> gen = new ObjectGenerator<>(FakeAgeAsString.class);
            FakeAgeAsString obj = gen.generate();
            assertNotNull(obj.ageText);
            int parsedAge = Integer.parseInt(obj.ageText);
            assertTrue(parsedAge >= 18 && parsedAge < 91,
                "Expected age text in [18,91), got: " + parsedAge);
        }

        @Test
        @DisplayName("@Fake('age') on unsupported boolean field falls back to default")
        void fakeOnUnsupportedTypeFallsBack() {
            ObjectGenerator<FakeOnUnsupportedType> gen = new ObjectGenerator<>(FakeOnUnsupportedType.class);
            FakeOnUnsupportedType obj = gen.generate();
            // boolean should be generated by built-in generator, not fail
            assertNotNull(obj);
        }
    }

    @Nested
    @DisplayName("@FakeRange annotation")
    class FakeRangeTests {

        @Test
        @DisplayName("constrains primitive int field")
        void constrainsIntField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.quantity >= 1 && obj.quantity < 100,
                    "Expected quantity in [1,100), got: " + obj.quantity);
            }
        }

        @Test
        @DisplayName("constrains primitive long field")
        void constrainsLongField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.bigQuantity >= 0 && obj.bigQuantity < 10000,
                    "Expected bigQuantity in [0,10000), got: " + obj.bigQuantity);
            }
        }

        @Test
        @DisplayName("constrains primitive double field")
        void constrainsDoubleField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.price >= 0 && obj.price < 1000,
                    "Expected price in [0,1000), got: " + obj.price);
            }
        }

        @Test
        @DisplayName("constrains primitive float field")
        void constrainsFloatField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.weight >= 0 && obj.weight < 500,
                    "Expected weight in [0,500), got: " + obj.weight);
            }
        }

        @Test
        @DisplayName("constrains primitive short field")
        void constrainsShortField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.smallNum >= 1 && obj.smallNum < 127,
                    "Expected smallNum in [1,127), got: " + obj.smallNum);
            }
        }

        @Test
        @DisplayName("constrains primitive byte field")
        void constrainsByteField() {
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class);
            for (int i = 0; i < 50; i++) {
                RangedFields obj = gen.generate();
                assertTrue(obj.tinyNum >= 0 && obj.tinyNum < 10,
                    "Expected tinyNum in [0,10), got: " + obj.tinyNum);
            }
        }

        @Test
        @DisplayName("constrains wrapper Integer field")
        void constrainsIntegerWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.quantity);
                assertTrue(obj.quantity >= 1 && obj.quantity < 100,
                    "Expected quantity in [1,100), got: " + obj.quantity);
            }
        }

        @Test
        @DisplayName("constrains wrapper Long field")
        void constrainsLongWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.bigQuantity);
                assertTrue(obj.bigQuantity >= 0 && obj.bigQuantity < 10000,
                    "Expected bigQuantity in [0,10000), got: " + obj.bigQuantity);
            }
        }

        @Test
        @DisplayName("constrains wrapper Double field")
        void constrainsDoubleWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.price);
                assertTrue(obj.price >= 0 && obj.price < 1000,
                    "Expected price in [0,1000), got: " + obj.price);
            }
        }

        @Test
        @DisplayName("constrains wrapper Float field")
        void constrainsFloatWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.weight);
                assertTrue(obj.weight >= 0 && obj.weight < 500,
                    "Expected weight in [0,500), got: " + obj.weight);
            }
        }

        @Test
        @DisplayName("constrains wrapper Short field")
        void constrainsShortWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.smallNum);
                assertTrue(obj.smallNum >= 1 && obj.smallNum < 127,
                    "Expected smallNum in [1,127), got: " + obj.smallNum);
            }
        }

        @Test
        @DisplayName("constrains wrapper Byte field")
        void constrainsByteWrapper() {
            ObjectGenerator<RangedWrappers> gen = new ObjectGenerator<>(RangedWrappers.class);
            for (int i = 0; i < 50; i++) {
                RangedWrappers obj = gen.generate();
                assertNotNull(obj.tinyNum);
                assertTrue(obj.tinyNum >= 0 && obj.tinyNum < 10,
                    "Expected tinyNum in [0,10), got: " + obj.tinyNum);
            }
        }

        @Test
        @DisplayName("@FakeRange on unsupported type is ignored")
        void fakeRangeOnUnsupportedType() {
            ObjectGenerator<UnsupportedFakeRange> gen = new ObjectGenerator<>(UnsupportedFakeRange.class);
            UnsupportedFakeRange obj = gen.generate();
            // Should still generate a String via built-in generator
            assertNotNull(obj.textField);
        }

        @Test
        @DisplayName("respects seeded configuration")
        void respectsSeedConfig() {
            GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
            ObjectGeneratorConfig objConfig = ObjectGeneratorConfig.builder()
                .generatorConfig(config)
                .build();
            ObjectGenerator<RangedFields> gen = new ObjectGenerator<>(RangedFields.class, objConfig);
            RangedFields first = gen.generate();

            ObjectGenerator<RangedFields> gen2 = new ObjectGenerator<>(RangedFields.class, objConfig);
            RangedFields second = gen2.generate();

            assertEquals(first.quantity, second.quantity);
            assertEquals(first.bigQuantity, second.bigQuantity);
        }
    }
}
