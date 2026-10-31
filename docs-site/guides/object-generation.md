---
layout: page
title: Object Generation
permalink: /guides/object-generation/
---

# Object Generation

`ObjectGenerator<T>` populates Java classes recursively.

## Configuration ownership

Use one immutable `GeneratorConfig` as the source of object-generation settings. Values supplied
through configuration collections or varargs are copied when the configuration is built; registered
generators, predicates, listeners, random sources, and factories remain caller-owned references and
are not invoked while the configuration is assembled. Treat those executable objects as immutable
for the lifetime of a configuration, or create a new configuration when they need to change.

## Basic usage

```java
ObjectGenerator<UserDto> gen = new ObjectGenerator<>(UserDto.class);
UserDto dto = gen.generate();
```

## With root GeneratorConfig

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .objectMaxDepth(2)
        .objectSemanticMode(ObjectGenerationSemanticMode.RELAXED)
        .objectNullProbability(0.10)
        .objectOptionalEmptyProbability(0.25)
        .objectDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 12, 31))
        .objectOverride(OrderDto.class, "status", () -> "PENDING")
        .objectExcludeField("internalToken")
        .build();

ObjectGenerator<OrderDto> orders = new ObjectGenerator<>(OrderDto.class, cfg);
OrderDto order = orders.generate();
```

`GeneratorConfig` is the public entry point, including for advanced object overrides and exclusions.
There is no separate public object-only config path to learn: object defaults, overrides, exclusions,
semantic controls, and registry wiring all live on the root `GeneratorConfig` builder.

## Construction policy

`SAFE_CONSTRUCTORS` is the default. It runs constructors instead of manufacturing partially
initialized instances, and it preserves field initializers unless
`objectOverrideDefaultInitialization(true)` is selected. An override that targets the field itself
(`objectOverride` by owner type and field name or by field predicate, or an `ObjectFaker` rule)
replaces its initializer either way; type-level overrides keep it. Field-level overrides take
precedence over type-level overrides, contextual or not.

| Target | Safe construction path | If unavailable |
|:---|:---|:---|
| Java record | Canonical constructor | Contextual construction failure |
| Mutable class with a no-argument constructor | No-argument constructor, then mutable fields | Contextual construction/access failure |
| Class with exactly one declared constructor | Generated constructor arguments (by parameter name with `-parameters`, otherwise by position), then mutable fields | Contextual construction/access failure |
| Class with multiple declared constructors | No implicit choice | Register a type factory |
| Interface or abstract root | No reflective allocation | Register a type factory |
| Local, anonymous, non-static inner, enum, annotation, array, or primitive root | Unsupported | Use a supported wrapper/factory design |

Constructor parameters behave like the fields they initialize when the class is compiled with
`-parameters`: by-name overrides, `objectExcludeField` on the parameter name (or `@Exclude` on the
backing field), Bean Validation constraints declared on the parameter, and semantic providers such
as `firstName` or `email` all apply. Without compiled parameter names the engine addresses
arguments by position, so `objectOverride(Money.class, "constructorArg0", ...)` targets the first
argument and exclusions by name have no effect.

A type override is also a root factory and runs before reflection. Contextual factories win over
plain factories and receive `"$root"`, the requested type, and depth zero:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(PaymentMethod.class, CardPayment::new)
    .build();

PaymentMethod payment = new ObjectGenerator<>(PaymentMethod.class, config).generate();
```

Factories must return a non-null value assignable to the registered type. They are the preferred
escape hatch for third-party, abstract, or deliberately immutable types.

`UNSAFE_CONSTRUCTOR_BYPASS` opts into Objenesis allocation for classes with no usable constructor;
prefer a type factory where one is possible:

```java
GeneratorConfig legacy = GeneratorConfig.builder()
    .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
    .build();
```

Unsafe bypass can skip constructor invariants and initializers. It still needs reflective access to
mutable fields, so it is not a JPMS workaround.

### Named modules and `opens`

`krandom-core` has the explicit module name `io.github.frikit.krandom`. A named consumer needs one
qualified `opens` clause for each package whose non-public constructors or mutable fields kRandom
populates:

```java
module com.example.fixtures {
    requires io.github.frikit.krandom;

    opens com.example.fixtures.model to io.github.frikit.krandom;
}
```

