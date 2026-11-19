# Architecture

A map of the code for contributors who are about to change it. It names the types that matter and
where they live, so you can find the closest existing example before you write anything. Short paths
such as `generator/Krandom.java` are relative to `core/src/main/java/io/github/frikit/krandom/`; paths
that start with a module name are relative to the repository root.

## Modules

| Module | Artifact | Role |
| --- | --- | --- |
| `core` | `krandom-core` | Everything that generates data. The other modules only adapt it. |
| `jackson` | `krandom-jackson` | Jackson 3 (`tools.jackson`) module: serializes a `Schema` as its JSON Schema. |
| `junit` | `krandom-junit` | JUnit Jupiter extension: a seed per test, injection, replay commands on failure. |
| `spring-boot-starter` | `krandom-spring-boot-starter` | Auto-configuration from `krandom.*` properties and a `@KrandomTest` slice. |
| `kotest-extensions` | `krandom-kotest-extensions` | Kotest `Arb`s backed by seeded kRandom configurations. |
| `kotlin-dsl` | `krandom-kotlin-dsl` | Kotlin builders (`krandom<T> { }`) and an adapter for Kotlin primary constructors. |
| `bom` | `krandom-bom` | Version alignment only; no jar. |

The published modules are listed in `publishedModules` in `gradle.properties`. That list drives
publishing, SBOMs, the API check, coverage floors and the module-boundary check, so adding a module
means adding it there and to every list the build asserts against (see [testing](testing.md)).

Only `core` has a `module-info.java`. It exports every `io.github.frikit.krandom.generator.*` package
and does **not** export `io.github.frikit.krandom.internal`. The other jars carry an
`Automatic-Module-Name` set in `buildSrc/src/main/kotlin/krandom-publishing-conventions.gradle.kts`, and
`scripts/verify_module_boundaries.sh` rejects packages split across jars.

## Entry points

A user meets kRandom through one of these. All of them end in the same generators.

| Entry point | File | What it is |
| --- | --- | --- |
| `Krandom` | `generator/Krandom.java` | A session that returns plain values (`city()`, `intBetween(...)`). Created with `Krandom.seeded("text")` or `Krandom.of(config)`. Not thread-safe. |
| `Generators` | `generator/Generators.java` | The static factory catalog: `ofX()`, `ofX(Locale)`, `ofX(GeneratorConfig)` for every generator, plus `ofObject`, `ofSchema`, `ofProviderHub`, `forType` and `threadLocal`. |
| Fluent namespaces | `generator/namespace/*Generators.java` | `Generators.person()`, `.location()`, `.finance()` and others: the same factories grouped by domain, each holding a `GeneratorConfig`. |
| `Generator<T>` | `generator/Generator.java` | The functional interface every generator implements: `generate()` plus `generateList`, `stream`, `map` and `filter`. |

`FacadeNamespaceParityTest` checks that every namespace method has a matching `Generators` factory
(`x()` needs `ofX`, unless `FACADE_ALIASES` names another). It does not check the other way, so a new
factory needs its namespace method added by hand.

## Configuration

`GeneratorConfig` (`generator/GeneratorConfig.java`) is the immutable configuration every generator
takes in its constructor. Build it with `GeneratorConfig.builder()` or `toBuilder()`. It holds:

- the random source: a numeric or string seed, a caller's `Random` or factory, or `secureRandom()` (one
  only);
- locale, clock, charset, string and collection bounds;
- the `DataRegistryContext` that resolves locale data;
- safety policies, one per sensitive domain (network, payment cards, banking, national IDs and
  others). Most default to a test-safe or disabled value, so a generator for real-looking identifiers
  can throw until a policy enables it;
- stream policies and seeding settings that control how child streams are derived;
- object-engine settings and installed `KRandomModule`s.

A generator calls `config.createRandom()` for its random source (a fresh `Random(seed)` when seeded),
reads locale data through `config.getRegistryContext()`, and gives each part of a composite value its
own stream with `config.forChildStream("name")`. `config.getGenerationRecipe()` serializes a seeded
configuration as a replayable recipe, or returns empty when the configuration cannot be replayed (for
example with a custom registry or modules).

`GeneratorProfile` offers ready-made builder templates; note that some of them set a fixed seed.

## Seeding

Determinism is the library's main promise; [determinism and compatibility](determinism-and-compatibility.md)
says what is guaranteed. The machinery:

| Piece | File | Role |
| --- | --- | --- |
| String seeds | `GeneratorConfig` (`STRING_SEED_DERIVATION`, `deriveSeed`) | `fnv1a64-v1`: FNV-1a 64 over the UTF-8 bytes. A numeric seed wins over a string seed. |
| Child streams | `GenerationRecipe.deriveChildSeed` | Derives a child seed from the parent seed's bytes and a stream name. `ChildStreamPolicy` chooses how. |
| Recipes | `generator/GenerationRecipe.java` | The line-oriented `krandom-recipe/v2` format: every setting needed to replay a run. |
| `internal.StreamSeeds` | `internal/StreamSeeds.java` | The same hash split into steps, so `Schema` derives per-cell seeds without building strings. |
| `internal.Reseeding` | `internal/Reseeding.java` | A registry of package-private `reseedTo(long)` hooks, so `Schema` reuses one generator per column instead of building one per value. |
| `internal.ConfigAccess` | `internal/ConfigAccess.java` | A cheap seed-replacing copy of a configuration for other packages. |
| `Seedable` | `generator/Seedable.java` | The public reseed contract. |

