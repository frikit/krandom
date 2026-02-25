/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * POJO with JSR-310 date/time and {@link UUID} fields used to verify that
 * {@link org.github.krandom.generator.object.ObjectGenerator} auto-populates
 * these types via the built-in generator registry.
 */
public class PersonWithDateTimes {

    private LocalDate     dob;
    private LocalDateTime createdAt;
    private Instant       updatedAt;
    private ZonedDateTime scheduledAt;
    private UUID          id;

    public PersonWithDateTimes() { }

    public LocalDate     getDob()         { return dob; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public Instant       getUpdatedAt()   { return updatedAt; }
    public ZonedDateTime getScheduledAt() { return scheduledAt; }
    public UUID          getId()          { return id; }
}
