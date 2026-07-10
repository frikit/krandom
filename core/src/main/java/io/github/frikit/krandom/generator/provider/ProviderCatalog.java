/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.finance.PaymentInfoGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.URLGenerator;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Canonical built-in provider definitions shared by every provider-facing API.
 *
 * <p>Custom runtime registrations remain scoped to an individual {@link ProviderHub}. This
 * catalog only defines the built-ins shipped with krandom and rejects canonical-key or alias
 * collisions during class initialization.
 */
public final class ProviderCatalog {

    private static final List<ProviderDescriptor<?>> BUILT_INS = buildBuiltIns();

    static {
        validate(BUILT_INS);
    }

    private ProviderCatalog() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns immutable definitions for every built-in provider.
     *
     * @return built-in provider descriptors in registration order
     */
    public static List<ProviderDescriptor<?>> builtIns() {
        return BUILT_INS;
    }

    static void validate(List<ProviderDescriptor<?>> descriptors) {
        Set<String> names = new HashSet<>();
        for (ProviderDescriptor<?> descriptor : descriptors) {
            addName(names, descriptor.getKey());
            for (String alias : descriptor.getAliases()) {
                addName(names, alias);
            }
        }
    }

    private static void addName(Set<String> names, String name) {
        if (!names.add(name)) {
            throw new IllegalArgumentException("Provider key or alias is duplicated: " + name);
        }
    }

    private static <T> ProviderDescriptor<T> descriptor(String key,
                                                         Class<T> providerType,
                                                         ProviderFactory factory,
                                                         List<String> aliases,
                                                         String... semanticKeys) {
        return new ProviderDescriptor<>(key, providerType, factory, aliases, Set.of(semanticKeys));
    }

