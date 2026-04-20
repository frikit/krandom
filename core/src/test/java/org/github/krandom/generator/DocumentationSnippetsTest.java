/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.base.CharGenerator;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.base.StringGenerator;
import org.github.krandom.generator.finance.CreditCardGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.object.ObjectGenerator;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Documentation snippets")
class DocumentationSnippetsTest {

    @Test
    @DisplayName("primitives and determinism snippets stay runnable")
    void primitivesAndDeterminismSnippetsStayRunnable() {
        int n = Generators.ofInt(10, 99).generate();
        double p = Generators.ofDouble(0.0, 1.0).generate();
        String token = Generators.ofString(
            StringGenerator.builder()
                           .length(16)
                           .charGenerator(CharGenerator.alphanumeric())
        ).generate();

        IntGenerator a = Generators.ofInt(1, 100, 123L);
        IntGenerator b = Generators.ofInt(1, 100, 123L);

        List<String> colors = List.of("red", "green", "blue");
        String one = Generators.pickFrom(colors).generate();
        List<String> two = Generators.pickSetFrom(colors, 2).generate();
        List<String> shuffled = Generators.shuffleOf(colors).generate();

        assertTrue(n >= 10 && n < 99);
        assertTrue(p >= 0.0 && p < 1.0);
        assertEquals(16, token.length());
        assertEquals(a.generate(), b.generate());
        assertEquals(a.generate(), b.generate());
        assertTrue(colors.contains(one));
        assertEquals(2, two.size());
        assertEquals(3, shuffled.size());
        assertEquals(Set.copyOf(colors), Set.copyOf(shuffled));
    }

    @Test
    @DisplayName("runnable snippets guide examples stay runnable")
    void runnableSnippetsGuideExamplesStayRunnable() {
        GeneratorConfig objectConfig = GeneratorConfig.builder()
                                                      .objectMaxDepth(3)
                                                      .build();

        ObjectGenerator<UserRecord> objectGenerator = Generators.ofObject(UserRecord.class, objectConfig);
        UserRecord user = objectGenerator.generate();

        GeneratorConfig userDataConfig = GeneratorConfig.builder()
                                                        .seed(42L)
                                                        .build();

        String name = new FullNameGenerator(userDataConfig).generate();
        String email = new EmailGenerator(userDataConfig).generate();

        GeneratorConfig locationConfig = GeneratorConfig.builder()
                                                        .locale(Locale.of("de", "DE"))
                                                        .seed(42L)
                                                        .build();

        String city = new CityGenerator(locationConfig).generate();
        String country = new CountryGenerator(locationConfig).generate();

        String amount = new MoneyGenerator().generate();
        String card = new CreditCardGenerator().generate();

        assertNotNull(user);
        assertNotNull(user.name());
        assertNotNull(user.address());
        assertNotNull(user.address().city());
        assertNotNull(user.address().country());
        assertNotNull(name);
        assertTrue(email.contains("@"));
        assertNotNull(city);
        assertNotNull(country);
        assertTrue(!amount.isBlank());
        assertTrue(!card.isBlank());
    }

    @Test
    @DisplayName("examples page snippets stay runnable")
    void examplesPageSnippetsStayRunnable() {
        GeneratorConfig cfg = GeneratorConfig.builder()
                                             .locale(Locale.US)
                                             .seed(20260303L)
                                             .build();

        FullNameGenerator names = new FullNameGenerator(cfg);
        EmailGenerator emails = new EmailGenerator(cfg);
        PhoneNumberGenerator phones = new PhoneNumberGenerator(cfg);
        StreetAddressGenerator addresses = new StreetAddressGenerator(cfg);

        Map<String, Object> user = Map.of(
            "id", Generators.ofUuid().generate().toString(),
            "name", names.generate(),
            "email", emails.generate(),
            "phone", phones.generate(),
            "address", addresses.generate()
        );

        Field f = Generators.ofField(cfg);
        Schema orders = Generators.ofSchema(cfg, new LinkedHashMap<>(Map.of(
            "orderId", f.bind("code.uuid"),
            "customer", f.bind("person.full_name"),
            "email", f.bind("person.email"),
            "currency", f.bind("finance.currency"),
            "amount", f.bind("finance.money"),
            "shipTo", f.bind("address.street_address")
        )));

        List<Map<String, Object>> batch = orders.generateBatch(5);
        Generator<Integer> stableIds = Generators.ofInt(1000, 9999, 77L);
        UniqueGenerator<String> uniqueEmails = Generators.unique(Generators.ofEmail());

        assertTrue(user.get("id").toString().contains("-"));
        assertTrue(user.get("email").toString().contains("@"));
        assertEquals(5, batch.size());
        assertEquals(6, batch.getFirst().size());

        int id = stableIds.generate();
        String firstEmail = uniqueEmails.generate();
        String secondEmail = uniqueEmails.generate();

        assertTrue(id >= 1000 && id < 9999);
        assertTrue(firstEmail.contains("@"));
        assertTrue(secondEmail.contains("@"));
        assertTrue(!firstEmail.equals(secondEmail));
    }

    private record AddressRecord(String city, String country) {
    }

    private record UserRecord(String name, AddressRecord address) {
    }
}
