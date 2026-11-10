/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Schema writers and dates and times")
class SchemaTemporalValuesTest {

    private static final ZoneOffset INDIA = ZoneOffset.ofHoursMinutes(5, 30);
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final Instant MORNING = Instant.parse("2024-01-05T09:05:00Z");

    /** RFC 3339's forms, with seconds always and a fraction only where it is not zero. */
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2}";

    private static final String PARTIAL_TIME = "\\d{2}:\\d{2}:\\d{2}(\\.\\d*[1-9])?";
    private static final String OFFSET = "(Z|[+-]\\d{2}:\\d{2})";
    private static final Map<String, Pattern> FORMATS = Map.of(
            "date", Pattern.compile(DATE),
            "time", Pattern.compile(PARTIAL_TIME + OFFSET),
            "time-local", Pattern.compile(PARTIAL_TIME),
            "date-time", Pattern.compile(DATE + "T" + PARTIAL_TIME + OFFSET),
            "date-time-local", Pattern.compile(DATE + "T" + PARTIAL_TIME));

    /** Members of each Java type an exported schema names a date or time format for. */
    record Moments(
            LocalDate date,
            LocalTime localTime,
            LocalDateTime localDateTime,
            OffsetTime offsetTime,
            OffsetDateTime offsetDateTime,
            ZonedDateTime zonedDateTime,
            Instant instant) {}

    private static final Map<String, Function<String, ?>> PARSERS = Map.of(
            "date", LocalDate::parse,
            "localTime", LocalTime::parse,
            "localDateTime", LocalDateTime::parse,
            "offsetTime", OffsetTime::parse,
            "offsetDateTime", OffsetDateTime::parse,
            "zonedDateTime", ZonedDateTime::parse,
            "instant", Instant::parse);

    private static String json(Object value) {
        StringBuilder out = new StringBuilder();
        try {
            Schema.appendJsonValue(out, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    static Stream<Arguments> values() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone(PARIS));
        calendar.setTimeInMillis(MORNING.toEpochMilli());
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 1, 5), "2024-01-05"),
                Arguments.of(LocalTime.of(9, 5), "09:05:00"),
                Arguments.of(LocalTime.of(9, 5, 7, 500_000_000), "09:05:07.5"),
                Arguments.of(LocalTime.of(9, 5, 7, 1), "09:05:07.000000001"),
                Arguments.of(LocalDateTime.of(2024, 1, 5, 9, 5), "2024-01-05T09:05:00"),
                Arguments.of(LocalDateTime.of(2024, 1, 5, 9, 5, 7, 120_000_000), "2024-01-05T09:05:07.12"),
                Arguments.of(OffsetTime.of(9, 5, 0, 0, ZoneOffset.UTC), "09:05:00Z"),
                Arguments.of(OffsetTime.of(9, 5, 7, 500_000_000, INDIA), "09:05:07.5+05:30"),
                Arguments.of(
                        OffsetDateTime.of(2024, 1, 5, 9, 5, 0, 0, ZoneOffset.ofHours(-3)), "2024-01-05T09:05:00-03:00"),
                Arguments.of(ZonedDateTime.of(2024, 1, 5, 9, 5, 0, 0, PARIS), "2024-01-05T09:05:00+01:00"),
                Arguments.of(ZonedDateTime.of(2024, 1, 5, 9, 5, 0, 0, ZoneOffset.UTC), "2024-01-05T09:05:00Z"),
                Arguments.of(MORNING, "2024-01-05T09:05:00Z"),
                Arguments.of(Instant.parse("2024-01-05T09:05:07.500Z"), "2024-01-05T09:05:07.5Z"),
                Arguments.of(new java.util.Date(MORNING.toEpochMilli()), "2024-01-05T09:05:00Z"),
                Arguments.of(
                        java.sql.Timestamp.from(Instant.parse("2024-01-05T09:05:07.123456789Z")),
                        "2024-01-05T09:05:07.123456789Z"),
                Arguments.of(java.sql.Date.valueOf(LocalDate.of(2024, 1, 5)), "2024-01-05"),
                Arguments.of(java.sql.Time.valueOf(LocalTime.of(9, 5)), "09:05:00"),
                Arguments.of(calendar, "2024-01-05T10:05:00+01:00"),
                // A calendar's offset comes from its own time zone at its instant, whatever the zone's ID names.
                Arguments.of(calendar(new SimpleTimeZone(3_600_000, "custom")), "2024-01-05T10:05:00+01:00"),
                Arguments.of(calendar(new SimpleTimeZone(7_200_000, "Europe/Paris")), "2024-01-05T11:05:00+02:00"));
    }

    private static Calendar calendar(TimeZone zone) {
        Calendar calendar = new GregorianCalendar(zone);
        calendar.setTimeInMillis(MORNING.toEpochMilli());
        return calendar;
    }

    @ParameterizedTest(name = "{0} is written as {1}")
    @MethodSource("values")
    @DisplayName("each date and time is written in RFC 3339, with seconds and a fraction only where it is not zero")
    void eachValueIsWrittenInItsFormat(Object value, String text) {
        assertEquals("\"" + text + "\"", json(value));
    }

    @Test
    @DisplayName("every writer writes the same text, at any depth")
    void everyWriterWritesTheSameText() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("at", ctx -> ZonedDateTime.of(2024, 1, 5, 9, 5, 0, 0, PARIS));
        fields.put("times", ctx -> List.of(LocalTime.of(9, 5)));
        Schema schema = new Schema(fields);
        String at = "2024-01-05T09:05:00+01:00";
        for (String output : List.of(
                schema.toJsonLines(1),
                schema.toJson(1),
                schema.toYaml(1),
                schema.toToml(1),
                schema.toCsv(1),
                schema.toXml(1),
                schema.toSqlInserts("moments", 1))) {
            assertTrue(output.contains(at), output);
            assertTrue(output.contains("09:05:00"), output);
            assertFalse(output.contains("[Europe/Paris]"), output);
        }
    }

    @Test
    @DisplayName("a date or time that RFC 3339 cannot write is refused, as a number JSON cannot write is")
    void valuesRfc3339CannotWriteAreRefused() {
        ZoneOffset secondsOffset = ZoneOffset.ofHoursMinutesSeconds(0, 19, 32);
        for (Object value : List.of(
                LocalDate.of(10_000, 1, 1),
                LocalDate.of(-1, 12, 31),
                calendar(new SimpleTimeZone(3_630_000, "odd")),
                LocalDateTime.of(10_000, 1, 1, 0, 0),
                OffsetDateTime.of(2024, 1, 5, 9, 5, 0, 0, secondsOffset),
                OffsetTime.of(9, 5, 0, 0, secondsOffset),
                ZonedDateTime.of(2024, 1, 5, 9, 5, 0, 0, secondsOffset),
                Instant.MAX,
                Instant.MIN)) {
            IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, () -> json(value));
            assertTrue(refused.getMessage().startsWith("RFC 3339 has no form for "), refused.getMessage());
        }
        // The first and last years it can write are written.
        assertEquals("\"0000-01-01\"", json(LocalDate.of(0, 1, 1)));
        assertEquals("\"9999-12-31T23:59:59.999999999\"", json(LocalDateTime.MAX.withYear(9999)));
        assertEquals("\"0000-01-01T00:00:00Z\"", json(Instant.parse("0000-01-01T00:00:00Z")));
        assertEquals("\"9999-12-31T23:59:59.999999999Z\"", json(Instant.parse("9999-12-31T23:59:59.999999999Z")));
    }

    private static void parse(Map<String, Object> property) {
        SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", Map.of("value", property)));
    }

    @Test
    @DisplayName("the parser measures a date or time const or enum by the text the writer gives it")
    void theParserMeasuresTheWrittenText() {
        LocalTime morning = LocalTime.of(9, 5);
        // 09:05 is written 09:05:00, which maxLength 5 does not allow, and which is that string as JSON.
        assertThrows(
                IllegalArgumentException.class,
                () -> parse(Map.of("type", "string", "const", morning, "maxLength", 5)));
        assertDoesNotThrow(() -> parse(Map.of("type", "string", "const", morning, "maxLength", 8)));
        assertDoesNotThrow(() -> parse(Map.of("const", morning, "pattern", "^09:05:00$")));
        assertThrows(
                IllegalArgumentException.class,
                () -> parse(Map.of("const", List.of(morning, "09:05:00"), "uniqueItems", true)));
        assertDoesNotThrow(() -> parse(Map.of("const", "09:05:00", "enum", List.of(morning))));
        // A date or time that RFC 3339 cannot write cannot be a const either.
        IllegalArgumentException unwritable =
                assertThrows(IllegalArgumentException.class, () -> parse(Map.of("const", LocalDate.of(10_000, 1, 1))));
        assertTrue(unwritable.getMessage().contains("RFC 3339 has no form for"), unwritable.getMessage());
    }

    @Test
    @DisplayName("a record's dates and times round-trip through export, parse, generate, and the writer")
    void exportedFormatsRoundTrip() {
        Map<String, Object> exported = JsonSchemaSupport.record(Moments.class);
        Map<?, ?> properties = (Map<?, ?>) exported.get("properties");
        Map<String, Object> formats = new LinkedHashMap<>();
        properties.forEach((name, schema) -> formats.put((String) name, ((Map<?, ?>) schema).get("format")));
        assertEquals(
                Map.of(
                        "date", "date",
                        "localTime", "time-local",
                        "localDateTime", "date-time-local",
                        "offsetTime", "time",
                        "offsetDateTime", "date-time",
                        "zonedDateTime", "date-time",
                        "instant", "date-time"),
                formats);

        // Values at zero seconds, with fractions, and in a named zone are written as their formats promise.
        Moments edges = new Moments(
                LocalDate.of(2024, 1, 5),
                LocalTime.of(9, 5),
                LocalDateTime.of(2024, 1, 5, 9, 5, 7, 500_000_000),
                OffsetTime.of(9, 5, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 5, 9, 5, 0, 1, INDIA),
                ZonedDateTime.of(2024, 1, 5, 9, 5, 0, 0, PARIS),
                MORNING);
        assertMeetTheirFormats(written(json(edges)), formats);

        // The schema parsed back generates values of the same formats, which the writer keeps.
        Schema parsed = SchemaParser.fromJsonSchema(
                exported, GeneratorConfig.builder().seed(7L).build());
        for (String line : parsed.toJsonLines(200).split("\n")) {
            assertMeetTheirFormats(written(line), formats);
        }
    }

    /** The members of a JSON object of strings, as written. */
    private static Map<String, String> written(String object) {
        Map<String, String> members = new LinkedHashMap<>();
        Matcher member = Pattern.compile("\"(\\w+)\":\"([^\"]*)\"").matcher(object);
        while (member.find()) {
            members.put(member.group(1), member.group(2));
        }
        assertEquals(PARSERS.keySet(), members.keySet(), object);
        return members;
    }

    private static void assertMeetTheirFormats(Map<String, String> members, Map<String, Object> formats) {
        members.forEach((name, text) -> {
            assertTrue(FORMATS.get(formats.get(name)).matcher(text).matches(), name + " is " + text);
            assertDoesNotThrow(() -> PARSERS.get(name).apply(text), name + " is " + text);
        });
    }
}
