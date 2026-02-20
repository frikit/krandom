/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.testhelper

import org.github.krandom.common.numbers.NaturalNumberGenerator
import org.github.krandom.common.string.hash.HashGeneratorResult
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

//hex hash
private fun isValidMD5(hash: String, length: Int): Boolean {
    return hash.matches("^[a-fA-F0-9]{$length}$".toRegex())
}

fun isValidHexHash(hash: String,
                   returnProcessorStrategy: HashGeneratorResult = HashGeneratorResult.NONE,
                   length: Int = 32) {

    assert(hash.length == length) { "Hash length = ${hash.length} != expected length = $length" }

    when (returnProcessorStrategy) {
        HashGeneratorResult.UPPER_CASE ->
            assert(isValidMD5(hash, length) && hash.matches("^[A-F0-9]{$length}$".toRegex())) {
                "Hex should contain only upper case letters and numbers, but it is not! [$hash]"
            }
        HashGeneratorResult.LOWER_CASE ->
            assert(isValidMD5(hash, length) && hash.matches("^[a-f0-9]{$length}$".toRegex())) {
                "Hex should contain only lower case letters and numbers, but it is not! [$hash]"
            }
        else ->

            assert(isValidMD5(hash, length)) {
                "Hex should contain only letters and numbers, but it is not! [$hash]"
            }
    }
}