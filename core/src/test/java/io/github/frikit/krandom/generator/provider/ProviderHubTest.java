/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GeneratorProfile;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.krandom.generator.finance.PaymentInfoGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.selection.UniqueGenerator;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyEmailGenerator;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProviderHub")
class ProviderHubTest {

    @Test
    @DisplayName("default hub exposes built-in providers and aliases")
    void builtInsAndAliasesAreAvailable() {
        ProviderHub hub = new ProviderHub();

        assertTrue(hub.providerNames().contains("person"));
        assertTrue(hub.providerNames().contains("internet"));
        assertTrue(hub.providerNames().contains("person.full_name"));
        assertTrue(hub.providerNames().contains("person.contact_info"));
        assertTrue(hub.providerNames().contains("person.person_info"));
        assertTrue(hub.providerNames().contains("person.job_info"));
        assertTrue(hub.providerNames().contains("address.address_info"));
        assertTrue(hub.providerNames().contains("address.city"));
        assertTrue(hub.providerNames().contains("commerce.product_info"));
        assertTrue(hub.providerNames().contains("commerce.order_info"));
        assertTrue(hub.providerNames().contains("commerce.shipment_info"));
        assertTrue(hub.providerNames().contains("internet.domain"));
        assertTrue(hub.providerNames().contains("finance.currency"));
        assertTrue(hub.providerNames().contains("finance.bank_info"));
        assertTrue(hub.providerNames().contains("finance.credit_card_info"));
        assertTrue(hub.providerNames().contains("finance.invoice_info"));
        assertTrue(hub.providerNames().contains("finance.payment_info"));
        assertTrue(hub.providerNames().contains("datetime.time"));
        assertTrue(hub.providerNames().contains("text.sentence"));
        assertTrue(hub.providerNames().contains("text.format"));
        assertTrue(hub.providerNames().contains("code.uuid"));
        assertTrue(hub.providerNames().contains("company.name"));
        assertTrue(hub.providerNames().contains("company.email"));
        assertTrue(hub.providerNames().contains("company.url"));
        assertTrue(hub.providerNames().contains("company.buzzword"));
        assertTrue(hub.providerNames().contains("company.catch_phrase"));
        assertTrue(hub.providerNames().contains("company.industry"));
        assertTrue(hub.providerNames().contains("company.info"));
        assertTrue(hub.providerNames().contains("security.password"));
        assertEquals("person.full_name", hub.aliases().get("full_name"));
        assertEquals("person.contact_info", hub.aliases().get("contact_info"));
        assertEquals("person.person_info", hub.aliases().get("person_info"));
        assertEquals("person.job_info", hub.aliases().get("job_info"));
        assertEquals("address.address_info", hub.aliases().get("address_info"));
        assertEquals("commerce.product_info", hub.aliases().get("product_info"));
        assertEquals("commerce.shipment_info", hub.aliases().get("shipment_info"));
        assertEquals("internet.url", hub.aliases().get("url"));
        assertEquals("finance.bank_info", hub.aliases().get("bank_info"));
        assertEquals("finance.credit_card_info", hub.aliases().get("credit_card_info"));
        assertEquals("commerce.order_info", hub.aliases().get("order_info"));
        assertEquals("finance.invoice_info", hub.aliases().get("invoice_info"));
        assertEquals("finance.payment_info", hub.aliases().get("payment_info"));
        assertEquals("company.name", hub.aliases().get("company_name"));
        assertEquals("company.email", hub.aliases().get("company_email"));
        assertEquals("company.url", hub.aliases().get("company_url"));
        assertEquals("company.buzzword", hub.aliases().get("company_buzzword"));
        assertEquals("company.catch_phrase", hub.aliases().get("company_catch_phrase"));
        assertEquals("company.industry", hub.aliases().get("company_industry"));
        assertEquals("company.info", hub.aliases().get("company_info"));
        assertEquals("security.password", hub.aliases().get("password"));

        assertInstanceOf(FullNameGenerator.class, hub.get("person"));
        assertInstanceOf(FullNameGenerator.class, hub.get("full_name"));
        assertInstanceOf(FirstNameGenerator.class, hub.get("person.first_name"));
        assertInstanceOf(LastNameGenerator.class, hub.get("person.last_name"));
        assertInstanceOf(EmailGenerator.class, hub.get("person.email"));
        assertInstanceOf(UsernameGenerator.class, hub.get("person.username"));
        assertInstanceOf(ContactInfoGenerator.class, hub.get("person.contact_info"));
        assertInstanceOf(PersonInfoGenerator.class, hub.get("person.person_info"));
        assertInstanceOf(JobInfoGenerator.class, hub.get("person.job_info"));
        assertInstanceOf(CompanyNameGenerator.class, hub.get("company.name"));
        assertInstanceOf(CompanyEmailGenerator.class, hub.get("company.email"));
        assertInstanceOf(CompanyUrlGenerator.class, hub.get("company.url"));
        assertInstanceOf(CompanyBuzzwordGenerator.class, hub.get("company.buzzword"));
        assertInstanceOf(CompanyCatchPhraseGenerator.class, hub.get("company.catch_phrase"));
        assertInstanceOf(IndustryGenerator.class, hub.get("company.industry"));
        assertInstanceOf(CompanyInfoGenerator.class, hub.get("company.info"));
        assertInstanceOf(PasswordGenerator.class, hub.get("security.password"));
        assertInstanceOf(AddressInfoGenerator.class, hub.get("address.address_info"));
        assertInstanceOf(StreetAddressGenerator.class, hub.get("address"));
        assertInstanceOf(CityGenerator.class, hub.get("address.city"));
        assertInstanceOf(PhoneNumberGenerator.class, hub.get("address.phone_number"));
        assertInstanceOf(ProductInfoGenerator.class, hub.get("commerce.product_info"));
        assertInstanceOf(OrderInfoGenerator.class, hub.get("commerce.order_info"));
        assertInstanceOf(ShipmentInfoGenerator.class, hub.get("commerce.shipment_info"));
        assertInstanceOf(URLGenerator.class, hub.get("internet"));
        assertInstanceOf(URLGenerator.class, hub.get("url"));
        assertInstanceOf(DomainGenerator.class, hub.get("internet.domain"));
        assertInstanceOf(HostnameGenerator.class, hub.get("internet.hostname"));
        assertInstanceOf(MoneyGenerator.class, hub.get("finance"));
        assertInstanceOf(CurrencyGenerator.class, hub.get("finance.currency"));
        assertInstanceOf(BankInfoGenerator.class, hub.get("finance.bank_info"));
        assertInstanceOf(CreditCardInfoGenerator.class, hub.get("finance.credit_card_info"));
        assertInstanceOf(InvoiceInfoGenerator.class, hub.get("finance.invoice_info"));
        assertInstanceOf(PaymentInfoGenerator.class, hub.get("finance.payment_info"));
        assertInstanceOf(DateGenerator.class, hub.get("datetime"));
        assertInstanceOf(TimeGenerator.class, hub.get("datetime.time"));
        assertInstanceOf(WordGenerator.class, hub.get("text"));
        assertInstanceOf(SentenceGenerator.class, hub.get("text.sentence"));
        assertInstanceOf(ParagraphGenerator.class, hub.get("text.paragraph"));
        assertInstanceOf(TextFormatProvider.class, hub.get("text.format"));
        assertInstanceOf(UUIDGenerator.class, hub.get("code"));
        assertInstanceOf(UUIDGenerator.class, hub.get("code.uuid"));
    }

