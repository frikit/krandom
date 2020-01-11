package org.github.krandom.testhelper

import org.github.krandom.common.numbers.NaturalNumberGenerator
import org.github.krandom.games.coin.enum.CoinResult
import java.util.stream.Collectors

//double
fun isBiggerOrEqual(actual: Double, expected: Double) {
    assert(actual >= expected) { "$actual >= $expected -> ${actual >= expected}" }
}

fun isLesserOrEqual(actual: Double, expected: Double) {
    assert(actual <= expected) { "$actual <= $expected -> ${actual <= expected}" }
}

fun isSmaller(actual: Double, expected: Double) {
    assert(actual < expected) { "$actual < $expected -> ${actual < expected}" }
}

fun isBigger(actual: Double, expected: Double) {
    assert(actual > expected) { "$actual > $expected -> ${actual > expected}" }
}

//float
fun isBiggerOrEqual(actual: Float, expected: Float) {
    assert(actual >= expected) { "$actual >= $expected -> ${actual >= expected}" }
}

fun isLesserOrEqual(actual: Float, expected: Float) {
    assert(actual <= expected) { "$actual <= $expected -> ${actual <= expected}" }
}

fun isSmaller(actual: Float, expected: Float) {
    assert(actual < expected) { "$actual < $expected -> ${actual < expected}" }
}

fun isBigger(actual: Float, expected: Float) {
    assert(actual > expected) { "$actual > $expected -> ${actual > expected}" }
}

//long
fun isBiggerOrEqual(actual: Long, expected: Long) {
    assert(actual >= expected) { "$actual >= $expected -> ${actual >= expected}" }
}

fun isLesserOrEqual(actual: Long, expected: Long) {
    assert(actual <= expected) { "$actual <= $expected -> ${actual <= expected}" }
}

fun isSmaller(actual: Long, expected: Long) {
    assert(actual < expected) { "$actual < $expected -> ${actual < expected}" }
}

fun isBigger(actual: Long, expected: Long) {
    assert(actual > expected) { "$actual > $expected -> ${actual > expected}" }
}

//int
fun isBiggerOrEqual(actual: Int, expected: Int) {
    assert(actual >= expected) { "$actual >= $expected -> ${actual >= expected}" }
}

fun isLesserOrEqual(actual: Int, expected: Int) {
    assert(actual <= expected) { "$actual <= $expected -> ${actual <= expected}" }
}

fun isSmaller(actual: Int, expected: Int) {
    assert(actual < expected) { "$actual < $expected -> ${actual < expected}" }
}

fun isBigger(actual: Int, expected: Int) {
    assert(actual > expected) { "$actual > $expected -> ${actual > expected}" }
}

private fun isSortedList(list: List<Long>): Boolean {
    return list.stream().sorted().collect(Collectors.toList()) == list
}

fun isSorted(list: List<Long>) {
    val isSort = isSortedList(list)
    assert(isSort) { "List[${list.size}] should be sorted, but it is not! {${list.take(10)}}" }
}

fun isNotSorted(list: List<Long>) {
    val isSort = isSortedList(list)
    assert(!isSort) { "List[${list.size}] should be sorted, but it is not! {${list.take(10)}}" }
}

//natural
private fun fitInRange(actual: Long, from: Long, to: Long) {
    assert(actual >= from) { "Number [$actual] should be >= $from" }
    assert(actual <= to) { "Number [$actual] should be <= $to" }
}

fun isNaturalNumberInRange(testCaseName: String, actual: Long, from: Long, to: Long) {
    assert(NaturalNumberGenerator.isNaturalNumber(actual)) { "Number [$actual] should be $testCaseName!" }
    fitInRange(actual, from, to)
}

fun isNaturalCompositeNumberInRange(testCaseName: String, actual: Long, from: Long, to: Long) {
    assert(NaturalNumberGenerator.isCompositeNumber(actual)) { "Number [$actual] should be $testCaseName!" }
    fitInRange(actual, from, to)
}

fun isNaturalPrimeNumberInRange(testCaseName: String, actual: Long, from: Long, to: Long) {
    assert(NaturalNumberGenerator.isPrimeNumber(actual)) { "Number [$actual] should be $testCaseName!" }
    fitInRange(actual, from, to)
}

//coin
fun isValidCoin(value: CoinResult, head: CoinResult = CoinResult.HEAD, tail: CoinResult = CoinResult.TAIL) {
    assert(value == head || value == tail) { "$value should be $head OR $tail" }
}

fun isValidCoin(values: List<CoinResult>, times: Int, head: CoinResult = CoinResult.HEAD, tail: CoinResult = CoinResult.TAIL) {
    assert(!values.isNullOrEmpty()) { "List is empty or have nulls! [$values]" }
    assert(values.size == times) { "Should generate [$times], but it generate [${values.size}]" }
    for (value in values) {
        isValidCoin(value, head, tail)
    }
}

//ip
fun isValidIP(ips: List<String>) {
    ips.forEach { ip ->
        val part1 = ip.split(".")[0].toInt()
        val part2 = ip.split(".")[1].toInt()
        val part3 = ip.split(".")[2].toInt()
        val part4 = ip.split(".")[3].toInt()
        //part 1
        assert(part1 >= 0) { "[$part1] should be >= 0!" }
        assert(part1 <= 223) { "[$part1] should be <= 223!" }

        //part 2
        assert(part2 >= 0) { "[$part2] should be >= 0!" }
        assert(part2 <= 255) { "[$part2] should be <= 255!" }

        //part 3
        assert(part3 >= 0) { "[$part3] should be >= 0!" }
        assert(part3 <= 255) { "[$part3] should be <= 255!" }

        //part 4
        assert(part4 >= 0) { "[$part4] should be >= 0!" }
        assert(part4 <= 255) { "[$part4] should be <= 255!" }
    }
}
