/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetDateTimeGenerator;
import io.github.frikit.krandom.generator.file.FilePathGenerator;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.krandom.generator.finance.PaymentInfo;
import io.github.frikit.krandom.generator.finance.PaymentInfoGenerator;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.network.UriGenerator;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.AvatarUrlGenerator;
import io.github.frikit.krandom.generator.user.CompanyEmailGenerator;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.MiddleNameGenerator;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.ProfileGenerator;
import io.github.frikit.krandom.generator.user.SimpleProfileGenerator;
import io.github.frikit.krandom.generator.user.SocialProfileGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Composite child-stream policy")
class ChildStreamPolicyTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    /**
     * Fingerprints of the first three values of every composite generator, captured from the
     * legacy implementation before child streams became configurable. The default policy must keep
     * them byte-identical. The {@code profile} and {@code simpleProfile} birthdays used to follow the
     * system clock, so their fingerprints were recaptured once birthdays followed the configured
     * clock.
     */
    private static final Map<String, String> LEGACY_FINGERPRINTS = Map.ofEntries(
        Map.entry("sentence", "f320293937a27329fc0cee1e"),
        Map.entry("word", "4b1b4796b57772f862f682b5"),
        Map.entry("paragraph", "4e0a887771021be3c1164794"),
        Map.entry("offsetDateTime", "4c1f703d6d42b0fb50486167"),
        Map.entry("url", "74531d4687701d66d934b5ee"),
        Map.entry("uri", "31b47f18c472c5440a58d52a"),
        Map.entry("hostname", "19b4e78205ccaf0749ab5a12"),
        Map.entry("companyEmail", "ccea41456d3b88d6e7d49386"),
        Map.entry("fullName", "3ff50ab8692c5c9eeacf2247"),
        Map.entry("fullNameWithMiddle", "d246a538b314c794bd9fc0b5"),
        Map.entry("fullNameWithInitial", "62df8c904e46ec850c5c6f30"),
        Map.entry("avatarUrl", "7a4d938d02df1c3636e0698a"),
        Map.entry("username", "0bc10e0229f82fdc39588e4d"),
        Map.entry("socialProfile", "792648ef3b6b8abbbd533237"),
        Map.entry("middleName", "03807ed314b786ac2c341432"),
        Map.entry("addressInfo", "d59d23569b064c822a11b2f2"),
        Map.entry("commerce", "17ce302ae4e7a1fb4764c2b5"),
        Map.entry("email", "3e67be363e85ca680a9086f2"),
        Map.entry("emailCompany", "6c049cd0069cb419df34d408"),
        Map.entry("orderInfo", "7ed5d06bacc49dc04ce388ce"),
        Map.entry("contactInfo", "4ded7f8bf60c7b75fabba896"),
        Map.entry("streetAddress", "1ec7f4fed1d423cc422b036d"),
        Map.entry("personInfo", "ff25b9f9142457b0ae71dfa0"),
        Map.entry("shipmentInfo", "16cdf6f7b0defd479b7dcfaa"),
        Map.entry("profile", "d9de9c062db5cfe2661a0b9f"),
        Map.entry("filePath", "0fb9282e391985e741bbe3c3"),
        Map.entry("productInfo", "0845051e287f5b32eb282e5e"),
        Map.entry("jobInfo", "2431a6433c670a55984ff5f3"),
        Map.entry("invoiceInfo", "cbd1bd0a3ef5666b76a621a7"),
        Map.entry("companyInfo", "6bd80d864d5d7becc66cb59c"),
        Map.entry("simpleProfile", "6e6b3359d857f89766cb2814"),
        Map.entry("bankInfo", "c645fe5e79939c29e2b3f272"),
        Map.entry("paymentInfo", "9a3883a42d73f200840d819b"),
        Map.entry("creditCardInfo", "52c3d45de401c08fda0f4393"),
        Map.entry("priceTag", "a65eeb3bbeb6f0315a0add90")
    );

    @Test
    @DisplayName("the default legacy policy keeps every composite's seeded output unchanged")
    void legacyPolicyKeepsSeededCompositeOutput() {
        GeneratorConfig config = baseConfig().build();

        assertEquals(ChildStreamPolicy.LEGACY, config.getChildStreamPolicy());
        assertEquals(LEGACY_FINGERPRINTS, fingerprints(config));
    }

    @Test
    @DisplayName("legacy children of a seeded composite start in the same state")
    void legacyChildrenShareOneStartingState() {
        PaymentInfo payment = new PaymentInfoGenerator(baseConfig().build()).generate();

        assertEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
    }

    @Test
    @DisplayName("independent child streams decorrelate siblings and stay reproducible")
    void independentPolicyGivesChildrenIndependentStreams() {
        GeneratorConfig config = baseConfig().childStreamPolicy(ChildStreamPolicy.INDEPENDENT).build();
        PaymentInfo payment = new PaymentInfoGenerator(config).generate();

        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
        assertNotEquals(suffix(payment.invoiceNumber()), suffix(payment.orderNumber()));
        assertEquals(payment, new PaymentInfoGenerator(config).generate());
        assertEquals(fingerprints(config), fingerprints(config));
        Map<String, String> legacy = fingerprints(baseConfig().build());
        Map<String, String> independent = fingerprints(config);
        for (String composite : legacy.keySet()) {
            if (!"commerce".equals(composite) && !"streetAddress".equals(composite)) {
                assertNotEquals(legacy.get(composite), independent.get(composite), composite);
            }
        }
        // These generate() methods only use the parent's own stream; their children serve other methods.
        assertEquals(legacy.get("commerce"), independent.get("commerce"));
        assertEquals(legacy.get("streetAddress"), independent.get("streetAddress"));
        assertNotEquals(new CommerceGenerator(baseConfig().build()).generateUpc(),
                        new CommerceGenerator(config).generateUpc());
        assertNotEquals(new StreetAddressGenerator(baseConfig().build()).generateFullAddress(),
                        new StreetAddressGenerator(config).generateFullAddress());
    }

    @Test
    @DisplayName("independent ISBN generators and nationality names use their own named streams")
    void independentIsbnAndNationalityNamesUseNamedStreams() {
        GeneratorConfig legacy = baseConfig().build();
        GeneratorConfig independent = baseConfig().childStreamPolicy(ChildStreamPolicy.INDEPENDENT).build();
        CommerceGenerator commerce = new CommerceGenerator(independent);

        assertEquals(new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, independent.forChildStream("isbn10")).generate(),
                     commerce.generateIsbn10());
        assertEquals(new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, independent.forChildStream("isbn13")).generate(),
                     commerce.generateIsbn13());

        FullNameGenerator.NameOptions italian = new FullNameGenerator.NameOptions(false, false, false, false, false, null, "it");
        assertNotEquals(nationalityNames(legacy, italian), nationalityNames(independent, italian),
                        "independent first and last names must not reuse the nationality's shared stream");
        assertEquals(nationalityNames(independent, italian), nationalityNames(independent, italian));
    }

    private static java.util.List<String> nationalityNames(GeneratorConfig config, FullNameGenerator.NameOptions options) {
        FullNameGenerator generator = new FullNameGenerator(config);
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            names.add(generator.generate(options));
        }
        return names;
    }

    @Test
    @DisplayName("child configurations derive named seeds only under the independent policy")
    void childConfigurationsDeriveNamedSeeds() {
        GeneratorConfig legacy = baseConfig().build();
        GeneratorConfig independent = GeneratorConfig.builder()
                                                     .seed("child-streams")
                                                     .childStreamPolicy(ChildStreamPolicy.INDEPENDENT)
                                                     .build();

        assertSame(legacy, legacy.forChildStream("invoice"));
        GeneratorConfig invoice = independent.forChildStream("invoice");
        assertEquals(GenerationRecipe.deriveChildSeed(independent.getSeed().getAsLong(), "invoice"),
                     invoice.getSeed().getAsLong());
        assertTrue(invoice.getStringSeed().isEmpty());
        assertEquals(ChildStreamPolicy.INDEPENDENT, invoice.getChildStreamPolicy());
        assertEquals(independent.getLocale(), invoice.getLocale());
        assertNotEquals(invoice.getSeed(), independent.forChildStream("order").getSeed());
        assertThrows(IllegalArgumentException.class, () -> independent.forChildStream(" "));
        assertThrows(NullPointerException.class, () -> independent.forChildStream(null));
    }

    @Test
    @DisplayName("independent child streams require a seed and are recorded in the recipe")
    void independentPolicyIsSeededAndRecorded() {
        assertThrows(IllegalStateException.class,
                     () -> GeneratorConfig.builder().childStreamPolicy(ChildStreamPolicy.INDEPENDENT).build());
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().childStreamPolicy(null));

        GeneratorConfig independent = baseConfig().childStreamPolicy(ChildStreamPolicy.INDEPENDENT).build();
        GenerationRecipe recipe = independent.getGenerationRecipe().orElseThrow();
        GeneratorConfig replayed = GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();

        assertEquals("INDEPENDENT", recipe.getSettings().get("child-stream-policy"));
        assertEquals(ChildStreamPolicy.INDEPENDENT, replayed.getChildStreamPolicy());
        assertEquals(ChildStreamPolicy.INDEPENDENT, independent.toBuilder().build().getChildStreamPolicy());
        assertEquals(new PaymentInfoGenerator(independent).generate(), new PaymentInfoGenerator(replayed).generate());
        assertTrue(baseConfig().build().getGenerationRecipe().orElseThrow().getSettings()
                               .keySet().stream().noneMatch("child-stream-policy"::equals));
    }

    private static GeneratorConfig.Builder baseConfig() {
        return GeneratorConfig.builder()
                              .seed(42L)
                              .clock(FIXED_CLOCK)
                              .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    private static Map<String, String> fingerprints(GeneratorConfig config) {
        Map<String, Function<GeneratorConfig, Supplier<?>>> composites = new LinkedHashMap<>();
        composites.put("sentence", c -> new SentenceGenerator(c)::generate);
        composites.put("word", c -> new WordGenerator(c)::generate);
        composites.put("paragraph", c -> new ParagraphGenerator(c)::generate);
        composites.put("offsetDateTime", c -> new OffsetDateTimeGenerator(c)::generate);
        composites.put("url", c -> new URLGenerator(c)::generate);
        composites.put("uri", c -> new UriGenerator(c)::generate);
        composites.put("hostname", c -> new HostnameGenerator(c)::generate);
        composites.put("companyEmail", c -> new CompanyEmailGenerator(c)::generate);
        composites.put("fullName", c -> new FullNameGenerator(c)::generate);
        composites.put("fullNameWithMiddle", c -> new FullNameGenerator(c)::generateWithMiddleName);
        composites.put("fullNameWithInitial", c -> new FullNameGenerator(c)::generateWithMiddleInitial);
        composites.put("avatarUrl", c -> new AvatarUrlGenerator(c)::generate);
        composites.put("username", c -> new UsernameGenerator(c)::generate);
        composites.put("socialProfile", c -> new SocialProfileGenerator(c)::generate);
        composites.put("middleName", c -> new MiddleNameGenerator(c)::generate);
        composites.put("addressInfo", c -> new AddressInfoGenerator(c)::generate);
        composites.put("commerce", c -> new CommerceGenerator(c)::generate);
        composites.put("email", c -> new EmailGenerator(c)::generate);
        composites.put("emailCompany", c -> new EmailGenerator(c)::generateCompanyEmail);
        composites.put("orderInfo", c -> new OrderInfoGenerator(c)::generate);
        composites.put("contactInfo", c -> new ContactInfoGenerator(c)::generate);
        composites.put("streetAddress", c -> new StreetAddressGenerator(c)::generate);
        composites.put("personInfo", c -> new PersonInfoGenerator(c)::generate);
        composites.put("shipmentInfo", c -> new ShipmentInfoGenerator(c)::generate);
        composites.put("profile", c -> new ProfileGenerator(c)::generate);
        composites.put("filePath", c -> new FilePathGenerator(c)::generate);
        composites.put("productInfo", c -> new ProductInfoGenerator(c)::generate);
        composites.put("jobInfo", c -> new JobInfoGenerator(c)::generate);
        composites.put("invoiceInfo", c -> new InvoiceInfoGenerator(c)::generate);
        composites.put("companyInfo", c -> new CompanyInfoGenerator(c)::generate);
        composites.put("simpleProfile", c -> new SimpleProfileGenerator(c)::generate);
        composites.put("bankInfo", c -> new BankInfoGenerator(c)::generate);
        composites.put("paymentInfo", c -> new PaymentInfoGenerator(c)::generate);
        composites.put("creditCardInfo", c -> new CreditCardInfoGenerator(c)::generate);
        composites.put("priceTag", c -> new CurrencyGenerator(c)::generatePriceTag);

        Map<String, String> result = new LinkedHashMap<>();
        composites.forEach((name, factory) -> {
            Supplier<?> supplier = factory.apply(config);
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                values.append(supplier.get()).append('\n');
            }
            result.put(name, sha256(values.toString()));
        });
        return result;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String suffix(String identifier) {
        return identifier.substring(identifier.indexOf('-') + 1);
    }
}
