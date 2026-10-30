/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator semantic coherence")
class ObjectGeneratorSemanticCoherenceTest {

    private static final Pattern RESERVED_EMAIL = Pattern.compile(".+@(?:example\\.(?:com|net|org)|.+\\.test)");

    @Test
    @DisplayName("mutable classes align related semantic fields")
    void mutableClassesAlignRelatedSemanticFields() {
        CoherentPerson value = new ObjectGenerator<>(CoherentPerson.class).generate();

        assertEquals(value.firstName + " " + value.lastName, value.fullName);
        assertTrue(value.email.startsWith(slug(value.firstName) + "." + slug(value.lastName) + "@"));
        assertEquals(value.domain, emailDomain(value.email));
        assertEquals("https://www." + value.domain, value.url);
        assertFalse(value.createdAt.toInstant(ZoneOffset.UTC).isAfter(value.updatedAt));
    }

    @Test
    @DisplayName("a blank domain field is backfilled from the email domain")
    void blankDomainFieldIsBackfilledFromEmail() {
        BlankDomainHolder value = new ObjectGenerator<>(BlankDomainHolder.class).generate();

        assertNotNull(value.email);
        assertEquals(emailDomain(value.email), value.domain);
    }

    @Test
    @DisplayName("an email-only type stays coherent without a domain field")
    void emailOnlyTypeStaysCoherentWithoutDomainField() {
        EmailOnly value = new ObjectGenerator<>(EmailOnly.class).generate();

        assertTrue(value.email().contains("@"));
    }

    @Test
    @DisplayName("an excluded blank domain field is left alone by coherence")
    void excludedDomainFieldIsLeftAlone() {
        GeneratorConfig config =
                GeneratorConfig.builder().objectExcludeField("domain").build();

        BlankDomainHolder value = new ObjectGenerator<>(BlankDomainHolder.class, config).generate();

        assertEquals("", value.domain);
        assertTrue(value.email.contains("@"));
    }

    @Test
    @DisplayName("a blank domain produced by an override is protected from coherence")
    void overriddenBlankDomainIsProtected() {
        // The override replaces the initializer with another blank value and marks the field protected,
        // so coherence resolves a domain from the email but must not write it into the domain field.
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(BlankDomainHolder.class, "domain", () -> "")
                .build();

        BlankDomainHolder value = new ObjectGenerator<>(BlankDomainHolder.class, config).generate();

        assertEquals("", value.domain);
        assertTrue(value.email.contains("@"));
    }

    @Test
    @DisplayName("a domain override replaces the blank initializer")
    void domainOverrideReplacesBlankInitializer() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(BlankDomainHolder.class, "domain", () -> "override.example")
                .build();

        BlankDomainHolder value = new ObjectGenerator<>(BlankDomainHolder.class, config).generate();

