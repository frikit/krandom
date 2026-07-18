/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Projects existing objects into named fields and writes them using the schema output formats.
 *
 * <p>The source iterable is consumed once and each format is written incrementally. This makes it
 * suitable for transforming large result sets without first materializing them as maps.
 *
 * @param <T> source object type
 */
public final class SchemaProjection<T> {

    private final Map<String, Function<T, ?>> fields;

    private SchemaProjection(Map<String, ? extends Function<? super T, ?>> fields) {
        Objects.requireNonNull(fields, "fields must not be null");
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("fields must not be empty");
        }

        Map<String, Function<T, ?>> copy = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, ? extends Function<? super T, ?>> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("field names must be non-blank");
            }
            Function<? super T, ?> extractor = entry.getValue();
            if (extractor == null) {
                throw new IllegalArgumentException("extractor for field '" + name + "' must not be null");
            }
            copy.put(name, extractor::apply);
        }
        this.fields = Collections.unmodifiableMap(copy);
    }

    /**
     * Creates an object projection with fields emitted in the map's iteration order.
     *
     * @param fields named value extractors
     * @param <T>    source object type
     * @return object projection
     */
    public static <T> SchemaProjection<T> of(Map<String, ? extends Function<? super T, ?>> fields) {
        return new SchemaProjection<>(fields);
    }

    /**
     * Projects one source object into an immutable, insertion-ordered map.
     *
     * @param value source object
     * @return projected fields
     */
    public Map<String, Object> project(T value) {
        Objects.requireNonNull(value, "value must not be null");
        Map<String, Object> projected = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, Function<T, ?>> entry : fields.entrySet()) {
            projected.put(entry.getKey(), entry.getValue().apply(value));
        }
        return Collections.unmodifiableMap(projected);
    }

    /**
     * Returns the immutable named field mapping.
     *
     * @return configured field extractors
     */
    public Map<String, Function<T, ?>> getFields() {
        return fields;
    }

    /**
     * Renders source objects as newline-delimited JSON.
     *
     * @param values source objects
     * @return JSONL payload
     */
    public String toJsonLines(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeJsonLines(builder, values));
    }

    /**
     * Writes source objects as newline-delimited JSON.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeJsonLines(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        for (T value : requireValues(values)) {
            Schema.appendJsonObject(out, project(value));
            out.append(Schema.NEWLINE);
        }
    }

    /**
     * Renders source objects as a JSON array.
     *
     * @param values source objects
     * @return JSON payload
     */
    public String toJson(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeJson(builder, values));
    }

    /**
     * Writes source objects as a JSON array.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeJson(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Iterator<? extends T> iterator = requireValues(values).iterator();
        out.append('[');
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                out.append(',');
            }
            Schema.appendJsonObject(out, project(iterator.next()));
            first = false;
        }
        out.append(']').append(Schema.NEWLINE);
    }

    /**
     * Renders source objects as CSV with projection field names as the header row.
     *
     * @param values source objects
     * @return CSV payload
     */
    public String toCsv(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeCsv(builder, values));
    }

    /**
     * Writes source objects as CSV with projection field names as the header row.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeCsv(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        List<String> columns = new ArrayList<>(fields.keySet());
        Schema.appendCsvRow(out, columns);
        out.append(Schema.NEWLINE);
        for (T value : requireValues(values)) {
            Map<String, Object> row = project(value);
            List<String> cells = new ArrayList<>(columns.size());
            for (String column : columns) {
                cells.add(Schema.toCsvCell(row.get(column)));
            }
            Schema.appendCsvRow(out, cells);
            out.append(Schema.NEWLINE);
        }
    }

    /**
     * Renders source objects as XML.
     *
     * @param values source objects
     * @return XML payload
     */
    public String toXml(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeXml(builder, values));
    }

    /**
     * Writes source objects as XML.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeXml(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Iterator<? extends T> iterator = requireValues(values).iterator();
        out.append(Schema.XML_DECLARATION).append(Schema.NEWLINE);
        if (!iterator.hasNext()) {
            out.append('<').append(Schema.DEFAULT_XML_ROOT_ELEMENT).append("/>").append(Schema.NEWLINE);
            return;
        }
        out.append('<').append(Schema.DEFAULT_XML_ROOT_ELEMENT).append('>').append(Schema.NEWLINE);
        while (iterator.hasNext()) {
            Map<String, Object> row = project(iterator.next());
            Schema.appendIndent(out, 2);
            out.append('<').append(Schema.DEFAULT_XML_RECORD_ELEMENT).append('>').append(Schema.NEWLINE);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Schema.appendXmlField(out, entry.getKey(), entry.getValue(), 4);
            }
            Schema.appendIndent(out, 2);
            out.append("</").append(Schema.DEFAULT_XML_RECORD_ELEMENT).append('>').append(Schema.NEWLINE);
        }
        out.append("</").append(Schema.DEFAULT_XML_ROOT_ELEMENT).append('>').append(Schema.NEWLINE);
    }

    /**
     * Renders source objects as SQL {@code INSERT} statements.
     *
     * @param tableName target table name
     * @param values    source objects
     * @return SQL payload
     */
    public String toSqlInserts(String tableName, Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeSqlInserts(builder, tableName, values));
    }

    /**
     * Writes source objects as SQL {@code INSERT} statements.
     *
     * @param out       destination
     * @param tableName target table name
     * @param values    source objects
     * @throws IOException if writing fails
     */
    public void writeSqlInserts(Appendable out, String tableName, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        String normalizedTableName = Schema.requireSqlIdentifier(tableName, "tableName");
        List<String> columns = new ArrayList<>(fields.keySet());
        for (T value : requireValues(values)) {
            Map<String, Object> row = project(value);
            out.append("INSERT INTO ");
            Schema.appendSqlIdentifier(out, normalizedTableName);
            out.append(" (");
            for (int column = 0; column < columns.size(); column++) {
                if (column > 0) {
                    out.append(", ");
                }
                Schema.appendSqlIdentifier(out, columns.get(column));
            }
            out.append(") VALUES (");
            for (int column = 0; column < columns.size(); column++) {
                if (column > 0) {
                    out.append(", ");
                }
                Schema.appendSqlValue(out, row.get(columns.get(column)));
            }
            out.append(");").append(Schema.NEWLINE);
        }
    }

    /**
     * Renders source objects as a YAML sequence.
     *
     * @param values source objects
     * @return YAML payload
     */
    public String toYaml(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeYaml(builder, values));
    }

    /**
     * Writes source objects as a YAML sequence.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeYaml(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Iterator<? extends T> iterator = requireValues(values).iterator();
        if (!iterator.hasNext()) {
            out.append("[]").append(Schema.NEWLINE);
            return;
        }
        while (iterator.hasNext()) {
            Schema.appendYamlListItem(out, project(iterator.next()), 0);
        }
    }

    /**
     * Renders source objects as a TOML array of {@code records} tables.
     *
     * @param values source objects
     * @return TOML payload
     */
    public String toToml(Iterable<? extends T> values) {
        return Schema.buildString(builder -> writeToml(builder, values));
    }

    /**
     * Writes source objects as a TOML array of {@code records} tables.
     *
     * @param out    destination
     * @param values source objects
     * @throws IOException if writing fails
     */
    public void writeToml(Appendable out, Iterable<? extends T> values) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        for (T value : requireValues(values)) {
            Schema.appendTomlRecord(out, project(value));
        }
    }

    /**
     * Writes projected source objects to an output stream in UTF-8 without closing the stream.
     *
     * @param out       destination stream
     * @param format    output format
     * @param values    source objects
     * @param tableName table name for SQL output
     * @throws IOException if writing fails
     */
    public void writeTo(OutputStream out, OutputFormat format, Iterable<? extends T> values, String tableName)
        throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writeTo(writer, format, values, tableName);
        writer.flush();
    }

    /**
     * Writes projected source objects to a writer without closing it.
     *
     * @param writer    destination writer
     * @param format    output format
     * @param values    source objects
     * @param tableName table name for SQL output
     * @throws IOException if writing fails
     */
    public void writeTo(Writer writer, OutputFormat format, Iterable<? extends T> values, String tableName)
        throws IOException {
        Objects.requireNonNull(writer, "writer must not be null");
        Objects.requireNonNull(format, "format must not be null");
        if (format == OutputFormat.JSONL) {
            writeJsonLines(writer, values);
        } else if (format == OutputFormat.JSON) {
            writeJson(writer, values);
        } else if (format == OutputFormat.CSV) {
            writeCsv(writer, values);
        } else if (format == OutputFormat.XML) {
            writeXml(writer, values);
        } else if (format == OutputFormat.SQL) {
            writeSqlInserts(writer, tableName, values);
        } else if (format == OutputFormat.YAML) {
            writeYaml(writer, values);
        } else {
            writeToml(writer, values);
        }
    }

    private static <T> Iterable<? extends T> requireValues(Iterable<? extends T> values) {
        return Objects.requireNonNull(values, "values must not be null");
    }
}
