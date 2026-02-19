package org.github.krandom.common

interface KRandomChar {

    fun randomChar(
        upperLetters: Boolean = true,
        lowerLetters: Boolean = true,
        numbers: Boolean = false,
        specialCharacters: Boolean = false
    ): Char
}
