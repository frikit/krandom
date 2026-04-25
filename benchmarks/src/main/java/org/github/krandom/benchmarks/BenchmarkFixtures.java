/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Shared fixture models for benchmark scenarios.
 */
public final class BenchmarkFixtures {

    private BenchmarkFixtures() {
    }

    public static final class Depth2Root {
        public Depth2Level1 level1;
    }

    public static final class Depth2Level1 {
        public Depth2Leaf leaf;
    }

    public static final class Depth2Leaf {
        public String value;
    }

    public static final class Depth5Root {
        public Depth5Level1 level1;
    }

    public static final class Depth5Level1 {
        public Depth5Level2 level2;
    }

    public static final class Depth5Level2 {
        public Depth5Level3 level3;
    }

    public static final class Depth5Level3 {
        public Depth5Level4 level4;
    }

    public static final class Depth5Level4 {
        public Depth5Leaf leaf;
    }

    public static final class Depth5Leaf {
        public String value;
    }

    public static final class Depth10Root {
        public Depth10Level1 level1;
    }

    public static final class Depth10Level1 {
        public Depth10Level2 level2;
    }

    public static final class Depth10Level2 {
        public Depth10Level3 level3;
    }

    public static final class Depth10Level3 {
        public Depth10Level4 level4;
    }

    public static final class Depth10Level4 {
        public Depth10Level5 level5;
    }

    public static final class Depth10Level5 {
        public Depth10Level6 level6;
    }

    public static final class Depth10Level6 {
        public Depth10Level7 level7;
    }

    public static final class Depth10Level7 {
        public Depth10Level8 level8;
    }

    public static final class Depth10Level8 {
        public Depth10Level9 level9;
    }

    public static final class Depth10Level9 {
        public Depth10Leaf leaf;
    }

    public static final class Depth10Leaf {
        public String value;
    }

    public static final class SimpleUser {
        public String firstName;
        public String lastName;
        public String email;
        public int age;
    }

    public static final class SemanticCustomer {
        public String          id;
        public String          firstName;
        public String          lastName;
        public String          fullName;
        public String          email;
        public String          username;
        public String          phoneNumber;
        public String          companyName;
        public String          url;
        public String          domain;
        public String          status;
        public boolean         active;
        public LocalDate       birthDate;
        public Instant         createdAt;
        public BigDecimal      balance;
        public String          currencyCode;
        public SemanticAddress address;
    }

    public static final class SemanticAddress {
        public String     streetAddress;
        public String     city;
        public String     state;
        public String     postalCode;
        public String     country;
        public BigDecimal latitude;
        public BigDecimal longitude;
    }

    /**
     * Minimal POJO that all competitor libraries can populate via public fields.
     * Kept intentionally simple so the benchmark measures library overhead, not model complexity.
     */
    public static final class ComparableUser {
        public String  firstName;
        public String  lastName;
        public String  email;
        public int     age;
        public String  city;
        public String  country;
    }
}
