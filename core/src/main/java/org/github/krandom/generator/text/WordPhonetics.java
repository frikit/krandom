/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

/**
 * Locale phonetic profile used by {@link WordGenerator}.
 */
record WordPhonetics(String[] onsets, String[] nuclei, String[] codas) {}
