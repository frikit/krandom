# Adding a generator

Step-by-step recipes for the most common change: a new generator, a new locale-aware vocabulary, and
exposing a generator by name. Each recipe names a merged commit that did the same thing; read its diff
(`git show --stat <commit>`, then `git show <commit>`) before you start, and mirror it.

Main-code paths are relative to `core/src/main/java/io/github/frikit/krandom/`, test paths to
`core/src/test/java/io/github/frikit/krandom/`.

## First check what already exists

Search before you create anything: `rg -i <name> core/src` and a look in
`core/src/main/resources/krandom/`. Some generators and vocabularies already exist with only part of
their entry points, for example data files and `Generators` factories but no namespace or `Krandom`
method. Finishing one of those means adding the missing entry points and their tests, not a second copy.

## Then decide what you are adding

| What it produces | Recipe | Example commit |
| --- | --- | --- |
| Values computed from rules or other generators (a checksum, a composite, a format) | [A plain generator](#a-plain-generator) | `e8efe14e` (postal locality) |
| Words or phrases a reader sees, which differ by language (statuses, types, labels) | [A locale-aware vocabulary](#a-locale-aware-vocabulary) | `4c483f90` (address types and purposes) |
| Data for a new locale, or more entries for an existing dataset | [Locale contribution guide](../locale-contribution-guide.md) | |

Language-specific vocabulary is **never** a hard-coded English array. Only values that are the same in
every language stay in code: international standards and codes (NATO phonetic alphabet, VIN, ISO
codes), proper nouns such as programming-language names, and country-specific identifiers such as
Brazil's CPF.

## A plain generator

1. **The class.** In the domain package (`generator/location`, `generator/finance`, ...), add a
   `public final class XGenerator implements Generator<T>` with constructors `()`, `(Locale)` and
   `(GeneratorConfig)`, the first two delegating to the third. Take the random source from
   `config.createRandom()` and locale data from `config.getRegistryContext()`. A composite value gives
   each part its own stream with `config.forChildStream("part")`. Reject `null` arguments with
   `Objects.requireNonNull`; the package is `@NullMarked`.
2. **Respect the safety policies.** If the value could be real (an identifier, an address on a real
   network, a card number), read the matching policy from `GeneratorConfig` and fail closed when it is
   disabled, as the existing generators of that domain do.
3. **The factories.** Add `ofX()`, `ofX(Locale)` and `ofX(GeneratorConfig)` to `generator/Generators.java`
   next to their neighbours, the method to the matching fluent namespace in `generator/namespace/`, and,
   for an everyday kind of value, a method to `generator/Krandom.java`. Write Javadoc for each.
   - A namespace method `x()` must have a matching facade factory `ofX`, or an entry in
     `FACADE_ALIASES` in `FacadeNamespaceParityTest`, which checks each namespace method against the
     facade.
   - A `Krandom` method is `kind("x", Generators::ofX)` plus its call. The kind name becomes the
     generator's child stream name (`krandom.x`), so it must be new and must never change.
4. **Tests.** A `XGeneratorTest` next to the class: behaviour through public API, rejection of bad
   arguments, determinism (the same seed gives the same values, two seeds differ), and every branch,
   because core coverage must stay at 100%. The catalog sweeps (`GeneratorCatalogEnvironmentTest`,
   `GeneratorCatalogNetworkSafetyTest`) call every `Generators` factory by themselves; if yours refuses
   some locales by throwing, list those refusals in `GeneratorCatalogEnvironmentTest`, as `e8efe14e`
   did. Test the new namespace and `Krandom` methods in `FluentNamespaceTest` and `KrandomTest`.
5. **Reuse in `Schema` (optional).** If the generator is expensive to build and will be a schema column,
   register a package-private `reseedTo(long)` hook with `internal.Reseeding` in a static initializer, as
   `generator/location/CityGenerator.java` does. `ReseedAsBuiltTest` then checks it.
6. **Docs.** A row in `docs-site/generator-catalog.md` (hand-maintained), a mention in the matching
   `docs-site/guides/` page if it has one, and an `### Added` entry in `CHANGELOG.md`.

## A locale-aware vocabulary

Mirror the address types (`generator/location/AddressType*.java`) exactly. For a vocabulary called
`X`:

1. **Data files.** `core/src/main/resources/krandom/<x_folder>/<prefix>.txt` for every locale prefix,
   one entry per line, UTF-8, in the locale's own language and script, plus a `default.txt` in English
   for locales without data. The address types also ship `en_US.txt`, `en_GB.txt` and `en_AU.txt`, so
   every locale prefix has a file; other vocabularies leave English to `default.txt` (see step 4).
   Entries come from real sources and are never invented or machine-translated: say in the pull request
   which entries were translated, so a person can review them separately, and stop and ask if you cannot
   source them. Document the folder in `core/src/main/resources/krandom/README.md`.
2. **Provider interface.** `XDataProvider extends LocaleDataProvider`, with the accessor
   (`List<String> getXs()`).
3. **Built-in provider.** `BuiltInXDataProvider` loads the file for a `SupportedLocale`'s
   `resourcePrefix()` with the shared `LocaleTextResourceLoader` (`generator/user`, also used from
   `generator/location`).
4. **Registry.** `XDataRegistry` with `isRegistered`, `forLocale`, `registeredKeys` and a static
   initializer. Pick the variant that matches your files:
   - a file for **every** locale prefix, English included: register every `SupportedLocale`
     unconditionally, as `AddressTypeDataRegistry` and `TitleDataRegistry` do;
   - English from `default.txt` only: register only locales that have a file, guarded by a
     `hasBuiltInResources(prefix)` check, as `MaritalStatusDataRegistry` does. Such registries also
     belong in the read-API tests that list registries of their kind (`VocabularyRegistryReadApiTest`,
     `GlobalRegistryReadApiTest`) and in `LocalizedVocabularyTest`'s list of registries that are
     registered exactly for the translated locales.

   Using the guarded variant with a file for every locale leaves the guard's false branch unreachable,
   and the core's 100% branch coverage fails.
5. **Vocabulary key.** A constant in `generator/Vocabulary.java` binding the provider type, the registry
   and a validator, so users can supply their own data for one configuration. Add it to the `ALL` list in
   the same file, raise the count that `VocabularyTest` asserts, and update the number of built-in
   vocabularies stated in `docs-site/guides/locale-aware-data.md` (no check catches that sentence).
6. **Generator.** `XGenerator` resolves the provider through `config.getRegistryContext()` for
   `Vocabulary.X` and draws from its list.
7. **Factories.** As for a plain generator: `Generators`, the namespace, and `Krandom` if it is an
   everyday kind.
8. **Tests.** A domain test of the generator, plus entries in:
   - `LocalizedVocabularyTest`: a `SingleList` entry, so English output is pinned and each locale draws
     only from its own data, and an `assertTranslated` call, which requires each locale's list to have
     no duplicates, at most a third of its entries equal to English ones and at least 90% of entries in
     the locale's script, and, when called as parallel, as many entries as English. Mirror what
     `4c483f90` added there;
   - `ScopedVocabularyRegistryTest` and `VocabularyTest`;
   - `FluentNamespaceTest` and `KrandomTest` for the new methods.
9. **Docs.** `docs-site/generator-catalog.md`, `docs-site/guides/locale-aware-data.md` where it lists
   vocabularies, and the changelog.

## Exposing a generator by name

`ProviderHub` resolves generators by name, for schemas and provider expressions. To add one:

1. Add a descriptor for it in `generator/provider/ProviderCatalog.java` next to similar built-ins, with
   honest safety and validity metadata (`ProviderSafetyMetadata`, `ProviderValidity`). Name collisions
   fail when the class loads.
2. Run `./gradlew :core:generateProviderCatalogDocumentation` and commit the regenerated
   `docs/reference/provider-catalog.md`; `ProviderCatalogDocumentationTest` fails until you do.

## Before you open the pull request

- The full gate passes (see [testing](testing.md)).
- No existing seeded output changed: a new generator adds a stream, it does not reuse a draw from an
  existing one. See [determinism](determinism-and-compatibility.md).
- The only public API change is the addition itself. `./gradlew checkApiCompatibility` passes.
