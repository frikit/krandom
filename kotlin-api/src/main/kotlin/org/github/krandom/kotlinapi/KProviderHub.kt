/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.provider.ConflictPolicy
import org.github.krandom.generator.provider.ProviderHub

/**
 * Kotlin wrapper around ProviderHub.
 */
class KProviderHub(@PublishedApi internal val delegate: ProviderHub) {

    fun get(name: String): Any = delegate.get(name)

    inline fun <reified T> getAs(name: String): T = delegate.get(name, T::class.java)

    fun has(name: String): Boolean = delegate.has(name)

    fun register(name: String, factory: (org.github.krandom.generator.GeneratorConfig) -> Any): KProviderHub {
        delegate.register(name) { cfg -> factory(cfg) }
        return this
    }

    fun register(
        name: String,
        factory: (org.github.krandom.generator.GeneratorConfig) -> Any,
        policy: ConflictPolicy
    ): KProviderHub {
        delegate.register(name, { cfg -> factory(cfg) }, policy)
        return this
    }

    fun alias(alias: String, target: String): KProviderHub {
        delegate.registerAlias(alias, target)
        return this
    }

    fun alias(alias: String, target: String, policy: ConflictPolicy): KProviderHub {
        delegate.registerAlias(alias, target, policy)
        return this
    }

    fun providerNames(): Set<String> = delegate.providerNames()

    fun aliases(): Map<String, String> = delegate.aliases()

    fun underlying(): ProviderHub = delegate
}
