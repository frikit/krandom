/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.core.model;

/**
 * Self-referential POJO used to verify that {@link io.github.frikit.krandom.generator.object.ObjectGenerator}
 * handles circular object graphs without stack overflow.
 */
public class CircularNode {

    private String       name;
    private CircularNode next;

    public CircularNode() {
    }

    public String getName() {
        return name;
    }

    public CircularNode getNext() {
        return next;
    }
}
