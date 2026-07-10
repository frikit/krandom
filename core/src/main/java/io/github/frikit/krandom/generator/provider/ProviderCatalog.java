/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.commerce.OrderInfo;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfo;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfo;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.datetime.TimezoneGenerator;
import io.github.frikit.krandom.generator.finance.BankInfo;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardInfo;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfo;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.finance.PaymentInfo;
import io.github.frikit.krandom.generator.finance.PaymentInfoGenerator;
import io.github.frikit.krandom.generator.identifier.EanGenerator;
import io.github.frikit.krandom.generator.identifier.HashGenerator;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.identifier.UpcGenerator;
import io.github.frikit.krandom.generator.location.AddressInfo;
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
import io.github.frikit.krandom.generator.user.CompanyInfo;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.ContactInfo;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.JobInfo;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.PersonInfo;
import io.github.frikit.krandom.generator.user.UsernameGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Canonical built-in provider definitions shared by every provider-facing API.
 *
 * <p>Custom runtime registrations remain scoped to an individual {@link ProviderHub}. This
 * catalog only defines the built-ins shipped with krandom and rejects canonical-key or alias
 * collisions during class initialization.
 */
public final class ProviderCatalog {

    private static final ProviderSafetyMetadata PAYMENT_CARD_SAFETY = new ProviderSafetyMetadata(
        ProviderValidity.GUARANTEED,
        ProviderValidity.CONFIGURATION_DEPENDENT,
        ProviderValidity.GUARANTEED,
        ProviderTestSafety.CONFIGURATION_DEPENDENT,
        ProviderSafetyPolicy.PAYMENT_CARD);
    private static final ProviderSafetyMetadata PHONE_NUMBER_SAFETY = new ProviderSafetyMetadata(
        ProviderValidity.GUARANTEED,
        ProviderValidity.NOT_APPLICABLE,
        ProviderValidity.CONFIGURATION_DEPENDENT,
        ProviderTestSafety.CONFIGURATION_DEPENDENT,
        ProviderSafetyPolicy.PHONE_NUMBER);
    private static final ProviderSafetyMetadata BANKING_SAFETY = new ProviderSafetyMetadata(
        ProviderValidity.CONFIGURATION_DEPENDENT,
        ProviderValidity.CONFIGURATION_DEPENDENT,
        ProviderValidity.CONFIGURATION_DEPENDENT,
        ProviderTestSafety.UNCLASSIFIED,
        ProviderSafetyPolicy.BANKING);
    private static final List<ProviderDescriptor<?>> BUILT_INS = buildBuiltIns();
    private static final List<ProviderDescriptor<?>> SCHEMA_ONLY_BUILT_INS = buildSchemaOnlyBuiltIns();
    private static final List<ProviderDescriptor<?>> SCHEMA_BUILT_INS = buildSchemaBuiltIns();

