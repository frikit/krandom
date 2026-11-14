/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import io.github.frikit.krandom.generator.Vocabulary;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("the German street layout writes the street, then the house number, in the German locales only")
class GermanStreetLayoutTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static final Pattern GERMAN_LINE = Pattern.compile("\\S+ [1-9][0-9]{0,3}");

    private static GeneratorConfig config(String tag, boolean germanLayout, ChildStreamPolicy policy) {
        return GeneratorConfig.builder()
                .locale(Locale.forLanguageTag(tag))
                .seed(42L)
                .clock(CLOCK)
                .childStreamPolicy(policy)
                .germanStreetLayout(germanLayout)
                .build();
    }

    @Test
    @DisplayName("is off by default")
    void isOffByDefault() {
        assertFalse(GeneratorConfig.defaults().isGermanStreetLayout());
        assertTrue(GeneratorConfig.builder().germanStreetLayout(true).build().isGermanStreetLayout());
    }

    @Test
    @DisplayName("seeded with 42, the German street lines read as German writes them")
    void germanLinesAreAsGermanWritesThem() {
        StreetAddressGenerator streets =
                new StreetAddressGenerator(config("de-DE", true, ChildStreamPolicy.INDEPENDENT));

        assertEquals("Schlossanger 7389", streets.generate());
        assertEquals("Rosenstrasse 9180", streets.generate(false));
        assertEquals("Kantgart. 1014, Konstanz, Baden-Württemberg 78462, Deutschland", streets.generateFullAddress());

        AddressInfo info = new AddressInfoGenerator(config("de-DE", true, ChildStreamPolicy.INDEPENDENT)).generate();
        assertEquals("Tulpenpfad 8680", info.street());
        assertEquals("Tulpenpfad 8680, Mönchengladbach, Nordrhein-Westfalen 41061, Deutschland", info.address());
        assertEquals("8680", info.streetNumber());
        assertEquals("Tulpen", info.streetName());
        assertEquals("Pfad", info.streetSuffix());
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "de-DE", "de-AT", "de-CH", "de-LU"})
    @DisplayName("each German locale lays out the same draws as without it, street first")
    void eachGermanLocaleLaysOutTheSameDraws(String tag) {
        for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
            StreetAddressGenerator usLayout = new StreetAddressGenerator(config(tag, false, policy));
            StreetAddressGenerator germanLayout = new StreetAddressGenerator(config(tag, true, policy));
            for (int i = 0; i < 200; i++) {
                boolean shortSuffix = i % 2 == 0;
                String[] parts = usLayout.generate(shortSuffix).split(" ", 3);
                String line = germanLayout.generate(shortSuffix);
                assertEquals(german(parts[1], parts[2]) + " " + parts[0], line, tag);
                assertTrue(GERMAN_LINE.matcher(line).matches(), line);
            }
            String usFull = usLayout.generateFullAddress();
            String germanFull = germanLayout.generateFullAddress();
            String[] usStreet = usFull.substring(0, usFull.indexOf(',')).split(" ", 3);
            assertEquals(
                    german(usStreet[1], usStreet[2]) + " " + usStreet[0] + usFull.substring(usFull.indexOf(',')),
                    germanFull);

            AddressInfoGenerator usInfo = new AddressInfoGenerator(config(tag, false, policy));
            AddressInfoGenerator germanInfo = new AddressInfoGenerator(config(tag, true, policy));
            for (int i = 0; i < 50; i++) {
                AddressInfo us = usInfo.generate();
                AddressInfo de = germanInfo.generate();
                String street = german(us.streetName(), us.streetSuffix()) + " " + us.streetNumber();
                assertEquals(street, de.street());
                assertEquals(street + us.address().substring(us.street().length()), de.address());
                assertEquals(us.streetUnit(), de.streetUnit());
                assertEquals(us.zip(), de.zip());
                assertEquals(us.city(), de.city());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"nl-NL", "nl-BE", "en-US", "en-DE", "fr-CH", "it-IT", "sv-SE"})
    @DisplayName("leaves every other locale as it is")
    void leavesOtherLocalesAsTheyAre(String tag) {
        for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
            StreetAddressGenerator off = new StreetAddressGenerator(config(tag, false, policy));
            StreetAddressGenerator on = new StreetAddressGenerator(config(tag, true, policy));
            AddressInfoGenerator infoOff = new AddressInfoGenerator(config(tag, false, policy));
            AddressInfoGenerator infoOn = new AddressInfoGenerator(config(tag, true, policy));
            for (int i = 0; i < 20; i++) {
                assertEquals(off.generate(), on.generate());
                assertEquals(off.generateFullAddress(), on.generateFullAddress());
                assertEquals(infoOff.generate(), infoOn.generate());
            }
        }
    }

    @Test
    @DisplayName("the pinned values of the Dutch and US locales do not change with it")
    void pinnedOtherLocalesDoNotChange() {
        List<String> others = List.of("nl-NL", "en-US");
        String pinned = Arrays.stream(StreetAddressPinsTest.PINNED_VALUES.split("\n"))
                .filter(line -> line.startsWith("nl-NL ") || line.startsWith("en-US "))
                .reduce("", (joined, line) -> joined + line + "\n");

        assertEquals(pinned, StreetAddressPinsTest.values(builder -> builder.germanStreetLayout(true), others));
    }

    @Test
    @DisplayName("the session, the schema columns and the object engine's address record follow it")
    void sessionAndSchemaFollowIt() {
        GeneratorConfig config = config("de-DE", true, ChildStreamPolicy.INDEPENDENT);
        assertTrue(GERMAN_LINE.matcher(Krandom.of(config).streetAddress()).matches());

        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("street", field.bind("address.street_address"));
        fields.put("info", field.bind("address.address_info"));
        for (Map<String, Object> row : new Schema(config, fields).nextRecords(20)) {
            assertTrue(GERMAN_LINE.matcher((String) row.get("street")).matches(), row.toString());
            assertTrue(
                    GERMAN_LINE
                            .matcher(((AddressInfo) row.get("info")).street())
                            .matches(),
                    row.toString());
        }
    }

    @Test
    @DisplayName("a recipe records it only when it is on, and replays it")
    void recipeRecordsAndReplaysIt() {
        GeneratorConfig on = config("de-DE", true, ChildStreamPolicy.INDEPENDENT);
        GenerationRecipe recipe = on.getGenerationRecipe().orElseThrow();
        assertEquals("true", recipe.getSettings().get("address.german-street-layout"));

        GeneratorConfig replay = GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();
        assertTrue(replay.isGermanStreetLayout());
        assertEquals(
                recipe.serialize(), replay.getGenerationRecipe().orElseThrow().serialize());
        StreetAddressGenerator original = new StreetAddressGenerator(on);
        StreetAddressGenerator replayed = new StreetAddressGenerator(replay);
        for (int i = 0; i < 20; i++) {
            assertEquals(original.generate(), replayed.generate());
        }

        GenerationRecipe off = config("de-DE", false, ChildStreamPolicy.INDEPENDENT)
                .getGenerationRecipe()
                .orElseThrow();
        assertFalse(off.getSettings().containsKey("address.german-street-layout"));
        assertFalse(GenerationRecipe.parse(off.serialize()).toGeneratorConfig().isGermanStreetLayout());
    }

    @Test
    @DisplayName("a child stream keeps it")
    void childStreamKeepsIt() {
        for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
            assertTrue(config("de-DE", true, policy).forChildStream("street").isGermanStreetLayout());
            assertFalse(config("de-DE", false, policy).forChildStream("street").isGermanStreetLayout());
        }
        assertTrue(GeneratorConfig.builder().germanStreetLayout(true).build().toBuilder()
                .build()
                .isGermanStreetLayout());
    }

    @Test
    @DisplayName("joins a registered German name and type as one word")
    void joinsRegisteredGermanData() {
        StreetAddressDataProvider streets = new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return Locale.GERMANY;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] {"Linden"};
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] {"Al."};
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] {"Allee"};
            }
        };
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .registryContext(DataRegistryContext.builder()
                        .isolated()
                        .register(Vocabulary.STREET_ADDRESS, streets)
                        .build())
                .germanStreetLayout(true)
                .seed(5L)
                .build();
        StreetAddressGenerator generator = new StreetAddressGenerator(config);

        assertTrue(generator.generate(false).matches("Lindenallee [1-9][0-9]{0,3}"));
        assertTrue(generator.generate(true).matches("Lindenal\\. [1-9][0-9]{0,3}"));
    }

    /** The street of a German line: the name and its type as one word, the type starting in lower case. */
    private static String german(String name, String type) {
        return name + type.substring(0, 1).toLowerCase(Locale.ROOT) + type.substring(1);
    }
}
