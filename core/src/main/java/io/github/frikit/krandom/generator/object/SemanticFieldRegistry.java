/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Registry for semantic object-generation field aliases and provider mappings.
 *
 * <p>The default registry contains krandom's built-in semantic names such as
 * {@code firstName}, {@code email}, {@code createdAt}, and {@code currencyCode}.
 * Build a derived registry when a project uses different field vocabulary but still
 * wants object generation to route those fields to the same semantic providers.
 */
public final class SemanticFieldRegistry {

    private static final SemanticFieldRegistry DEFAULTS = buildDefaults();

    private final Map<String, String>      keysByAlias;
    private final Map<String, Set<String>> aliasesByKey;
    private final Map<String, String>      providerNamesByKey;

    private SemanticFieldRegistry(Builder builder) {
        this.keysByAlias = Collections.unmodifiableMap(new LinkedHashMap<>(builder.keysByAlias));
        this.aliasesByKey = buildAliasesByKey(this.keysByAlias);
        this.providerNamesByKey = Collections.unmodifiableMap(new LinkedHashMap<>(builder.providerNamesByKey));
    }

    /**
     * Returns the built-in semantic field registry.
     *
     * @return default semantic registry
     */
    public static SemanticFieldRegistry defaults() {
        return DEFAULTS;
    }

    /**
     * Creates an empty registry builder.
     *
     * @return empty registry builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder pre-populated from this registry.
     *
     * @return builder containing this registry's aliases and providers
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns the canonical semantic key for a field name.
     *
     * @param fieldName field name to look up
     * @return canonical semantic key, or {@code null} when no alias matches
     */
    public String semanticKeyForFieldName(String fieldName) {
        return keysByAlias.get(normalizeFieldName(fieldName));
    }

    /**
     * Returns all normalized aliases registered for a canonical semantic key.
     *
     * @param semanticKey canonical or aliased semantic key
     * @return immutable alias set, or an empty set when no aliases are registered
     */
    public Set<String> semanticAliasesFor(String semanticKey) {
        return aliasesByKey.getOrDefault(canonicalKeyFor(semanticKey), Set.of());
    }

    /**
     * Returns the provider name for a semantic key.
     *
     * @param semanticKey canonical or aliased semantic key
     * @return provider name, or {@code null} when the key is not provider-backed
     */
    public String semanticProviderNameFor(String semanticKey) {
        return providerNamesByKey.get(canonicalKeyFor(semanticKey));
    }

    Set<String> providerBackedSemanticKeys() {
        return providerNamesByKey.keySet();
    }

    private static SemanticFieldRegistry buildDefaults() {
        return builder()
            .alias("firstname", "firstname", "givenname")
            .alias("lastname", "lastname", "surname", "familyname")
            .alias("fullname", "fullname", "displayname")
            .alias("email", "email", "emailaddress")
            .alias("username", "username", "userhandle")
            .alias("phone", "phone", "phonenumber", "mobile", "mobilephone", "telephone")
            .alias("streetaddress", "street", "streetaddress", "addressline1")
            .alias("city", "city", "town")
            .alias("state", "state", "province", "region")
            .alias("postalcode", "postalcode", "postcode", "zipcode", "zip")
            .alias("country", "country", "countryname")
            .alias("companyname", "company", "companyname", "organization", "organisation")
            .alias("industry", "industry", "sector")
            .alias("companyemail", "companyemail", "businessemail", "corporateemail")
            .alias("companyurl", "companyurl", "companywebsite", "businesswebsite")
            .alias("password", "password", "passcode")
            .alias("url", "url", "website", "homepage", "link")
            .alias("domain", "domain", "hostname")
            .alias("currency", "currency", "currencycode")
            .alias("uuid", "uuid", "guid")
            .alias("createdat", "createdat", "createdon", "createddate", "createdtimestamp", "creationdate")
            .alias("updatedat", "updatedat", "updatedon", "updateddate", "updatedtimestamp", "lastupdated", "modifiedat", "modifiedon")
            .alias("birthdate", "birthdate", "dateofbirth", "dob", "birthday", "bornon")
            .alias("age", "age", "ageyears", "yearsold")
            .alias("amount", "amount", "totalamount", "subtotal", "paymentamount")
            .alias("balance", "balance", "accountbalance", "currentbalance")
            .alias("price", "price", "unitprice", "cost")
            .alias("id", "id", "userid", "accountid", "orderid", "customerid", "productid", "geoid", "identifier")
            .alias("active", "active", "isactive", "enabled", "isenabled")
            .alias("status", "status", "accountstatus", "orderstatus")
            .alias("latitude", "latitude", "lat")
            .alias("longitude", "longitude", "lon", "lng")
            .provider("firstname", "person.first_name")
            .provider("lastname", "person.last_name")
            .provider("fullname", "person.full_name")
            .provider("email", "person.email")
            .provider("username", "person.username")
            .provider("phone", "address.phone_number")
            .provider("streetaddress", "address.street_address")
            .provider("city", "address.city")
            .provider("state", "address.state")
            .provider("postalcode", "address.postal_code")
            .provider("country", "address.country")
            .provider("companyname", "company.name")
            .provider("industry", "company.industry")
            .provider("companyemail", "company.email")
            .provider("companyurl", "company.url")
            .provider("password", "security.password")
            .provider("url", "internet.url")
            .provider("domain", "internet.domain")
            .provider("currency", "finance.currency")
            .provider("uuid", "code.uuid")
            .build();
    }

