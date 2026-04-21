/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies lightweight sibling-field coherence after structural value generation.
 */
final class SemanticCoherenceAdjuster {

    private static final Pattern MONEY_FRAGMENT = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    private final ObjectGeneratorConfig config;
    private final UniqueFieldTracker uniqueFieldTracker;

    SemanticCoherenceAdjuster(ObjectGeneratorConfig config, UniqueFieldTracker uniqueFieldTracker) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.uniqueFieldTracker = Objects.requireNonNull(uniqueFieldTracker, "uniqueFieldTracker must not be null");
    }

    void adjustInstance(Class<?> ownerType, Object instance, List<Field> fields, boolean allowOverwriteExisting) {
        Objects.requireNonNull(ownerType, "ownerType must not be null");
        Objects.requireNonNull(instance, "instance must not be null");
        if (config.getSemanticMode() == ObjectGenerationSemanticMode.STRUCTURAL_ONLY) {
            return;
        }

        List<Slot> slots = new ArrayList<>();
        for (Field field : fields) {
            if (config.shouldExclude(field)) {
                continue;
            }
            field.setAccessible(true);
            slots.add(new ReflectionSlot(ownerType, field, instance, config.isIgnoreErrors()));
        }
        apply(slots, allowOverwriteExisting);
    }

    void adjustRecordArguments(Class<?> ownerType,
                               RecordComponent[] components,
                               Field[] backingFields,
                               Object[] args) {
        Objects.requireNonNull(ownerType, "ownerType must not be null");
        Objects.requireNonNull(components, "components must not be null");
        Objects.requireNonNull(backingFields, "backingFields must not be null");
        Objects.requireNonNull(args, "args must not be null");
        if (config.getSemanticMode() == ObjectGenerationSemanticMode.STRUCTURAL_ONLY) {
            return;
        }

        List<Slot> slots = new ArrayList<>(components.length);
        for (int i = 0; i < components.length; i++) {
            Field backingField = backingFields[i];
            if (config.shouldExclude(backingField)) {
                continue;
            }
            slots.add(new RecordSlot(ownerType, components[i], backingField, args, i));
        }
        apply(slots, true);
    }

    private void apply(List<Slot> slots, boolean allowOverwriteExisting) {
        if (slots.isEmpty()) {
            return;
        }

        Map<String, Slot> slotsBySemanticKey = new LinkedHashMap<>();
        for (Slot slot : slots) {
            String semanticKey = FieldGeneratorResolver.semanticKeyForFieldName(slot.fieldName());
            if (semanticKey != null) {
                slotsBySemanticKey.putIfAbsent(semanticKey, slot);
            }
        }
        if (slotsBySemanticKey.isEmpty()) {
            return;
        }

        harmonizeAddressCountry(slotsBySemanticKey, allowOverwriteExisting);
        String domain = harmonizeDomain(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeFullName(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeEmail(slotsBySemanticKey, domain, allowOverwriteExisting);
        harmonizeCompanyEmail(slotsBySemanticKey, domain, allowOverwriteExisting);
        harmonizeUrl(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeCompanyUrl(slotsBySemanticKey, domain, allowOverwriteExisting);
        harmonizeTimestamps(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeAgeAndBirthDate(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeMonetaryFields(slotsBySemanticKey, allowOverwriteExisting);
        harmonizeActiveStatus(slotsBySemanticKey, allowOverwriteExisting);
    }

    private void harmonizeAddressCountry(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot countrySlot = slotsBySemanticKey.get("country");
        if (countrySlot == null || !canAssign(countrySlot, allowOverwriteExisting)) {
            return;
        }

        boolean hasAddressSignals = stringValue(slotsBySemanticKey.get("streetaddress")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("city")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("state")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("postalcode")) != null;
        if (!hasAddressSignals) {
            return;
        }

        String localeCountry = localeCurrentCountry();
        if (localeCountry != null) {
            countrySlot.setValue(applyUniqueness(countrySlot, "country", localeCountry));
        }
    }

    private String harmonizeDomain(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot domainSlot = slotsBySemanticKey.get("domain");
        String resolvedDomain = normalizeDomain(stringValue(domainSlot));
        if (resolvedDomain != null) {
            if (canAssign(domainSlot, allowOverwriteExisting)) {
                Object assigned = applyUniqueness(domainSlot, "domain", resolvedDomain);
                domainSlot.setValue(assigned);
                resolvedDomain = normalizeDomain(stringValue(assigned));
            }
            return resolvedDomain;
        }

        resolvedDomain = normalizeDomain(emailDomain(stringValue(slotsBySemanticKey.get("email"))));
        if (resolvedDomain == null) {
            resolvedDomain = normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("url"))));
        }
        if (resolvedDomain == null) {
            resolvedDomain = normalizeDomain(domainFromCompany(stringValue(slotsBySemanticKey.get("companyname"))));
        }

        if (resolvedDomain != null && canAssign(domainSlot, allowOverwriteExisting)) {
            Object assigned = applyUniqueness(domainSlot, "domain", resolvedDomain);
            domainSlot.setValue(assigned);
            resolvedDomain = normalizeDomain(stringValue(assigned));
        }
        return resolvedDomain;
    }

    private void harmonizeFullName(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot firstNameSlot = slotsBySemanticKey.get("firstname");
        Slot lastNameSlot = slotsBySemanticKey.get("lastname");
        Slot fullNameSlot = slotsBySemanticKey.get("fullname");
        if (firstNameSlot == null || lastNameSlot == null || fullNameSlot == null) {
            return;
        }

        String firstName = normalizedHumanName(stringValue(firstNameSlot));
        String lastName = normalizedHumanName(stringValue(lastNameSlot));
        if (firstName == null || lastName == null || !canAssign(fullNameSlot, allowOverwriteExisting)) {
            return;
        }

        fullNameSlot.setValue(applyUniqueness(fullNameSlot, "fullname", firstName + " " + lastName));
    }

    private void harmonizeEmail(Map<String, Slot> slotsBySemanticKey, String resolvedDomain, boolean allowOverwriteExisting) {
        Slot emailSlot = slotsBySemanticKey.get("email");
        if (emailSlot == null || !canAssign(emailSlot, allowOverwriteExisting)) {
            return;
        }

        String localPart = emailLocalPart(slotsBySemanticKey);
        if (localPart == null) {
            return;
        }

        String domain = resolvedDomain;
        if (domain == null) {
            domain = normalizeDomain(emailDomain(stringValue(emailSlot)));
        }
        if (domain == null) {
            domain = normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("url"))));
        }
        if (domain == null) {
            domain = "example.com";
        }

        emailSlot.setValue(applyUniqueness(emailSlot, "email", localPart + "@" + domain));
    }

    private void harmonizeCompanyEmail(Map<String, Slot> slotsBySemanticKey, String resolvedDomain, boolean allowOverwriteExisting) {
        Slot companyEmailSlot = slotsBySemanticKey.get("companyemail");
        if (companyEmailSlot == null || !canAssign(companyEmailSlot, allowOverwriteExisting)) {
            return;
        }

        String domain = resolvedDomain;
        if (domain == null) {
            domain = normalizeDomain(emailDomain(stringValue(companyEmailSlot)));
        }
        if (domain == null) {
            domain = normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("companyurl"))));
        }
        if (domain == null) {
            domain = normalizeDomain(domainFromCompany(stringValue(slotsBySemanticKey.get("companyname"))));
        }
        if (domain == null) {
            return;
        }

        companyEmailSlot.setValue(applyUniqueness(companyEmailSlot, "companyemail", "hello@" + domain));
    }

    private void harmonizeUrl(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot urlSlot = slotsBySemanticKey.get("url");
        if (urlSlot == null || !canAssign(urlSlot, allowOverwriteExisting)) {
            return;
        }

        String domain = normalizeDomain(stringValue(slotsBySemanticKey.get("domain")));
        if (domain == null) {
            domain = normalizeDomain(emailDomain(stringValue(slotsBySemanticKey.get("email"))));
        }
        if (domain == null) {
            return;
        }

        urlSlot.setValue(applyUniqueness(urlSlot, "url", "https://www." + domain));
    }

    private void harmonizeCompanyUrl(Map<String, Slot> slotsBySemanticKey, String resolvedDomain, boolean allowOverwriteExisting) {
        Slot companyUrlSlot = slotsBySemanticKey.get("companyurl");
        if (companyUrlSlot == null || !canAssign(companyUrlSlot, allowOverwriteExisting)) {
            return;
        }

        String domain = resolvedDomain;
        if (domain == null) {
            domain = normalizeDomain(urlHost(stringValue(companyUrlSlot)));
        }
        if (domain == null) {
            domain = normalizeDomain(emailDomain(stringValue(slotsBySemanticKey.get("companyemail"))));
        }
        if (domain == null) {
            domain = normalizeDomain(domainFromCompany(stringValue(slotsBySemanticKey.get("companyname"))));
        }
        if (domain == null) {
            return;
        }

        companyUrlSlot.setValue(applyUniqueness(companyUrlSlot, "companyurl", "https://www." + domain));
    }

    private void harmonizeTimestamps(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot createdAtSlot = slotsBySemanticKey.get("createdat");
        Slot updatedAtSlot = slotsBySemanticKey.get("updatedat");
        if (createdAtSlot == null || updatedAtSlot == null) {
            return;
        }

        Instant createdAt = toInstant(createdAtSlot.getValue());
        Instant updatedAt = toInstant(updatedAtSlot.getValue());
        if (createdAt == null || updatedAt == null || !createdAt.isAfter(updatedAt)) {
            return;
        }

        if (canAssign(updatedAtSlot, allowOverwriteExisting)) {
            updatedAtSlot.setValue(fromInstant(createdAt, updatedAtSlot.rawType()));
            return;
        }
        if (canAssign(createdAtSlot, allowOverwriteExisting)) {
            createdAtSlot.setValue(fromInstant(updatedAt, createdAtSlot.rawType()));
        }
    }

    private void harmonizeAgeAndBirthDate(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot birthDateSlot = slotsBySemanticKey.get("birthdate");
        Slot ageSlot = slotsBySemanticKey.get("age");
        if (birthDateSlot == null || ageSlot == null) {
            return;
        }

        LocalDate birthDate = toLocalDate(birthDateSlot.getValue());
        Integer age = toInteger(ageSlot.getValue());
        if (birthDate != null) {
            int derivedAge = ageFromBirthDate(birthDate);
            if (age != null && age == derivedAge) {
                return;
            }
            if (canAssign(ageSlot, allowOverwriteExisting)) {
                ageSlot.setValue(fromAge(derivedAge, ageSlot.rawType()));
                return;
            }
            if (age != null && canAssign(birthDateSlot, allowOverwriteExisting)) {
                birthDateSlot.setValue(fromLocalDate(LocalDate.now().minusYears(age), birthDateSlot.rawType()));
            }
            return;
        }

        if (age != null && canAssign(birthDateSlot, allowOverwriteExisting)) {
            birthDateSlot.setValue(fromLocalDate(LocalDate.now().minusYears(age), birthDateSlot.rawType()));
        }
    }

    private void harmonizeActiveStatus(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot activeSlot = slotsBySemanticKey.get("active");
        Slot statusSlot = slotsBySemanticKey.get("status");
        if (activeSlot == null || statusSlot == null) {
            return;
        }

        Boolean active = toBoolean(activeSlot.getValue());
        Boolean statusActive = activeFromStatus(statusSlot.getValue());
        if (active != null) {
            if (statusActive != null && statusActive == active) {
                return;
            }
            Object statusValue = statusValueFor(active, statusSlot.rawType());
            if (statusValue != null && canAssign(statusSlot, allowOverwriteExisting)) {
                statusSlot.setValue(statusValue);
                return;
            }
            if (statusActive != null && canAssign(activeSlot, allowOverwriteExisting)) {
                activeSlot.setValue(statusActive);
            }
            return;
        }

        if (statusActive != null && canAssign(activeSlot, allowOverwriteExisting)) {
            activeSlot.setValue(statusActive);
        }
    }

    private void harmonizeMonetaryFields(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot priceSlot = slotsBySemanticKey.get("price");
        Slot amountSlot = slotsBySemanticKey.get("amount");
        Slot balanceSlot = slotsBySemanticKey.get("balance");
        boolean hasAnyMoneySlot = priceSlot != null | amountSlot != null | balanceSlot != null;
        if (!hasAnyMoneySlot) {
            return;
        }

        String currencyCode = currencyCode(slotsBySemanticKey.get("currency"));
        BigDecimal price = moneyValue(priceSlot);
        BigDecimal amount = moneyValue(amountSlot);
        BigDecimal balance = moneyValue(balanceSlot);

        boolean backfillPrice = price == null & amount != null & canAssign(priceSlot, allowOverwriteExisting);
        if (backfillPrice) {
            price = assignMoney(priceSlot, amount, currencyCode);
        }
        boolean backfillAmount = amount == null & price != null & canAssign(amountSlot, allowOverwriteExisting);
        if (backfillAmount) {
            amount = assignMoney(amountSlot, price, currencyCode);
        }
        boolean backfillBalance = balance == null & amount != null & canAssign(balanceSlot, allowOverwriteExisting);
        if (backfillBalance) {
            balance = assignMoney(balanceSlot, amount, currencyCode);
        }

        boolean amountBelowPrice = isLessThan(amount, price);
        if (amountBelowPrice) {
            if (canAssign(amountSlot, allowOverwriteExisting)) {
                amount = assignMoney(amountSlot, price, currencyCode);
            } else if (canAssign(priceSlot, allowOverwriteExisting)) {
                price = assignMoney(priceSlot, amount, currencyCode);
            }
        }

        boolean balanceBelowAmount = isLessThan(balance, amount);
        if (balanceBelowAmount) {
            if (canAssign(balanceSlot, allowOverwriteExisting)) {
                balance = assignMoney(balanceSlot, amount, currencyCode);
            } else if (canAssign(amountSlot, allowOverwriteExisting)) {
                amount = assignMoney(amountSlot, balance, currencyCode);
            }
        }

        boolean formatPriceString = shouldFormatMoneyString(price, priceSlot, allowOverwriteExisting);
        if (formatPriceString) {
            assignMoney(priceSlot, price, currencyCode);
        }
        boolean formatAmountString = shouldFormatMoneyString(amount, amountSlot, allowOverwriteExisting);
        if (formatAmountString) {
            assignMoney(amountSlot, amount, currencyCode);
        }
        boolean formatBalanceString = shouldFormatMoneyString(balance, balanceSlot, allowOverwriteExisting);
        if (formatBalanceString) {
            assignMoney(balanceSlot, balance, currencyCode);
        }
    }

    private boolean canAssign(Slot slot, boolean allowOverwriteExisting) {
        if (slot == null || isProtected(slot)) {
            return false;
        }
        if (allowOverwriteExisting) {
            return true;
        }
        Object currentValue = slot.getValue();
        if (currentValue == null) {
            return true;
        }
        if (slot.rawType() == String.class) {
            return stringValue(currentValue) == null;
        }
        return false;
    }

    private boolean isProtected(Slot slot) {
        if (config.getContextualFieldOverride(slot.ownerType(), slot.fieldName()).isPresent()
            || config.getFieldOverride(slot.ownerType(), slot.fieldName()).isPresent()
            || config.getContextualTypeOverride(slot.rawType()).isPresent()
            || config.getTypeOverride(slot.rawType()).isPresent()) {
            return true;
        }

        if (config.getSemanticMode() == ObjectGenerationSemanticMode.RELAXED) {
            AnnotatedElement annotatedElement = slot.annotatedElement();
            if (annotatedElement.isAnnotationPresent(Randomizer.class)) {
                return true;
            }
            return BeanValidationSupport.constraintGeneratorFor(annotatedElement, slot.rawType()) != null;
        }
        return false;
    }

    private Object applyUniqueness(Slot slot, String semanticKey, String candidate) {
        if (candidate == null || !isUniqueField(slot.fieldName(), semanticKey)) {
            return candidate;
        }

        AtomicInteger attempt = new AtomicInteger();
        String normalizedFieldName = FieldGeneratorResolver.normalizeSemanticFieldName(semanticKey);
        return uniqueFieldTracker.nextUnique(normalizedFieldName, () -> uniquifyString(candidate, attempt.getAndIncrement()),
                                             config.getUniquenessMaxAttempts());
    }

    private boolean isUniqueField(String fieldName, String semanticKey) {
        String normalizedFieldName = FieldGeneratorResolver.normalizeSemanticFieldName(fieldName);
        Set<String> uniqueFieldNames = config.getUniqueFieldNames();
        if (uniqueFieldNames.contains(normalizedFieldName) || uniqueFieldNames.contains(semanticKey)) {
            return true;
        }
        for (String alias : FieldGeneratorResolver.semanticAliasesFor(semanticKey)) {
            if (uniqueFieldNames.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private static String uniquifyString(String candidate, int attempt) {
        if (attempt == 0) {
            return candidate;
        }
        int atIndex = candidate.indexOf('@');
        if (atIndex > 0) {
            return candidate.substring(0, atIndex) + attempt + candidate.substring(atIndex);
        }
        return candidate + attempt;
    }

    private static String emailLocalPart(Map<String, Slot> slotsBySemanticKey) {
        String firstName = normalizedHumanName(stringValue(slotsBySemanticKey.get("firstname")));
        String lastName = normalizedHumanName(stringValue(slotsBySemanticKey.get("lastname")));
        if (firstName != null && lastName != null) {
            String left = slugFragment(firstName);
            String right = slugFragment(lastName);
            if (left != null && right != null) {
                return left + "." + right;
            }
        }

        String fullName = normalizedHumanName(stringValue(slotsBySemanticKey.get("fullname")));
        if (fullName == null) {
            return null;
        }

        String[] parts = fullName.split("\\s+");
        if (parts.length < 2) {
            String fullNameSlug = slugFragment(fullName);
            if (fullNameSlug != null) {
                return fullNameSlug;
            }
        } else {
            String left = slugFragment(parts[0]);
            String right = slugFragment(parts[parts.length - 1]);
            if (left != null && right != null) {
                return left + "." + right;
            }
        }
        return stringValue(slotsBySemanticKey.get("companyname")) != null ? "hello" : null;
    }

    private static String domainFromCompany(String companyName) {
        String slug = slugFragment(companyName);
        return slug != null ? slug + ".com" : null;
    }

    private static String normalizedHumanName(String value) {
        String trimmed = stringValue(value);
        return trimmed == null ? null : trimmed.trim();
    }

    private static String slugFragment(String value) {
        String trimmed = stringValue(value);
        if (trimmed == null) {
            return null;
        }
        StringBuilder slug = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (ch <= 127 && Character.isLetterOrDigit(ch)) {
                slug.append(Character.toLowerCase(ch));
            }
        }
        return slug.isEmpty() ? null : slug.toString();
    }

    private static String normalizeDomain(String candidate) {
        String trimmed = stringValue(candidate);
        if (trimmed == null) {
            return null;
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        return normalized.isBlank() ? null : normalized;
    }

    private static String emailDomain(String email) {
        String trimmed = stringValue(email);
        if (trimmed == null) {
            return null;
        }
        int atIndex = trimmed.indexOf('@');
        return atIndex >= 0 && atIndex + 1 < trimmed.length() ? trimmed.substring(atIndex + 1) : null;
    }

    private static String urlHost(String url) {
        String trimmed = stringValue(url);
        if (trimmed == null) {
            return null;
        }
        try {
            String host = URI.create(trimmed).getHost();
            return host != null ? host : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String stringValue(Slot slot) {
        return slot == null ? null : stringValue(slot.getValue());
    }

    private static Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof LocalDate localDate) {
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        return null;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        Instant instant = toInstant(value);
        return instant != null ? instant.atOffset(ZoneOffset.UTC).toLocalDate() : null;
    }

    private static BigDecimal moneyValue(Slot slot) {
        return slot == null ? null : moneyValue(slot.getValue());
    }

    private static BigDecimal moneyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return normalizeMoney(decimal);
        }
        if (value instanceof BigInteger integer) {
            return normalizeMoney(new BigDecimal(integer));
        }
        if (value instanceof Byte | value instanceof Short | value instanceof Integer | value instanceof Long) {
            return normalizeMoney(BigDecimal.valueOf(((Number) value).longValue()));
        }
        if (value instanceof Float | value instanceof Double) {
            return normalizeMoney(BigDecimal.valueOf(((Number) value).doubleValue()));
        }
        if (value instanceof String stringValue) {
            Matcher matcher = MONEY_FRAGMENT.matcher(stringValue);
            if (!matcher.find()) {
                return null;
            }
            return normalizeMoney(new BigDecimal(matcher.group()));
        }
        return null;
    }

    private static Integer toInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Long longValue && longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
            return longValue.intValue();
        }
        if (value instanceof Short shortValue) {
            return shortValue.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.valueOf(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String localeCurrentCountry() {
        Locale locale = config.getGeneratorConfig().getLocale();
        boolean localeHasNoCountry = locale.getCountry().isBlank();
        boolean localeRegistered = config.getGeneratorConfig().getRegistryContext().isCountryRegistered(locale);
        if (localeHasNoCountry | !localeRegistered) {
            return null;
        }
        return new CountryGenerator(config.getGeneratorConfig()).currentCountry();
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.abs().setScale(2, RoundingMode.HALF_UP);
    }

    private static String currencyCode(Slot slot) {
        return slot == null ? null : currencyCode(slot.getValue());
    }

    private static String currencyCode(Object value) {
        if (value instanceof org.github.krandom.generator.finance.Currency currency) {
            return currency.getCode();
        }
        if (value instanceof java.util.Currency currency) {
            return currency.getCurrencyCode();
        }
        if (!(value instanceof String stringValue)) {
            return null;
        }
        String normalized = stringValue(stringValue);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        org.github.krandom.generator.finance.Currency libraryCurrency =
            org.github.krandom.generator.finance.Currency.fromCode(normalized);
        if (libraryCurrency != null) {
            return libraryCurrency.getCode();
        }
        try {
            return java.util.Currency.getInstance(normalized).getCurrencyCode();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Object moneyValueFor(BigDecimal value, Class<?> rawType, String currencyCode) {
        BigDecimal normalized = normalizeMoney(value);
        if (rawType == BigDecimal.class) {
            return normalized;
        }
        if (rawType == BigInteger.class) {
            return normalized.setScale(0, RoundingMode.HALF_UP).toBigInteger();
        }
        if (rawType == byte.class | rawType == Byte.class) {
            return normalized.setScale(0, RoundingMode.HALF_UP).byteValue();
        }
        if (rawType == short.class | rawType == Short.class) {
            return normalized.setScale(0, RoundingMode.HALF_UP).shortValue();
        }
        if (rawType == int.class | rawType == Integer.class) {
            return normalized.setScale(0, RoundingMode.HALF_UP).intValue();
        }
        if (rawType == long.class | rawType == Long.class) {
            return normalized.setScale(0, RoundingMode.HALF_UP).longValue();
        }
        if (rawType == float.class | rawType == Float.class) {
            return normalized.floatValue();
        }
        if (rawType == double.class | rawType == Double.class) {
            return normalized.doubleValue();
        }
        if (rawType == String.class) {
            return currencyCode != null ? currencyCode + " " + normalized.toPlainString() : normalized.toPlainString();
        }
        return null;
    }

    private static BigDecimal assignMoney(Slot slot, BigDecimal value, String currencyCode) {
        if (slot == null || value == null) {
            return value;
        }
        Object converted = moneyValueFor(value, slot.rawType(), currencyCode);
        if (converted == null) {
            return value;
        }
        slot.setValue(converted);
        return normalizeMoney(value);
    }

    private static Boolean toBoolean(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private boolean shouldFormatMoneyString(BigDecimal value, Slot slot, boolean allowOverwriteExisting) {
        if (value == null || slot == null) {
            return false;
        }
        if (slot.rawType() != String.class) {
            return false;
        }
        return canAssign(slot, allowOverwriteExisting);
    }

    private static boolean isLessThan(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) < 0;
    }

    private static int ageFromBirthDate(LocalDate birthDate) {
        return Math.toIntExact(ChronoUnit.YEARS.between(birthDate, LocalDate.now()));
    }

    private static Object fromAge(int age, Class<?> rawType) {
        if (rawType == int.class || rawType == Integer.class) {
            return age;
        }
        if (rawType == long.class || rawType == Long.class) {
            return (long) age;
        }
        if (rawType == short.class || rawType == Short.class) {
            return (short) age;
        }
        if (rawType == String.class) {
            return Integer.toString(age);
        }
        throw new ObjectGenerationException("Unsupported semantic age type: " + rawType.getName());
    }

    private static Object fromLocalDate(LocalDate localDate, Class<?> rawType) {
        return fromInstant(localDate.atStartOfDay().toInstant(ZoneOffset.UTC), rawType);
    }

    private static Boolean activeFromStatus(Object statusValue) {
        if (statusValue == null) {
            return null;
        }
        String normalized = FieldGeneratorResolver.normalizeSemanticFieldName(statusValue.toString());
        if (Set.of("active", "enabled", "open").contains(normalized)) {
            return Boolean.TRUE;
        }
        if (Set.of("inactive", "disabled", "closed", "suspended", "archived", "deleted").contains(normalized)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static Object statusValueFor(boolean active, Class<?> rawType) {
        if (rawType == String.class) {
            return active ? "ACTIVE" : "INACTIVE";
        }
        if (!rawType.isEnum()) {
            return null;
        }
        Object[] constants = rawType.getEnumConstants();
        List<Object> matches = new ArrayList<>();
        for (Object constant : constants) {
            Boolean constantActive = activeFromStatus(constant);
            if (constantActive != null && constantActive == active) {
                matches.add(constant);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        matches.sort(Comparator.comparing(value -> FieldGeneratorResolver.normalizeSemanticFieldName(value.toString())));
        return matches.getFirst();
    }

    private static Object fromInstant(Instant instant, Class<?> rawType) {
        if (rawType == Instant.class) {
            return instant;
        }
        if (rawType == LocalDate.class) {
            return instant.atOffset(ZoneOffset.UTC).toLocalDate();
        }
        if (rawType == LocalDateTime.class) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (rawType == OffsetDateTime.class) {
            return instant.atOffset(ZoneOffset.UTC);
        }
        if (rawType == ZonedDateTime.class) {
            return instant.atZone(ZoneOffset.UTC);
        }
        if (rawType == java.util.Date.class) {
            return java.util.Date.from(instant);
        }
        if (rawType == java.sql.Date.class) {
            return java.sql.Date.valueOf(instant.atOffset(ZoneOffset.UTC).toLocalDate());
        }
        if (rawType == java.sql.Timestamp.class) {
            return java.sql.Timestamp.from(instant);
        }
        throw new ObjectGenerationException("Unsupported semantic timestamp type: " + rawType.getName());
    }

    private interface Slot {

        Class<?> ownerType();

        String fieldName();

        Class<?> rawType();

        AnnotatedElement annotatedElement();

        Object getValue();

        void setValue(Object value);
    }

    private static final class ReflectionSlot implements Slot {

        private final Class<?> ownerType;
        private final Field field;
        private final Object instance;
        private final boolean ignoreErrors;

        private ReflectionSlot(Class<?> ownerType, Field field, Object instance, boolean ignoreErrors) {
            this.ownerType = ownerType;
            this.field = field;
            this.instance = instance;
            this.ignoreErrors = ignoreErrors;
        }

        @Override
        public Class<?> ownerType() {
            return ownerType;
        }

        @Override
        public String fieldName() {
            return field.getName();
        }

        @Override
        public Class<?> rawType() {
            return field.getType();
        }

        @Override
        public AnnotatedElement annotatedElement() {
            return field;
        }

        @Override
        public Object getValue() {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new ObjectGenerationException(
                    "Could not read field '" + ownerType.getSimpleName() + "." + field.getName() + "'", e);
            }
        }

        @Override
        public void setValue(Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                if (!ignoreErrors) {
                    throw new ObjectGenerationException(
                        "Could not align field '" + ownerType.getSimpleName() + "." + field.getName() + "'", e);
                }
            }
        }
    }

    private static final class RecordSlot implements Slot {

        private final Class<?> ownerType;
        private final RecordComponent component;
        private final Field backingField;
        private final Object[] args;
        private final int index;

        private RecordSlot(Class<?> ownerType, RecordComponent component, Field backingField, Object[] args, int index) {
            this.ownerType = ownerType;
            this.component = component;
            this.backingField = backingField;
            this.args = args;
            this.index = index;
        }

        @Override
        public Class<?> ownerType() {
            return ownerType;
        }

        @Override
        public String fieldName() {
            return component.getName();
        }

        @Override
        public Class<?> rawType() {
            return component.getType();
        }

        @Override
        public AnnotatedElement annotatedElement() {
            return backingField;
        }

        @Override
        public Object getValue() {
            return args[index];
        }

        @Override
        public void setValue(Object value) {
            args[index] = value;
        }
    }
}