        assertEquals("override.example", value.domain);
        assertTrue(value.email.contains("@"));
    }

    @Test
    @DisplayName("records align related semantic fields")
    void recordsAlignRelatedSemanticFields() {
        CoherentPersonRecord value = new ObjectGenerator<>(CoherentPersonRecord.class).generate();

        assertEquals(value.firstName() + " " + value.lastName(), value.fullName());
        assertTrue(value.email().startsWith(slug(value.firstName()) + "." + slug(value.lastName()) + "@"));
        assertEquals(value.domain(), emailDomain(value.email()));
        assertEquals("https://www." + value.domain(), value.url());
        assertFalse(value.createdAt().toInstant(ZoneOffset.UTC).isAfter(value.updatedAt()));
    }

    @Test
    @DisplayName("explicit field overrides still win over semantic coherence")
    void explicitFieldOverridesStillWinOverSemanticCoherence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(CoherentPerson.class, "email", () -> "custom@example.org")
                .build();

        CoherentPerson value = new ObjectGenerator<>(CoherentPerson.class, config).generate();

        assertEquals("custom@example.org", value.email);
        assertEquals(value.firstName + " " + value.lastName, value.fullName);
    }

    @Test
    @DisplayName("relaxed mode keeps annotated email fields untouched during coherence")
    void relaxedModeKeepsAnnotatedEmailFieldsUntouchedDuringCoherence() {
        AnnotatedEmailPerson value = new ObjectGenerator<>(AnnotatedEmailPerson.class).generate();

        assertEquals("ANNOTATED", value.email);
        assertEquals(value.firstName + " " + value.lastName, value.fullName);
    }

    @Test
    @DisplayName("derived emails stay unique across one generator sequence")
    void derivedEmailsStayUniqueAcrossOneGeneratorSequence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(FixedNamePerson.class, "firstName", () -> "Alice")
                .override(FixedNamePerson.class, "lastName", () -> "Smith")
                .override(FixedNamePerson.class, "domain", () -> "example.com")
                .build();

        ObjectGenerator<FixedNamePerson> generator = new ObjectGenerator<>(FixedNamePerson.class, config);

        String first = generator.generate().email;
        String second = generator.generate().email;
        String third = generator.generate().email;

        assertEquals("alice.smith@example.com", first);
        assertEquals("alice.smith1@example.com", second);
        assertEquals("alice.smith2@example.com", third);
    }

    @Test
    @DisplayName("generated emails stay on reserved domains whatever their sibling fields")
    void generatedEmailsStayOnReservedDomainsWhateverTheirSiblingFields() {
        assertReservedEmails(CompanyNameEmail.class, CompanyNameEmail::companyEmail);
        assertReservedEmails(CompanyDomainEmail.class, CompanyDomainEmail::companyEmail);
        assertReservedEmails(CompanySite.class, CompanySite::companyEmail);
        assertReservedEmails(CompanyUrlEmail.class, CompanyUrlEmail::companyEmail);
        assertReservedEmails(PersonDomainEmail.class, PersonDomainEmail::email);
        assertReservedEmails(PersonUrlEmail.class, PersonUrlEmail::email);
        assertReservedEmails(PersonCompanyEmail.class, PersonCompanyEmail::email);
    }

    @Test
    @DisplayName("fields sharing a domain with an email field take the email's reserved domain")
    void fieldsSharingADomainWithAnEmailFieldTakeTheReservedDomain() {
        GeneratorConfig config = GeneratorConfig.builder().seed(61L).build();

        PersonDomainEmail person = new ObjectGenerator<>(PersonDomainEmail.class, config).generate();
        CompanyDomainEmail company = new ObjectGenerator<>(CompanyDomainEmail.class, config).generate();

        assertEquals(person.domain(), emailDomain(person.email()));
        assertEquals("hello@" + company.domain(), company.companyEmail());
        assertTrue(company.domain().endsWith(".test"), company.domain());
    }

    @Test
    @DisplayName("a domain without an email field keeps its generated value and its URL sibling")
    void aDomainWithoutAnEmailFieldKeepsItsGeneratedValue() {
        GeneratorConfig config = GeneratorConfig.builder().seed(61L).build();

        DomainWithoutEmail value = new ObjectGenerator<>(DomainWithoutEmail.class, config).generate();

        assertFalse(value.domain().endsWith(".test"), value.domain());
        assertEquals("https://www." + value.domain(), value.companyUrl());
    }

    @Test
    @DisplayName("an overridden domain is used verbatim for generated emails")
    void anOverriddenDomainIsUsedVerbatimForGeneratedEmails() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(PersonDomainEmail.class, "domain", () -> "corp.com")
                .build();

        PersonDomainEmail value = new ObjectGenerator<>(PersonDomainEmail.class, config).generate();

        assertEquals("corp.com", emailDomain(value.email()));
    }

    @Test
    @DisplayName("the realistic email policy derives ordinary company domains")
    void theRealisticEmailPolicyDerivesOrdinaryCompanyDomains() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(61L)
                .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                .build();

        CompanyNameEmail company = new ObjectGenerator<>(CompanyNameEmail.class, config).generate();
        PersonDomainEmail person = new ObjectGenerator<>(PersonDomainEmail.class, config).generate();

        assertTrue(company.companyEmail().startsWith("hello@"), company.companyEmail());
        assertTrue(company.companyEmail().endsWith(".com"), company.companyEmail());
        assertEquals(person.domain(), emailDomain(person.email()));
        assertFalse(RESERVED_EMAIL.matcher(person.email()).matches(), person.email());
    }

    @Test
    @DisplayName("an @Email field does not hand an ordinary domain to its siblings")
    void anEmailFieldDoesNotHandAnOrdinaryDomainToItsSiblings() {
        assertReservedEmails(ConstrainedEmails.class, ConstrainedEmails::email);
        assertReservedEmails(ConstrainedEmails.class, ConstrainedEmails::companyEmail);
    }

    @Test
    @DisplayName("a generated domain beside only a company email takes that email's domain")
    void aGeneratedDomainBesideOnlyACompanyEmailTakesThatEmailsDomain() {
        GeneratorConfig config = GeneratorConfig.builder().seed(61L).build();

        DomainCompanyEmail value = new ObjectGenerator<>(DomainCompanyEmail.class, config).generate();

        assertEquals(value.domain(), emailDomain(value.companyEmail()));
        assertTrue(RESERVED_EMAIL.matcher(value.companyEmail()).matches(), value.companyEmail());
    }

    @Test
    @DisplayName("a company name without an email field keeps deriving an ordinary domain")
    void aCompanyNameWithoutAnEmailFieldKeepsDerivingAnOrdinaryDomain() {
        GeneratorConfig config = GeneratorConfig.builder().seed(61L).build();

        CompanyWithoutEmail value = new ObjectGenerator<>(CompanyWithoutEmail.class, config).generate();

        assertTrue(value.companyUrl().endsWith(".com"), value.companyUrl());
    }

    @Test
    @DisplayName("an override that is not applied does not authorize a generated domain")
    void anOverrideThatIsNotAppliedDoesNotAuthorizeAGeneratedDomain() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(ChildWithCompanyEmail.class, "domain", () -> "corp.com")
                .build();

        ChildWithCompanyEmail value = new ObjectGenerator<>(ChildWithCompanyEmail.class, config).generate();

        assertTrue(RESERVED_EMAIL.matcher(value.companyEmail).matches(), value.companyEmail);
    }

    @Test
    @DisplayName("an override of a generic field's erased type does not authorize a generated domain")
    void anOverrideOfAGenericFieldsErasedTypeDoesNotAuthorizeAGeneratedDomain() {
        AtomicInteger invocations = new AtomicInteger();
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(Object.class, () -> {
                    invocations.incrementAndGet();
                    return "corp.com";
                })
                .build();

        ContactWithGenericDomain value = new ObjectGenerator<>(ContactWithGenericDomain.class, config).generate();

        assertEquals(0, invocations.get());
        assertTrue(RESERVED_EMAIL.matcher(value.companyEmail).matches(), value.companyEmail);
    }

    @Test
    @DisplayName("an override of a generic field itself is used verbatim for generated emails")
    void anOverrideOfAGenericFieldItselfIsUsedVerbatimForGeneratedEmails() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(GenericDomainHolder.class, "domain", () -> "corp.com")
                .build();

        ContactWithGenericDomain value = new ObjectGenerator<>(ContactWithGenericDomain.class, config).generate();

        assertEquals("hello@corp.com", value.companyEmail);
    }

    @Test
    @DisplayName("a unique domain stays reserved across retries, and its emails follow it")
    void aUniqueDomainStaysReservedAcrossRetries() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .seed(61L)
                        .objectUniqueField("domain")
                        .build())
                .override(CompanyDomainEmail.class, "companyName", () -> "Acme")
                .build();
        ObjectGenerator<CompanyDomainEmail> generator = new ObjectGenerator<>(CompanyDomainEmail.class, config);

        List<String> domains = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CompanyDomainEmail value = generator.generate();
            assertEquals("hello@" + value.domain(), value.companyEmail());
            domains.add(value.domain());
        }

        assertEquals(List.of("acme.test", "n1.acme.test", "n2.acme.test"), domains);
    }

    @Test
    @DisplayName("createdAt never lands after updatedAt across seeded sequences")
    void createdAtNeverLandsAfterUpdatedAtAcrossSeededSequences() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        ObjectGenerator<TemporalCoherenceHolder> generator =
                new ObjectGenerator<>(TemporalCoherenceHolder.class, config);

        for (int i = 0; i < 40; i++) {
            TemporalCoherenceHolder value = generator.generate();
            assertFalse(value.createdAt.toInstant(ZoneOffset.UTC).isAfter(value.updatedAt));
        }
    }

    @Test
    @DisplayName("strict mode can still overwrite annotated targets through semantic coherence")
    void strictModeCanStillOverwriteAnnotatedTargetsThroughSemanticCoherence() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                .build();

        AnnotatedEmailPerson value = new ObjectGenerator<>(AnnotatedEmailPerson.class, config).generate();

        assertNotEquals("ANNOTATED", value.email);
        assertTrue(value.email.startsWith(slug(value.firstName) + "." + slug(value.lastName) + "@"));
    }

    @Test
    @DisplayName("birthDate and age stay aligned")
    void birthDateAndAgeStayAligned() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        GeneratorConfig config =
                GeneratorConfig.builder().seed(53L).clock(clock).build();
        AgeCoherenceHolder value = new ObjectGenerator<>(AgeCoherenceHolder.class, config).generate();

        assertEquals((int) ChronoUnit.YEARS.between(value.dateOfBirth, LocalDate.now(clock)), value.ageYears);
    }

    @Test
    @DisplayName("birthDate is rebuilt from overridden age when age is protected")
    void birthDateIsRebuiltFromProtectedAge() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .clock(Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC")))
                        .build())
                .override(AgeCoherenceHolder.class, "ageYears", () -> 42)
                .build();

        AgeCoherenceHolder value = new ObjectGenerator<>(AgeCoherenceHolder.class, config).generate();

        assertEquals(42, value.ageYears);
        assertEquals(LocalDate.of(1984, 6, 4), value.dateOfBirth);
    }

    @Test
    @DisplayName("explicit age and birthDate overrides are both preserved")
    void explicitAgeAndBirthDateOverridesArePreserved() {
        LocalDate explicitBirthDate = LocalDate.of(2000, 1, 1);
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(AgeCoherenceHolder.class, "ageYears", () -> 42)
                .override(AgeCoherenceHolder.class, "dateOfBirth", () -> explicitBirthDate)
                .build();

        AgeCoherenceHolder value = new ObjectGenerator<>(AgeCoherenceHolder.class, config).generate();

        assertEquals(42, value.ageYears);
        assertEquals(explicitBirthDate, value.dateOfBirth);
    }

    @Test
    @DisplayName("active and string status stay aligned")
    void activeAndStringStatusStayAligned() {
        LifecycleCoherenceHolder value = new ObjectGenerator<>(LifecycleCoherenceHolder.class).generate();

        assertTrue(
                value.isEnabled
                        ? java.util.Set.of("ACTIVE", "ENABLED").contains(value.status)
                        : java.util.Set.of("INACTIVE", "DISABLED", "SUSPENDED").contains(value.status));
    }

    @Test
    @DisplayName("active and enum status stay aligned")
    void activeAndEnumStatusStayAligned() {
        EnumLifecycleCoherenceHolder value = new ObjectGenerator<>(EnumLifecycleCoherenceHolder.class).generate();

        assertEquals(value.isEnabled, value.status == LifecycleState.ACTIVE || value.status == LifecycleState.ENABLED);
    }

    @Test
    @DisplayName("locale-backed address fields align to one seeded coherent address cluster")
    void localeBackedAddressFieldsAlignToOneSeededCoherentAddressCluster() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.GERMANY).seed(17L).build();
        long generationSeed = new java.util.Random(17L).nextLong();
        AddressInfo expected = new AddressInfoGenerator(GeneratorConfig.builder()
                        .locale(Locale.GERMANY)
                        .seed(generationSeed)
                        .build())
                .generate();

        AddressCoherenceHolder value = new ObjectGenerator<>(AddressCoherenceHolder.class, config).generate();

        assertEquals(expected.city(), value.city);
        assertEquals(expected.state(), value.state);
        assertEquals(expected.zip(), value.postalCode);
        assertEquals(expected.country(), value.country);
    }

    @Test
    @DisplayName("explicit country overrides still win over address coherence")
    void explicitCountryOverridesStillWinOverAddressCoherence() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(AddressCoherenceHolder.class, "country", () -> "Canada")
                .build();

        AddressCoherenceHolder value = new ObjectGenerator<>(AddressCoherenceHolder.class, config).generate();

        assertEquals("Canada", value.country);
    }

    @Test
    @DisplayName("money fields stay ordered and string money fields derive formatted values")
    void moneyFieldsStayOrderedAndStringMoneyFieldsDeriveFormattedValues() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .override(StringMoneyHolder.class, "price", () -> "7.50")
                .override(StringMoneyHolder.class, "currencyCode", () -> "USD")
                .build();

        StringMoneyHolder stringMoney = new ObjectGenerator<>(StringMoneyHolder.class, config).generate();
        OrderedMoneyHolder orderedMoney = new ObjectGenerator<>(OrderedMoneyHolder.class).generate();

        assertEquals("7.50", stringMoney.price);
        assertEquals("USD 7.50", stringMoney.amount);
        assertEquals("USD 7.50", stringMoney.balance);

        assertNotNull(orderedMoney.price);
        assertNotNull(orderedMoney.amount);
        assertNotNull(orderedMoney.balance);
        assertTrue(orderedMoney.amount.compareTo(orderedMoney.price) >= 0);
        assertTrue(orderedMoney.balance.compareTo(orderedMoney.amount) >= 0);
    }

    private static String slug(String value) {
        StringBuilder normalized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch <= 127 && Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    private static String emailDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }

    private static <T> void assertReservedEmails(Class<T> type, Function<T, String> email) {
        ObjectGenerator<T> generator =
                new ObjectGenerator<>(type, GeneratorConfig.builder().seed(61L).build());
        for (int i = 0; i < 100; i++) {
            String value = email.apply(generator.generate());
            assertTrue(RESERVED_EMAIL.matcher(value).matches(), type.getSimpleName() + ": " + value);
        }
    }

    static class CoherentPerson {

        String firstName;
        String lastName;
        String fullName;
        String email;
        String domain;
        String url;
        LocalDateTime createdAt;
        Instant updatedAt;
    }

    record CoherentPersonRecord(
            String firstName,
            String lastName,
            String fullName,
            String email,
            String domain,
            String url,
            LocalDateTime createdAt,
            Instant updatedAt) {}

    static class FixedNamePerson {

        String firstName;
        String lastName;
        String domain;
        String email;
    }

    static class TemporalCoherenceHolder {

        LocalDateTime createdAt;
        Instant updatedAt;
    }

    static class AgeCoherenceHolder {

        LocalDate dateOfBirth;
        int ageYears;
    }

    static class LifecycleCoherenceHolder {

        boolean isEnabled;
        String status;
    }

    static class EnumLifecycleCoherenceHolder {

        boolean isEnabled;
        LifecycleState status;
    }

    static class AddressCoherenceHolder {

        String city;
        String state;
        String postalCode;
        String country;
    }

    static class StringMoneyHolder {

        String currencyCode;
        String price;
        String amount;
        String balance;
    }

    static class OrderedMoneyHolder {

        BigDecimal price;
        BigDecimal amount;
        BigDecimal balance;
    }

    static class AnnotatedEmailPerson {

        String firstName;
        String lastName;
        String fullName;

        @Randomizer(AnnotatedValueGenerator.class)
        String email;
    }

    public static class AnnotatedValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }

    enum LifecycleState {
        ACTIVE,
        ENABLED,
        INACTIVE,
        DISABLED,
        PENDING
    }

    static class BlankDomainHolder {

        String email;
        /** The safe construction policy keeps this initializer, so coherence sees a present but blank domain. */
        String domain = "";
    }

    record EmailOnly(String email) {}

    record CompanyNameEmail(String companyName, String companyEmail) {}

    record CompanyDomainEmail(String companyName, String domain, String companyEmail) {}

    record CompanySite(String companyName, String website, String url, String domain, String companyEmail) {}

    record CompanyUrlEmail(String companyUrl, String companyEmail) {}

    record PersonDomainEmail(String firstName, String lastName, String domain, String email) {}

    record PersonUrlEmail(String firstName, String lastName, String url, String email) {}

    record PersonCompanyEmail(String firstName, String lastName, String companyName, String email) {}

    record DomainWithoutEmail(String domain, String companyUrl) {}

    record ConstrainedEmails(@Email String email, String companyEmail) {}

    record DomainCompanyEmail(String domain, String companyEmail) {}

    record CompanyWithoutEmail(String companyName, String companyUrl) {}

    static class BaseWithDomain {

        String domain;
    }

    static class ChildWithCompanyEmail extends BaseWithDomain {

        String companyEmail;
    }

    static class GenericDomainHolder<T> {

        T domain;
    }

    static class ContactWithGenericDomain extends GenericDomainHolder<String> {

        String companyEmail;
    }
}
