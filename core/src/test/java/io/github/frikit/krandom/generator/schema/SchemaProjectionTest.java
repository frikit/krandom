/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SchemaProjection")
class SchemaProjectionTest {

    @Test
    @DisplayName("projects existing objects in declared field order")
    void projectsExistingObjectsInDeclaredFieldOrder() {
        Map<String, Function<Person, ?>> fields = new LinkedHashMap<>();
        fields.put("name", Person::name);
        fields.put("age", Person::age);
        SchemaProjection<Person> projection = SchemaProjection.of(fields);

        assertEquals(Map.of("name", "Ada", "age", 36), projection.project(new Person("Ada", 36)));
        assertEquals(
            "[{\"name\":\"Ada\",\"age\":36},{\"name\":\"Lin\",\"age\":28}]\n",
            projection.toJson(List.of(new Person("Ada", 36), new Person("Lin", 28))));
    }

    @Test
    @DisplayName("writes every supported format from a one-shot object sequence")
    void writesEverySupportedFormatFromOneShotObjectSequence() throws Exception {
        Map<String, Function<Person, ?>> fields = new LinkedHashMap<>();
        fields.put("name", Person::name);
        fields.put("age", Person::age);
        SchemaProjection<Person> projection = SchemaProjection.of(fields);

        for (OutputFormat format : OutputFormat.values()) {
            StringWriter writer = new StringWriter();
            projection.writeTo(writer, format, oneShot(new Person("Ada", 36)), "people");
            assertEquals(expected(format), writer.toString(), "format: " + format);
        }
    }

    @Test
    @DisplayName("validates projections, values, and TOML nulls")
    void validatesProjectionsValuesAndTomlNulls() {
        assertThrows(NullPointerException.class, () -> SchemaProjection.of(null));
        assertThrows(IllegalArgumentException.class, () -> SchemaProjection.of(Map.of()));
        assertThrows(IllegalArgumentException.class,
                     () -> SchemaProjection.of(Map.of(" ", (Function<Person, String>) Person::name)));
        Map<String, Function<Person, ?>> nullExtractor = new LinkedHashMap<>();
        nullExtractor.put("name", null);
        assertThrows(IllegalArgumentException.class, () -> SchemaProjection.of(nullExtractor));
        Map<String, Function<Person, ?>> nullName = new LinkedHashMap<>();
        nullName.put(null, Person::name);
        assertThrows(IllegalArgumentException.class, () -> SchemaProjection.of(nullName));

        SchemaProjection<Person> projection = SchemaProjection.of(Map.of("name", Person::name));
        assertThrows(NullPointerException.class, () -> projection.project(null));
        assertThrows(NullPointerException.class, () -> projection.toJson(null));
        assertThrows(IllegalArgumentException.class,
                     () -> SchemaProjection.of(Map.of("name", person -> null)).toToml(List.of(new Person("Ada", 36))));
    }

    @Test
    @DisplayName("convenience writers render empty and populated projections consistently")
    void convenienceWritersRenderEveryFormat() throws Exception {
        Map<String, Function<Person, ?>> fields = new LinkedHashMap<>();
        fields.put("name", Person::name);
        fields.put("age", Person::age);
        SchemaProjection<Person> projection = SchemaProjection.of(fields);
        List<Person> values = List.of(new Person("Ada", 36));

        assertEquals(List.of("name", "age"), List.copyOf(projection.getFields().keySet()));
        assertThrows(UnsupportedOperationException.class, projection.getFields()::clear);
        assertEquals("{\"name\":\"Ada\",\"age\":36}\n", projection.toJsonLines(values));
        assertEquals("name,age\nAda,36\n", projection.toCsv(values));
        assertEquals(expected(OutputFormat.XML), projection.toXml(values));
        assertEquals(expected(OutputFormat.SQL), projection.toSqlInserts("people", values));
        assertEquals(expected(OutputFormat.YAML), projection.toYaml(values));
        assertEquals(expected(OutputFormat.TOML), projection.toToml(values));
        assertEquals("[]\n", projection.toJson(List.of()));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<records/>\n", projection.toXml(List.of()));
        assertEquals("[]\n", projection.toYaml(List.of()));
        assertEquals("", projection.toToml(List.of()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        projection.writeTo(output, OutputFormat.JSON, values, "people");
        assertEquals(expected(OutputFormat.JSON), output.toString(StandardCharsets.UTF_8));
        assertThrows(NullPointerException.class, () -> projection.writeTo((ByteArrayOutputStream) null, OutputFormat.JSON,
                                                                          values, "people"));
        assertThrows(NullPointerException.class, () -> projection.writeTo((StringWriter) null, OutputFormat.JSON,
                                                                          values, "people"));
        assertThrows(NullPointerException.class, () -> projection.writeTo(new StringWriter(), null, values, "people"));
        assertThrows(NullPointerException.class, () -> projection.writeJsonLines(new StringBuilder(), null));
    }

    private static Iterable<Person> oneShot(Person person) {
        return new Iterable<>() {
            private boolean iterated;

            @Override
            public Iterator<Person> iterator() {
                if (iterated) {
                    throw new AssertionError("sequence must only be iterated once");
                }
                iterated = true;
                return List.of(person).iterator();
            }
        };
    }

    private static String expected(OutputFormat format) {
        return switch (format) {
            case JSONL -> "{\"name\":\"Ada\",\"age\":36}\n";
            case JSON -> "[{\"name\":\"Ada\",\"age\":36}]\n";
            case CSV -> "name,age\nAda,36\n";
            case XML -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<records>\n"
                        + "  <record>\n"
                        + "    <name>Ada</name>\n"
                        + "    <age>36</age>\n"
                        + "  </record>\n"
                        + "</records>\n";
            case SQL -> "INSERT INTO \"people\" (\"name\", \"age\") VALUES ('Ada', 36);\n";
            case YAML -> "- name: \"Ada\"\n  age: 36\n";
            case TOML -> "[[records]]\nname = \"Ada\"\nage = 36\n";
        };
    }

    private record Person(String name, int age) {
    }
}
