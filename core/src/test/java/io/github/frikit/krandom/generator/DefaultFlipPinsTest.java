/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.object.Randomizer;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded values of the four settings that 4.0 turns on by default, {@code advancingConvenienceStreams},
 * {@code strictMathGaussian}, {@code objectReseedBuiltInRandomizers} and {@code germanStreetLayout}, with all
 * four set off and with all four set on, recorded on {@code main} at 3.2.0 before any default changed. With
 * every setting given explicitly, these values do not depend on the defaults, so they stay the same when the
 * defaults change.
 *
 * <p>Values that follow the Java runtime are checked against an oracle rather than pinned: a price tag's amount
 * is formatted with the runtime's locale data, and {@link Math} may differ in its last bit between platforms.
 */
@DisplayName("the four 4.0 default flips, each set explicitly, keep their seeded values")
class DefaultFlipPinsTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    /** The 3.x behaviour of all four. */
    static final UnaryOperator<GeneratorConfig.Builder> ALL_OFF = builder -> builder.advancingConvenienceStreams(false)
            .strictMathGaussian(false)
            .objectReseedBuiltInRandomizers(false)
            .germanStreetLayout(false);

    /** The 4.0 behaviour of all four. */
    static final UnaryOperator<GeneratorConfig.Builder> ALL_ON = builder -> builder.advancingConvenienceStreams(true)
            .strictMathGaussian(true)
            .objectReseedBuiltInRandomizers(true)
            .germanStreetLayout(true);

    static final List<String> LOCALES = List.of("de", "de-DE", "de-AT", "de-CH", "nl-NL", "en-US");

    static final String OFF_VALUES = """
            company emails [aharris@advancedassociatesco.test, aharris@advancedassociatesco.test, aharris@advancedassociatesco.test]
            middle names [Caroline Hazel Tran, Steven Hazel Mitchell, Olivia Ellie Taylor, Carter H. Green, Stephanie H. Reyes, Hunter D. Bell]
            session mnelson@example.org | Walter Ross
            de street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Konstanz, Baden-Württemberg 78462, Deutschland
            de info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de schema [{street=3876 Tannen Höhe, email=waltervogt@example.net}, {street=1951 Park Berg, email=laura_becker@example.com}]
            de session 7072 Beethoven Ring
            de object Bruno, 139 Buchen Anger, 2426 Birken Br. | Stefanie, 6309 Eichen Anger, 5237 Wald Gart.
            de-DE street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Konstanz, Baden-Württemberg 78462, Deutschland
            de-DE info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de-DE schema [{street=3876 Tannen Höhe, email=waltervogt@example.net}, {street=1951 Park Berg, email=laura_becker@example.com}]
            de-DE session 7072 Beethoven Ring
            de-DE object Bruno, 7448 Mozart Wall, 2426 Birken Br. | Stefanie, 2922 Wald Aue, 5237 Wald Gart.
            de-AT street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Rostock, Baden-Württemberg 9342, Österreich
            de-AT info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Oldenburg, Rheinland-Pfalz 9342, Österreich
            de-AT schema [{street=3876 Tannen Höhe, email=waltervogt@example.net}, {street=1951 Park Berg, email=laura_becker@example.com}]
            de-AT session 7072 Beethoven Ring
            de-AT object Bruno, 7448 Mozart Wall, 2426 Birken Br. | Stefanie, 2922 Wald Aue, 5237 Wald Gart.
            de-CH street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Rostock, Baden-Württemberg 9342, Schweiz
            de-CH info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Oldenburg, Rheinland-Pfalz 9342, Schweiz
            de-CH schema [{street=3876 Tannen Höhe, email=waltervogt@example.net}, {street=1951 Park Berg, email=laura_becker@example.com}]
            de-CH session 7072 Beethoven Ring
            de-CH object Bruno, 7448 Mozart Wall, 2426 Birken Br. | Stefanie, 2922 Wald Aue, 5237 Wald Gart.
            nl-NL street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Reusel, GR 8649 ST, Nederland
            nl-NL info 8680 Boslaan bn. | 8680 Boslaan bn., Helmond, DR 8649 ST, Nederland
            nl-NL schema [{street=3876 Wilhelminastraat gr., email=mariusveenstra@example.net}, {street=1951 Stationsstraat wg., email=charlotte_vanschaik@example.com}]
            nl-NL session 7072 Hoofdstraat wg.
            nl-NL object Gerben, 7448 Julianastraat pad, 2426 Boslaan gr. | Daphne, 2922 Wilhelminastraat erf, 5237 Hoofdstraat bd.
            en-US street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Renton, WA 98056, United States
            en-US info 8680 Lakeview Holw | 8680 Lakeview Holw, Fontana, CA 92335, United States
            en-US schema [{street=3876 Elm Knl, email=landonhill@example.net}, {street=1951 Cherry Mnr, email=savannah_edwards@example.com}]
            en-US session 7072 Fairview Pt
            en-US object Jordan, 7448 Washington Anx, 2426 Lakeview Path | Ivy, 2922 Elm Hl, 5237 Aspen Mews
            randomizers 48 1470758198 KFOTMJCeZ
            randomizers -107 -1541425382 NEjCpaWp
            randomizers -5 880964928 OHVNKoqLU
            """;

    static final String ON_VALUES = """
            company emails [aharris@advancedassociatesco.test, juliagarcia@advancedresourcesltd.test, edavis@alpharesourcescorp.test]
            middle names [Caroline Hazel Tran, Steven Audrey Mitchell, Olivia Amanda Taylor, Carter L. Green, Stephanie J. Reyes, Hunter P. Bell]
            session mnelson@example.org | Walter Ross
            de street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland
            de info Tulpenpfad 8680 | Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de session Beethovenring 7072
            de object Bruno, Buchenanger 139, Birkenbr. 2426 | Stefanie, Eichenanger 6309, Waldgart. 5237
            de-DE street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland
            de-DE info Tulpenpfad 8680 | Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
            de-DE schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-DE session Beethovenring 7072
            de-DE object Bruno, Mozartwall 7448, Birkenbr. 2426 | Stefanie, Waldaue 2922, Waldgart. 5237
            de-AT street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Rostock, Baden-Württemberg 9342, Österreich
            de-AT info Tulpenpfad 8680 | Tulpenpfad 8680, Oldenburg, Rheinland-Pfalz 9342, Österreich
            de-AT schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-AT session Beethovenring 7072
            de-AT object Bruno, Mozartwall 7448, Birkenbr. 2426 | Stefanie, Waldaue 2922, Waldgart. 5237
            de-CH street Schlossanger 7389 | Rosenstrasse 9180 | Kantgart. 1014, Rostock, Baden-Württemberg 9342, Schweiz
            de-CH info Tulpenpfad 8680 | Tulpenpfad 8680, Oldenburg, Rheinland-Pfalz 9342, Schweiz
            de-CH schema [{street=Tannenhöhe 3876, email=waltervogt@example.net}, {street=Parkberg 1951, email=laura_becker@example.com}]
            de-CH session Beethovenring 7072
            de-CH object Bruno, Mozartwall 7448, Birkenbr. 2426 | Stefanie, Waldaue 2922, Waldgart. 5237
            nl-NL street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Reusel, GR 8649 ST, Nederland
            nl-NL info 8680 Boslaan bn. | 8680 Boslaan bn., Helmond, DR 8649 ST, Nederland
            nl-NL schema [{street=3876 Wilhelminastraat gr., email=mariusveenstra@example.net}, {street=1951 Stationsstraat wg., email=charlotte_vanschaik@example.com}]
            nl-NL session 7072 Hoofdstraat wg.
            nl-NL object Gerben, 7448 Julianastraat pad, 2426 Boslaan gr. | Daphne, 2922 Wilhelminastraat erf, 5237 Hoofdstraat bd.
            en-US street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Renton, WA 98056, United States
            en-US info 8680 Lakeview Holw | 8680 Lakeview Holw, Fontana, CA 92335, United States
            en-US schema [{street=3876 Elm Knl, email=landonhill@example.net}, {street=1951 Cherry Mnr, email=savannah_edwards@example.com}]
            en-US session 7072 Fairview Pt
            en-US object Jordan, 7448 Washington Anx, 2426 Lakeview Path | Ivy, 2922 Elm Hl, 5237 Aspen Mews
            randomizers 48 1470758198 KFOTMJCeZ
            randomizers -107 -1541425382 NEjCpaWp
            randomizers -5 880964928 OHVNKoqLU
            built-in randomizers [b904f4d8-fe65-48a3-a97b-7f26457e4913 Penelope 8656 Forest Sq, ea080d25-7914-4b54-8460-a49a4bcd0325 Victor 3974 Jackson Gdns, 6dab29a0-6853-40c5-abd9-17c560381af1 Nicholas 2470 Church Path]
            gaussian 10 [-0.0388436147345816, 0.7916654062154486, 0.08786661673846564, 2.3761061677475346] [99.41734577898127, 111.87498109323172]
            gaussian 14 [0.7299233329370594, -0.31054186561011643, -1.0265431925416346, 1.0495360551026751] [110.94884999405589, 95.34187201584825]
            gaussian 42 [-0.3249011602434719, -0.7283863934921454, -0.25959749624431383, 1.5110439411230536] [95.12648259634793, 89.07420409761782]
            """;

    @Test
    @DisplayName("with all four off, the 3.x values")
    void offValues() {
        assertEquals(OFF_VALUES, values(ALL_OFF));
    }

    @Test
    @DisplayName("with all four on, the 4.0 values")
    void onValues() {
        assertEquals(ON_VALUES, values(ALL_ON));
    }

    @Test
    @DisplayName("with all four off, a built-in @Randomizer that is not Seedable draws from its own source")
    void offBuiltInRandomizersAreUnseeded() {
        assertNotEquals(builtIns(ALL_OFF), builtIns(ALL_OFF));
    }

    @Test
    @DisplayName("with all four off, each price tag is the first amount of the money stream; on, the next one")
    void priceTags() {
        GeneratorConfig off = seeded(42L, "en-US", ALL_OFF);
        CurrencyGenerator offTags = new CurrencyGenerator(off);
        GeneratorConfig on = seeded(42L, "en-US", ALL_ON);
        CurrencyGenerator onTags = new CurrencyGenerator(on);
        MoneyGenerator onMoney = new MoneyGenerator(on.forChildStream("money"));
        for (int i = 0; i < 4; i++) {
            assertEquals(new MoneyGenerator(off.forChildStream("money")).generate(), offTags.generatePriceTag());
            assertEquals(onMoney.generate(), onTags.generatePriceTag());
        }
    }

    @Test
    @DisplayName("with all four off, Gaussian draws use Math; on, StrictMath")
    void gaussianDraws() {
        for (long seed = 0; seed < 200; seed++) {
            assertEquals(
                    boxMuller(seed, false),
                    new NormalDistributionGenerator(seeded(seed, "en-US", ALL_OFF)).generateList(6),
                    "seed " + seed);
            assertEquals(
                    boxMuller(seed, true),
                    new NormalDistributionGenerator(seeded(seed, "en-US", ALL_ON)).generateList(6),
                    "seed " + seed);
        }
    }

    static GeneratorConfig seeded(long seed, String tag, UnaryOperator<GeneratorConfig.Builder> settings) {
        return settings.apply(GeneratorConfig.builder()
                        .seed(seed)
                        .locale(Locale.forLanguageTag(tag))
                        .clock(CLOCK))
                .build();
    }

    /**
     * The pinned values under {@code settings}, one line for each group. With the reseed on, the built-in
     * randomizers replay and are pinned too, and with StrictMath on, the Gaussian values are the same on every
     * platform and are pinned too.
     */
    static String values(UnaryOperator<GeneratorConfig.Builder> settings) {
        List<String> out = new ArrayList<>();
        GeneratorConfig us = seeded(42L, "en-US", settings);
        EmailGenerator emails = new EmailGenerator(us);
        List<String> companyEmails = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            companyEmails.add(emails.generateCompanyEmail());
        }
        out.add("company emails " + companyEmails);
        FullNameGenerator names = new FullNameGenerator(us);
        out.add("middle names "
                + List.of(
                        names.generateWithMiddleName(),
                        names.generateWithMiddleName(),
                        names.generateWithMiddleName(Gender.FEMALE),
                        names.generateWithMiddleInitial(),
                        names.generateWithMiddleInitial(),
                        names.generateWithMiddleInitial(Gender.MALE)));
        GeneratorConfig session = seeded(42L, "en-US", settings);
        Krandom krandom = Krandom.of(session);
        out.add("session " + krandom.email() + " | " + krandom.fullName());

        for (String tag : LOCALES) {
            GeneratorConfig config = seeded(42L, tag, settings);
            StreetAddressGenerator streets = new StreetAddressGenerator(config);
            out.add(tag + " street " + streets.generate() + " | " + streets.generate(false) + " | "
                    + streets.generateFullAddress());
            AddressInfo info = new AddressInfoGenerator(config).generate();
            out.add(tag + " info " + info.street() + " | " + info.address());
            Field field = new Field(config);
            Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
            fields.put("street", field.bind("address.street_address"));
            fields.put("email", field.bind("person.email"));
            out.add(tag + " schema " + new Schema(config, fields).nextRecords(2));
            out.add(tag + " session " + Krandom.of(config).streetAddress());
            ObjectGenerator<Resident> residents = new ObjectGenerator<>(Resident.class, config);
            out.add(tag + " object " + residents.generate() + " | " + residents.generate());
        }

        ObjectGenerator<WithRandomizers> objects = new ObjectGenerator<>(WithRandomizers.class, us);
        for (int i = 0; i < 3; i++) {
            WithRandomizers value = objects.generate();
            out.add("randomizers " + value.seedable + " " + value.plain + " " + value.text);
        }
        if (seeded(42L, "en-US", settings).isObjectReseedBuiltInRandomizers()) {
            out.add("built-in randomizers " + builtIns(settings));
        }
        if (seeded(42L, "en-US", settings).isStrictMathGaussian()) {
            for (long seed : List.of(10L, 14L, 42L)) {
                out.add("gaussian " + seed + " "
                        + new NormalDistributionGenerator(seeded(seed, "en-US", settings)).generateList(4) + " "
                        + new NormalDistributionGenerator(100.0, 15.0, seeded(seed, "en-US", settings))
                                .generateList(2));
            }
        }
        return String.join("\n", out) + "\n";
    }

    private static List<String> builtIns(UnaryOperator<GeneratorConfig.Builder> settings) {
        ObjectGenerator<WithRandomizers> objects =
                new ObjectGenerator<>(WithRandomizers.class, seeded(42L, "en-US", settings));
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WithRandomizers value = objects.generate();
            values.add(value.uuid + " " + value.first + " " + value.street);
        }
        return values;
    }

    /** Box-Muller over the seed's own {@link Random}, with {@link StrictMath} or with {@link Math}. */
    private static List<Double> boxMuller(long seed, boolean strict) {
        Random random = new Random(seed);
        List<Double> values = new ArrayList<>();
        while (values.size() < 6) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            while (u1 == 0.0) {
                u1 = random.nextDouble();
            }
            double angle = 2.0 * Math.PI * u2;
            double magnitude = strict ? StrictMath.sqrt(-2.0 * StrictMath.log(u1)) : Math.sqrt(-2.0 * Math.log(u1));
            values.add(0.0 + magnitude * (strict ? StrictMath.cos(angle) : Math.cos(angle)) * 1.0);
            values.add(0.0 + magnitude * (strict ? StrictMath.sin(angle) : Math.sin(angle)) * 1.0);
        }
        return values;
    }

    public static class Resident {
        public String firstName;
        public String street;
        public String streetAddress;

        @Override
        public String toString() {
            return firstName + ", " + street + ", " + streetAddress;
        }
    }

    public static class WithRandomizers {
        @Randomizer(ByteGenerator.class)
        public byte seedable;

        @Randomizer(UUIDGenerator.class)
        public UUID uuid;

        @Randomizer(FirstNameGenerator.class)
        public String first;

        @Randomizer(StreetAddressGenerator.class)
        public String street;

        public int plain;
        public String text;
    }
}
