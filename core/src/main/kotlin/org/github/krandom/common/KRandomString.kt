package org.github.krandom.common

interface KRandomString {

    fun randomString(length: Int = 5, specialCharacters: Boolean = true, numbers: Boolean = true): String
}