An exported package is not automatically open for reflection. If the clause is missing, object
generation fails with `REFLECTION` context and an error containing the exact directive to add. A
root factory avoids reflective construction and therefore does not require `opens` for that value.

Semantic modes:

- `RELAXED`: use semantic field names when available, but let annotations and bean validation win.
- `STRICT`: semantic field names win whenever a semantic match exists.
- `STRUCTURAL_ONLY`: disable field-name semantics and use type-based generation only.

## Semantic field defaults

In `RELAXED` and `STRICT` modes, `ObjectGenerator<T>` recognizes common business field names after normalization.
`created_at`, `createdAt`, and `created-at` all resolve to the same semantic key.

Current built-in semantic coverage includes:

- strings such as `firstName`, `lastName`, `fullName`, `email`, `username`, `phoneNumber`, `streetAddress`, `city`, `postalCode`, `country`, `companyName`, `url`, `domain`, `uuid`, and `status`
- typed business fields such as `createdAt`, `updatedAt`, `birthDate`, `age`, `amount`, `balance`, `price`, `currency`, `id`, `active`, `latitude`, and `longitude`

String semantics reuse the same provider taxonomy as `ProviderHub`, so values such as `firstName`, `city`, `url`, `currencyCode`, and `uuid` resolve through providers like `person.first_name`, `address.city`, `internet.url`, `finance.currency`, and `code.uuid`. Locale and deterministic seed settings still come from the shared root `GeneratorConfig`.

Examples:

- `dateOfBirth`, `dob`, and `birth_date` map to the same birth-date semantic
- `accountId`, `customer_id`, and `identifier` map to the ID semantic
- `isEnabled` and `active` map to the active-flag semantic
- `lat` and `latitude` map to the latitude semantic

