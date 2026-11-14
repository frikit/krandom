/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded street addresses, address records, the address schema columns and the session's place methods, and the
 * recipe of a default seeded configuration, as 3.1.0 gives them, recorded before the German street layout was
 * added. A configuration that does not opt in must keep giving them.
 */
@DisplayName("seeded addresses and recipes without the German street layout keep their 3.1.0 values")
class StreetAddressPinsTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    static final List<String> LOCALES = List.of("de", "de-DE", "de-AT", "de-CH", "nl-NL", "en-US");

    static final String PINNED_VALUES = """
                de LEGACY street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., München, Bayern 80313, Deutschland
                de LEGACY info 7389 Schloss Anger | 7389 Schloss Anger, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
                de LEGACY schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6003 Schloss Gr., Würzburg, Bayern 97070, Deutschland, street=6003 Schloss Gr., streetNumber=6003, streetName=Schloss, streetSuffix=Gr., streetPrefix=, streetUnit=, city=Würzburg, state=Bayern, stateAbbr=, zip=97070, country=Deutschland, countryAbbr=DE]}, {street=1951 Park Berg, info=AddressInfo[address=5226 Bismarck Anger, Oberhausen, Nordrhein-Westfalen 46045, Deutschland, street=5226 Bismarck Anger, streetNumber=5226, streetName=Bismarck, streetSuffix=Anger, streetPrefix=, streetUnit=, city=Oberhausen, state=Nordrhein-Westfalen, stateAbbr=, zip=46045, country=Deutschland, countryAbbr=DE]}]
                de INDEPENDENT street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Konstanz, Baden-Württemberg 78462, Deutschland
                de INDEPENDENT info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
                de INDEPENDENT schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6924 Linden Steig, Würzburg, Bayern 97070, Deutschland, street=6924 Linden Steig, streetNumber=6924, streetName=Linden, streetSuffix=Steig, streetPrefix=, streetUnit=, city=Würzburg, state=Bayern, stateAbbr=, zip=97070, country=Deutschland, countryAbbr=DE]}, {street=1951 Park Berg, info=AddressInfo[address=770 Linden Ufer, Oberhausen, Nordrhein-Westfalen 46045, Deutschland, street=770 Linden Ufer, streetNumber=770, streetName=Linden, streetSuffix=Ufer, streetPrefix=, streetUnit=, city=Oberhausen, state=Nordrhein-Westfalen, stateAbbr=, zip=46045, country=Deutschland, countryAbbr=DE]}]
                de krandom 6848 Schiller Anger | Worms | 85201
                de-DE LEGACY street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., München, Bayern 80313, Deutschland
                de-DE LEGACY info 7389 Schloss Anger | 7389 Schloss Anger, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
                de-DE LEGACY schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6003 Schloss Gr., Würzburg, Bayern 97070, Deutschland, street=6003 Schloss Gr., streetNumber=6003, streetName=Schloss, streetSuffix=Gr., streetPrefix=, streetUnit=, city=Würzburg, state=Bayern, stateAbbr=, zip=97070, country=Deutschland, countryAbbr=DE]}, {street=1951 Park Berg, info=AddressInfo[address=5226 Bismarck Anger, Oberhausen, Nordrhein-Westfalen 46045, Deutschland, street=5226 Bismarck Anger, streetNumber=5226, streetName=Bismarck, streetSuffix=Anger, streetPrefix=, streetUnit=, city=Oberhausen, state=Nordrhein-Westfalen, stateAbbr=, zip=46045, country=Deutschland, countryAbbr=DE]}]
                de-DE INDEPENDENT street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Konstanz, Baden-Württemberg 78462, Deutschland
                de-DE INDEPENDENT info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland
                de-DE INDEPENDENT schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6924 Linden Steig, Würzburg, Bayern 97070, Deutschland, street=6924 Linden Steig, streetNumber=6924, streetName=Linden, streetSuffix=Steig, streetPrefix=, streetUnit=, city=Würzburg, state=Bayern, stateAbbr=, zip=97070, country=Deutschland, countryAbbr=DE]}, {street=1951 Park Berg, info=AddressInfo[address=770 Linden Ufer, Oberhausen, Nordrhein-Westfalen 46045, Deutschland, street=770 Linden Ufer, streetNumber=770, streetName=Linden, streetSuffix=Ufer, streetPrefix=, streetUnit=, city=Oberhausen, state=Nordrhein-Westfalen, stateAbbr=, zip=46045, country=Deutschland, countryAbbr=DE]}]
                de-DE krandom 6848 Schiller Anger | Worms | 85201
                de-AT LEGACY street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Paderborn, Saarland 9384, Österreich
                de-AT LEGACY info 7389 Schloss Anger | 7389 Schloss Anger, Oldenburg, Rheinland-Pfalz 9384, Österreich
                de-AT LEGACY schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6003 Schloss Gr., Stralsund, Saarland 9398, Österreich, street=6003 Schloss Gr., streetNumber=6003, streetName=Schloss, streetSuffix=Gr., streetPrefix=, streetUnit=, city=Stralsund, state=Saarland, stateAbbr=, zip=9398, country=Österreich, countryAbbr=AT]}, {street=1951 Park Berg, info=AddressInfo[address=5226 Bismarck Anger, Regensburg, Berlin 6935, Österreich, street=5226 Bismarck Anger, streetNumber=5226, streetName=Bismarck, streetSuffix=Anger, streetPrefix=, streetUnit=, city=Regensburg, state=Berlin, stateAbbr=, zip=6935, country=Österreich, countryAbbr=AT]}]
                de-AT INDEPENDENT street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Rostock, Baden-Württemberg 9342, Österreich
                de-AT INDEPENDENT info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Oldenburg, Rheinland-Pfalz 9342, Österreich
                de-AT INDEPENDENT schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6924 Linden Steig, Stralsund, Saarland 9719, Österreich, street=6924 Linden Steig, streetNumber=6924, streetName=Linden, streetSuffix=Steig, streetPrefix=, streetUnit=, city=Stralsund, state=Saarland, stateAbbr=, zip=9719, country=Österreich, countryAbbr=AT]}, {street=1951 Park Berg, info=AddressInfo[address=770 Linden Ufer, Regensburg, Berlin 4714, Österreich, street=770 Linden Ufer, streetNumber=770, streetName=Linden, streetSuffix=Ufer, streetPrefix=, streetUnit=, city=Regensburg, state=Berlin, stateAbbr=, zip=4714, country=Österreich, countryAbbr=AT]}]
                de-AT krandom 6848 Schiller Anger | Worms | 3358
                de-CH LEGACY street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Paderborn, Saarland 9384, Schweiz
                de-CH LEGACY info 7389 Schloss Anger | 7389 Schloss Anger, Oldenburg, Rheinland-Pfalz 9384, Schweiz
                de-CH LEGACY schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6003 Schloss Gr., Stralsund, Saarland 9398, Schweiz, street=6003 Schloss Gr., streetNumber=6003, streetName=Schloss, streetSuffix=Gr., streetPrefix=, streetUnit=, city=Stralsund, state=Saarland, stateAbbr=, zip=9398, country=Schweiz, countryAbbr=CH]}, {street=1951 Park Berg, info=AddressInfo[address=5226 Bismarck Anger, Regensburg, Berlin 6935, Schweiz, street=5226 Bismarck Anger, streetNumber=5226, streetName=Bismarck, streetSuffix=Anger, streetPrefix=, streetUnit=, city=Regensburg, state=Berlin, stateAbbr=, zip=6935, country=Schweiz, countryAbbr=CH]}]
                de-CH INDEPENDENT street 7389 Schloss Anger | 9180 Rosen Strasse | 1014 Kant Gart., Rostock, Baden-Württemberg 9342, Schweiz
                de-CH INDEPENDENT info 8680 Tulpen Pfad | 8680 Tulpen Pfad, Oldenburg, Rheinland-Pfalz 9342, Schweiz
                de-CH INDEPENDENT schema [{street=3876 Tannen Höhe, info=AddressInfo[address=6924 Linden Steig, Stralsund, Saarland 9719, Schweiz, street=6924 Linden Steig, streetNumber=6924, streetName=Linden, streetSuffix=Steig, streetPrefix=, streetUnit=, city=Stralsund, state=Saarland, stateAbbr=, zip=9719, country=Schweiz, countryAbbr=CH]}, {street=1951 Park Berg, info=AddressInfo[address=770 Linden Ufer, Regensburg, Berlin 4714, Schweiz, street=770 Linden Ufer, streetNumber=770, streetName=Linden, streetSuffix=Ufer, streetPrefix=, streetUnit=, city=Regensburg, state=Berlin, stateAbbr=, zip=4714, country=Schweiz, countryAbbr=CH]}]
                de-CH krandom 6848 Schiller Anger | Worms | 3358
                nl-NL LEGACY street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Krimpen aan den IJssel, FR 5130 EL, Nederland
                nl-NL LEGACY info 7389 Molenstraat pad | 7389 Molenstraat pad, Helmond, DR 5130 EL, Nederland
                nl-NL LEGACY schema [{street=3876 Wilhelminastraat gr., info=AddressInfo[address=6003 Sportlaan dk., Harderwijk, LI 9522 EN, Nederland, street=6003 Sportlaan dk., streetNumber=6003, streetName=Sportlaan, streetSuffix=dk., streetPrefix=, streetUnit=, city=Harderwijk, state=Limburg, stateAbbr=LI, zip=9522 EN, country=Nederland, countryAbbr=NL]}, {street=1951 Stationsstraat wg., info=AddressInfo[address=5226 Parallelweg hf., Veghel, GE 1392 ZE, Nederland, street=5226 Parallelweg hf., streetNumber=5226, streetName=Parallelweg, streetSuffix=hf., streetPrefix=, streetUnit=, city=Veghel, state=Gelderland, stateAbbr=GE, zip=1392 ZE, country=Nederland, countryAbbr=NL]}]
                nl-NL INDEPENDENT street 7389 Molenstraat pad | 9180 Dorpsstraat Plantsoen | 1014 Beatrixstraat erf, Reusel, GR 8649 ST, Nederland
                nl-NL INDEPENDENT info 8680 Boslaan bn. | 8680 Boslaan bn., Helmond, DR 8649 ST, Nederland
                nl-NL INDEPENDENT schema [{street=3876 Wilhelminastraat gr., info=AddressInfo[address=6924 Waterstraat str., Harderwijk, LI 4023 XQ, Nederland, street=6924 Waterstraat str., streetNumber=6924, streetName=Waterstraat, streetSuffix=str., streetPrefix=, streetUnit=, city=Harderwijk, state=Limburg, stateAbbr=LI, zip=4023 XQ, country=Nederland, countryAbbr=NL]}, {street=1951 Stationsstraat wg., info=AddressInfo[address=770 Lindelaan erf, Veghel, GE 9328 XQ, Nederland, street=770 Lindelaan erf, streetNumber=770, streetName=Lindelaan, streetSuffix=erf, streetPrefix=, streetUnit=, city=Veghel, state=Gelderland, stateAbbr=GE, zip=9328 XQ, country=Nederland, countryAbbr=NL]}]
                nl-NL krandom 6848 Parallelweg pad | Vierakker | 4692 EG
                en-US LEGACY street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Milwaukee, WI 53209, United States
                en-US LEGACY info 7389 River Park | 7389 River Park, Fontana, CA 92335, United States
                en-US LEGACY schema [{street=3876 Elm Knl, info=AddressInfo[address=6003 Jackson Spur, Jersey City, NJ 07310, United States, street=6003 Jackson Spur, streetNumber=6003, streetName=Jackson, streetSuffix=Spur, streetPrefix=, streetUnit=, city=Jersey City, state=New Jersey, stateAbbr=NJ, zip=07310, country=United States, countryAbbr=US]}, {street=1951 Cherry Mnr, info=AddressInfo[address=5226 Hill Bnd, Burlington, VT 05401, United States, street=5226 Hill Bnd, streetNumber=5226, streetName=Hill, streetSuffix=Bnd, streetPrefix=, streetUnit=, city=Burlington, state=Vermont, stateAbbr=VT, zip=05401, country=United States, countryAbbr=US]}]
                en-US INDEPENDENT street 7389 River Park | 9180 Jefferson Grove | 1014 Willow Xing, Renton, WA 98056, United States
                en-US INDEPENDENT info 8680 Lakeview Holw | 8680 Lakeview Holw, Fontana, CA 92335, United States
                en-US INDEPENDENT schema [{street=3876 Elm Knl, info=AddressInfo[address=6924 Lincoln Ct, Jersey City, NJ 07310, United States, street=6924 Lincoln Ct, streetNumber=6924, streetName=Lincoln, streetSuffix=Ct, streetPrefix=, streetUnit=, city=Jersey City, state=New Jersey, stateAbbr=NJ, zip=07310, country=United States, countryAbbr=US]}, {street=1951 Cherry Mnr, info=AddressInfo[address=770 Liberty Lndg, Burlington, VT 05401, United States, street=770 Liberty Lndg, streetNumber=770, streetName=Liberty, streetSuffix=Lndg, streetPrefix=, streetUnit=, city=Burlington, state=Vermont, stateAbbr=VT, zip=05401, country=United States, countryAbbr=US]}]
                en-US krandom 6848 Lincoln Park | Elk Grove | 57692
                """;

    @Test
    @DisplayName("street addresses, address records, schema columns and session places keep their values")
    void seededAddressValuesArePinned() {
        assertEquals(PINNED_VALUES, values(UnaryOperator.identity(), LOCALES));
    }

    @Test
    @DisplayName("the recipe of a default seeded configuration keeps its text")
    void defaultRecipeTextIsPinned() {
        assertEquals(
                """
                format=krandom-recipe
                recipe-version=v1
                library-version=development
                algorithm=java.util.Random-v1
                seed=42
                locale=de-DE
                clock-instant=2026-07-10T12%3A00%3A00Z
                clock-zone=Z
                profile=custom
                safety-policy=legacy-unclassified
                construction-policy=safe-constructors
                provider-dataset-version=builtin-v1
                setting.banking.safety-policy=DISABLED
                setting.business-tax-identifier.safety-policy=DISABLED
                setting.charset=US-ASCII
                setting.child-stream-policy=INDEPENDENT
                setting.collection.max=10
                setting.collection.min=1
                setting.crypto-address.safety-policy=DISABLED
                setting.email.domain-policy=TEST_SAFE_RESERVED_DOMAINS
                setting.identity-document.safety-policy=DISABLED
                setting.national-id.safety-policy=DISABLED
                setting.network.safety-policy=TEST_SAFE_RESERVED
                setting.object.ignore-errors=false
                setting.object.max-depth=5
                setting.object.null-probability=0.0
                setting.object.optional-empty-probability=0.0
                setting.object.override-default-initialization=false
                setting.object.pool-size=10
                setting.object.requested-unique-fields=-
                setting.object.semantic-mode=RELAXED
                setting.object.unique-fields=email%2Cemailaddress%2Cusername%2Cuserhandle%2Cuuid%2Cguid%2Cid
                setting.object.uniqueness-max-attempts=256
                setting.payment.card-safety-policy=TEST_SAFE_NON_ROUTABLE
                setting.phone-number.safety-policy=TEST_SAFE_WHERE_AVAILABLE
                setting.securities-identifier.safety-policy=DISABLED
                setting.string.max=20
                setting.string.min=5
                """,
                GeneratorConfig.builder()
                        .locale(Locale.GERMANY)
                        .seed(42L)
                        .clock(CLOCK)
                        .build()
                        .getGenerationRecipe()
                        .orElseThrow()
                        .serialize());
    }

    /**
     * The pinned values of the given locales, from configurations that {@code options} adjusts, one line for each
     * value group.
     */
    static String values(UnaryOperator<GeneratorConfig.Builder> options, List<String> tags) {
        List<String> out = new ArrayList<>();
        for (String tag : tags) {
            Locale locale = Locale.forLanguageTag(tag);
            for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
                GeneratorConfig config = options.apply(GeneratorConfig.builder()
                                .locale(locale)
                                .seed(42L)
                                .clock(CLOCK)
                                .childStreamPolicy(policy))
                        .build();
                StreetAddressGenerator streets = new StreetAddressGenerator(config);
                out.add(tag + " " + policy + " street " + streets.generate() + " | " + streets.generate(false) + " | "
                        + streets.generateFullAddress());
                AddressInfo info = new AddressInfoGenerator(config).generate();
                out.add(tag + " " + policy + " info " + info.street() + " | " + info.address());
                Field field = new Field(config);
                Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
                fields.put("street", field.bind("address.street_address"));
                fields.put("info", field.bind("address.address_info"));
                out.add(tag + " " + policy + " schema " + new Schema(config, fields).nextRecords(2));
            }
            Krandom fake = Krandom.of(
                    options.apply(GeneratorConfig.builder().seed("pins").locale(locale))
                            .build());
            out.add(tag + " krandom " + fake.streetAddress() + " | " + fake.city() + " | " + fake.postalCode());
        }
        return String.join("\n", out) + "\n";
    }
}
