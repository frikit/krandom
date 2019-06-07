package krandom.user

import krandom.KRandomUser
import krandom.user.common.GenericUserGenerator
import krandom.utils.ResourcePathHolder

class SurName : KRandomUser<String>, GenericUserGenerator() {

    //cache parsed list
    private val list by lazy { initCache(ResourcePathHolder.relativeSurNamePath) }

    override fun randomData(): String {
        val list = randomDatas()
        val index = kRandomCommon.randomInt(0, list.size - 1)
        return list[index]
    }

    override fun randomDatas(): List<String> {
        val size = kRandomCommon.randomInt(1..maxAllowSize)
        return randomDatas(size)
    }

    override fun randomDatas(size: Int): List<String> {
        if (size > maxAllowSize) IllegalArgumentException("Size cannot be > 10_000!")
        return list.shuffled().take(size).toList()
    }
}