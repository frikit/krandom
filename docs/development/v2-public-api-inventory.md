# kRandom v2 Public API Inventory

**Baseline:** `1.5.0`
**Development line:** `1.6.0-SNAPSHOT`
**Status:** Classified for the 1.6 bridge

## Machine-readable inventory

Run:

```bash
./gradlew generatePublicApiInventory
```

The task builds every published jar module and writes complete public class, constructor, method, field, annotation, and compatibility metadata to:

```text
build/reports/api-inventory/<module>/inventory.html
build/reports/api-inventory/<module>/inventory.xml
```

`./gradlew checkApiContract` separately compares the current jars with the Maven Central `apiBaselineVersion` from `gradle.properties`. It fails on binary/source incompatibility and on added, removed, or structurally changed public elements not classified in `config/api-evolution-allowlist.txt`. Annotation-only and behavioral changes still require human review.

## Classification rule

Every public API present in 1.5.0 has the default disposition **KEEP** unless it appears in an exception table below. That conservative default prevents a large v2 cleanup from becoming an excuse for arbitrary churn.

New public APIs are not accepted implicitly. They require a use case, tests, Javadocs, a changelog entry, an update to this inventory, and a narrow evolution-allowlist entry. The compatibility gate continues to inspect allowlisted elements for breakage.

The allowed dispositions are:

| Disposition | Meaning |
|:---|:---|
| **KEEP** | Stable v2 API; changes remain subject to compatibility policy |
| **DEPRECATE 1.6** | Keep as a thin delegate in 1.6 and remove in v2 |
| **REPLACE FIRST** | Do not deprecate until the named replacement exists |
| **INTERNALIZE v2** | Move out of the public surface only after migration evidence and compatibility review |
| **DECISION REQUIRED** | Resolve the named contract question before implementation |

## Published artifacts

| Artifact | Module/API identity | Disposition |
|:---|:---|:---|
| `krandom-bom` | Maven/Gradle platform; no Java classes | **KEEP** |
| `krandom-core` | `io.github.frikit.krandom` | **KEEP**, with exceptions below |
| `krandom-jackson` | `io.github.frikit.krandom.jackson` | **KEEP** |
| `krandom-junit` | `io.github.frikit.krandom.junit` | **KEEP** |
| `krandom-spring-boot-starter` | `io.github.frikit.krandom.spring.boot.starter` | **KEEP** |
| `krandom-kotest-extensions` | `io.github.frikit.krandom.kotest` | **KEEP**, behavior hardening required |
| `krandom-kotlin-dsl` | `io.github.frikit.krandom.kotlin.dsl` | **KEEP**, typed additions required |

The Java module names are part of the compatibility contract. `krandom-core` now carries an
explicit descriptor named `io.github.frikit.krandom`; this preserves its established automatic
module name while declaring its runtime module graph. The remaining published jars retain their
listed automatic module names until their named-module consumer contracts are completed. The BOM
deliberately has no Java module identity.

## `Generators` facade exceptions

The facade currently has hundreds of public static declarations. Domain namespaces and generators remain the long-term discoverable API; v2 should not add another alias for an operation that already has one.

| Current API | Disposition | Canonical/replacement API | Reason |
|:---|:---|:---|:---|
| `constant(value)` | **DEPRECATE 1.6** | `ofConstant(value)` | Align with the established `ofX` factory family |
| `pickFrom(list)` | **DEPRECATE 1.6** | `pick(list)` | One concise selection verb is sufficient |
| `pickSetFrom(list, count)` | **DEPRECATE 1.6** | `pickSet(list, count)` | Align pick and pick-set factory naming |
| `pickset(list, count)` | **DEPRECATE 1.6** | `pickSet(list, count)` | Existing spelling violates Java casing |
| `shuffleOf(list)` | **DEPRECATE 1.6** | `shuffle(list)` | Redundant alias |
| `uniqueValues(generator)` | **DEPRECATE 1.6** | `unique(generator)` | Redundant alias |
| `ofUrl()` / `ofURL()` | **DECISION REQUIRED** | One acronym convention | Current overload sets differ |
| `ofUri()` / `ofURI()` | **DECISION REQUIRED** | One acronym convention | Current overload sets differ |
| `ofUuid()` | **DECISION REQUIRED** | Keep or add `ofUUID()` bridge | Must use the same acronym rule as URL/URI |

