/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@KrandomTest
@TestPropertySource(properties = {"krandom.seed=42", "krandom.locale=fr-FR"})
class KrandomTestSliceTest {

    @SpringBootConfiguration
    static class SliceConfiguration {
    }

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    GeneratorConfig generatorConfig;

    @Autowired
    ProviderHub providerHub;

    @Autowired
    KrandomObjectFakerFactory factory;

    @Test
    @DisplayName("@KrandomTest loads GeneratorConfig bean")
    void loadsGeneratorConfig() {
        assertNotNull(generatorConfig);
    }

    @Test
    @DisplayName("@KrandomTest loads ProviderHub bean")
    void loadsProviderHub() {
        assertNotNull(providerHub);
    }

    @Test
    @DisplayName("@KrandomTest loads KrandomObjectFakerFactory bean")
    void loadsFactory() {
        assertNotNull(factory);
    }

    @Test
    @DisplayName("factory produces working generators")
    void factoryGeneratesObjects() {
        var gen = factory.generator(SampleEntity.class);
        SampleEntity entity = gen.generate();
        assertNotNull(entity);
        assertNotNull(entity.name);
    }

    @Test
    @DisplayName("seed property is applied")
    void seedPropertyApplied() {
        assertTrue(generatorConfig.getSeed().isPresent());
        assertEquals(42L, generatorConfig.getSeed().getAsLong());
    }

    @Test
    @DisplayName("locale property is applied")
    void localePropertyApplied() {
        assertEquals("fr", generatorConfig.getLocale().getLanguage());
        assertEquals("FR", generatorConfig.getLocale().getCountry());
    }

    @Test
    @DisplayName("slice does not run unrelated auto-configuration")
    void sliceExcludesUnrelatedAutoConfiguration() {
        assertTrue(applicationContext
                        .getBeansOfType(org.springframework.core.task.TaskExecutor.class)
                        .isEmpty(),
                "full auto-configuration must stay disabled inside the slice");
    }

    public static class SampleEntity {
        public String name;
        public int count;
    }
}
