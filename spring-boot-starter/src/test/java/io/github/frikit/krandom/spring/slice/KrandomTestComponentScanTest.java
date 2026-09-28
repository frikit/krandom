/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import io.github.frikit.krandom.spring.KrandomTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@KrandomTest
@DisplayName("@KrandomTest with a component-scanning application class")
class KrandomTestComponentScanTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    GeneratorConfig generatorConfig;

    @Autowired
    KrandomObjectFakerFactory factory;

    @Test
    @DisplayName("excludes scanned application components that need full auto-configuration")
    void scannedApplicationComponentsAreExcluded() {
        assertEquals(0, context.getBeanNamesForType(ReportService.class).length);
        assertEquals(0, context.getBeanNamesForType(FixtureService.class).length);
        assertNotNull(generatorConfig);
        assertNotNull(factory.generator(SliceFixture.class).generate());
    }

    public static class SliceFixture {
        public String name;
    }
}