All other facade methods default to **KEEP** for the 1.6 bridge. Stage 3 may reduce the v2 facade only through a reviewed inventory update and an available 1.6 migration path.

## Registry exceptions

Public provider interfaces and lookup operations remain **KEEP**. Static global mutation is different: it leaks state between tests and cannot provide complete registry isolation.

| API family | Disposition | Required replacement |
|:---|:---|:---|
| Static `register(...)` methods and profession `append(...)` on data registries | **DEPRECATE in 1.6 / REMOVE in v2** | Context-scoped registration covering every provider family |
| Static registry reset/clear hooks, where public | **REPLACE FIRST** | Disposable scoped contexts |
| `registeredKeys()` | **KEEP**, fix semantics | Return an immutable snapshot, not a live view |
| Provider interfaces and locale keys | **KEEP** | Typed provider catalog becomes their single source of truth |

All 23 global mutation methods are now deprecated in 1.6 as thin compatibility adapters. Their
Javadocs link to the exact `DataRegistryContext.Builder` replacement and
[`v1.6-to-v2.md`](../migration/v1.6-to-v2.md) explains configuration-scoped migration.
The later-added registry classes remain **KEEP** with no public-member change: their private
bridge validators reuse the matching context registration before globally mutating state, so
compatibility registrations reject the same malformed provider data.

