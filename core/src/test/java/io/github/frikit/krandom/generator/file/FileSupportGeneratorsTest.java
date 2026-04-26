/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("File support generators")
class FileSupportGeneratorsTest {

    private static void assertPathLooksValid(String path) {
        assertNotNull(path);
        assertTrue(path.startsWith("/"));
        assertTrue(path.contains("/"));
    }

    @Test
    void dirPathGenerator() {
        DirPathGenerator gen = new DirPathGenerator(Locale.GERMANY);
        String path = gen.generate();
        assertTrue(path.startsWith("/"));
        assertTrue(path.contains("/"));
        assertEquals(Locale.GERMANY, gen.getLocale());
        assertEquals(
            new DirPathGenerator(GeneratorConfig.builder().seed(42L).locale(Locale.US).build()).generate(),
            new DirPathGenerator(GeneratorConfig.builder().seed(42L).locale(Locale.US).build()).generate()
        );
        assertThrows(NullPointerException.class, () -> new DirPathGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new DirPathGenerator((GeneratorConfig) null));
    }

    @Test
    void dirPathLocaleBranches() {
        assertPathLooksValid(new DirPathGenerator(Locale.GERMANY).generate());      // de
        assertPathLooksValid(new DirPathGenerator(Locale.FRANCE).generate());       // fr
        assertPathLooksValid(new DirPathGenerator(Locale.of("es", "ES")).generate()); // es
        assertPathLooksValid(new DirPathGenerator(Locale.ITALY).generate());        // it
        assertPathLooksValid(new DirPathGenerator(Locale.of("pt", "BR")).generate()); // pt
        assertPathLooksValid(new DirPathGenerator(Locale.JAPAN).generate());        // ja
        assertPathLooksValid(new DirPathGenerator(Locale.CHINA).generate());        // zh
        assertPathLooksValid(new DirPathGenerator(Locale.US).generate());           // default
    }

    @Test
    void filePathGenerator() {
        FilePathGenerator gen = new FilePathGenerator(Locale.JAPAN);
        String path = gen.generate();
        assertTrue(path.startsWith("/"));
        assertTrue(path.matches(".+/.+\\.[a-z0-9]+"));
        assertEquals(Locale.JAPAN, gen.getLocale());
        assertTrue(gen.generateWithExtension("json").endsWith(".json"));
        assertTrue(gen.generateWithExtension(".xml").endsWith(".xml"));
        assertThrows(IllegalArgumentException.class, () -> gen.generateWithExtension(null));
        assertThrows(IllegalArgumentException.class, () -> gen.generateWithExtension(" "));
        assertThrows(NullPointerException.class, () -> new FilePathGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new FilePathGenerator((GeneratorConfig) null));
    }

    @Test
    void mimeTypeGenerator() {
        MimeTypeGenerator gen = new MimeTypeGenerator();
        String mime = gen.generate();
        assertTrue(mime.matches("[a-z]+/[a-z0-9.+-]+"));
        assertEquals(
            new MimeTypeGenerator(GeneratorConfig.builder().seed(11L).build()).generate(),
            new MimeTypeGenerator(GeneratorConfig.builder().seed(11L).build()).generate()
        );
        assertThrows(NullPointerException.class, () -> new MimeTypeGenerator(null));
    }

    @Test
    void semverGenerator() {
        SemverGenerator gen = new SemverGenerator(GeneratorConfig.builder().seed(99L).build());
        assertTrue(gen.generate().matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue(gen.generateStable().matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue(gen.generatePrerelease().matches("\\d+\\.\\d+\\.\\d+-(alpha|beta|rc)\\.\\d+"));
        assertThrows(NullPointerException.class, () -> new SemverGenerator(null));
    }
}
