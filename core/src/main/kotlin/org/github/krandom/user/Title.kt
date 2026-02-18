package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.user.enum.TitleResult

class Title<E : Enum<E>>(val values: List<E>) : KRandomUser<E>, GenericUserGenerator<E>() {

    companion object {
        fun initDefault(): Title<TitleResult> {
            return Title(TitleResult.values().toList())
        }
    }

    private val list = values

    override fun randomData(): E {
        return randomData(list)
    }

    override fun randomDatas(size: Int): List<E> {
        return randomDatas(list, size)
    }
}
