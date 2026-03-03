package org.github.krandom.scalaapi

import org.github.krandom.generator.Generator

import scala.jdk.CollectionConverters._

/**
 * Scala-native facade for core Generator.
 */
trait ScalaGenerator[+T] {
  def one: T
  def many(count: Int): Vector[T]
  def stream: LazyList[T]
  def underlying: Generator[_]
}

private[scalaapi] final class ScalaGeneratorImpl[T](private val delegate: Generator[T]) extends ScalaGenerator[T] {
  override def one: T = delegate.generate()

  override def many(count: Int): Vector[T] =
    delegate.generateList(count).asScala.toVector

  override def stream: LazyList[T] = LazyList.continually(delegate.generate())

  override def underlying: Generator[_] = delegate
}
