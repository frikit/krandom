/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Native-image reachability metadata")
class NativeImageMetadataTest {

    private static final String METADATA_PATH =
        "/META-INF/native-image/io.github.frikit/krandom/reachability-metadata.json";

    @Test
    @DisplayName("includes object reflection and classpath resource reachability")
    void includesObjectReflectionAndClasspathResourceReachability() throws IOException {
        try (InputStream input = NativeImageMetadataTest.class.getResourceAsStream(METADATA_PATH)) {
            assertNotNull(input, "native-image reachability metadata must be packaged");
            String metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(metadata.contains("io.github.frikit.krandom.generator.object.ObjectGenerator"));
            assertTrue(metadata.contains("io.github.frikit.krandom.generator.object.ObjectFaker"));
            assertTrue(metadata.contains("krandom/.*\\\\.txt"));
        }
    }
}
