# Migrating From DataFaker To krandom

DataFaker is the actively-maintained realism leader. krandom gives you the same
realistic, locale-aware values **plus** one-call object-graph generation, so you
stop hand-stitching "DataFaker for fields + Instancio/EasyRandom for structure."

## Why switch / trade-offs

**Pros of krandom**
- Realistic field data **and** full object/record graphs in one library.
- 35 locales, checksum national IDs, schema export (CSV/JSONL/XML/SQL).
- A DataFaker-expression adapter, so you can port `#{Provider.method}` strings as-is.

**What DataFaker still has that krandom doesn't**
- More locales (~60+ vs 35) — gap tracked in [`../feature-parity/GAP-TRACKER.md`](../feature-parity/GAP-TRACKER.md).
- ~150 novelty/fandom/sport/food providers (Pokémon, Star Wars, Beer…) — krandom keeps these out of core; they will live in an opt-in `krandom-novelty` module.

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:1.4.0")
}
```

## Entry-point mapping

| DataFaker | krandom |
|---|---|
| `Faker faker = new Faker(Locale.US)` | `GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(42L).build()` |
| `faker.name().fullName()` | `Generators.ofFullName(cfg).generate()` / `Generators.person(cfg).fullName().generate()` |
| `faker.internet().emailAddress()` | `Generators.ofEmail(cfg).generate()` |
| `faker.address().city()` | `Generators.location(cfg).city().generate()` |
| `faker.finance().iban()` | `Generators.ofIban().generate()` |
| `faker.expression("#{Name.fullName}")` | `Generators.ofDataFakerExpression("#{Name.fullName}", cfg).generate()` |
| `faker.numerify("###-###")` | `Generators.ofTemplate("###-###").generate()` (also `letterify`/`bothify`) |
| `faker.regexify("[A-Z]{3}\\d{4}")` | `Generators.ofRegex("[A-Z]{3}\\d{4}").generate()` |
| `faker.unique()...` | `Generators.unique(generator)` |
| `faker.collection(...).len(n).generate()` | `generator.generateList(n)` / `generator.stream().limit(n)` |
| CSV/JSON schema output | `Schema.toCsv()` / `Schema.toJsonLines()` / `toXml()` / `toSqlInserts()` |

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