    @Test
    @DisplayName("locale and seed config is propagated to custom providers")
    void localeAndSeedPropagateToFactory() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.JAPAN).seed(42L).build();
        ProviderHub hub = new ProviderHub(config);
        AtomicReference<GeneratorConfig> observed = new AtomicReference<>();

        hub.register("custom", cfg -> {
            observed.set(cfg);
            return "ok";
        });

        assertEquals("ok", hub.get("custom"));
        assertNotNull(observed.get());
        assertEquals(Locale.JAPAN, observed.get().getLocale());
        assertTrue(observed.get().getSeed().isPresent());
        assertEquals(42L, observed.get().getSeed().getAsLong());
    }

    @Test
    @DisplayName("typed get validates runtime type")
    void typedGetValidatesType() {
        ProviderHub hub = new ProviderHub(Locale.US);

        MoneyGenerator money = hub.get("finance", MoneyGenerator.class);
        assertNotNull(money.generate());

        IllegalArgumentException mismatch = assertThrows(IllegalArgumentException.class,
                                                         () -> hub.get("finance", FullNameGenerator.class));
        assertTrue(mismatch.getMessage().contains("not"));
    }

    @Test
    @DisplayName("registration conflict can fail or replace")
    void registerConflictPolicy() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class,
                     () -> hub.register("person", cfg -> "x"));

        hub.register("person", cfg -> "replacement", ConflictPolicy.REPLACE);
        assertEquals("replacement", hub.get("person"));
    }

    @Test
    @DisplayName("alias registration validates target and conflict policy")
    void aliasRegistrationAndConflicts() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class,
                     () -> hub.registerAlias("mystery", "missing"));

        hub.register("custom", cfg -> 10);
        hub.registerAlias("c", "custom");
        assertEquals(10, hub.get("c"));

        assertThrows(IllegalArgumentException.class,
                     () -> hub.registerAlias("c", "custom"));

        hub.registerAlias("c", "custom", ConflictPolicy.REPLACE);
        assertEquals(10, hub.get("c"));

        assertThrows(IllegalArgumentException.class,
                     () -> hub.registerAlias("person", "custom", ConflictPolicy.REPLACE));
    }

    @Test
    @DisplayName("has returns true for canonical names and aliases")
    void hasChecksCanonicalAndAlias() {
        ProviderHub hub = new ProviderHub();

        assertTrue(hub.has("person"));
        assertTrue(hub.has("full_name"));
        assertTrue(hub.has("person.email"));
        assertTrue(hub.has("contact_info"));
        assertTrue(hub.has("product_info"));
        assertTrue(hub.has("format"));
        assertFalse(hub.has("not_exists"));
    }

    @Test
    @DisplayName("text format helpers are exposed through provider hub")
    void textFormatHelpersAreExposed() {
        ProviderHub hub = new ProviderHub(GeneratorConfig.builder().locale(Locale.US).seed(42L).build());
        TextFormatProvider format = hub.get("text.format", TextFormatProvider.class);

        assertTrue(format.lexify("??").matches("[a-z]{2}"));
        assertTrue(format.lexify("??", true).matches("[A-Z]{2}"));
        assertTrue(format.template("??-##").matches("[a-z]{2}-\\d{2}"));
        assertTrue(format.asciify("***").chars().noneMatch(ch -> ch == '*'));
        assertTrue(format.regexify("[A-Z]{2}\\d{3}").matches("[A-Z]{2}\\d{3}"));
        assertTrue(format.examplify("AA-999").matches("[A-Z]{2}-\\d{3}"));
    }

    @Test
    @DisplayName("provider hub exposes reusable uniqueness helpers")
    void providerHubExposesUniquenessHelpers() {
        ProviderHub hub = new ProviderHub();

        UniqueGenerator<UUID> unique = hub.unique(new UUIDGenerator());
        String first = unique.generate().toString();
        String second = unique.generate().toString();
        assertNotNull(first);
        assertNotNull(second);
        assertFalse(first.equals(second));

        UniqueGenerator<String> bounded = hub.unique(() -> UUID.randomUUID().toString(), 3);
        assertNotNull(bounded.generate());

        UniqueGenerator<String> comparatorDistinct = hub.unique(() -> UUID.randomUUID().toString(), String::equalsIgnoreCase);
        assertNotNull(comparatorDistinct.generate());

        assertThrows(NullPointerException.class, () -> hub.unique(null));
        assertThrows(NullPointerException.class, () -> hub.unique(null, 3));
        assertThrows(NullPointerException.class, () -> hub.unique(new UUIDGenerator(), null));
    }

    @Test
    @DisplayName("leaf built-ins remain reachable through their canonical names")
    void leafBuiltInsRemainReachableThroughCanonicalNames() {
        ProviderHub hub = new ProviderHub();

        assertInstanceOf(MoneyGenerator.class, hub.get("finance.money"));
        assertInstanceOf(DateGenerator.class, hub.get("datetime.date"));
        assertInstanceOf(WordGenerator.class, hub.get("text.word"));
    }

    @Test
    @DisplayName("unknown provider throws and null or blank names are rejected")
    void unknownAndInvalidNameValidation() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class, () -> hub.get("missing"));
        assertThrows(NullPointerException.class, () -> hub.get(null));
        assertThrows(IllegalArgumentException.class, () -> hub.get("   "));
        assertThrows(NullPointerException.class, () -> hub.get("person", null));
    }

    @Test
    @DisplayName("constructor and registration validate null arguments")
    void nullArgumentValidation() {
        assertThrows(NullPointerException.class, () -> new ProviderHub((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new ProviderHub((Locale) null));
        assertThrows(NullPointerException.class, () -> new ProviderHub((GeneratorProfile) null));
        assertThrows(NullPointerException.class, () -> new ProviderHub(Locale.US, null));
        assertThrows(NullPointerException.class, () -> new ProviderHub(GeneratorConfig.defaults(), null));

        ProviderHub hub = new ProviderHub();
        assertThrows(NullPointerException.class, () -> hub.register("x", (ProviderFactory) null));
        assertThrows(NullPointerException.class, () -> hub.register("x", (ProfiledProviderFactory) null));
        assertThrows(NullPointerException.class, () -> hub.register("x", cfg -> "v", null));
        assertThrows(NullPointerException.class, () -> hub.registerAlias("x", "person", null));
    }

    @Test
    @DisplayName("getConfig returns the configured generator config")
    void getConfigReturnsConfiguredValue() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.ITALY).seed(7L).build();
        ProviderHub hub = new ProviderHub(config);

        assertSame(config, hub.getConfig());
    }

    @Test
    @DisplayName("alias equal to target canonical name is accepted")
    void aliasEqualToTargetCanonicalNameAllowed() {
        ProviderHub hub = new ProviderHub();

        hub.registerAlias("person", "person", ConflictPolicy.REPLACE);
        assertEquals("person", hub.aliases().get("person"));
    }

    @Test
    @DisplayName("profile-based constructors and registration expose profile metadata")
    void profileConstructorsAndRegistration() {
        ProviderHub hub = new ProviderHub(GeneratorProfile.FAST);
        assertSame(GeneratorProfile.FAST, hub.getProfile());

        ProviderHub localeHub = new ProviderHub(Locale.JAPAN, GeneratorProfile.STRICT);
        assertEquals(Locale.JAPAN, localeHub.getConfig().getLocale());
        assertSame(GeneratorProfile.STRICT, localeHub.getProfile());

        ProviderHub configHub = new ProviderHub(GeneratorConfig.defaults(), GeneratorProfile.REALISTIC);
        assertSame(GeneratorProfile.REALISTIC, configHub.getProfile());

        hub.register("profiled", (profile, cfg) -> profile.name() + ":" + cfg.getLocale());
        assertTrue(hub.get("profiled").toString().startsWith("FAST:"));
    }
}
