/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.utils.ResourcePathHolder

class FirstName : KRandomUser<String>, GenericUserGenerator<String>() {

    //cache parsed list
    private val list by lazy { initCache(ResourcePathHolder.relativeFirstNamePath) }

    override fun randomData(): String {
        return randomData(list)
    }

    override fun randomDatas(size: Int): List<String> {
        return randomDatas(list, size)
    }
}
