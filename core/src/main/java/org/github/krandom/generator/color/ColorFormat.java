/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.color;

/**
 * Enumeration of supported color output formats.
 *
 * <p>Defines various representations for color values:
 * <ul>
 *   <li>{@link #HEX} - Standard 6-digit hexadecimal with # prefix (e.g., "#79c157")</li>
 *   <li>{@link #SHORT_HEX} - Abbreviated 3-digit hexadecimal with # prefix (e.g., "#60f")</li>
 *   <li>{@link #RGB} - CSS RGB function notation (e.g., "rgb(110,52,164)")</li>
 *   <li>{@link #HEX_0X} - Hexadecimal number with 0x prefix (e.g., "0x79c157")</li>
 * </ul>
 *
 * @see ColorGenerator
 */
public enum ColorFormat {
    
    /**
     * Standard 6-digit hexadecimal format with # prefix.
     * <p>Example: {@code "#79c157"}
     */
    HEX,
    
    /**
     * Abbreviated 3-digit hexadecimal format with # prefix.
     * <p>Each RGB component is represented by a single hex digit (0-F).
     * <p>Example: {@code "#60f"} (equivalent to {@code "#6600ff"})
     */
    SHORT_HEX,
    
    /**
     * CSS RGB function notation.
     * <p>Format: {@code rgb(R,G,B)} where R, G, B are integers in [0, 255].
     * <p>Example: {@code "rgb(110,52,164)"}
     */
    RGB,
    
    /**
     * Hexadecimal number with 0x prefix.
     * <p>Example: {@code "0x79c157"}
     */
    HEX_0X
}
