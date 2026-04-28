/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.commerce.OrderInfo;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfo;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfo;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.finance.BankInfo;
import io.github.frikit.krandom.generator.finance.BankInfoGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardInfo;
import io.github.frikit.krandom.generator.finance.CreditCardInfoGenerator;
import io.github.frikit.krandom.generator.datetime.TimezoneGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.InvoiceInfo;
import io.github.frikit.krandom.generator.finance.InvoiceInfoGenerator;
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
import io.github.frikit.krandom.generator.provider.ConflictPolicy;
import io.github.frikit.krandom.generator.provider.ProviderFactory;
import io.github.frikit.krandom.generator.provider.TextFormatProvider;
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
import io.github.frikit.krandom.generator.user.ContactInfo;
import io.github.frikit.krandom.generator.user.ContactInfoGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobInfo;
import io.github.frikit.krandom.generator.user.JobInfoGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PersonInfo;
import io.github.frikit.krandom.generator.user.PersonInfoGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Resolves string field references to concrete value providers.
 */
public final class FieldLookup {

    private final GeneratorConfig                  config;
    private final Map<String, SchemaValueProvider> providers = new LinkedHashMap<>();
    private final Map<String, String>              aliases   = new LinkedHashMap<>();

    /**
     * Creates a lookup with generators initialized from the provided config.
     *
     * @param config generator config used for locale/seed propagation
     */
    public FieldLookup(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        registerBuiltIns();
    }

    private static String normalize(String reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        String key = reference.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
        return key;
    }

