/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithAllConstraints;
import org.github.krandom.generator.core.model.PersonWithConstraints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — Bean Validation constraint respect")
class ObjectGeneratorBeanValidationTest {

    private static final int SAMPLES = 50;

    @Test
    @DisplayName("@Size(min=3, max=10) on String is respected")
    void sizeConstraintOnString() {
        for (int i = 0; i < SAMPLES; i++) {
            String username = new ObjectGenerator<>(PersonWithConstraints.class).generate().getUsername();
            assertNotNull(username);
            assertTrue(username.length() >= 3 && username.length() <= 10,
                    "username length " + username.length() + " violates @Size(3, 10)");
        }
    }

    @Test
    @DisplayName("@Min(18) @Max(120) on int is respected")
    void minMaxConstraintOnInt() {
        for (int i = 0; i < SAMPLES; i++) {
            int age = new ObjectGenerator<>(PersonWithConstraints.class).generate().getAge();
            assertTrue(age >= 18 && age <= 120,
                    "age " + age + " violates @Min(18) @Max(120)");
        }
    }

    @Test
    @DisplayName("@Email on String produces email-like value")
    void emailConstraint() {
        for (int i = 0; i < SAMPLES; i++) {
            String email = new ObjectGenerator<>(PersonWithConstraints.class).generate().getEmail();
            assertNotNull(email);
            assertTrue(email.contains("@"),
                    "email '" + email + "' does not contain '@'");
        }
    }

    @Test
    @DisplayName("@Pattern(regexp='\\d{5}') on String produces 5 digits")
    void patternConstraint() {
        for (int i = 0; i < SAMPLES; i++) {
            String zip = new ObjectGenerator<>(PersonWithConstraints.class).generate().getZipCode();
            assertNotNull(zip);
            assertTrue(zip.matches("\\d{5}"),
                    "zipCode '" + zip + "' does not match \\d{5}");
        }
    }

    @Test
    @DisplayName("@Positive on long produces value > 0")
    void positiveConstraintOnLong() {
        for (int i = 0; i < SAMPLES; i++) {
            long salary = new ObjectGenerator<>(PersonWithConstraints.class).generate().getSalary();
            assertTrue(salary > 0, "salary " + salary + " violates @Positive");
        }
    }

    @Test
    @DisplayName("@DecimalMin('0.01') @DecimalMax('9.99') on BigDecimal is respected")
    void decimalMinMaxConstraint() {
        BigDecimal lo = new BigDecimal("0.01");
        BigDecimal hi = new BigDecimal("9.99");
        for (int i = 0; i < SAMPLES; i++) {
            BigDecimal price = new ObjectGenerator<>(PersonWithConstraints.class).generate().getPrice();
            assertNotNull(price);
            assertTrue(price.compareTo(lo) >= 0 && price.compareTo(hi) <= 0,
                    "price " + price + " violates @DecimalMin/Max");
        }
    }

    // ── Comprehensive sign/bound constraint coverage ──────────────────────────

    @Nested
    @DisplayName("All int/Integer sign constraints")
    class IntSignConstraintsTest {

        @Test
        @DisplayName("@Positive int produces value > 0")
        void positiveInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getPositiveInt();
                assertTrue(v > 0, "@Positive int must be > 0, got " + v);
            }
        }

        @Test
        @DisplayName("@PositiveOrZero int produces value >= 0")
        void posOrZeroInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getPosOrZeroInt();
                assertTrue(v >= 0, "@PositiveOrZero int must be >= 0, got " + v);
            }
        }

        @Test
        @DisplayName("@Negative int produces value < 0")
        void negativeInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getNegativeInt();
                assertTrue(v < 0, "@Negative int must be < 0, got " + v);
            }
        }

        @Test
        @DisplayName("@NegativeOrZero int produces value <= 0")
        void negOrZeroInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getNegOrZeroInt();
                assertTrue(v <= 0, "@NegativeOrZero int must be <= 0, got " + v);
            }
        }

        @Test
        @DisplayName("@Min(5) int produces value >= 5")
        void minOnlyInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getMinOnlyInt();
                assertTrue(v >= 5, "@Min(5) int must be >= 5, got " + v);
            }
        }

        @Test
        @DisplayName("@Max(100) int produces value <= 100")
        void maxOnlyInt() {
            for (int i = 0; i < SAMPLES; i++) {
                int v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getMaxOnlyInt();
                assertTrue(v <= 100, "@Max(100) int must be <= 100, got " + v);
            }
        }

        @Test
        @DisplayName("@Min(1) @Max(50) Integer (boxed) produces value in [1, 50]")
        void boxedIntWithMinMax() {
            for (int i = 0; i < SAMPLES; i++) {
                Integer v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getBoxedInt();
                assertNotNull(v);
                assertTrue(v >= 1 && v <= 50, "@Min(1) @Max(50) Integer must be in [1,50], got " + v);
            }
        }
    }

    @Nested
    @DisplayName("All long/Long sign constraints")
    class LongSignConstraintsTest {

        @Test
        @DisplayName("@PositiveOrZero long produces value >= 0")
        void posOrZeroLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getPosOrZeroLong();
                assertTrue(v >= 0, "@PositiveOrZero long must be >= 0, got " + v);
            }
        }

        @Test
        @DisplayName("@Negative long produces value < 0")
        void negativeLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getNegativeLong();
                assertTrue(v < 0, "@Negative long must be < 0, got " + v);
            }
        }

        @Test
        @DisplayName("@NegativeOrZero long produces value <= 0")
        void negOrZeroLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getNegOrZeroLong();
                assertTrue(v <= 0, "@NegativeOrZero long must be <= 0, got " + v);
            }
        }

        @Test
        @DisplayName("@Min(10) @Max(1000) long produces value in [10, 1000]")
        void minMaxLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getMinMaxLong();
                assertTrue(v >= 10 && v <= 1000, "@Min(10) @Max(1000) long must be in [10,1000], got " + v);
            }
        }

        @Test
        @DisplayName("@Min(5) long produces value >= 5")
        void minOnlyLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getMinOnlyLong();
                assertTrue(v >= 5, "@Min(5) long must be >= 5, got " + v);
            }
        }

        @Test
        @DisplayName("@Max(200) long produces value <= 200")
        void maxOnlyLong() {
            for (int i = 0; i < SAMPLES; i++) {
                long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getMaxOnlyLong();
                assertTrue(v <= 200, "@Max(200) long must be <= 200, got " + v);
            }
        }

        @Test
        @DisplayName("@Positive Long (boxed) produces value > 0")
        void boxedLongPositive() {
            for (int i = 0; i < SAMPLES; i++) {
                Long v = new ObjectGenerator<>(PersonWithAllConstraints.class).generate().getBoxedLong();
                assertNotNull(v);
                assertTrue(v > 0, "@Positive Long must be > 0, got " + v);
            }
        }
    }
}
