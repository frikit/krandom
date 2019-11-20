package org.github.krandom.user

interface KRandomUser<T> {

    fun randomData(): T

    fun randomDatas(size: Int = 1): List<T>
}
