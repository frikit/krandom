/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Object subtype mapping")
class ObjectSubtypeTest {

    interface Payment {}

    abstract static class Vehicle {

        String label;
    }

    static class CardPayment implements Payment {

        String cardNumber;
    }

    static class Bike extends Vehicle {

        int gears;
    }

    static class Order {

        Payment payment;
        Vehicle vehicle;
        String  id;
    }

    @Test
    @DisplayName("objectSubtype populates interface and abstract fields with mapped implementations")
    void subtypeMappingPopulatesAbstractFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .objectSubtype(Payment.class, CardPayment.class)
                                                .objectSubtype(Vehicle.class, Bike.class)
                                                .build();

        Order order = Generators.ofObject(Order.class, config).generate();

        CardPayment payment = assertInstanceOf(CardPayment.class, order.payment);
        assertNotNull(payment.cardNumber, "mapped implementation should be fully populated");
        Bike vehicle = assertInstanceOf(Bike.class, order.vehicle);
        assertNotNull(vehicle.label, "inherited fields of the implementation should be populated");
        assertNotNull(order.id);
    }

    @Test
    @DisplayName("unmapped interface and abstract fields remain null")
    void unmappedAbstractFieldsRemainNull() {
        Order order = Generators.ofObject(Order.class, GeneratorConfig.builder().seed(1L).build()).generate();

        assertNull(order.payment);
        assertNull(order.vehicle);
    }

    @Test
    @DisplayName("ObjectGeneratorConfig.subtype resolves mapped and unmapped types")
    void objectGeneratorConfigSubtype() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .subtype(Payment.class, CardPayment.class)
                                                            .build();

        assertSame(CardPayment.class, config.resolveSubtype(Payment.class));
        assertSame(Order.class, config.resolveSubtype(Order.class));

        Order order = new ObjectGenerator<>(Order.class, config).generate();
        assertInstanceOf(CardPayment.class, order.payment);
    }

    @Test
    @DisplayName("generatorConfig(...) inherits subtype mappings; later subtype(...) calls merge")
    void inheritedSubtypesMergeWithLaterExplicitOnes() {
        GeneratorConfig gc = GeneratorConfig.builder()
                                            .objectSubtype(Payment.class, CardPayment.class)
                                            .build();
        ObjectGeneratorConfig ogc = ObjectGeneratorConfig.builder()
                                                         .generatorConfig(gc)
                                                         .subtype(Vehicle.class, Bike.class)
                                                         .build();

        assertSame(CardPayment.class, ogc.resolveSubtype(Payment.class));
        assertSame(Bike.class, ogc.resolveSubtype(Vehicle.class));
    }

    @Test
    @DisplayName("explicit subtype(...) before generatorConfig(...) suppresses inheritance")
    void explicitSubtypesSuppressInheritance() {
        GeneratorConfig gc = GeneratorConfig.builder()
                                            .objectSubtype(Payment.class, CardPayment.class)
                                            .build();
        ObjectGeneratorConfig ogc = ObjectGeneratorConfig.builder()
                                                         .subtype(Vehicle.class, Bike.class)
                                                         .generatorConfig(gc)
                                                         .build();

        assertSame(Bike.class, ogc.resolveSubtype(Vehicle.class));
        assertSame(Payment.class, ogc.resolveSubtype(Payment.class));
    }

    @Test
    @DisplayName("subtype mappings survive toGeneratorConfig and toBuilder round-trips")
    void subtypesSurviveRoundTrips() {
        ObjectGeneratorConfig ogc = ObjectGeneratorConfig.builder()
                                                         .subtype(Payment.class, CardPayment.class)
                                                         .build();
        assertEquals(CardPayment.class, ogc.toGeneratorConfig().getObjectSubtypes().get(Payment.class));

        GeneratorConfig gc = GeneratorConfig.builder()
                                            .objectSubtype(Payment.class, CardPayment.class)
                                            .build();
        assertEquals(CardPayment.class, gc.toBuilder().build().getObjectSubtypes().get(Payment.class));
    }

    @Test
    @DisplayName("non-assignable implementations are rejected")
    void rejectsNonAssignableImplementation() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectSubtype(Payment.class, Bike.class));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().subtype(Payment.class, Bike.class));
    }

    @Test
    @DisplayName("abstract and interface implementations are rejected")
    void rejectsNonConcreteImplementation() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectSubtype(Vehicle.class, Vehicle.class));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectSubtype(Payment.class, Payment.class));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().subtype(Vehicle.class, Vehicle.class));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().subtype(Payment.class, Payment.class));
    }

    @Test
    @DisplayName("null arguments are rejected")
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectSubtype(null, CardPayment.class));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectSubtype(Payment.class, null));
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().subtype(null, CardPayment.class));
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().subtype(Payment.class, null));
    }
}
