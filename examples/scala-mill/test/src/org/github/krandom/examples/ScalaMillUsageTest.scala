package org.github.krandom.examples

import org.github.krandom.scalaapi.ScalaGenerators
import org.scalatest.funsuite.AnyFunSuite

class ScalaMillUsageTest extends AnyFunSuite {

  test("scala api can generate fixture data") {
    val fixture = UserFixture(
      name = ScalaGenerators.fullName().one,
      email = ScalaGenerators.email().one,
      country = ScalaGenerators.word().one
    )

    assert(fixture.name.nonEmpty)
    assert(fixture.email.contains("@"))
    assert(fixture.country.nonEmpty)
  }
}
