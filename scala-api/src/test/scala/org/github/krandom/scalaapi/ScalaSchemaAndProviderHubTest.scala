package org.github.krandom.scalaapi

import org.github.krandom.generator.provider.ConflictPolicy
import org.junit.jupiter.api.Test

import java.util.Locale

import org.junit.jupiter.api.Assertions._

class ScalaSchemaAndProviderHubTest {

  @Test
  def schemaWrapperGeneratesExpectedKeys(): Unit = {
    val field = ScalaGenerators.field(Locale.US)
    val schema = ScalaGenerators.schema(
      Locale.US,
      Map(
        "name" -> field.bind("person.full_name"),
        "email" -> field.bind("person.email"),
        "country" -> field.bind("address.country")
      )
    )

    val one = schema.one
    assertTrue(one.contains("name"))
    assertTrue(one.contains("email"))
    assertTrue(one.contains("country"))

    val many = schema.many(3)
    assertEquals(3, many.size)
  }

  @Test
  def providerHubSupportsCustomRegistrationAndAlias(): Unit = {
    val hub = ScalaGenerators.providerHub(Locale.US)
    assertTrue(hub.has("person"))
    assertTrue(hub.providerNames.contains("person"))

    hub.register("custom", _ => "value")
    assertEquals("value", hub.get("custom"))

    hub.alias("c", "custom")
    assertEquals("value", hub.get("c"))

    hub.register("custom", _ => "replacement", ConflictPolicy.REPLACE)
    assertEquals("replacement", hub.get("custom"))
  }
}
