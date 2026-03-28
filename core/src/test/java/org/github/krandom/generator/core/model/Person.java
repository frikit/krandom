/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

/**
 * Extends {@link BaseJavaClass} (tests inheritance field collection) and contains:
 * <ul>
 *   <li>Primitive / String fields — base type generation</li>
 *   <li>{@link Status} enum field — enum random selection</li>
 *   <li>{@link Address} nested object field — recursive generation</li>
 * </ul>
 */
public class Person extends BaseJavaClass {

    private String  firstName;
    private String  lastName;
    private int     age;
    private double  salary;
    private boolean active;
    private Status  status;
    private Address address;

    public Person() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public double getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }

    public Status getStatus() {
        return status;
    }

    public Address getAddress() {
        return address;
    }
}
