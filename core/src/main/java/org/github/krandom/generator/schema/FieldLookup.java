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
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Resolves string field references to concrete value providers.
 */
public final class FieldLookup {

    private final Map<String, SchemaValueProvider> providers;

    /**
     * Creates a lookup with generators initialized from the provided config.
     *
     * @param config generator config used for locale/seed propagation
     */
    public FieldLookup(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        Locale locale = effective.getLocale();

        FullNameGenerator fullName = new FullNameGenerator(effective);
        EmailGenerator email = new EmailGenerator(effective);
        CityGenerator city = new CityGenerator(effective);
        StateGenerator state = new StateGenerator(effective);
        StreetAddressGenerator street = new StreetAddressGenerator(effective);
        PostalCodeGenerator postalCode = new PostalCodeGenerator(effective);
        CountryGenerator country = new CountryGenerator(effective);
        DomainGenerator domain = new DomainGenerator(effective);
        URLGenerator url = new URLGenerator(effective);
        CurrencyGenerator currency = new CurrencyGenerator(effective);
        MoneyGenerator money = new MoneyGenerator(effective);
        CreditCardGenerator card = new CreditCardGenerator(effective);
        DateGenerator date = new DateGenerator(effective);
        TimezoneGenerator timezone = new TimezoneGenerator(effective);
        WordGenerator word = new WordGenerator(effective);
        SentenceGenerator sentence = new SentenceGenerator(effective);
        UUIDGenerator uuid = new UUIDGenerator(effective);
        EanGenerator ean = new EanGenerator(effective);
        UpcGenerator upc = new UpcGenerator(effective);
        IsbnGenerator isbn = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, effective);
        HashGenerator hash = new HashGenerator(effective);

        Map<String, SchemaValueProvider> map = new LinkedHashMap<>();
        map.put("person.full_name", ctx -> fullName.generate());
        map.put("person.email", ctx -> email.generate());
        map.put("address.city", ctx -> city.generate());
        map.put("address.state", ctx -> state.generate());
        map.put("address.street", ctx -> street.generate());
        map.put("address.postal_code", ctx -> postalCode.generate());
        map.put("address.country", ctx -> country.generate());
        map.put("internet.domain", ctx -> domain.generate());
        map.put("internet.url", ctx -> url.generate());
        map.put("finance.currency_iso_code", ctx -> currency.generateCurrencyIsoCode(locale));
        map.put("finance.price", ctx -> money.generatePrice(locale));
        map.put("finance.credit_card_number", ctx -> card.generateNumber());
        map.put("finance.cvv", ctx -> card.generateCvv());
        map.put("datetime.date", ctx -> date.generateString());
        map.put("datetime.timestamp", ctx -> date.generateUnixTime());
        map.put("datetime.timezone", ctx -> timezone.generateTimezone());
        map.put("text.word", ctx -> word.generateWord());
        map.put("text.sentence", ctx -> sentence.generateSentence());
        map.put("code.uuid4", ctx -> uuid.generateV4().toString());
        map.put("code.ean13", ctx -> ean.generateEan13());
        map.put("code.upc", ctx -> upc.generate());
        map.put("code.isbn13", ctx -> isbn.generate());
        map.put("code.sha256", ctx -> hash.generateSha256());
        this.providers = Map.copyOf(map);
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
     * Resolves a string reference to a provider.
     *
     * @param reference field reference
     * @return resolved value provider
     */
    public SchemaValueProvider resolve(String reference) {
        String key = normalize(reference);
        SchemaValueProvider provider = providers.get(key);
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
        return providers.keySet();
    }
}
