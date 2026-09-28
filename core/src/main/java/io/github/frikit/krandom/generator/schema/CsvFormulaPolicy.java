/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

/**
 * Controls how CSV output treats cells that spreadsheet applications would evaluate as formulas.
 *
 * <p>A spreadsheet evaluates a cell that starts with {@code =}, {@code +}, {@code -}, {@code @}, a
 * tab, or a carriage return as a formula when the file is opened, and generated text can start that
 * way (for example a phone number such as {@code +1-555-0100}).
 */
public enum CsvFormulaPolicy {

    /** Writes every cell unchanged. This is the default. */
    PRESERVE,

    /**
     * Prefixes formula-like cells with an apostrophe so spreadsheets show them as text. Numeric
     * cells such as {@code -5} stay unchanged. Use this for files people open in a spreadsheet: the
     * prefix is part of the cell value for other CSV readers.
     */
    NEUTRALIZE
}
