/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("k-random reference parity — exclusions and declarative rules")
class KRandomReferenceExclusionAndAnnotationParityTest {

    @Test
    @DisplayName("FieldPredicates.nameMatches(...) excludes matching fields in nested object graphs")
    void regexFieldPredicateExcludesNestedFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectExclude(FieldPredicates.nameMatches(".*Secret"))
                                                .build();

        SecretRoot root = Generators.ofObject(SecretRoot.class, config).generate();

        assertNull(root.rootSecret);
        assertNotNull(root.visibleValue);
        assertNotNull(root.child);
        assertNull(root.child.childSecret);
        assertNotNull(root.child.childVisibleValue);
    }

    @Test
    @DisplayName("FieldPredicates.inClass(...) excludes inherited fields declared by the base class")
    void declaringClassPredicateExcludesInheritedFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectExclude(FieldPredicates.inClass(BaseSecret.class))
                                                .build();

        ChildSecret child = Generators.ofObject(ChildSecret.class, config).generate();

        assertNull(child.inheritedSecret);
        assertNotNull(child.localValue);
    }

    @Test
    @DisplayName("TypePredicates helpers can drive object type exclusions")
    void typePredicatesDriveTypeExclusions() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectExcludeType(
                                                    TypePredicates.named(LocalDate.class.getName())
                                                                  .or(TypePredicates.isArray()))
                                                .build();

        TypePredicateTarget target = Generators.ofObject(TypePredicateTarget.class, config).generate();

        assertNull(target.createdOn);
        assertNull(target.tags);
        assertNotNull(target.title);
    }

    @Test
    @DisplayName("TypePredicates.isAssignableFrom(...) excludes abstract/interface fields")
    void assignabilityPredicateExcludesInterfaceField() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(PaymentMethod.class, CardPayment::new)
                                                .objectExcludeType(TypePredicates.isAssignableFrom(CardPayment.class))
                                                .build();

        AbstractTypeTarget target = Generators.ofObject(AbstractTypeTarget.class, config).generate();

        assertNull(target.paymentMethod);
        assertNotNull(target.title);
    }

    @Test
    @DisplayName("@Exclude maps to the native object exclusion annotation")
    void excludeAnnotationMapsToNativeAnnotation() {
        AnnotatedExcludeTarget target = Generators.ofObject(AnnotatedExcludeTarget.class).generate();

        assertNull(target.password);
        assertNotNull(target.username);
    }

    @Test
    @DisplayName("@RandomizerArgument converts reference-style constructor argument types")
    void randomizerArgumentConvertsReferenceStyleTypes() {
        ArgumentConversionTarget target = Generators.ofObject(ArgumentConversionTarget.class).generate();

        assertEquals(
            "active=true|count=17|ratio=2.5|bigInteger=12345678901234567890|bigDecimal=19.95"
            + "|utilDate=2024-05-10T11:12:13|sqlDate=2024-05-11|sqlTime=09:30:00"
            + "|timestamp=2024-05-12 13:14:15.0|localDate=2024-05-13|localTime=10:15:30"
            + "|localDateTime=2024-05-14T16:17:18|numbers=[1, 2, 3]|words=[red, blue]",
            target.summary);
    }

    @Test
    @DisplayName("@RandomizerArgument converts empty array constructor arguments")
    void randomizerArgumentConvertsEmptyArrayArguments() {
        EmptyArrayArgumentTarget target = Generators.ofObject(EmptyArrayArgumentTarget.class).generate();

        assertEquals("[]", target.summary);
    }

    static class SecretRoot {

        String      rootSecret;
        String      visibleValue;
        SecretChild child;
    }

    static class SecretChild {

        String childSecret;
        String childVisibleValue;
    }

    static class BaseSecret {

        String inheritedSecret;
    }

    static class ChildSecret extends BaseSecret {

        String localValue;
    }

    static class TypePredicateTarget {

        LocalDate createdOn;
        String[]  tags;
        String    title;
    }

    interface PaymentMethod {
    }

    static class CardPayment implements PaymentMethod {
    }

    static class AbstractTypeTarget {

        PaymentMethod paymentMethod;
        String        title;
    }

    static class AnnotatedExcludeTarget {

        @Exclude
        String password;
        String username;
    }

    static class ArgumentConversionTarget {

        @Randomizer(ArgumentSummaryGenerator.class)
        @RandomizerArgument(type = boolean.class, value = "true")
        @RandomizerArgument(type = long.class, value = "17")
        @RandomizerArgument(type = double.class, value = "2.5")
        @RandomizerArgument(type = BigInteger.class, value = "12345678901234567890")
        @RandomizerArgument(type = BigDecimal.class, value = "19.95")
        @RandomizerArgument(type = java.util.Date.class, value = "2024-05-10 11:12:13")
        @RandomizerArgument(type = java.sql.Date.class, value = "2024-05-11")
        @RandomizerArgument(type = java.sql.Time.class, value = "09:30:00")
        @RandomizerArgument(type = java.sql.Timestamp.class, value = "2024-05-12 13:14:15")
        @RandomizerArgument(type = LocalDate.class, value = "2024-05-13")
        @RandomizerArgument(type = LocalTime.class, value = "10:15:30")
        @RandomizerArgument(type = LocalDateTime.class, value = "2024-05-14T16:17:18")
        @RandomizerArgument(type = int[].class, value = "1, 2, 3,")
        @RandomizerArgument(type = String[].class, value = "red, blue,")
        String summary;
    }

    static class EmptyArrayArgumentTarget {

        @Randomizer(EmptyArraySummaryGenerator.class)
        @RandomizerArgument(type = int[].class, value = "")
        String summary;
    }

    public static class ArgumentSummaryGenerator implements Generator<String> {

        private final String summary;

        @SuppressWarnings("checkstyle:ParameterNumber")
        public ArgumentSummaryGenerator(boolean active,
                                        long count,
                                        double ratio,
                                        BigInteger bigInteger,
                                        BigDecimal bigDecimal,
                                        java.util.Date utilDate,
                                        java.sql.Date sqlDate,
                                        java.sql.Time sqlTime,
                                        java.sql.Timestamp timestamp,
                                        LocalDate localDate,
                                        LocalTime localTime,
                                        LocalDateTime localDateTime,
                                        int[] numbers,
                                        String[] words) {
            this.summary = "active=" + active
                           + "|count=" + count
                           + "|ratio=" + ratio
                           + "|bigInteger=" + bigInteger
                           + "|bigDecimal=" + bigDecimal.toPlainString()
                           + "|utilDate=" + LocalDateTime.ofInstant(utilDate.toInstant(), ZoneOffset.UTC)
                           + "|sqlDate=" + sqlDate
                           + "|sqlTime=" + sqlTime
                           + "|timestamp=" + timestamp
                           + "|localDate=" + localDate
                           + "|localTime=" + localTime
                           + "|localDateTime=" + localDateTime
                           + "|numbers=" + Arrays.toString(numbers)
                           + "|words=" + Arrays.toString(words);
        }

        @Override
        public String generate() {
            return summary;
        }
    }

    public static class EmptyArraySummaryGenerator implements Generator<String> {

        private final String summary;

        public EmptyArraySummaryGenerator(int[] numbers) {
            this.summary = Arrays.toString(numbers);
        }

        @Override
        public String generate() {
            return summary;
        }
    }
}
