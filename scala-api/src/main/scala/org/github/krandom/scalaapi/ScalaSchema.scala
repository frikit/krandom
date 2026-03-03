package org.github.krandom.scalaapi

import org.github.krandom.generator.schema.Schema

import scala.jdk.CollectionConverters._

/**
 * Scala wrapper over Schema bulk generation.
 */
final class ScalaSchema(private val delegate: Schema) {
  def one: Map[String, Any] =
    delegate.generate().asScala.map { case (k, v) => k -> v.asInstanceOf[Any] }.toMap

  def many(count: Int): Vector[Map[String, Any]] =
    delegate
      .generateBatch(count)
      .asScala
      .map(_.asScala.map { case (k, v) => k -> v.asInstanceOf[Any] }.toMap)
      .toVector

  def locale = delegate.getLocale

  def underlying: Schema = delegate
}
