/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * A sealed interface is generated as one of its permitted subclasses, with no {@code objectSubtype}
 * mapping or type override.
 */
class SealedTypeExampleTest {

    public sealed interface PaymentMethod permits Card, BankTransfer {}

    public record Card(String number) implements PaymentMethod {}

    public record BankTransfer(String iban) implements PaymentMethod {}

    public record Checkout(PaymentMethod payment) {}

    @Test
    void sealedInterfaceIsGeneratedAsAPermittedSubclass() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();

        List<Checkout> checkouts = new ObjectGenerator<>(Checkout.class, config).generateList(20);

        Set<Class<?>> subclasses = checkouts.stream()
                .map(checkout -> checkout.payment().getClass())
                .collect(Collectors.toSet());
        assertEquals(Set.of(Card.class, BankTransfer.class), subclasses);
        // A sealed root works the same way.
        assertNotNull(new ObjectGenerator<>(PaymentMethod.class, config).generate());
    }
}