FNV-1a is implemented in `GeneratorConfig`, `GenerationRecipe` and `internal/StreamSeeds.java`. They
must stay identical: changing one changes seeds and breaks replay.

## Locale data

Locale-specific values (names, cities, streets, professions, vocabularies) come from text files, not
from code.

- **Files** live under `core/src/main/resources/krandom/`, one entry per line, UTF-8, `#` comments.
  `core/src/main/resources/krandom/README.md` states the naming rules and per-folder minimums.
- **`SupportedLocale`** (`generator/locale/SupportedLocale.java`) lists every built-in locale, native
  or curated fallback, and gives each its resource prefix (`resourcePrefix()`, such as `de_DE`). The
  order matters: a language-only lookup resolves to the first matching constant.
- **`Vocabulary`** (`generator/Vocabulary.java`) has one constant per dataset. It binds the provider
  interface, the global registry and a validator.
- **Providers and registries:** for each dataset there is an `XDataProvider` interface, a
  `BuiltInXDataProvider` that loads the files, and an `XDataRegistry` that registers a provider for
  each `SupportedLocale` in a static initializer.
- **`DataRegistryContext`** (`generator/DataRegistryContext.java`) resolves a provider for a locale:
  scoped exact locale, then global exact, then scoped language, then global language. Users add their
  own data per configuration with `LocaleDataBundle` or a verified `LocalDataPack`; there is no global
  registration.

A worked example to copy: `generator/location/CityGenerator.java` with `BuiltInCityDataProvider`,
`CityDataRegistry` and `Vocabulary.CITY`. For a small vocabulary, copy the address types in
`generator/location/AddressType*.java`. [Adding a generator](adding-a-generator.md) walks through
both.

## Generators

`generator/` is split by domain: `user`, `location`, `finance`, `network`, `datetime`, `text`,
`commerce`, `identifier` and more; `generator/base` holds primitive, numeric and regex generators.
A typical generator:

- is a `public final class` implementing `Generator<T>`;
- has constructors `()`, `(Locale)` and `(GeneratorConfig)`, the first two delegating to the third;
- keeps `config`, a `Random` from `config.createRandom()` and the data it needs;
- may register a `reseedTo` hook with `Reseeding` in a static initializer (`CityGenerator` does).

## Extension points and larger features

| Feature | Where | Notes |
| --- | --- | --- |
| Modules | `generator/extension/KRandomModule.java` | Installed per configuration with `GeneratorConfig.Builder.install`; never discovered from the classpath. `configure` can run again when a configuration is copied, so keep it free of side effects. |
| Provider hub | `generator/provider/` | `ProviderHub` maps names to generators. `ProviderCatalog` is the source of truth for built-ins and generates `docs/reference/provider-catalog.md`. |
| Data packs | `generator/datapack/LocalDataPack.java` | Offline, checksum-verified local datasets; no network access. |
| Schema | `generator/schema/` | `Schema` generates records with a child stream per record and column, and writes JSONL, JSON, CSV, XML, SQL, YAML and TOML; `SchemaParser` reads JSON Schema and OpenAPI. |
| Object engine | `generator/object/` | `ObjectGenerator` fills records, classes and sealed types; `FieldGeneratorResolver` decides each field (overrides, `@Randomizer`, Bean Validation, semantic names); `SemanticFieldRegistry` maps field names such as `email` to generators. Kotlin constructors are handled by an adapter found through `ServiceLoader`. |
| Safety policies | `generator/network`, `generator/finance`, `generator/user`, `generator/location` | Each generator enforces its own policy, for example reserved IP ranges and `.test` domains by default. `ProviderSafetyMetadata` and `ProviderValidity` record the claims. |

## Integration modules

- **jackson:** `KrandomJacksonModule` and the `KrandomJackson` helpers. Jackson 3 only.
- **junit:** `KrandomExtension` and `@KrandomSeed`. On failure it prints the seed or recipe with
  Gradle and Maven commands to replay it; replay reads `krandom.junit.*` settings.
- **spring-boot-starter:** `KrandomAutoConfiguration` builds a `GeneratorConfig` from
  `KrandomProperties` (`krandom.*`). `krandom.recipe` excludes the individual properties.
- **kotest-extensions:** `KrandomArb.kt`. Each sample's configuration is a child of a recipe, so the
  configuration must be recipe-portable.
- **kotlin-dsl:** `KrandomDsl.kt` and `KotlinPrimaryConstructorAdapter`.

## Things that surprise newcomers

- `Krandom.intBetween` includes its upper bound; `Generators.ofInt(min, max)` excludes it.
- `Krandom.seeded("42")` is a string seed, not the numeric seed `42`.
- The default charset is US-ASCII, and the default locale is `Locale.US`.
- `childStreamPolicy` and `objectFieldStreamPolicy` have different defaults.
- Locale fallback differs by generator: some throw `UnsupportedOperationException` for a locale
  without data, others fall back to a bundled `default.txt`.
- Classes in `internal` are `public` so other packages can call them; JPMS hides them, the classpath
  does not. They are still not public API.
- Resource file names are not uniform across folders (`cities/de_DE_cities.txt` but
  `names/first_male/de_DE.txt`); follow the folder you are adding to.
