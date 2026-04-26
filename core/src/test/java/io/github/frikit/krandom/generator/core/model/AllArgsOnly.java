/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.core.model;

/**
 * Test model with only an all-args constructor (no no-arg constructor).
 * Used to verify Objenesis-based instantiation.
 */
public class AllArgsOnly {

    private String name;
    private int    age;

    public AllArgsOnly(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
