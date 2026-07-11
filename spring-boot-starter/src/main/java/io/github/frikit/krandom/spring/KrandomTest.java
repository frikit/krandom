/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test slice annotation that starts a Spring test context containing only the krandom
 * auto-configuration beans.
 *
 * <p>Annotating a test class with {@code @KrandomTest} bootstraps the Spring TestContext
 * framework (no extra {@code @ExtendWith} or {@code @SpringBootTest} required), disables full
 * application auto-configuration, and imports only
 * {@link io.github.frikit.krandom.generator.GeneratorConfig},
 * {@link io.github.frikit.krandom.generator.provider.ProviderHub}, and
 * {@link KrandomObjectFakerFactory}.
 *
 * <p>This is the krandom equivalent of Spring Boot's test slice annotations like
 * {@code @DataJpaTest} or {@code @WebMvcTest}. Like those slices, it looks for the test's
 * {@code @SpringBootConfiguration} class (a nested {@code @SpringBootConfiguration} or the
 * application class in a parent package) and requires the Spring Boot test libraries on the test
 * classpath, which {@code spring-boot-starter-test} provides.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   @KrandomTest
 *   class UserFixtureTest {
 *
 *       @Autowired
 *       KrandomObjectFakerFactory factory;
 *
 *       @Test
 *       void generateUser() {
 *           User user = factory.generator(User.class).generate();
 *           assertNotNull(user.getFirstName());
 *       }
 *   }
 * }</pre>
 *
 * <p>Properties such as {@code krandom.seed} and {@code krandom.locale} are
 * honored via {@code @TestPropertySource} or {@code @SpringBootTest(properties = ...)}.
 *
 * <p><b>Note on placement:</b> this annotation intentionally lives in {@code src/main/java}
 * (not {@code src/test/java}) so it is part of the published artifact and usable from
 * consumers' own test suites — the same packaging approach Spring Boot uses for its
 * test slice annotations.
 *
 * @see KrandomAutoConfiguration
 * @see KrandomObjectFakerFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ExtendWith(SpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(KrandomAutoConfiguration.class)
public @interface KrandomTest {
}
