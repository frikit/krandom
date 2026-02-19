/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator

class Gender(extraGenders: List<String> = emptyList()) : KRandomUser<String>, GenericUserGenerator<String>() {

    companion object {
        val DEFAULT = listOf("Male", "Female")
    }

    //cache parsed list
    private val list = DEFAULT + extraGenders

    override fun randomData(): String {
        return randomData(list)
    }

    override fun randomDatas(size: Int): List<String> {
        return randomDatas(list, size)
    }
}
