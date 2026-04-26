/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test slice annotation that loads only the krandom auto-configuration beans.
 *
 * <p>Annotating a test class with {@code @KrandomTest} enables a lightweight
 * Spring application context containing only {@link io.github.frikit.krandom.generator.GeneratorConfig},
 * {@link io.github.frikit.krandom.generator.provider.ProviderHub}, and
 * {@link KrandomObjectFakerFactory} — without pulling in the full application context.
 *
 * <p>This is the krandom equivalent of Spring Boot's test slice annotations like
 * {@code @DataJpaTest} or {@code @WebMvcTest}.
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
 * @see KrandomAutoConfiguration
 * @see KrandomObjectFakerFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(KrandomAutoConfiguration.class)
public @interface KrandomTest {
}
