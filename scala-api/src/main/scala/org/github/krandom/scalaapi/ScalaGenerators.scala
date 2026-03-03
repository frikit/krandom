package org.github.krandom.scalaapi

import org.github.krandom.generator.Generators
import org.github.krandom.generator.schema.{Field, SchemaValueProvider}

import java.util.Locale
import scala.jdk.CollectionConverters._

/**
 * Scala entrypoint for Java-backed generators.
 */
object ScalaGenerators {

  def defaults: ScalaConfig = ScalaConfig()

  def int(): ScalaGenerator[Int] = wrapInt(Generators.ofInt())

  def int(min: Int, max: Int): ScalaGenerator[Int] = wrapInt(Generators.ofInt(min, max))

  def int(min: Int, max: Int, seed: Long): ScalaGenerator[Int] = wrapInt(Generators.ofInt(min, max, seed))

  def word(): ScalaGenerator[String] = wrap(Generators.ofWord())

  def sentence(): ScalaGenerator[String] = wrap(Generators.ofSentence())

  def url(): ScalaGenerator[String] = wrap(Generators.ofUrl())

  def email(): ScalaGenerator[String] = wrap(Generators.ofEmail())

  def fullName(): ScalaGenerator[String] = wrap(Generators.ofFullName())

  def fullName(config: ScalaConfig): ScalaGenerator[String] =
    wrap(new org.github.krandom.generator.user.FullNameGenerator(config.toJava))

  def fromType[T](clazz: Class[T]): ScalaGenerator[T] = wrap(Generators.forType(clazz))

  def field(): Field = Generators.ofField()

  def field(locale: Locale): Field = Generators.ofField(locale)

  def schema(fields: Map[String, SchemaValueProvider]): ScalaSchema = {
    val javaSchema = Generators.ofSchema(fields.asJava)
    new ScalaSchema(javaSchema)
  }

  def schema(locale: Locale, fields: Map[String, SchemaValueProvider]): ScalaSchema = {
    val javaSchema = Generators.ofSchema(locale, fields.asJava)
    new ScalaSchema(javaSchema)
  }

  def schema(config: ScalaConfig, fields: Map[String, SchemaValueProvider]): ScalaSchema = {
    val javaSchema = Generators.ofSchema(config.toJava, fields.asJava)
    new ScalaSchema(javaSchema)
  }

  def providerHub(): ScalaProviderHub = new ScalaProviderHub(Generators.ofProviderHub())

  def providerHub(locale: Locale): ScalaProviderHub = new ScalaProviderHub(Generators.ofProviderHub(locale))

  def providerHub(config: ScalaConfig): ScalaProviderHub = new ScalaProviderHub(Generators.ofProviderHub(config.toJava))

  private def wrap[T](generator: org.github.krandom.generator.Generator[T]): ScalaGenerator[T] =
    new ScalaGeneratorImpl[T](generator)

  private def wrapInt(generator: org.github.krandom.generator.Generator[Integer]): ScalaGenerator[Int] =
    new ScalaGenerator[Int] {
      override def one: Int = generator.generate().intValue()

      override def many(count: Int): Vector[Int] =
        generator.generateList(count).asScala.map(_.intValue()).toVector

      override def stream: LazyList[Int] = LazyList.continually(one)

      override def underlying: org.github.krandom.generator.Generator[_] = generator
    }
}
