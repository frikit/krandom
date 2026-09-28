/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.provider.ProviderHub;

/**
 * Callback that registers providers and aliases on the auto-configured {@link ProviderHub} once,
 * when the hub bean is created.
 *
 * <p>Declare customizers as beans (in application configuration or a test's
 * {@code @TestConfiguration}); they run in {@link org.springframework.core.Ordered} /
 * {@link org.springframework.core.annotation.Order @Order} order before the hub is injected
 * anywhere. Registering providers here instead of in a {@code @BeforeEach} method avoids
 * {@code "Provider already registered"} failures when the Spring TestContext framework reuses a
 * cached context, and therefore the same hub, across test methods and classes.
 *
 * <pre>{@code
 *   @Bean
 *   KrandomProviderCustomizer orderNumbers() {
 *       return hub -> hub.register("order.number", config -> Generators.ofProviderTemplate("ORD-#####", config));
 *   }
 * }</pre>
 *
 * <p>Customizers apply only to the auto-configured hub; an application-defined {@code ProviderHub}
 * bean is used as is.
 */
@FunctionalInterface
public interface KrandomProviderCustomizer {

    /**
     * Customizes the provider hub before it is published as a bean.
     *
     * @param providerHub the auto-configured provider hub
     */
    void customize(ProviderHub providerHub);
}
