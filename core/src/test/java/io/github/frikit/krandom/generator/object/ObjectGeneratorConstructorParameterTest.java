/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Classes built through their single declared constructor resolve each parameter like the field
 * it initializes. The test sources are compiled with {@code -parameters}; the positional fallback
 * is exercised with fixtures compiled at test time.
 */
@DisplayName("ObjectGenerator — constructor parameters")
class ObjectGeneratorConstructorParameterTest {

    private static final String EMAIL_PATTERN = "[^@\\s]+@example\\.(com|net|org)";

    /** Compiled without {@code -parameters}: its constructor parameters have no runtime names. */
    private static final String NAMELESS_SOURCE = """
        public final class NamelessContact {
            public final String firstName;
            public final String email;

            public NamelessContact(String firstName, String email) {
                this.firstName = firstName;
                this.email = email;
            }
        }
        """;

    /** Same class name, compiled with {@code -parameters}, parameters named like the positional keys. */
    private static final String POSITIONALLY_NAMED_SOURCE = """
        public final class NamelessContact {
            public final String constructorArg0;
            public final String constructorArg1;

            public NamelessContact(String constructorArg0, String constructorArg1) {
                this.constructorArg0 = constructorArg0;
                this.constructorArg1 = constructorArg1;
            }
        }
        """;

    static final class Contact {

        private final String firstName;
        private final String email;

        Contact(String firstName, String email) {
            this.firstName = firstName;
            this.email = email;
        }
    }

    static final class Money {

        private final String currency;

        @Exclude
        private final long amount;

        private Money(String currency, long amount) {
            this.currency = currency;
            this.amount = amount;
        }
    }

    static final class Badge {

        private final String label;

        Badge(String title) {
            this.label = title;
        }
    }

    static final class Threshold {

        private final int level;

        Threshold(int level) {
            this.level = level;
        }
    }

    record Level(int level) {}

    private static GeneratorConfig.Builder seeded() {
        return GeneratorConfig.builder().seed(42L);
    }

    @Test
    @DisplayName("parameter names drive semantic resolution")
    void parameterNamesDriveSemanticResolution() {
        Contact contact = new ObjectGenerator<>(Contact.class, seeded().build()).generate();

        assertNotNull(contact.firstName);
        assertFalse(contact.firstName.isBlank());
        assertTrue(contact.email.matches(EMAIL_PATTERN), contact.email);
    }

    @Test
    @DisplayName("seeded constructor arguments are reproducible")
    void seededConstructorArgumentsAreReproducible() {
        Contact first = new ObjectGenerator<>(Contact.class, seeded().build()).generate();
        Contact second = new ObjectGenerator<>(Contact.class, seeded().build()).generate();

        assertEquals(first.firstName, second.firstName);
        assertEquals(first.email, second.email);
    }

    @Test
    @DisplayName("by-name overrides apply to constructor parameters")
    void byNameOverridesApplyToConstructorParameters() {
        GeneratorConfig config = seeded().objectOverride(Contact.class, "email", () -> "ada@example.com")
                .build();

        Contact contact = new ObjectGenerator<>(Contact.class, config).generate();

        assertEquals("ada@example.com", contact.email);
    }

    @Test
    @DisplayName("by-name exclusions apply to constructor parameters")
    void byNameExclusionsApplyToConstructorParameters() {
        GeneratorConfig config = seeded().objectExcludeField("email").build();

        Contact contact = new ObjectGenerator<>(Contact.class, config).generate();

        assertNull(contact.email);
        assertNotNull(contact.firstName);
    }

    @Test
    @DisplayName("a name exclusion applies to a parameter that no field backs, and survives toBuilder")
    void nameExclusionAppliesToParameterWithoutBackingField() {
        GeneratorConfig config =
                seeded().objectExcludeField("title").build().toBuilder().build();
        assertTrue(config.isObjectFieldNameExcluded("title"));
        assertFalse(config.isObjectFieldNameExcluded("label"));

        Badge badge = new ObjectGenerator<>(Badge.class, config).generate();

        assertNull(badge.label);
    }

