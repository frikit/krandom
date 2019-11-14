package org.github.krandom.algorithms

import org.github.krandom.utils.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object FibonacciAlgorithmSpek : Spek({

    val startExp: List<Long> = listOf(0, 1, 1, 2, 3, 5)
    val endExp: List<Long> = listOf(1779979416004714189, 2880067194370816120, 4660046610375530309, 7540113804746346429)
            .reversed()

    val fibonacciTillMaxLong: List<Long> by lazy {
        arrayListOf(
                0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89,
                144, 233, 377, 610, 987, 1597, 2584, 4181,
                6765, 10946, 17711, 28657, 46368, 75025,
                121393, 196418, 317811, 514229, 832040,
                1346269, 2178309, 3524578, 5702887, 9227465,
                14930352, 24157817, 39088169, 63245986, 102334155,
                165580141, 267914296, 433494437, 701408733,
                1134903170, 1836311903, 2971215073, 4807526976,
                7778742049, 12586269025, 20365011074, 32951280099,
                53316291173, 86267571272, 139583862445, 225851433717,
                365435296162, 591286729879, 956722026041,
                1548008755920, 2504730781961, 4052739537881,
                6557470319842, 10610209857723, 17167680177565,
                27777890035288, 44945570212853, 72723460248141,
                117669030460994, 190392490709135, 308061521170129,
                498454011879264, 806515533049393, 1304969544928657,
                2111485077978050, 3416454622906707, 5527939700884757,
                8944394323791464, 14472334024676221,
                23416728348467685, 37889062373143906, 61305790721611591,
                99194853094755497, 160500643816367088, 259695496911122585,
                420196140727489673, 679891637638612258, 1100087778366101931,
                1779979416004714189, 2880067194370816120, 4660046610375530309,
                7540113804746346429
        )
    }

    describe("a random tests for fibonacci algorithm") {

        TestLifecycle.onTestStart { "get number with invalid index -1" }
        describe("get number with index -1") {
            it("should throw exception") {
                try {
                    FibonacciAlgorithm.getNumber(-1)
                } catch (exp: IllegalArgumentException) {
                    assert(exp.message != "")
                    assert(exp.message == "Index should be >= 0!")
                }
            }
        }
        TestLifecycle.onTestFinish { "get number with invalid index -1" }

        TestLifecycle.onTestStart { "get number with invalid index 93" }
        describe("get number with index 93") {
            it("should throw exception") {
                try {
                    FibonacciAlgorithm.getNumber(-1)
                } catch (exp: IllegalArgumentException) {
                    assert(exp.message != "")
                    assert(exp.message == "Index should be >= 0!")
                }
            }
        }
        TestLifecycle.onTestFinish { "get number with invalid index 93" }

        for (i in startExp.indices) {
            TestLifecycle.onTestStart { "get number with valid index $i" }
            describe("get number with index $i") {
                it("should ${startExp[i]}") {
                    assert(FibonacciAlgorithm.getNumber(i) == startExp[i])
                }
            }
            TestLifecycle.onTestFinish { "get number with valid index $i" }
        }

        for ((incrementation, i) in (92 downTo 92 - (endExp.size - 1)).withIndex()) {

            TestLifecycle.onTestStart { "get number with valid index $i" }
            describe("get number with index $i") {
                it("should ${endExp[incrementation]}") {
                    assert(FibonacciAlgorithm.getNumber(i) == endExp[incrementation])
                }
            }
            TestLifecycle.onTestFinish { "get number with valid index $i" }

        }

        TestLifecycle.onTestStart { "get random fibonacci number" }
        describe("get random fibonacci number") {
            for (i in fibonacciTillMaxLong.indices) {
                val rnd = FibonacciAlgorithm.randomNumber()
                it("value [$rnd] should be in expected") {
                    assert(fibonacciTillMaxLong.contains(rnd))
                }
            }
        }
        TestLifecycle.onTestFinish { "get random fibonacci number" }

        TestLifecycle.onTestStart { "generate random fibonacci sequence all default" }
        describe("generate random fibonacci sequence all default") {
            val seq: List<Long> = FibonacciAlgorithm.generateSequence()
            it("seq should be the same as expected") {
                assert(seq == fibonacciTillMaxLong)
            }
        }
        TestLifecycle.onTestFinish { "generate random fibonacci sequence all default" }

        TestLifecycle.onTestStart { "generate random fibonacci sequence from 1" }
        describe("generate random fibonacci sequence from 1") {
            val seq: List<Long> = FibonacciAlgorithm.generateSequence(from = 1)
            val exp = fibonacciTillMaxLong.subList(1, fibonacciTillMaxLong.size)
            it("seq should be the same as expected without first") {
                assert(seq == exp)
            }
        }
        TestLifecycle.onTestFinish { "generate random fibonacci sequence from 1" }

    }
})
