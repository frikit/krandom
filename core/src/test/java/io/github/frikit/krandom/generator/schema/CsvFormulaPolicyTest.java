/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CSV output keeps values unchanged by default and neutralizes spreadsheet formulas on request.
 */
class CsvFormulaPolicyTest {

    public record Row(String text, int number) {
    }

    private static Schema schema() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("=cmd", ctx -> "=HYPERLINK(\"http://x\")");
        fields.put("plus", ctx -> "+1-555");
        fields.put("minus", ctx -> "-5");
        fields.put("at", ctx -> "@SUM(A1)");
        fields.put("tab", ctx -> "\tx");
        fields.put("cr", ctx -> "\rx");
        fields.put("plain", ctx -> "ok");
        fields.put("empty", ctx -> "");
        return new Schema(fields);
    }

    @Test
    @DisplayName("the default keeps cell text unchanged")
    void defaultPreservesCells() {
        String csv = schema().toCsv(1);
        assertEquals(schema().toCsv(1, CsvFormulaPolicy.PRESERVE), csv);
        assertEquals("=cmd,plus,minus,at,tab,cr,plain,empty", csv.lines().findFirst().orElseThrow());
    }

    @Test
    @DisplayName("NEUTRALIZE prefixes formula-like text but keeps numbers")
    void neutralizePrefixesFormulas() {
        // Split on \n only: the quoted cell legitimately contains a carriage return.
        String[] lines = schema().toCsv(1, CsvFormulaPolicy.NEUTRALIZE).split("\n");
        assertEquals("'=cmd,plus,minus,at,tab,cr,plain,empty", lines[0]);
        assertEquals("\"'=HYPERLINK(\"\"http://x\"\")\",'+1-555,-5,'@SUM(A1),'\tx,\"'\rx\",ok,", lines[1]);
    }

    @Test
    @DisplayName("projections accept the same policy")
    void projectionsAcceptThePolicy() {
        Map<String, Function<Row, ?>> fields = new LinkedHashMap<>();
        fields.put("text", Row::text);
        fields.put("number", Row::number);
        SchemaProjection<Row> projection = SchemaProjection.of(fields);
        List<Row> rows = List.of(new Row("=1+1", -2));
        assertEquals(List.of("text,number", "'=1+1,-2"),
                     projection.toCsv(rows, CsvFormulaPolicy.NEUTRALIZE).lines().toList());
        assertEquals(List.of("text,number", "=1+1,-2"), projection.toCsv(rows).lines().toList());
    }
}
