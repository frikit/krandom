package krandom.user

import krandom.KRandomUser
import krandom.common.KRandomCommon
import krandom.common.Randomizer
import krandom.utils.ResourceResolver

class FirstName : KRandomUser<String> {

    private val kRandomCommon: KRandomCommon by lazy { Randomizer() }
    private val maxAllowSize = 10_000
    //cache parsed list
    private val names by lazy { nameList() }

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
        validateList(names)
        return names.shuffled().take(size).toList()
    }

    private fun nameList(): List<String> {
        return ResourceResolver
                .getResourceContent("person/firstName/names.txt")
                .split(",\n")
                .toList()
                .requireNoNulls()
    }

    private fun validateList(list: List<String>) {
        if (list.isEmpty()) throw java.lang.IllegalArgumentException("List with options can't be empty!")
        if (list.none { it != "" }) throw java.lang.IllegalArgumentException("List with options can't be empty or consist only of empty elements!")
    }
}