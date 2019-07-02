package krandom.user

import krandom.user.common.GenericUserGenerator
import krandom.utils.ResourcePathHolder

class FirstName : KRandomUser<String>, GenericUserGenerator() {

    //cache parsed list
    private val list by lazy { initCache(ResourcePathHolder.relativeFirstNamePath) }

    override fun randomData(): String {
        return randomData(list)
    }

    override fun randomDatas(): List<String> {
        val size = kRandomCommon.randomInt(2..maxAllowSize)
        return randomDatas(list, size)
    }

    override fun randomDatas(size: Int): List<String> {
        return randomDatas(list, size)
    }
}