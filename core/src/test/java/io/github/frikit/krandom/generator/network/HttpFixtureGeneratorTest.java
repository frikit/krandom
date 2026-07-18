/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpFixtureGenerator")
class HttpFixtureGeneratorTest {

    @Test
    void generatesCoherentFixture() {
        HttpFixture fixture = new HttpFixtureGenerator(GeneratorConfig.builder().seed(42L).build()).generate();

        assertTrue(fixture.method().matches("GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS"));
        assertTrue(fixture.version().matches("HTTP/(1\\.0|1\\.1|2|3)"));
        assertTrue(fixture.status().code() >= 100 && fixture.status().code() <= 599);
        assertTrue(fixture.statusLine().matches("HTTP/(1\\.0|1\\.1|2|3) \\d{3} .+"));
        assertFalse(fixture.requestHeaderName().isBlank());
        assertFalse(fixture.responseHeaderName().isBlank());
        assertFalse(fixture.contentType().isBlank());
        assertTrue(fixture.contentEncoding().matches("identity|gzip|br|deflate"));
        assertFalse(fixture.userAgent().isBlank());
        assertTrue(HttpFixtureGenerator.isBodyCompatible(fixture.contentType(), fixture.responseBody()));
    }

    @Test
    void seededGeneratorsProduceSameFixtures() {
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).build();
        HttpFixtureGenerator first = new HttpFixtureGenerator(config);
        HttpFixtureGenerator second = new HttpFixtureGenerator(config);

        assertEquals(first.generate(), second.generate());
        assertEquals(first.responseBody("application/json"), second.responseBody("application/json"));
    }

    @Test
    void publicFactoriesAndValidationAreAvailable() {
        assertNotNull(Generators.ofHttpFixture().generate());
        assertNotNull(Generators.network().httpFixture().generate());
        assertNotNull(new HttpFixtureGenerator().generate());
        assertThrows(NullPointerException.class, () -> new HttpFixtureGenerator(null));
        assertThrows(IllegalArgumentException.class, () -> new HttpStatus(99, "Too early"));
        assertThrows(IllegalArgumentException.class, () -> new HttpStatus(600, "Too late"));
        assertThrows(NullPointerException.class, () -> new HttpStatus(200, null));
        assertThrows(IllegalArgumentException.class, () -> new HttpStatus(200, " "));
        assertEquals("HTTP/2 200 OK", new HttpStatus(200, "OK").statusLine("HTTP/2"));
        assertThrows(IllegalArgumentException.class, () -> new HttpStatus(200, "OK").statusLine("2"));
        assertThrows(NullPointerException.class, () -> new HttpStatus(200, "OK").statusLine(null));
        assertThrows(IllegalArgumentException.class, () -> new HttpFixture(" ", "HTTP/2", new HttpStatus(200, "OK"),
                                                                       "Accept", "ETag", "text/plain", "identity",
                                                                       "agent", "body"));
    }

    @Test
    @DisplayName("provides every documented component and compatible media body shape")
    void providesComponentsAndCompatibleMediaBodyShapes() {
        HttpFixtureGenerator generator = new HttpFixtureGenerator(GeneratorConfig.builder().seed(99L).build());

        assertTrue(List.of("HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/3").contains(generator.httpVersion()));
        assertTrue(List.of("Accept", "Accept-Language", "Authorization", "Content-Type", "Host", "Origin", "User-Agent")
                       .contains(generator.requestHeaderName()));
        assertTrue(List.of("Cache-Control", "Content-Length", "Content-Type", "ETag", "Location", "Retry-After", "Vary")
                       .contains(generator.responseHeaderName()));
        assertTrue(List.of("identity", "gzip", "br", "deflate").contains(generator.contentEncoding()));
        HttpStatus status = generator.status();
        assertTrue(status.reasonPhrase().contains(" ") || status.reasonPhrase().matches("[A-Za-z]+"));

        for (String contentType : List.of("application/json", "application/xml", "text/html", "text/csv",
                                          "application/javascript", "text/css", "application/graphql", "text/markdown")) {
            String body = generator.responseBody(contentType);
            assertTrue(HttpFixtureGenerator.isBodyCompatible(contentType, body), contentType);
            assertFalse(HttpFixtureGenerator.isBodyCompatible(contentType, "invalid"), contentType);
        }

        assertFalse(HttpFixtureGenerator.isBodyCompatible("application/json", "{"));
        assertFalse(HttpFixtureGenerator.isBodyCompatible("application/xml", "<response>"));
        assertTrue(HttpFixtureGenerator.isBodyCompatible("application/octet-stream",
                                                         generator.responseBody("application/octet-stream")));
        assertFalse(HttpFixtureGenerator.isBodyCompatible("application/octet-stream", " \t"));
        assertTrue(HttpFixtureGenerator.isBodyCompatible("text/plain", generator.responseBody("text/plain")));
        assertFalse(HttpFixtureGenerator.isBodyCompatible("text/plain", " \t"));
        for (String alias : List.of("application/ld+json; charset=UTF-8", "application/problem+json",
                                    "application/vnd.api+json")) {
            assertTrue(HttpFixtureGenerator.isBodyCompatible(alias, generator.responseBody(alias)));
        }
        assertTrue(HttpFixtureGenerator.isBodyCompatible("text/xml", generator.responseBody("text/xml")));
        assertThrows(NullPointerException.class, () -> generator.responseBody(null));
        assertThrows(NullPointerException.class, () -> HttpFixtureGenerator.isBodyCompatible(null, "body"));
        assertThrows(NullPointerException.class, () -> HttpFixtureGenerator.isBodyCompatible("text/plain", null));
    }
}
