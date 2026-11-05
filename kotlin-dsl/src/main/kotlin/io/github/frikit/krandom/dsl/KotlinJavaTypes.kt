/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.jvm.jvmErasure

/**
 * Builds the Java type that the object engine reads from a Kotlin type, for a type whose reflected
 * Java type does not describe it: one rebuilt from a value class's type arguments, or a parameter of a
 * constructor that Kotlin compiles to a synthetic JVM constructor with no type arguments.
 */
internal object KotlinJavaTypes {
    /**
     * The Java form of [type]: a type parameter of [owner] is the owner's own type variable, which the
     * resolver binds, and any other its erasure; an array of a class is that class's array, at any
     * depth; a primitive is its wrapper where it is [boxed], inside a type argument, or nullable.
     */
    fun of(
        type: KType,
        owner: Class<*>,
        boxed: Boolean = false,
    ): Type {
        val ownerParameter = owner.kotlin.typeParameters.indexOf(type.classifier)
        if (ownerParameter >= 0) {
            return owner.typeParameters[ownerParameter]
        }
        val erasure = type.jvmErasure
        if (erasure.java.isArray && type.arguments.isNotEmpty()) {
            val component =
                type.arguments
                    .single()
                    .type
                    ?.let { element -> of(element, owner, boxed = true) }
            return when (component) {
                null -> {
                    erasure.java
                }

                is Class<*> -> {
                    java.lang.reflect.Array
                        .newInstance(component, 0)
                        .javaClass
                }

                else -> {
                    KotlinGenericArrayType(component)
                }
            }
        }
        val javaClass = if (boxed || type.isMarkedNullable) erasure.javaObjectType else erasure.java
        if (type.arguments.isEmpty()) {
            return javaClass
        }
        return KotlinParameterizedType(javaClass, type.arguments.map { projection -> argument(projection, owner) })
    }

    private fun argument(
        projection: KTypeProjection,
        owner: Class<*>,
    ): Type {
        val argument = projection.type ?: return KotlinWildcardType(Any::class.java, null)
        val javaType = of(argument, owner, boxed = true)
        return when (projection.variance) {
            KVariance.OUT -> KotlinWildcardType(javaType, null)
            KVariance.IN -> KotlinWildcardType(Any::class.java, javaType)
            else -> javaType
        }
    }
}

// The three types below equal the JDK's reflected types of the same structure, in both directions and
// with the same hash, as their interfaces require, so a contextual generator that compares or caches by
// GenerationContext.getDeclaredType() treats them as it treats reflected types.

private class KotlinParameterizedType(
    private val raw: Class<*>,
    private val arguments: List<Type>,
) : ParameterizedType {
    override fun getRawType(): Type = raw

    override fun getActualTypeArguments(): Array<Type> = arguments.toTypedArray()

    override fun getOwnerType(): Type? = raw.declaringClass

    override fun equals(other: Any?): Boolean =
        other is ParameterizedType &&
            raw == other.rawType &&
            ownerType == other.ownerType &&
            actualTypeArguments.contentEquals(other.actualTypeArguments)

    override fun hashCode(): Int = actualTypeArguments.contentHashCode() xor ownerType.hashCode() xor raw.hashCode()

    override fun toString(): String = arguments.joinToString(", ", "${raw.typeName}<", ">") { it.typeName }
}

private class KotlinWildcardType(
    private val upper: Type,
    private val lower: Type?,
) : WildcardType {
    override fun getUpperBounds(): Array<Type> = arrayOf(upper)

    override fun getLowerBounds(): Array<Type> = listOfNotNull(lower).toTypedArray()

    override fun equals(other: Any?): Boolean =
        other is WildcardType &&
            upperBounds.contentEquals(other.upperBounds) &&
            lowerBounds.contentEquals(other.lowerBounds)

    override fun hashCode(): Int = lowerBounds.contentHashCode() xor upperBounds.contentHashCode()

    override fun toString(): String =
        when {
            lower != null -> "? super ${lower.typeName}"
            upper == Any::class.java -> "?"
            else -> "? extends ${upper.typeName}"
        }
}

private class KotlinGenericArrayType(
    private val component: Type,
) : GenericArrayType {
    override fun getGenericComponentType(): Type = component

    override fun equals(other: Any?): Boolean = other is GenericArrayType && component == other.genericComponentType

    override fun hashCode(): Int = component.hashCode()

    override fun toString(): String = "${component.typeName}[]"
}
