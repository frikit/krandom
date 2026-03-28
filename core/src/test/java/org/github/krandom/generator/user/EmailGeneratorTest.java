/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailGeneratorTest {

    private EmailGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new EmailGenerator();
    }

    // Constructor Tests

    @Test
    void testDefaultConstructor() {
        assertNotNull(generator);
        assertNotNull(generator.generate());
    }

    @Test
    void testLocaleConstructor() {
        EmailGenerator gen = new EmailGenerator(Locale.US);
        assertNotNull(gen);
        assertNotNull(gen.generate());
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        EmailGenerator gen = new EmailGenerator(config);
        assertNotNull(gen);
        assertNotNull(gen.generate());
    }

    @Test
    void testNullConfig() {
        assertThrows(NullPointerException.class,
                     () -> new EmailGenerator((GeneratorConfig) null));
    }

    @Test
    void testNullLocale() {
        assertThrows(NullPointerException.class,
                     () -> new EmailGenerator((Locale) null));
    }

    // Basic Generation Tests

    @Test
    void testGenerate() {
        String email = generator.generate();
        assertNotNull(email);
        assertTrue(email.contains("@"), "Email should contain @");
        assertTrue(email.matches(".+@.+\\..+"), "Email should match basic format");
    }

    @Test
    void testGenerateMultiple() {
        for (int i = 0; i < 100; i++) {
            String email = generator.generate();
            assertNotNull(email);
            assertTrue(email.contains("@"));
            assertValidEmail(email);
        }
    }

    @Test
    void testGenerateVariety() {
        Set<String> emails = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            emails.add(generator.generate());
        }
        assertTrue(emails.size() > 100, "Should generate variety of emails");
    }

    // Domain Tests

    @Test
    void testGenerateWithCustomDomain() {
        String email = generator.generate("example.com");
        assertNotNull(email);
        assertTrue(email.endsWith("@example.com"), "Email should use custom domain");
    }

    @Test
    void testGenerateWithMultipleCustomDomains() {
        String email1 = generator.generate("company.com");
        String email2 = generator.generate("test.org");
        String email3 = generator.generate("example.net");

        assertTrue(email1.endsWith("@company.com"));
        assertTrue(email2.endsWith("@test.org"));
        assertTrue(email3.endsWith("@example.net"));
    }

    @Test
    void testGenerateWithNullDomain() {
        assertThrows(NullPointerException.class,
                     () -> generator.generate((String) null));
    }

    @Test
    void testGenerateFreeEmail() {
        String email = generator.generateFreeEmail();
        assertNotNull(email);
        assertValidEmail(email);
        String domain = email.substring(email.indexOf('@') + 1);
        assertTrue(List.of("gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com",
                           "protonmail.com", "mail.com", "aol.com", "zoho.com", "gmx.com", "yandex.com",
                           "qq.com").contains(domain));
    }

    @Test
    void testGenerateSafeEmail() {
        String email = generator.generateSafeEmail();
        assertNotNull(email);
        assertValidEmail(email);
        assertTrue(email.endsWith("@example.com")
                   || email.endsWith("@example.org")
                   || email.endsWith("@example.net"));
    }

    @Test
    void testGetFreeEmailProvider() {
        String provider = generator.getFreeEmailProvider();
        assertNotNull(provider);
        assertFalse(provider.isBlank());
        assertFalse(provider.contains("@"));
        assertTrue(provider.contains("."));
    }

    @Test
    void testGenerateFreeEmailDomainAlias() {
        String provider = generator.generateFreeEmailDomain();
        assertNotNull(provider);
        assertFalse(provider.isBlank());
        assertFalse(provider.contains("@"));
        assertTrue(provider.contains("."));
    }

    @Test
    void testGenerateCompanyEmail() {
        String email = generator.generateCompanyEmail();
        assertNotNull(email);
        assertTrue(email.matches("[a-z0-9.]+@[a-z0-9]+\\.[a-z]{2,}"));
    }

    @Test
    void testGetFreeEmailProviderLocaleAware() {
        EmailGenerator de = new EmailGenerator(Locale.GERMANY);
        String provider = de.getFreeEmailProvider();
        assertTrue(List.of("gmx.de", "web.de", "gmail.com", "outlook.com").contains(provider));
    }

    @Test
    void testGetFreeEmailProviderAllLocaleBranches() {
        assertTrue(List.of("gmx.de", "web.de", "gmail.com", "outlook.com")
                       .contains(new EmailGenerator(Locale.GERMANY).getFreeEmailProvider()));
        assertTrue(List.of("orange.fr", "laposte.net", "gmail.com", "outlook.com")
                       .contains(new EmailGenerator(Locale.FRANCE).getFreeEmailProvider()));
        assertTrue(List.of("hotmail.es", "gmail.com", "outlook.com", "yahoo.com")
                       .contains(new EmailGenerator(Locale.of("es", "ES")).getFreeEmailProvider()));
        assertTrue(List.of("libero.it", "gmail.com", "outlook.com", "yahoo.com")
                       .contains(new EmailGenerator(Locale.ITALY).getFreeEmailProvider()));
        assertTrue(List.of("uol.com.br", "bol.com.br", "gmail.com", "outlook.com")
                       .contains(new EmailGenerator(Locale.of("pt", "BR")).getFreeEmailProvider()));
        assertTrue(List.of("yahoo.co.jp", "gmail.com", "outlook.com")
                       .contains(new EmailGenerator(Locale.JAPAN).getFreeEmailProvider()));
        assertTrue(List.of("qq.com", "163.com", "126.com", "gmail.com")
                       .contains(new EmailGenerator(Locale.CHINA).getFreeEmailProvider()));
        assertTrue(List.of("gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com",
                           "protonmail.com", "mail.com", "aol.com", "zoho.com", "gmx.com", "yandex.com",
                           "qq.com")
                       .contains(new EmailGenerator(Locale.US).getFreeEmailProvider()));
    }

    @Test
    void testPopularDomains() {
        Set<String> domains = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String email = generator.generate();
            String domain = email.substring(email.indexOf('@') + 1);
            domains.add(domain);
        }

        // Should see multiple popular domains
        assertTrue(domains.size() > 5, "Should use variety of popular domains");

        // Check some expected domains appear
        String[] allEmails = new String[200];
        for (int i = 0; i < 200; i++) {
            allEmails[i] = generator.generate();
        }
        String combined = String.join(" ", allEmails);
        assertTrue(combined.contains("gmail.com") || combined.contains("yahoo.com") ||
                   combined.contains("outlook.com"), "Should use common email providers");
    }

    // Format Tests

    @Test
    void testGenerateWithFormatFirstnameDotLastname() {
        String email = generator.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME);
        assertNotNull(email);
        assertTrue(email.contains("@"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("."),
                   "FIRSTNAME_DOT_LASTNAME format should contain dot");
    }

    @Test
    void testGenerateWithFormatFirstnameLastname() {
        String email = generator.generate(EmailFormat.FIRSTNAME_LASTNAME);
        assertNotNull(email);
        assertTrue(email.contains("@"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertFalse(localPart.contains(".") || localPart.contains("_"),
                    "FIRSTNAME_LASTNAME format should not contain separators");
    }

    @Test
    void testGenerateWithFormatFirstinitialLastname() {
        String email = generator.generate(EmailFormat.FIRSTINITIAL_LASTNAME);
        assertNotNull(email);
        assertTrue(email.contains("@"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.length() >= 2,
                   "FIRSTINITIAL_LASTNAME should have at least 2 characters");
    }

    @Test
    void testGenerateWithFormatFirstnameUnderscoreLastname() {
        String email = generator.generate(EmailFormat.FIRSTNAME_UNDERSCORE_LASTNAME);
        assertNotNull(email);
        assertTrue(email.contains("@"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("_"),
                   "FIRSTNAME_UNDERSCORE_LASTNAME format should contain underscore");
    }

    @Test
    void testGenerateWithFormatLastnameDotFirstname() {
        String email = generator.generate(EmailFormat.LASTNAME_DOT_FIRSTNAME);
        assertNotNull(email);
        assertTrue(email.contains("@"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("."),
                   "LASTNAME_DOT_FIRSTNAME format should contain dot");
    }

    @Test
    void testGenerateWithNullFormat() {
        assertThrows(NullPointerException.class,
                     () -> generator.generate((EmailFormat) null));
    }

    @Test
    void testAllEmailFormats() {
        for (EmailFormat format : EmailFormat.values()) {
            String email = generator.generate(format);
            assertNotNull(email, "Should generate email for format: " + format);
            assertValidEmail(email);
        }
    }

    // Format and Domain Combined Tests

    @Test
    void testGenerateWithFormatAndDomain() {
        String email = generator.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME, "example.com");
        assertNotNull(email);
        assertTrue(email.endsWith("@example.com"));

        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("."));
    }

    @Test
    void testGenerateWithAllFormatsAndCustomDomain() {
        for (EmailFormat format : EmailFormat.values()) {
            String email = generator.generate(format, "test.org");
            assertNotNull(email);
            assertTrue(email.endsWith("@test.org"),
                       "Email with format " + format + " should use custom domain");
        }
    }

    @Test
    void testGenerateWithNullFormatInCombined() {
        assertThrows(NullPointerException.class,
                     () -> generator.generate(null, "example.com"));
    }

    @Test
    void testGenerateWithNullDomainInCombined() {
        assertThrows(NullPointerException.class,
                     () -> generator.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME, null));
    }

    // Locale-Specific Tests

    @Test
    void testLocaleUS() {
        EmailGenerator usGen = new EmailGenerator(Locale.US);
        String email = usGen.generate();
        assertNotNull(email);
        assertValidEmail(email);
    }

    @Test
    void testLocaleGB() {
        EmailGenerator gbGen = new EmailGenerator(Locale.UK);
        String email = gbGen.generate();
        assertNotNull(email);
        assertValidEmail(email);
    }

    @Test
    void testLocaleDE() {
        EmailGenerator deGen = new EmailGenerator(Locale.GERMANY);
        String email = deGen.generate();
        assertNotNull(email);
        assertValidEmail(email);
    }

    @Test
    void testLocaleFR() {
        EmailGenerator frGen = new EmailGenerator(Locale.FRANCE);
        String email = frGen.generate();
        assertNotNull(email);
        assertValidEmail(email);
    }

    @Test
    void testLocaleJP() {
        EmailGenerator jpGen = new EmailGenerator(Locale.JAPAN);
        String email = jpGen.generate();
        assertNotNull(email);
        assertValidEmail(email);
    }

    @Test
    void testAll10Locales() {
        Locale[] locales = {
            new Locale("en", "US"),
            new Locale("en", "GB"),
            new Locale("en", "AU"),
            new Locale("de", "DE"),
            new Locale("fr", "FR"),
            new Locale("es", "ES"),
            new Locale("it", "IT"),
            new Locale("pt", "BR"),
            new Locale("ja", "JP"),
            new Locale("zh", "CN")
        };

        for (Locale locale : locales) {
            EmailGenerator gen = new EmailGenerator(locale);
            String email = gen.generate();
            assertNotNull(email, "Should generate email for locale: " + locale);
            assertValidEmail(email);
        }
    }

    // Seeding Tests

    @Test
    void testSeededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        EmailGenerator gen1 = new EmailGenerator(config);
        EmailGenerator gen2 = new EmailGenerator(config);

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededGenerationWithDomain() {
        GeneratorConfig config = GeneratorConfig.builder().seed(67890L).build();
        EmailGenerator gen1 = new EmailGenerator(config);
        EmailGenerator gen2 = new EmailGenerator(config);

        assertEquals(gen1.generate("example.com"), gen2.generate("example.com"));
        assertEquals(gen1.generate("test.org"), gen2.generate("test.org"));
    }

    @Test
    void testSeededGenerationWithFormat() {
        GeneratorConfig config = GeneratorConfig.builder().seed(11111L).build();
        EmailGenerator gen1 = new EmailGenerator(config);
        EmailGenerator gen2 = new EmailGenerator(config);

        assertEquals(gen1.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME),
                     gen2.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME));
    }

    @Test
    void testDifferentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder().seed(111L).build();
        GeneratorConfig config2 = GeneratorConfig.builder().seed(222L).build();
        EmailGenerator gen1 = new EmailGenerator(config1);
        EmailGenerator gen2 = new EmailGenerator(config2);

        Set<String> emails1 = new HashSet<>();
        Set<String> emails2 = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            emails1.add(gen1.generate());
            emails2.add(gen2.generate());
        }

        // Different seeds should produce different sequences
        assertNotEquals(emails1, emails2);
    }

    // Stream and List Tests

    @Test
    void testStream() {
        List<String> emails = generator.stream().limit(20).toList();
        assertEquals(20, emails.size());
        emails.forEach(this::assertValidEmail);
    }

    @Test
    void testGenerateList() {
        List<String> emails = generator.generateList(15);
        assertEquals(15, emails.size());
        emails.forEach(this::assertValidEmail);
    }

    @Test
    void testGenerateEmptyList() {
        List<String> emails = generator.generateList(0);
        assertTrue(emails.isEmpty());
    }

    @Test
    void testGenerateListNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateList(-1));
    }

    // Format Validation Tests

    @Test
    void testEmailFormatConsistency() {
        for (int i = 0; i < 100; i++) {
            String email = generator.generate();
            assertValidEmail(email);

            // Check @ appears exactly once
            long atCount = email.chars().filter(ch -> ch == '@').count();
            assertEquals(1, atCount, "Email should contain exactly one @");

            // Check domain has at least one dot
            String domain = email.substring(email.indexOf('@') + 1);
            assertTrue(domain.contains("."), "Domain should contain at least one dot");
        }
    }

    @Test
    void testLocalPartLowercase() {
        for (int i = 0; i < 50; i++) {
            String email = generator.generate();
            String localPart = email.substring(0, email.indexOf('@'));
            assertEquals(localPart.toLowerCase(), localPart,
                         "Local part should be lowercase");
        }
    }

    @Test
    void testNoSpacesInEmail() {
        for (int i = 0; i < 50; i++) {
            String email = generator.generate();
            assertFalse(email.contains(" "), "Email should not contain spaces");
        }
    }

    @Test
    void testGenerateFromDomains() {
        String email = generator.generateFromDomains("acme.dev", "krandom.org");
        assertTrue(email.endsWith("@acme.dev") || email.endsWith("@krandom.org"));
        assertThrows(NullPointerException.class, () -> generator.generateFromDomains((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromDomains());
        assertThrows(IllegalArgumentException.class, () -> generator.generateFromDomains(" "));
    }

    @Test
    void testUniqueGeneration() {
        Set<String> unique = new HashSet<>();
        for (int i = 0; i < 120; i++) {
            unique.add(generator.generateUnique());
        }
        assertEquals(120, unique.size());
    }

    @Test
    void testUniqueGenerationWithDomainAndDomainList() {
        Set<String> uniqueFixed = new HashSet<>();
        Set<String> uniqueFromSet = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            uniqueFixed.add(generator.generateUnique("example.com"));
            uniqueFromSet.add(generator.generateUniqueFromDomains("x.dev", "y.dev"));
        }
        assertEquals(100, uniqueFixed.size());
        assertEquals(100, uniqueFromSet.size());
        assertThrows(NullPointerException.class, () -> generator.generateUnique((String) null));
        assertThrows(NullPointerException.class, () -> generator.generateUniqueFromDomains((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateUniqueFromDomains());
        assertThrows(IllegalArgumentException.class, () -> generator.generateUniqueFromDomains((String) null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateUniqueFromDomains("x.dev", " "));
    }

    @Test
    void testUniqueGenerationFailurePathViaReflection() throws Exception {
        EmailGenerator generator = new EmailGenerator(GeneratorConfig.builder().seed(1L).build());

        Field issuedEmailsField = EmailGenerator.class.getDeclaredField("issuedEmails");
        issuedEmailsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> issued = (Set<String>) issuedEmailsField.get(generator);
        issued.add("fixed@example.com");

        Method uniqueInternal = EmailGenerator.class.getDeclaredMethod("generateUniqueInternal", Supplier.class);
        uniqueInternal.setAccessible(true);

        Supplier<String> supplier = () -> "fixed@example.com";
        InvocationTargetException ex =
            assertThrows(InvocationTargetException.class, () -> uniqueInternal.invoke(generator, supplier));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    // Helper Methods

    private void assertValidEmail(String email) {
        assertNotNull(email);
        assertTrue(email.contains("@"), "Email should contain @");
        assertTrue(email.matches(".+@.+\\..+"), "Email should match basic format");
        assertFalse(email.contains(" "), "Email should not contain spaces");

        int atIndex = email.indexOf('@');
        assertTrue(atIndex > 0, "Local part should not be empty");
        assertTrue(atIndex < email.length() - 1, "Domain should not be empty");
    }
}
