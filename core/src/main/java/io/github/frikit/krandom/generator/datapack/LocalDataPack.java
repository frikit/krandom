/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datapack;

import io.github.frikit.krandom.generator.user.UniversityData;
import io.github.frikit.krandom.generator.user.UniversityDataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Verified, local-only fixture data pack.
 *
 * <p>A pack is a directory containing {@value #MANIFEST_FILE_NAME} and a University CSV file. The
 * manifest is versioned and declares the pack locale, source, license, file name, and SHA-256
 * checksum. The loader only accepts direct child files of the supplied directory, performs no
 * network access, and limits both manifest and data-file sizes.
 *
 * <p>Register the result with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerDataPack(LocalDataPack)}
 * to make it available only to that configuration.
 */
public final class LocalDataPack {

    @FunctionalInterface
    interface InputStreamOpener {
        InputStream open(Path path) throws IOException;
    }

    @FunctionalInterface
    interface MessageDigestFactory {
        MessageDigest create() throws NoSuchAlgorithmException;
    }

    /** Manifest file name expected in every local data pack. */
    public static final String MANIFEST_FILE_NAME = "krandom-data-pack.properties";

    /** Maximum data file size accepted by the loader. */
    public static final int MAX_DATA_FILE_BYTES = 1_048_576;

    private static final int    FORMAT_VERSION = 1;
    private static final int    MAX_MANIFEST_BYTES = 65_536;
    private static final String UNIVERSITY_HEADER = "name,degree,prefix,suffix,place";

    private final Locale                 locale;
    private final String                 source;
    private final String                 license;
    private final List<UniversityData>   universities;
    private final UniversityDataProvider universityProvider;

    private LocalDataPack(Locale locale, String source, String license, List<UniversityData> universities) {
        this.locale = locale;
        this.source = source;
        this.license = license;
        this.universities = List.copyOf(universities);
        this.universityProvider = new PackUniversityDataProvider(locale, this.universities);
    }

    /**
     * Loads and verifies a data pack directory.
     *
     * @param directory local directory containing the manifest and University CSV file
     * @return verified immutable data pack
     * @throws NullPointerException     if {@code directory} is null
     * @throws IllegalArgumentException if the directory, manifest, checksum, or data is invalid
     */
    public static LocalDataPack load(Path directory) {
        Path root = normalizeDirectory(directory);
        Properties manifest = readManifest(root);
        requireFormatVersion(manifest);
        Locale locale = parseLocale(requireProperty(manifest, "locale"));
        String source = requireProperty(manifest, "source");
        String license = requireProperty(manifest, "license");
        Path universityFile = resolveDirectChild(root, requireProperty(manifest, "university.file"));
        byte[] bytes = readBounded(universityFile, MAX_DATA_FILE_BYTES, "university data file");
        verifySha256(bytes, requireProperty(manifest, "university.sha256"));
        return new LocalDataPack(locale, source, license, parseUniversities(bytes));
    }

    /**
     * Returns the supported manifest format version.
     *
     * @return format version
     */
    public int formatVersion() {
        return FORMAT_VERSION;
    }

    /**
     * Returns the locale declared by this data pack.
     *
     * @return pack locale
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Returns the required provenance source declaration.
     *
     * @return source declaration
     */
    public String source() {
        return source;
    }

    /**
     * Returns the required license declaration.
     *
     * @return license declaration
     */
    public String license() {
        return license;
    }

    /**
     * Returns coherent University fixtures from the verified data file.
     *
     * @return immutable fixture list
     */
    public List<UniversityData> universities() {
        return universities;
    }

    /**
     * Returns the provider to register on a configuration-scoped context.
     *
     * @return University data provider
     */
    public UniversityDataProvider universityProvider() {
        return universityProvider;
    }

