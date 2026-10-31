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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    private static GeneratorConfig.Builder reservedNames() {
        return GeneratorConfig.builder().seed(61L).networkSafetyPolicy(NetworkSafetyPolicy.TEST_SAFE_RESERVED);
    }

    private static boolean isReservedUrl(String url) {
        return url.matches("[a-z]+://(?:[a-z0-9]+\\.)*[a-z0-9]+\\.test(?:[/?].*)?");
    }

    @Test
    @DisplayName("the reserved network policy derives .test domains and URLs from a generated domain")
    void theReservedNetworkPolicyDerivesReservedUrlsFromAGeneratedDomain() {
        DomainWithoutEmail value =
                new ObjectGenerator<>(DomainWithoutEmail.class, reservedNames().build()).generate();

        assertTrue(value.domain().matches("[a-z]+\\.test"), value.domain());
        assertEquals("https://www." + value.domain(), value.companyUrl());
    }

    @Test
    @DisplayName("the reserved network policy derives .test from a company name, with or without an email field")
    void theReservedNetworkPolicyDerivesTestFromACompanyName() {
        GeneratorConfig config = reservedNames()
                .objectOverride(CompanyWithoutEmail.class, "companyName", () -> "Acme Labs")
                .objectOverride(CompanyNameEmail.class, "companyName", () -> "Acme Labs")
                .build();

        assertEquals(
                "https://www.acmelabs.test",
                new ObjectGenerator<>(CompanyWithoutEmail.class, config)
                        .generate()
                        .companyUrl());
        assertEquals(
                "hello@acmelabs.test",
                new ObjectGenerator<>(CompanyNameEmail.class, config).generate().companyEmail());
    }

    @Test
    @DisplayName("the reserved network policy shares a generated domain with an email field")
    void theReservedNetworkPolicySharesAGeneratedDomainWithAnEmailField() {
        PersonDomainEmail value =
                new ObjectGenerator<>(PersonDomainEmail.class, reservedNames().build()).generate();

        assertTrue(value.domain().endsWith(".test"), value.domain());
        assertEquals(value.domain(), emailDomain(value.email()));
    }

    @Test
    @DisplayName("a realistic email does not lend its domain to a URL under the reserved network policy")
    void aRealisticEmailDoesNotLendItsDomainToAUrl() {
        GeneratorConfig config = reservedNames()
                .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                .build();

        PersonUrlEmail person = new ObjectGenerator<>(PersonUrlEmail.class, config).generate();
        assertFalse(RESERVED_EMAIL.matcher(person.email()).matches(), person.email());
        assertTrue(isReservedUrl(person.url()), person.url());

        CompanyUrlEmail company = new ObjectGenerator<>(CompanyUrlEmail.class, config).generate();
        assertFalse(RESERVED_EMAIL.matcher(company.companyEmail()).matches(), company.companyEmail());
        assertTrue(isReservedUrl(company.companyUrl()), company.companyUrl());

        BlankDomainHolder holder = new ObjectGenerator<>(BlankDomainHolder.class, config).generate();
        assertFalse(RESERVED_EMAIL.matcher(holder.email).matches(), holder.email);
        assertEquals("", holder.domain);

        CompanyEmailBlankUrl blankUrl = new ObjectGenerator<>(CompanyEmailBlankUrl.class, config).generate();
        assertFalse(RESERVED_EMAIL.matcher(blankUrl.companyEmail).matches(), blankUrl.companyEmail);
        assertEquals("", blankUrl.companyUrl);
    }

    @Test
    @DisplayName("an overridden domain or URL is a supplied destination, and its siblings may share it")
    void anOverriddenDomainOrUrlIsASuppliedDestination() {
        GeneratorConfig domainOverride = reservedNames()
                .objectOverride(DomainWithoutEmail.class, "domain", () -> "corp.com")
                .build();
        assertEquals(
                "https://www.corp.com",
                new ObjectGenerator<>(DomainWithoutEmail.class, domainOverride)
                        .generate()
                        .companyUrl());

        GeneratorConfig urlOverride = reservedNames()
                .objectOverride(DomainAndUrl.class, "url", () -> "https://corp.com/x")
                .build();
        DomainAndUrl value = new ObjectGenerator<>(DomainAndUrl.class, urlOverride).generate();
        assertEquals("https://corp.com/x", value.url());
        assertTrue(value.domain().endsWith(".test"), value.domain());
    }

    @Test
    @DisplayName("an overridden email or a class's own domain does not choose a destination for a generated URL")
    void anOverriddenEmailOrInitializerDoesNotChooseADestination() {
        GeneratorConfig emailOverride = reservedNames()
                .objectOverride(PersonUrlEmail.class, "email", () -> "jane@corp.com")
                .build();
        PersonUrlEmail person = new ObjectGenerator<>(PersonUrlEmail.class, emailOverride).generate();
        assertEquals("jane@corp.com", person.email());
        assertTrue(isReservedUrl(person.url()), person.url());

        GeneratorConfig fragmentOverride = reservedNames()
                .objectOverride(PersonUrlEmail.class, "email", () -> "jane@real.com#.test")
                .build();
        PersonUrlEmail fragment = new ObjectGenerator<>(PersonUrlEmail.class, fragmentOverride).generate();
        assertEquals("jane@real.com#.test", fragment.email());
        assertTrue(isReservedUrl(fragment.url()), fragment.url());

        PresetOrdinaryDomain preset = new ObjectGenerator<>(
                        PresetOrdinaryDomain.class, reservedNames().build())
                .generate();
        assertEquals("corp.com", preset.domain);
        assertTrue(isReservedUrl(preset.url), preset.url);
        assertTrue(isReservedUrl(preset.companyUrl), preset.companyUrl);
    }

    @Test
    @DisplayName("a unique URL stays on reserved names across retries, by numbering a subdomain")
    void aUniqueUrlStaysOnReservedNamesAcrossRetries() {
        GeneratorConfig urls = reservedNames()
                .objectUniqueField("url")
                .objectOverride(DomainAndUrl.class, "domain", () -> "acme.test")
                .build();
        ObjectGenerator<DomainAndUrl> urlGenerator = new ObjectGenerator<>(DomainAndUrl.class, urls);
        assertEquals(
                List.of("https://www.acme.test", "https://n1.www.acme.test", "https://n2.www.acme.test"),
                List.of(
                        urlGenerator.generate().url(),
                        urlGenerator.generate().url(),
                        urlGenerator.generate().url()));

        GeneratorConfig companyUrls = reservedNames()
                .objectUniqueField("companyUrl")
                .objectOverride(DomainWithoutEmail.class, "domain", () -> "acme.test")
                .build();
        ObjectGenerator<DomainWithoutEmail> companyUrlGenerator =
                new ObjectGenerator<>(DomainWithoutEmail.class, companyUrls);
        assertEquals(
                List.of("https://www.acme.test", "https://n1.www.acme.test"),
                List.of(
                        companyUrlGenerator.generate().companyUrl(),
                        companyUrlGenerator.generate().companyUrl()));

        GeneratorConfig fullNames = reservedNames()
                .objectUniqueField("fullName")
                .objectOverride(NamePair.class, "firstName", () -> "Ada")
                .objectOverride(NamePair.class, "lastName", () -> "Lovelace")
                .build();
        ObjectGenerator<NamePair> nameGenerator = new ObjectGenerator<>(NamePair.class, fullNames);
        assertEquals(
                List.of("Ada Lovelace", "Ada Lovelace1"),
                List.of(
                        nameGenerator.generate().fullName(),
                        nameGenerator.generate().fullName()));
    }

    @Test
    @DisplayName("the realistic network policy keeps its numbering of a unique URL")
    void theRealisticNetworkPolicyKeepsItsNumberingOfAUniqueUrl() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(61L)
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .objectUniqueField("url")
                .objectOverride(DomainAndUrl.class, "domain", () -> "acme.com")
                .build();
        ObjectGenerator<DomainAndUrl> generator = new ObjectGenerator<>(DomainAndUrl.class, config);

        assertEquals(
                List.of("https://www.acme.com", "https://www.acme.com1"),
                List.of(generator.generate().url(), generator.generate().url()));
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
    @DisplayName("two Instants on one day can differ, and createdAt stays first")
    void twoInstantsOnOneDayCanDifferAndCreatedAtStaysFirst() {
        LocalDate day = LocalDate.of(2024, 2, 29);
        boolean differed = false;

        for (long seed = 0; seed < 200; seed++) {
            GeneratorConfig config = GeneratorConfig.builder()
                    .seed(seed)
                    .objectDateRange(day, day)
                    .build();
            InstantToInstant value = new ObjectGenerator<>(InstantToInstant.class, config).generate();
            assertFalse(value.createdAt().isAfter(value.updatedAt()), value.toString());
            differed |= value.createdAt().isBefore(value.updatedAt());
        }

        assertTrue(differed, "two instants on one day must be able to differ");
    }

    @Test
    @DisplayName(
            "an Instant with a time of day keeps createdAt not after updatedAt, for each type that can hold the time")
    void instantWithATimeOfDayKeepsCreatedAtNotAfterUpdatedAt() {
        // java.util.Date holds milliseconds, so a finer Instant would stay after an updatedAt aligned with it.
        assertCreatedAtNotAfterUpdatedAt(
                InstantToUtilDate.class, InstantToUtilDate::createdAt, InstantToUtilDate::updatedAt);
        assertCreatedAtNotAfterUpdatedAt(
                InstantToTimestamp.class, InstantToTimestamp::createdAt, InstantToTimestamp::updatedAt);
        assertCreatedAtNotAfterUpdatedAt(
                InstantToLocalDateTime.class, InstantToLocalDateTime::createdAt, InstantToLocalDateTime::updatedAt);
        assertCreatedAtNotAfterUpdatedAt(
                InstantToOffsetDateTime.class, InstantToOffsetDateTime::createdAt, InstantToOffsetDateTime::updatedAt);
        assertCreatedAtNotAfterUpdatedAt(
                InstantToZonedDateTime.class, InstantToZonedDateTime::createdAt, InstantToZonedDateTime::updatedAt);
        assertCreatedAtNotAfterUpdatedAt(
                UtilDateToInstant.class, UtilDateToInstant::createdAt, UtilDateToInstant::updatedAt);
    }

    private static <T> void assertCreatedAtNotAfterUpdatedAt(
            Class<T> type, Function<T, Object> createdAt, Function<T, Object> updatedAt) {
        // One day, so that the order depends on the time of day.
        LocalDate day = LocalDate.of(2024, 2, 29);
        for (long seed = 0; seed < 500; seed++) {
            GeneratorConfig config = GeneratorConfig.builder()
                    .seed(seed)
                    .objectDateRange(day, day)
                    .build();
            T value = new ObjectGenerator<>(type, config).generate();
            Instant created = SemanticCoherenceAdjuster.toInstant(createdAt.apply(value));
            Instant updated = SemanticCoherenceAdjuster.toInstant(updatedAt.apply(value));
            assertFalse(created.isAfter(updated), type.getSimpleName() + " seed " + seed + ": " + value);
        }
    }

    @Test
    @DisplayName("an Instant createdAt stays on or before the day of an updatedAt that holds only a date")
    void instantCreatedAtStaysOnOrBeforeADateOnlyUpdatedAt() {
        for (long seed = 0; seed < 500; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();
            InstantToLocalDate local = new ObjectGenerator<>(InstantToLocalDate.class, config).generate();
            InstantToSqlDate sql = new ObjectGenerator<>(InstantToSqlDate.class, config).generate();

            assertFalse(
                    local.createdAt().atOffset(ZoneOffset.UTC).toLocalDate().isAfter(local.updatedAt()),
                    local.toString());
            assertFalse(
                    sql.createdAt()
                            .atOffset(ZoneOffset.UTC)
                            .toLocalDate()
                            .isAfter(sql.updatedAt().toLocalDate()),
                    sql.toString());
        }
    }

    @Test
    @DisplayName("an Instant aligned with a date-only sibling, or derived from a protected age, is at midnight UTC")
    void alignedAndDerivedInstantsCanBeAtMidnight() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        boolean aligned = false;

        for (long seed = 0; seed < 200; seed++) {
            GeneratorConfig config =
                    GeneratorConfig.builder().seed(seed).clock(clock).build();
            DateToInstant value = new ObjectGenerator<>(DateToInstant.class, config).generate();
            if (value.updatedAt().equals(value.createdAt().atStartOfDay().toInstant(ZoneOffset.UTC))) {
                aligned = true;
            } else {
                assertTrue(
                        value.updatedAt()
                                .isAfter(value.createdAt().atStartOfDay().toInstant(ZoneOffset.UTC)),
                        value.toString());
            }
        }
        assertTrue(aligned, "some updatedAt must have been aligned with the date-only createdAt");

        ObjectGeneratorConfig protectedAge = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder().clock(clock).build())
                .override(InstantBirthDate.class, "age", () -> 45)
                .build();
        assertEquals(
                Instant.parse("1981-06-04T00:00:00Z"),
                new ObjectGenerator<>(InstantBirthDate.class, protectedAge).generate().birthDate);
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

    @Test
    @DisplayName("a derived email's numbered values run out at objectUniquenessMaxAttempts, then generation fails")
    void derivedEmailFailsWhenItsNumberedValuesRunOut() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        ObjectGenerator<FixedNamePerson> generator = new ObjectGenerator<>(
                FixedNamePerson.class,
                fixedNames(
                        FixedNamePerson.class,
                        GeneratorConfig.builder()
                                .seed(61L)
                                .objectUniquenessMaxAttempts(3)
                                .generationFailureListener(diagnostics::add)));

        assertEquals("alice.smith@example.com", generator.generate().email);
        assertEquals("alice.smith1@example.com", generator.generate().email);
        assertEquals("alice.smith2@example.com", generator.generate().email);
        assertTrue(diagnostics.isEmpty());

        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);

        GenerationFailureContext context = error.getContext().orElseThrow();
        assertEquals("FixedNamePerson.email", context.path());
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals(GenerationOperation.ALIGN_SEMANTICS, context.operation());
        assertEquals(FixedNamePerson.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertEquals(0, context.depth());
        assertEquals(
                "Could not generate a unique value at 'FixedNamePerson.email' (declared type java.lang.String, depth 0)",
                error.getMessage());
        assertTrue(error.getCause().getMessage().contains("FixedNamePerson.email' after 3 attempts"));
        assertEquals(1, diagnostics.size());
        assertEquals(context, diagnostics.getFirst().context());
        assertTrue(diagnostics.getFirst().causeType().endsWith("UniqueFieldTracker$ExhaustedException"));
    }

    @Test
    @DisplayName("a record and a nested object's derived values fail the same way")
    void derivedValueFailureInRecordsAndNestedObjects() {
        GeneratorConfig.Builder strict = GeneratorConfig.builder().seed(61L).objectUniquenessMaxAttempts(1);

        ObjectGenerator<FixedNameRecord> records =
                new ObjectGenerator<>(FixedNameRecord.class, fixedNames(FixedNameRecord.class, strict));
        assertEquals("alice.smith@example.com", records.generate().email());
        assertEquals(
                "FixedNameRecord.email",
                assertThrows(ObjectGenerationException.class, records::generate)
                        .getContext()
                        .orElseThrow()
                        .path());

        ObjectGenerator<FixedNameTeam> teams =
                new ObjectGenerator<>(FixedNameTeam.class, fixedNames(FixedNameRecord.class, strict));
        assertEquals("alice.smith@example.com", teams.generate().lead().email());
        ObjectGenerationException nested = assertThrows(ObjectGenerationException.class, teams::generate);
        assertEquals(
                "FixedNameTeam.lead.email", nested.getContext().orElseThrow().path());
        assertTrue(nested.getCause().getMessage().contains("FixedNameRecord.email' after 1 attempts"));
    }

    @Test
    @DisplayName("objectIgnoreErrors leaves an exhausted derived value null and tells the listener")
    void lenientModeLeavesExhaustedDerivedValueNull() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig.Builder lenient = GeneratorConfig.builder()
                .seed(61L)
                .objectUniquenessMaxAttempts(1)
                .objectIgnoreErrors(true)
                .generationFailureListener(diagnostics::add);

        ObjectGenerator<FixedNamePerson> people =
                new ObjectGenerator<>(FixedNamePerson.class, fixedNames(FixedNamePerson.class, lenient));
        assertEquals("alice.smith@example.com", people.generate().email);
        assertNull(people.generate().email);

        ObjectGenerator<FixedNameRecord> records =
                new ObjectGenerator<>(FixedNameRecord.class, fixedNames(FixedNameRecord.class, lenient));
        assertEquals("alice.smith@example.com", records.generate().email());
        assertNull(records.generate().email());

        ObjectGenerator<FixedNameTeam> teams =
                new ObjectGenerator<>(FixedNameTeam.class, fixedNames(FixedNameRecord.class, lenient));
        assertEquals("alice.smith@example.com", teams.generate().lead().email());
        assertNull(teams.generate().lead().email());

        assertEquals(
                List.of("FixedNamePerson.email", "FixedNameRecord.email", "FixedNameTeam.lead.email"),
                diagnostics.stream()
                        .map(diagnostic -> diagnostic.context().path())
                        .toList());
    }

    @Test
    @DisplayName("an exhausted unique domain stays null, and the email beside it stays on a reserved domain")
    void exhaustedUniqueDomainKeepsEmailOnReservedDomain() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .seed(61L)
                        .objectUniqueField("domain")
                        .objectUniquenessMaxAttempts(2)
                        .objectIgnoreErrors(true)
                        .build())
                .override(CompanyDomainEmail.class, "companyName", () -> "Acme")
                .build();
        ObjectGenerator<CompanyDomainEmail> generator = new ObjectGenerator<>(CompanyDomainEmail.class, config);

        assertEquals("acme.test", generator.generate().domain());
        assertEquals("n1.acme.test", generator.generate().domain());

        CompanyDomainEmail exhausted = generator.generate();

        assertNull(exhausted.domain());
        assertTrue(RESERVED_EMAIL.matcher(exhausted.companyEmail()).matches(), exhausted.companyEmail());
    }

    @Test
    @DisplayName("a derived value the field already holds is kept, so it is not numbered or counted twice")
    void derivedValueTheFieldAlreadyHoldsIsKept() {
        ObjectGenerator<DomainOnly> domains = new ObjectGenerator<>(
                DomainOnly.class,
                GeneratorConfig.builder()
                        .seed(61L)
                        .objectUniqueField("domain")
                        .objectUniquenessMaxAttempts(1)
                        .build());
        assertFalse(domains.generate().domain().startsWith("n1."));

        ObjectGenerator<PatternedName> names = new ObjectGenerator<>(
                PatternedName.class,
                ObjectGeneratorConfig.builder()
                        .generatorConfig(GeneratorConfig.builder()
                                .seed(61L)
                                .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                .objectUniqueField("fullName")
                                .objectUniquenessMaxAttempts(1)
                                .build())
                        .override(PatternedName.class, "firstName", () -> "Alice")
                        .override(PatternedName.class, "lastName", () -> "Smith")
                        .build());
        assertEquals("Alice Smith", names.generate().fullName());
        assertEquals(
                GenerationOperation.GENERATE,
                assertThrows(ObjectGenerationException.class, names::generate)
                        .getContext()
                        .orElseThrow()
                        .operation());
    }

    @Test
    @DisplayName("generation and coherence draw on one set of values for a field")
    void generationAndCoherenceShareOneFieldsValues() {
        for (long seed = 1; seed <= 10; seed++) {
            ObjectGenerator<TwoNames> generator = new ObjectGenerator<>(
                    TwoNames.class,
                    ObjectGeneratorConfig.builder()
                            .generatorConfig(GeneratorConfig.builder()
                                    .seed(seed)
                                    .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                    .objectUniqueField("fullName")
                                    .build())
                            .override(TwoNames.class, "firstName", () -> "Alice")
                            .override(TwoNames.class, "lastName", () -> "Smith")
                            .build());

            List<String> names = new ArrayList<>();
            assertThrows(ObjectGenerationException.class, () -> {
                for (int i = 0; i < 10; i++) {
                    names.add(generator.generate().fullName());
                }
            });

            assertEquals(names.size(), new HashSet<>(names).size(), names.toString());
        }
    }

    @Test
    @DisplayName("a field initializer the pass would derive again stays as the class set it, unchecked")
    void keptInitializerStaysUnchecked() {
        ObjectGenerator<PresetDomain> generator = new ObjectGenerator<>(
                PresetDomain.class,
                GeneratorConfig.builder().objectUniqueField("domain").build());

        for (int i = 0; i < 3; i++) {
            assertEquals("acme.test", generator.generate().domain);
        }
    }

    @Test
    @DisplayName("a seeded coherence failure carries the replay recipe")
    void seededCoherenceFailureCarriesReplayRecipe() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        ObjectGenerator<AnnotatedNames> generator = new ObjectGenerator<>(
                AnnotatedNames.class,
                GeneratorConfig.builder()
                        .seed("replayable")
                        .objectUniquenessMaxAttempts(1)
                        .generationFailureListener(diagnostics::add)
                        .build());

        // Annotations keep the names fixed and the recipe portable. An email can only differ by its
        // generated domain, so it soon repeats, and one attempt leaves no room to number it.
        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, () -> {
            for (int i = 0; i < 100; i++) {
                generator.generate();
            }
        });

        assertEquals(
                GenerationOperation.ALIGN_SEMANTICS,
                error.getContext().orElseThrow().operation());
        assertTrue(diagnostics.getFirst().replayIdentity().orElseThrow().contains("format=krandom-recipe"));
    }

    @Test
    @DisplayName("coherence does not overwrite a unique timestamp with a value that repeats")
    void coherenceKeepsUniqueTimestamps() {
        LocalDateTime later = LocalDateTime.of(2999, 1, 1, 0, 0);
        LocalDateTime earlier = LocalDateTime.of(1900, 1, 1, 0, 0);

        assertDistinct(
                Lifecycle.class,
                uniqueIn("updatedAt")
                        .override(Lifecycle.class, "createdAt", () -> later)
                        .build(),
                Lifecycle::updatedAt);
        assertDistinct(
                Lifecycle.class,
                uniqueIn("createdAt")
                        .override(Lifecycle.class, "updatedAt", () -> earlier)
                        .build(),
                Lifecycle::createdAt);
    }

    @Test
    @DisplayName("the other timestamp adapts to a unique one")
    void otherTimestampAdaptsToUniqueOne() {
        ObjectGenerator<Lifecycle> generator =
                new ObjectGenerator<>(Lifecycle.class, uniqueIn("updatedAt").build());

        for (int i = 0; i < 40; i++) {
            Lifecycle value = generator.generate();
            assertFalse(value.createdAt().isAfter(value.updatedAt()));
        }
    }

    @Test
    @DisplayName("coherence does not overwrite a unique age or birth date with a value that repeats")
    void coherenceKeepsUniqueAgeAndBirthDate() {
        assertDistinct(
                Lifespan.class,
                uniqueIn("age")
                        .override(Lifespan.class, "birthDate", () -> LocalDate.of(1990, 1, 1))
                        .build(),
                Lifespan::age);
        assertDistinct(
                Lifespan.class,
                uniqueIn("birthDate").override(Lifespan.class, "age", () -> 30).build(),
                Lifespan::birthDate);
    }

    @Test
    @DisplayName("coherence still derives an alias of a requested name, which then repeats with no failure")
    void coherenceDerivesAnAliasOfARequestedName() {
        ObjectGenerator<AliasedLifespan> generator = new ObjectGenerator<>(
                AliasedLifespan.class,
                uniqueIn("age")
                        .override(AliasedLifespan.class, "birthDate", () -> LocalDate.of(1990, 1, 1))
                        .build());

        assertEquals(
                1,
                generator.generateList(10).stream()
                        .map(AliasedLifespan::yearsOld)
                        .distinct()
                        .count());
    }

    @Test
    @DisplayName("a default name still numbers a derived value, and no names at all leaves it to repeat")
    void defaultNameNumbersADerivedValueAndNoNamesRepeatIt() {
        ObjectGenerator<FixedNamePerson> numbered = new ObjectGenerator<>(
                FixedNamePerson.class,
                fixedNames(FixedNamePerson.class, GeneratorConfig.builder().seed(61L)));
        assertEquals("alice.smith@example.com", numbered.generate().email);
        assertEquals("alice.smith1@example.com", numbered.generate().email);

        ObjectGenerator<FixedNamePerson> repeating = new ObjectGenerator<>(
                FixedNamePerson.class,
                fixedNames(
                        FixedNamePerson.class,
                        GeneratorConfig.builder().seed(61L).objectUniqueFields()));
        assertEquals("alice.smith@example.com", repeating.generate().email);
        assertEquals("alice.smith@example.com", repeating.generate().email);
    }

    @Test
    @DisplayName("the birth date adapts to a unique age")
    void birthDateAdaptsToUniqueAge() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("UTC"));
        ObjectGenerator<Lifespan> generator = new ObjectGenerator<>(
                Lifespan.class,
                GeneratorConfig.builder()
                        .seed(61L)
                        .clock(clock)
                        .objectUniqueField("age")
                        .build());

        for (int i = 0; i < 10; i++) {
            Lifespan value = generator.generate();
            assertEquals(LocalDate.of(2026, 6, 4).minusYears(value.age()), value.birthDate());
        }
    }

    @Test
    @DisplayName("coherence does not overwrite a unique amount with a value that repeats")
    void coherenceKeepsUniqueMoney() {
        BigDecimal low = BigDecimal.ZERO;
        BigDecimal high = new BigDecimal("99999999.00");

        assertDistinct(
                Charge.class,
                uniqueIn("price").override(Charge.class, "amount", () -> low).build(),
                Charge::price);
        assertDistinct(
                Charge.class,
                uniqueIn("amount").override(Charge.class, "price", () -> high).build(),
                Charge::amount);
        assertDistinct(
                Charge.class,
                uniqueIn("balance").override(Charge.class, "amount", () -> high).build(),
                Charge::balance);
    }

    @Test
    @DisplayName("coherence does not overwrite a unique status or active flag with a value that repeats")
    void coherenceKeepsUniqueStatusAndActiveFlag() {
        ObjectGenerator<EnumAccount> statuses = new ObjectGenerator<>(
                EnumAccount.class,
                uniqueIn("status")
                        .override(EnumAccount.class, "active", () -> true)
                        .build());
        assertEquals(
                Set.of(LifecycleState.values()),
                statuses.generateList(LifecycleState.values().length).stream()
                        .map(EnumAccount::status)
                        .collect(Collectors.toSet()));
        assertEquals(
                "EnumAccount.status",
                assertThrows(ObjectGenerationException.class, statuses::generate)
                        .getContext()
                        .orElseThrow()
                        .path());

        ObjectGenerator<TextAccount> flags = new ObjectGenerator<>(
                TextAccount.class,
                uniqueIn("active")
                        .override(TextAccount.class, "status", () -> "ACTIVE")
                        .build());
        assertEquals(
                Set.of(true, false),
                flags.generateList(2).stream().map(TextAccount::active).collect(Collectors.toSet()));
        assertEquals(
                "TextAccount.active",
                assertThrows(ObjectGenerationException.class, flags::generate)
                        .getContext()
                        .orElseThrow()
                        .path());
    }

    @Test
    @DisplayName("the active flag adapts to a unique status")
    void activeFlagAdaptsToUniqueStatus() {
        ObjectGenerator<EnumAccount> generator =
                new ObjectGenerator<>(EnumAccount.class, uniqueIn("status").build());

        Set<LifecycleState> statuses = new HashSet<>();
        for (int i = 0; i < LifecycleState.values().length; i++) {
            EnumAccount value = generator.generate();
            statuses.add(value.status());
            Boolean expected = SemanticCoherenceAdjuster.activeFromStatus(value.status());
            if (expected != null) {
                assertEquals(expected, value.active());
            }
        }
        assertEquals(Set.of(LifecycleState.values()), statuses);
    }

    private static ObjectGeneratorConfig fixedNames(Class<?> owner, GeneratorConfig.Builder base) {
        return ObjectGeneratorConfig.builder()
                .generatorConfig(base.build())
                .override(owner, "firstName", () -> "Alice")
                .override(owner, "lastName", () -> "Smith")
                .override(owner, "domain", () -> "example.com")
                .build();
    }

    private static ObjectGeneratorConfig.Builder uniqueIn(String fieldName) {
        return ObjectGeneratorConfig.builder()
                .generatorConfig(GeneratorConfig.builder()
                        .seed(61L)
                        .objectUniqueField(fieldName)
                        .build());
    }

    /** A sibling pinned by an override would give every object the same derived value. */
    private static <T> void assertDistinct(Class<T> type, ObjectGeneratorConfig config, Function<T, Object> field) {
        ObjectGenerator<T> generator = new ObjectGenerator<>(type, config);
        Set<Object> values = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            values.add(field.apply(generator.generate()));
        }
        assertEquals(10, values.size());
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

    record DateToInstant(LocalDate createdAt, Instant updatedAt) {}

    static class InstantBirthDate {

        Instant birthDate;
        int age;
    }

    record InstantToInstant(Instant createdAt, Instant updatedAt) {}

    record InstantToUtilDate(Instant createdAt, java.util.Date updatedAt) {}

    record InstantToTimestamp(Instant createdAt, java.sql.Timestamp updatedAt) {}

    record InstantToLocalDateTime(Instant createdAt, LocalDateTime updatedAt) {}

    record InstantToOffsetDateTime(Instant createdAt, OffsetDateTime updatedAt) {}

    record InstantToZonedDateTime(Instant createdAt, ZonedDateTime updatedAt) {}

    record UtilDateToInstant(java.util.Date createdAt, Instant updatedAt) {}

    record InstantToLocalDate(Instant createdAt, LocalDate updatedAt) {}

    record InstantToSqlDate(Instant createdAt, java.sql.Date updatedAt) {}

    static class FixedNamePerson {

        String firstName;
        String lastName;
        String domain;
        String email;
    }

    record FixedNameRecord(String firstName, String lastName, String domain, String email) {}

    record FixedNameTeam(FixedNameRecord lead) {}

    record AnnotatedNames(
            @Randomizer(AnnotatedValueGenerator.class) String firstName,
            @Randomizer(AnnotatedValueGenerator.class) String lastName,
            String email) {}

    record DomainOnly(String domain) {}

    record PatternedName(
            String firstName,
            String lastName,

            @jakarta.validation.constraints.Pattern(regexp = "Alice Smith") String fullName) {}

    record TwoNames(
            String firstName,
            String lastName,

            @jakarta.validation.constraints.Pattern(regexp = "Alice Smith|Bob Jones") String fullName) {}

    static class PresetDomain {

        String domain = "acme.test";
    }

    record Lifecycle(LocalDateTime createdAt, LocalDateTime updatedAt) {}

    record Lifespan(LocalDate birthDate, int age) {}

    record AliasedLifespan(LocalDate birthDate, int yearsOld) {}

    record Charge(BigDecimal price, BigDecimal amount, BigDecimal balance) {}

    record EnumAccount(boolean active, LifecycleState status) {}

    record TextAccount(boolean active, String status) {}

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

    record DomainAndUrl(String domain, String url) {}

    record NamePair(String firstName, String lastName, String fullName) {}

    static class PresetOrdinaryDomain {

        String domain = "corp.com";
        String url;
        String companyUrl;
    }

    static class CompanyEmailBlankUrl {

        String companyEmail;
        /** Stays blank: its only sibling's domain is an ordinary one, which a generated URL may not take. */
        String companyUrl = "";
    }

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