    static {
        validate(BUILT_INS);
        validateSchema(SCHEMA_BUILT_INS);
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

    /**
     * Returns immutable definitions for every provider that exposes a built-in schema reference.
     *
     * @return schema-capable built-in descriptors in registration order
     */
    public static List<ProviderDescriptor<?>> schemaBuiltIns() {
        return SCHEMA_BUILT_INS;
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

    static void validateSchema(List<ProviderDescriptor<?>> descriptors) {
        Set<String> references = new HashSet<>();
        for (ProviderDescriptor<?> descriptor : descriptors) {
            for (ProviderSchemaProjection<?> projection : descriptor.getSchemaProjections()) {
                addName(references, projection.getReference());
                for (String alias : projection.getAliases()) {
                    addName(references, alias);
                }
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
        return new ProviderDescriptor<>(key, providerType, factory, aliases, Set.of(semanticKeys), List.of());
    }

    @SafeVarargs
    private static <T> List<ProviderSchemaProjection<T>> projections(ProviderSchemaProjection<T>... projections) {
        return List.of(projections);
    }

    private static <T> ProviderSchemaProjection<T> string(String reference,
                                                           BiFunction<? super T, io.github.frikit.krandom.generator.GeneratorConfig, ?> extractor,
                                                           String... aliases) {
        return new ProviderSchemaProjection<>(reference, List.of(aliases), extractor, false, null, null, Set.of());
    }

    private static <T> ProviderSchemaProjection<T> stringFormat(String reference,
                                                                 BiFunction<? super T, io.github.frikit.krandom.generator.GeneratorConfig, ?> extractor,
                                                                 String format,
                                                                 String... aliases) {
        return new ProviderSchemaProjection<>(reference, List.of(aliases), extractor, false, format, null, Set.of());
    }

    private static <T> ProviderSchemaProjection<T> integer(String reference,
                                                            BiFunction<? super T, io.github.frikit.krandom.generator.GeneratorConfig, ?> extractor,
                                                            String... aliases) {
        return new ProviderSchemaProjection<>(reference, List.of(aliases), extractor, true, null, null, Set.of());
    }

    private static <T> ProviderSchemaProjection<T> record(String reference,
                                                           BiFunction<? super T, io.github.frikit.krandom.generator.GeneratorConfig, ?> extractor,
                                                           Class<?> recordType,
                                                           List<String> aliases,
                                                           Set<String> nullableComponents) {
        return new ProviderSchemaProjection<>(reference,
                                              aliases,
                                              extractor,
                                              false,
                                              null,
                                              recordType,
                                              nullableComponents);
    }

    private static List<ProviderDescriptor<?>> buildSchemaBuiltIns() {
        List<ProviderDescriptor<?>> descriptors = new ArrayList<>(BUILT_INS);
        descriptors.addAll(SCHEMA_ONLY_BUILT_INS);
        return List.copyOf(descriptors);
    }

    private static List<ProviderDescriptor<?>> buildBuiltIns() {
        return List.of(
            descriptor("person.full_name", FullNameGenerator.class, FullNameGenerator::new,
                       List.of("name", "full_name"), "fullname")
                .withSchemaProjections(projections(string("person.full_name",
                                                          (provider, config) -> provider.generate(),
                                                          "name",
                                                          "full_name",
                                                          "fullname"))),
            descriptor("person.first_name", FirstNameGenerator.class, FirstNameGenerator::new,
                       List.of("first_name"), "firstname")
                .withSchemaProjections(projections(string("person.first_name",
                                                          (provider, config) -> provider.generate(),
                                                          "first_name",
                                                          "firstname"))),
            descriptor("person.last_name", LastNameGenerator.class, LastNameGenerator::new,
                       List.of("last_name"), "lastname")
                .withSchemaProjections(projections(string("person.last_name",
                                                          (provider, config) -> provider.generate(),
                                                          "last_name",
                                                          "lastname"))),
            descriptor("person.email", EmailGenerator.class, EmailGenerator::new, List.of("email"), "email")
                .withSchemaProjections(projections(stringFormat("person.email",
                                                                (provider, config) -> provider.generate(),
                                                                "email",
                                                                "email"))),
            descriptor("person.username", UsernameGenerator.class, UsernameGenerator::new,
                       List.of("username"), "username")
                .withSchemaProjections(projections(string("person.username",
                                                          (provider, config) -> provider.generate(),
                                                          "username"))),
            descriptor("person.contact_info", ContactInfoGenerator.class, ContactInfoGenerator::new,
                       List.of("contact_info"))
                .withSchemaProjections(projections(record("person.contact_info",
                                                          (provider, config) -> provider.generate(),
                                                          ContactInfo.class,
                                                          List.of("contact_info"),
                                                          Set.of()))),
            descriptor("person.person_info", PersonInfoGenerator.class, PersonInfoGenerator::new,
                       List.of("person_info"))
                .withSchemaProjections(projections(record("person.person_info",
                                                          (provider, config) -> provider.generate(),
                                                          PersonInfo.class,
                                                          List.of("person_info"),
                                                          Set.of()))),
            descriptor("person.job_info", JobInfoGenerator.class, JobInfoGenerator::new, List.of("job_info"))
                .withSchemaProjections(projections(record("person.job_info",
                                                          (provider, config) -> provider.generate(),
                                                          JobInfo.class,
                                                          List.of("job_info"),
                                                          Set.of()))),
            descriptor("person", FullNameGenerator.class, FullNameGenerator::new, List.of())
                .withSchemaProjections(projections(string("person", (provider, config) -> provider.generate()))),
            descriptor("company.name", CompanyNameGenerator.class, CompanyNameGenerator::new,
                       List.of("company_name"), "companyname")
                .withSchemaProjections(projections(string("company.name",
                                                          (provider, config) -> provider.generate(),
                                                          "company_name"))),
            descriptor("company.email", CompanyEmailGenerator.class, CompanyEmailGenerator::new,
                       List.of("company_email"), "companyemail")
                .withSchemaProjections(projections(stringFormat("company.email",
                                                                (provider, config) -> provider.generate(),
                                                                "email",
                                                                "company_email"))),
            descriptor("company.url", CompanyUrlGenerator.class, CompanyUrlGenerator::new,
                       List.of("company_url"), "companyurl")
                .withSchemaProjections(projections(stringFormat("company.url",
                                                                (provider, config) -> provider.generate(),
                                                                "uri",
                                                                "company_url"))),
            descriptor("company.buzzword", CompanyBuzzwordGenerator.class, CompanyBuzzwordGenerator::new,
                       List.of("company_buzzword"))
                .withSchemaProjections(projections(string("company.buzzword",
                                                          (provider, config) -> provider.generate(),
                                                          "company_buzzword"))),
            descriptor("company.catch_phrase", CompanyCatchPhraseGenerator.class, CompanyCatchPhraseGenerator::new,
                       List.of("company_catch_phrase"))
                .withSchemaProjections(projections(string("company.catch_phrase",
                                                          (provider, config) -> provider.generate(),
                                                          "company_catch_phrase"))),
            descriptor("company.industry", IndustryGenerator.class, IndustryGenerator::new,
                       List.of("company_industry"), "industry")
                .withSchemaProjections(projections(string("company.industry",
                                                          (provider, config) -> provider.generate(),
                                                          "company_industry"))),
            descriptor("company.info", CompanyInfoGenerator.class, CompanyInfoGenerator::new,
                       List.of("company_info"))
                .withSchemaProjections(projections(record("company.info",
                                                          (provider, config) -> provider.generate(),
                                                          CompanyInfo.class,
                                                          List.of("company_info"),
                                                          Set.of()))),
            descriptor("security.password", PasswordGenerator.class, PasswordGenerator::new,
                       List.of("password"), "password")
                .withSchemaProjections(projections(string("security.password",
                                                          (provider, config) -> provider.generate(),
                                                          "password"))),
            descriptor("address.address_info", AddressInfoGenerator.class, AddressInfoGenerator::new,
                       List.of("address_info"))
                .withSchemaProjections(projections(record("address.address_info",
                                                          (provider, config) -> provider.generate(),
                                                          AddressInfo.class,
                                                          List.of("address_info"),
                                                          Set.of()))),
            descriptor("address.street_address", StreetAddressGenerator.class, StreetAddressGenerator::new,
                       List.of("location", "street_address"), "streetaddress")
                .withSchemaProjections(projections(string("address.street",
                                                          (provider, config) -> provider.generate()),
                                                  string("address.street_address",
                                                         (provider, config) -> provider.generate(),
                                                         "location",
                                                         "street_address"),
                                                  string("address", (provider, config) -> provider.generate()))),
            descriptor("address.city", CityGenerator.class, CityGenerator::new, List.of("city"), "city")
                .withSchemaProjections(projections(string("address.city",
                                                          (provider, config) -> provider.generate(),
                                                          "city"))),
            descriptor("address.state", StateGenerator.class, StateGenerator::new, List.of("state"), "state")
                .withSchemaProjections(projections(string("address.state",
                                                          (provider, config) -> provider.generate(),
                                                          "state"))),
            descriptor("address.postal_code", PostalCodeGenerator.class, PostalCodeGenerator::new,
                       List.of("postal_code"), "postalcode")
                .withSchemaProjections(projections(string("address.postal_code",
                                                          (provider, config) -> provider.generate(),
                                                          "postal_code"))),
            descriptor("address.country", CountryGenerator.class, CountryGenerator::new,
                       List.of("country"), "country")
                .withSchemaProjections(projections(string("address.country",
                                                          (provider, config) -> provider.generate(),
                                                          "country"))),
            descriptor("address.phone_number", PhoneNumberGenerator.class, PhoneNumberGenerator::new,
                       List.of("phone_number"), "phone")
                .withSafetyMetadata(PHONE_NUMBER_SAFETY)
                .withSchemaProjections(projections(ProviderCatalog.<PhoneNumberGenerator>string("address.phone_number",
                                                          (provider, config) -> provider.generate(),
                                                          "phone_number")
                                           .withSafetyMetadata(PHONE_NUMBER_SAFETY))),
            descriptor("address", StreetAddressGenerator.class, StreetAddressGenerator::new, List.of()),
            descriptor("internet.url", URLGenerator.class, URLGenerator::new,
                       List.of("network", "url"), "url")
                .withSchemaProjections(projections(stringFormat("internet.url",
                                                                (provider, config) -> provider.generate(),
                                                                "uri",
                                                                "network",
                                                                "url"))),
            descriptor("internet.domain", DomainGenerator.class, DomainGenerator::new, List.of("domain"), "domain")
                .withSchemaProjections(projections(string("internet.domain",
                                                          (provider, config) -> provider.generate(),
                                                          "domain"))),
            descriptor("internet.hostname", HostnameGenerator.class, HostnameGenerator::new, List.of("hostname"))
                .withSchemaProjections(projections(stringFormat("internet.hostname",
                                                                (provider, config) -> provider.generate(),
                                                                "hostname",
                                                                "hostname"))),
            descriptor("internet", URLGenerator.class, URLGenerator::new, List.of())
                .withSchemaProjections(projections(stringFormat("internet",
                                                                (provider, config) -> provider.generate(),
                                                                "uri"))),
            descriptor("commerce.product_info", ProductInfoGenerator.class, ProductInfoGenerator::new,
                       List.of("product_info"))
                .withSchemaProjections(projections(record("commerce.product_info",
                                                          (provider, config) -> provider.generate(),
                                                          ProductInfo.class,
                                                          List.of("product_info"),
                                                          Set.of()))),
            descriptor("commerce.order_info", OrderInfoGenerator.class, OrderInfoGenerator::new,
                       List.of("order_info"))
                .withSchemaProjections(projections(record("commerce.order_info",
                                                          (provider, config) -> provider.generate(),
                                                          OrderInfo.class,
                                                          List.of("order_info"),
                                                          Set.of()))),
            descriptor("commerce.shipment_info", ShipmentInfoGenerator.class, ShipmentInfoGenerator::new,
                       List.of("shipment_info"))
                .withSchemaProjections(projections(record("commerce.shipment_info",
                                                          (provider, config) -> provider.generate(),
                                                          ShipmentInfo.class,
                                                          List.of("shipment_info"),
                                                          Set.of("deliveredOn")))),
            descriptor("finance.money", MoneyGenerator.class, MoneyGenerator::new, List.of("money"))
                .withSchemaProjections(projections(string("finance.price",
                                                          (provider, config) -> provider.generatePrice(config.getLocale()),
                                                          "finance.money",
                                                          "money"),
                                                  string("finance",
                                                         (provider, config) -> provider.generatePrice(config.getLocale())))),
            descriptor("finance.currency", CurrencyGenerator.class, CurrencyGenerator::new,
                       List.of("currency"), "currency")
                .withSchemaProjections(projections(string("finance.currency_iso_code",
                                                          (provider, config) -> provider.generateCurrencyIsoCode(config.getLocale()),
                                                          "finance.currency",
                                                          "currency"))),
            descriptor("finance.bank_info", BankInfoGenerator.class, BankInfoGenerator::new,
                       List.of("bank_info"))
                .withSafetyMetadata(BANKING_SAFETY)
                .withSchemaProjections(projections(ProviderCatalog.<BankInfoGenerator>record("finance.bank_info",
                                                          (provider, config) -> provider.generate(),
                                                          BankInfo.class,
                                                          List.of("bank_info"),
                                                          Set.of())
                                           .withSafetyMetadata(BANKING_SAFETY))),
            descriptor("finance.credit_card_info", CreditCardInfoGenerator.class, CreditCardInfoGenerator::new,
                       List.of("credit_card_info"))
                .withSafetyMetadata(PAYMENT_CARD_SAFETY)
                .withSchemaProjections(projections(ProviderCatalog.<CreditCardInfoGenerator>record("finance.credit_card_info",
                                                          (provider, config) -> provider.generate(),
                                                          CreditCardInfo.class,
                                                          List.of("credit_card_info"),
                                                          Set.of())
                                           .withSafetyMetadata(PAYMENT_CARD_SAFETY))),
            descriptor("finance.invoice_info", InvoiceInfoGenerator.class, InvoiceInfoGenerator::new,
                       List.of("invoice_info"))
                .withSchemaProjections(projections(record("finance.invoice_info",
                                                          (provider, config) -> provider.generate(),
                                                          InvoiceInfo.class,
                                                          List.of("invoice_info"),
                                                          Set.of()))),
            descriptor("finance.payment_info", PaymentInfoGenerator.class, PaymentInfoGenerator::new,
                       List.of("payment_info"))
                .withSchemaProjections(projections(record("finance.payment_info",
                                                          (provider, config) -> provider.generate(),
                                                          PaymentInfo.class,
                                                          List.of("payment_info"),
                                                          Set.of("settledOn")))),
            descriptor("finance", MoneyGenerator.class, MoneyGenerator::new, List.of()),
            descriptor("datetime.date", DateGenerator.class, DateGenerator::new, List.of("date"))
                .withSchemaProjections(projections(stringFormat("datetime.date",
                                                                (provider, config) -> provider.generateString(),
                                                                "date",
                                                                "date"),
                                                  integer("datetime.timestamp",
                                                          (provider, config) -> provider.generateUnixTime()),
                                                  stringFormat("datetime",
                                                               (provider, config) -> provider.generateString(),
                                                               "date"))),
            descriptor("datetime.time", TimeGenerator.class, TimeGenerator::new, List.of("time"))
                .withSchemaProjections(projections(stringFormat("datetime.time",
                                                                (provider, config) -> provider.generateString(),
                                                                "time",
                                                                "time"))),
            descriptor("datetime", DateGenerator.class, DateGenerator::new, List.of()),
            descriptor("text.word", WordGenerator.class, WordGenerator::new, List.of("word"))
                .withSchemaProjections(projections(string("text.word", (provider, config) -> provider.generateWord(), "word"))),
            descriptor("text.sentence", SentenceGenerator.class, SentenceGenerator::new, List.of("sentence"))
                .withSchemaProjections(projections(string("text.sentence", (provider, config) -> provider.generateSentence(), "sentence"))),
            descriptor("text.paragraph", ParagraphGenerator.class, ParagraphGenerator::new, List.of("paragraph"))
                .withSchemaProjections(projections(string("text.paragraph", (provider, config) -> provider.generate(), "paragraph"))),
            descriptor("text.format", TextFormatProvider.class, TextFormatProvider::new, List.of("format"))
                .withSchemaProjections(projections(string("text.format", (provider, config) -> provider.template("??-####"), "format"))),
            descriptor("text", WordGenerator.class, WordGenerator::new, List.of())
                .withSchemaProjections(projections(string("text", (provider, config) -> provider.generateWord()))),
            descriptor("code.uuid", UUIDGenerator.class, UUIDGenerator::new,
                       List.of("uuid", "identifier"), "uuid")
                .withSchemaProjections(projections(stringFormat("code.uuid4",
                                                                (provider, config) -> provider.generateV4().toString(),
                                                                "uuid",
                                                                "code.uuid",
                                                                "uuid",
                                                                "identifier"))),
            descriptor("code", UUIDGenerator.class, UUIDGenerator::new, List.of())
        );
    }

    private static List<ProviderDescriptor<?>> buildSchemaOnlyBuiltIns() {
        return List.of(
            descriptor("finance.credit_card", CreditCardGenerator.class, CreditCardGenerator::new, List.of())
                .withSafetyMetadata(PAYMENT_CARD_SAFETY)
                .withSchemaProjections(projections(ProviderCatalog.<CreditCardGenerator>string("finance.credit_card_number",
                                                          (provider, config) -> provider.generateNumber())
                                           .withSafetyMetadata(PAYMENT_CARD_SAFETY),
                                                  ProviderCatalog.<CreditCardGenerator>string("finance.cvv",
                                                                                                (provider, config) -> provider.generateCvv()))),
            descriptor("datetime.timezone", TimezoneGenerator.class, TimezoneGenerator::new, List.of())
                .withSchemaProjections(projections(string("datetime.timezone",
                                                          (provider, config) -> provider.generateTimezone()))),
            descriptor("code.ean13", EanGenerator.class, EanGenerator::new, List.of())
                .withSchemaProjections(projections(string("code.ean13",
                                                          (provider, config) -> provider.generateEan13()))),
            descriptor("code.upc", UpcGenerator.class, UpcGenerator::new, List.of())
                .withSchemaProjections(projections(string("code.upc", (provider, config) -> provider.generate()))),
            descriptor("code.isbn13", IsbnGenerator.class,
                       config -> new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, config),
                       List.of())
                .withSchemaProjections(projections(string("code.isbn13", (provider, config) -> provider.generate()))),
            descriptor("code.sha256", HashGenerator.class, HashGenerator::new, List.of())
                .withSchemaProjections(projections(string("code.sha256", (provider, config) -> provider.generateSha256())))
        );
    }
}