`DataRegistryContext` weather lookup, key snapshot, and builder registration are additive **KEEP**
APIs. They provide validated, locale-fallback-aware weather vocabulary that is isolated per
`GeneratorConfig`; `WeatherDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`WeatherGenerator` remains **KEEP** with no new public member and now resolves vocabulary through
the configuration context.
`DataRegistryContext` measurement lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated, locale-fallback-aware measurement units scoped to one
`GeneratorConfig`; `MeasurementDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`MeasurementGenerator` remains **KEEP** with no new public member and now resolves units through
the configuration context.
`DataRegistryContext` financial-term lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated, locale-fallback-aware financial terms scoped to one
`GeneratorConfig`; `FinancialTermDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`FinancialTermGenerator` remains **KEEP** with no new public member and now resolves terms through
the configuration context.
`DataRegistryContext` restaurant-type lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated, locale-fallback-aware restaurant types scoped to one
`GeneratorConfig`; `RestaurantTypeDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`RestaurantTypeGenerator` remains **KEEP** with no new public member and now resolves types through
the configuration context.
`DataRegistryContext` hobby lookup, key snapshot, and builder registration are additive **KEEP**
APIs. They provide validated, locale-fallback-aware hobbies scoped to one `GeneratorConfig`;
`HobbyDataRegistry.register(...)` remains the 1.6 compatibility adapter. `HobbyGenerator` remains
**KEEP** with no new public member and now resolves hobbies through the configuration context.
`DataRegistryContext` nationality lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated, locale-fallback-aware nationalities scoped to one
`GeneratorConfig`; `NationalityDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`NationalityGenerator` remains **KEEP** with no new public member and now resolves demonyms through
the configuration context.
`DataRegistryContext` pronoun lookup, key snapshot, and builder registration are additive **KEEP**
APIs. They provide validated, locale-fallback-aware `subject/object` pronoun sets scoped to one
`GeneratorConfig`; `PronounDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`PronounGenerator` remains **KEEP** with no new public member and now resolves sets through the
configuration context.
`DataRegistryContext` blood-type lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated parallel, positive-weight distributions scoped to one
`GeneratorConfig`; `BloodTypeDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`BloodTypeGenerator` remains **KEEP** with no new public member and now resolves distributions
through the configuration context.
`DataRegistryContext` Chinese-zodiac lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated twelve-item cycles scoped to one `GeneratorConfig`;
`ChineseZodiacDataRegistry.register(...)` remains the 1.6 compatibility adapter.
`ChineseZodiacGenerator` remains **KEEP** with no new public member and now resolves its cycle
through the configuration context.
`DataRegistryContext` Western-zodiac lookup, key snapshot, and builder registration are additive
**KEEP** APIs. They provide validated twelve-item cycles scoped to one `GeneratorConfig`;
`ZodiacDataRegistry.register(...)` remains the 1.6 compatibility adapter. `ZodiacGenerator` remains
**KEEP** with no new public member and now resolves its cycle through the configuration context.

`ProviderCatalog`, `ProviderDescriptor`, `ProviderSchemaProjection`, `ProviderSafetyMetadata`,
`ProviderSafetyPolicy`, `ProviderValidity`, and `ProviderTestSafety` are additive **KEEP** APIs.
They expose the immutable built-in definitions used by `ProviderHub`, `FieldLookup`, and object
semantic inference: canonical key, declared result type, config-aware factory, aliases, typed
schema extractors/metadata, conservative validity and test-safety claims, and semantic mappings.
Classified schema references carry the same configuration-selected contract through their
`x-krandom-safety` JSON Schema extension. They do not replace the existing per-hub runtime
registration API. Catalog initialization rejects provider, schema-reference, and alias collisions,
while each descriptor checks that its factory returns the declared provider type.

## Object-generation exceptions

| API | Disposition | Required work |
|:---|:---|:---|
| `ObjectGenerator`, `ObjectFaker`, annotations, and public predicates | **KEEP** | Harden construction, constraints, type handling, and errors |
| `ObjectConstructionPolicy` | **KEEP** | `SAFE_CONSTRUCTORS` is the v2 default; `UNSAFE_CONSTRUCTOR_BYPASS` is the explicit legacy opt-in |
| `ObjectConstructionAdapter` / `ObjectConstructionContext` | **KEEP** | Service-loaded construction bridge; Kotlin support uses the existing resolver without adding Kotlin runtime to core |
| `GeneratorConfig` object-generation methods | **KEEP** | Remove duplicated internal configuration state without changing the public path |
| `ObjectGeneratorConfig` | Internal implementation, not public API | Remove its references from public Javadocs before refactoring |

Public Javadocs currently leak the package-private `ObjectGeneratorConfig` type through `Fake`, `FakeRange`, `Exclude`, `FieldPredicates`, and `TypePredicates`. Those links must be rewritten to the public `GeneratorConfig`/`ObjectFaker` path.

`GeneratorConfig.getObjectConstructionPolicy()` and
`GeneratorConfig.Builder.objectConstructionPolicy(...)` are additive **KEEP** APIs. The enum names
the safety boundary directly: safe mode invokes constructors, while the unsafe value preserves the
1.5 Objenesis fallback only when a consumer opts in. The migration path is therefore one explicit
builder call for consumers that temporarily require constructor bypass.

`ObjectConstructionAdapter` and `ObjectConstructionContext` are additive **KEEP** extension APIs.
They are intentionally Kotlin-free: core discovers adapters through `ServiceLoader`, and adapters
generate every constructor argument through the same object-resolution pipeline. The Kotlin DSL's
public provider class is implementation-only despite its JVM visibility, which ServiceLoader
requires; consumers should use `krandom<T>`, not instantiate the provider.

## Structured failure context additions

| API | Disposition | Reason |
|:---|:---|:---|
| `GenerationFailureContext` | **KEEP** | One sanitized context shape for object and schema failures |
| `GenerationFailureCategory` | **KEEP** | Stable machine-readable failure reason |
| `GenerationOperation` | **KEEP** | Stable operation at the failing boundary |
| `GenerationFailureDiagnostic` | **KEEP** | Value-free listener event containing context, cause class name, and optional replay identity |
| `GenerationFailureListener` | **KEEP** | Synchronous optional observer that cannot access values or throwables |
| `GeneratorConfig.Builder.generationFailureListener(...)` | **KEEP** | Scoped listener configuration with a no-op default |
| `ObjectGenerationException.getContext()` | **KEEP** | Additive migration path; empty for legacy/unmigrated boundaries |
| `SchemaGenerationException.getContext()` | **KEEP** | New schema failures expose field/record context; optional preserves legacy deserialization |

The context deliberately excludes generated values and third-party exception messages. Existing
exception constructors and inheritance remain compatible; the original cause is preserved.
Diagnostics expose only the cause class name, not the throwable. Replay identity remains empty
until Step 2.7 introduces the recipe contract. Listener callback failures are sanitized and cannot
replace strict failures or lenient fallback behavior.
`ObjectGenerator` and `ObjectFaker` remain **KEEP** with no new public members; their exact
class-level evolution classifications cover private contextual-failure implementation reported by
japicmp.
`GeneratorConfig` and its builder also retain **KEEP** dispositions; their exact class-level
classifications cover private listener storage, while the accessor and builder method above are
the complete additive public surface.
`GenerationRecipe` is an additive **KEEP** replay value object. It records the independently
versioned recipe format and random algorithm, then recreates a seed-owned `GeneratorConfig` with a
fixed clock. `GeneratorConfig.getGenerationRecipe()` is intentionally optional: caller-owned,
secure, callback-backed, and custom-registry configurations do not expose a misleading partial
recipe. The `generationProfile`, `safetyPolicy`, and `providerDatasetVersion` config labels are
also additive **KEEP** metadata required by the portable recipe contract.
`PaymentCardSafetyPolicy`, `GeneratorConfig.getPaymentCardSafetyPolicy()`, and
`GeneratorConfig.Builder.paymentCardSafetyPolicy(...)` are additive **KEEP** APIs. The default
produces issuer-shaped card numbers that deliberately fail Luhn; `CHECKSUM_VALID` is an explicit
validator-fixture opt-in, not a processor sandbox credential. Portable recipes store the typed
selection as `payment.card-safety-policy`, independently of the legacy diagnostic safety label.
`PhoneNumberSafetyPolicy`, `GeneratorConfig.getPhoneNumberSafetyPolicy()`, and
`GeneratorConfig.Builder.phoneNumberSafetyPolicy(...)` are additive **KEEP** APIs. The default
uses NANPA's fictional 555-0100 through 555-0199 range only for US locale-style
output; other locales and custom formats remain explicitly unclassified. Recipes persist the typed
selection as `phone-number.safety-policy` and preserve legacy unclassified replay when absent.
`PhoneNumberGenerator` remains **KEEP** with no public-member change; its exact class-level
classification covers the private policy-selection implementation.
`NationalIdSafetyPolicy`, `GeneratorConfig.getNationalIdSafetyPolicy()`, and
`GeneratorConfig.Builder.nationalIdSafetyPolicy(...)` are additive **KEEP** APIs. Configuration
defaults to `DISABLED` because national-ID shape or checksum validity cannot prove a safe fixture;
`REALISTIC_UNCLASSIFIED` is an explicit isolated-test compatibility opt-in. The locale and seeded
`NationalIdGenerator` constructors are **DEPRECATE 1.6** bridges that preserve prior behavior and
are scheduled for v2 removal. Portable recipes store the typed policy as
`national-id.safety-policy` and replay legacy recipes as unclassified output.
`BankingSafetyPolicy`, `GeneratorConfig.getBankingSafetyPolicy()`, and
`GeneratorConfig.Builder.bankingSafetyPolicy(...)` are additive **KEEP** APIs. Configuration
defaults to `DISABLED` because account, routing, IBAN, or BIC structure cannot prove a safe
fixture; `REALISTIC_UNCLASSIFIED` is an explicit isolated-test compatibility opt-in. The
no-argument and locale constructors of `BankAccountGenerator`, `AbaRoutingGenerator`,
`BbanGenerator`, `IbanGenerator`, `BicGenerator`, and `BankInfoGenerator` are **DEPRECATE 1.6**
bridges that preserve prior behavior and are scheduled for v2 removal. Portable recipes store the
typed policy as `banking.safety-policy` and replay legacy recipes as unclassified output.
`IdentityDocumentSafetyPolicy`, `GeneratorConfig.getIdentityDocumentSafetyPolicy()`, and
`GeneratorConfig.Builder.identityDocumentSafetyPolicy(...)` are additive **KEEP** APIs.
Configuration defaults to `DISABLED` because generic passport and driving-license shapes cannot
prove a safe fixture; `REALISTIC_UNCLASSIFIED` is an explicit isolated-test compatibility opt-in.
The no-argument `PassportGenerator` and `DrivingLicenseGenerator` constructors are **DEPRECATE
1.6** bridges that preserve prior behavior and are scheduled for v2 removal. Portable recipes
store the typed policy as `identity-document.safety-policy` and replay legacy recipes as
unclassified output.
`BusinessTaxIdentifierSafetyPolicy`,
`GeneratorConfig.getBusinessTaxIdentifierSafetyPolicy()`, and
`GeneratorConfig.Builder.businessTaxIdentifierSafetyPolicy(...)` are additive **KEEP** APIs.
Configuration defaults to `DISABLED` because CNPJ and EIN format or checksum validity cannot prove
a safe fixture; `REALISTIC_UNCLASSIFIED` is an explicit isolated-test compatibility opt-in. The
no-argument `CnpjGenerator` and `EinGenerator` constructors are **DEPRECATE 1.6** bridges that
preserve prior behavior and are scheduled for v2 removal. `Generators.ofEin(GeneratorConfig)` is
an additive **KEEP** canonical facade. Portable recipes store the typed policy as
`business-tax-identifier.safety-policy` and replay legacy recipes as unclassified output.
`CnpjGenerator.withAlphanumericFormat()` is additive **KEEP**. It produces an explicitly
unclassified alphanumeric CNPJ shape with the Receita Federal check-digit algorithm and does not
claim that the resulting identifier is assigned or fictitious.
`CryptoAddressSafetyPolicy`, `GeneratorConfig.getCryptoAddressSafetyPolicy()`, and
`GeneratorConfig.Builder.cryptoAddressSafetyPolicy(...)` are additive **KEEP** APIs.
Configuration defaults to `DISABLED` because a plausible address does not establish a test network,
unspendability, or non-routability; `REALISTIC_UNCLASSIFIED` is an explicit isolated-test
compatibility opt-in. The no-argument `CryptoAddressGenerator` constructor is a **DEPRECATE 1.6**
bridge that preserves prior behavior and is scheduled for v2 removal.
`Generators.ofCryptoAddress(GeneratorConfig)` is an additive **KEEP** canonical facade. Portable
recipes store the typed policy as `crypto-address.safety-policy` and replay legacy recipes as
unclassified output.
`GenerationRecipe.deriveChildSeed(...)` is additive **KEEP**: object and schema internals use it
for named structural streams, and its algorithm is pinned by the recipe version.
`GenerationRecipe.serializeForDiagnostics()` is additive **KEEP**: it retains the numeric replay
seed but removes original textual seed material before a recipe is published to logs or test
reports. `KrandomExtension.RECIPE_REPORT_ENTRY_KEY` is additive **KEEP** and carries that safe
recipe in JUnit report entries.
`SchemaGenerationException.getReplayRecipe()` is additive **KEEP**. It exposes the same safe
recipe included in a schema-generation failure message, or remains empty for legacy and metadata
boundaries where no generation source exists.
`Field` remains **KEEP** with no new public members; its exact class-level classification covers
the package-private non-throwing membership probe used by schema semantic resolution.
`Schema` remains **KEEP** with no new public members; its exact class-level classification covers
private structured-record conversion and contextual failure propagation.
`FieldLookup` remains **KEEP** with no new public members; its exact class-level classification
covers private catalog-driven registration and JSON Schema conversion helpers.

## Integration exceptions

| API | Disposition | Contract work |
|:---|:---|:---|
| `Generator<T>.toArb()` and Kotest helpers | **KEEP** | Honor Kotest `RandomSource`; document/provide shrinking and edge cases |
| Kotlin string-based field rules | **KEEP as bridge** | Add typed `KProperty1` rules before deciding v2 removal |
| `@KrandomTest` | **KEEP** | Make it a standalone composed Spring test slice |
| `KrandomExtension` and `@KrandomSeed` | **KEEP** | Add portable recipe replay without source edits |
| Jackson helpers/module | **KEEP** | No current contract exception |

## APIs intentionally retained

- Generator classes remain constructible for consumers who prefer direct types over the facade.
- Domain namespaces remain the preferred discoverability layer.
- Migration adapters such as DataFaker expression support remain public until real usage data justifies deprecation.
- Provider interfaces remain public extension points.
- Schema export APIs remain public while their supported import/metadata subset is documented more precisely.

## Review checklist for a new public API

- [ ] A consumer use case cannot be served by composition or an existing namespace.
- [ ] The name and overloads follow one existing convention.
- [ ] Behavior, edge cases, thread use, and replay are tested.
- [ ] Javadocs reference only public supported types.
- [ ] The symbol is added to this inventory with a disposition.
- [ ] The compatibility/evolution reports are reviewed.
- [ ] The changelog and consumer examples are updated when relevant.

Until all boxes are satisfied, keep the implementation package-private.