    @Test
    @DisplayName("@Exclude on the backing field excludes the constructor parameter and keeps the primitive default")
    void excludeAnnotationOnBackingFieldAppliesToConstructorParameter() {
        Money money = new ObjectGenerator<>(Money.class, seeded().build()).generate();

        assertNotNull(money.currency);
        assertEquals(0L, money.amount);
    }

    @Test
    @DisplayName("without compiled parameter names arguments are addressed by position")
    void withoutParameterNamesArgumentsAreAddressedByPosition(@TempDir Path directory) throws Exception {
        Class<?> nameless = compileFixture(directory, NAMELESS_SOURCE);
        Constructor<?> constructor = nameless.getDeclaredConstructors()[0];
        assertFalse(constructor.getParameters()[0].isNamePresent());

        GeneratorConfig config = seeded().objectOverride(nameless, "constructorArg1", () -> "ada@example.com")
                .objectExcludeField("firstName")
                .build();

        Object contact = new ObjectGenerator<>(nameless, config).generate();

        assertEquals("ada@example.com", nameless.getField("email").get(contact));
        // Exclusions are name-based, so they cannot apply to a nameless parameter.
        assertNotNull(nameless.getField("firstName").get(contact));
    }

    @Test
    @DisplayName("positional parameters use constructorArg<index> as their independent stream identity")
    void positionalParametersUseIndexedStreamIdentity(@TempDir Path namelessDir, @TempDir Path namedDir)
            throws Exception {
        Class<?> nameless = compileFixture(namelessDir, NAMELESS_SOURCE);
        Class<?> positionallyNamed = compileFixture(namedDir, POSITIONALLY_NAMED_SOURCE, "-parameters");
        assertEquals(nameless.getName(), positionallyNamed.getName());
        GeneratorConfig config = seeded().objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                .build();

        Object fromNameless = new ObjectGenerator<>(nameless, config).generate();
        Object fromNamed = new ObjectGenerator<>(positionallyNamed, config).generate();

        assertEquals(
                positionallyNamed.getField("constructorArg0").get(fromNamed),
                nameless.getField("firstName").get(fromNameless));
        assertEquals(
                positionallyNamed.getField("constructorArg1").get(fromNamed),
                nameless.getField("email").get(fromNameless));
    }

    @Test
    @DisplayName("a record argument the canonical constructor rejects is a contextual construction failure")
    void recordArgumentMismatchIsContextual() {
        GeneratorConfig config =
                seeded().objectOverride(Level.class, "level", () -> "high").build();

        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(Level.class, config).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.CONSTRUCTION, context.category());
        assertEquals(GenerationOperation.CONSTRUCT, context.operation());
        assertEquals("Level", context.path());
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("a constructor argument the class constructor rejects is a contextual construction failure")
    void classArgumentMismatchIsContextual() {
        GeneratorConfig config =
                seeded().objectOverride(Threshold.class, "level", () -> "high").build();

        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(Threshold.class, config).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.CONSTRUCTION, context.category());
        assertEquals(GenerationOperation.CONSTRUCT, context.operation());
        assertEquals("Threshold", context.path());
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    /**
     * Compiles {@code source}, a public class named {@code NamelessContact}, into {@code directory}
     * with the given javac options and loads it through its own class loader.
     */
    private static Class<?> compileFixture(Path directory, String source, String... options) throws Exception {
        Path file = directory.resolve("NamelessContact.java");
        Files.writeString(file, source);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "tests must run on a JDK with javac");
        List<String> arguments = new ArrayList<>(List.of(options));
        arguments.addAll(List.of("-d", directory.toString(), file.toString()));
        int status = compiler.run(null, null, null, arguments.toArray(String[]::new));
        assertEquals(0, status, "fixture compilation failed");
        try (URLClassLoader loader = new URLClassLoader(
                new URL[] {directory.toUri().toURL()},
                ObjectGeneratorConstructorParameterTest.class.getClassLoader())) {
            return loader.loadClass("NamelessContact");
        }
    }
}
