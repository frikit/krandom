/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

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
}
