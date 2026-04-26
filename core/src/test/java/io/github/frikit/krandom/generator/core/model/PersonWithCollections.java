/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.core.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * POJO with generic collection fields used to verify that
 * {@link io.github.frikit.krandom.generator.object.ObjectGenerator} auto-populates
 * {@link List}, {@link Set}, and {@link Map} fields from their declared type arguments.
 */
public class PersonWithCollections {

    private List<String>         hobbies;
    private Set<Status>          roles;
    private Map<String, Integer> attributes;

    public PersonWithCollections() {
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public Set<Status> getRoles() {
        return roles;
    }

    public Map<String, Integer> getAttributes() {
        return attributes;
    }
}
