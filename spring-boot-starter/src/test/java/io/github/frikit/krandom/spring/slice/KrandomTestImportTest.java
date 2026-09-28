/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import io.github.frikit.krandom.spring.KrandomTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@KrandomTest
@Import(FixtureService.class)
@DisplayName("@KrandomTest with explicitly imported application beans")
class KrandomTestImportTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    FixtureService fixtureService;

    @Autowired
    KrandomObjectFakerFactory factory;

    @Test
    @DisplayName("imports opted-in beans while other scanned components stay excluded")
    void importedBeansAreAvailable() {
        assertSame(factory, fixtureService.factory());
        assertEquals(0, context.getBeanNamesForType(ReportService.class).length);
    }
}
