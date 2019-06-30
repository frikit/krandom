package krandom.user

import krandom.KRandomUser
import krandom.exceptions.SizeLimitExceedException
import krandom.user.common.GenericUserGenerator
import krandom.utils.ResourcePathHolder

class FirstName : KRandomUser<String>, GenericUserGenerator() {

    //cache parsed list
    private val list by lazy { initCache(ResourcePathHolder.relativeFirstNamePath) }

    override fun randomData(): String {
        val list = randomDatas()
        val index = kRandomCommon.randomInt(0, list.size - 1)
        return list[index]
    }

    override fun randomDatas(): List<String> {
        val size = kRandomCommon.randomInt(2..maxAllowSize)
        return randomDatas(size)
    }

    override fun randomDatas(size: Int): List<String> {
        if (size > maxAllowSize) SizeLimitExceedException("Size cannot be > $maxAllowSize!")
        return list.shuffled().take(size).toList()
    }
}