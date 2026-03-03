package org.github.krandom.scalaapi

import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.util.Locale

import org.junit.jupiter.api.Assertions._

class ScalaConfigTest {

  @Test
  def toJavaAndBackRoundTrip(): Unit = {
    val cfg = ScalaConfig(
      seed = Some(42L),
      charset = StandardCharsets.UTF_8,
      minStringLength = 3,
      maxStringLength = 8,
      minCollectionSize = 0,
      maxCollectionSize = 4,
      locale = Locale.JAPAN
    )

    val javaCfg = cfg.toJava
    assertTrue(javaCfg.getSeed.isPresent)
    assertEquals(42L, javaCfg.getSeed.getAsLong)
    assertEquals(StandardCharsets.UTF_8, javaCfg.getCharset)
    assertEquals(3, javaCfg.getMinStringLength)
    assertEquals(8, javaCfg.getMaxStringLength)
    assertEquals(0, javaCfg.getMinCollectionSize)
    assertEquals(4, javaCfg.getMaxCollectionSize)
    assertEquals(Locale.JAPAN, javaCfg.getLocale)

    val roundTrip = ScalaConfig.fromJava(javaCfg)
    assertEquals(cfg, roundTrip)
  }
}
