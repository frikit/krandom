package org.github.krandom.scalaapi

import org.github.krandom.generator.provider.{ConflictPolicy, ProviderHub}

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

/**
 * Scala wrapper around ProviderHub.
 */
final class ScalaProviderHub(private val delegate: ProviderHub) {
  def get(name: String): Any = delegate.get(name)

  def getAs[T: ClassTag](name: String): T = {
    val runtimeClass = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    delegate.get(name, runtimeClass)
  }

  def has(name: String): Boolean = delegate.has(name)

  def register(name: String, factory: org.github.krandom.generator.GeneratorConfig => Any): ScalaProviderHub = {
    delegate.register(name, (config: org.github.krandom.generator.GeneratorConfig) => factory(config))
    this
  }

  def register(name: String, factory: org.github.krandom.generator.GeneratorConfig => Any, policy: ConflictPolicy): ScalaProviderHub = {
    delegate.register(name, (config: org.github.krandom.generator.GeneratorConfig) => factory(config), policy)
    this
  }

  def alias(alias: String, target: String): ScalaProviderHub = {
    delegate.registerAlias(alias, target)
    this
  }

  def alias(alias: String, target: String, policy: ConflictPolicy): ScalaProviderHub = {
    delegate.registerAlias(alias, target, policy)
    this
  }

  def providerNames: Set[String] = delegate.providerNames().asScala.toSet

  def aliases: Map[String, String] = delegate.aliases().asScala.toMap

  def underlying: ProviderHub = delegate
}
