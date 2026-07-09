/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.Size;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldGeneratorResolver edge coverage")
class FieldGeneratorResolverCoverageTest {

    @SuppressWarnings("unchecked")
    private static List<Object> invokeToListType(Class<?> rawType, List<Object> values) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toListType", Class.class, List.class);
        method.setAccessible(true);
        return (List<Object>) method.invoke(null, rawType, values);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeStatic(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(null, args);
    }

    private static Object invokeInstance(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static FieldGeneratorResolver resolver(ObjectGeneratorConfig config) {
        return new FieldGeneratorResolver(
            config, new ObjectPool(config.getObjectPoolSize()), new UniqueFieldTracker(), 0L, Map.of());
    }

    @Test
    @DisplayName("toListType handles interface, abstract, concrete subtype, and CopyOnWriteArrayList")
    void toListTypeFallbackBranches() throws Exception {
        List<Object> values = new ArrayList<>(List.of("a", "b", "c"));

        List<Object> interfaceFallback = invokeToListType(CustomList.class, values);
        assertEquals(ArrayList.class, interfaceFallback.getClass());
        assertEquals(values, interfaceFallback);

        List<Object> abstractFallback = invokeToListType(AbstractCustomList.class, values);
        assertEquals(ArrayList.class, abstractFallback.getClass());
        assertEquals(values, abstractFallback);

        List<Object> concreteSubtype = invokeToListType(ConcreteCustomList.class, values);
        assertEquals(ConcreteCustomList.class, concreteSubtype.getClass());
        assertEquals(values, concreteSubtype);

        List<Object> copyOnWrite = invokeToListType(CopyOnWriteArrayList.class, values);
        assertEquals(CopyOnWriteArrayList.class, copyOnWrite.getClass());
        assertEquals(values, copyOnWrite);
    }

    @Test
    @DisplayName("temporal builder helpers cover default-min custom-max branches")
    void temporalBuilderHelpersCoverDefaultMinCustomMaxBranches() throws Exception {
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).build();
        Random seedSource = new Random(9L);
        LocalDate min = LocalDate.of(1970, 1, 1);
        LocalDate max = LocalDate.of(2099, 12, 31);

        Generator<LocalDate> dateGenerator =
            invokeStatic("buildDateGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<LocalDateTime> localDateTimeGenerator =
            invokeStatic("buildLocalDateTimeGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<Instant> instantGenerator =
            invokeStatic("buildInstantGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<ZonedDateTime> zonedDateTimeGenerator =
            invokeStatic("buildZonedDateTimeGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<java.util.Date> utilDateGenerator =
            invokeStatic("buildUtilDateGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<java.sql.Date> sqlDateGenerator =
            invokeStatic("buildSqlDateGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);
        Generator<java.sql.Timestamp> sqlTimestampGenerator =
            invokeStatic("buildSqlTimestampGenerator",
                         new Class<?>[]{ GeneratorConfig.class, Random.class, LocalDate.class, LocalDate.class },
                         config, seedSource, min, max);

        assertFalse(dateGenerator.generate().isAfter(max));
        assertFalse(localDateTimeGenerator.generate().toLocalDate().isAfter(max));
        assertFalse(instantGenerator.generate().atZone(ZoneOffset.UTC).toLocalDate().isAfter(max));
        assertFalse(zonedDateTimeGenerator.generate().toLocalDate().isAfter(max));
        assertFalse(utilDateGenerator.generate().toInstant().atZone(ZoneOffset.UTC).toLocalDate().isAfter(max));
        assertFalse(sqlDateGenerator.generate().toLocalDate().isAfter(max));
        assertFalse(sqlTimestampGenerator.generate().toInstant().atZone(ZoneOffset.UTC).toLocalDate().isAfter(max));
    }

    @Test
    @DisplayName("semantic status helper returns null for non-enum types")
    void semanticStatusHelperReturnsNullForNonEnumTypes() throws Exception {
        FieldGeneratorResolver resolver = resolver(ObjectGeneratorConfig.builder().build());

        assertNull(invokeInstance(resolver,
                                  "semanticStatusEnumGenerator",
                                  new Class<?>[]{ Class.class },
                                  String.class));
    }

    @Test
    @DisplayName("uniqueness and nullability helpers cover alias, optional, and probability branches")
    void uniquenessAndNullabilityHelpersCoverAliasOptionalAndProbabilityBranches() throws Exception {
        ObjectGeneratorConfig lowProbabilityConfig = ObjectGeneratorConfig.builder()
                                                                         .uniqueFields("accountstatus")
                                                                         .nullProbability(0.1)
                                                                         .optionalEmptyProbability(0.1)
                                                                         .build();
        FieldGeneratorResolver lowProbabilityResolver = resolver(lowProbabilityConfig);
        Field valueField = NullabilityTarget.class.getDeclaredField("value");
        Field optionalField = NullabilityTarget.class.getDeclaredField("optional");

        assertTrue((boolean) invokeInstance(lowProbabilityResolver,
                                            "isUniqueField",
                                            new Class<?>[]{ String.class, String.class },
                                            "status",
                                            "status"));
        assertFalse((boolean) invokeInstance(lowProbabilityResolver,
                                             "shouldReturnNull",
                                             new Class<?>[]{ java.lang.reflect.AnnotatedElement.class, Class.class, Generator.class, Generator.class },
                                             null,
                                             String.class,
                                             null,
                                             null));
        assertFalse((boolean) invokeInstance(lowProbabilityResolver,
                                             "shouldReturnNull",
                                             new Class<?>[]{ java.lang.reflect.AnnotatedElement.class, Class.class, Generator.class, Generator.class },
                                             valueField,
                                             Optional.class,
                                             null,
                                             null));
        assertFalse((boolean) invokeInstance(lowProbabilityResolver,
                                             "shouldReturnNull",
                                             new Class<?>[]{ java.lang.reflect.AnnotatedElement.class, Class.class, Generator.class, Generator.class },
                                             valueField,
                                             String.class,
                                             null,
                                             null));
        assertFalse((boolean) invokeInstance(lowProbabilityResolver,
                                             "shouldReturnEmptyOptional",
                                             new Class<?>[]{ java.lang.reflect.AnnotatedElement.class },
                                             (Object) null));
        assertFalse((boolean) invokeInstance(lowProbabilityResolver,
                                             "shouldReturnEmptyOptional",
                                             new Class<?>[]{ java.lang.reflect.AnnotatedElement.class },
                                             optionalField));

        ObjectGeneratorConfig highProbabilityConfig = ObjectGeneratorConfig.builder()
                                                                          .nullProbability(1.0)
                                                                          .optionalEmptyProbability(1.0)
                                                                          .build();
        FieldGeneratorResolver highProbabilityResolver = resolver(highProbabilityConfig);

        assertTrue((boolean) invokeInstance(highProbabilityResolver,
                                            "shouldReturnNull",
                                            new Class<?>[]{ java.lang.reflect.AnnotatedElement.class, Class.class, Generator.class, Generator.class },
                                            valueField,
                                            String.class,
                                            null,
                                            null));
        assertTrue((boolean) invokeInstance(highProbabilityResolver,
                                            "shouldReturnEmptyOptional",
                                            new Class<?>[]{ java.lang.reflect.AnnotatedElement.class },
                                            optionalField));
    }

    @Test
    @DisplayName("semantic resolver covers typed coordinate wrappers, missing typed lookups, strict validated semantics, and float rounding")
    void semanticResolverCoversTypedCoordinateWrappersMissingTypedLookupsStrictValidatedSemanticsAndFloatRounding() throws Exception {
        FieldGeneratorResolver relaxedResolver = resolver(ObjectGeneratorConfig.builder()
                                                                               .generatorConfig(GeneratorConfig.builder().seed(7L).build())
                                                                               .build());

        @SuppressWarnings("unchecked")
        Generator<Float> primitiveLatitudeGenerator = (Generator<Float>) invokeInstance(relaxedResolver,
                                                                                        "semanticGeneratorFor",
                                                                                        new Class<?>[]{ Class.class, String.class },
                                                                                        float.class,
                                                                                        "latitude");
        @SuppressWarnings("unchecked")
        Generator<BigDecimal> latitudeGenerator = (Generator<BigDecimal>) invokeInstance(relaxedResolver,
                                                                                         "semanticGeneratorFor",
                                                                                         new Class<?>[]{ Class.class, String.class },
                                                                                         BigDecimal.class,
                                                                                         "latitude");
        @SuppressWarnings("unchecked")
        Generator<Float> longitudeGenerator = (Generator<Float>) invokeInstance(relaxedResolver,
                                                                                "semanticGeneratorFor",
                                                                                new Class<?>[]{ Class.class, String.class },
                                                                                Float.class,
                                                                                "longitude");

        assertNotNull(primitiveLatitudeGenerator);
        float primitiveLatitude = primitiveLatitudeGenerator.generate();
        assertTrue(primitiveLatitude >= 24.5f && primitiveLatitude <= 49.0f);

        assertNotNull(latitudeGenerator);
        BigDecimal latitude = latitudeGenerator.generate();
        assertTrue(latitude.compareTo(BigDecimal.valueOf(24.5)) >= 0);
        assertTrue(latitude.compareTo(BigDecimal.valueOf(49.0)) <= 0);
        assertEquals(6, latitude.scale());

        assertNotNull(longitudeGenerator);
        float longitude = longitudeGenerator.generate();
        assertTrue(longitude >= -125.0f && longitude <= -66.0f);

        assertNull(invokeInstance(relaxedResolver,
                                  "semanticGeneratorFor",
                                  new Class<?>[]{ Class.class, String.class },
                                  Boolean.class,
                                  "latitude"));

        Generator<Float> roundedFloatGenerator =
            invokeStatic("floatGenerator",
                         new Class<?>[]{ Long.class, Random.class, float.class, float.class, Integer.class },
                         null,
                         new Random(1L),
                         1.25f,
                         1.75f,
                         2);
        float roundedValue = roundedFloatGenerator.generate();
        assertTrue(roundedValue >= 1.25f && roundedValue <= 1.75f);

        FieldGeneratorResolver strictResolver = resolver(ObjectGeneratorConfig.builder()
                                                                              .generatorConfig(GeneratorConfig.builder().seed(11L).build())
                                                                              .semanticMode(ObjectGenerationSemanticMode.STRICT)
                                                                              .build());
        Field usernameField = StrictValidatedSemanticString.class.getDeclaredField("username");
        String username = (String) invokeInstance(strictResolver,
                                                  "resolveAndGenerate",
                                                  new Class<?>[]{
                                                      java.lang.reflect.Type.class,
                                                      Class.class,
                                                      String.class,
                                                      Class.class,
                                                      int.class,
                                                      java.lang.reflect.AnnotatedElement.class
                                                  },
                                                  String.class,
                                                  String.class,
                                                  "username",
                                                  StrictValidatedSemanticString.class,
                                                  0,
                                                  usernameField);

        assertNotNull(username);
        assertNotEquals(40, username.length());
    }


    interface CustomList<E> extends List<E> {

    }


    abstract static class AbstractCustomList<E> extends java.util.AbstractList<E> {

    }


    static final class ConcreteCustomList<E> extends ArrayList<E> {

    }

    static final class NullabilityTarget {

        String           value;
        Optional<String> optional;
        BigDecimal       status;
    }

    static final class StrictValidatedSemanticString {

        @Size(min = 40, max = 40)
        String username;
    }
}
