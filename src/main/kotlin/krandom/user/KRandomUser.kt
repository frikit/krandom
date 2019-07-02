package krandom.user

interface KRandomUser<T> {

    fun randomData(): T

    fun randomDatas(): List<T>

    fun randomDatas(size: Int): List<T>
}