/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.color.ColorFormat;
import io.github.frikit.krandom.generator.color.ColorGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.finance.CryptoAddressGenerator;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.EinGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdRegistry;
import io.github.frikit.krandom.generator.network.SlugGenerator;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaParser;
import io.github.frikit.krandom.generator.text.NextWordGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.EmailFormat;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
class EnvironmentDeterminismTest {

    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");
    /** Formats numbers with Arabic-Indic digits by default. */
    private static final Locale ARABIC = Locale.forLanguageTag("ar-SA");

    private Locale originalDefaultLocale;

    @BeforeEach
    void rememberDefaultLocale() {
        originalDefaultLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(originalDefaultLocale);
    }

    @Test
    void slugUsesLocaleNeutralCaseMapping() {
        Locale.setDefault(TURKISH);

        assertEquals("title-input", new SlugGenerator().slugify("TITLE INPUT"));
    }

    @Test
    void cryptoChainUsesLocaleNeutralCaseMapping() {
        Locale.setDefault(TURKISH);
        GeneratorConfig cryptoConfig = GeneratorConfig.builder()
                                                      .seed(7L)
                                                      .cryptoAddressSafetyPolicy(
                                                          CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                      .build();

        assertTrue(new CryptoAddressGenerator(cryptoConfig).generate("BITCOIN").startsWith("1"));
    }

    @Test
    void companyUrlUsesLocaleNeutralCaseMapping() {
        Locale.setDefault(TURKISH);

        assertTrue(new CompanyUrlGenerator(Locale.US)
                       .generateFromCompanyName("TITLE INDUSTRIES")
                       .startsWith("https://www.titleindustries."));
    }

    @Test
    void corpusNormalizationUsesLocaleNeutralCaseMapping() {
        Locale.setDefault(TURKISH);

        NextWordGenerator generator = new NextWordGenerator(new String[] { "TITLE", "INPUT" });

        assertArrayEquals(new String[] { "title", "input" }, generator.getCorpusWords());
    }

    @Test
    void schemaSemanticResolutionUsesLocaleNeutralCaseMapping() {
        Locale.setDefault(TURKISH);
        Schema schema = SchemaParser.fromJsonSchema(Map.of(
            "type", "object",
            "properties", Map.of("ID", Map.of("type", "string"))));

        String id = (String) schema.generate().get("ID");

        assertTrue(id.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void emailUsesConfiguredNameLocale() {
        Locale.setDefault(Locale.ROOT);
        List<String> expectedEmails = emailSamples();
        Locale.setDefault(TURKISH);

        assertEquals(expectedEmails, emailSamples());
    }

    @Test
    void usernameUsesConfiguredNameLocale() {
        Locale.setDefault(Locale.ROOT);
        List<String> expectedUsernames = usernameSamples();
        Locale.setDefault(TURKISH);

        assertEquals(expectedUsernames, usernameSamples());
    }

    @Test
    void rgbaDecimalFormattingUsesDotRegardlessOfJvmLocale() {
        Locale.setDefault(Locale.GERMANY);
        ColorGenerator generator = new ColorGenerator(GeneratorConfig.builder().seed(42L).build());

        String rgba = generator.generate(ColorFormat.RGBA);

        assertTrue(rgba.matches("rgba\\(\\d{1,3},\\d{1,3},\\d{1,3},\\d\\.\\d{3}\\)"));
    }

    @Test
    void nationalIdsUseAsciiDigitsRegardlessOfJvmLocale() {
        Locale.setDefault(ARABIC);
        Random random = new Random(7L);
        for (String key : NationalIdRegistry.registeredKeys()) {
            NationalIdProvider provider = NationalIdRegistry.forLocale(Locale.forLanguageTag(key.replace('_', '-')));
            assertNotNull(provider, key);
            for (int i = 0; i < 20; i++) {
                assertAsciiDigits(provider.generate(random));
            }
        }
    }

    @Test
    void formattedNumbersUseAsciiDigitsRegardlessOfJvmLocale() {
        Locale.setDefault(ARABIC);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(9L)
                                                .businessTaxIdentifierSafetyPolicy(
                                                    BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        for (int i = 0; i < 20; i++) {
            assertAsciiDigits(new PostalCodeGenerator(config).generate());
            assertAsciiDigits(new DateGenerator(config).generateAmerican());
            assertAsciiDigits(new TimeGenerator(config).generateString());
            assertAsciiDigits(new EinGenerator(config).generate());
        }
    }

    private static void assertAsciiDigits(String value) {
        assertTrue(value.chars().noneMatch(ch -> Character.isDigit(ch) && (ch < '0' || ch > '9')),
                   "non-ASCII digits in " + value);
    }

    @Test
    void hslaDecimalFormattingUsesDotRegardlessOfJvmLocale() {
        Locale.setDefault(Locale.GERMANY);
        ColorGenerator generator = new ColorGenerator(GeneratorConfig.builder().seed(42L).build());

        String hsla = generator.generate(ColorFormat.HSLA);

        assertTrue(hsla.matches("hsla\\(\\d{1,3},\\d{1,3}%,\\d{1,3}%,\\d\\.\\d{3}\\)"));
    }

    private static List<String> emailSamples() {
        List<String> values = new ArrayList<>();
        for (long seed = 0; seed < 256; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).locale(Locale.US).build();
            values.add(new EmailGenerator(config).generate(EmailFormat.FIRSTNAME_DOT_LASTNAME, "example.com"));
        }
        return values;
    }

    private static List<String> usernameSamples() {
        List<String> values = new ArrayList<>();
        for (long seed = 0; seed < 256; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).locale(Locale.US).build();
            values.add(new UsernameGenerator(config).generate());
        }
        return values;
    }
}
