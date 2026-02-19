/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.user.enum

/**
 * Exactly copied from wikipedia
 * https://en.wikipedia.org/wiki/Title#Titles_in_English-speaking_areas
 */
enum class TitleResult(val title: String, val fullTitle: String, val description: String) {
    MR("Mr", "Mister", "Adult man (regardless of marital status)"),
    MRS("Mrs", "Mistress", "Adult woman (usually just for married women, widows, and divorcées)"),
    MX("Mx", "Mx", "Gender neutral (regardless of marital status)"),
    MS("Ms", "Mistress", "Adult woman (regardless of marital status)"),
    MISS("Miss", "Mistress", "Adult woman (regardless of marital status)"),
    MASTER("Master", "Master", "For male children: Young boys were formerly addressed as \"Master [first name].\" This was the standard form for servants to use in addressing their employer's minor sons."),
    MAID("Maid", "Maid", "Archaic: When used as a title before a name (and not as a general term for a young domestic worker housemaid girl), this was a way to denote an unmarried woman, such as the character Maid Marian."),
    MADAM("Madam", "Madam", "Adult woman"),
    MADAME("Madame", "Madame", "Adult woman");

    fun getTitle(usStyle: Boolean = false): String {
        val suffix = if (usStyle && title.length <= 3) "." else ""
        return title + suffix
    }
}
