/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.ObjectConstructionAdapter
import io.github.frikit.krandom.generator.`object`.ObjectConstructionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import java.util.Optional
import kotlin.Metadata
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * Service-loaded bridge that constructs immutable Kotlin values through their primary constructor,
 * boxes value classes through theirs, and lists the subclasses of sealed types.
 *
 * The bridge lives in the Kotlin module so `krandom-core` keeps no Kotlin runtime dependency. It is
 * internal to Kotlin callers; `ServiceLoader` still finds it, because Kotlin compiles an internal
 * class and its constructor to public JVM members.
 */
internal class KotlinPrimaryConstructorAdapter : ObjectConstructionAdapter {
    override fun supports(type: Class<*>): Boolean {
        if (!type.isAnnotationPresent(Metadata::class.java)) {
            return false
        }
        val kotlinType = type.kotlin
        if (kotlinType.objectInstance != null || kotlinType.isValue || kotlinType.isSealed ||
            Modifier.isAbstract(type.modifiers)
        ) {
            return true
        }
        val constructor = kotlinType.primaryConstructor ?: return false
        // A constructor that takes a value class is private on the JVM, beside a synthetic public form,
        // so only its Kotlin form can be called. A class with a no-argument constructor of its own is
        // still built through that one and filled field by field, rules on its other fields included.
        val needsKotlinForm =
            constructor.parameters.any { parameter -> parameter.type.jvmErasure.isValue } &&
                type.declaredConstructors.none { candidate -> candidate.parameterCount == 0 }
        return needsKotlinForm ||
            type.declaredFields.any { field ->
                !Modifier.isStatic(field.modifiers) && !field.isSynthetic && Modifier.isFinal(field.modifiers)
            }
    }

    /** Lists a Kotlin sealed type's subclasses from its metadata, whatever JVM target compiled it. */
    override fun sealedSubclasses(type: Class<*>): Optional<List<Class<*>>> {
        val kotlinType = type.kotlin
        return if (kotlinType.isSealed) {
            Optional.of(kotlinType.sealedSubclasses.map { subclass -> subclass.java })
        } else {
            Optional.empty()
        }
    }

    override fun construct(context: ObjectConstructionContext<*>): Any {
        val kotlinType = context.type.kotlin
        kotlinType.objectInstance?.let { return it }
        if (Modifier.isAbstract(context.type.modifiers)) {
            throw UnsupportedOperationException(
                "Kotlin abstract type ${context.type.name} requires a concrete type override",
            )
        }

        val constructor =
            kotlinType.primaryConstructor
                ?: throw UnsupportedOperationException(
                    "Kotlin immutable type ${context.type.name} has no primary constructor",
                )
        // A private constructor is called too, as the object engine calls a Java one.
        constructor.isAccessible = true
        val javaParameters = jvmParameters(context.type, constructor)
        val arguments = LinkedHashMap<KParameter, Any?>()
        constructor.parameters
            .filter { parameter -> parameter.kind == KParameter.Kind.VALUE }
            .forEachIndexed { index, parameter ->
                val name =
                    parameter.name
                        ?: throw UnsupportedOperationException(
                            "Kotlin constructor parameter $index of ${context.type.name} has no stable name",
                        )
                val rawType = parameter.type.jvmErasure.java
                if (context.isExcluded(name)) {
                    // Exclusions win over overrides, as they do for fields.
                    when {
                        parameter.isOptional -> {
                            return@forEachIndexed
                        }

                        parameter.type.isMarkedNullable -> {
                            arguments[parameter] = null
                            return@forEachIndexed
                        }

                        else -> {
                            throw IllegalStateException(
                                "Excluded Kotlin constructor parameter '$name' of ${context.type.name} " +
                                    "is required and not nullable",
                            )
                        }
                    }
                }
                if (parameter.isOptional && !context.hasExplicitOverride(name, rawType)) {
                    return@forEachIndexed
                }
                val annotations = annotationElement(context.type, parameter, javaParameters.getOrNull(index))
                val value = generateArgument(context, parameter.type, name, annotations)
                if (!parameter.type.isMarkedNullable && value == null) {
                    throw IllegalStateException(
                        "Kotlin non-null constructor parameter '$name' of ${context.type.name} resolved to null",
                    )
                }
                arguments[parameter] = value
            }
        return constructor.callBy(arguments)
    }

    /**
     * Generates an argument of [type]. A value class is generated as the value it wraps, under the
     * parameter's name and constraints, and boxed through its primary constructor, so its `init`
     * block runs; a rule for the parameter or the value class supplies the value class itself. A
     * [rebuilt] type was built from the type arguments of the value classes that wrap it, so its
     * reflected Java type does not describe it.
     */
    private fun generateArgument(
        context: ObjectConstructionContext<*>,
        type: KType,
        name: String,
        element: AnnotatedElement?,
        rebuilt: Boolean = false,
    ): Any? {
        val valueClass = type.jvmErasure
        if (!valueClass.isValue) {
            return context.generate(javaTypeOf(type, context.type, rebuilt), valueClass.java, name, element)
        }
        val constructor = valueClass.primaryConstructor!!.apply { isAccessible = true }
        if (context.hasExplicitOverride(name, valueClass.java)) {
            val ruled = context.generate(valueClass.java, valueClass.java, name, element)
            // A rule may give the value class, or the value it wraps, as Java code can only do.
            return if (ruled == null || valueClass.isInstance(ruled)) {
                ruled
            } else {
                box(context, valueClass, constructor, ruled, name)
            }
        }
        val underlying = constructor.parameters.single().type
        val wrapped = substituted(underlying, valueClass.typeParameters.zip(type.arguments).toMap())
        val value = generateArgument(context, wrapped, name, element, rebuilt = wrapped !== underlying)
        return if (value == null && !wrapped.isMarkedNullable) {
            null
        } else {
            box(context, valueClass, constructor, value, name)
        }
    }

