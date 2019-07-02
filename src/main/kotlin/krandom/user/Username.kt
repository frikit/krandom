package krandom.user

import krandom.user.common.GenericUserGenerator

class Username(val numbers: Boolean) : KRandomUser<String>, GenericUserGenerator() {

    override fun randomData(): String {
        var res = kRandomCommon.randomString(7, false, numbers)
        if (numbers) {
            while (!res.contains("[0-9]+".toRegex())) {
                res = kRandomCommon.randomString(7, false, numbers)
            }
        }
        return res
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