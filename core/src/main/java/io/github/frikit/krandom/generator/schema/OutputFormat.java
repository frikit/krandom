/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

/**
 * Supported output formats for {@link Schema#writeTo}.
 */
public enum OutputFormat {

    /** Newline-delimited JSON (one JSON object per line). */
    JSONL,

    /** A JSON array containing all generated records. */
    JSON,

    /** Comma-separated values with a header row. */
    CSV,

    /** XML with configurable root and record element names. */
    XML,

    /** SQL {@code INSERT} statements (requires a table name). */
    SQL,

    /** YAML document containing a sequence of generated records. */
    YAML,

    /** TOML array-of-tables document containing generated records. */
    TOML
}
