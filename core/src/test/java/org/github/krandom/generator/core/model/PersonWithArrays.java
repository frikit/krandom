/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

/**
 * POJO with array fields used to verify that {@link org.github.krandom.generator.object.ObjectGenerator}
 * auto-populates arrays of primitive, wrapper, and object types.
 */
public class PersonWithArrays {

    private String[]  tags;
    private int[]     scores;
    private Address[] addresses;

    public PersonWithArrays() {
    }

    public String[] getTags() {
        return tags;
    }

    public int[] getScores() {
        return scores;
    }

    public Address[] getAddresses() {
        return addresses;
    }
}
