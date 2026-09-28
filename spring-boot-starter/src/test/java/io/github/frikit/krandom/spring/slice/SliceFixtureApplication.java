/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application class in a parent package of the slice tests: {@code @KrandomTest} finds it the way
 * Spring Boot slices find a real application, and its component scan would pick up every
 * {@code @Service} in this package.
 */
@SpringBootApplication
public class SliceFixtureApplication {
}
