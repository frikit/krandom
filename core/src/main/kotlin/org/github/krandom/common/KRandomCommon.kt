package org.github.krandom.common

interface KRandomCommon : KRandomDouble, KRandomFloat, KRandomLong, KRandomInt {

    //TODO add exclude params to methods to make easier to exclude unwanted values from result
    //TODO exclude a list of values

    //Chars
    fun randomChar(upperLetters: Boolean = true,
                   lowerLetters: Boolean = true,
                   numbers: Boolean = false,
                   specialCharacters: Boolean = false): Char

    //Boolean
    fun randomBoolean(): Boolean

    //Strings
    fun randomString(length: Int = 5, specialCharacters: Boolean = true, numbers: Boolean = true): String
}
