package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.utils.apache.RandomStringUtils.generateRandomString

class Username(val numbers: Boolean) : KRandomUser<String>, GenericUserGenerator<String>() {

    override fun randomData(): String {
        val block: () -> String = { kRandomCommon.randomString(7, false, numbers) }
        return generateRandomString(block, numbers)
    }

    override fun randomDatas(size: Int): List<String> {
        isValidSize(size)

        return (1..size).map { randomData() }.toList()
    }

}
