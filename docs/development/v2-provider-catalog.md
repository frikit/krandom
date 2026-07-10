# V2 Provider Catalog and Registry Scope

**Status:** In Progress
**Master plan:** [Step 2.8](v2-master-implementation-plan.md#step-28--unify-provider-catalogs-and-registry-scope)

## Inventory (2026-07-10)

Provider identity is currently defined in three independent places:

| Surface | Current responsibility | Source of truth today | Main v2 gap |
| --- | --- | --- | --- |
| `ProviderHub` | Provider keys, aliases, factory, profile/config propagation | 48 canonical registrations and 46 aliases embedded in `registerBuiltIns()` | No schema metadata or semantic aliases |
| `FieldLookup` | Schema tokens, aliases, factories/extractors, JSON Schema fragments | A second embedded built-in registration list | Duplicates hub keys and result-type knowledge |
| `SemanticFieldRegistry` | Object field-name inference and a subset of provider names | 32 semantic keys and 20 provider mappings | Names can point at providers not checked against the hub |
| Global data registries | Locale provider values, locale fallback, input validation | 23 static registry classes across user, location, commerce, finance, measurement, and weather | Mutable process-wide state and uneven snapshot behavior |
| `DataRegistryContext` | Per-configuration override/fallback | 11 locale families: names, gender/title/suffix/profession, location, and national IDs | Does not yet cover the other global registries |

The canonical catalog must carry canonical key, aliases, provider type, factory, optional schema
extractor/metadata, semantic aliases, locale requirement and fallback policy, data validation
strategy, and safety classification. This makes duplicate keys and aliases an initialization error
rather than a discrepancy discovered by a consumer.

## Stage 1: Inventory and snapshot contracts

**Goal:** Establish the present registry boundary and remove APIs that claim to return snapshots
while exposing live concurrent-map views.
**Success Criteria:** Every `registeredKeys`, provider-name, schema-reference, and alias API
returns an immutable point-in-time collection. The inventory lists the independent sources that
the typed catalog will replace.
**Tests:** Capture a legacy registry result, then register a provider and prove the captured set
does not change; repeat for schema references and aliases.
**Status:** Complete

All 18 legacy registries that wrapped a concurrent-map key set now use `Set.copyOf`. The three
registries that already used `Set.copyOf` (`CityDataRegistry`, `StateDataRegistry`, and
`StreetAddressDataRegistry`) required no behavior change. `ProviderHub` already made snapshots;
the focused contract test now locks that behavior down. `FieldLookup` now copies into ordered,
immutable collections so its diagnostic order remains stable as well as isolated from later
registrations.

## Stage 2: Typed catalog and generated consumers

**Goal:** Define one immutable descriptor catalog, then make `ProviderHub`, `FieldLookup`, and
`SemanticFieldRegistry` derive their built-ins from it.
**Success Criteria:** Catalog construction rejects duplicate keys, alias collisions, missing
provider targets, invalid type/extractor pairs, and incomplete schema metadata. Existing public
keys and aliases retain their v1 meanings.
**Tests:** Catalog completeness, duplicate/alias collision, provider type validation, schema
metadata coverage, and semantic-provider target validation.
**Status:** Not Started

## Stage 3: Config-scoped locale data

**Goal:** Put all locale data family overrides in `DataRegistryContext` and retain static
registration only as a deprecated 1.6 compatibility adapter.
**Success Criteria:** Two contexts can override every family independently; locale fallback and
array validation have one implementation; no context reads mutable global state when isolated.
**Tests:** Per-family override isolation, locale fallback matrix, malformed provider data, and
concurrent contexts without cross-contamination.
**Status:** Not Started

## Stage 4: Migration and release gate

**Goal:** Migrate public consumers, publish the catalog semantics, and remove duplicated
registrations.
**Success Criteria:** Documentation, schema metadata, object inference, and provider lookups all
derive from the catalog. The deprecated global adapter is clearly documented and covered by
compatibility tests.
**Tests:** Full consumer matrix, API/binary compatibility checks, and public examples that use a
context rather than static mutation.
**Status:** Not Started