    private static Path normalizeDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory must not be null");
        Path root = directory.toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("data pack directory does not exist or is not a directory: " + root);
        }
        return root;
    }

    private static Properties readManifest(Path root) {
        Path manifest = resolveDirectChild(root, MANIFEST_FILE_NAME);
        byte[] bytes = readBounded(manifest, MAX_MANIFEST_BYTES, "data pack manifest");
        Properties properties = new Properties();
        String text = new String(bytes, StandardCharsets.UTF_8);
        for (String line : text.split("\\R", -1)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("data pack manifest entries must use non-blank key=value syntax");
            }
            properties.setProperty(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
        }
        return properties;
    }

    private static Path resolveDirectChild(Path root, String fileName) {
        Path relative = Path.of(fileName);
        if (relative.isAbsolute() || relative.getNameCount() != 1 || fileName.equals(".") || fileName.equals("..")) {
            throw new IllegalArgumentException("data pack file must be a direct child of the pack directory: " + fileName);
        }
        return root.resolve(relative);
    }

    private static byte[] readBounded(Path file, int maximumBytes, String label) {
        return readBounded(file, maximumBytes, label, Files::newInputStream);
    }

    static byte[] readBounded(Path file,
                              int maximumBytes,
                              String label,
                              InputStreamOpener inputStreamOpener) {
        try {
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException(label + " does not exist or is not a regular file: " + file);
            }
            byte[] bytes;
            try (InputStream input = inputStreamOpener.open(file)) {
                bytes = input.readNBytes(maximumBytes + 1);
            }
            if (bytes.length > maximumBytes) {
                throw new IllegalArgumentException(label + " exceeds " + maximumBytes + " bytes: " + file);
            }
            return bytes;
        } catch (IOException ex) {
            throw new IllegalArgumentException("could not read " + label + ": " + file, ex);
        }
    }

    private static void requireFormatVersion(Properties manifest) {
        String rawVersion = requireProperty(manifest, "format.version");
        if (!String.valueOf(FORMAT_VERSION).equals(rawVersion)) {
            throw new IllegalArgumentException("unsupported data pack format.version: " + rawVersion);
        }
    }

    private static String requireProperty(Properties manifest, String key) {
        String value = manifest.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("data pack manifest requires non-blank " + key);
        }
        return value.trim();
    }

    private static Locale parseLocale(String value) {
        Locale locale = Locale.forLanguageTag(value);
        if (locale.equals(Locale.ROOT)) {
            throw new IllegalArgumentException("data pack locale must be a BCP 47 language tag: " + value);
        }
        return locale;
    }

    private static void verifySha256(byte[] bytes, String expected) {
        verifySha256(bytes, expected, () -> MessageDigest.getInstance("SHA-256"));
    }

    static void verifySha256(byte[] bytes, String expected, MessageDigestFactory messageDigestFactory) {
        if (!expected.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("university.sha256 must be a 64-character hexadecimal SHA-256 digest");
        }
        String actual;
        try {
            actual = HexFormat.of().formatHex(messageDigestFactory.create().digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 must be available in every Java runtime", ex);
        }
        if (!actual.equalsIgnoreCase(expected)) {
            throw new IllegalArgumentException("university data file checksum does not match university.sha256");
        }
    }

    private static List<UniversityData> parseUniversities(byte[] bytes) {
        String data = new String(bytes, StandardCharsets.UTF_8);
        String[] rows = data.split("\\R", -1);
        if (!UNIVERSITY_HEADER.equals(rows[0])) {
            throw new IllegalArgumentException("university data must start with header: " + UNIVERSITY_HEADER);
        }
        List<UniversityData> universities = new ArrayList<>();
        for (int index = 1; index < rows.length; index++) {
            String row = rows[index];
            if (row.isBlank() && index == rows.length - 1) {
                continue;
            }
            List<String> columns = parseCsvRow(row, index + 1);
            if (columns.size() != 5) {
                throw new IllegalArgumentException("university row " + (index + 1) + " must contain 5 columns");
            }
            universities.add(new UniversityData(columns.get(0).trim(), columns.get(1).trim(), columns.get(2).trim(),
                                                columns.get(3).trim(), columns.get(4).trim()));
        }
        if (universities.isEmpty()) {
            throw new IllegalArgumentException("university data must contain at least one fixture row");
        }
        return universities;
    }

    private static List<String> parseCsvRow(String row, int lineNumber) {
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        boolean afterQuotedValue = false;
        for (int index = 0; index < row.length(); index++) {
            char character = row.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    value.append(character);
                    index++;
                } else if (value.isEmpty() || quoted) {
                    quoted = !quoted;
                    afterQuotedValue = !quoted;
                } else {
                    throw new IllegalArgumentException("university row " + lineNumber + " has an unescaped quote");
                }
            } else if (character == ',') {
                if (quoted) {
                    value.append(character);
                } else {
                    columns.add(value.toString());
                    value.setLength(0);
                    afterQuotedValue = false;
                }
            } else if (afterQuotedValue) {
                throw new IllegalArgumentException("university row " + lineNumber + " has text after a quoted value");
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("university row " + lineNumber + " has an unterminated quoted value");
        }
        columns.add(value.toString());
        return columns;
    }

    private static final class PackUniversityDataProvider implements UniversityDataProvider {

        private final Locale           locale;
        private final UniversityData[] universities;

        private PackUniversityDataProvider(Locale locale, List<UniversityData> universities) {
            this.locale = locale;
            this.universities = universities.toArray(UniversityData[]::new);
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public UniversityData[] getUniversities() {
            return universities.clone();
        }
    }
}
