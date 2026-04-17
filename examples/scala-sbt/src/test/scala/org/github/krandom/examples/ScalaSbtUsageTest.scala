package org.github.krandom.examples

import org.github.krandom.generator.Generators
import org.scalatest.funsuite.AnyFunSuite

class ScalaSbtUsageTest extends AnyFunSuite {

  test("core can generate fixture data from scala") {
    val fixture = UserFixture(
      name = Generators.ofFullName().generate(),
      email = Generators.ofEmail().generate(),
      country = Generators.ofCountry().generate()
    )

    assert(fixture.name.nonEmpty)
    assert(fixture.email.contains("@"))
    assert(fixture.country.nonEmpty)
  }
}
