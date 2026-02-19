/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

interface KRandomChar {

    fun randomChar(
        upperLetters: Boolean = true,
        lowerLetters: Boolean = true,
        numbers: Boolean = false,
        specialCharacters: Boolean = false
    ): Char
}
