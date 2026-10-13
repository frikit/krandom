/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The Jakarta Validation API is a compile-only dependency of core. These tests reload the whole
 * {@code io.github.frikit.krandom} package tree through a class loader that refuses every
 * {@code jakarta.*} class, to prove that constraint lookups degrade to "no constraints", that
 * objects still generate, and that no method signature of {@link BeanValidationSupport} mentions a
 * Jakarta type (reflection over the class must keep working).
 */
@DisplayName("Bean Validation availability")
class BeanValidationAvailabilityTest {

    static final class Constrained {

        @NotBlank @Size(min = 4, max = 4) String code;
    }

    static final class CaseInsensitiveEmail {

        @Email(regexp = ".*@EXAMPLE\\.(COM|NET|ORG)", flags = Pattern.Flag.CASE_INSENSITIVE)
        String contact;
    }

    record Customer(@Email String email, @Min(1) int visits) {}

    static final class Order {

        @NotBlank String id;

        @Size(min = 1, max = 3) List<String> tags;

        Customer customer;
    }

    @Test
    @DisplayName("the API is detected on the test class path")
    void apiIsDetectedOnTheTestClassPath() {
        assertTrue(BeanValidationSupport.isAvailable());
    }

    @Test
    @DisplayName("email pattern flags are applied")
    void emailPatternFlagsAreApplied() {
        CaseInsensitiveEmail value = new ObjectGenerator<>(CaseInsensitiveEmail.class).generate();

        assertTrue(value.contact.toLowerCase(Locale.ROOT).matches(".*@example\\.(com|net|org)"), value.contact);
    }

    @Test
    @DisplayName("without the API every constraint lookup reports no constraints")
    void withoutTheApiConstraintsAreIgnored() throws Exception {
        Class<?> support = new HidingClassLoader().loadClass(BeanValidationSupport.class.getName());
        assertNotSame(BeanValidationSupport.class, support);
        assertFalse((Boolean) invoke(support, "isAvailable"));

        Field annotated = Constrained.class.getDeclaredField("code");
        Object model = invoke(support, "constraintModelFor", annotated, String.class);
        assertFalse((Boolean) invokeOn(model, "constrained"));
        assertNull(invoke(support, "sizeRangeFor", annotated));
    }

    @Test
    @DisplayName("without the API annotated POJOs, records and collection members still generate")
    void withoutTheApiObjectsStillGenerate() throws Exception {
        ClassLoader hiding = new HidingClassLoader();
        Class<?> generatorType = hiding.loadClass(ObjectGenerator.class.getName());
        Class<?> orderType = hiding.loadClass(Order.class.getName());
        assertNotSame(Order.class, orderType);

        Object generator = generatorType.getConstructor(Class.class).newInstance(orderType);
        // ServiceLoader resolves construction adapters through the context loader; keep every
        // type on the hiding side so the reloaded engine only sees reloaded providers.
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(hiding);
        Object order;
        try {
            order = generatorType.getMethod("generate").invoke(generator);
        } finally {
            thread.setContextClassLoader(previous);
        }

        assertSame(orderType, order.getClass());
        assertNotNull(fieldValue(order, "id"));
        List<?> tags = (List<?>) fieldValue(order, "tags");
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        Object customer = fieldValue(order, "customer");
        assertNotNull(customer);
        assertSame(hiding.loadClass(Customer.class.getName()), customer.getClass());
        Method email = customer.getClass().getDeclaredMethod("email");
        email.setAccessible(true);
        assertNotNull(email.invoke(customer));
    }

    private static Object fieldValue(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object invoke(Class<?> type, String name, Object... arguments) throws Exception {
        Class<?>[] parameterTypes =
                switch (name) {
                    case "constraintModelFor" -> new Class<?>[] {AnnotatedElement.class, Class.class};
                    case "sizeRangeFor" -> new Class<?>[] {AnnotatedElement.class};
                    default -> new Class<?>[0];
                };
        // getDeclaredMethod resolves every declared signature, so this also fails if any method of
        // the class mentions a Jakarta type.
        Method method = type.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static Object invokeOn(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }

    /**
     * Defines every {@code io.github.frikit.krandom} class afresh from the parent's class bytes
     * (child first) while throwing {@link ClassNotFoundException} for every {@code jakarta.*}
     * class. The classes keep the parent's protection domain so coverage instrumentation still
     * applies; resources such as locale data are still served by the parent.
     */
    static final class HidingClassLoader extends ClassLoader {

        private static final String RELOADED_PREFIX = "io.github.frikit.krandom.";

        HidingClassLoader() {
            super(BeanValidationAvailabilityTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("jakarta.")) {
                throw new ClassNotFoundException(name);
            }
            if (!name.startsWith(RELOADED_PREFIX)) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    loaded = defineFromParent(name);
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        }

        private Class<?> defineFromParent(String name) throws ClassNotFoundException {
            String resource = name.replace('.', '/') + ".class";
            try (InputStream in = getParent().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new ClassNotFoundException(name);
                }
                byte[] bytes = in.readAllBytes();
                return defineClass(name, bytes, 0, bytes.length, BeanValidationSupport.class.getProtectionDomain());
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }
}
