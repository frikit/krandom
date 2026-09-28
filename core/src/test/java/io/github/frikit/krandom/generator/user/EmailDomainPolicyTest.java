/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.locale.LocaleDataBundle;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Email domain safety policy")
class EmailDomainPolicyTest {

    private static final Pattern RESERVED_EMAIL =
        Pattern.compile("^[a-z0-9]+(?:[._][a-z0-9]+)?@example\\.(?:com|net|org)$");
    private static final Pattern RESERVED_COMPANY_EMAIL =
        Pattern.compile("^[a-z0-9]+(?:\\.[a-z0-9]+)?@[a-z0-9]+\\.test$");
    private static final Set<String> MAILBOX_PROVIDERS = Set.of(
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com", "protonmail.com",
        "mail.com", "aol.com", "zoho.com", "gmx.com", "yandex.com", "qq.com");

    @Test
    @DisplayName("default policy emits reserved domains and ASCII local parts for every supported locale")
    void defaultPolicyUsesReservedDomainsAndAsciiLocalPartsForEverySupportedLocale() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder()
                                                                         .locale(supportedLocale.locale())
                                                                         .seed(11L)
                                                                         .build());
            for (int i = 0; i < 200; i++) {
                String email = generator.generate();
                assertTrue(RESERVED_EMAIL.matcher(email).matches(), supportedLocale + ": " + email);
            }
        }
    }

    @Test
    @DisplayName("convenience constructors inherit the reserved-domain default")
    void convenienceConstructorsUseReservedDomains() {
        assertTrue(RESERVED_EMAIL.matcher(new EmailGenerator().generate()).matches());
        assertTrue(RESERVED_EMAIL.matcher(new EmailGenerator(Locale.JAPAN).generate()).matches());
        assertTrue(RESERVED_EMAIL.matcher(new EmailGenerator().generate(EmailFormat.LASTNAME_DOT_FIRSTNAME)).matches());
        assertTrue(RESERVED_EMAIL.matcher(new EmailGenerator().generateUnique()).matches());
    }

    @Test
    @DisplayName("realistic policy restores popular mailbox-provider domains")
    void realisticPolicyRestoresMailboxProviderDomains() {
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder()
                                                                     .seed(3L)
                                                                     .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                                     .build());
        Set<String> domains = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            domains.add(domainOf(generator.generate()));
        }

        assertEquals(MAILBOX_PROVIDERS, domains);
    }

    @Test
    @DisplayName("free-provider methods fail closed under the reserved-domain default")
    void freeProviderMethodsFailClosedByDefault() {
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder().seed(3L).build());
        List<Executable> calls = List.of(generator::generateFreeEmail,
                                         generator::getFreeEmailProvider,
                                         generator::generateFreeEmailDomain);

        for (Executable call : calls) {
            IllegalStateException error = assertThrows(IllegalStateException.class, call);
            assertTrue(error.getMessage().contains("emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)"),
                       error.getMessage());
        }
    }

    @Test
    @DisplayName("free-provider methods return mailbox providers once realistic domains are opted in")
    void freeProviderMethodsWorkWhenRealisticDomainsAreOptedIn() {
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder()
                                                                     .seed(3L)
                                                                     .locale(Locale.GERMANY)
                                                                     .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                                     .build());

        assertTrue(List.of("gmx.de", "web.de", "gmail.com", "outlook.com").contains(domainOf(generator.generateFreeEmail())));
        assertTrue(List.of("gmx.de", "web.de", "gmail.com", "outlook.com").contains(generator.getFreeEmailProvider()));
        assertTrue(List.of("gmx.de", "web.de", "gmail.com", "outlook.com").contains(generator.generateFreeEmailDomain()));
    }

    @Test
    @DisplayName("safe and caller-supplied domains are unaffected by the policy")
    void safeAndCallerSuppliedDomainsIgnoreThePolicy() {
        EmailGenerator realistic = new EmailGenerator(GeneratorConfig.builder()
                                                                     .seed(4L)
                                                                     .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                                     .build());

        assertTrue(RESERVED_EMAIL.matcher(realistic.generateSafeEmail()).matches());
        assertTrue(realistic.generate("corp.test").endsWith("@corp.test"));
        assertTrue(new EmailGenerator().generateFromDomains("a.test", "b.test").matches("^[a-z0-9._]+@[ab]\\.test$"));
    }

    @Test
    @DisplayName("Latin-script names drop diacritics and German umlauts expand")
    void latinScriptNamesAreTransliterated() {
        assertEquals("juergen.mueller@example.com", dotEmail(Locale.GERMANY, "Jürgen", "Müller"));
        assertEquals("aenne.strasse@example.com", dotEmail(Locale.of("de", "AT"), "Änne", "Straße"));
        assertEquals("jose.garcia@example.com", dotEmail(Locale.of("es", "ES"), "José", "García"));
        assertEquals("francois.lefevre@example.com", dotEmail(Locale.FRANCE, "François", "Lefèvre"));
        assertEquals("lukasz.wojcik@example.com", dotEmail(Locale.of("pl", "PL"), "Łukasz", "Wójcik"));
        assertEquals("soren.aeroe@example.com", dotEmail(Locale.of("da", "DK"), "Søren", "Ærøe"));
        assertEquals("ilkay.gundogan@example.com", dotEmail(Locale.of("tr", "TR"), "İlkay", "Gündoğan"));
        assertEquals("zoe.oconnor@example.com", dotEmail(Locale.UK, "Zoë", "O'Connor"));
        assertEquals("dorde.dzeko@example.com", dotEmail(Locale.of("hr", "HR"), "Đorđe", "Džeko"));
    }

    @Test
    @DisplayName("Cyrillic and Greek names are transliterated to ASCII")
    void cyrillicAndGreekNamesAreTransliterated() {
        assertEquals("aleksandr.ivanov@example.com", dotEmail(Locale.of("ru", "RU"), "Александр", "Иванов"));
        assertEquals("yuliya.zhukova@example.com", dotEmail(Locale.of("ru", "RU"), "Юлия", "Жукова"));
        assertEquals("oleksandr.shevchenko@example.com", dotEmail(Locale.of("uk", "UA"), "Олександр", "Шевченко"));
        assertEquals("hryhorii.yizhakevych@example.com", dotEmail(Locale.of("uk", "UA"), "Григорій", "Їжакевич"));
        assertEquals("yevhen.kovalenko@example.com", dotEmail(Locale.of("uk", "UA"), "Євген", "Коваленко"));
        assertEquals("shtilyan.atanasov@example.com", dotEmail(Locale.of("bg", "BG"), "Щилян", "Атанасов"));
        assertEquals("aleksandar.bozhkov@example.com", dotEmail(Locale.of("bg", "BG"), "Александър", "Божков"));
        assertEquals("georgios.papadopoulos@example.com", dotEmail(Locale.of("el", "GR"), "Γεώργιος", "Παπαδόπουλος"));
        assertEquals("theodoros.christou@example.com", dotEmail(Locale.of("el", "GR"), "Θεόδωρος", "Χρήστου"));
        assertEquals("psychi.xanthi@example.com", dotEmail(Locale.of("el", "GR"), "Ψυχή", "Ξάνθη"));
        assertEquals("evangelos.avgi@example.com", dotEmail(Locale.of("el", "GR"), "Ευάγγελος", "Αυγή"));
        assertEquals("nataliia.yakovenko@example.com", dotEmail(Locale.of("uk", "UA"), "Наталія", "Яковенко"));
        assertEquals("yurii.yosypenko@example.com", dotEmail(Locale.of("uk", "UA"), "Юрій", "Йосипенко"));
        assertEquals("olha.kuziv@example.com", dotEmail(Locale.of("uk", "UA"), "Ольга", "Кузьів"));
        assertEquals("mariana.ivanenko@example.com", dotEmail(Locale.of("uk", "UA"), "Мар’яна", "Іваненко"));
        assertEquals("fedor.shchukin@example.com", dotEmail(Locale.of("ru", "RU"), "Фёдор", "Щукин"));
        assertEquals("hristo.hristov@example.com", dotEmail(Locale.of("bg", "BG"), "Христо", "Христов"));
        assertEquals("iurii.kuzmin@example.com", dotEmail(Locale.of("ru", "RU"), "Iurii", "Кузьмин"));
    }

    @Test
    @DisplayName("names with letters outside the supported transliterations are replaced, not truncated")
    void partiallyRomanizableNamesAreReplaced() {
        String serbian = dotEmail(Locale.of("sr", "RS"), "Ђорђе", "Jovanović");

        assertTrue(serbian.matches("^[a-z]+\\.jovanovic@example\\.com$"), serbian);
        assertFalse(serbian.startsWith("orde."), serbian);
    }

    @Test
    @DisplayName("names in scripts without a built-in transliteration use romanized locale names")
    void unromanizableScriptsUseRomanizedLocaleNames() {
        for (Locale locale : List.of(Locale.JAPAN, Locale.CHINA, Locale.KOREA, Locale.of("ar", "SA"),
                                     Locale.of("he", "IL"), Locale.of("hi", "IN"), Locale.of("th", "TH"),
                                     Locale.TAIWAN)) {
            EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder().locale(locale).seed(5L).build());
            for (int i = 0; i < 100; i++) {
                String email = generator.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME, "example.com");
                assertTrue(email.matches("^[a-z]+\\.[a-z]+@example\\.com$"), locale + ": " + email);
            }
        }
        assertTrue(List.of(LocaleTextResourceLoader.load("krandom/names/romanized/first/ja_JP.txt")).contains("yuki"));
        assertTrue(List.of(LocaleTextResourceLoader.load("krandom/names/romanized/last/ja_JP.txt")).contains("tanaka"));
        assertTrue(dotEmail(Locale.JAPAN, "翔", "佐藤").matches("^[a-z]+\\.[a-z]+@example\\.com$"));
        assertTrue(dotEmail(Locale.JAPAN, "Ken", "佐藤").matches("^[a-z]+\\.[a-z]+@example\\.com$"));
    }

    @Test
    @DisplayName("names that cannot be romanized for an unknown language fall back to English names")
    void unknownScriptFallsBackToEnglishNames() {
        Locale amharic = Locale.of("am", "ET");
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .registerLocaleData(LocaleDataBundle.builder(amharic)
                                                                                             .firstNames(new String[] { "አበበ" },
                                                                                                         new String[] { "ሄለን" })
                                                                                             .lastNames("በቀለ")
                                                                                             .build())
                                                         .build();
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder()
                                                                     .locale(amharic)
                                                                     .registryContext(context)
                                                                     .seed(8L)
                                                                     .build());

        for (int i = 0; i < 50; i++) {
            assertTrue(RESERVED_EMAIL.matcher(generator.generate()).matches());
        }
    }

    @Test
    @DisplayName("company emails use the reserved .test TLD by default and realistic TLDs only when opted in")
    void companyEmailsFollowThePolicy() {
        CompanyEmailGenerator reserved = new CompanyEmailGenerator(GeneratorConfig.builder().seed(9L).build());
        CompanyEmailGenerator realistic = new CompanyEmailGenerator(GeneratorConfig.builder()
                                                                                   .seed(9L)
                                                                                   .emailDomainPolicy(
                                                                                       EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                                                   .build());

        for (int i = 0; i < 200; i++) {
            String email = reserved.generate();
            assertTrue(RESERVED_COMPANY_EMAIL.matcher(email).matches(), email);
            assertFalse(realistic.generate().endsWith(".test"));
        }
        assertEquals("acme", domainOf(reserved.generate("Acme")).replace(".test", ""));
        assertTrue(new CompanyEmailGenerator().generate("Acme").endsWith("@acme.test"));
    }

    @Test
    @DisplayName("contact, person and profile emails follow the configured policy")
    void compositeEmailsFollowThePolicy() {
        for (Locale locale : List.of(Locale.US, Locale.JAPAN, Locale.of("ru", "RU"))) {
            GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(12L).build();
            ContactInfoGenerator contacts = new ContactInfoGenerator(config);
            PersonInfoGenerator people = new PersonInfoGenerator(config);
            for (int i = 0; i < 50; i++) {
                assertTrue(RESERVED_EMAIL.matcher(contacts.generate().email()).matches(), locale.toString());
                assertTrue(RESERVED_EMAIL.matcher(people.generate().contact().email()).matches(), locale.toString());
            }
        }

        ContactInfoGenerator realistic = new ContactInfoGenerator(GeneratorConfig.builder()
                                                                                 .seed(12L)
                                                                                 .emailDomainPolicy(
                                                                                     EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                                                 .build());
        assertTrue(MAILBOX_PROVIDERS.contains(domainOf(realistic.generate().email())));
    }

    private static String dotEmail(Locale locale, String firstName, String lastName) {
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .registerLocaleData(LocaleDataBundle.builder(locale)
                                                                                             .firstNames(new String[] { firstName },
                                                                                                         new String[] { firstName })
                                                                                             .lastNames(lastName)
                                                                                             .build())
                                                         .build();
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder()
                                                                     .locale(locale)
                                                                     .registryContext(context)
                                                                     .seed(1L)
                                                                     .build());
        return generator.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME, "example.com");
    }

    private static String domainOf(String email) {
        return email.substring(email.indexOf('@') + 1);
    }
}
