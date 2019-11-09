package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.utils.RandomizerUtils.generateRandomString

class Username(val numbers: Boolean) : KRandomUser<String>, GenericUserGenerator() {

    override fun randomData(): String {
        val block: () -> String = { kRandomCommon.randomString(7, false, numbers) }
        return generateRandomString(block, numbers)
    }

    override fun randomDatas(): List<String> {
        val times = kRandomCommon.randomInt(1, maxAllowSize)
        return randomDatas(times)
    }

    override fun randomDatas(size: Int): List<String> {
        isValidSize(size)
        val res = arrayListOf<String>()
        repeat((1..size).count()) {
            val item = randomData()
            res.add(item)
        }

        return res
    }

}
