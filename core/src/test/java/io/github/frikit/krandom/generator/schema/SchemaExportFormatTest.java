/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exported documents must stay valid for their format's parsers, whatever text the fields hold.
 */
class SchemaExportFormatTest {

    private static Schema schemaOf(String fieldName, Object value) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put(fieldName, ctx -> value);
        return new Schema(fields);
    }

    @Test
    @DisplayName("XML stays well-formed for characters XML 1.0 cannot represent")
    void xmlStaysWellFormed() throws Exception {
        String value = "a\u0001b\u0000c￾￿d\uD800e\uDC00f😀g\r\n\t\"'&<>h\uD800";
        String name = "bad name \"q\" & 's'\n\t\r";
        String xml = schemaOf(name, value).toXml(1);

        Document document = DocumentBuilderFactory.newInstance()
                                                  .newDocumentBuilder()
                                                  .parse(new InputSource(new StringReader(xml)));
        Element field = (Element) document.getElementsByTagName("field").item(0);
        assertEquals(name, field.getAttribute("name"));
        assertEquals("a�b�c��d�e�f😀g\r\n\t\"'&<>h�", field.getTextContent());
    }

    @Test
    @DisplayName("SQL quotes a column name with dots as one identifier and keeps qualified table names")
    void sqlQuotesColumnNamesAsSingleIdentifiers() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("customer.name", ctx -> "Ada");
        fields.put("say \"hi\"", ctx -> 1);
        String sql = new Schema(fields).toSqlInserts("crm.customers", 1);
        assertTrue(sql.startsWith("INSERT INTO \"crm\".\"customers\" (\"customer.name\", \"say \"\"hi\"\"\")"), sql);
    }

    @Test
    @DisplayName("TOML quotes keys outside the ASCII bare-key alphabet")
    void tomlQuotesNonAsciiKeys() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("prénom", ctx -> "Zoë");
        fields.put("plain_key-1", ctx -> 1);
        String toml = new Schema(fields).toToml(1);
        assertTrue(toml.contains("\"prénom\" = "), toml);
        assertTrue(toml.contains("\nplain_key-1 = "), toml);
    }
}
