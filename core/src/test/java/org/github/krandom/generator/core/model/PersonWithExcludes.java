/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

import org.github.krandom.generator.object.Exclude;

/**
 * POJO used to verify field-exclusion behaviour in
 * {@link org.github.krandom.generator.object.ObjectGenerator}.
 *
 * <ul>
 *   <li>{@code password} — annotated with {@link Exclude}; must never be populated.</li>
 *   <li>{@code username} — normal field; always populated.</li>
 *   <li>{@code age} — normal field; always populated.</li>
 * </ul>
 */
public class PersonWithExcludes {

    @Exclude
    private String password;
    private String username;
    private int    age;

    public PersonWithExcludes() { }

    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public int    getAge()      { return age; }
}
