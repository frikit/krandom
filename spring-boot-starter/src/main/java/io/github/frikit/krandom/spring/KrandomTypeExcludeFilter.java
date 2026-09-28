/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import org.springframework.boot.test.context.filter.annotation.StandardAnnotationCustomizableTypeExcludeFilter;

/**
 * Component-scan filter for the {@link KrandomTest} slice.
 *
 * <p>Like Spring Boot's own slices, the slice keeps only what it imports explicitly: every
 * component found by the application's {@code @ComponentScan} (for example a {@code @Service} of an
 * {@code @SpringBootApplication} in a parent package) is excluded, because auto-configuration is
 * disabled and such components usually depend on auto-configured infrastructure. Tests opt their
 * own beans in with {@code @Import}. Instantiated reflectively by Spring Boot's
 * {@code TypeExcludeFiltersContextCustomizer}.
 */
final class KrandomTypeExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<KrandomTest> {

    KrandomTypeExcludeFilter(Class<?> testClass) {
        super(testClass);
    }
}
