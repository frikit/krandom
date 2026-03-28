/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLGeneratorTest {

    @Test
    void testDefaultConstructor() {
        URLGenerator generator = new URLGenerator();
        assertNotNull(generator);
    }

    @Test
    void testLocaleConstructor() {
        URLGenerator generator = new URLGenerator(Locale.US);
        assertNotNull(generator);
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        URLGenerator generator = new URLGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new URLGenerator((GeneratorConfig) null));
    }

    @Test
    void testGenerateNotNull() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate();
        assertNotNull(url);
    }

    @Test
    void testGenerateHasProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate();
        assertTrue(url.contains("://"), "URL should contain protocol separator");
    }

    @Test
    void testGenerateHasValidProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate();
        assertTrue(
            url.startsWith("http://") ||
            url.startsWith("https://") ||
            url.startsWith("ftp://") ||
            url.startsWith("ws://") ||
            url.startsWith("wss://"),
            "URL should start with valid protocol"
        );
    }

    @Test
    void testGenerateWithSpecificProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("https");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    void testGenerateWithCustomProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("custom");
        assertTrue(url.startsWith("custom://"));
    }

    @Test
    void testGenerateWithNullProtocolThrowsException() {
        URLGenerator generator = new URLGenerator();
        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void testGenerateWithPath() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generateWithPath();
        assertTrue(url.contains("/"), "URL with path should contain slash");
        int protocolEnd = url.indexOf("://") + 3;
        int firstSlash = url.indexOf("/", protocolEnd);
        assertTrue(firstSlash > protocolEnd, "URL should have path after domain");
    }

    @Test
    void testGenerateWithPathAndQuery() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generateWithPathAndQuery();
        assertTrue(url.contains("?"), "URL should contain query separator");
        assertTrue(url.contains("="), "URL should contain query parameter");
    }

    @Test
    void testBogusStyleUrlAliases() {
        URLGenerator generator = new URLGenerator(Locale.UK);
        assertTrue(generator.generateUrl().contains("://"));
        assertTrue(generator.generateUrlWithPath().contains("/"));
        assertTrue(Set.of("http", "https", "ftp", "ws", "wss").contains(generator.generateProtocol()));
    }

    @Test
    void testGetProtocol() {
        URLGenerator generator = new URLGenerator();
        String protocol = generator.getProtocol();
        assertNotNull(protocol);
        assertTrue(Set.of("http", "https", "ftp", "ws", "wss").contains(protocol));
    }

    @Test
    void testGetPath() {
        URLGenerator generator = new URLGenerator();
        String path = generator.getPath();
        assertNotNull(path);
        assertTrue(path.startsWith("/"), "Path should start with slash");
    }

    @Test
    void testGetQueryString() {
        URLGenerator generator = new URLGenerator();
        String query = generator.getQueryString();
        assertNotNull(query);
        assertFalse(query.startsWith("?"), "Query string should not start with ?");
        assertTrue(query.contains("="), "Query string should contain =");
    }

    @Test
    void testSeededGeneratorProducesSameResults() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(42L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(42L).build());

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededGeneratorWithPathProducesSameResults() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(999L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generateWithPath(), gen2.generateWithPath());
        assertEquals(gen1.generateWithPath(), gen2.generateWithPath());
    }

    @Test
    void testSeededGeneratorWithPathAndQueryProducesSameResults() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(777L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(777L).build());

        assertEquals(gen1.generateWithPathAndQuery(), gen2.generateWithPathAndQuery());
    }

    @Test
    void testDifferentSeedsProduceDifferentResults() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(100L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(200L).build());

        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testGenerateMultipleURLs() {
        URLGenerator generator = new URLGenerator();
        Set<String> urls = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            urls.add(generator.generate());
        }
        assertTrue(urls.size() > 10, "Should generate diverse URLs");
    }

    @Test
    void testGenerateMultipleProtocols() {
        URLGenerator generator = new URLGenerator();
        Set<String> protocols = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            protocols.add(generator.getProtocol());
        }
        assertTrue(protocols.size() >= 3, "Should generate multiple protocols");
    }

    @Test
    void testPathHasMultipleSegments() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(123L).build());
        Set<Integer> segmentCounts = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String path = generator.getPath();
            int segments = path.split("/").length - 1; // -1 for leading slash
            segmentCounts.add(segments);
        }
        assertTrue(segmentCounts.size() > 1, "Should generate paths with varying segment counts");
    }

    @Test
    void testQueryStringHasMultipleParams() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(456L).build());
        Set<Integer> paramCounts = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String query = generator.getQueryString();
            int params = query.split("&").length;
            paramCounts.add(params);
        }
        assertTrue(paramCounts.size() > 1, "Should generate queries with varying parameter counts");
    }

    @Test
    void testLocaleInfluencesDomain() {
        URLGenerator deGenerator = new URLGenerator(Locale.GERMANY);
        Set<String> urls = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            urls.add(deGenerator.generate());
        }
        boolean hasDeUrl = urls.stream().anyMatch(u -> u.contains(".de"));
        assertTrue(hasDeUrl, "German locale should sometimes produce .de URLs");
    }

    @Test
    void testHTTPProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("http");
        assertTrue(url.startsWith("http://"));
        assertFalse(url.startsWith("https://"));
    }

    @Test
    void testHTTPSProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("https");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    void testFTPProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("ftp");
        assertTrue(url.startsWith("ftp://"));
    }

    @Test
    void testWSProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("ws");
        assertTrue(url.startsWith("ws://"));
    }

    @Test
    void testWSSProtocol() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generate("wss");
        assertTrue(url.startsWith("wss://"));
    }

    @Test
    void testURLWithPathHasDomain() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generateWithPath();
        int protocolEnd = url.indexOf("://") + 3;
        int pathStart = url.indexOf("/", protocolEnd);
        String domain = url.substring(protocolEnd, pathStart);
        assertTrue(domain.contains("."), "Should have domain with TLD");
    }

    @Test
    void testURLWithQueryHasPath() {
        URLGenerator generator = new URLGenerator();
        String url = generator.generateWithPathAndQuery();
        int queryStart = url.indexOf("?");
        int protocolEnd = url.indexOf("://") + 3;
        String beforeQuery = url.substring(protocolEnd, queryStart);
        assertTrue(beforeQuery.contains("/"), "Should have path before query");
    }

    @Test
    void testQueryParametersHaveValues() {
        URLGenerator generator = new URLGenerator();
        String query = generator.getQueryString();
        String[] params = query.split("&");
        for (String param : params) {
            assertTrue(param.contains("="), "Each parameter should have a value");
            String[] parts = param.split("=");
            assertEquals(2, parts.length, "Parameter should have key and value");
        }
    }

    @Test
    void testPathSegmentsAreAlphabetic() {
        URLGenerator generator = new URLGenerator();
        for (int i = 0; i < 20; i++) {
            String path = generator.getPath();
            String[] segments = path.substring(1).split("/"); // Remove leading slash
            for (String segment : segments) {
                assertTrue(segment.matches("[a-z0-9]+"), "Path segment should be alphanumeric: " + segment);
            }
        }
    }

    @Test
    void testGenerateWithPathHasDifferentSegmentCounts() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(333L).build());
        Set<Integer> segmentCounts = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            String path = generator.getPath();
            int segments = path.split("/").length - 1; // -1 for leading slash
            segmentCounts.add(segments);
        }

        assertTrue(segmentCounts.size() > 1, "Should generate paths with varying segment counts");
    }

    @Test
    void testGenerateWithQueryHasDifferentParamCounts() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(444L).build());
        Set<Integer> paramCounts = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            String query = generator.getQueryString();
            int params = query.split("&").length;
            paramCounts.add(params);
        }

        assertTrue(paramCounts.size() > 1, "Should generate queries with varying parameter counts");
    }

    @Test
    void testSeededGeneratorGetProtocolConsistency() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(666L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(666L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getProtocol(), gen2.getProtocol());
        }
    }

    @Test
    void testSeededGeneratorGetPathConsistency() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(777L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(777L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getPath(), gen2.getPath());
        }
    }

    @Test
    void testSeededGeneratorGetQueryStringConsistency() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(888L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(888L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getQueryString(), gen2.getQueryString());
        }
    }

    @Test
    void testGenerateWithDomain() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(1L).build());
        String url = generator.generateWithDomain("example.com");
        assertTrue(url.contains("://example.com"));
    }

    @Test
    void testGenerateWithDomainNullThrows() {
        URLGenerator generator = new URLGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithDomain(null));
    }

    @Test
    void testGenerateWithDomainPrefix() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(2L).build());
        String url = generator.generateWithDomainPrefix("api");
        assertTrue(url.contains("://api."));
    }

    @Test
    void testGenerateWithTrailingSlashPathAndExtension() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(3L).build());
        URLGenerator.URLOptions options =
            new URLGenerator.URLOptions("https", "example.com", null, "/", new String[] { "txt" }, false);
        String url = generator.generateWithOptions(options);
        assertTrue(url.startsWith("https://example.com/"));
        assertTrue(url.endsWith(".txt"));
    }

    @Test
    void testGenerateWithDomainPrefixNullThrows() {
        URLGenerator generator = new URLGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithDomainPrefix(null));
    }

    @Test
    void testGenerateWithFixedPath() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(3L).build());
        String url = generator.generateWithFixedPath("/api/v1");
        assertTrue(url.contains("/api/v1"));
    }

    @Test
    void testGenerateWithFixedPathNullThrows() {
        URLGenerator generator = new URLGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithFixedPath(null));
    }

    @Test
    void testGenerateWithFixedPathWithoutLeadingSlash() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(4L).build());
        String url = generator.generateWithFixedPath("api/v1");
        assertTrue(url.contains("/api/v1"));
    }

    @Test
    void testGenerateWithExtensions() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(5L).build());
        String url = generator.generateWithExtensions("gif", "jpg");
        assertTrue(url.matches(".*\\.(gif|jpg)$"));
        assertTrue(url.contains("/"));
    }

    @Test
    void testGenerateWithOptionsComposite() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(6L).build());
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(
            "https", "example.com", "cdn", "/assets/logo", new String[] { "png" }, true
        );
        String url = generator.generateWithOptions(options);
        assertTrue(url.startsWith("https://cdn.example.com/assets/logo/"));
        assertTrue(url.contains(".png?"));
    }

    @Test
    void testGenerateWithOptionsNullThrows() {
        URLGenerator generator = new URLGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateWithOptions(null));
    }

    @Test
    void testGenerateWithOptionsBlankDomainThrows() {
        URLGenerator generator = new URLGenerator();
        URLGenerator.URLOptions options = new URLGenerator.URLOptions("https", " ", null, null, null, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsBlankPathThrows() {
        URLGenerator generator = new URLGenerator();
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(null, null, null, " ", null, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsEmptyExtensionsThrows() {
        URLGenerator generator = new URLGenerator();
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(null, null, null, null, new String[0], false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsBlankExtensionThrows() {
        URLGenerator generator = new URLGenerator();
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(null, null, null, null, new String[] { " " }, false);
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithOptions(options));
    }

    @Test
    void testGenerateWithOptionsNormalizesDotExtensions() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(7L).build());
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(
            "https", "example.com", null, "/download/file", new String[] { ".zip" }, false
        );
        String url = generator.generateWithOptions(options);
        assertTrue(url.contains(".zip"));
        assertTrue(url.contains("%20") || url.contains("-"));
    }

    @Test
    void testGenerateWithOptionsQueryWithoutPathCreatesPath() {
        URLGenerator generator = new URLGenerator(GeneratorConfig.builder().seed(8L).build());
        URLGenerator.URLOptions options = new URLGenerator.URLOptions(
            "https", "example.com", null, null, null, true
        );
        String url = generator.generateWithOptions(options);
        assertTrue(url.contains("/"));
        assertTrue(url.contains("?"));
    }

    @Test
    void testDefaultExtensionsExposed() {
        String[] extensions = URLGenerator.URLOptions.defaultExtensions();
        assertNotNull(extensions);
        assertTrue(extensions.length > 0);
    }

    @Test
    void testUrlOptionsDefaultConstructor() {
        URLGenerator.URLOptions options = new URLGenerator.URLOptions();
        assertNull(options.protocol());
        assertNull(options.domain());
        assertNull(options.domainPrefix());
        assertNull(options.path());
        assertNull(options.extensions());
        assertFalse(options.withQuery());
    }

    @Test
    void testGenerateWithSpecificProtocolConsistentWithSeed() {
        URLGenerator gen1 = new URLGenerator(GeneratorConfig.builder().seed(999L).build());
        URLGenerator gen2 = new URLGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generate("ftp"), gen2.generate("ftp"));
        assertEquals(gen1.generate("ws"), gen2.generate("ws"));
    }

    @Test
    void testAllProtocolsGenerated() {
        URLGenerator generator = new URLGenerator();
        Set<String> protocols = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            String protocol = generator.getProtocol();
            protocols.add(protocol);
        }

        assertTrue(protocols.size() >= 3, "Should generate at least 3 different protocols");
    }

    @Test
    void testQueryValuesAreNumeric() {
        URLGenerator generator = new URLGenerator();
        String query = generator.getQueryString();
        String[] params = query.split("&");

        for (String param : params) {
            String[] parts = param.split("=");
            String value = parts[1];
            assertTrue(value.matches("\\d+"), "Query value should be numeric: " + value);
        }
    }

    @Test
    void testGeneratePathWithSingleSegment() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(1L).build());

        boolean foundSingleSegment = false;
        for (int i = 0; i < 100; i++) {
            String path = gen.getPath();
            int segments = path.split("/").length - 1;
            if (segments == 1) {
                foundSingleSegment = true;
                break;
            }
        }

        assertTrue(foundSingleSegment, "Should generate paths with single segment");
    }

    @Test
    void testGeneratePathWithMultipleSegments() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(2L).build());

        boolean foundMultipleSegments = false;
        for (int i = 0; i < 100; i++) {
            String path = gen.getPath();
            int segments = path.split("/").length - 1;
            if (segments > 1) {
                foundMultipleSegments = true;
                break;
            }
        }

        assertTrue(foundMultipleSegments, "Should generate paths with multiple segments");
    }

    @Test
    void testGenerateQueryWithSingleParam() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(3L).build());

        boolean foundSingleParam = false;
        for (int i = 0; i < 100; i++) {
            String query = gen.getQueryString();
            int params = query.split("&").length;
            if (params == 1) {
                foundSingleParam = true;
                break;
            }
        }

        assertTrue(foundSingleParam, "Should generate queries with single parameter");
    }

    @Test
    void testGenerateQueryWithMultipleParams() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(4L).build());

        boolean foundMultipleParams = false;
        for (int i = 0; i < 100; i++) {
            String query = gen.getQueryString();
            int params = query.split("&").length;
            if (params > 1) {
                foundMultipleParams = true;
                break;
            }
        }

        assertTrue(foundMultipleParams, "Should generate queries with multiple parameters");
    }

    @Test
    void testQueryStringLoopBranchCoverage() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(5555L).build());

        // Generate many queries to ensure loop iterations cover all branches
        for (int i = 0; i < 50; i++) {
            String query = gen.getQueryString();
            assertFalse(query.isEmpty());

            String[] parts = query.split("&");
            assertTrue(parts.length >= 1 && parts.length <= 3,
                       "Query should have 1-3 parameters");
        }
    }

    @Test
    void testPathLoopBranchCoverage() {
        URLGenerator gen = new URLGenerator(GeneratorConfig.builder().seed(6666L).build());

        // Generate many paths to ensure loop iterations cover all branches
        for (int i = 0; i < 50; i++) {
            String path = gen.getPath();
            assertTrue(path.startsWith("/"));

            int segments = path.split("/").length - 1;
            assertTrue(segments >= 1 && segments <= 3,
                       "Path should have 1-3 segments");
        }
    }

    @Test
    void testQueryAndTldAliases() {
        URLGenerator generator = new URLGenerator(Locale.US);
        String query = generator.generateQuery();
        String tld = generator.generateTld();
        assertFalse(query.startsWith("?"));
        assertFalse(query.isBlank());
        assertFalse(tld.isBlank());
    }
}
