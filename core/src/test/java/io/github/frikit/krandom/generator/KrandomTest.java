/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.location.AddressPurposeDataRegistry;
import io.github.frikit.krandom.generator.location.AddressTypeDataRegistry;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Krandom")
class KrandomTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2025, 6, 30);

    private static Krandom uk(String seed) {
        return Krandom.of(GeneratorConfig.builder()
                .seed(seed)
                .locale(Locale.UK)
                .clock(FIXED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build());
    }

    private static List<Object> everyKind(Krandom fake) {
        return List.of(
                fake.firstName(),
                fake.middleName(),
                fake.lastName(),
                fake.fullName(),
                fake.title(),
                fake.email(),
                fake.phoneNumber(),
                fake.nationalId(),
                fake.nationalIdCompact(),
                fake.birthday(25, 80),
                fake.streetAddress(),
                fake.city(),
                fake.postalCode(),
                fake.addressType(),
                fake.addressPurpose(),
                fake.country(),
                fake.countryCode(),
                fake.countryCodeAlpha3(),
                fake.countryCodeNumeric(),
                fake.countryCodes(),
                fake.today(),
                fake.dateBetween(TODAY.minusYears(1), TODAY),
                fake.intBetween(1, 1_000_000),
                fake.pick("MR", "MRS", "MISS", "MS"),
                fake.pick(List.of(1, 2, 3, 4, 5)));
    }

    private static <T> List<T> draw(int count, Function<Krandom, T> kind, Krandom fake) {
        List<T> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(kind.apply(fake));
        }
        return values;
    }

    @Test
    @DisplayName("the same seed gives the same values for every kind")
    void sameSeedGivesSameValues() {
        assertEquals(everyKind(uk("fixtures")), everyKind(uk("fixtures")));
        assertNotEquals(everyKind(uk("fixtures")), everyKind(uk("other fixtures")));
    }

    @Test
    @DisplayName("repeated calls of one kind vary")
    void repeatedCallsVary() {
        Krandom fake = uk("fixtures");

        assertTrue(new HashSet<>(draw(10, Krandom::firstName, fake)).size() > 1);
        assertTrue(new HashSet<>(draw(10, f -> f.intBetween(1, 1_000_000), fake)).size() > 1);
    }

    @Test
    @DisplayName("a kind's values do not shift when other kinds are drawn in between")
    void kindsAreStableAgainstOtherKinds() {
        Krandom alone = uk("fixtures");
        Krandom interleaved = uk("fixtures");

        List<String> expected = draw(5, Krandom::firstName, alone);
        List<String> actual = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            interleaved.email();
            interleaved.lastName();
            actual.add(interleaved.firstName());
            interleaved.intBetween(1, 10);
        }

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("each kind draws from its own derived stream, even under the legacy child-stream policy")
    void kindsUseDerivedStreamsUnderLegacyPolicy() {
        GeneratorConfig legacy = GeneratorConfig.builder().seed(42L).build();
        Krandom fake = Krandom.of(legacy);

        assertEquals(ChildStreamPolicy.LEGACY, legacy.getChildStreamPolicy());
        assertEquals(
                Generators.ofFirstName(fake.streamConfig("krandom.firstName")).generate(), fake.firstName());
        assertEquals(
                Generators.ofLastName(fake.streamConfig("krandom.lastName")).generate(), fake.lastName());
    }

    @Test
    @DisplayName("each country-code method draws from its own stream, and countryCodes keeps one country's codes")
    void countryCodesInEachFormat() {
        Krandom fake = uk("fixtures");
        CountryGenerator alpha2 = Generators.ofCountry(fake.streamConfig("krandom.countryCode"));
        CountryGenerator alpha3 = Generators.ofCountry(fake.streamConfig("krandom.countryCodeAlpha3"));
        CountryGenerator numeric = Generators.ofCountry(fake.streamConfig("krandom.countryCodeNumeric"));
        CountryGenerator codes = Generators.ofCountry(fake.streamConfig("krandom.countryCodes"));

        // Interleaved, so a generator shared between two methods would shift the other method's values.
        for (int i = 0; i < 5; i++) {
            assertEquals(alpha2.generateCode(), fake.countryCode());
            assertEquals(alpha3.generateCodeAlpha3(), fake.countryCodeAlpha3());
            assertEquals(numeric.generateCodeNumeric(), fake.countryCodeNumeric());
            assertEquals(codes.generateCodes(), fake.countryCodes());
        }
        assertTrue(fake.countryCode().matches("[A-Z]{2}"));
        assertTrue(fake.countryCodeAlpha3().matches("[A-Z]{3}"));
        assertTrue(fake.countryCodeNumeric().matches("\\d{3}"));
    }

    @Test
    @DisplayName("address types and purposes come from the session locale")
    void addressTypesAndPurposesFollowTheLocale() {
        Krandom fake = uk("fixtures");
        List<String> types = AddressTypeDataRegistry.forLocale(Locale.UK).getAddressTypes();
        List<String> purposes = AddressPurposeDataRegistry.forLocale(Locale.UK).getAddressPurposes();

        for (int i = 0; i < 20; i++) {
            assertTrue(types.contains(fake.addressType()));
            assertTrue(purposes.contains(fake.addressPurpose()));
        }
        assertTrue(purposes.contains("Correspondence address"));
    }

    @Test
    @DisplayName("streamConfig derives a seed from the session seed and the name")
    void streamConfigDerivesSeed() {
        Krandom fake = Krandom.of(GeneratorConfig.builder().seed(7L).build());

        GeneratorConfig stream = fake.streamConfig("orders");

        assertEquals(
                GenerationRecipe.deriveChildSeed(7L, "orders"), stream.getSeed().orElseThrow());
        assertEquals(stream.getSeed(), fake.streamConfig("orders").getSeed());
        assertNotEquals(stream.getSeed(), fake.streamConfig("payments").getSeed());
    }

    @Test
    @DisplayName("an unseeded session keeps the root configuration and still generates values")
    void unseededSessionUsesRootConfig() {
        GeneratorConfig unseeded = GeneratorConfig.defaults();
        Krandom fake = Krandom.of(unseeded);

        assertSame(unseeded, fake.getConfig());
        assertSame(unseeded, fake.streamConfig("orders"));
        assertFalse(fake.firstName().isBlank());
    }

    @Test
    @DisplayName("of rejects a caller-owned Random or random factory, which every kind would share")
    void ofRejectsSharedRandomSources() {
        GeneratorConfig shared = GeneratorConfig.builder().random(new Random(7)).build();
        GeneratorConfig factory =
                GeneratorConfig.builder().randomFactory(() -> new Random(7)).build();

        assertThrows(IllegalArgumentException.class, () -> Krandom.of(shared));
        assertThrows(IllegalArgumentException.class, () -> Krandom.of(factory));
        assertFalse(Krandom.of(GeneratorConfig.builder().secureRandom().build())
                .firstName()
                .isBlank());
    }

    @Test
    @DisplayName("streamConfig rejects null, blank, and multi-line names")
    void streamConfigRejectsInvalidNames() {
        Krandom fake = Krandom.seeded("fixtures");

        assertThrows(NullPointerException.class, () -> fake.streamConfig(null));
        assertThrows(IllegalArgumentException.class, () -> fake.streamConfig(" "));
        assertThrows(IllegalArgumentException.class, () -> fake.streamConfig("a\nb"));
        assertThrows(IllegalArgumentException.class, () -> fake.streamConfig("a\rb"));
    }

    @Test
    @DisplayName("the seeded factories set the seed and the locale")
    void seededFactoriesSetSeedAndLocale() {
        Krandom defaultLocale = Krandom.seeded("fixtures");
        Krandom german = Krandom.seeded("fixtures", Locale.GERMANY);

        assertEquals(
                GeneratorConfig.defaults().getLocale(),
                defaultLocale.getConfig().getLocale());
        assertEquals(Locale.GERMANY, german.getConfig().getLocale());
        assertEquals(defaultLocale.getConfig().getSeed(), german.getConfig().getSeed());
        assertEquals(
                Krandom.seeded("fixtures").firstName(),
                Krandom.seeded("fixtures").firstName());
    }

    @Test
    @DisplayName("of rejects a null configuration")
    void ofRejectsNull() {
        assertThrows(NullPointerException.class, () -> Krandom.of(null));
    }

    @Test
    @DisplayName("today follows the configured clock, and dates stay inside their bounds")
    void datesFollowClockAndBounds() {
        Krandom fake = uk("fixtures");

        assertEquals(TODAY, fake.today());
        for (int i = 0; i < 50; i++) {
            LocalDate date = fake.dateBetween(TODAY.minusDays(3), TODAY);
            assertFalse(date.isBefore(TODAY.minusDays(3)) || date.isAfter(TODAY), date.toString());

            LocalDate birthday = fake.birthday(25, 80);
            assertFalse(
                    birthday.isAfter(TODAY.minusYears(25)) || !birthday.isAfter(TODAY.minusYears(81)),
                    birthday.toString());

            LocalDate exactAge = fake.birthday(30, 30);
            assertFalse(
                    exactAge.isAfter(TODAY.minusYears(30)) || !exactAge.isAfter(TODAY.minusYears(31)),
                    exactAge.toString());
        }
        assertEquals(TODAY, fake.dateBetween(TODAY, TODAY));
        LocalDate newborn = fake.birthday(0, 0);
        assertFalse(newborn.isAfter(TODAY) || !newborn.isAfter(TODAY.minusYears(1)), newborn.toString());
    }

    @Test
    @DisplayName("birthday rejects a negative or inverted age range")
    void birthdayRejectsInvalidAges() {
        Krandom fake = uk("fixtures");

        assertThrows(IllegalArgumentException.class, () -> fake.birthday(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> fake.birthday(40, 30));
    }

    @Test
    @DisplayName("every call of a method shares its stream, whatever the arguments")
    void callsOfOneMethodShareAStream() {
        Krandom plain = uk("fixtures");
        Krandom mixed = uk("fixtures");

        List<Integer> expected = draw(5, f -> f.intBetween(1, 1_000_000), plain);
        mixed.intBetween(1, 6);
        List<Integer> shifted = draw(5, f -> f.intBetween(1, 1_000_000), mixed);

        assertNotEquals(expected, shifted);
    }

    @Test
    @DisplayName("numbers and choices are unaffected by calls of other methods")
    void numbersAndChoicesAreStableAgainstOtherMethods() {
        Krandom alone = uk("fixtures");
        Krandom interleaved = uk("fixtures");

        List<Integer> expectedNumbers = draw(5, f -> f.intBetween(1, 1_000_000), alone);
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            interleaved.pick("x", "y");
            interleaved.dateBetween(TODAY.minusYears(1), TODAY);
            numbers.add(interleaved.intBetween(1, 1_000_000));
            interleaved.firstName();
        }

        assertEquals(expectedNumbers, numbers);
    }

    @Test
    @DisplayName("intBetween covers both bounds and rejects an inverted range")
    void intBetweenIsInclusive() {
        Krandom fake = uk("fixtures");

        assertEquals(new HashSet<>(List.of(1, 2, 3)), new HashSet<>(draw(200, f -> f.intBetween(1, 3), fake)));
        assertEquals(Integer.MAX_VALUE, fake.intBetween(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(5, fake.intBetween(5, 5));
        assertThrows(IllegalArgumentException.class, () -> fake.intBetween(2, 1));
    }

    @Test
    @DisplayName("pick chooses among the given values and rejects empty or null input")
    void pickChoosesGivenValues() {
        Krandom fake = uk("fixtures");

        assertEquals(new HashSet<>(List.of("a", "b")), new HashSet<>(draw(100, f -> f.pick("a", "b"), fake)));
        assertEquals("only", fake.pick(List.of("only")));
        assertThrows(IllegalArgumentException.class, () -> fake.pick(List.of()));
        assertThrows(NullPointerException.class, () -> fake.pick((List<String>) null));
        assertThrows(NullPointerException.class, () -> fake.pick((String[]) null));
    }

    @Test
    @DisplayName("nationalIdCompact keeps only the letters and digits of the next national ID")
    void nationalIdCompactRemovesSeparators() {
        String written = uk("fixtures").nationalId();
        String compact = uk("fixtures").nationalIdCompact();

        assertTrue(written.contains(" "), written);
        assertEquals(written.replace(" ", ""), compact);
        assertTrue(compact.matches("[A-Z]{2}\\d{6}[A-D]"), compact);
    }

    @Test
    @DisplayName("national IDs follow the configured safety policy")
    void nationalIdFollowsSafetyPolicy() {
        Krandom fake = Krandom.seeded("fixtures", Locale.UK);

        assertThrows(IllegalStateException.class, fake::nationalId);
    }
}
