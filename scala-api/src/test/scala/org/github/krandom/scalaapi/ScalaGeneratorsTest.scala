package org.github.krandom.scalaapi

import org.junit.jupiter.api.Test

import java.util.Locale

import org.junit.jupiter.api.Assertions._

class ScalaGeneratorsTest {

  @Test
  def seededIntIsDeterministic(): Unit = {
    val a = ScalaGenerators.int(1, 100, 77L)
    val b = ScalaGenerators.int(1, 100, 77L)

    assertEquals(a.one, b.one)
    assertEquals(a.one, b.one)
  }

  @Test
  def manyAndStreamWork(): Unit = {
    val ints = ScalaGenerators.int(10, 20)
    val batch = ints.many(5)
    assertEquals(5, batch.size)
    assertTrue(batch.forall(v => v >= 10 && v <= 20))

    val firstThree = ints.stream.take(3).toVector
    assertEquals(3, firstThree.size)
  }

  @Test
  def localeConfigAppliesToFullName(): Unit = {
    val nameGen = ScalaGenerators.fullName(ScalaConfig(locale = Locale.GERMANY, seed = Some(7L)))
    val value = nameGen.one
    assertNotNull(value)
    assertFalse(value.isBlank)
  }

  @Test
  def fromTypeWorks(): Unit = {
    val fromType = ScalaGenerators.fromType(classOf[java.lang.Integer])
    val value = fromType.one
    assertNotNull(value)
  }
}
