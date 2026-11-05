/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Composite child-stream policy")
class ChildStreamPolicyTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    /**
     * Fingerprints of the first three values of every composite generator, captured from the
     * legacy implementation before child streams became configurable. The legacy policy must keep
     * them byte-identical. The {@code profile} and {@code simpleProfile} birthdays used to follow the
     * system clock, so their fingerprints were recaptured once birthdays followed the configured
     * clock. The composites with an {@code en_US} address were recaptured once an address took its
     * city, state and ZIP code from one postal place, and named the locale's country.
     */
    private static final Map<String, String> LEGACY_FINGERPRINTS = Map.ofEntries(
            Map.entry("sentence", "f320293937a27329fc0cee1e"),
            Map.entry("word", "4b1b4796b57772f862f682b5"),
            Map.entry("paragraph", "4e0a887771021be3c1164794"),
            Map.entry("offsetDateTime", "bb258657dc5a38b2ef8bd84f"),
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
            Map.entry("addressInfo", "33e9fb7bdec903d1fa9681c5"),
            Map.entry("commerce", "17ce302ae4e7a1fb4764c2b5"),
            Map.entry("email", "3e67be363e85ca680a9086f2"),
            Map.entry("emailCompany", "6c049cd0069cb419df34d408"),
            Map.entry("orderInfo", "61f808e175b8f527ae677bd1"),
            Map.entry("contactInfo", "4ded7f8bf60c7b75fabba896"),
            Map.entry("streetAddress", "1ec7f4fed1d423cc422b036d"),
            Map.entry("personInfo", "4017f14e0910e7b6f79e36ce"),
            Map.entry("shipmentInfo", "10921da58ff268a88bd5a588"),
            Map.entry("profile", "79b8d372b12276846097d954"),
            Map.entry("filePath", "0fb9282e391985e741bbe3c3"),
            Map.entry("productInfo", "0845051e287f5b32eb282e5e"),
            Map.entry("jobInfo", "2431a6433c670a55984ff5f3"),
            Map.entry("invoiceInfo", "dd04c90689bb3cba68faf100"),
            Map.entry("companyInfo", "032364465628a1ffaeb4cd7b"),
            Map.entry("simpleProfile", "0130ee2f2bc95a7feca1c321"),
            Map.entry("bankInfo", "c645fe5e79939c29e2b3f272"),
            Map.entry("paymentInfo", "2afe217878c3700f9b2a9100"),
            Map.entry("creditCardInfo", "52c3d45de401c08fda0f4393"),
            Map.entry("priceTag", "a65eeb3bbeb6f0315a0add90"));

    /**
     * The same fingerprints under the default independent policy, captured from the opt-in
     * {@link ChildStreamPolicy#INDEPENDENT} policy of 2.10.0, before it became the default, and for
     * the composites with an {@code en_US} address recaptured with their legacy fingerprints. The
     * {@code commerce} and {@code streetAddress} entries equal their legacy fingerprints because
     * those {@code generate()} methods only use the parent's own stream.
     */
    private static final Map<String, String> INDEPENDENT_FINGERPRINTS = Map.ofEntries(
            Map.entry("sentence", "ef455c60ad7bd1c55e42c413"),
            Map.entry("word", "a947c592410a2cd4721ce768"),
            Map.entry("paragraph", "729efacb53f219cd2e44928b"),
            Map.entry("offsetDateTime", "c6bf5ecd053c16335541ea34"),
            Map.entry("url", "945da45b66cfe8ec36823c70"),
            Map.entry("uri", "d6f3ceb244a25781a90ab489"),
            Map.entry("hostname", "140af7f858e34fa1674516f8"),
            Map.entry("companyEmail", "298d21c091a492eda9706aa6"),
            Map.entry("fullName", "f50583b2b93d58ed6df426f5"),
            Map.entry("fullNameWithMiddle", "591bbcbf4c604b4e4e400ce7"),
            Map.entry("fullNameWithInitial", "1e04e611f22ed005f149ef0d"),
            Map.entry("avatarUrl", "2821714eeab54a71be97163d"),
            Map.entry("username", "400f9408368e35a7aecf65a7"),
            Map.entry("socialProfile", "10d757591832cff3040a753b"),
            Map.entry("middleName", "9ea3d7c9b397f2429bd6499b"),
            Map.entry("addressInfo", "3faf7240ef852e9c91f27722"),
            Map.entry("commerce", "17ce302ae4e7a1fb4764c2b5"),
            Map.entry("email", "26b800511dedab89fbf82b61"),
            Map.entry("emailCompany", "407b6d86ab579497bcdb5fc8"),
            Map.entry("orderInfo", "cb61334ddb9aff365cecb314"),
            Map.entry("contactInfo", "045743f457119e34ef0a942d"),
            Map.entry("streetAddress", "1ec7f4fed1d423cc422b036d"),
            Map.entry("personInfo", "00e3fbd744e7620c2711890e"),
            Map.entry("shipmentInfo", "c2aa678dae6f755a0318d71b"),
            Map.entry("profile", "4789d71337858e628b1c3e07"),
            Map.entry("filePath", "27bbf3aa053cd6477449445a"),
            Map.entry("productInfo", "f9cd8efdbe56839b0167edda"),
            Map.entry("jobInfo", "8e65091f58b496238fafc2a5"),
            Map.entry("invoiceInfo", "ba1bd7a22e9b55cc1ef8f19f"),
            Map.entry("companyInfo", "4a8348e0011ca4dd46fa9b87"),
            Map.entry("simpleProfile", "bbe06d5e09be6535ea4af6ed"),
            Map.entry("bankInfo", "79da71fb019e2d16c55c379a"),
            Map.entry("paymentInfo", "03aeb1462e48eb61df9ea5fe"),
            Map.entry("creditCardInfo", "c9a336712de7a0bbbabae13a"),
            Map.entry("priceTag", "7110291b27f59b27c3c81c20"));

    @Test
    @DisplayName("independent child streams are the default and keep their seeded output")
    void independentPolicyIsTheDefault() {
        GeneratorConfig config = baseConfig().build();

        assertEquals(ChildStreamPolicy.INDEPENDENT, config.getChildStreamPolicy());
        assertEquals(ChildStreamPolicy.INDEPENDENT, GeneratorConfig.defaults().getChildStreamPolicy());
        assertEquals(INDEPENDENT_FINGERPRINTS, fingerprints(config));
        assertEquals(
                INDEPENDENT_FINGERPRINTS,
                fingerprints(baseConfig()
                        .childStreamPolicy(ChildStreamPolicy.INDEPENDENT)
                        .build()));
    }

    @Test
    @DisplayName("the legacy policy keeps every composite's earlier seeded output")
    void legacyPolicyKeepsSeededCompositeOutput() {
        GeneratorConfig config = legacyConfig();

        assertEquals(ChildStreamPolicy.LEGACY, config.getChildStreamPolicy());
        assertEquals(LEGACY_FINGERPRINTS, fingerprints(config));
    }

    @Test
    @DisplayName("legacy children of a seeded composite start in the same state")
    void legacyChildrenShareOneStartingState() {
        PaymentInfo payment = new PaymentInfoGenerator(legacyConfig()).generate();

        assertEquals("PAY-359D41BAF78A", payment.paymentNumber());
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
    }

    @Test
    @DisplayName("default child streams decorrelate siblings and stay reproducible")
    void defaultPolicyGivesChildrenIndependentStreams() {
        GeneratorConfig config = baseConfig().build();
        PaymentInfo payment = new PaymentInfoGenerator(config).generate();

        assertEquals("PAY-7DC1F9C37F29", payment.paymentNumber());
        assertEquals("INV-F852387C7D39", payment.invoiceNumber());
        assertEquals("ORD-B9D4F25FCB19", payment.orderNumber());
        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
        assertNotEquals(suffix(payment.invoiceNumber()), suffix(payment.orderNumber()));
        assertEquals(payment, new PaymentInfoGenerator(config).generate());
        Map<String, String> legacy = fingerprints(legacyConfig());
        Map<String, String> independent = fingerprints(config);
        for (String composite : legacy.keySet()) {
            if (!"commerce".equals(composite) && !"streetAddress".equals(composite)) {
                assertNotEquals(legacy.get(composite), independent.get(composite), composite);
            }
        }
        // These generate() methods only use the parent's own stream; their children serve other methods.
        assertEquals(legacy.get("commerce"), independent.get("commerce"));
        assertEquals(legacy.get("streetAddress"), independent.get("streetAddress"));
        assertNotEquals(
                new CommerceGenerator(legacyConfig()).generateUpc(), new CommerceGenerator(config).generateUpc());
        assertNotEquals(
                new StreetAddressGenerator(legacyConfig()).generateFullAddress(),
                new StreetAddressGenerator(config).generateFullAddress());
    }

    @Test
    @DisplayName("default ISBN generators and nationality names use their own named streams")
    void independentIsbnAndNationalityNamesUseNamedStreams() {
        GeneratorConfig legacy = legacyConfig();
        GeneratorConfig independent = baseConfig().build();
        CommerceGenerator commerce = new CommerceGenerator(independent);

        assertEquals(
                new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, independent.forChildStream("isbn10")).generate(),
                commerce.generateIsbn10());
        assertEquals(
                new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, independent.forChildStream("isbn13")).generate(),
                commerce.generateIsbn13());

        FullNameGenerator.NameOptions italian =
                new FullNameGenerator.NameOptions(false, false, false, false, false, null, "it");
        assertNotEquals(
                nationalityNames(legacy, italian),
                nationalityNames(independent, italian),
                "independent first and last names must not reuse the nationality's shared stream");
        assertEquals(nationalityNames(independent, italian), nationalityNames(independent, italian));
    }

    private static java.util.List<String> nationalityNames(
            GeneratorConfig config, FullNameGenerator.NameOptions options) {
        FullNameGenerator generator = new FullNameGenerator(config);
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            names.add(generator.generate(options));
        }
        return names;
    }

    @Test
    @DisplayName("seeded child configurations derive named seeds under the default policy only")
    void childConfigurationsDeriveNamedSeeds() {
        GeneratorConfig legacy = legacyConfig();
        GeneratorConfig independent =
                GeneratorConfig.builder().seed("child-streams").build();

        assertSame(legacy, legacy.forChildStream("invoice"));
        GeneratorConfig invoice = independent.forChildStream("invoice");
        assertEquals(
                GenerationRecipe.deriveChildSeed(independent.getSeed().getAsLong(), "invoice"),
                invoice.getSeed().getAsLong());
        assertTrue(invoice.getStringSeed().isEmpty());
        assertEquals(ChildStreamPolicy.INDEPENDENT, invoice.getChildStreamPolicy());
        assertEquals(independent.getLocale(), invoice.getLocale());
        assertNotEquals(invoice.getSeed(), independent.forChildStream("order").getSeed());
        assertThrows(IllegalArgumentException.class, () -> independent.forChildStream(" "));
        assertThrows(NullPointerException.class, () -> independent.forChildStream(null));
    }

    static Stream<Arguments> unseededConfigurations() {
        return Stream.of(
                Arguments.of(Named.of("default source", GeneratorConfig.defaults())),
                Arguments.of(Named.of(
                        "secure random",
                        GeneratorConfig.builder().secureRandom().build())),
                Arguments.of(Named.of(
                        "caller-owned random",
                        GeneratorConfig.builder().random(new Random(7L)).build())),
                Arguments.of(Named.of(
                        "random factory",
                        GeneratorConfig.builder().randomFactory(Random::new).build())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unseededConfigurations")
    @DisplayName("an unseeded configuration builds under the default policy and passes itself to every child")
    void unseededConfigurationsPassThemselvesToChildren(GeneratorConfig config) {
        assertEquals(ChildStreamPolicy.INDEPENDENT, config.getChildStreamPolicy());
        assertTrue(config.getSeed().isEmpty());
        assertSame(config, config.forChildStream("invoice"));
        for (String name : new String[] {" ", "two\nlines", "two\rlines"}) {
            IllegalArgumentException error =
                    assertThrows(IllegalArgumentException.class, () -> config.forChildStream(name));
            assertEquals("streamIdentity must be a non-blank single-line value", error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> config.forChildStream(null));
    }

    @Test
    @DisplayName("unseeded children draw from their own sources, and caller-owned randomness is never reseeded")
    void unseededChildrenDrawFromTheirOwnSources() {
        Random shared = new Random(7L);
        GeneratorConfig owned = GeneratorConfig.builder().random(shared).build();
        for (GeneratorConfig config : List.of(
                GeneratorConfig.defaults(),
                GeneratorConfig.builder().secureRandom().build(),
                owned)) {
            PaymentInfo payment = new PaymentInfoGenerator(config).generate();

            assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
            assertNotEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
            assertNotEquals(suffix(payment.invoiceNumber()), suffix(payment.orderNumber()));
        }
        assertSame(shared, owned.forChildStream("invoice").createRandom());

        AtomicInteger calls = new AtomicInteger();
        GeneratorConfig repeating = GeneratorConfig.builder()
                .randomFactory(() -> {
                    calls.incrementAndGet();
                    return new Random(7L);
                })
                .build();
        repeating.forChildStream("invoice");
        assertEquals(0, calls.get(), "forChildStream must not call the factory");
        PaymentInfo payment = new PaymentInfoGenerator(repeating).generate();
        assertTrue(calls.get() > 1, "every child calls the factory");
        // A factory that repeats one seed repeats it in every child: its sources are used as they are.
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.invoiceNumber()));
        assertEquals(suffix(payment.paymentNumber()), suffix(payment.orderNumber()));
    }

    @Test
    @DisplayName("a recipe records either policy, and replay restores it and its values")
    void recipesRecordAndReplayEitherPolicy() {
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().childStreamPolicy(null));
        for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
            GeneratorConfig config = baseConfig().childStreamPolicy(policy).build();
            GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();
            GeneratorConfig replayed =
                    GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();

            assertEquals(policy.name(), recipe.getSettings().get("child-stream-policy"));
            assertEquals(policy, replayed.getChildStreamPolicy());
            assertEquals(policy, config.toBuilder().build().getChildStreamPolicy());
            assertEquals(fingerprints(config), fingerprints(replayed));
        }
    }

    @Test
    @DisplayName("a recipe written under the earlier legacy default, without the policy setting, is rejected")
    void recipeWithoutThePolicyIsRejected() {
        String recorded = legacyConfig().getGenerationRecipe().orElseThrow().serialize();
        String earlier = recorded.replace("setting.child-stream-policy=LEGACY\n", "");
        GenerationRecipe recipe = GenerationRecipe.parse(earlier);

        assertNotEquals(recorded, earlier);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, recipe::toGeneratorConfig);
        assertEquals("Recipe requires setting: child-stream-policy", error.getMessage());
    }

    private static GeneratorConfig.Builder baseConfig() {
        return GeneratorConfig.builder()
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .seed(42L)
                .clock(FIXED_CLOCK)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    private static GeneratorConfig legacyConfig() {
        return baseConfig().childStreamPolicy(ChildStreamPolicy.LEGACY).build();
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
