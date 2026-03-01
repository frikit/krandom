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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AvatarUrlGenerator")
class AvatarUrlGeneratorTest {

    @Test
    @DisplayName("generates avatar URLs")
    void generatesUrls() {
        AvatarUrlGenerator generator = new AvatarUrlGenerator();
        String url = generator.generate();
        assertTrue(url.startsWith("https://"));
        assertTrue(url.contains("dicebear") || url.contains("robohash") || url.contains("ui-avatars"));
    }

    @Test
    @DisplayName("supports username and size options")
    void options() {
        AvatarUrlGenerator generator = new AvatarUrlGenerator(GeneratorConfig.builder().seed(5L).build());
        String byName = generator.generate("john doe");
        String bySize = generator.generate(128);
        String full = generator.generate("alice", 512);

        assertTrue(byName.contains("john%20doe"));
        assertTrue(bySize.contains("128") || bySize.contains("size=128"));
        assertTrue(full.contains("alice"));
    }

    @Test
    @DisplayName("covers all avatar providers")
    void providers() {
        boolean dicebear = false;
        boolean robohash = false;
        boolean uiAvatars = false;

        for (int seed = 0; seed < 64; seed++) {
            AvatarUrlGenerator generator = new AvatarUrlGenerator(
                    GeneratorConfig.builder().seed(seed).build()
            );
            String url = generator.generate("user", 256);
            dicebear |= url.contains("dicebear");
            robohash |= url.contains("robohash");
            uiAvatars |= url.contains("ui-avatars");
        }

        assertTrue(dicebear && robohash && uiAvatars);
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        AvatarUrlGenerator a = new AvatarUrlGenerator(cfg);
        AvatarUrlGenerator b = new AvatarUrlGenerator(cfg);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("validates config and input")
    void validation() {
        AvatarUrlGenerator generator = new AvatarUrlGenerator();
        assertThrows(NullPointerException.class, () -> new AvatarUrlGenerator(null));
        assertThrows(NullPointerException.class, () -> generator.generate((String) null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("  "));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("user", 8));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("user", 2048));
    }

    @Test
    @DisplayName("Generators factory exposes avatar URL generator")
    void factory() {
        assertNotNull(Generators.ofAvatarUrl().generate());
    }
}
