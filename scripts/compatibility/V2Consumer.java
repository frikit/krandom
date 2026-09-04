/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
import io.github.frikit.krandom.generator.*;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.extension.*;
import io.github.frikit.krandom.generator.object.*;
import io.github.frikit.krandom.generator.provider.*;
import io.github.frikit.krandom.generator.schema.*;
import java.time.*;
import java.util.*;

/** Compiled against released 2.2.0, then run unchanged against the release candidate. */
public class V2Consumer {
    public record Fixture(String name, int age, List<Integer> values) {}
    public static class Tracking implements Generator<String> {
        private final String country;
        public Tracking(GeneratorConfig config) { country = config.getLocale().getCountry(); }
        public String generate() { return "TRACK-" + country; }
    }
    public static class Extension implements KRandomModule {
        public String id() { return "compatibility.extension"; }
        public void configure(KRandomModuleContext context) {
            context.registerProvider(ProviderDescriptor.builder("compatibility.tracking", Tracking.class, Tracking::new)
                .aliases("compatibilityTrack")
                .schemaProjection(ProviderSchemaProjection.builder("compatibility.tracking_code",
                    (Tracking generator, GeneratorConfig config) -> generator.generate()).build())
                .build());
        }
    }
    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder().seed(42)
            .clock(Clock.fixed(Instant.parse("2026-09-04T23:59:59Z"), ZoneOffset.UTC))
            .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY).build();
        System.out.print(config.getGenerationRecipe().orElseThrow().serialize().replaceAll("(?m)^library-version=.*\\n", ""));
        for (GeneratorConfig variant : List.of(config,
                config.toBuilder().objectOverride(Fixture.class, "name", () -> "fixed").build(),
                config.toBuilder().objectExcludeField("name").build())) {
            new ObjectGenerator<>(Fixture.class, variant).generateList(30).forEach(System.out::println);
        }
        GeneratorConfig replay = config.getGenerationRecipe().orElseThrow().toGeneratorConfig();
        new ObjectGenerator<>(Fixture.class, replay).generateList(30).forEach(System.out::println);
        DateGenerator dates = new DateGenerator(config);
        for (int i = 0; i < 30; i++) System.out.println(dates.future(7));
        ObjectModel<Fixture> model = ObjectModel.of(Fixture.class).configure(f -> f.ruleFor("name", () -> "model"));
        System.out.println(model.faker(config).generate());
        GeneratorConfig extended = config.toBuilder().install(new Extension()).build();
        System.out.println(new ProviderHub(extended).get("compatibilityTrack", Tracking.class).generate());
        System.out.println(new FieldLookup(extended).resolve("compatibility.tracking_code")
            .generate(new SchemaContext(extended.getLocale(), new Random(42), 0)));
    }
}
