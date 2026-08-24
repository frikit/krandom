# Migrating From DataFaker To krandom

DataFaker is the actively-maintained realism leader. krandom gives you the same
realistic, locale-aware values **plus** one-call object-graph generation, so you
stop hand-stitching "DataFaker for fields + Instancio/EasyRandom for structure."

## Why switch / trade-offs

**Pros of krandom**
- Realistic field data **and** full object/record graphs in one library.
- 50 supported locale variants (35 native datasets and 15 curated fallbacks), checksum national IDs,
  schema output (JSONL/JSON/CSV/XML/SQL/YAML/TOML) and existing-object projection.
- A DataFaker-expression adapter, so you can port `#{Provider.method}` strings as-is.

**What DataFaker still has that krandom doesn't**
- More locale tags (70 advertised vs 50 supported variants) — breadth gap tracked in [`../feature-parity/GAP-TRACKER.md`](../feature-parity/GAP-TRACKER.md).
- Long-tail novelty, fandom, sport, food, and medical providers — krandom intentionally keeps
  these out of core. Keep DataFaker for those calls or supply a local, provenance-declared data
  pack when the domain is project-specific.

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:2.0.0")
}
```

## Entry-point mapping

| DataFaker | krandom |
|---|---|
| `Faker faker = new Faker(Locale.US)` | `GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(42L).build()` |
| `faker.name().fullName()` | `Generators.ofFullName(cfg).generate()` / `Generators.person(cfg).fullName().generate()` |
| `faker.internet().emailAddress()` | `Generators.ofEmail(cfg).generate()` |
| `faker.address().city()` | `Generators.location(cfg).city().generate()` |
| `faker.finance().iban()` | `new IbanGenerator(cfg).generate()` where `cfg` is built with `.bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)` for an isolated compatibility fixture |
| `faker.expression("#{Name.fullName}")` | `Generators.ofDataFakerExpression("#{Name.fullName}", cfg).generate()` |
| `faker.numerify("###-###")` | `Generators.ofTemplate("###-###").generate()` (also `letterify`/`bothify`) |
| `faker.regexify("[A-Z]{3}\\d{4}")` | `Generators.ofRegex("[A-Z]{3}\\d{4}").generate()` |
| `faker.unique()...` | `Generators.unique(generator)` |
| `faker.collection(...).len(n).generate()` | `generator.generateList(n)` / `generator.stream().limit(n)` |
| CSV/JSON/YAML/TOML schema output | `Schema.toCsv()` / `toJsonLines()` / `toJson()` / `toXml()` / `toSqlInserts()` / `toYaml()` / `toToml()`; use `SchemaProjection<T>` for existing objects |

## Examples

DataFaker (field-by-field, then assembled by hand):

```java
Faker faker = new Faker();
User u = new User();
u.setFullName(faker.name().fullName());
u.setEmail(faker.internet().emailAddress());
u.setCity(faker.address().city());
```

krandom (one call fills the whole graph, realistically):

```java
GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
User u = new ObjectFaker<>(User.class, cfg).generate();   // fullName, email, city auto-resolve by name+type
```

Pin only the fields a test asserts on:

```java
User u = new ObjectFaker<>(User.class, cfg)
    .ruleFor("email", Generators.ofEmail(cfg))
    .ruleFor("status", () -> "ACTIVE")
    .generate();
```

## Determinism

Both seed their PRNG, but a seed is scoped to its own library: the same krandom
version + `GeneratorConfig` (seed, locale, sizes, overrides) + call order yields
repeatable krandom output. DataFaker's exact strings are **not** reproduced.

## Tracked gaps

Provider/locale coverage and the novelty module are tracked in
[`../feature-parity/GAP-TRACKER.md`](../feature-parity/GAP-TRACKER.md) and
[`../feature-parity/datafaker-parity.md`](../feature-parity/datafaker-parity.md).
