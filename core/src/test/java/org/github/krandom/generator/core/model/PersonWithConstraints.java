/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Test model with Bean Validation constraints on its fields.
 */
public class PersonWithConstraints {

    @Size(min = 3, max = 10)
    String username;

    @Min(18)
    @Max(120)
    int age;

    @Email
    String email;

    @Pattern(regexp = "\\d{5}")
    String zipCode;

    @Positive
    long salary;

    @DecimalMin("0.01")
    @DecimalMax("9.99")
    BigDecimal price;

    public PersonWithConstraints() {
    }

    public String getUsername() {
        return username;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getZipCode() {
        return zipCode;
    }

    public long getSalary() {
        return salary;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