    private static Map<String, Set<String>> buildAliasesByKey(Map<String, String> keysByAlias) {
        Map<String, Set<String>> aliases = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : keysByAlias.entrySet()) {
            aliases.computeIfAbsent(entry.getValue(), ignored -> new LinkedHashSet<>()).add(entry.getKey());
        }
        Map<String, Set<String>> unmodifiable = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : aliases.entrySet()) {
            unmodifiable.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(unmodifiable);
    }

    private String canonicalKeyFor(String semanticKey) {
        String normalized = normalizeSemanticKey(semanticKey);
        return keysByAlias.getOrDefault(normalized, normalized);
    }

    static String normalizeFieldName(String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName");
        StringBuilder normalized = new StringBuilder(fieldName.length());
        for (int i = 0; i < fieldName.length(); i++) {
            char ch = fieldName.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    private static String normalizeSemanticKey(String semanticKey) {
        String normalized = normalizeFieldName(semanticKey);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("semanticKey must contain at least one letter or digit");
        }
        return normalized;
    }

    /**
     * Mutable builder for {@link SemanticFieldRegistry}.
     */
    public static final class Builder {

        private final Map<String, String> keysByAlias        = new LinkedHashMap<>();
        private final Map<String, String> providerNamesByKey = new LinkedHashMap<>();

        private Builder() {
        }

        private Builder(SemanticFieldRegistry source) {
            keysByAlias.putAll(source.keysByAlias);
            providerNamesByKey.putAll(source.providerNamesByKey);
        }

        /**
         * Registers one canonical semantic key with one or more field-name aliases.
         *
         * @param semanticKey canonical semantic key
         * @param fieldNames field-name aliases that should resolve to the key
         * @return this builder
         */
        public Builder alias(String semanticKey, String... fieldNames) {
            String canonicalKey = normalizeSemanticKey(semanticKey);
            Objects.requireNonNull(fieldNames, "fieldNames");
            if (fieldNames.length == 0) {
                throw new IllegalArgumentException("fieldNames must not be empty");
            }
            for (String fieldName : fieldNames) {
                String alias = normalizeSemanticKey(fieldName);
                keysByAlias.put(alias, canonicalKey);
            }
            return this;
        }

        /**
         * Registers the provider used for provider-backed string semantics.
         *
         * @param semanticKey canonical semantic key
         * @param providerName provider lookup name
         * @return this builder
         */
        public Builder provider(String semanticKey, String providerName) {
            String canonicalKey = normalizeSemanticKey(semanticKey);
            Objects.requireNonNull(providerName, "providerName");
            if (providerName.isBlank()) {
                throw new IllegalArgumentException("providerName must not be blank");
            }
            providerNamesByKey.put(canonicalKey, providerName);
            return this;
        }

        /**
         * Builds an immutable registry.
         *
         * @return immutable semantic field registry
         */
        public SemanticFieldRegistry build() {
            return new SemanticFieldRegistry(this);
        }
    }
}
