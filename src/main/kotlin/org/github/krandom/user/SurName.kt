package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.utils.ResourcePathHolder

class SurName : KRandomUser<String>, GenericUserGenerator<String>() {

    //cache parsed list
    private val list by lazy { initCache(ResourcePathHolder.relativeSurNamePath) }

    override fun randomData(): String {
        return randomData(list)
    }

    override fun randomDatas(size: Int): List<String> {
        return randomDatas(list, size)
    }
}
