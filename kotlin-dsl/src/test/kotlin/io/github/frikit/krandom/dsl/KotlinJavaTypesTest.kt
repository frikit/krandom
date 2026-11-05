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

            it("equals the reflected type of the same structure, in both directions and with the same hash") {
                for (
                (kotlinType, field) in
                listOf(
                    typeOf<List<String>>() to "list",
                    typeOf<MutableList<out String>>() to "produced",
                    typeOf<MutableList<in String>>() to "consumed",
                    typeOf<List<*>>() to "any",
                    typeOf<Array<List<String>>>() to "arrays",
                    typeOf<Map.Entry<String, Int>>() to "entry",
                )
                ) {
                    val built = KotlinJavaTypes.of(kotlinType, Any::class.java)
                    val reflected = ReflectedTypes::class.java.getDeclaredField(field).genericType

                    (built == reflected) shouldBe true
                    (reflected == built) shouldBe true
                    built.hashCode() shouldBe reflected.hashCode()
                }
            }

            it("differs from a type of another structure") {
                val list = KotlinJavaTypes.of(typeOf<List<String>>(), Any::class.java)
                val produced =
                    KotlinJavaTypes.of(
                        typeOf<MutableList<out String>>(),
                        Any::class.java,
                    ) as ParameterizedType
                val consumed =
                    KotlinJavaTypes.of(
                        typeOf<MutableList<in String>>(),
                        Any::class.java,
                    ) as ParameterizedType
                val arrays = KotlinJavaTypes.of(typeOf<Array<List<String>>>(), Any::class.java)

                (list == KotlinJavaTypes.of(typeOf<List<Int>>(), Any::class.java)) shouldBe false
                (list == KotlinJavaTypes.of(typeOf<Set<String>>(), Any::class.java)) shouldBe false
                (list == String::class.java) shouldBe false
                (produced.actualTypeArguments[0] == consumed.actualTypeArguments[0]) shouldBe false
                (produced.actualTypeArguments[0] == String::class.java) shouldBe false
                (arrays == KotlinJavaTypes.of(typeOf<Array<List<Int>>>(), Any::class.java)) shouldBe false
                (arrays == list) shouldBe false
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

/** Fields whose reflected generic types the built types must equal. */
@Suppress("unused")
private class ReflectedTypes {
    val list: List<String> = emptyList()
    val produced: MutableList<out String> = mutableListOf()
    val consumed: MutableList<in String> = mutableListOf()
    val any: List<*> = emptyList<Any>()
    val arrays: Array<List<String>> = emptyArray()
    val entry: Map.Entry<String, Int>? = null
}
