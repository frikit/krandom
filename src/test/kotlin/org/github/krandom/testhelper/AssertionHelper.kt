package org.github.krandom.testhelper

import org.github.krandom.common.numbers.NaturalNumberGenerator

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
