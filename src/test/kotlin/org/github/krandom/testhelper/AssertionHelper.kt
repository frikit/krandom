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