    /**
     * The Java type of [type]. A type parameter of [owner] is the owner's own type variable, which the
     * resolver binds, also where a value class's type argument passed it on. A [rebuilt] type, and a
     * parameterized type of a constructor that takes a value class, which compiles to a synthetic JVM
     * constructor whose parameters keep no type arguments, take the Java type of their Kotlin form.
     */
    private fun javaTypeOf(
        type: KType,
        owner: Class<*>,
        rebuilt: Boolean,
    ): Type {
        val ownerParameter = owner.kotlin.typeParameters.indexOf(type.classifier)
        if (ownerParameter >= 0) {
            return owner.typeParameters[ownerParameter]
        }
        val reflected = type.javaType
        return if (rebuilt || (reflected is Class<*> && type.arguments.isNotEmpty())) {
            KotlinJavaTypes.of(type, owner)
        } else {
            reflected
        }
    }

    /**
     * [type] with the type parameters in [bindings] replaced by their arguments at any depth, or [type]
     * itself when none occurs. A parameter bound to a star projection stays itself at the top, so the
     * failure names it, and is a star projection inside a type argument.
     */
    private fun substituted(
        type: KType,
        bindings: Map<KTypeParameter, KTypeProjection>,
    ): KType {
        val classifier = type.classifier
        if (classifier is KTypeParameter) {
            val argument = bindings[classifier]?.type ?: return type
            return argument.withNullability(type.isMarkedNullable || argument.isMarkedNullable)
        }
        val arguments =
            type.arguments.map { projection ->
                val argument = projection.type
                val binding = (argument?.classifier as? KTypeParameter)?.let(bindings::get)
                when {
                    argument == null -> projection
                    binding != null && binding.type == null -> KTypeProjection.STAR
                    else -> KTypeProjection(projection.variance, substituted(argument, bindings))
                }
            }
        return if (arguments == type.arguments) {
            type
        } else {
            (classifier as KClass<*>).createType(arguments, type.isMarkedNullable)
        }
    }

    private fun box(
        context: ObjectConstructionContext<*>,
        valueClass: KClass<*>,
        constructor: KFunction<*>,
        value: Any?,
        name: String,
    ): Any =
        try {
            constructor.call(value)!!
        } catch (rejected: InvocationTargetException) {
            throw IllegalStateException(
                "Kotlin value class ${valueClass.java.name} rejected the value generated for '$name' of " +
                    "${context.type.name}; constrain the value it wraps or register a rule",
                rejected.targetException,
            )
        } catch (mismatch: IllegalArgumentException) {
            throw IllegalStateException(
                "Kotlin value class ${valueClass.java.name} cannot hold the ${value?.javaClass?.name} " +
                    "generated for '$name' of ${context.type.name}",
                mismatch,
            )
        }

    /**
     * The JVM parameters that carry the constructor parameters' annotations. A value class's
     * constructor has no JVM constructor of its own; the private one that boxes carries them.
     */
    private fun jvmParameters(
        type: Class<*>,
        constructor: KFunction<*>,
    ): List<Parameter> =
        constructor.javaConstructor?.parameters?.toList()
            ?: type.declaredConstructors
                .single()
                .parameters
                .toList()

    private fun propertyField(
        owner: Class<*>,
        name: String?,
    ): AnnotatedElement? =
        owner.kotlin.memberProperties
            .firstOrNull { property -> property.name == name }
            ?.javaField

    /**
     * The parameter's or its property's generation annotations, else those declared inside the value
     * classes it wraps, outermost first, else the parameter or its property for nullability.
     */
    private fun annotationElement(
        owner: Class<*>,
        parameter: KParameter,
        javaParameter: AnnotatedElement?,
    ): AnnotatedElement? {
        val field = propertyField(owner, parameter.name)
        return listOf(javaParameter, field).firstOrNull(::hasGenerationAnnotation)
            ?: valueClassAnnotationElement(parameter.type)
            ?: javaParameter
            ?: field
    }

    private fun valueClassAnnotationElement(type: KType): AnnotatedElement? {
        val valueClass = type.jvmErasure
        if (!valueClass.isValue) {
            return null
        }
        val underlying = valueClass.primaryConstructor!!.parameters.single()
        val javaParameter =
            valueClass.java.declaredConstructors
                .single()
                .parameters
                .single()
        return listOf(javaParameter, propertyField(valueClass.java, underlying.name))
            .firstOrNull(::hasGenerationAnnotation)
            ?: valueClassAnnotationElement(underlying.type)
    }

    private fun hasGenerationAnnotation(element: AnnotatedElement?): Boolean =
        element?.annotations?.any { annotation ->
            val typeName = annotation.annotationClass.java.name
            typeName.startsWith("jakarta.validation.") ||
                typeName.startsWith("io.github.frikit.krandom.generator.object.")
        } == true
}
