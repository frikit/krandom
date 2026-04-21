/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JobInfoGenerator")
class JobInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured job payload")
    void generateJobInfo() {
        JobInfo info = new JobInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(!info.descriptor().isBlank());
        assertTrue(!info.level().isBlank());
        assertTrue(!info.title().isBlank());
        assertTrue(!info.type().isBlank());
        assertTrue(!info.profession().isBlank());
        assertTrue(info.title().contains(info.level()));
        assertTrue(info.title().contains(info.profession()));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        JobInfoGenerator one = new JobInfoGenerator(config);
        JobInfoGenerator two = new JobInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new JobInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new JobInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new JobInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofJobInfo().generate());
        assertNotNull(Generators.ofJobInfo(Locale.US).generate());
        assertNotNull(Generators.ofJobInfo(GeneratorConfig.defaults()).generate());
    }

    @Test
    @DisplayName("joinNonBlank skips null and blank segments")
    void joinNonBlankSkipsNullAndBlankSegments() throws Exception {
        Method joinNonBlank = JobInfoGenerator.class.getDeclaredMethod("joinNonBlank", String.class, String[].class);
        joinNonBlank.setAccessible(true);

        assertEquals("Senior Engineer",
                     joinNonBlank.invoke(null, new Object[] { " ", new String[] { null, "", "Senior", "Engineer" } }));
    }
}
