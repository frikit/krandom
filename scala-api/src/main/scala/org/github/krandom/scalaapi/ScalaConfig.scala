package org.github.krandom.scalaapi

import org.github.krandom.generator.GeneratorConfig

import java.nio.charset.{Charset, StandardCharsets}
import java.util.Locale

/**
 * Scala-friendly configuration wrapper for core GeneratorConfig.
 */
final case class ScalaConfig(
    seed: Option[Long] = None,
    charset: Charset = StandardCharsets.US_ASCII,
    minStringLength: Int = 5,
    maxStringLength: Int = 20,
    minCollectionSize: Int = 1,
    maxCollectionSize: Int = 10,
    locale: Locale = Locale.US
) {
  def toJava: GeneratorConfig = {
    val builder = GeneratorConfig
      .builder()
      .charset(charset)
      .stringLength(minStringLength, maxStringLength)
      .collectionSize(minCollectionSize, maxCollectionSize)
      .locale(locale)
    seed.foreach(s => builder.seed(s))
    builder.build()
  }
}

object ScalaConfig {
  def fromJava(config: GeneratorConfig): ScalaConfig =
    ScalaConfig(
      seed = if (config.getSeed.isPresent) Some(config.getSeed.getAsLong) else None,
      charset = config.getCharset,
      minStringLength = config.getMinStringLength,
      maxStringLength = config.getMaxStringLength,
      minCollectionSize = config.getMinCollectionSize,
      maxCollectionSize = config.getMaxCollectionSize,
      locale = config.getLocale
    )
}
