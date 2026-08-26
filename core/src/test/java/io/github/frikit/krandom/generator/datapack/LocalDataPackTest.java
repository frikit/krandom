/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datapack;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.UniversityData;
import io.github.frikit.krandom.generator.user.UniversityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocalDataPack")
class LocalDataPackTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    @DisplayName("loads a verified, provenance-declared university pack")
    void loadsVerifiedProvenanceDeclaredUniversityPack() throws IOException {
        LocalDataPack pack = writePack(temporaryDirectory,
                                       "name,degree,prefix,suffix,place\n"
                                       + "Northbridge University,BSc,School of,University,Northbridge\n"
                                       + "Riverdale Institute,MSc,Institute of,Institute,Riverdale\n");

        assertEquals(1, pack.formatVersion());
        assertEquals(Locale.forLanguageTag("en-US"), pack.locale());
        assertEquals("Synthetic fixture data", pack.source());
        assertEquals("CC0-1.0", pack.license());
        assertEquals(2, pack.universities().size());
        assertEquals(new UniversityData("Northbridge University", "BSc", "School of", "University", "Northbridge"),
                     pack.universities().getFirst());
        assertArrayEquals(pack.universities().toArray(UniversityData[]::new),
                          pack.universityProvider().getUniversities());
        assertEquals(Locale.forLanguageTag("en-US"), pack.universityProvider().getLocale());
    }

    @Test
    @DisplayName("university data packs are scoped to the configured registry")
    void universityDataPacksAreScopedToConfiguredRegistry() throws IOException {
        LocalDataPack pack = writePack(temporaryDirectory,
                                       "name,degree,prefix,suffix,place\n"
                                       + "Northbridge University,BSc,School of,University,Northbridge\n"
                                       + "Riverdale Institute,MSc,Institute,Institute,Riverdale\n");
        DataRegistryContext context = DataRegistryContext.builder().isolated().registerDataPack(pack).build();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.forLanguageTag("en-US"))
                                                .seed(42L)
                                                .registryContext(context)
                                                .build();

        UniversityGenerator first = new UniversityGenerator(config);
        UniversityGenerator second = new UniversityGenerator(config);

        assertTrue(context.isUniversityRegistered(Locale.forLanguageTag("en-US")));
        assertFalse(DataRegistryContext.builder().isolated().build()
                                            .isUniversityRegistered(Locale.forLanguageTag("en-US")));
        assertEquals(first.generate(), second.generate());
        assertEquals(2, first.getUniversityCount());
        assertEquals(Locale.forLanguageTag("en-US"), first.getLocale());
        assertTrue(first.isLocaleExplicitlySupported());
    }

    @Test
    @DisplayName("rejects checksum mismatches, unsupported versions, and traversal paths")
    void rejectsChecksumMismatchesUnsupportedVersionsAndTraversalPaths() throws IOException {
        writePack(temporaryDirectory,
                  "name,degree,prefix,suffix,place\n"
                  + "Northbridge University,BSc,School of,University,Northbridge\n");
        Path manifest = temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME);

        Files.writeString(manifest, validManifest("universities.csv", "0".repeat(64)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("universities.csv", sha256("header"))
                                    .replace("format.version=1", "format.version=2"));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("../universities.csv", sha256("header")));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("rejects malformed manifests and malformed university rows")
    void rejectsMalformedManifestsAndUniversityRows() throws IOException {
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME), "format.version=1\n");
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Path data = temporaryDirectory.resolve("universities.csv");
        String malformed = "name,degree,prefix,suffix,place\n"
                           + "Northbridge University,BSc,School of,University\n";
        Files.writeString(data, malformed);
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          validManifest("universities.csv", sha256(malformed)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("parses quoted university fields and rejects invalid CSV structures")
    void parsesQuotedUniversityFieldsAndRejectsInvalidCsvStructures() throws IOException {
        LocalDataPack pack = writePack(temporaryDirectory,
                                       "name,degree,prefix,suffix,place\n"
                                       + "\"Northbridge, University\",BSc,\"School of \"\"Arts\"\"\",University,\"Northbridge\"\n");

        assertEquals("Northbridge, University", pack.universities().getFirst().name());
        assertEquals("School of \"Arts\"", pack.universities().getFirst().prefix());

        assertInvalidData("wrong,header\nNorthbridge,BSc,School,University,Northbridge\n");
        assertInvalidData("name,degree,prefix,suffix,place\n");
        assertInvalidData("name,degree,prefix,suffix,place\n\nNorthbridge,BSc,School,University,Northbridge\n");
        assertInvalidData("name,degree,prefix,suffix,place\nNorth\"bridge,BSc,School,University,Northbridge\n");
        assertInvalidData("name,degree,prefix,suffix,place\n\"North\"text,BSc,School,University,Northbridge\n");
        assertInvalidData("name,degree,prefix,suffix,place\n\"Northbridge,BSc,School,University,Northbridge\n");
    }

    @Test
    @DisplayName("rejects invalid locale, provenance, and file declarations")
    void rejectsInvalidLocaleProvenanceAndFileDeclarations() throws IOException {
        String data = "name,degree,prefix,suffix,place\nNorthbridge,BSc,School,University,Northbridge\n";
        Path manifest = temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME);
        Files.writeString(temporaryDirectory.resolve("universities.csv"), data);

        Files.writeString(manifest, validManifest("universities.csv", sha256(data)).replace("locale=en-US", "locale=not_a_tag"));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("universities.csv", sha256(data)).replace("source=Synthetic fixture data", "source= "));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("universities.csv", sha256(data)).replace("license=CC0-1.0", "license=\t"));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("/universities.csv", sha256(data)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest(".", sha256(data)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("..", sha256(data)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.writeString(manifest, validManifest("universities.csv", "short"));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("enforces bounded local data files and rejects missing directories")
    void enforcesBoundedLocalDataFilesAndRejectsMissingDirectories() throws IOException {
        assertThrows(NullPointerException.class, () -> LocalDataPack.load(null));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory.resolve("missing")));

        String data = "name,degree,prefix,suffix,place\n" + "x".repeat(LocalDataPack.MAX_DATA_FILE_BYTES);
        Path file = temporaryDirectory.resolve("universities.csv");
        Files.writeString(file, data);
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          validManifest("universities.csv", sha256(data)));

        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("accepts commented manifests and rejects malformed or non-file pack entries")
    void validatesManifestSyntaxAndRegularFiles() throws IOException {
        String data = "name,degree,prefix,suffix,place\nNorthbridge,BSc,School,University,Northbridge\n";
        Files.writeString(temporaryDirectory.resolve("universities.csv"), data);
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          "# local fixture data\n! comment\n\n" + validManifest("universities.csv", sha256(data)));
        assertEquals(1, LocalDataPack.load(temporaryDirectory).formatVersion());

        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME), "not-a-property\n");
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));

        Files.delete(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME));
        Files.createDirectory(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("rejects a university path that is not a regular file")
    void rejectsUniversityDirectory() throws IOException {
        Files.createDirectory(temporaryDirectory.resolve("universities.csv"));
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          validManifest("universities.csv", "0".repeat(64)));

        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    @Test
    @DisplayName("reports filesystem read failures with the affected file")
    void reportsFilesystemReadFailures() throws IOException {
        Path file = Files.writeString(temporaryDirectory.resolve("unreadable.csv"), "fixture");
        IOException cause = new IOException("read denied");

        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> LocalDataPack.readBounded(file, 100, "fixture file", ignored -> {
                throw cause;
            }));

        assertTrue(failure.getMessage().contains(file.toString()));
        assertSame(cause, failure.getCause());
    }

    @Test
    @DisplayName("reports a Java runtime without the required SHA-256 digest")
    void reportsMissingSha256Digest() {
        NoSuchAlgorithmException cause = new NoSuchAlgorithmException("SHA-256 unavailable");

        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> LocalDataPack.verifySha256(new byte[0], "0".repeat(64), () -> {
                throw cause;
            }));

        assertSame(cause, failure.getCause());
    }

    private void assertInvalidData(String data) throws IOException {
        Files.writeString(temporaryDirectory.resolve("universities.csv"), data);
        Files.writeString(temporaryDirectory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          validManifest("universities.csv", sha256(data)));
        assertThrows(IllegalArgumentException.class, () -> LocalDataPack.load(temporaryDirectory));
    }

    private static LocalDataPack writePack(Path directory, String data) throws IOException {
        Files.writeString(directory.resolve("universities.csv"), data);
        Files.writeString(directory.resolve(LocalDataPack.MANIFEST_FILE_NAME),
                          validManifest("universities.csv", sha256(data)));
        return LocalDataPack.load(directory);
    }

    private static String validManifest(String fileName, String checksum) {
        return "format.version=1\n"
               + "locale=en-US\n"
               + "source=Synthetic fixture data\n"
               + "license=CC0-1.0\n"
               + "university.file=" + fileName + "\n"
               + "university.sha256=" + checksum + "\n";
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                                                          .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(ex);
        }
    }
}
