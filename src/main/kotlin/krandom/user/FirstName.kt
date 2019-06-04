package krandom.user

import krandom.KRandomUser
import krandom.common.KRandomCommon
import krandom.common.Randomizer
import java.io.File
import java.nio.charset.Charset

val file = File("src/main/resources/person/firstName/names.txt")

class FirstName : KRandomUser<String> {

    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val maxAllowSize = 10_000

    override fun randomData(): String {
        val list = randomDatas()
        val index = kRandomCommon.randomInt(0, list.size - 1)
        return list[index]
    }

    override fun randomDatas(): List<String> {
        return randomDatas(maxAllowSize)
    }

    override fun randomDatas(size: Int): List<String> {
        if (size > maxAllowSize) IllegalArgumentException("Size cannot be > 10_000!")
        val list = simpleParse()
        return list.shuffled().take(size).toList()
    }

    private fun simpleParse(): List<String> {
        return file
                .readText(Charset.defaultCharset())
                .split(",\n")
                .toList()
    }
}