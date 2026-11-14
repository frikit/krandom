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
precedence over type-level overrides, contextual or not. A field-level override, contextual or not, and
an `ObjectFaker` rule, by field name, accessor, or path, may return `null` to leave a reference field
`null`, so the type argument of its generator may be nullable, such as
`ContextualGenerator<@Nullable String>`, or `ContextualGenerator<String?>` in Kotlin, and so may the
property type of a `PropertySelector` or `PropertyPath`.

| Target | Safe construction path | If unavailable |
|:---|:---|:---|
| Java record | Canonical constructor | Contextual construction failure |
| Mutable class with a no-argument constructor | No-argument constructor, then mutable fields | Contextual construction/access failure |
| Class with exactly one declared constructor | Generated constructor arguments (by parameter name with `-parameters`, otherwise by position), then mutable fields | Contextual construction/access failure |
| Class with multiple declared constructors | No implicit choice | Register a type factory |
| Sealed interface or abstract sealed class | One of its concrete subclasses: see [Sealed types](#sealed-types) | Register a type factory or `objectSubtype` mapping |
| Other interface or abstract root | Its `objectSubtype` mapping, otherwise no reflective allocation | Register a type factory |
| Local, anonymous, non-static inner, enum, annotation, array, or primitive root | Unsupported | Use a supported wrapper/factory design |

Constructor parameters behave like the fields they initialize when the class is compiled with
`-parameters`: by-name overrides, `objectExcludeField` on the parameter name (or `@Exclude` on the
backing field), Bean Validation constraints, and semantic providers such as `firstName` or `email`
all apply. Without compiled parameter names the engine addresses arguments by position, so
`objectOverride(Money.class, "constructorArg0", ...)` targets the first argument and exclusions by
name have no effect.

An argument meets the Bean Validation constraints declared on its parameter and on the field it
initializes: the nearest field with the parameter's name and declared type, on the class or a
superclass. So a constraint on a final field reaches the argument that fills it:

```java
final class Account {
    @Size(min = 3, max = 3) private final String code;
    private final List<@Email String> contacts;

    Account(String code, List<String> contacts) { // code has three characters, each contact is an email
        this.code = code;
        this.contacts = contacts;
    }
}
```

The field's getter counts as part of the field, as it does for field population. Where the parameter
declares a constraint of the same type as the field, the parameter's applies, and where its type
arguments declare any constraints, as in `List<@Size(max = 5) String>`, its container-element
constraints apply instead of the field's. A field of another type, such as a `String` filled from an
`int` parameter, adds nothing, and nor does a static field.

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
- `STRICT`: semantic field names win whenever a semantic match exists, over `@Randomizer`, `@Fake`, and
  `@FakeRange` too, and the coherence pass can overwrite annotated fields. Bean Validation constraints still
  take precedence when a value is generated, and the pass writes a derived value into a constrained field only
  where the field's own constraints allow it: see [coherence and constraints](#coherence-and-constraints).
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

A `Calendar` or `GregorianCalendar` field is a non-lenient `GregorianCalendar` with ISO week rules, as `Generators.ofCalendar()` gives, drawn as a `ZonedDateTime` field is: in a random zone, with the range on its local date, and with the same semantic windows for `createdAt`, `updatedAt`, and `birthDate`.

Object generation also runs a lightweight coherence pass after sibling values are generated. Common pairs such as `firstName` + `lastName` -> `fullName`, `firstName` + `lastName` + `domain` -> `email`, `domain` -> `url`, `createdAt` / `updatedAt`, `birthDate` / `age`, and `active` / `status` are aligned automatically. Address-like clusters now reuse one locale-backed address snapshot so `streetAddress`, `city`, `state`, `postalCode`, and `country` stay consistent with each other, and money-like clusters keep `price <= amount <= balance` while deriving missing sibling values when one amount is already present. A value moved to keep that order is rounded toward it, down in a field that stays at or below its sibling and up in one that stays at or above, to whole units in an integer field and to cents in any other, with a `float` or a `double` read as it prints. A field whose type cannot hold the value, such as a `byte` beside a price of 300, keeps its own, and a money string keeps every digit of its amount when it takes the currency code. In `RELAXED` mode, that pass still leaves a field with `@Randomizer`, `@Fake`, `@FakeRange`, or a Bean Validation constraint alone. In `STRICT` mode it can overwrite them, but it writes a derived value into a constrained field only where the field's own constraints allow it: see [coherence and constraints](#coherence-and-constraints). Explicit field and type overrides always win, and so does a field initializer that generation keeps, which the pass writes only where it is blank. Generated emails keep to the [email-domain contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#email-domain-contract), and generated domains and URLs to the [network contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#network-contract): a generated `domain` is reserved, so an email built from name fields beside it takes that domain, and a `companyName` derives a `.test` domain.

### Coherence and constraints

In `RELAXED` mode the coherence pass leaves a field with `@Randomizer`, `@Fake`, `@FakeRange`, or a Bean
Validation constraint alone, as generation lets each of them win over field-name semantics. In `STRICT` mode
the pass overwrites an annotated field, and writes a derived value into a field with a Bean Validation constraint only
where the value meets the field's own constraints, read as Hibernate Validator reads them. Where it would break
one, the field keeps the value it was generated with, which meets them, as a field the pass cannot assign does.
So a `@Size(max = 12)` `email` keeps its generated value rather than take `ada.lovelace@example.com`, and a
`@Size(min = 3, max = 3)` `fullName` rather than take `Ada Lovelace`, which a `@Size(max = 20)` `fullName` takes.
A unique field's numbered value must meet the constraints too. A sibling that depends on such a field follows
it where it can: a `price` above what a `@Max(100)` `amount` allows moves down to the amount, and an `active`
flag follows a `@Size(max = 6)` `status` that cannot take `INACTIVE`. Where neither can move, both keep their
values, as two overrides do. Dates and ages follow the same rule: see
[date ranges and coherence](#date-ranges-and-coherence).

### Date ranges and coherence

Aligning `createdAt` and `updatedAt` keeps both in a configured `objectDateRange`, by the date each type
reads: the local date of an `OffsetDateTime`, `ZonedDateTime` or `Calendar`, and the UTC date of every other
type, as the generators draw them. `updatedAt` moves to `createdAt`'s instant and reads it at UTC. Where that date
is outside the range:

- An `OffsetDateTime`, `ZonedDateTime` or `Calendar` `updatedAt` reads the instant at the offset nearest UTC, in
  15-minute steps, that gives a date in the range. So it can have an offset unlike `createdAt`'s, as in
  `2024-02-29T23:45-11:15` beside `2024-02-29T23:00-12:00`. An `OffsetDateTime` also stays within `-12:00` to
  `+14:00`, as in the
  [UTC-offset contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#utc-offset-contract).
- Otherwise `updatedAt` stays, and `createdAt` moves to its instant, by the same steps. So an
  `OffsetDateTime` `createdAt` beside a `LocalDate` `updatedAt` can become midnight UTC.

`createdAt` stays not after `updatedAt`, compared by instant, or by UTC date beside an `updatedAt` that holds
only a date. A `birthDate` stays in the range, and `age` follows it: the whole years from the date `birthDate`
reads, by the same rule, to the configured clock's date. So an `OffsetDateTime` `birthDate` of
`2000-06-05T00:30+14:00` gives an `age` of 25 on `2026-06-04`, although that instant falls on `2000-06-04` at UTC.

A field that cannot move, because of an override, a requested unique name, or an annotation or Bean
Validation constraint in `RELAXED` mode, need not be in the range: a `@Past` value is drawn before the clock
whatever the range. Where it leaves no date in the range that keeps the pair in order, its sibling follows it
out of the range, because the order wins. A `birthDate` derived from such an `age` keeps that age, so it can
be out of the range too, unless no date of its type can hold it, as for an `age` of 2,147,483,647: then
`birthDate` keeps its value.

In `STRICT` mode a constrained `createdAt`, `updatedAt`, `birthDate`, or `age` can move, but never out of its
own Bean Validation constraints, read against the configured clock as Hibernate Validator reads them. Where a
move would break one, the field cannot move, as above. So a `@Past` or `@PastOrPresent` `updatedAt` does not
take a `createdAt` that is past the clock, and `createdAt` moves back to it instead. A `@Future` or
`@FutureOrPresent` `createdAt` does not follow an `updatedAt` into the past. An `age` does not take the age its
`birthDate` gives where `@Min`, `@Max`, or `@Pattern` rejects it, and `birthDate` follows the `age` instead.
Where the sibling's own constraints stop it following too, both keep their values, in either mode: a `@Future`
`createdAt` stays after a `@Past` `updatedAt`, and a `@Past` `birthDate` beside an overridden `age` of 0 keeps
its date, as today is not past.

A gap in a zone's rules moves a date-time forward. Where that would take a field past the last date of the
range, a `ZonedDateTime` or `Calendar` holds the drawn date-time at UTC instead, and an `OffsetDateTime` holds
it at its drawn offset. `Pacific/Apia` and `Pacific/Fakaofo`, for example, skipped `2011-12-30`, and a daylight
saving gap in `America/Nuuk` skipped 23:00 to midnight on `2024-03-30`. `new ZonedDateTimeGenerator(min, max)`
and `Generators.ofCalendar(min, max)` hold such a value at UTC too. A `java.sql.Date` reads the JVM's default
zone, so on a JVM in `Pacific/Apia` or `Pacific/Fakaofo` the date `2011-12-30` reads `2011-12-31`.

## Bean Validation

Object population natively composes 21 Jakarta Validation constraints across fields, getters,
records, interface accessors, and constructor parameters. See the [Bean Validation support matrix]({{ '/guides/bean-validation/' | relative_url }})
for exact target types, intersection rules, precedence, and exclusions.

## Sealed types

A sealed interface or abstract sealed class, as a root or as a member, is generated as one of the
concrete classes of its hierarchy, so it needs no `objectSubtype` mapping or type factory:

```java
sealed interface Shape permits Square, Circle {}
record Circle(double radius) implements Shape {}
record Square(double side) implements Shape {}
record Drawing(Shape shape) {}

GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
Drawing drawing = new ObjectGenerator<>(Drawing.class, config).generate();
// drawing.shape() is a Circle or a Square, and the same one for the same seed
```

Each class is equally likely, and the choice is drawn from the seeded stream in binary-name order rather
than the order the type permits them, so the same seed chooses the same classes. The classes are:

- each permitted subclass that is concrete, a record, or an enum, as itself. An enum gives one of its
  constants, and a concrete subclass that is sealed too is generated as itself, not as its own subclasses.
- the classes of each permitted subclass that is a sealed interface or abstract class, at any depth. A
  class reached through two sealed parents is one choice.

A type override or `objectSubtype` mapping for the sealed type wins over the choice. A permitted subclass
with a type override is a choice that its override generates, and one with an `objectSubtype` mapping is
a choice of the mapped class. A permitted subclass that is an interface or abstract class but not sealed,
such as a Java `non-sealed` interface, needs one of the two: without it, generation fails as an unsupported
type that names the subclass, whatever the seed. A sealed type with no class to generate, such as a Kotlin
sealed interface with no subclass, fails the same way. With `objectIgnoreErrors(true)` the member stays
`null` and `generationFailureListener` is told.

A class already being generated on the member's path is left out of the choice while another class
remains, so a recursive hierarchy ends in a leaf. For `sealed interface Expr permits Num, Add` and
`record Add(Expr left, Expr right)`, a generated `Add` holds two `Num` values. Where every class is on the
path, the cycle is cut as [any other cycle](#depth-limit) is. The chosen class counts as being generated as
its own type, with the type arguments the member gives it, so a member of that type inside it, such as the
`next` of `record Link<T>(Link<T> next) implements Rope<T>` chosen for a `Rope<String>`, is cut as a cycle
too.

A sealed root is generated from its seed as a root of the chosen class would be, except that the chosen
class counts as being generated, so the members of a recursive root leave it out of their choices while
another class remains. A type override for the chosen class generates it, an enum included. A generic
subclass takes the type arguments of the declared type: a `Result<String>` member generated as
`record Ok<T>(T value) implements Result<T>` holds a `String`. The arguments are passed as declared and not
checked against the subclass. A root is a raw class, so the subclasses of a generic sealed root take no
arguments, and one that uses its type parameters fails as an unsupported type. The implementation of an
`objectSubtype` mapping takes the arguments of a generic member in the same way.

A concrete sealed class is generated as itself, and a sealed JDK type, such as
`java.lang.constant.ConstantDesc`, is not generated. The `krandom-kotlin-dsl` module reads the
subclasses of a Kotlin sealed type from its metadata and builds them, as the
[Kotlin DSL guide]({{ '/guides/kotlin-dsl/' | relative_url }}#sealed-types-and-value-classes) describes.

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
  constructor parameter, or on the field that a constructor parameter initializes, such as a final
  field filled through the constructor.
- `@NotEmpty` on a collection class that is generated as a nested object, such as a custom
  `AbstractCollection`. Above the limit such a class is built through its constructor and not filled,
  so it can still be empty. `@NotEmpty` and `@NotBlank` are rejected on other nested object types.
- `@Size` alone does not count, because Bean Validation accepts `null` for it.

A required member that no depth could generate, such as an interface that is not sealed and has no
`objectSubtype` mapping, fails as an unsupported type, as it does above the limit.

Collections, arrays, maps and optionals are not cut off at the limit, but the nested objects inside them
are: a `List<Leaf>` holds `null` elements, and a `Map<Leaf, String>` and an `Optional<Leaf>` are empty.
A constraint on a type argument, such as `List<@NotNull Leaf>`, does not change this.

A container that cannot hold `null` cannot hold a cut element either. A `Queue<Leaf>` or `Deque<Leaf>`,
which is generated as an `ArrayDeque`, a concrete queue such as `ArrayDeque<Leaf>`, a `PriorityQueue`, and
a `TreeSet`, `SortedSet` or `NavigableSet` of `Comparable` elements therefore fail as any collection that
rejects an element does: with `COLLECTION_INSERTION` context at the container's path, such as
`Top.mid.queue`. With `objectIgnoreErrors(true)`, the whole container is `null` and
`generationFailureListener` is told. A `LinkedList` is a list, so it holds the `null` elements.

Cycles are cut separately. Below the limit, a member that would repeat a nested type already being
generated, such as `next.next` of a self-typed `@NotNull Node next`, is left `null` whatever its
constraints. At the limit it fails like any other required member, and a larger `objectMaxDepth` then
lets generation succeed without giving it a value. An element cut this way is `null` too, so a queue of
the cycle's type fails as above, and so does a sorted set of it when the type is `Comparable`.

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
`ObjectFaker` rule with a generator is an override. A rule on a nested path, a nested context rule and a
dependent rule run after generation, and their values are checked as well: a repeat applies the rule again,
a dependent rule to the same object, up to `objectUniquenessMaxAttempts` times. A rule may give an object the
value it already holds, which repeats nothing, and the value it replaces is given back where generation gave
it to that member, but not where the class set it. `postProcess` runs last, and its values are not checked.

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
- `ObjectFaker.strict()` to check the rules before every generation, as described below

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

### Root rules that keep the recipe (3.2+)

A root `ruleFor(path, generator)`, a root `ruleForContext(...)`, a root `ignore(...)` and `include(...)` are
installed in the configuration that generation runs with, as overrides and exclusions that no recipe can
record. With one of them the faker generates without a portable recipe: its report gives no recipe format,
its failure diagnostics no replay identity, and under the default `ObjectFieldStreamPolicy.LEGACY` the other
members draw from one shared sequence instead of their named streams, so every seeded member changes. Nested
and dependent rules run after generation, so they keep the recipe and the other values already.

`ruleForKeepingRecipe(path, generator)`, with the same accessor and `PropertyPath` forms as `ruleFor`, is a
root rule that keeps them too:

```java
GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();

UserDto plain = new ObjectFaker<>(UserDto.class, config).generate();
UserDto pinned = new ObjectFaker<>(UserDto.class, config)
        .ruleForKeepingRecipe(UserDto::getNickname, () -> "ada")
        .generate();
// pinned differs from plain only in its nickname.
```

It is installed and applied as `ruleFor` is, wherever generation fills its member, so the strict check
reports it as a root rule. But generation keeps the recipe of the faker's `GeneratorConfig` and its named
streams, so a seeded object differs only in the members the rule supplies, and in what semantic coherence
derives from them, such as an email from a name. Replaying the recipe with the same faker gives the same
values, as it does for nested and dependent rules; the rule's own generator replays only if it is seeded or
constant. The recipe is kept while every root rule comes from `ruleForKeepingRecipe`: a root `ruleFor`, a
root `ruleForContext`, a root `ignore` or an `include` drops it as before, and so does a configuration with
no portable recipe of its own. Existing rules keep their behaviour, so existing seeded values stay the same.

### Checking fixture rules with `ObjectFaker.strict()`

`ObjectFaker.strict()` checks the configuration before every `generate()` and `populate(existing)`, and
`assertConfigurationIsValid()` runs the same check on demand. Either fails with one `FixtureRuleException`
that lists every problem at once. Each problem is a `FixtureRuleProblem` that names its path, its rule kind
and its registration number, never a value. `ObjectFaker.strict()` has nothing to do with
`ObjectGenerationSemanticMode.STRICT`.

```java
ObjectFaker<OrderDto> faker = new ObjectFaker<>(OrderDto.class)
        .ruleFor("customer.address.city", () -> "London")
        .ruleFor("customer", order -> new CustomerDto())
        .strict();

faker.generate();
// FixtureRuleException: ObjectFaker for com.example.OrderDto has 2 fixture rule problems:
// - UNDECIDED root properties [status]: each needs a rule, an include or an ignore
// - SHADOWED rule #1 'customer.address.city': dependent rule #2 'customer' runs after it and replaces
//   the whole of 'customer'
```

Rules, includes and ignores are numbered in the order they are registered, across profiles and composed
`ObjectModel` values. The check reports:

| Problem | Reported when |
|:---|:---|
| Undecided | A settable root field or record component has no rule, include or ignore. In include mode only the included ones need a rule. |
| Shadowed | A later rule for the same path replaces a rule. A rule on an ancestor path that runs later overwrites a nested rule. A `GeneratorConfig` contextual field override or contextual predicate override comes before a root rule. A root rule replaces or comes before a `GeneratorConfig` field override for the same member, or a nested or dependent rule overwrites one. |
| Unused | A later nested ignore clears the path of a rule. The `GeneratorConfig` excludes the member of a root rule. A `GeneratorConfig` type override builds the root, so `generate()` applies no root rule or root ignore. For `generate()`, a nested ignore clears a member that never gets a value, because `objectMaxDepth`, an exclusion or include mode leaves an object on its path `null`. |
| Ambiguous | A root rule, a root ignore or an include-mode exclusion also applies at another path. |

Rules run in this order: root rules and root context rules during generation, as field overrides; then
nested rules, nested context rules and dependent rules, each kind in registration order; then nested
includes prune and nested ignores clear. A root rule, a root ignore and include mode apply by the member's
declaring class and name, so they also reach that member in a nested object of the root's type, or of
another subclass of the class that declares an inherited member: `ruleFor("id", () -> 42L)` on
`Order extends BaseEntity` also sets the `id` of a `Customer extends BaseEntity` generated inside it. A dependent rule,
such as `ruleFor("id", order -> 42L)`, applies at its own path only. A nested rule past `objectMaxDepth` is
used: it creates the objects on its path that generation leaves `null`, there or at a cycle or an
exclusion, each as a root of its own, so a root rule, a root ignore and include mode also reach their
members, and the check lists those paths too.

A rule that replaces an earlier one for the same path is how a profile or a shared `ObjectModel` is refined
per test, and `ObjectFaker.strict()` reports the earlier rule as shadowed, so leave `ObjectFaker.strict()`
out of fixtures that refine rules that way. To see which members keep an initializer, the check creates one
object of each class it follows with the class's no-argument constructor, as generation does. Without
`ObjectFaker.strict()`, generation is unchanged.

The check takes a constructor to be free to replace the arguments that generation passes it, so it does not
follow a parent behind a constructor argument or a record component: where generation passes `null` for
such a member, past `objectMaxDepth`, at a `null` draw or as an excluded argument, a nested rule builds the
parent as a root of its own, and a root rule that reaches its members goes unreported. From 3.2,
`followConstructorArguments()` makes the check take every constructor to store its arguments as given, as a
record's canonical constructor does, and report those routes too:

```java
record Node(String label, Node child) {}

new ObjectFaker<>(Node.class, GeneratorConfig.builder().objectNullProbability(1.0).build())
        .ruleFor("label", () -> "x")
        .ruleFor("child.child.label", () -> "y")
        .followConstructorArguments()
        .assertConfigurationIsValid();
// FixtureRuleException: - AMBIGUOUS rule #1 'label': ObjectFaker installs it for Node.label wherever
//   generation reaches that member, so it also applies at 'child.label', 'child.child.label'
```

It is an opt-in because the check can then find more problems, so `ObjectFaker.strict()` can newly fail, and
because a constructor that replaces a `null` argument, such as with a new object, gets a report of a route
that generation never takes. Generation is unchanged either way.

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
