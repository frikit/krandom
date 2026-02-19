/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

interface KRandomString {

    fun randomString(length: Int = 5, specialCharacters: Boolean = true, numbers: Boolean = true): String
}