    private static List<ProviderDescriptor<?>> buildBuiltIns() {
        return List.of(
            descriptor("person.full_name", FullNameGenerator.class, FullNameGenerator::new,
                       List.of("name", "full_name"), "fullname"),
            descriptor("person.first_name", FirstNameGenerator.class, FirstNameGenerator::new,
                       List.of("first_name"), "firstname"),
            descriptor("person.last_name", LastNameGenerator.class, LastNameGenerator::new,
                       List.of("last_name"), "lastname"),
            descriptor("person.email", EmailGenerator.class, EmailGenerator::new, List.of("email"), "email"),
            descriptor("person.username", UsernameGenerator.class, UsernameGenerator::new,
                       List.of("username"), "username"),
            descriptor("person.contact_info", ContactInfoGenerator.class, ContactInfoGenerator::new,
                       List.of("contact_info")),
            descriptor("person.person_info", PersonInfoGenerator.class, PersonInfoGenerator::new,
                       List.of("person_info")),
            descriptor("person.job_info", JobInfoGenerator.class, JobInfoGenerator::new, List.of("job_info")),
            descriptor("person", FullNameGenerator.class, FullNameGenerator::new, List.of()),
            descriptor("company.name", CompanyNameGenerator.class, CompanyNameGenerator::new,
                       List.of("company_name"), "companyname"),
            descriptor("company.email", CompanyEmailGenerator.class, CompanyEmailGenerator::new,
                       List.of("company_email"), "companyemail"),
            descriptor("company.url", CompanyUrlGenerator.class, CompanyUrlGenerator::new,
                       List.of("company_url"), "companyurl"),
            descriptor("company.buzzword", CompanyBuzzwordGenerator.class, CompanyBuzzwordGenerator::new,
                       List.of("company_buzzword")),
            descriptor("company.catch_phrase", CompanyCatchPhraseGenerator.class, CompanyCatchPhraseGenerator::new,
                       List.of("company_catch_phrase")),
            descriptor("company.industry", IndustryGenerator.class, IndustryGenerator::new,
                       List.of("company_industry"), "industry"),
            descriptor("company.info", CompanyInfoGenerator.class, CompanyInfoGenerator::new,
                       List.of("company_info")),
            descriptor("security.password", PasswordGenerator.class, PasswordGenerator::new,
                       List.of("password"), "password"),
            descriptor("address.address_info", AddressInfoGenerator.class, AddressInfoGenerator::new,
                       List.of("address_info")),
            descriptor("address.street_address", StreetAddressGenerator.class, StreetAddressGenerator::new,
                       List.of("location", "street_address"), "streetaddress"),
            descriptor("address.city", CityGenerator.class, CityGenerator::new, List.of("city"), "city"),
            descriptor("address.state", StateGenerator.class, StateGenerator::new, List.of("state"), "state"),
            descriptor("address.postal_code", PostalCodeGenerator.class, PostalCodeGenerator::new,
                       List.of("postal_code"), "postalcode"),
            descriptor("address.country", CountryGenerator.class, CountryGenerator::new,
                       List.of("country"), "country"),
            descriptor("address.phone_number", PhoneNumberGenerator.class, PhoneNumberGenerator::new,
                       List.of("phone_number"), "phone"),
            descriptor("address", StreetAddressGenerator.class, StreetAddressGenerator::new, List.of()),
            descriptor("internet.url", URLGenerator.class, URLGenerator::new,
                       List.of("network", "url"), "url"),
            descriptor("internet.domain", DomainGenerator.class, DomainGenerator::new, List.of("domain"), "domain"),
            descriptor("internet.hostname", HostnameGenerator.class, HostnameGenerator::new, List.of("hostname")),
            descriptor("internet", URLGenerator.class, URLGenerator::new, List.of()),
            descriptor("commerce.product_info", ProductInfoGenerator.class, ProductInfoGenerator::new,
                       List.of("product_info")),
            descriptor("commerce.order_info", OrderInfoGenerator.class, OrderInfoGenerator::new,
                       List.of("order_info")),
            descriptor("commerce.shipment_info", ShipmentInfoGenerator.class, ShipmentInfoGenerator::new,
                       List.of("shipment_info")),
            descriptor("finance.money", MoneyGenerator.class, MoneyGenerator::new, List.of("money")),
            descriptor("finance.currency", CurrencyGenerator.class, CurrencyGenerator::new,
                       List.of("currency"), "currency"),
            descriptor("finance.bank_info", BankInfoGenerator.class, BankInfoGenerator::new,
                       List.of("bank_info")),
            descriptor("finance.credit_card_info", CreditCardInfoGenerator.class, CreditCardInfoGenerator::new,
                       List.of("credit_card_info")),
            descriptor("finance.invoice_info", InvoiceInfoGenerator.class, InvoiceInfoGenerator::new,
                       List.of("invoice_info")),
            descriptor("finance.payment_info", PaymentInfoGenerator.class, PaymentInfoGenerator::new,
                       List.of("payment_info")),
            descriptor("finance", MoneyGenerator.class, MoneyGenerator::new, List.of()),
            descriptor("datetime.date", DateGenerator.class, DateGenerator::new, List.of("date")),
            descriptor("datetime.time", TimeGenerator.class, TimeGenerator::new, List.of("time")),
            descriptor("datetime", DateGenerator.class, DateGenerator::new, List.of()),
            descriptor("text.word", WordGenerator.class, WordGenerator::new, List.of("word")),
            descriptor("text.sentence", SentenceGenerator.class, SentenceGenerator::new, List.of("sentence")),
            descriptor("text.paragraph", ParagraphGenerator.class, ParagraphGenerator::new, List.of("paragraph")),
            descriptor("text.format", TextFormatProvider.class, TextFormatProvider::new, List.of("format")),
            descriptor("text", WordGenerator.class, WordGenerator::new, List.of()),
            descriptor("code.uuid", UUIDGenerator.class, UUIDGenerator::new,
                       List.of("uuid", "identifier"), "uuid"),
            descriptor("code", UUIDGenerator.class, UUIDGenerator::new, List.of())
        );
    }
}
