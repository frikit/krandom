/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.GeneratorProfile;
import org.github.krandom.generator.commerce.OrderInfoGenerator;
import org.github.krandom.generator.commerce.ProductInfoGenerator;
import org.github.krandom.generator.commerce.ShipmentInfoGenerator;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.TimeGenerator;
import org.github.krandom.generator.finance.BankInfoGenerator;
import org.github.krandom.generator.finance.CreditCardInfoGenerator;
import org.github.krandom.generator.finance.InvoiceInfoGenerator;
import org.github.krandom.generator.finance.PaymentInfoGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.finance.CurrencyGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.location.AddressInfoGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.PostalCodeGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.DomainGenerator;
import org.github.krandom.generator.network.HostnameGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.text.ParagraphGenerator;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.CompanyBuzzwordGenerator;
import org.github.krandom.generator.user.CompanyCatchPhraseGenerator;
import org.github.krandom.generator.user.CompanyEmailGenerator;
import org.github.krandom.generator.user.CompanyInfoGenerator;
import org.github.krandom.generator.user.CompanyNameGenerator;
import org.github.krandom.generator.user.CompanyUrlGenerator;
import org.github.krandom.generator.user.ContactInfoGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.IndustryGenerator;
import org.github.krandom.generator.user.JobInfoGenerator;
import org.github.krandom.generator.user.LastNameGenerator;
import org.github.krandom.generator.user.PasswordGenerator;
import org.github.krandom.generator.user.PersonInfoGenerator;
import org.github.krandom.generator.user.UsernameGenerator;

import java.util.function.BiPredicate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mimesis-style generic provider hub with alias-based lookup and runtime registration.
 */
public final class ProviderHub {

    private final GeneratorConfig              config;
    private final GeneratorProfile             profile;
    private final Map<String, ProviderFactory> providers = new LinkedHashMap<>();
    private final Map<String, String>          aliases   = new LinkedHashMap<>();

