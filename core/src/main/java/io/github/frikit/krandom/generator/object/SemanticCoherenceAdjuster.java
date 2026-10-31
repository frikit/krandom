/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureListener;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Clock;
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
import org.jspecify.annotations.Nullable;

/**
 * Applies lightweight sibling-field coherence after structural value generation.
 */
final class SemanticCoherenceAdjuster {

    private static final Pattern MONEY_FRAGMENT = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
    /** The domains RFC 2606 reserves, which cannot reach a real mailbox. */
    private static final Pattern RESERVED_DOMAIN =
            Pattern.compile("(?:.+\\.)?example\\.(?:com|net|org)|.+\\.(?:test|example|invalid|localhost)");
    /** The fields this pass derives a value for that it cannot number apart from one already used. */
    private static final Set<String> UNNUMBERED_KEYS =
            Set.of("createdat", "updatedat", "birthdate", "age", "price", "amount", "balance", "active", "status");

    private final ObjectGeneratorConfig config;
    private final UniqueFieldTracker uniqueFieldTracker;
    private final SemanticFieldRegistry semanticRegistry;
    private final @Nullable Long generationSeed;
    private final int depth;
    private final String objectPath;
    private @Nullable AddressInfo addressInfo;
    private boolean addressInfoResolved;

    SemanticCoherenceAdjuster(ObjectGeneratorConfig config, UniqueFieldTracker uniqueFieldTracker) {
        this(config, uniqueFieldTracker, null);
    }

    SemanticCoherenceAdjuster(
            ObjectGeneratorConfig config, UniqueFieldTracker uniqueFieldTracker, @Nullable Long generationSeed) {
        this(config, uniqueFieldTracker, generationSeed, 0, "$root");
    }

