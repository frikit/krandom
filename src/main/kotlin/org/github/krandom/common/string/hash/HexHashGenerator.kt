package org.github.krandom.common.string.hash

import org.github.krandom.common.Randomizer
import java.util.*

object HexHashGenerator {

    private const val MD5_DEFAULT = 32
    private const val MAX_ALLOW = 1024
    private fun validateMsg(actualLength: Int) = "I generate hex hash > 0 && <= $MAX_ALLOW but the input = $actualLength!"

    private val kRandom = Randomizer()

    private fun transformResult(result: String, returnProcessorStrategy: HashGeneratorResult): String {
        return when (returnProcessorStrategy) {
            HashGeneratorResult.UPPER_CASE -> result.toUpperCase()
            HashGeneratorResult.LOWER_CASE -> result.toLowerCase()
            else -> result
        }
    }

    private fun validateResult(length: Int) {
        require(length > 0) { validateMsg(length) }
        require(length <= MAX_ALLOW) { validateMsg(length) }
    }

    private fun generateHexSeq(length: Int): String {
        var size = 0
        val builder = StringBuilder(length)

        while (size < length) {
            val integer = kRandom.randomInt()
            val hex = Integer.toHexString(integer)
            builder.append(hex)
            size += hex.length
        }

        return builder.toString().take(length)
    }

    fun randomHexHash(length: Int = MD5_DEFAULT, returnProcessorStrategy: HashGeneratorResult = HashGeneratorResult.NONE): String {
        validateResult(length)

        val res = if (length <= MD5_DEFAULT) {
            //uuid default impl work very fast vs mine own impl
            UUID.randomUUID().toString().replace("-".toRegex(), "").take(length)
        } else {
            //TODO maybe to call uuid multiple times and clean result and return it??
            //TODO will be faster and better?!
            generateHexSeq(length)
        }

        return transformResult(res, returnProcessorStrategy)
    }


}