    /**
     * Creates provider hub with default configuration.
     */
    public ProviderHub() {
        this(GeneratorConfig.defaults(), GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub for a specific locale.
     *
     * @param locale locale for locale-aware providers
     */
    public ProviderHub(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build(), GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub with explicit generator config.
     *
     * @param config generator config propagated to providers
     */
    public ProviderHub(GeneratorConfig config) {
        this(config, GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub from a named configuration profile.
     *
     * @param profile profile template used to construct default config
     */
    public ProviderHub(GeneratorProfile profile) {
        this(Objects.requireNonNull(profile, "profile must not be null").createConfig(), profile);
    }

    /**
     * Creates provider hub for locale using a named profile.
     *
     * @param locale  locale for locale-aware providers
     * @param profile profile template used to construct config
     */
    public ProviderHub(Locale locale, GeneratorProfile profile) {
        this(Objects.requireNonNull(profile, "profile must not be null")
                    .createConfig(Objects.requireNonNull(locale, "locale must not be null")),
             profile);
    }

    /**
     * Creates provider hub with explicit config and profile metadata.
     *
     * @param config  generator config propagated to providers
     * @param profile profile metadata for profile-aware extensions
     */
    public ProviderHub(GeneratorConfig config, GeneratorProfile profile) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        registerBuiltIns();
    }

    private static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return key;
    }

    /**
     * Resolves provider by name or alias.
     *
     * @param name provider name or alias
     * @return provider instance
     */
    public Object get(String name) {
        String canonical = resolveName(name);
        ProviderFactory factory = providers.get(canonical);
        return factory.create(config);
    }

    /**
     * Resolves typed provider by name or alias.
     *
     * @param name provider name or alias
     * @param type expected type
     * @param <T>  expected provider type
     * @return typed provider instance
     */
    public <T> T get(String name, Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        Object provider = get(name);
        if (!type.isInstance(provider)) {
            throw new IllegalArgumentException("Provider '" + name + "' is "
                                               + provider.getClass().getName() + ", not " + type.getName());
        }
        return type.cast(provider);
    }

    /**
     * Checks whether provider name or alias is supported.
     *
     * @param name provider name or alias
     * @return true if supported
     */
    public boolean has(String name) {
        String key = normalize(name);
        return providers.containsKey(key) || aliases.containsKey(key);
    }

    /**
     * Registers provider factory using {@link ConflictPolicy#FAIL}.
     *
     * @param name    canonical provider name
     * @param factory provider factory
     */
    public void register(String name, ProviderFactory factory) {
        register(name, factory, ConflictPolicy.FAIL);
    }

    /**
     * Registers profile-aware provider factory using {@link ConflictPolicy#FAIL}.
     *
     * @param name    canonical provider name
     * @param factory profile-aware provider factory
     */
    public void register(String name, ProfiledProviderFactory factory) {
        register(name, factory, ConflictPolicy.FAIL);
    }

    /**
     * Registers provider factory.
     *
     * @param name    canonical provider name
     * @param factory provider factory
     * @param policy  conflict policy
     */
    public void register(String name, ProviderFactory factory, ConflictPolicy policy) {
        String key = normalize(name);
        ProviderFactory value = Objects.requireNonNull(factory, "factory must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (providers.containsKey(key) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Provider already registered: " + key);
        }
        providers.put(key, value);
    }

    /**
     * Registers profile-aware provider factory.
     *
     * @param name    canonical provider name
     * @param factory profile-aware provider factory
     * @param policy  conflict policy
     */
    public void register(String name, ProfiledProviderFactory factory, ConflictPolicy policy) {
        ProfiledProviderFactory profiledFactory = Objects.requireNonNull(factory, "factory must not be null");
        register(name, cfg -> profiledFactory.create(profile, cfg), policy);
    }

    /**
     * Registers alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias      alias name
     * @param targetName canonical provider name
     */
    public void registerAlias(String alias, String targetName) {
        registerAlias(alias, targetName, ConflictPolicy.FAIL);
    }

    /**
     * Registers alias.
     *
     * @param alias      alias name
     * @param targetName canonical provider name
     * @param policy     conflict policy
     */
    public void registerAlias(String alias, String targetName, ConflictPolicy policy) {
        String aliasKey = normalize(alias);
        String targetKey = normalize(targetName);
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (!providers.containsKey(targetKey)) {
            throw new IllegalArgumentException("Target provider is not registered: " + targetKey);
        }
        if (providers.containsKey(aliasKey) && !aliasKey.equals(targetKey)) {
            throw new IllegalArgumentException("Alias conflicts with canonical provider name: " + aliasKey);
        }
        if (aliases.containsKey(aliasKey) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Alias already registered: " + aliasKey);
        }
        aliases.put(aliasKey, targetKey);
    }

    /**
     * Returns canonical provider names.
     *
     * @return immutable canonical provider names
     */
    public Set<String> providerNames() {
        return Set.copyOf(providers.keySet());
    }

    /**
     * Returns alias mapping.
     *
     * @return immutable alias map (alias -> canonical name)
     */
    public Map<String, String> aliases() {
        return Map.copyOf(aliases);
    }

    /**
     * Returns generator config used by this hub.
     *
     * @return generator config
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    /**
     * Returns profile metadata associated with this hub.
     */
    public GeneratorProfile getProfile() {
        return profile;
    }

    /**
     * Wraps a generator so repeated calls return unique values.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"));
    }

    /**
     * Wraps a generator so repeated calls return unique values, with bounded retry attempts.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source, int maxAttempts) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"), maxAttempts);
    }

    /**
     * Wraps a generator so repeated calls return comparator-distinct values.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source, BiPredicate<T, T> comparator) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"),
                                     Objects.requireNonNull(comparator, "comparator must not be null"));
    }

    private String resolveName(String name) {
        String key = normalize(name);
        if (providers.containsKey(key)) {
            return key;
        }
        String canonical = aliases.get(key);
        if (canonical == null) {
            throw new IllegalArgumentException("Unknown provider '" + name + "'. "
                                               + "Known providers: " + providerNames() + ", aliases: " + aliases.keySet());
        }
        return canonical;
    }

    private void registerBuiltIns() {
        register("person.full_name", cfg -> new FullNameGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.first_name", cfg -> new FirstNameGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.last_name", cfg -> new LastNameGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.email", cfg -> new EmailGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.username", cfg -> new UsernameGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.contact_info", cfg -> new ContactInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.person_info", cfg -> new PersonInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("person.job_info", cfg -> new JobInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("person", cfg -> new FullNameGenerator(cfg), ConflictPolicy.REPLACE);

        register("company.name", cfg -> new CompanyNameGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.email", cfg -> new CompanyEmailGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.url", cfg -> new CompanyUrlGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.buzzword", cfg -> new CompanyBuzzwordGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.catch_phrase", cfg -> new CompanyCatchPhraseGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.industry", cfg -> new IndustryGenerator(cfg), ConflictPolicy.REPLACE);
        register("company.info", cfg -> new CompanyInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("security.password", cfg -> new PasswordGenerator(cfg), ConflictPolicy.REPLACE);

        register("address.address_info", cfg -> new AddressInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.street_address", cfg -> new StreetAddressGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.city", cfg -> new CityGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.state", cfg -> new StateGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.postal_code", cfg -> new PostalCodeGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.country", cfg -> new CountryGenerator(cfg), ConflictPolicy.REPLACE);
        register("address.phone_number", cfg -> new PhoneNumberGenerator(cfg), ConflictPolicy.REPLACE);
        register("address", cfg -> new StreetAddressGenerator(cfg), ConflictPolicy.REPLACE);

        register("internet.url", cfg -> new URLGenerator(cfg), ConflictPolicy.REPLACE);
        register("internet.domain", cfg -> new DomainGenerator(cfg), ConflictPolicy.REPLACE);
        register("internet.hostname", cfg -> new HostnameGenerator(cfg), ConflictPolicy.REPLACE);
        register("internet", cfg -> new URLGenerator(cfg), ConflictPolicy.REPLACE);

        register("commerce.product_info", cfg -> new ProductInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("commerce.order_info", cfg -> new OrderInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("commerce.shipment_info", cfg -> new ShipmentInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.money", cfg -> new MoneyGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.currency", cfg -> new CurrencyGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.bank_info", cfg -> new BankInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.credit_card_info", cfg -> new CreditCardInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.invoice_info", cfg -> new InvoiceInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance.payment_info", cfg -> new PaymentInfoGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance", cfg -> new MoneyGenerator(cfg), ConflictPolicy.REPLACE);

        register("datetime.date", cfg -> new DateGenerator(cfg), ConflictPolicy.REPLACE);
        register("datetime.time", cfg -> new TimeGenerator(cfg), ConflictPolicy.REPLACE);
        register("datetime", cfg -> new DateGenerator(cfg), ConflictPolicy.REPLACE);

        register("text.word", cfg -> new WordGenerator(cfg), ConflictPolicy.REPLACE);
        register("text.sentence", cfg -> new SentenceGenerator(cfg), ConflictPolicy.REPLACE);
        register("text.paragraph", cfg -> new ParagraphGenerator(cfg), ConflictPolicy.REPLACE);
        register("text.format", cfg -> new TextFormatProvider(cfg), ConflictPolicy.REPLACE);
        register("text", cfg -> new WordGenerator(cfg), ConflictPolicy.REPLACE);

        register("code.uuid", cfg -> new UUIDGenerator(cfg), ConflictPolicy.REPLACE);
        register("code", cfg -> new UUIDGenerator(cfg), ConflictPolicy.REPLACE);

        registerAlias("name", "person.full_name", ConflictPolicy.REPLACE);
        registerAlias("full_name", "person.full_name", ConflictPolicy.REPLACE);
        registerAlias("first_name", "person.first_name", ConflictPolicy.REPLACE);
        registerAlias("last_name", "person.last_name", ConflictPolicy.REPLACE);
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
        registerAlias("money", "finance.money", ConflictPolicy.REPLACE);
        registerAlias("currency", "finance.currency", ConflictPolicy.REPLACE);
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
        registerAlias("uuid", "code.uuid", ConflictPolicy.REPLACE);
        registerAlias("identifier", "code.uuid", ConflictPolicy.REPLACE);
    }
}
