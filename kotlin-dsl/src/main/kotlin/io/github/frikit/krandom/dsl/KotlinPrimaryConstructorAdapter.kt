/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.ObjectConstructionAdapter
import io.github.frikit.krandom.generator.`object`.ObjectConstructionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Modifier
import kotlin.Metadata
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * Service-loaded bridge that constructs immutable Kotlin values through their primary constructor.
 *
 * The bridge lives in the Kotlin module so `krandom-core` keeps no Kotlin runtime dependency.
 */
class KotlinPrimaryConstructorAdapter : ObjectConstructionAdapter {

    override fun supports(type: Class<*>): Boolean {
        if (!type.isAnnotationPresent(Metadata::class.java)) {
            return false
        }
        val kotlinType = type.kotlin
        if (kotlinType.objectInstance != null || kotlinType.isValue || kotlinType.isSealed
            || Modifier.isAbstract(type.modifiers)) {
            return true
        }
        return kotlinType.primaryConstructor != null
            && type.declaredFields.any { field ->
                !Modifier.isStatic(field.modifiers) && !field.isSynthetic && Modifier.isFinal(field.modifiers)
            }
    }

    override fun construct(context: ObjectConstructionContext<*>): Any {
        val kotlinType = context.type.kotlin
        kotlinType.objectInstance?.let { return it }
        if (kotlinType.isValue) {
            throw UnsupportedOperationException(
                "Kotlin value classes are not supported for object generation; register a type override"
            )
        }
        if (kotlinType.isSealed || Modifier.isAbstract(context.type.modifiers)) {
            throw UnsupportedOperationException(
                "Kotlin sealed and abstract types require a concrete type override"
            )
        }

        val constructor = kotlinType.primaryConstructor
            ?: throw UnsupportedOperationException(
                "Kotlin immutable type ${context.type.name} has no primary constructor"
            )
        val javaParameters = constructor.javaConstructor?.parameters.orEmpty()
        val arguments = LinkedHashMap<KParameter, Any?>()
        constructor.parameters
            .filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
            .forEachIndexed { index, parameter ->
                val name = parameter.name
                    ?: throw UnsupportedOperationException(
                        "Kotlin constructor parameter $index of ${context.type.name} has no stable name"
                    )
                val rawType = parameter.type.jvmErasure.java
                if (parameter.isOptional && !context.hasExplicitOverride(name, rawType)) {
                    return@forEachIndexed
                }
                val annotations = annotationElement(context, parameter, javaParameters.getOrNull(index))
                val value = context.generate(parameter.type.javaType, rawType, name, annotations)
                if (!parameter.type.isMarkedNullable && value == null) {
                    throw IllegalStateException(
                        "Kotlin non-null constructor parameter '$name' of ${context.type.name} resolved to null"
                    )
                }
                arguments[parameter] = value
            }
        return constructor.callBy(arguments)
    }

    private fun propertyField(context: ObjectConstructionContext<*>, parameter: KParameter): AnnotatedElement? =
        context.type.kotlin.memberProperties
            .firstOrNull { property -> property.name == parameter.name }
            ?.javaField

    private fun annotationElement(
        context: ObjectConstructionContext<*>,
        parameter: KParameter,
        javaParameter: AnnotatedElement?
    ): AnnotatedElement? {
        val field = propertyField(context, parameter)
        return when {
            hasGenerationAnnotation(javaParameter) -> javaParameter
            hasGenerationAnnotation(field) -> field
            else -> javaParameter ?: field
        }
    }

    private fun hasGenerationAnnotation(element: AnnotatedElement?): Boolean =
        element?.annotations?.any { annotation ->
            val typeName = annotation.annotationClass.java.name
            typeName.startsWith("jakarta.validation.")
                || typeName.startsWith("io.github.frikit.krandom.generator.object.")
        } == true
}
