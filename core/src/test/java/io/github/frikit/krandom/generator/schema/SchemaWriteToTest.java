/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SchemaWriteToTest {

    private Schema schema;

    @BeforeEach
    void setUp() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", ctx -> "Alice");
        fields.put("age", ctx -> 25);
        schema = new Schema(fields);
    }

    @Test
    @DisplayName("writeTo OutputStream with JSONL format")
    void writeToOutputStreamJsonl() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.JSONL, 3);
        String result = baos.toString(StandardCharsets.UTF_8);
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("{"));
            assertTrue(line.endsWith("}"));
        }
    }

    @Test
    @DisplayName("writeTo OutputStream with CSV format")
    void writeToOutputStreamCsv() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.CSV, 3);
        String result = baos.toString(StandardCharsets.UTF_8);
        String[] lines = result.split("\n");
        // header + 3 data rows
        assertEquals(4, lines.length);
    }

    @Test
    @DisplayName("writeTo OutputStream with XML format")
    void writeToOutputStreamXml() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.XML, 2);
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("<?xml"));
        assertTrue(result.contains("<records>"));
        assertTrue(result.contains("<record>"));
    }

    @Test
    @DisplayName("writeTo OutputStream with SQL format uses default table name")
    void writeToOutputStreamSqlDefaultTable() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.SQL, 2);
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("INSERT INTO \"data\""));
    }

    @Test
    @DisplayName("writeTo OutputStream with SQL format uses custom table name")
    void writeToOutputStreamSqlCustomTable() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.SQL, 2, "users");
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("INSERT INTO \"users\""));
    }

    @Test
    @DisplayName("writeTo Writer with all formats")
    void writeToWriter() throws IOException {
        for (OutputFormat format : OutputFormat.values()) {
            StringWriter writer = new StringWriter();
            schema.writeTo(writer, format, 2, "test_table");
            String result = writer.toString();
            assertFalse(result.isEmpty(), "Output should not be empty for format: " + format);
        }
    }

    @Test
    @DisplayName("writeTo with zero count produces minimal output")
    void writeToZeroCount() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.writeTo(baos, OutputFormat.JSONL, 0);
        String result = baos.toString(StandardCharsets.UTF_8);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("writeTo rejects null OutputStream")
    void writeToRejectsNullOutputStream() {
        assertThrows(NullPointerException.class,
            () -> schema.writeTo((java.io.OutputStream) null, OutputFormat.JSONL, 1));
    }

    @Test
    @DisplayName("writeTo rejects null format")
    void writeToRejectsNullFormat() {
        assertThrows(NullPointerException.class,
            () -> schema.writeTo(new ByteArrayOutputStream(), null, 1));
    }

    @Test
    @DisplayName("writeTo wraps reflective failures from record accessors")
    void writeToWrapsRecordAccessorFailure() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("payload", ctx -> new ThrowingRecord("data"));
        Schema throwingSchema = new Schema(fields);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> throwingSchema.writeTo(new ByteArrayOutputStream(), OutputFormat.JSONL, 1));
        assertTrue(ex.getMessage().contains("Failed to read record component 'value'"));
    }

    private record ThrowingRecord(String value) {
        @Override
        public String value() {
            throw new IllegalStateException("accessor failure");
        }
    }
}
