/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Declarative schema-based record generator.
 *
 * <p>A record maps each column, in the order of the fields, to its value, which is {@code null} where the
 * column's provider returns {@code null}, as a parsed property of type {@code null} always does and one
 * with OpenAPI's {@code nullable} does half of the time. {@link #nextRecord()} and
 * {@link #nextRecords(int)} type a record as {@code Map<String, @Nullable Object>}, which says so.
 * {@link #generate()}, {@link #generateBatch(int)}, and {@link #loop(int)} return the same records as
 * {@code Map<String, Object>}, which claims values that are never {@code null}, and are deprecated. As a
 * {@code Generator<Map<String, Object>>}, a schema keeps that type in the methods it inherits, such as
 * {@code stream()}; {@code schema::nextRecord} is a {@code Generator<Map<String, @Nullable Object>>}
 * of the same records. Every one of these methods draws the schema's next records from one sequence.
 *
 * <p>When supplied a portable seeded {@link GeneratorConfig}, every value receives a named child
 * stream derived from its record index and field name. Adding an unrelated schema column therefore
 * does not perturb existing column values. A {@link Field} reference and the {@code #} and {@code ?} of a
 * {@link Field} template make a generator for each value, seeded from that stream, unless the field's own
 * configuration uses a caller-owned, factory-made, or secure random source. Caller-owned, secure, and
 * unseeded random sources retain their documented sequential behavior.
 *
 * <p>Every writer writes a date or time as the RFC 3339 text of the format its type exports, with the
 * seconds always and a fraction only where it is not zero: a {@code ZonedDateTime} with its offset and no
 * zone ID, an {@code Instant} with {@code Z}, a {@code java.util.Date} as the instant it holds, a
 * {@code Calendar} at the offset its own time zone gives that instant, and a {@code java.sql.Date} or
 * {@code Time} as its date or time of day. A year outside 0000 to 9999, an offset with seconds or beyond
 * 23:59, and a Julian day of a {@code java.sql.Date} that ISO does not have, which RFC 3339 cannot write,
 * are refused with an {@link IllegalArgumentException}.
 *
 * <p>Every writer writes text that UTF-8 can encode. An unpaired surrogate, half of a UTF-16 surrogate pair
 * without its other half, is written as U+FFFD wherever it is, in a value, a key, a CSV header, or a table or
 * column name, and a pair as the character it encodes.
 */
public final class Schema implements Generator<Map<String, Object>> {

    private static final String JSON_SCHEMA_DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";
    static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    static final String DEFAULT_XML_ROOT_ELEMENT = "records";
    static final String DEFAULT_XML_RECORD_ELEMENT = "record";
    static final char NEWLINE = '\n';

    /** The first and last second RFC 3339 can write, whose years have four digits. */
    private static final long FIRST_SECOND =
            LocalDate.of(0, 1, 1).atStartOfDay().toEpochSecond(ZoneOffset.UTC);

    private static final long LAST_SECOND =
            LocalDate.of(9999, 12, 31).atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);

    /**
     * Plain scalars of letters, digits, {@code _} and {@code -} that YAML 1.1 or 1.2 reads as another type
     * than a string, in any letter case: a boolean or null word; an integer, including YAML 1.1's {@code _}
     * separators, octal, hexadecimal and binary forms, and YAML 1.2's {@code 0o} octal; a float with an
     * exponent; and a date.
     */
    private static final Pattern YAML_NON_STRING = Pattern.compile("(?i)y|n|yes|no|true|false|on|off|null"
            + "|[-+]?(?:[0-9][0-9_]*|0x[0-9a-f_]+|0o[0-7]+|0b[01_]+|[0-9][0-9_]*e[-+]?[0-9]+)"
            + "|[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}");

    private final GeneratorConfig config;
    private final Map<String, SchemaValueProvider> fields;
    private final Random random;
    private final Optional<GenerationRecipe> generationRecipe;
    private final @org.jspecify.annotations.Nullable RecordRule recordRule;
    private int nextRecordIndex;

    @FunctionalInterface
    interface StringBuilderWriter {

        void write(StringBuilder builder) throws IOException;
    }

    /**
     * A rule over whole records, which a parsed root const or enum sets. As every record that the schema gives
     * then meets the rule, the rule also gives the JSON Schema of each column's values, which the column's
     * provider alone need not give.
     */
    interface RecordRule {

        /**
         * The record to keep: the generated one where it meets the rule, and otherwise one that does, drawing
         * from the random source given where it needs a draw.
         */
        Map<String, @Nullable Object> apply(Map<String, @Nullable Object> record, Supplier<Random> random);

        /** The JSON Schema of a column's values in the records that the rule keeps. */
        Map<String, ?> columnSchema(String column);
    }

    /**
     * Creates schema generator with default configuration.
     *
     * @param fields schema field mapping
     */
    public Schema(Map<String, SchemaValueProvider> fields) {
        this(GeneratorConfig.defaults(), fields);
    }

    /**
     * Creates schema generator for a specific locale.
     *
     * @param locale locale for generators
     * @param fields schema field mapping
     */
    public Schema(Locale locale, Map<String, SchemaValueProvider> fields) {
        this(GeneratorConfig.builder().locale(locale).build(), fields);
    }

    /**
     * Creates schema generator with explicit configuration.
     *
     * @param config generator config
     * @param fields schema field mapping
     */
    public Schema(GeneratorConfig config, Map<String, SchemaValueProvider> fields) {
        this(config, fields, null);
    }

    /** A schema whose records also keep to a rule over the whole record, such as a parsed root const or enum. */
    Schema(
            GeneratorConfig config,
            Map<String, SchemaValueProvider> fields,
            @org.jspecify.annotations.Nullable RecordRule recordRule) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(fields, "fields must not be null");
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("fields must not be empty");
        }
        this.fields = validateAndCopy(fields);
        this.random = config.createRandom();
        this.generationRecipe = config.getGenerationRecipe();
        this.recordRule = recordRule;
        this.nextRecordIndex = 0;
    }

    private static Map<String, SchemaValueProvider> validateAndCopy(Map<String, SchemaValueProvider> fields) {
        Map<String, SchemaValueProvider> copy = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("field names must be non-blank");
            }
            SchemaValueProvider provider = entry.getValue();
            if (provider == null) {
                throw new IllegalArgumentException("provider for field '" + name + "' must not be null");
            }
            copy.put(name, provider);
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Generates the next record.
     *
     * @return the next record
     * @deprecated The record's type claims values that are never {@code null}, but a value is {@code null}
     *     where its provider returns {@code null}. Use {@link #nextRecord()}, which returns the same record
     *     typed {@code Map<String, @Nullable Object>}.
     */
    @Deprecated
    @Override
    public Map<String, Object> generate() {
        return legacyRecord(nextRecord());
    }

    /**
     * Generates the next record: each column, in the order of the fields, mapped to its value, which is
     * {@code null} where the column's provider returns {@code null}.
     *
     * @return the next record
     */
    public Map<String, @Nullable Object> nextRecord() {
        return generateAtIndex(nextRecordIndex++);
    }

    /**
     * Generates a batch of schema records.
     *
     * @param count record count
     * @return generated record list
     * @deprecated The records' type claims values that are never {@code null}, but a value is {@code null}
     *     where its provider returns {@code null}. Use {@link #nextRecords(int)}, which returns the same
     *     records typed {@code Map<String, @Nullable Object>}.
     */
    @Deprecated
    public List<Map<String, Object>> generateBatch(int count) {
        return legacyRecords(nextRecords(count));
    }

    /**
     * Alias for {@link #generateBatch(int)}.
     *
     * @param iterations number of records
     * @return generated record list
     * @deprecated The records' type claims values that are never {@code null}, but a value is {@code null}
     *     where its provider returns {@code null}. Use {@link #nextRecords(int)}, which returns the same
     *     records typed {@code Map<String, @Nullable Object>}.
     */
    @Deprecated
    public List<Map<String, Object>> loop(int iterations) {
        return legacyRecords(nextRecords(iterations));
    }

    /**
     * Generates the next {@code count} records, each as {@link #nextRecord()} generates it.
     *
     * @param count record count
     * @return the records in the order generated, in a new list
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public List<Map<String, @Nullable Object>> nextRecords(int count) {
        validateCount(count);
        List<Map<String, @Nullable Object>> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            records.add(nextRecord());
        }
        return records;
    }

    // The deprecated methods keep their 3.0 type, which claims values that are never null. The records they
    // return are the ones the methods replacing them return, null values included, as they always were.
    @SuppressWarnings("NullAway")
    static Map<String, Object> legacyRecord(Map<String, @Nullable Object> record) {
        return record;
    }

    @SuppressWarnings("NullAway")
    private static List<Map<String, Object>> legacyRecords(List<Map<String, @Nullable Object>> records) {
        return records;
    }

    /**
     * Renders generated records as newline-delimited JSON.
     *
     * @param count record count
     * @return JSONL payload
     */
    public String toJsonLines(int count) {
        return buildString(builder -> writeJsonLines(builder, count));
    }

    /**
     * Writes generated records as newline-delimited JSON without materializing a batch list.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeJsonLines(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);
        for (int i = 0; i < count; i++) {
            appendJsonObject(out, nextRecord());
            out.append(NEWLINE);
        }
    }

    /**
     * Renders generated records as a JSON array.
     *
     * @param count record count
     * @return JSON payload
     */
    public String toJson(int count) {
        return buildString(builder -> writeJson(builder, count));
    }

    /**
     * Writes generated records as a JSON array without materializing a batch list.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeJson(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);
        out.append('[');
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                out.append(',');
            }
            appendJsonObject(out, nextRecord());
        }
        out.append(']').append(NEWLINE);
    }

    /**
     * Renders generated records as a YAML sequence.
     *
     * @param count record count
     * @return YAML payload
     */
    public String toYaml(int count) {
        return buildString(builder -> writeYaml(builder, count));
    }

    /**
     * Writes generated records as a YAML sequence without materializing a batch list.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeYaml(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);
        if (count == 0) {
            out.append("[]").append(NEWLINE);
            return;
        }
        for (int i = 0; i < count; i++) {
            appendYamlListItem(out, nextRecord(), 0);
        }
    }

    /**
     * Renders generated records as a TOML array of {@code records} tables.
     *
     * @param count record count
     * @return TOML payload
     */
    public String toToml(int count) {
        return buildString(builder -> writeToml(builder, count));
    }

    /**
     * Writes generated records as a TOML array of {@code records} tables without materializing a
     * batch list. TOML has no representation for {@code null}, so null values are rejected.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeToml(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);
        for (int i = 0; i < count; i++) {
            appendTomlRecord(out, nextRecord());
        }
    }

    /**
     * Renders generated records as CSV.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param count record count
     * @return CSV payload with a header row
     */
    public String toCsv(int count) {
        return toCsv(count, CsvFormulaPolicy.PRESERVE);
    }

    /**
     * Renders generated records as CSV, applying a spreadsheet formula policy to every cell.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param count         record count
     * @param formulaPolicy how cells that spreadsheets would evaluate as formulas are written
     * @return CSV payload with a header row
     */
    public String toCsv(int count, CsvFormulaPolicy formulaPolicy) {
        return buildString(builder -> writeCsv(builder, count, formulaPolicy));
    }

    /**
     * Writes generated records as CSV without materializing a batch list.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeCsv(Appendable out, int count) throws IOException {
        writeCsv(out, count, CsvFormulaPolicy.PRESERVE);
    }

    /**
     * Writes generated records as CSV without materializing a batch list, applying a spreadsheet
     * formula policy to every cell.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param out           appendable destination
     * @param count         record count
     * @param formulaPolicy how cells that spreadsheets would evaluate as formulas are written
     * @throws IOException if the appendable fails
     */
    public void writeCsv(Appendable out, int count, CsvFormulaPolicy formulaPolicy) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Objects.requireNonNull(formulaPolicy, "formulaPolicy must not be null");
        validateCount(count);

        List<String> columns = new ArrayList<>(fields.keySet());
        appendCsvRow(out, columns, formulaPolicy);
        out.append(NEWLINE);

        for (int i = 0; i < count; i++) {
            Map<String, @Nullable Object> record = nextRecord();
            List<String> row = new ArrayList<>(columns.size());
            for (String column : columns) {
                row.add(toCsvCell(record.get(column)));
            }
            appendCsvRow(out, row, formulaPolicy);
            out.append(NEWLINE);
        }
    }

    /**
     * Renders generated records as XML.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON text nodes.
     *
     * @param count record count
     * @return XML payload
     */
    public String toXml(int count) {
        return buildString(builder -> writeXml(builder, count));
    }

    /**
     * Writes generated records as XML without materializing a batch list.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON text nodes.
     * Characters that XML 1.0 cannot represent are written as U+FFFD.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeXml(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);

        out.append(XML_DECLARATION).append(NEWLINE);
        if (count == 0) {
            out.append('<').append(DEFAULT_XML_ROOT_ELEMENT).append("/>").append(NEWLINE);
            return;
        }

        out.append('<').append(DEFAULT_XML_ROOT_ELEMENT).append('>').append(NEWLINE);
        for (int i = 0; i < count; i++) {
            Map<String, @Nullable Object> record = nextRecord();
            appendIndent(out, 2);
            out.append('<').append(DEFAULT_XML_RECORD_ELEMENT).append('>').append(NEWLINE);
            for (Map.Entry<String, @Nullable Object> field : record.entrySet()) {
                appendXmlField(out, field.getKey(), field.getValue(), 4);
            }
            appendIndent(out, 2);
            out.append("</").append(DEFAULT_XML_RECORD_ELEMENT).append('>').append(NEWLINE);
        }
        out.append("</").append(DEFAULT_XML_ROOT_ELEMENT).append('>').append(NEWLINE);
    }

    /**
     * Renders generated records as SQL {@code INSERT} statements.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON string literals.
     *
     * @param tableName target table name
     * @param count     record count
     * @return SQL payload
     */
    public String toSqlInserts(String tableName, int count) {
        return buildString(builder -> writeSqlInserts(builder, tableName, count));
    }

    /**
     * Writes generated records as SQL {@code INSERT} statements without materializing a batch list.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON string literals.
     *
     * @param out       appendable destination
     * @param tableName target table name
     * @param count     record count
     * @throws IOException if the appendable fails
     */
    public void writeSqlInserts(Appendable out, String tableName, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);

        List<String> columns = new ArrayList<>(fields.keySet());
        String normalizedTableName = requireSqlIdentifier(tableName, "tableName");
        for (int i = 0; i < count; i++) {
            Map<String, @Nullable Object> record = nextRecord();
            out.append("INSERT INTO ");
            appendSqlIdentifier(out, normalizedTableName);
            out.append(" (");
            for (int col = 0; col < columns.size(); col++) {
                if (col > 0) {
                    out.append(", ");
                }
                appendSqlColumn(out, columns.get(col));
            }
            out.append(") VALUES (");
            for (int col = 0; col < columns.size(); col++) {
                if (col > 0) {
                    out.append(", ");
                }
                appendSqlValue(out, record.get(columns.get(col)));
            }
            out.append(");").append(NEWLINE);
        }
    }

    /**
     * Writes generated records directly to an {@link OutputStream} in the specified format
     * with bounded memory. Records are generated and flushed incrementally, enabling
     * million-row dataset generation without OOM.
     *
     * <p>For {@link OutputFormat#SQL}, the table name defaults to {@code "data"}.
     * Use {@link #writeTo(OutputStream, OutputFormat, int, String)} to specify a custom table name.
     *
     * @param out    output stream to write to (UTF-8 encoding)
     * @param format output format
     * @param count  number of records to generate
     * @throws IOException if writing fails
     */
    public void writeTo(OutputStream out, OutputFormat format, int count) throws IOException {
        writeTo(out, format, count, "data");
    }

    /**
     * Writes generated records directly to an {@link OutputStream} in the specified format
     * with bounded memory, using the given table name for SQL output.
     *
     * @param out       output stream to write to (UTF-8 encoding)
     * @param format    output format
     * @param count     number of records to generate
     * @param tableName table name for SQL output (ignored for other formats)
     * @throws IOException if writing fails
     */
    public void writeTo(OutputStream out, OutputFormat format, int count, String tableName) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writeTo(writer, format, count, tableName);
        writer.flush();
    }

    /**
     * Writes generated records directly to a {@link Writer} in the specified format
     * with bounded memory.
     *
     * @param writer    writer to write to
     * @param format    output format
     * @param count     number of records to generate
     * @param tableName table name for SQL output (ignored for other formats)
     * @throws IOException if writing fails
     */
    public void writeTo(Writer writer, OutputFormat format, int count, String tableName) throws IOException {
        Objects.requireNonNull(writer, "writer must not be null");
        Objects.requireNonNull(format, "format must not be null");
        if (format == OutputFormat.JSONL) {
            writeJsonLines(writer, count);
        } else if (format == OutputFormat.JSON) {
            writeJson(writer, count);
        } else if (format == OutputFormat.CSV) {
            writeCsv(writer, count);
        } else if (format == OutputFormat.XML) {
            writeXml(writer, count);
        } else if (format == OutputFormat.SQL) {
            writeSqlInserts(writer, tableName, count);
        } else if (format == OutputFormat.YAML) {
            writeYaml(writer, count);
        } else {
            writeToml(writer, count);
        }
    }

    /**
     * Returns configured locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns immutable schema field mapping.
     *
     * @return schema fields
     */
    public Map<String, SchemaValueProvider> getFields() {
        return fields;
    }

    /**
     * Exports this schema definition to a JSON Schema-like map.
     *
     * <p>Types are read from provider metadata so exporting the schema does not consume
     * generator state. Providers without metadata export as unconstrained JSON Schema fragments.
     *
     * @return JSON Schema document as a nested map
     */
    public Map<String, Object> toJsonSchema() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("$schema", JSON_SCHEMA_DRAFT_2020_12);
        root.put("type", "object");
        root.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>(fields.size());

        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String field = entry.getKey();
            required.add(field);
            try {
                // A rule over whole records can give a column values that its provider does not.
                properties.put(
                        field,
                        JsonSchemaSupport.copyJsonSchema(
                                recordRule == null ? entry.getValue().jsonSchema() : recordRule.columnSchema(field)));
            } catch (RuntimeException ex) {
                throw new SchemaGenerationException(
                        field, -1, GenerationFailureCategory.SCHEMA_METADATA, GenerationOperation.EXPORT_SCHEMA, ex);
            }
        }

        root.put("properties", Collections.unmodifiableMap(properties));
        root.put("required", Collections.unmodifiableList(required));
        return Collections.unmodifiableMap(root);
    }

    private Map<String, @Nullable Object> generateAtIndex(int recordIndex) {
        Map<String, @Nullable Object> record = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String name = entry.getKey();
            SchemaValueProvider provider = entry.getValue();
            try {
                SchemaContext context =
                        new SchemaContext(config.getLocale(), randomForField(recordIndex, name), recordIndex);
                record.put(name, provider.generate(context));
            } catch (RuntimeException ex) {
                throw new SchemaGenerationException(
                        name,
                        recordIndex,
                        ex,
                        generationRecipe
                                .map(GenerationRecipe::serializeForDiagnostics)
                                .orElse(null));
            }
        }
        // A rule over the whole record draws from the stream of no column, as a column name is never blank.
        return recordRule == null ? record : recordRule.apply(record, () -> randomForField(recordIndex, ""));
    }

    private Random randomForField(int recordIndex, String fieldName) {
        if (generationRecipe.isEmpty()) {
            return random;
        }
        // The source that the recipe's childRandom gives for this identity, marked as a named stream.
        return new FieldStream(GenerationRecipe.deriveChildSeed(
                generationRecipe.orElseThrow().getSeed(), schemaFieldStreamIdentity(recordIndex, fieldName)));
    }

    /**
     * The named stream of one value of a schema with a portable recipe, which this type marks, so that a
     * provider that makes a generator for each value, such as a {@link Field} reference, seeds it from here.
     */
    static final class FieldStream extends Random {

        private static final long serialVersionUID = 1L;

        FieldStream(long seed) {
            super(seed);
        }
    }

    private static String schemaFieldStreamIdentity(int recordIndex, String fieldName) {
        return "schema|record=" + recordIndex + "|field=" + fieldName.length() + ':' + fieldName;
    }

    private static void validateCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
    }

    static void appendIndent(Appendable out, int spaces) throws IOException {
        for (int i = 0; i < spaces; i++) {
            out.append(' ');
        }
    }

    static void appendXmlField(Appendable out, String fieldName, @Nullable Object value, int indent)
            throws IOException {
        appendIndent(out, indent);
        String elementName = isValidXmlElementName(fieldName) ? fieldName : "field";
        boolean useNameAttribute = !elementName.equals(fieldName);

        out.append('<').append(elementName);
        if (useNameAttribute) {
            out.append(" name=\"");
            appendXmlAttribute(out, fieldName);
            out.append('"');
        }
        if (value == null) {
            out.append("/>").append(NEWLINE);
            return;
        }
        out.append('>');
        appendXmlValue(out, value);
        out.append("</").append(elementName).append('>').append(NEWLINE);
    }

    private static void appendXmlValue(Appendable out, Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value instanceof CharSequence
                || value instanceof Character
                || value instanceof Number
                || value instanceof Boolean) {
            appendXmlText(out, String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?>) {
            StringBuilder builder = new StringBuilder();
            appendJsonValue(builder, value);
            appendXmlText(out, builder.toString());
            return;
        }
        appendXmlText(out, String.valueOf(value));
    }

    private static void appendXmlText(Appendable out, String value) throws IOException {
        appendXmlEscaped(out, value, false);
    }

    private static void appendXmlAttribute(Appendable out, String value) throws IOException {
        appendXmlEscaped(out, value, true);
    }

    /**
     * Escapes text for XML 1.0. Characters XML cannot represent (most control characters,
     * U+FFFE, U+FFFF and unpaired surrogates) become U+FFFD so the document stays well-formed.
     * Carriage returns, and tabs and newlines inside attributes, are written as character
     * references because parsers would otherwise normalize them away.
     */
    private static void appendXmlEscaped(Appendable out, String value, boolean attribute) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isHighSurrogate(ch)
                    && i + 1 < value.length()
                    && Character.isLowSurrogate(value.charAt(i + 1))) {
                out.append(ch).append(value.charAt(++i));
                continue;
            }
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append(attribute ? "&quot;" : "\"");
                case '\'' -> out.append(attribute ? "&apos;" : "'");
                case '\r' -> out.append("&#13;");
                case '\n' -> out.append(attribute ? "&#10;" : "\n");
                case '\t' -> out.append(attribute ? "&#9;" : "\t");
                default -> out.append(isXmlCharacter(ch) ? ch : '�');
            }
        }
    }

    private static boolean isXmlCharacter(char ch) {
        return ch >= 0x20 && !Character.isSurrogate(ch) && ch != '￾' && ch != '￿';
    }

    private static boolean isValidXmlElementName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        char first = value.charAt(0);
        if (!(Character.isLetter(first) || first == '_')) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.')) {
                return false;
            }
        }
        return true;
    }

    static String requireSqlIdentifier(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        String[] parts = value.split("\\.", -1);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new IllegalArgumentException(label + " must not contain blank identifier segments");
            }
        }
        return value;
    }

    /** Writes a possibly schema-qualified table name: {@code crm.customers} becomes two identifiers. */
    static void appendSqlIdentifier(Appendable out, String identifier) throws IOException {
        String[] parts = identifier.split("\\.", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append('.');
            }
            appendSqlColumn(out, parts[i]);
        }
    }

    /** Writes a column name as one quoted identifier, even when it contains dots. */
    static void appendSqlColumn(Appendable out, String column) throws IOException {
        column = encodable(column);
        out.append('"');
        for (int j = 0; j < column.length(); j++) {
            char ch = column.charAt(j);
            if (ch == '"') {
                out.append("\"\"");
            } else {
                out.append(ch);
            }
        }
        out.append('"');
    }

    static void appendSqlValue(Appendable out, @org.jspecify.annotations.Nullable Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value == null) {
            out.append("NULL");
            return;
        }
        if (value instanceof Number number) {
            appendSqlNumber(out, number);
            return;
        }
        if (value instanceof Boolean bool) {
            out.append(bool ? "TRUE" : "FALSE");
            return;
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?>) {
            StringBuilder builder = new StringBuilder();
            appendJsonValue(builder, value);
            appendSqlStringLiteral(out, builder.toString());
            return;
        }
        appendSqlStringLiteral(out, String.valueOf(value));
    }

    private static void appendSqlStringLiteral(Appendable out, String value) throws IOException {
        value = encodable(value);
        out.append('\'');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\'') {
                out.append("''");
            } else {
                out.append(ch);
            }
        }
        out.append('\'');
    }

    static void appendJsonValue(Appendable out, @org.jspecify.annotations.Nullable Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value == null) {
            out.append("null");
            return;
        }
        if (value instanceof CharSequence text) {
            appendJsonString(out, text.toString());
            return;
        }
        if (value instanceof Character ch) {
            appendJsonString(out, String.valueOf(ch));
            return;
        }
        if (value instanceof Number number) {
            appendJsonNumber(out, number);
            return;
        }
        if (value instanceof Boolean) {
            out.append(String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> map) {
            appendJsonObject(out, map);
            return;
        }
        if (value instanceof Iterable<?> items) {
            appendJsonArray(out, items);
            return;
        }
        appendJsonString(out, String.valueOf(value));
    }

    private static void appendJsonNumber(Appendable out, Number value) throws IOException {
        if (value instanceof Double doubleValue) {
            appendFiniteJsonNumber(out, Double.isFinite(doubleValue), value);
            return;
        }
        if (value instanceof Float floatValue) {
            appendFiniteJsonNumber(out, Float.isFinite(floatValue), value);
            return;
        }
        out.append(String.valueOf(value));
    }

    private static void appendSqlNumber(Appendable out, Number value) throws IOException {
        if (value instanceof Double doubleValue) {
            appendFiniteSqlNumber(out, Double.isFinite(doubleValue), value);
            return;
        }
        if (value instanceof Float floatValue) {
            appendFiniteSqlNumber(out, Float.isFinite(floatValue), value);
            return;
        }
        out.append(String.valueOf(value));
    }

    private static void appendFiniteJsonNumber(Appendable out, boolean finite, Number value) throws IOException {
        if (!finite) {
            throw new IllegalArgumentException("JSON does not support non-finite numbers: " + value);
        }
        out.append(String.valueOf(value));
    }

    private static void appendFiniteSqlNumber(Appendable out, boolean finite, Number value) throws IOException {
        if (!finite) {
            throw new IllegalArgumentException("SQL does not support non-finite numbers: " + value);
        }
        out.append(String.valueOf(value));
    }

    static void appendJsonObject(Appendable out, Map<?, ?> value) throws IOException {
        out.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (!first) {
                out.append(',');
            }
            appendJsonString(out, String.valueOf(entry.getKey()));
            out.append(':');
            appendJsonValue(out, entry.getValue());
            first = false;
        }
        out.append('}');
    }

    private static void appendJsonArray(Appendable out, Iterable<?> values) throws IOException {
        out.append('[');
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                out.append(',');
            }
            appendJsonValue(out, value);
            first = false;
        }
        out.append(']');
    }

    static void appendJsonString(Appendable out, String value) throws IOException {
        appendQuoted(out, value, false);
    }

    /**
     * Writes a TOML basic string. TOML reads the same escapes as JSON, but also forbids U+007F raw, which
     * JSON allows, so that one is escaped too.
     */
    private static void appendTomlString(Appendable out, String value) throws IOException {
        appendQuoted(out, value, true);
    }

    private static void appendQuoted(Appendable out, String value, boolean escapeDelete) throws IOException {
        value = encodable(value);
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (ch < 0x20 || (escapeDelete && ch == 0x7F)) {
                        appendJsonUnicodeEscape(out, ch);
                    } else {
                        out.append(ch);
                    }
                }
            }
        }
        out.append('"');
    }

    /**
     * The text that every writer writes for a string: the string, with each unpaired surrogate, half of a
     * UTF-16 surrogate pair without its other half, as U+FFFD, so that UTF-8 can encode the document, and
     * each pair kept as the character it encodes. A writer cannot escape such a half instead, as TOML has no
     * escape for a surrogate and strict JSON parsers reject a lone one. XML writes U+FFFD for it too.
     */
    private static String encodable(String value) {
        StringBuilder replaced = null;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isHighSurrogate(ch)
                    && i + 1 < value.length()
                    && Character.isLowSurrogate(value.charAt(i + 1))) {
                i++;
            } else if (Character.isSurrogate(ch)) {
                if (replaced == null) {
                    replaced = new StringBuilder(value);
                }
                replaced.setCharAt(i, '\uFFFD');
            }
        }
        return replaced == null ? value : replaced.toString();
    }

    private static void appendJsonUnicodeEscape(Appendable out, char ch) throws IOException {
        out.append("\\u");
        out.append(Character.forDigit((ch >> 12) & 0xF, 16));
        out.append(Character.forDigit((ch >> 8) & 0xF, 16));
        out.append(Character.forDigit((ch >> 4) & 0xF, 16));
        out.append(Character.forDigit(ch & 0xF, 16));
    }

    static void appendYamlListItem(Appendable out, Object value, int indent) throws IOException {
        Object normalized = normalizeStructuredValue(value);
        appendIndent(out, indent);
        out.append("- ");
        if (normalized instanceof Map<?, ?> map) {
            appendYamlMapAfterListMarker(out, map, indent);
            return;
        }
        if (normalized instanceof Iterable<?> values) {
            if (!values.iterator().hasNext()) {
                out.append("[]").append(NEWLINE);
                return;
            }
            out.append(NEWLINE);
            for (Object item : values) {
                appendYamlListItem(out, item, indent + 2);
            }
            return;
        }
        appendJsonValue(out, normalized);
        out.append(NEWLINE);
    }

    private static void appendYamlMapAfterListMarker(Appendable out, Map<?, ?> map, int listIndent) throws IOException {
        if (map.isEmpty()) {
            out.append("{}").append(NEWLINE);
            return;
        }
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (first) {
                appendYamlKey(out, entry.getKey());
                appendYamlMapValue(out, entry.getValue(), listIndent + 2);
                first = false;
            } else {
                appendIndent(out, listIndent + 2);
                appendYamlKey(out, entry.getKey());
                appendYamlMapValue(out, entry.getValue(), listIndent + 2);
            }
        }
    }

    private static void appendYamlMap(Appendable out, Map<?, ?> map, int indent) throws IOException {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            appendIndent(out, indent);
            appendYamlKey(out, entry.getKey());
            appendYamlMapValue(out, entry.getValue(), indent);
        }
    }

    private static void appendYamlKey(Appendable out, Object key) throws IOException {
        String value = String.valueOf(key);
        if (isPlainYamlKey(value)) {
            out.append(value);
        } else {
            appendJsonString(out, value);
        }
        out.append(':');
    }

    /**
     * Whether a key can be written plain and read back as the same string. Only letters, digits, {@code _}
     * and {@code -} are plain, and not a key that YAML 1.1 or 1.2 reads as another type.
     */
    private static boolean isPlainYamlKey(String value) {
        if (value.isEmpty() || YAML_NON_STRING.matcher(value).matches()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    private static void appendYamlMapValue(Appendable out, Object value, int parentIndent) throws IOException {
        Object normalized = normalizeStructuredValue(value);
        if (normalized instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                out.append(" {}").append(NEWLINE);
                return;
            }
            out.append(NEWLINE);
            appendYamlMap(out, map, parentIndent + 2);
            return;
        }
        if (normalized instanceof Iterable<?> values) {
            if (!values.iterator().hasNext()) {
                out.append(" []").append(NEWLINE);
                return;
            }
            out.append(NEWLINE);
            for (Object item : values) {
                appendYamlListItem(out, item, parentIndent + 2);
            }
            return;
        }
        out.append(' ');
        appendJsonValue(out, normalized);
        out.append(NEWLINE);
    }

    static void appendTomlRecord(Appendable out, Map<String, @Nullable Object> record) throws IOException {
        out.append("[[records]]").append(NEWLINE);
        for (Map.Entry<String, @Nullable Object> entry : record.entrySet()) {
            appendTomlKey(out, entry.getKey());
            out.append(" = ");
            appendTomlValue(out, entry.getValue());
            out.append(NEWLINE);
        }
    }

    private static void appendTomlValue(Appendable out, @Nullable Object value) throws IOException {
        Object normalized = normalizeStructuredValue(value);
        if (normalized == null) {
            throw new IllegalArgumentException("TOML does not support null values");
        }
        if (normalized instanceof CharSequence text) {
            appendTomlString(out, text.toString());
            return;
        }
        if (normalized instanceof Character character) {
            appendTomlString(out, String.valueOf(character));
            return;
        }
        if (normalized instanceof Number || normalized instanceof Boolean) {
            appendJsonValue(out, normalized);
            return;
        }
        if (normalized instanceof Map<?, ?> map) {
            out.append('{');
            if (!map.isEmpty()) {
                out.append(' ');
            }
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(", ");
                }
                appendTomlKey(out, String.valueOf(entry.getKey()));
                out.append(" = ");
                appendTomlValue(out, entry.getValue());
                first = false;
            }
            if (!map.isEmpty()) {
                out.append(' ');
            }
            out.append('}');
            return;
        }
        if (normalized instanceof Iterable<?> values) {
            out.append('[');
            boolean first = true;
            for (Object item : values) {
                if (!first) {
                    out.append(", ");
                }
                appendTomlValue(out, item);
                first = false;
            }
            out.append(']');
            return;
        }
        appendTomlString(out, String.valueOf(normalized));
    }

    private static void appendTomlKey(Appendable out, String key) throws IOException {
        if (isPlainTomlKey(key)) {
            out.append(key);
        } else {
            appendTomlString(out, key);
        }
    }

    /** TOML 1.0 bare keys are limited to ASCII letters, digits, underscores and hyphens. */
    private static boolean isPlainTomlKey(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            boolean asciiLetterOrDigit =
                    (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
            if (!(asciiLetterOrDigit || ch == '_' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    static void appendCsvRow(Appendable out, List<String> values, CsvFormulaPolicy formulaPolicy) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            String value = values.get(i);
            appendCsvCell(out, formulaPolicy == CsvFormulaPolicy.NEUTRALIZE ? neutralizeFormula(value) : value);
        }
    }

    /** Prefixes an apostrophe to text a spreadsheet would evaluate as a formula; numbers stay unchanged. */
    private static String neutralizeFormula(String value) {
        if (value.isEmpty() || "=+-@\t\r".indexOf(value.charAt(0)) < 0 || isDecimalNumber(value)) {
            return value;
        }
        return "'" + value;
    }

    private static boolean isDecimalNumber(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException notNumeric) {
            return false;
        }
    }

    private static void appendCsvCell(Appendable out, String value) throws IOException {
        value = encodable(value);
        if (!needsCsvQuoting(value)) {
            out.append(value);
            return;
        }
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                out.append("\"\"");
            } else {
                out.append(ch);
            }
        }
        out.append('"');
    }

    private static boolean needsCsvQuoting(String value) {
        return value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0
                || value.startsWith(" ")
                || value.endsWith(" ");
    }

    static String toCsvCell(@org.jspecify.annotations.Nullable Object value) {
        Object normalized = normalizeStructuredValue(value);
        if (normalized == null) {
            return "";
        }
        if (normalized instanceof CharSequence
                || normalized instanceof Character
                || normalized instanceof Number
                || normalized instanceof Boolean) {
            return String.valueOf(normalized);
        }
        if (normalized instanceof Map<?, ?> || normalized instanceof Iterable<?>) {
            return buildString(builder -> appendJsonValue(builder, normalized));
        }
        return String.valueOf(normalized);
    }

    static String buildString(StringBuilderWriter writer) {
        StringBuilder builder = new StringBuilder();
        try {
            writer.write(builder);
        } catch (IOException e) {
            throw new IllegalStateException("StringBuilder should not throw IOException", e);
        }
        return builder.toString();
    }

    static @org.jspecify.annotations.Nullable Object normalizeStructuredValue(
            @org.jspecify.annotations.Nullable Object value) {
        return normalizeStructuredValue(value, "");
    }

    private static @org.jspecify.annotations.Nullable Object normalizeStructuredValue(
            @org.jspecify.annotations.Nullable Object value, String componentPath) {
        if (value == null
                || value instanceof CharSequence
                || value instanceof Character
                || value instanceof Number
                || value instanceof Boolean) {
            return value;
        }
        String temporal = rfc3339(value);
        if (temporal != null) {
            return temporal;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> normalized = new LinkedHashMap<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(entry.getKey(), normalizeStructuredValue(entry.getValue(), componentPath));
            }
            return Collections.unmodifiableMap(normalized);
        }
        if (value instanceof Optional<?> optional) {
            return normalizeStructuredValue(optional.orElse(null), componentPath);
        }
        if (value instanceof Iterable<?> items) {
            List<Object> normalized = new ArrayList<>();
            for (Object item : items) {
                normalized.add(normalizeStructuredValue(item, componentPath));
            }
            return Collections.unmodifiableList(normalized);
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> normalized = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                normalized.add(normalizeStructuredValue(Array.get(value, i), componentPath));
            }
            return Collections.unmodifiableList(normalized);
        }
        if (value.getClass().isRecord()) {
            return recordToMap(value, componentPath);
        }
        return value;
    }

    /**
     * The RFC 3339 text of a date or time, in the form of the format its type exports: full-date for a
     * {@code LocalDate}, partial-time for a {@code LocalTime}, a date-time without an offset for a
     * {@code LocalDateTime}, and with one for the rest, where a {@code ZonedDateTime} gives its offset without
     * its zone ID and an {@code Instant} {@code Z}. Seconds are always written, and a fraction only where it is
     * not zero. A {@code java.util.Date} is written as the instant it holds, a {@code Calendar} at the offset
     * its own time zone gives that instant, and a {@code java.sql.Date} or {@code Time} as its date or time of
     * day. Null for any other value.
     *
     * @throws IllegalArgumentException for a year outside 0000 to 9999, an offset with seconds or beyond
     *     23:59, or a Julian day that ISO does not have, which RFC 3339 cannot write
     */
    static @org.jspecify.annotations.Nullable String rfc3339(Object value) {
        if (value instanceof LocalDate date) {
            return withYear(date.getYear(), value, DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        }
        if (value instanceof LocalTime time) {
            return DateTimeFormatter.ISO_LOCAL_TIME.format(time);
        }
        if (value instanceof LocalDateTime dateTime) {
            return withYear(dateTime.getYear(), value, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime));
        }
        if (value instanceof OffsetTime time) {
            return withOffset(time.getOffset(), value, DateTimeFormatter.ISO_OFFSET_TIME.format(time));
        }
        if (value instanceof OffsetDateTime dateTime) {
            return offsetDateTime(dateTime, value);
        }
        if (value instanceof ZonedDateTime dateTime) {
            return offsetDateTime(dateTime.toOffsetDateTime(), value);
        }
        if (value instanceof Instant instant) {
            if (instant.getEpochSecond() < FIRST_SECOND || instant.getEpochSecond() > LAST_SECOND) {
                throw unwritable(value, "its year is outside 0000 to 9999");
            }
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.atOffset(ZoneOffset.UTC));
        }
        // A java.sql.Date or Time holds no instant, and its toInstant() throws, so it comes before java.util.Date.
        if (value instanceof java.sql.Date date) {
            return rfc3339(isoDate(date));
        }
        if (value instanceof java.sql.Time time) {
            return rfc3339(time.toLocalTime());
        }
        if (value instanceof java.util.Date date) {
            return rfc3339(date.toInstant());
        }
        if (value instanceof Calendar calendar) {
            return calendar(calendar);
        }
        return null;
    }

    /**
     * The ISO date that a {@code java.sql.Date}'s fields name, read in the default time zone as
     * {@code toLocalDate()} reads them, but with their era, which {@code toLocalDate()} drops, so 1 BCE is
     * the year 0000. Its calendar is Julian before 1582, so a day that ISO does not have, such as 29 February
     * 1500, is refused.
     */
    private static LocalDate isoDate(java.sql.Date date) {
        Calendar fields = new GregorianCalendar();
        fields.setTime(date);
        int yearOfEra = fields.get(Calendar.YEAR);
        int year = fields.get(Calendar.ERA) == GregorianCalendar.BC ? 1 - yearOfEra : yearOfEra;
        try {
            return LocalDate.of(year, fields.get(Calendar.MONTH) + 1, fields.get(Calendar.DAY_OF_MONTH));
        } catch (DateTimeException noIsoDay) {
            throw unwritable(date, "its day is no ISO date");
        }
    }

    /**
     * A calendar at the offset its own time zone gives its instant, as the zone's ID may name no region, or
     * a region whose rules differ from the zone's. RFC 3339 writes any offset of whole minutes up to 23:59,
     * beyond the 18:00 a {@code ZoneOffset} holds, so the local date-time is worked out from the instant.
     */
    private static String calendar(Calendar calendar) {
        int offset = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
        if (offset % 60_000 != 0) {
            throw unwritable(calendar, "its offset has seconds");
        }
        int minutes = offset / 60_000;
        if (Math.abs(minutes) > 23 * 60 + 59) {
            throw unwritable(calendar, "its offset is beyond 23:59");
        }
        Instant instant = calendar.toInstant();
        LocalDateTime local = LocalDateTime.ofEpochSecond(
                instant.getEpochSecond() + minutes * 60L, instant.getNano(), ZoneOffset.UTC);
        return withYear(local.getYear(), calendar, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(local))
                + offsetText(minutes);
    }

    /** An offset as RFC 3339 writes it, {@code Z} for none, as {@code ISO_OFFSET_DATE_TIME} does. */
    private static String offsetText(int minutes) {
        if (minutes == 0) {
            return "Z";
        }
        int magnitude = Math.abs(minutes);
        return String.format(Locale.ROOT, "%s%02d:%02d", minutes < 0 ? "-" : "+", magnitude / 60, magnitude % 60);
    }

    /**
     * The text a value that is no number, boolean, structure, or null is written as: a date or time's RFC 3339
     * text, and anything else's {@code toString()}.
     */
    static String writtenText(Object value) {
        String temporal = rfc3339(value);
        return temporal != null ? temporal : value.toString();
    }

    private static String offsetDateTime(OffsetDateTime dateTime, Object value) {
        return withOffset(
                dateTime.getOffset(),
                value,
                withYear(dateTime.getYear(), value, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime)));
    }

    private static String withYear(int year, Object value, String text) {
        if (year < 0 || year > 9999) {
            throw unwritable(value, "its year is outside 0000 to 9999");
        }
        return text;
    }

    private static String withOffset(ZoneOffset offset, Object value, String text) {
        if (offset.getTotalSeconds() % 60 != 0) {
            throw unwritable(value, "its offset has seconds");
        }
        return text;
    }

    private static IllegalArgumentException unwritable(Object value, String reason) {
        return new IllegalArgumentException("RFC 3339 has no form for " + value + ": " + reason);
    }

    private static Map<String, Object> recordToMap(Object record, String componentPath) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        Map<String, Object> values = new LinkedHashMap<>(components.length);
        for (RecordComponent component : components) {
            String path = componentPath.isEmpty() ? component.getName() : componentPath + "." + component.getName();
            Object componentValue = SchemaRecordAccess.read(record, component, path);
            values.put(component.getName(), normalizeStructuredValue(componentValue, path));
        }
        return Collections.unmodifiableMap(values);
    }
}