    SemanticCoherenceAdjuster(
            ObjectGeneratorConfig config,
            UniqueFieldTracker uniqueFieldTracker,
            @Nullable Long generationSeed,
            int depth,
            String objectPath) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.uniqueFieldTracker = Objects.requireNonNull(uniqueFieldTracker, "uniqueFieldTracker must not be null");
        this.semanticRegistry = config.getSemanticRegistry();
        this.generationSeed = generationSeed;
        this.depth = depth;
        this.objectPath = Objects.requireNonNull(objectPath, "objectPath must not be null");
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
            slots.add(new ReflectionSlot(
                    ownerType,
                    field,
                    instance,
                    config.isIgnoreErrors(),
                    depth,
                    config.getGeneratorConfig().getGenerationFailureListener()));
        }
        apply(slots, allowOverwriteExisting);
    }

    void adjustRecordArguments(
            Class<?> ownerType, RecordComponent[] components, Field[] backingFields, @Nullable Object[] args) {
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
            String semanticKey = semanticRegistry.semanticKeyForFieldName(slot.fieldName());
            if (semanticKey != null) {
                slotsBySemanticKey.putIfAbsent(semanticKey, slot);
            }
        }
        if (slotsBySemanticKey.isEmpty()) {
            return;
        }

        harmonizeAddressCluster(slotsBySemanticKey, allowOverwriteExisting);
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

    private void harmonizeAddressCluster(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        boolean hasAddressSignals = stringValue(slotsBySemanticKey.get("streetaddress")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("city")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("state")) != null;
        hasAddressSignals |= stringValue(slotsBySemanticKey.get("postalcode")) != null;
        if (!hasAddressSignals) {
            return;
        }

        AddressInfo coherentAddress = coherentAddressInfo();
        if (coherentAddress == null) {
            return;
        }

        assignAddressField(
                slotsBySemanticKey.get("streetaddress"),
                "streetaddress",
                coherentAddress.street(),
                allowOverwriteExisting);
        assignAddressField(slotsBySemanticKey.get("city"), "city", coherentAddress.city(), allowOverwriteExisting);
        assignAddressField(slotsBySemanticKey.get("state"), "state", coherentAddress.state(), allowOverwriteExisting);
        assignAddressField(
                slotsBySemanticKey.get("postalcode"), "postalcode", coherentAddress.zip(), allowOverwriteExisting);
        assignAddressField(
                slotsBySemanticKey.get("country"), "country", coherentAddress.country(), allowOverwriteExisting);
    }

    private void assignAddressField(
            @Nullable Slot slot, String semanticKey, String value, boolean allowOverwriteExisting) {
        if (slot == null || slot.rawType() != String.class || !canAssign(slot, allowOverwriteExisting)) {
            return;
        }
        String normalized = stringValue(value);
        if (normalized == null) {
            return;
        }
        slot.setValue(applyUniqueness(slot, semanticKey, normalized));
    }

    private @Nullable AddressInfo coherentAddressInfo() {
        if (addressInfoResolved) {
            return addressInfo;
        }
        addressInfoResolved = true;
        if (localeCurrentCountry() == null) {
            return null;
        }
        GeneratorConfig addressConfig = config.getGeneratorConfig();
        if (generationSeed != null) {
            addressConfig =
                    addressConfig.toBuilder().seed(generationSeed.longValue()).build();
        }
        addressInfo = new AddressInfoGenerator(addressConfig).generate();
        return addressInfo;
    }

    private @Nullable String harmonizeDomain(Map<String, Slot> slotsBySemanticKey, boolean allowOverwriteExisting) {
        Slot domainSlot = slotsBySemanticKey.get("domain");
        if (domainSlot != null) {
            String declaredDomain =
                    sharedDomain(normalizeDomain(stringValue(domainSlot.getValue())), slotsBySemanticKey);
            if (declaredDomain != null) {
                if (canAssign(domainSlot, allowOverwriteExisting)) {
                    Object assigned = applyUniqueness(domainSlot, "domain", declaredDomain);
                    domainSlot.setValue(assigned);
                    return normalizeDomain(stringValue(assigned));
                }
                return declaredDomain;
            }
        }

        String resolvedDomain = normalizeDomain(emailDomain(stringValue(slotsBySemanticKey.get("email"))));
        if (resolvedDomain == null) {
            resolvedDomain = sharedDomain(
                    normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("url")))), slotsBySemanticKey);
        }
        if (resolvedDomain == null) {
            resolvedDomain = normalizeDomain(
                    domainFromCompany(stringValue(slotsBySemanticKey.get("companyname")), slotsBySemanticKey));
        }
        if (resolvedDomain == null) {
            resolvedDomain = normalizeDomain(emailDomain(stringValue(slotsBySemanticKey.get("companyemail"))));
        }

        if (resolvedDomain != null && domainSlot != null && canAssign(domainSlot, allowOverwriteExisting)) {
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

    private void harmonizeEmail(
            Map<String, Slot> slotsBySemanticKey, @Nullable String resolvedDomain, boolean allowOverwriteExisting) {
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
            domain = emailSafeDomain(
                    normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("url")))), slotsBySemanticKey);
        }
        if (domain == null) {
            domain = "example.com";
        }

        emailSlot.setValue(applyUniqueness(emailSlot, "email", localPart + "@" + domain));
    }

    private void harmonizeCompanyEmail(
            Map<String, Slot> slotsBySemanticKey, @Nullable String resolvedDomain, boolean allowOverwriteExisting) {
        Slot companyEmailSlot = slotsBySemanticKey.get("companyemail");
        if (companyEmailSlot == null || !canAssign(companyEmailSlot, allowOverwriteExisting)) {
            return;
        }

        String domain = resolvedDomain;
        if (domain == null) {
            domain = normalizeDomain(emailDomain(stringValue(companyEmailSlot)));
        }
        if (domain == null) {
            domain = emailSafeDomain(
                    normalizeDomain(urlHost(stringValue(slotsBySemanticKey.get("companyurl")))), slotsBySemanticKey);
        }
        if (domain == null) {
            domain = normalizeDomain(
                    domainFromCompany(stringValue(slotsBySemanticKey.get("companyname")), slotsBySemanticKey));
        }
        if (domain == null) {
            return;
        }

        companyEmailSlot.setValue(applyUniqueness(companyEmailSlot, "companyemail", "hello@" + domain));
    }

    /** Keeps a domain out of a fixture's shared domain when an email field would inherit it unsafely. */
    private @Nullable String sharedDomain(@Nullable String domain, Map<String, Slot> slotsBySemanticKey) {
        return hasEmailField(slotsBySemanticKey) ? emailSafeDomain(domain, slotsBySemanticKey) : domain;
    }

    private static boolean hasEmailField(Map<String, Slot> slotsBySemanticKey) {
        return slotsBySemanticKey.containsKey("email") || slotsBySemanticKey.containsKey("companyemail");
    }

    /**
     * Returns the domain if a generated email may use it. Under the test-safe policy it must be
     * reserved or come from an override of a sibling field, because a domain the caller supplies is
     * used verbatim.
     */
    private @Nullable String emailSafeDomain(@Nullable String domain, Map<String, Slot> slotsBySemanticKey) {
        if (domain == null
                || !usesReservedEmailDomains()
                || RESERVED_DOMAIN.matcher(domain).matches()
                || isSuppliedDomain(domain, slotsBySemanticKey)) {
            return domain;
        }
        return null;
    }

    private boolean usesReservedEmailDomains() {
        return config.getGeneratorConfig().getEmailDomainPolicy() == EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS;
    }

    private boolean isSuppliedDomain(String domain, Map<String, Slot> slotsBySemanticKey) {
        return domain.equals(normalizeDomain(suppliedValue(slotsBySemanticKey.get("domain"))))
                || domain.equals(normalizeDomain(urlHost(suppliedValue(slotsBySemanticKey.get("url")))))
                || domain.equals(normalizeDomain(urlHost(suppliedValue(slotsBySemanticKey.get("companyurl")))));
    }

    private @Nullable String suppliedValue(@Nullable Slot slot) {
        // Not type overrides: one on String protects every email too, leaving nothing to authorize, and
        // one on a generic field's erasure does not apply to the value generation resolved for it.
        return slot != null && hasMemberOverride(slot) ? stringValue(slot) : null;
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

    private void harmonizeCompanyUrl(
            Map<String, Slot> slotsBySemanticKey, @Nullable String resolvedDomain, boolean allowOverwriteExisting) {
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
            domain = normalizeDomain(
                    domainFromCompany(stringValue(slotsBySemanticKey.get("companyname")), slotsBySemanticKey));
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
            assignConverted(updatedAtSlot, fromInstant(createdAt, updatedAtSlot.rawType()));
            return;
        }
        if (canAssign(createdAtSlot, allowOverwriteExisting)) {
            assignConverted(createdAtSlot, fromInstant(updatedAt, createdAtSlot.rawType()));
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
            if (age != null && isProtected(ageSlot)) {
                if (canAssign(birthDateSlot, allowOverwriteExisting)) {
                    assignConverted(birthDateSlot, fromLocalDate(today().minusYears(age), birthDateSlot.rawType()));
                }
                return;
            }
            int derivedAge = ageFromBirthDate(birthDate);
            if (age != null && age == derivedAge) {
                return;
            }
            if (canAssign(ageSlot, allowOverwriteExisting)) {
                assignConverted(ageSlot, fromAge(derivedAge, ageSlot.rawType()));
                return;
            }
            return;
        }

        if (age != null && canAssign(birthDateSlot, allowOverwriteExisting)) {
            assignConverted(birthDateSlot, fromLocalDate(today().minusYears(age), birthDateSlot.rawType()));
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

    private boolean canAssign(@Nullable Slot slot, boolean allowOverwriteExisting) {
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
        Field field = slot.annotatedElement();
        // An inherited field is also protected by an override registered for the generated type,
        // although field overrides are resolved under the declaring class.
        if (hasFieldOverride(slot.ownerType(), slot.fieldName())
                || hasMemberOverride(slot)
                || hasTypeOverride(slot)
                || keepsUniqueValue(slot)) {
            return true;
        }

        // RELAXED lets declared annotations and bean validation constraints take precedence over
        // semantics; STRICT deliberately lets semantics win.
        if (config.getSemanticMode() == ObjectGenerationSemanticMode.RELAXED) {
            if (field.isAnnotationPresent(Randomizer.class) || field.isAnnotationPresent(FakeRange.class)) {
                return true;
            }
            return BeanValidationSupport.constraintGeneratorFor(field, slot.rawType()) != null;
        }
        return false;
    }

    /**
     * A derived value that could repeat must not replace the value generation tracked for a requested
     * name, so that field keeps it and the sibling adapts where it can.
     */
    private boolean keepsUniqueValue(Slot slot) {
        // A slot only reaches this pass through its semantic key.
        String semanticKey = Objects.requireNonNull(
                semanticRegistry.semanticKeyForFieldName(slot.fieldName()), "semanticKey must not be null");
        return UNNUMBERED_KEYS.contains(semanticKey) && config.isRequestedUniqueField(slot.fieldName());
    }

    /** True when an override of this field, not a generator or a constraint, produced its value. */
    private boolean hasMemberOverride(Slot slot) {
        Field field = slot.annotatedElement();
        return hasFieldOverride(field.getDeclaringClass(), slot.fieldName())
                || config.getContextualFieldPredicateOverride(field).isPresent()
                || config.getFieldPredicateOverride(field).isPresent();
    }

    private boolean hasTypeOverride(Slot slot) {
        return config.getContextualTypeOverride(slot.rawType()).isPresent()
                || config.getTypeOverride(slot.rawType()).isPresent();
    }

    private boolean hasFieldOverride(Class<?> ownerType, String fieldName) {
        return config.getContextualFieldOverride(ownerType, fieldName).isPresent()
                || config.getFieldOverride(ownerType, fieldName).isPresent();
    }

    @Nullable Object applyUniqueness(Slot slot, String semanticKey, @Nullable String candidate) {
        // Generation tracked a value it gave the field, and a value the class set itself is not checked.
        if (candidate == null || !isUniqueField(slot.fieldName(), semanticKey) || candidate.equals(slot.getValue())) {
            return candidate;
        }

        AtomicInteger attempt = new AtomicInteger();
        String fieldKey = UniqueFieldTracker.fieldKey(slot.annotatedElement().getDeclaringClass(), slot.fieldName());
        try {
            return uniqueFieldTracker.nextUnique(
                    fieldKey,
                    () -> "domain".equals(semanticKey)
                            ? uniquifyDomain(candidate, attempt.getAndIncrement())
                            : uniquifyString(candidate, attempt.getAndIncrement()),
                    config.getUniquenessMaxAttempts());
        } catch (UniqueFieldTracker.ExhaustedException exhausted) {
            return handleExhaustedUniqueField(slot, exhausted);
        }
    }

    /**
     * The numbered values of a derived field ran out. Lenient generation leaves the member {@code null}
     * rather than keep its generated value, which may be a domain no generated email can take.
     */
    private @Nullable Object handleExhaustedUniqueField(Slot slot, UniqueFieldTracker.ExhaustedException cause) {
        String path = objectPath + "." + slot.fieldName();
        String declaredType = slot.annotatedElement().getGenericType().getTypeName();
        GenerationFailureContext context = new GenerationFailureContext(
                GenerationFailureCategory.UNSUPPORTED_TYPE,
                GenerationOperation.ALIGN_SEMANTICS,
                path,
                slot.ownerType(),
                declaredType,
                depth,
                -1);
        GeneratorConfig generatorConfig = config.getGeneratorConfig();
        return new ObjectGenerationFailurePolicy(
                        config.isIgnoreErrors(),
                        generatorConfig.getGenerationFailureListener(),
                        () -> generatorConfig.getGenerationRecipe().map(GenerationRecipe::serializeForDiagnostics))
                .handle(
                        new ObjectGenerationException(
                                "Could not generate a unique value at '" + path + "' (declared type " + declaredType
                                        + ", depth " + depth + ")",
                                context,
                                cause),
                        null);
    }

    /** Numbers a subdomain, so that a reserved domain stays reserved: {@code n1.acme.test}. */
    static String uniquifyDomain(String domain, int attempt) {
        return attempt == 0 ? domain : "n" + attempt + "." + domain;
    }

    boolean isUniqueField(String fieldName, String semanticKey) {
        String normalizedFieldName = FieldGeneratorResolver.normalizeSemanticFieldName(fieldName);
        Set<String> uniqueFieldNames = config.getUniqueFieldNames();
        if (uniqueFieldNames.contains(normalizedFieldName) || uniqueFieldNames.contains(semanticKey)) {
            return true;
        }
        for (String alias : semanticRegistry.semanticAliasesFor(semanticKey)) {
            if (uniqueFieldNames.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    static String uniquifyString(String candidate, int attempt) {
        if (attempt == 0) {
            return candidate;
        }
        int atIndex = candidate.indexOf('@');
        if (atIndex > 0) {
            return candidate.substring(0, atIndex) + attempt + candidate.substring(atIndex);
        }
        return candidate + attempt;
    }

    static @Nullable String emailLocalPart(Map<String, Slot> slotsBySemanticKey) {
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

    private @Nullable String domainFromCompany(@Nullable String companyName, Map<String, Slot> slotsBySemanticKey) {
        String slug = slugFragment(companyName);
        if (slug == null) {
            return null;
        }
        return slug + (usesReservedEmailDomains() && hasEmailField(slotsBySemanticKey) ? ".test" : ".com");
    }

    private static @Nullable String normalizedHumanName(@Nullable String value) {
        String trimmed = stringValue(value);
        return trimmed == null ? null : trimmed.trim();
    }

    static @Nullable String slugFragment(@Nullable String value) {
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

    static @Nullable String normalizeDomain(@Nullable String candidate) {
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

    static @Nullable String emailDomain(@Nullable String email) {
        String trimmed = stringValue(email);
        if (trimmed == null) {
            return null;
        }
        int atIndex = trimmed.indexOf('@');
        return atIndex >= 0 && atIndex + 1 < trimmed.length() ? trimmed.substring(atIndex + 1) : null;
    }

    static @Nullable String urlHost(@Nullable String url) {
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

    private static @Nullable String stringValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static @Nullable String stringValue(@Nullable Slot slot) {
        return slot == null ? null : stringValue(slot.getValue());
    }

    static @Nullable Instant toInstant(@Nullable Object value) {
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
        // java.sql.Date and java.sql.Time throw UnsupportedOperationException from toInstant().
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (value instanceof java.sql.Time) {
            return null;
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        return null;
    }

    static @Nullable LocalDate toLocalDate(@Nullable Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        Instant instant = toInstant(value);
        return instant != null ? instant.atOffset(ZoneOffset.UTC).toLocalDate() : null;
    }

    static @Nullable BigDecimal moneyValue(@Nullable Slot slot) {
        return slot == null ? null : moneyValue(slot.getValue());
    }

    static @Nullable BigDecimal moneyValue(@Nullable Object value) {
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

    static @Nullable Integer toInteger(@Nullable Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Long longValue && longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
            return longValue.intValue();
        }
        if (value instanceof Short shortValue) {
            return shortValue.intValue();
        }
        if (value instanceof Byte byteValue) {
            return byteValue.intValue();
        }
        if (value instanceof Double || value instanceof Float) {
            // Whole years, like the ages fromAge writes; NaN and out-of-range values have none.
            double number = ((Number) value).doubleValue();
            return number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE ? (int) number : null;
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

    private @Nullable String localeCurrentCountry() {
        Locale locale = config.getGeneratorConfig().getLocale();
        boolean localeHasNoCountry = locale.getCountry().isBlank();
        boolean localeRegistered =
                config.getGeneratorConfig().getRegistryContext().isRegistered(Vocabulary.COUNTRY, locale);
        if (localeHasNoCountry | !localeRegistered) {
            return null;
        }
        return new CountryGenerator(config.getGeneratorConfig()).currentCountry();
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.abs().setScale(2, RoundingMode.HALF_UP);
    }

    static @Nullable String currencyCode(@Nullable Slot slot) {
        return slot == null ? null : currencyCode(slot.getValue());
    }

    static @Nullable String currencyCode(@Nullable Object value) {
        if (value instanceof io.github.frikit.krandom.generator.finance.Currency currency) {
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
        io.github.frikit.krandom.generator.finance.Currency libraryCurrency =
                io.github.frikit.krandom.generator.finance.Currency.fromCode(normalized);
        if (libraryCurrency != null) {
            return libraryCurrency.getCode();
        }
        try {
            return java.util.Currency.getInstance(normalized).getCurrencyCode();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static @Nullable Object moneyValueFor(BigDecimal value, Class<?> rawType, @Nullable String currencyCode) {
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

    static @Nullable BigDecimal assignMoney(
            @Nullable Slot slot, @Nullable BigDecimal value, @Nullable String currencyCode) {
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

    static @Nullable Boolean toBoolean(@Nullable Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    boolean shouldFormatMoneyString(@Nullable BigDecimal value, @Nullable Slot slot, boolean allowOverwriteExisting) {
        if (value == null || slot == null) {
            return false;
        }
        if (slot.rawType() != String.class) {
            return false;
        }
        return canAssign(slot, allowOverwriteExisting);
    }

    static boolean isLessThan(@Nullable BigDecimal left, @Nullable BigDecimal right) {
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) < 0;
    }

    private int ageFromBirthDate(LocalDate birthDate) {
        return Math.toIntExact(ChronoUnit.YEARS.between(birthDate, today()));
    }

    private LocalDate today() {
        Clock clock = config.getGeneratorConfig().getClock();
        return LocalDate.now(clock);
    }

    static @Nullable Object fromAge(int age, Class<?> rawType) {
        if (rawType == int.class || rawType == Integer.class) {
            return age;
        }
        if (rawType == long.class || rawType == Long.class) {
            return (long) age;
        }
        // A narrow field keeps its generated value rather than wrapping an age it cannot hold.
        if (rawType == short.class || rawType == Short.class) {
            return age >= Short.MIN_VALUE && age <= Short.MAX_VALUE ? Short.valueOf((short) age) : null;
        }
        if (rawType == byte.class || rawType == Byte.class) {
            return age >= Byte.MIN_VALUE && age <= Byte.MAX_VALUE ? Byte.valueOf((byte) age) : null;
        }
        if (rawType == float.class || rawType == Float.class) {
            return (float) age;
        }
        if (rawType == double.class || rawType == Double.class) {
            return (double) age;
        }
        if (rawType == String.class) {
            return Integer.toString(age);
        }
        // The coherence pass cannot represent this age type; the generated value is kept.
        return null;
    }

    private static @Nullable Object fromLocalDate(LocalDate localDate, Class<?> rawType) {
        return fromInstant(localDate.atStartOfDay().toInstant(ZoneOffset.UTC), rawType);
    }

    static @Nullable Boolean activeFromStatus(@Nullable Object statusValue) {
        if (statusValue == null) {
            return null;
        }
        String normalized = FieldGeneratorResolver.normalizeSemanticFieldName(statusValue.toString());
        if (Set.of("active", "enabled", "open").contains(normalized)) {
            return Boolean.TRUE;
        }
        if (Set.of("inactive", "disabled", "closed", "suspended", "archived", "deleted")
                .contains(normalized)) {
            return Boolean.FALSE;
        }
        return null;
    }

    static @Nullable Object statusValueFor(boolean active, Class<?> rawType) {
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
        matches.sort(
                Comparator.comparing(value -> FieldGeneratorResolver.normalizeSemanticFieldName(value.toString())));
        return matches.getFirst();
    }

    static @Nullable Object fromInstant(Instant instant, Class<?> rawType) {
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
        // The coherence pass cannot represent this timestamp type; the generated value is kept.
        return null;
    }

    private static void assignConverted(Slot slot, @Nullable Object converted) {
        if (converted != null) {
            slot.setValue(converted);
        }
    }

    interface Slot {

        Class<?> ownerType();

        String fieldName();

        Class<?> rawType();

        Field annotatedElement();

        @Nullable Object getValue();

        void setValue(@Nullable Object value);
    }

    static final class ReflectionSlot implements Slot {

        private final Class<?> ownerType;
        private final Field field;
        private final Object instance;
        private final ObjectGenerationFailurePolicy failurePolicy;
        private final int depth;

        ReflectionSlot(Class<?> ownerType, Field field, Object instance, boolean ignoreErrors) {
            this(ownerType, field, instance, ignoreErrors, 0);
        }

        ReflectionSlot(Class<?> ownerType, Field field, Object instance, boolean ignoreErrors, int depth) {
            this(ownerType, field, instance, ignoreErrors, depth, diagnostic -> {});
        }

        ReflectionSlot(
                Class<?> ownerType,
                Field field,
                Object instance,
                boolean ignoreErrors,
                int depth,
                GenerationFailureListener listener) {
            this.ownerType = ownerType;
            this.field = field;
            this.instance = instance;
            this.failurePolicy = new ObjectGenerationFailurePolicy(ignoreErrors, listener);
            this.depth = depth;
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
        public Field annotatedElement() {
            return field;
        }

        @Override
        public @Nullable Object getValue() {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                GenerationFailureContext context =
                        failureContext(GenerationFailureCategory.REFLECTION, GenerationOperation.READ);
                return failurePolicy.handle(
                        new ObjectGenerationException(
                                "Could not read semantic field at '" + context.path() + "' (declared type "
                                        + context.declaredType() + ", depth " + depth + ")",
                                context,
                                e),
                        null);
            }
        }

        @Override
        public void setValue(@Nullable Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                GenerationFailureContext context =
                        failureContext(GenerationFailureCategory.ASSIGNMENT, GenerationOperation.ALIGN_SEMANTICS);
                failurePolicy.handle(
                        new ObjectGenerationException(
                                "Could not align semantic field at '" + context.path() + "' (declared type "
                                        + context.declaredType() + ", depth " + depth + ")",
                                context,
                                e),
                        null);
            }
        }

        private GenerationFailureContext failureContext(
                GenerationFailureCategory category, GenerationOperation operation) {
            return new GenerationFailureContext(
                    category,
                    operation,
                    ownerType.getSimpleName() + "." + field.getName(),
                    ownerType,
                    field.getGenericType().getTypeName(),
                    depth,
                    -1);
        }
    }

    private static final class RecordSlot implements Slot {

        private final Class<?> ownerType;
        private final RecordComponent component;
        private final Field backingField;
        private final @Nullable Object[] args;
        private final int index;

        private RecordSlot(
                Class<?> ownerType, RecordComponent component, Field backingField, @Nullable Object[] args, int index) {
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
        public Field annotatedElement() {
            return backingField;
        }

        @Override
        public @Nullable Object getValue() {
            return args[index];
        }

        @Override
        public void setValue(@Nullable Object value) {
            args[index] = value;
        }
    }
}
