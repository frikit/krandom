/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/** The four seeding settings that 4.0.0 turns on by default, set through {@code krandom.*} properties. */
@ExtendWith(OutputCaptureExtension.class)
class KrandomSeedingPropertiesTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class));

    /** Property name, the recipe setting that records it, and the configuration getter. */
    static Stream<Arguments> seedingSettings() {
        return Stream.of(
                Arguments.of(
                        "krandom.advancing-convenience-streams",
                        "setting.advancing-convenience-streams",
                        (Predicate<GeneratorConfig>) GeneratorConfig::isAdvancingConvenienceStreams),
                Arguments.of(
                        "krandom.strict-math-gaussian", "setting.gaussian.strict-math", (Predicate<GeneratorConfig>)
                                GeneratorConfig::isStrictMathGaussian),
                Arguments.of(
                        "krandom.object-reseed-built-in-randomizers",
                        "setting.object.reseed-built-in-randomizers",
                        (Predicate<GeneratorConfig>) GeneratorConfig::isObjectReseedBuiltInRandomizers),
                Arguments.of("krandom.german-street-layout", "setting.address.german-street-layout", (Predicate<
                                GeneratorConfig>)
                        GeneratorConfig::isGermanStreetLayout));
    }

    @ParameterizedTest(name = "{0} unset keeps the default")
    @MethodSource("seedingSettings")
    void unsetKeepsTheDefault(String property, String recipeSetting, Predicate<GeneratorConfig> setting) {
        runner.run(context -> {
            GeneratorConfig config = context.getBean(GeneratorConfig.class);
            assertEquals(setting.test(GeneratorConfig.defaults()), setting.test(config));
            assertTrue(setting.test(config), property + " is on by default from 4.0.0");
        });
    }

    @ParameterizedTest(name = "{0}=true keeps it on")
    @MethodSource("seedingSettings")
    void trueKeepsItOn(String property, String recipeSetting, Predicate<GeneratorConfig> setting) {
        runner.withPropertyValues(property + "=true").run(context -> {
            assertTrue(setting.test(context.getBean(GeneratorConfig.class)));
        });
    }

    @ParameterizedTest(name = "{0}=false gives the 3.x value and leaves the others on")
    @MethodSource("seedingSettings")
    void falseGivesThe3xValue(String property, String recipeSetting, Predicate<GeneratorConfig> setting) {
        runner.withPropertyValues(property + "=false").run(context -> {
            GeneratorConfig config = context.getBean(GeneratorConfig.class);
            assertEquals(false, setting.test(config), property);
            seedingSettings()
                    .filter(other -> !other.get()[0].equals(property))
                    .forEach(other -> assertTrue(
                            predicate(other).test(config), other.get()[0] + " must stay on when " + property));
        });
    }

    @ParameterizedTest(name = "the logged recipe records {0}=false")
    @MethodSource("seedingSettings")
    void loggedRecipeRecordsFalse(
            String property, String recipeSetting, Predicate<GeneratorConfig> setting, CapturedOutput output) {
        runner.withPropertyValues("krandom.seed=42", property + "=false").run(context -> {
            assertEquals(false, setting.test(context.getBean(GeneratorConfig.class)));
            assertTrue(output.getAll().contains(recipeSetting + "=false"), output.getAll());
        });
    }

    @ParameterizedTest(name = "the logged recipe records {0} on by default")
    @MethodSource("seedingSettings")
    void loggedRecipeRecordsTheDefault(
            String property, String recipeSetting, Predicate<GeneratorConfig> setting, CapturedOutput output) {
        runner.withPropertyValues("krandom.seed=42").run(context -> {
            assertTrue(setting.test(context.getBean(GeneratorConfig.class)));
            assertTrue(output.getAll().contains(recipeSetting + "=true"), output.getAll());
        });
    }

    @SuppressWarnings("unchecked")
    private static Predicate<GeneratorConfig> predicate(Arguments arguments) {
        return (Predicate<GeneratorConfig>) arguments.get()[2];
    }
}
