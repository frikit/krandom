/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.TimezoneGenerator;
import org.github.krandom.generator.finance.CreditCardGenerator;
import org.github.krandom.generator.finance.CurrencyGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.identifier.EanGenerator;
import org.github.krandom.generator.identifier.HashGenerator;
import org.github.krandom.generator.identifier.IsbnGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.identifier.UpcGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PostalCodeGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.DomainGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.provider.ConflictPolicy;
import org.github.krandom.generator.provider.ProviderFactory;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;

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
        ProviderFactory providerFactory = Objects.requireNonNull(factory, "factory must not be null");
        Class<T> expectedType = Objects.requireNonNull(providerType, "providerType must not be null");
        Function<? super T, ?> extractor = Objects.requireNonNull(valueExtractor, "valueExtractor must not be null");
        register(reference, ctx -> {
            Object provider = providerFactory.create(config);
            if (!expectedType.isInstance(provider)) {
                throw new IllegalArgumentException(
                    "Provider for reference '" + reference + "' is "
                    + provider.getClass().getName() + ", not " + expectedType.getName());
            }
            return extractor.apply(expectedType.cast(provider));
        }, policy);
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

    private void registerBuiltIns() {
        Locale locale = config.getLocale();

        FullNameGenerator fullName = new FullNameGenerator(config);
        EmailGenerator email = new EmailGenerator(config);
        CityGenerator city = new CityGenerator(config);
        StateGenerator state = new StateGenerator(config);
        StreetAddressGenerator street = new StreetAddressGenerator(config);
        PostalCodeGenerator postalCode = new PostalCodeGenerator(config);
        CountryGenerator country = new CountryGenerator(config);
        DomainGenerator domain = new DomainGenerator(config);
        URLGenerator url = new URLGenerator(config);
        CurrencyGenerator currency = new CurrencyGenerator(config);
        MoneyGenerator money = new MoneyGenerator(config);
        CreditCardGenerator card = new CreditCardGenerator(config);
        DateGenerator date = new DateGenerator(config);
        TimezoneGenerator timezone = new TimezoneGenerator(config);
        WordGenerator word = new WordGenerator(config);
        SentenceGenerator sentence = new SentenceGenerator(config);
        UUIDGenerator uuid = new UUIDGenerator(config);
        EanGenerator ean = new EanGenerator(config);
        UpcGenerator upc = new UpcGenerator(config);
        IsbnGenerator isbn = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, config);
        HashGenerator hash = new HashGenerator(config);

        register("person.full_name", ctx -> fullName.generate(), ConflictPolicy.REPLACE);
        register("person.email", ctx -> email.generate(), ConflictPolicy.REPLACE);
        register("address.city", ctx -> city.generate(), ConflictPolicy.REPLACE);
        register("address.state", ctx -> state.generate(), ConflictPolicy.REPLACE);
        register("address.street", ctx -> street.generate(), ConflictPolicy.REPLACE);
        register("address.postal_code", ctx -> postalCode.generate(), ConflictPolicy.REPLACE);
        register("address.country", ctx -> country.generate(), ConflictPolicy.REPLACE);
        register("internet.domain", ctx -> domain.generate(), ConflictPolicy.REPLACE);
        register("internet.url", ctx -> url.generate(), ConflictPolicy.REPLACE);
        register("finance.currency_iso_code", ctx -> currency.generateCurrencyIsoCode(locale), ConflictPolicy.REPLACE);
        register("finance.price", ctx -> money.generatePrice(locale), ConflictPolicy.REPLACE);
        register("finance.credit_card_number", ctx -> card.generateNumber(), ConflictPolicy.REPLACE);
        register("finance.cvv", ctx -> card.generateCvv(), ConflictPolicy.REPLACE);
        register("datetime.date", ctx -> date.generateString(), ConflictPolicy.REPLACE);
        register("datetime.timestamp", ctx -> date.generateUnixTime(), ConflictPolicy.REPLACE);
        register("datetime.timezone", ctx -> timezone.generateTimezone(), ConflictPolicy.REPLACE);
        register("text.word", ctx -> word.generateWord(), ConflictPolicy.REPLACE);
        register("text.sentence", ctx -> sentence.generateSentence(), ConflictPolicy.REPLACE);
        register("code.uuid4", ctx -> uuid.generateV4().toString(), ConflictPolicy.REPLACE);
        register("code.ean13", ctx -> ean.generateEan13(), ConflictPolicy.REPLACE);
        register("code.upc", ctx -> upc.generate(), ConflictPolicy.REPLACE);
        register("code.isbn13", ctx -> isbn.generate(), ConflictPolicy.REPLACE);
        register("code.sha256", ctx -> hash.generateSha256(), ConflictPolicy.REPLACE);
    }
}
