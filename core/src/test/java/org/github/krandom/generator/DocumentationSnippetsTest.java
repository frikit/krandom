/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.base.CharGenerator;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.base.StringGenerator;
import org.github.krandom.generator.commerce.OrderInfo;
import org.github.krandom.generator.commerce.ProductInfo;
import org.github.krandom.generator.commerce.ShipmentInfo;
import org.github.krandom.generator.finance.BankInfo;
import org.github.krandom.generator.finance.CreditCardGenerator;
import org.github.krandom.generator.finance.CreditCardInfo;
import org.github.krandom.generator.finance.InvoiceInfo;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.finance.PaymentInfo;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.locale.LocaleDataBundle;
import org.github.krandom.generator.object.ObjectGenerator;
import org.github.krandom.generator.object.ObjectGenerationSemanticMode;
import org.github.krandom.generator.provider.ProviderHub;
import org.github.krandom.generator.provider.TextFormatProvider;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.JobInfo;
import org.github.krandom.generator.user.PersonInfo;
import org.github.krandom.generator.user.CompanyInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
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
    @DisplayName("getting started guide snippets stay runnable")
    void gettingStartedGuideSnippetsStayRunnable() {
        int roll = Generators.ofInt(1, 7).generate();
        String name = Generators.ofFullName().generate();
        String ipv4 = Generators.ofIPv4().generate();

        GeneratorConfig cfg = GeneratorConfig.builder()
                                             .locale(Locale.US)
                                             .seed(42L)
                                             .build();

        OrderDto order = Generators.ofObject(OrderDto.class, cfg).generate();
        UserFixture user = Generators.ofObjectFaker(UserFixture.class, cfg)
                                     .ruleFor("email", () -> "owner@example.test")
                                     .ruleFor("address.city", () -> "Berlin")
                                     .generate();

        Field field = Generators.ofField(cfg);
        LinkedHashMap<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("orderId", field.bind("code.uuid"));
        fields.put("email", field.bind("person.email"));
        fields.put("amount", field.bind("finance.money"));
        Schema orders = Generators.ofSchema(cfg, fields);

        EmailGenerator a = new EmailGenerator(cfg);
        EmailGenerator b = new EmailGenerator(cfg);

        String jsonl = orders.toJsonLines(10);
        String csv = orders.toCsv(10);

        assertTrue(roll >= 1 && roll < 7);
        assertNotNull(name);
        assertNotNull(ipv4);
        assertNotNull(order.email());
        assertNotNull(order.amount());
        assertEquals("owner@example.test", user.email);
        assertEquals("Berlin", user.address.city);
        assertEquals(a.generate(), b.generate());
        assertEquals(10, jsonl.lines().count());
        assertTrue(csv.startsWith("orderId,email,amount"));
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
        PersonInfo person = Generators.ofPersonInfo(cfg).generate();
        CompanyInfo company = Generators.ofCompanyInfo(cfg).generate();
        JobInfo job = Generators.ofJobInfo(cfg).generate();
        ProductInfo product = Generators.ofProductInfo(cfg).generate();
        OrderInfo order = Generators.ofOrderInfo(cfg).generate();
        ShipmentInfo shipment = Generators.ofShipmentInfo(cfg).generate();
        BankInfo bank = Generators.ofBankInfo(cfg).generate();
        CreditCardInfo cardInfo = Generators.ofCreditCardInfo(cfg).generate();
        InvoiceInfo invoice = Generators.ofInvoiceInfo(cfg).generate();
        PaymentInfo payment = Generators.ofPaymentInfo(cfg).generate();

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
        assertNotNull(person.contact());
        assertNotNull(person.address());
        assertTrue(person.contact().email().contains("@"));
        assertEquals(person.username(), person.contact().email().substring(0, person.contact().email().indexOf('@')));
        assertEquals(company.email().substring(company.email().indexOf('@') + 1),
                     URI.create(company.website()).getHost().substring("www.".length()));
        assertTrue(job.title().contains(job.profession()));
        assertTrue(product.upc().matches("\\d{12}"));
        assertEquals(order.customer().address(), order.shippingAddress());
        assertEquals(0, order.total().compareTo(order.subtotal().add(order.shipping()).add(order.tax()).setScale(2)));
        assertEquals(shipment.recipient().address(), shipment.destination());
        assertTrue(shipment.deliveredOn() == null || !shipment.deliveredOn().isBefore(shipment.shippedOn()));
        assertTrue(bank.routingNumber().matches("\\d{9}"));
        assertTrue(cardInfo.exp().matches("\\d{2}/\\d{2}"));
        assertEquals(invoice.customer().address(), invoice.billingAddress());
        assertEquals(0, invoice.total().compareTo(invoice.subtotal().add(invoice.tax()).setScale(2)));
        assertEquals(payment.payer().address(), payment.billingAddress());
        assertTrue(payment.settledOn() == null || !payment.settledOn().isBefore(payment.authorizedOn()));

        int id = stableIds.generate();
        String firstEmail = uniqueEmails.generate();
        String secondEmail = uniqueEmails.generate();

        assertTrue(id >= 1000 && id < 9999);
        assertTrue(firstEmail.contains("@"));
        assertTrue(secondEmail.contains("@"));
        assertTrue(!firstEmail.equals(secondEmail));
    }

    @Test
    @DisplayName("locale aware custom bundle snippets stay runnable")
    void localeAwareCustomBundleSnippetsStayRunnable() {
        Locale locale = Locale.of("es", "MX");

        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .firstNames(new String[] { "Mateo" }, new String[] { "Sofía" })
                                                  .lastNames("Hernández", "Ramírez")
                                                  .genderLabels("Hombre", "Mujer")
                                                  .cities("Ciudad de México", "Guadalajara")
                                                  .states(new String[] { "Jalisco", "Ciudad de México" },
                                                          new String[] { "JAL", "CDMX" })
                                                  .countries("México")
                                                  .streetAddress(new String[] { "Juárez" },
                                                                 new String[] { "Av" },
                                                                 new String[] { "Avenida" })
                                                  .build();

        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerLocaleData(bundle)
                                                         .build();

        GeneratorConfig mx = GeneratorConfig.builder()
                                            .locale(locale)
                                            .registryContext(context)
                                            .seed(7L)
                                            .build();

        String firstName = new org.github.krandom.generator.user.FirstNameGenerator(mx)
            .generate(org.github.krandom.generator.user.Gender.MALE);
        String city = new CityGenerator(mx).generate();
        String street = new StreetAddressGenerator(mx).generate();

        assertEquals("Mateo", firstName);
        assertTrue(Set.of("Ciudad de México", "Guadalajara").contains(city));
        assertTrue(street.contains("Juárez"));
    }

    @Test
    @DisplayName("schema and provider hub guide snippets stay runnable")
    void schemaAndProviderHubGuideSnippetsStayRunnable() {
        ProviderHub hub = new ProviderHub(GeneratorConfig.builder().locale(Locale.US).seed(42L).build());
        TextFormatProvider format = hub.get("text.format", TextFormatProvider.class);

        String sku = format.template("SKU-??-####");
        String coupon = format.lexify("promo-????");
        String token = format.asciify("***-***");
        String reference = format.regexify("[A-Z]{3}\\d{4}");
        String invoice = format.examplify("INV-2026-AB12");

        assertTrue(sku.matches("SKU-[a-z]{2}-\\d{4}"));
        assertTrue(coupon.matches("promo-[a-z]{4}"));
        assertTrue(token.length() == 7 && token.charAt(3) == '-');
        assertTrue(reference.matches("[A-Z]{3}\\d{4}"));
        assertTrue(invoice.matches("[A-Z]{3}-\\d{4}-[A-Z]{2}\\d{2}"));
    }

    @Test
    @DisplayName("choosing an api guide snippets stay runnable")
    void choosingAnApiGuideSnippetsStayRunnable() {
        int roll = Generators.ofInt(1, 7).generate();
        String email = Generators.ofEmail().generate();
        String city = Generators.ofCity().generate();

        GeneratorConfig cfg = GeneratorConfig.builder()
                                             .locale(Locale.US)
                                             .seed(42L)
                                             .build();

        OrderDto order = Generators.ofObject(OrderDto.class, cfg).generate();

        UserFixture user = Generators.ofObjectFaker(UserFixture.class)
                                     .ruleFor("email", () -> "owner@example.test")
                                     .ruleFor("address.city", () -> "Berlin")
                                     .generate();

        Field field = Generators.ofField();
        LinkedHashMap<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("orderId", field.bind("code.uuid"));
        fields.put("email", field.bind("person.email"));
        fields.put("amount", field.bind("finance.money"));
        Schema orders = Generators.ofSchema(fields);

        String jsonl = orders.toJsonLines(10);
        String csv = orders.toCsv(10);

        assertTrue(roll >= 1 && roll < 7);
        assertTrue(email.contains("@"));
        assertNotNull(city);
        assertNotNull(order.email());
        assertNotNull(order.amount());
        assertEquals("owner@example.test", user.email);
        assertEquals("Berlin", user.address.city);
        assertEquals(10, jsonl.lines().count());
        assertTrue(csv.startsWith("orderId,email,amount"));
    }

    @Test
    @DisplayName("performance and observability guide snippets stay runnable")
    void performanceAndObservabilityGuideSnippetsStayRunnable() {
        GeneratorConfig cfg = GeneratorConfig.builder()
                                             .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                             .seed(42L)
                                             .build();

        OrderDto order = Generators.ofObject(OrderDto.class, cfg).generate();

        assertNotNull(order);
        assertNotNull(order.email());
        assertNotNull(order.amount());
    }

    private record AddressRecord(String city, String country) {
    }

    private record UserRecord(String name, AddressRecord address) {
    }

    private record OrderDto(String email, String amount) {
    }

    static final class UserFixture {

        String email;
        FixtureAddress address;
    }

    static final class FixtureAddress {

        String city;
    }
}
