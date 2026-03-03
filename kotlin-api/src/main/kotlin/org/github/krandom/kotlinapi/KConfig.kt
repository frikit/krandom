/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.GeneratorConfig
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Kotlin-friendly config wrapper converted to [GeneratorConfig] for core generators.
 */
data class KConfig(
    val seed: Long? = null,
    val charset: Charset = StandardCharsets.US_ASCII,
    val minStringLength: Int = 5,
    val maxStringLength: Int = 20,
    val minCollectionSize: Int = 1,
    val maxCollectionSize: Int = 10,
    val locale: Locale = Locale.US
) {
    fun toJava(): GeneratorConfig {
        val builder = GeneratorConfig.builder()
            .charset(charset)
            .stringLength(minStringLength, maxStringLength)
            .collectionSize(minCollectionSize, maxCollectionSize)
            .locale(locale)
        if (seed != null) {
            builder.seed(seed)
        }
        return builder.build()
    }

    companion object {
        fun fromJava(config: GeneratorConfig): KConfig = KConfig(
            seed = if (config.seed.isPresent) config.seed.asLong else null,
            charset = config.charset,
            minStringLength = config.minStringLength,
            maxStringLength = config.maxStringLength,
            minCollectionSize = config.minCollectionSize,
            maxCollectionSize = config.maxCollectionSize,
            locale = config.locale
        )
    }
}