Configured object date ranges still apply to semantic temporal fields. If you set `objectDateRange(...)`, that range becomes the effective window for semantic fields such as `dob`, `createdAt`, and `updatedAt`. An `OffsetDateTime` field is generated with the range on its local date, and its offset follows the [UTC-offset contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#utc-offset-contract). An `Instant` field is drawn anywhere within a UTC day of the range, including the whole of its last day: see the [Instant contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#instant-contract).

Object generation also runs a lightweight coherence pass after sibling values are generated. Common pairs such as `firstName` + `lastName` -> `fullName`, `firstName` + `lastName` + `domain` -> `email`, `domain` -> `url`, `createdAt` / `updatedAt`, `birthDate` / `age`, and `active` / `status` are aligned automatically. Address-like clusters now reuse one locale-backed address snapshot so `streetAddress`, `city`, `state`, `postalCode`, and `country` stay consistent with each other, and money-like clusters keep `price <= amount <= balance` while deriving missing sibling values when one amount is already present. In `RELAXED` mode, that pass still leaves annotated or Bean Validation-constrained target fields alone; explicit field and type overrides always win. Generated emails keep to the [email-domain contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#email-domain-contract), and generated domains and URLs to the [network contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#network-contract): a generated `domain` is reserved, so an email beside it takes that domain, and a `companyName` derives a `.test` domain.

## Bean Validation

Object population natively composes 21 Jakarta Validation constraints across fields, getters,
records, and interface accessors. See the [Bean Validation support matrix]({{ '/guides/bean-validation/' | relative_url }})
for exact target types, intersection rules, precedence, and exclusions.

## Depth limit

`objectMaxDepth` (default 5) limits how deep nested classes and records are generated. The root is at
depth 0, and each nested object is one deeper. With `objectMaxDepth(1)`, the root's nested objects are
generated, but not the nested objects inside them.

A member whose nested object would be past the limit is left `null`, unless the member itself is
required. A required member fails generation instead:

```java
record Leaf(String value) {}
record Mid(@NotNull Leaf leaf) {}
record Top(Mid mid) {}

GeneratorConfig config = GeneratorConfig.builder().objectMaxDepth(1).build();

// Throws ObjectGenerationException for 'Top.mid.leaf', with the cause
// "required member would be null at objectMaxDepth 1; raise objectMaxDepth or override the member"
new ObjectGenerator<>(Top.class, config).generate();
```

To generate the member, raise `objectMaxDepth`, or give it an override: `objectOverride(Mid.class, "leaf", ...)`
and `objectOverride(Leaf.class, ...)` run before the limit is checked. Excluding the member leaves it
`null` without a failure. With `objectIgnoreErrors(true)`, the member stays `null` and
`generationFailureListener` is told.

What counts as required:

- `@NotNull` on the member, whether on a field, getter, interface accessor, record component or
  constructor parameter. `@NotNull` on a final field alone is not seen, because a constructor argument
  only reads the constraints on its parameter.
- `@NotEmpty` on a collection class that is generated as a nested object, such as a custom
  `AbstractCollection`. Above the limit such a class is built through its constructor and not filled,
  so it can still be empty. `@NotEmpty` and `@NotBlank` are rejected on other nested object types.
- `@Size` alone does not count, because Bean Validation accepts `null` for it.

A required member that no depth could generate, such as an interface with no `objectSubtype` mapping,
fails as an unsupported type, as it does above the limit.

Collections, arrays, maps and optionals are not cut off at the limit, but the nested objects inside them
are: a `List<Leaf>` holds `null` elements, and a `Map<Leaf, String>` and an `Optional<Leaf>` are empty.
A constraint on a type argument, such as `List<@NotNull Leaf>`, does not change this.

Cycles are cut separately. Below the limit, a member that would repeat a nested type already being
generated, such as `next.next` of a self-typed `@NotNull Node next`, is left `null` whatever its
constraints. At the limit it fails like any other required member, and a larger `objectMaxDepth` then
lets generation succeed without giving it a value.

`ObjectFaker` rules and ignores on nested paths run after generation, so they do not prevent the
failure: raise `objectMaxDepth` as well, or use `objectOverride`.

## Unique fields

Request a field with `objectUniqueField` when its values must not repeat. A generator remembers the
values it gives a requested field for as long as it lives, including in nested objects, and checks a
value however it was produced: by an override, `@Randomizer`, `@Fake`, `@FakeRange`, a Bean Validation
constraint, a semantic field name, a built-in type or an enum. On a repeat it draws again, up to
`objectUniquenessMaxAttempts` (default 256) times for each value. A `null` is never a repeat. If every
draw repeats, generation fails:

```java
record Seat(@Min(1) @Max(3) int id) {}

ObjectGenerator<Seat> seats = new ObjectGenerator<>(
        Seat.class, GeneratorConfig.builder().objectUniqueField("id").build());
seats.generateList(3); // ids 1, 2 and 3, in some order

// Throws ObjectGenerationException for 'Seat.id', with the cause
// "Could not generate a unique value for field 'com.example.Seat.id' after 256 attempts; ..."
seats.generate();
```

With `objectIgnoreErrors(true)`, the field gets the default for its type instead (`0`, `false` or
`null`), and `generationFailureListener` is told.

The draws are random, so a domain that is nearly used up can fail before every value has appeared.
Raising `objectUniquenessMaxAttempts` makes that less likely.

An override is drawn again like any other generator, so one that returns a constant on a requested field
gives one object and fails on the next from the same generator. A new generator starts over. A root
`ObjectFaker` rule with a generator is an override. A dependent rule, a rule on a nested path and
`postProcess` run after generation, so their values are not checked.

A requested name is matched as given, ignoring case and separators, and does not reach a semantic alias.
Requesting `id` guarantees nothing for `customerId`, because a foreign key can repeat.

Each field has its own values, so `orderId` and `customerId` can be equal, and so can `Order.id` and a
nested `Customer.id`. A field inherited from a superclass is one field, whichever subclass reaches it, and
a constructor argument shares the values of the field with its name, on its class or nearest superclass.

Some values of a requested field are not checked:

- The elements of a list, set or array are checked under the field's name, but the values inside an
  `Optional` or a `Map` are not, and neither is a nested object as a whole.
- Values are compared with `equals`, so a type without value equality, such as `AtomicInteger`, never
  counts as a repeat.
- A value the class sets itself, such as a field initializer that generation keeps, is not checked.

### Names unique by default

`email`, `emailAddress`, `username`, `userHandle`, `uuid`, `guid` and `id` are unique by default, so that
generated test data looks plausible. This is not a guarantee, and it covers fewer values than a request:

- A semantic value is checked under these names and their semantic aliases, so a generated `String email`
  or `long customerId` does not repeat.
- A built-in or enum value is checked under these names alone. `boolean id` fails on the object after its
  two values run out, while `boolean accountId` repeats them.
- A value from an override, `@Randomizer`, `@Fake`, `@FakeRange` or a Bean Validation constraint is not
  checked. `objectOverride(User.class, "email", () -> "owner@example.test")`, a rule that gives `email` a
  constant, and `record Order(@Min(1) @Max(100) int customerId)` all repeat their values with no failure.

`objectUniqueField` keeps these names, and only the name it adds gets the guarantee above.
`objectUniqueFields` requests the names you give it and replaces the rest, and `objectUniqueFields()`,
with no names, makes nothing unique. A name's aliases are still checked on semantic values whether you
requested the name or not, so `objectUniqueField("id")` leaves a generated `long customerId` as it was.

The coherence pass numbers a derived `email`, `fullName`, `domain`, `url` or address field on a repeat,
as in `alice.smith1@example.com` and `n1.acme.test`. This applies to any of these unique names, requested
or not. The numbers stop at `objectUniquenessMaxAttempts`, after which generation fails, and
`objectIgnoreErrors(true)` leaves the field `null`. A `createdAt`, `updatedAt`, `birthDate`, `age`,
`price`, `amount`, `balance`, `active` or `status` cannot be numbered, so a requested one keeps its
generated value and the other field of the pair adapts where it can. If that field cannot change either,
the two stay out of line, because uniqueness wins.

## Fluent fixtures

Use `ObjectFaker<T>` when the default object graph is close but you want explicit rules. JavaBean
getter and record-accessor references provide refactor-safe root paths; `PropertyPath.then(...)`
composes nested paths without string literals:

```java
ObjectFaker<UserDto> faker = new ObjectFaker<>(UserDto.class)
        .ruleFor(UserDto::getFirstName, () -> "Ada")
        .ruleFor(UserDto::getLastName, () -> "Lovelace")
        .ruleFor(PropertyPath.of(UserDto::getAddress).then(Address::getCity), () -> "London")
        .ruleFor(UserDto::getEmail,
                 user -> user.getFirstName().toLowerCase() + "."
                         + user.getLastName().toLowerCase() + "@example.com")
        .ignore(UserDto::getPassword)
        .profile("minimal", configured -> configured.include("firstName", "email"))
        .useProfile("minimal");

UserDto user = faker.generate();
List<UserDto> users = faker.generateList(100);
```

Supported in the current fixture layer:

- path-aware `ruleFor(...)` with plain generators, including nested paths such as `address.city`
- type-safe getter and record-accessor rules, with nested `PropertyPath` composition
- path-aware dependent `ruleFor(...)` based on the generated root object
- path-aware `ruleForContext(...)` with nested owner/depth metadata
- path-aware `ignore(...)`, including nested paths such as `address.city`
- path-aware `include(...)` to whitelist root fields or prune nested payload slices such as `address.city`
- named `profile(...)` plus `useProfile(...)` for reusable rule bundles
- a later rule for the identical path replaces the earlier one (a same-kind replacement keeps its
  place), so a profile or a composed `ObjectModel` can be refined per test; rules on nested paths
  below it still apply unless the new rule is a dependent rule on the parent, which runs last
- `afterGenerate(...)` / `postProcess(...)`
- `populate(existing)` for mutable classes
- `strict()` to validate the complete explicit rule/include/ignore decision before generation

Use `ObjectModel<T>` when a fixture definition must be immutable, reusable, and composable:

```java
ObjectModel<UserDto> names = ObjectModel.of(UserDto.class)
        .configure(faker -> faker
                .ruleFor(UserDto::getFirstName, () -> "Ada")
                .ruleFor(UserDto::getLastName, () -> "Lovelace"));

ObjectModel<UserDto> contact = ObjectModel.of(UserDto.class)
        .configure(faker -> faker.ruleFor(
                UserDto::getEmail,
                user -> user.getFirstName().toLowerCase() + "@example.com"));

UserDto user = names.and(contact).generate(config);
```

Each model creates a fresh `ObjectFaker`; stateful field generators should be constructed inside
the model configuration callback rather than captured and shared.

Nested `include(...)` paths keep the parent object alive and clear sibling fields below that path unless you also include the wider root object.

`ObjectFaker<T>` reuses the same root `GeneratorConfig`, semantic resolver, locale, seed, and override rules as `ObjectGenerator<T>`.

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