    /**
     * Registers a schema reference using {@link ConflictPolicy#FAIL}.
     *
     * @param reference schema token reference
     * @param provider  value provider
     */
    public void register(String reference, SchemaValueProvider provider) {
        register(reference, provider, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference.
     *
     * @param reference schema token reference
     * @param provider  value provider
     * @param policy    conflict policy
     */
    public void register(String reference, SchemaValueProvider provider, ConflictPolicy policy) {
        String key = normalize(reference);
        SchemaValueProvider value = Objects.requireNonNull(provider, "provider must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if ((providers.containsKey(key) || aliases.containsKey(key)) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field reference already registered: " + key);
        }
        aliases.remove(key);
        providers.put(key, value);
    }

    /**
     * Registers a schema reference backed by a provider factory.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor) {
        registerProvider(reference, factory, providerType, valueExtractor, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference backed by a provider factory.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param policy         conflict policy
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     ConflictPolicy policy) {
        registerProvider(reference, factory, providerType, valueExtractor, JsonSchemaSupport.any(), policy);
    }

    /**
     * Registers a schema reference backed by a provider factory with explicit JSON Schema metadata.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param jsonSchema     JSON Schema fragment for extracted values
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     Map<String, ?> jsonSchema) {
        registerProvider(reference, factory, providerType, valueExtractor, jsonSchema, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference backed by a provider factory with explicit JSON Schema metadata.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param jsonSchema     JSON Schema fragment for extracted values
     * @param policy         conflict policy
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     Map<String, ?> jsonSchema,
                                     ConflictPolicy policy) {
        String key = normalize(reference);
        ProviderFactory providerFactory = Objects.requireNonNull(factory, "factory must not be null");
        Class<T> expectedType = Objects.requireNonNull(providerType, "providerType must not be null");
        Function<? super T, ?> extractor = Objects.requireNonNull(valueExtractor, "valueExtractor must not be null");
        Map<String, ?> schema = Objects.requireNonNull(jsonSchema, "jsonSchema must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if ((providers.containsKey(key) || aliases.containsKey(key)) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field reference already registered: " + key);
        }
        Object provider = Objects.requireNonNull(providerFactory.create(config), "provider factory must not return null");
        if (!expectedType.isInstance(provider)) {
            throw new IllegalArgumentException(
                "Provider for reference '" + reference + "' is "
                + provider.getClass().getName() + ", not " + expectedType.getName());
        }
        T typedProvider = expectedType.cast(provider);
        register(key, ctx -> extractor.apply(typedProvider), schema, conflictPolicy);
    }

    /**
     * Registers a schema reference alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias           alias token
     * @param targetReference canonical target reference
     */
    public void registerAlias(String alias, String targetReference) {
        registerAlias(alias, targetReference, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference alias.
     *
     * @param alias           alias token
     * @param targetReference canonical target reference
     * @param policy          conflict policy
     */
    public void registerAlias(String alias, String targetReference, ConflictPolicy policy) {
        String aliasKey = normalize(alias);
        String targetKey = normalize(targetReference);
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (!providers.containsKey(targetKey)) {
            throw new IllegalArgumentException("Target field reference is not registered: " + targetKey);
        }
        if (providers.containsKey(aliasKey) && !aliasKey.equals(targetKey)) {
            throw new IllegalArgumentException("Alias conflicts with canonical field reference: " + aliasKey);
        }
        if (aliases.containsKey(aliasKey) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field alias already registered: " + aliasKey);
        }
        aliases.put(aliasKey, targetKey);
    }

    /**
     * Checks whether a canonical reference or alias is registered.
     *
     * @param reference reference or alias
     * @return true if supported
     */
    public boolean has(String reference) {
        String key = normalize(reference);
        return providers.containsKey(key) || aliases.containsKey(key);
    }

    /**
     * Resolves a string reference to a provider.
     *
     * @param reference field reference
     * @return resolved value provider
     */
    public SchemaValueProvider resolve(String reference) {
        String canonical = resolveName(reference);
        SchemaValueProvider provider = providers.get(canonical);
        if (provider == null) {
            throw new IllegalArgumentException(
                "Unknown field reference '" + reference + "'. Supported references: " + supportedReferences());
        }
        return provider;
    }

    /**
     * Returns supported string references.
     *
     * @return immutable reference set
     */
    public Set<String> supportedReferences() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    /**
     * Returns the current alias mapping.
     *
     * @return immutable alias map (alias -&gt; canonical reference)
     */
    public Map<String, String> aliases() {
        return Collections.unmodifiableMap(aliases);
    }

    /**
     * Returns the generator config used by this lookup.
     *
     * @return generator config
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    private String resolveName(String reference) {
        String key = normalize(reference);
        if (providers.containsKey(key)) {
            return key;
        }
        String canonical = aliases.get(key);
        if (canonical == null) {
            throw new IllegalArgumentException(
                "Unknown field reference '" + reference + "'. Supported references: " + supportedReferences()
                + ", aliases: " + aliases.keySet());
        }
        return canonical;
    }

    private void registerString(String reference, SchemaValueProvider provider) {
        register(reference, provider, JsonSchemaSupport.string(), ConflictPolicy.REPLACE);
    }

    private void registerString(String reference, SchemaValueProvider provider, String format) {
        register(reference, provider, JsonSchemaSupport.stringFormat(format), ConflictPolicy.REPLACE);
    }

    private void registerInteger(String reference, SchemaValueProvider provider) {
        register(reference, provider, JsonSchemaSupport.integer(), ConflictPolicy.REPLACE);
    }

    private void registerRecord(String reference, SchemaValueProvider provider, Class<?> recordType) {
        register(reference, provider, JsonSchemaSupport.record(recordType), ConflictPolicy.REPLACE);
    }

    private void registerRecord(String reference,
                                SchemaValueProvider provider,
                                Class<?> recordType,
                                String... nullableComponents) {
        register(reference, provider, JsonSchemaSupport.record(recordType, Set.of(nullableComponents)), ConflictPolicy.REPLACE);
    }

    private void register(String reference,
                          SchemaValueProvider provider,
                          Map<String, ?> jsonSchema,
                          ConflictPolicy policy) {
        register(reference, SchemaValueProvider.withJsonSchema(provider, jsonSchema), policy);
    }

    private void registerBuiltIns() {
        Locale locale = config.getLocale();

        FullNameGenerator fullName = new FullNameGenerator(config);
        FirstNameGenerator firstName = new FirstNameGenerator(config);
        LastNameGenerator lastName = new LastNameGenerator(config);
        EmailGenerator email = new EmailGenerator(config);
        UsernameGenerator username = new UsernameGenerator(config);
        ContactInfoGenerator contactInfo = new ContactInfoGenerator(config);
        PersonInfoGenerator personInfo = new PersonInfoGenerator(config);
        JobInfoGenerator jobInfo = new JobInfoGenerator(config);
        CompanyNameGenerator companyName = new CompanyNameGenerator(config);
        CompanyEmailGenerator companyEmail = new CompanyEmailGenerator(config);
        CompanyUrlGenerator companyUrl = new CompanyUrlGenerator(config);
        CompanyBuzzwordGenerator companyBuzzword = new CompanyBuzzwordGenerator(config);
        CompanyCatchPhraseGenerator companyCatchPhrase = new CompanyCatchPhraseGenerator(config);
        IndustryGenerator industry = new IndustryGenerator(config);
        CompanyInfoGenerator companyInfo = new CompanyInfoGenerator(config);
        PasswordGenerator password = new PasswordGenerator(config);
        AddressInfoGenerator addressInfo = new AddressInfoGenerator(config);
        CityGenerator city = new CityGenerator(config);
        StateGenerator state = new StateGenerator(config);
        StreetAddressGenerator street = new StreetAddressGenerator(config);
        PostalCodeGenerator postalCode = new PostalCodeGenerator(config);
        CountryGenerator country = new CountryGenerator(config);
        PhoneNumberGenerator phoneNumber = new PhoneNumberGenerator(config);
        DomainGenerator domain = new DomainGenerator(config);
        HostnameGenerator hostname = new HostnameGenerator(config);
        URLGenerator url = new URLGenerator(config);
        ProductInfoGenerator productInfo = new ProductInfoGenerator(config);
        OrderInfoGenerator orderInfo = new OrderInfoGenerator(config);
        ShipmentInfoGenerator shipmentInfo = new ShipmentInfoGenerator(config);
        CurrencyGenerator currency = new CurrencyGenerator(config);
        MoneyGenerator money = new MoneyGenerator(config);
        CreditCardGenerator card = new CreditCardGenerator(config);
        BankInfoGenerator bankInfo = new BankInfoGenerator(config);
        CreditCardInfoGenerator creditCardInfo = new CreditCardInfoGenerator(config);
        InvoiceInfoGenerator invoiceInfo = new InvoiceInfoGenerator(config);
        PaymentInfoGenerator paymentInfo = new PaymentInfoGenerator(config);
        DateGenerator date = new DateGenerator(config);
        TimeGenerator time = new TimeGenerator(config);
        TimezoneGenerator timezone = new TimezoneGenerator(config);
        WordGenerator word = new WordGenerator(config);
        SentenceGenerator sentence = new SentenceGenerator(config);
        ParagraphGenerator paragraph = new ParagraphGenerator(config);
        TextFormatProvider textFormat = new TextFormatProvider(config);
        UUIDGenerator uuid = new UUIDGenerator(config);
        EanGenerator ean = new EanGenerator(config);
        UpcGenerator upc = new UpcGenerator(config);
        IsbnGenerator isbn = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, config);
        HashGenerator hash = new HashGenerator(config);

        registerString("person.full_name", ctx -> fullName.generate());
        registerString("person.first_name", ctx -> firstName.generate());
        registerString("person.last_name", ctx -> lastName.generate());
        registerString("person.email", ctx -> email.generate(), "email");
        registerString("person.username", ctx -> username.generate());
        registerRecord("person.contact_info", ctx -> contactInfo.generate(), ContactInfo.class);
        registerRecord("person.person_info", ctx -> personInfo.generate(), PersonInfo.class);
        registerRecord("person.job_info", ctx -> jobInfo.generate(), JobInfo.class);
        registerString("person", ctx -> fullName.generate());
        registerString("company.name", ctx -> companyName.generate());
        registerString("company.email", ctx -> companyEmail.generate(), "email");
        registerString("company.url", ctx -> companyUrl.generate(), "uri");
        registerString("company.buzzword", ctx -> companyBuzzword.generate());
        registerString("company.catch_phrase", ctx -> companyCatchPhrase.generate());
        registerString("company.industry", ctx -> industry.generate());
        registerRecord("company.info", ctx -> companyInfo.generate(), CompanyInfo.class);
        registerString("security.password", ctx -> password.generate());
        registerRecord("address.address_info", ctx -> addressInfo.generate(), AddressInfo.class);
        registerString("address.city", ctx -> city.generate());
        registerString("address.state", ctx -> state.generate());
        registerString("address.street", ctx -> street.generate());
        registerString("address.street_address", ctx -> street.generate());
        registerString("address.postal_code", ctx -> postalCode.generate());
        registerString("address.country", ctx -> country.generate());
        registerString("address.phone_number", ctx -> phoneNumber.generate());
        registerString("address", ctx -> street.generate());
        registerString("internet.domain", ctx -> domain.generate());
        registerString("internet.hostname", ctx -> hostname.generate(), "hostname");
        registerString("internet.url", ctx -> url.generate(), "uri");
        registerString("internet", ctx -> url.generate(), "uri");
        registerRecord("commerce.product_info", ctx -> productInfo.generate(), ProductInfo.class);
        registerRecord("commerce.order_info", ctx -> orderInfo.generate(), OrderInfo.class);
        registerRecord("commerce.shipment_info", ctx -> shipmentInfo.generate(), ShipmentInfo.class, "deliveredOn");
        registerString("finance.currency_iso_code", ctx -> currency.generateCurrencyIsoCode(locale));
        registerString("finance.price", ctx -> money.generatePrice(locale));
        registerRecord("finance.bank_info", ctx -> bankInfo.generate(), BankInfo.class);
        registerString("finance.credit_card_number", ctx -> card.generateNumber());
        registerRecord("finance.credit_card_info", ctx -> creditCardInfo.generate(), CreditCardInfo.class);
        registerString("finance.cvv", ctx -> card.generateCvv());
        registerRecord("finance.invoice_info", ctx -> invoiceInfo.generate(), InvoiceInfo.class);
        registerRecord("finance.payment_info", ctx -> paymentInfo.generate(), PaymentInfo.class, "settledOn");
        registerString("finance", ctx -> money.generatePrice(locale));
        registerString("datetime.date", ctx -> date.generateString(), "date");
        registerString("datetime.time", ctx -> time.generateString(), "time");
        registerInteger("datetime.timestamp", ctx -> date.generateUnixTime());
        registerString("datetime.timezone", ctx -> timezone.generateTimezone());
        registerString("datetime", ctx -> date.generateString(), "date");
        registerString("text.word", ctx -> word.generateWord());
        registerString("text.sentence", ctx -> sentence.generateSentence());
        registerString("text.paragraph", ctx -> paragraph.generate());
        registerString("text.format", ctx -> textFormat.template("??-####"));
        registerString("text", ctx -> word.generateWord());
        registerString("code.uuid4", ctx -> uuid.generateV4().toString(), "uuid");
        registerString("code.ean13", ctx -> ean.generateEan13());
        registerString("code.upc", ctx -> upc.generate());
        registerString("code.isbn13", ctx -> isbn.generate());
        registerString("code.sha256", ctx -> hash.generateSha256());

        registerAlias("name", "person.full_name", ConflictPolicy.REPLACE);
        registerAlias("full_name", "person.full_name", ConflictPolicy.REPLACE);
        registerAlias("fullname", "person.full_name", ConflictPolicy.REPLACE);
        registerAlias("first_name", "person.first_name", ConflictPolicy.REPLACE);
        registerAlias("firstname", "person.first_name", ConflictPolicy.REPLACE);
        registerAlias("last_name", "person.last_name", ConflictPolicy.REPLACE);
        registerAlias("lastname", "person.last_name", ConflictPolicy.REPLACE);
        registerAlias("email", "person.email", ConflictPolicy.REPLACE);
        registerAlias("username", "person.username", ConflictPolicy.REPLACE);
        registerAlias("contact_info", "person.contact_info", ConflictPolicy.REPLACE);
        registerAlias("person_info", "person.person_info", ConflictPolicy.REPLACE);
        registerAlias("job_info", "person.job_info", ConflictPolicy.REPLACE);
        registerAlias("company_name", "company.name", ConflictPolicy.REPLACE);
        registerAlias("company_email", "company.email", ConflictPolicy.REPLACE);
        registerAlias("company_url", "company.url", ConflictPolicy.REPLACE);
        registerAlias("company_buzzword", "company.buzzword", ConflictPolicy.REPLACE);
        registerAlias("company_catch_phrase", "company.catch_phrase", ConflictPolicy.REPLACE);
        registerAlias("company_industry", "company.industry", ConflictPolicy.REPLACE);
        registerAlias("company_info", "company.info", ConflictPolicy.REPLACE);
        registerAlias("password", "security.password", ConflictPolicy.REPLACE);
        registerAlias("address_info", "address.address_info", ConflictPolicy.REPLACE);
        registerAlias("location", "address.street_address", ConflictPolicy.REPLACE);
        registerAlias("street_address", "address.street_address", ConflictPolicy.REPLACE);
        registerAlias("city", "address.city", ConflictPolicy.REPLACE);
        registerAlias("state", "address.state", ConflictPolicy.REPLACE);
        registerAlias("postal_code", "address.postal_code", ConflictPolicy.REPLACE);
        registerAlias("country", "address.country", ConflictPolicy.REPLACE);
        registerAlias("phone_number", "address.phone_number", ConflictPolicy.REPLACE);
        registerAlias("network", "internet.url", ConflictPolicy.REPLACE);
        registerAlias("url", "internet.url", ConflictPolicy.REPLACE);
        registerAlias("domain", "internet.domain", ConflictPolicy.REPLACE);
        registerAlias("hostname", "internet.hostname", ConflictPolicy.REPLACE);
        registerAlias("finance.currency", "finance.currency_iso_code", ConflictPolicy.REPLACE);
        registerAlias("finance.money", "finance.price", ConflictPolicy.REPLACE);
        registerAlias("money", "finance.price", ConflictPolicy.REPLACE);
        registerAlias("currency", "finance.currency_iso_code", ConflictPolicy.REPLACE);
        registerAlias("bank_info", "finance.bank_info", ConflictPolicy.REPLACE);
        registerAlias("credit_card_info", "finance.credit_card_info", ConflictPolicy.REPLACE);
        registerAlias("product_info", "commerce.product_info", ConflictPolicy.REPLACE);
        registerAlias("order_info", "commerce.order_info", ConflictPolicy.REPLACE);
        registerAlias("shipment_info", "commerce.shipment_info", ConflictPolicy.REPLACE);
        registerAlias("invoice_info", "finance.invoice_info", ConflictPolicy.REPLACE);
        registerAlias("payment_info", "finance.payment_info", ConflictPolicy.REPLACE);
        registerAlias("date", "datetime.date", ConflictPolicy.REPLACE);
        registerAlias("time", "datetime.time", ConflictPolicy.REPLACE);
        registerAlias("word", "text.word", ConflictPolicy.REPLACE);
        registerAlias("sentence", "text.sentence", ConflictPolicy.REPLACE);
        registerAlias("paragraph", "text.paragraph", ConflictPolicy.REPLACE);
        registerAlias("format", "text.format", ConflictPolicy.REPLACE);
        registerAlias("code.uuid", "code.uuid4", ConflictPolicy.REPLACE);
        registerAlias("uuid", "code.uuid4", ConflictPolicy.REPLACE);
        registerAlias("identifier", "code.uuid4", ConflictPolicy.REPLACE);
    }
}
