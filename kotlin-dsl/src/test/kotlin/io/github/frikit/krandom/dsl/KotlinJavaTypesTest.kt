/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

class KotlinJavaTypesTest :
    DescribeSpec({

        describe("Java types built from Kotlin types") {

            it("maps the owner's type parameter to its type variable and any other to its erasure") {
                val ownerParameter = KMixed::class.typeParameters.single().createType()
                val otherParameter = KBox::class.typeParameters.single().createType()

                KotlinJavaTypes.of(ownerParameter, KMixed::class.java) shouldBe KMixed::class.java.typeParameters[0]
                KotlinJavaTypes.of(otherParameter, KMixed::class.java) shouldBe Any::class.java
            }

            it("boxes a primitive where it is nullable or asked to be") {
                KotlinJavaTypes.of(typeOf<Int>(), Any::class.java) shouldBe Int::class.javaPrimitiveType
                KotlinJavaTypes.of(typeOf<Int?>(), Any::class.java) shouldBe Int::class.javaObjectType
                KotlinJavaTypes.of(typeOf<Int>(), Any::class.java, boxed = true) shouldBe Int::class.javaObjectType
            }

            it("builds parameterized types with their projections") {
                KotlinJavaTypes.of(typeOf<List<Int>>(), Any::class.java).typeName shouldBe
                    "java.util.List<java.lang.Integer>"
                KotlinJavaTypes.of(typeOf<MutableList<out String>>(), Any::class.java).typeName shouldBe
                    "java.util.List<? extends java.lang.String>"
                KotlinJavaTypes.of(typeOf<MutableList<in String>>(), Any::class.java).typeName shouldBe
                    "java.util.List<? super java.lang.String>"
                KotlinJavaTypes.of(typeOf<List<*>>(), Any::class.java).typeName shouldBe "java.util.List<?>"

                val contravariant =
                    KotlinJavaTypes.of(typeOf<MutableList<in String>>(), Any::class.java) as ParameterizedType
                val wildcard = contravariant.actualTypeArguments.single().shouldBeInstanceOf<WildcardType>()
                wildcard.upperBounds.toList() shouldBe listOf(Any::class.java)
                wildcard.lowerBounds.toList() shouldBe listOf(String::class.java)
                contravariant.rawType shouldBe List::class.java
                contravariant.ownerType shouldBe null

                val entry = KotlinJavaTypes.of(typeOf<Map.Entry<String, Int>>(), Any::class.java) as ParameterizedType
                entry.ownerType shouldBe Map::class.java
            }

            it("builds an array of a class as that class's array, at any depth") {
                KotlinJavaTypes.of(typeOf<Array<String>>(), Any::class.java) shouldBe Array<String>::class.java
                KotlinJavaTypes.of(typeOf<Array<Int>>(), Any::class.java) shouldBe Array<Int>::class.java
                KotlinJavaTypes.of(typeOf<IntArray>(), Any::class.java) shouldBe IntArray::class.java
                KotlinJavaTypes.of(typeOf<Array<*>>(), Any::class.java) shouldBe Array<Any>::class.java
                KotlinJavaTypes.of(typeOf<List<Array<String>>>(), Any::class.java).typeName shouldBe
                    "java.util.List<java.lang.String[]>"
            }

            it("builds a generic array for an array of a parameterized type or of a type variable") {
                val ofLists = KotlinJavaTypes.of(typeOf<Array<List<String>>>(), Any::class.java)
                val ownerParameter = KMixed::class.typeParameters.single().createType()
                val ofVariables =
                    KotlinJavaTypes.of(
                        Array::class.createType(listOf(KTypeProjection.invariant(ownerParameter))),
                        KMixed::class.java,
                    )

                ofLists.typeName shouldBe "java.util.List<java.lang.String>[]"
                ofLists
                    .shouldBeInstanceOf<GenericArrayType>()
                    .genericComponentType.typeName shouldBe "java.util.List<java.lang.String>"
                ofVariables.typeName shouldBe "T[]"
            }
        }
    })
