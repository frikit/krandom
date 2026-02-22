# easy-random Reference

**Repository:** https://github.com/j-easy/easy-random
**Author:** Mahmoud Ben Hassine
**License:** MIT
**Status:** Maintenance mode (since November 2020) — bug fixes only
**Stable releases:** 5.0.x (Java 11+), 4.3.x (Java 8)
**Maven coordinates:** `org.jeasy:easy-random-core`

---

## 1. Purpose

Easy Random generates random instances of arbitrary Java classes by traversing and populating entire object graphs recursively. It implements the **ObjectMother** pattern for the JVM.

**Primary use cases:**

- Generating test fixtures without hand-crafting data builders
- Populating databases with random domain objects at scale
- Testing algorithms (sorting, persistence, serialization) where specific values do not matter
- Load testing REST services and batch applications

```java
EasyRandom easyRandom = new EasyRandom();
Person person = easyRandom.nextObject(Person.class);
Stream<Person> people = easyRandom.objects(Person.class, 100);
```

---

## 2. Core API

### `EasyRandom` (extends `java.util.Random`)

```java
public class EasyRandom extends Random {
    public EasyRandom() { }
    public EasyRandom(EasyRandomParameters parameters) { }

    public <T> T nextObject(Class<T> type) { }
    public <T> Stream<T> objects(Class<T> type, int streamSize) { }
}
```

Because it extends `java.util.Random`, it is seeded and reproducible like any `Random` instance.

**Internal `nextObject` pipeline:**

1. If type is a Java record, randomize each component and invoke the canonical constructor.
2. Check `ExclusionPolicy.shouldBeExcluded(type)` — return null if excluded.
3. Look up a `Randomizer` for the type via `RandomizerProvider` — short-circuit if found.
4. Handle non-introspectable types (enum, array, collection, map) with dedicated populators.
5. Check object pool (recursion guard) — return a cached instance if pool is full.
6. Instantiate via `ObjectFactory` (default: Objenesis).
7. Register new instance in the pool.
8. Collect all declared + inherited fields (skip static fields, skip inner-class `this$0`).
9. For each field: delegate to `FieldPopulator.populateField()`.

### `EasyRandomParameters`

Fluent configuration object. All setters return `this`.

| Parameter                       | Default                          | Description                                                       |
|---------------------------------|----------------------------------|-------------------------------------------------------------------|
| `seed`                          | `123L`                           | Deterministic seed for reproducibility                            |
| `charset`                       | `US_ASCII`                       | Used for `String`/`char` generation                               |
| `objectPoolSize`                | `10`                             | Max cached instances per type (recursion guard)                   |
| `randomizationDepth`            | `Integer.MAX_VALUE`              | Max depth of object graph                                         |
| `collectionSizeRange`           | `[1, 100]`                       | Min/max element count for collections and arrays                  |
| `stringLengthRange`             | `[1, 32]`                        | Min/max character count for strings                               |
| `dateRange`                     | `[2010-01-01, 2030-01-01]`       | Range for date/time types                                         |
| `timeRange`                     | `[LocalTime.MIN, LocalTime.MAX]` | Range for time-only types                                         |
| `scanClasspathForConcreteTypes` | `false`                          | Scan classpath for concrete subtypes of abstract/interface fields |
| `overrideDefaultInitialization` | `false`                          | Randomize fields that already have a non-null/non-default value   |
| `ignoreRandomizationErrors`     | `false`                          | Silently set field to null on error instead of throwing           |
| `bypassSetters`                 | `false`                          | Use direct field reflection instead of setter methods             |

```java
EasyRandomParameters parameters = new EasyRandomParameters()
    .seed(42L)
    .charset(StandardCharsets.UTF_8)
    .objectPoolSize(50)
    .randomizationDepth(5)
    .collectionSizeRange(2, 10)
    .stringLengthRange(5, 20)
    .dateRange(LocalDate.of(2000, 1, 1), LocalDate.of(2025, 12, 31))
    .timeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))
    .scanClasspathForConcreteTypes(true)
    .overrideDefaultInitialization(true)
    .ignoreRandomizationErrors(true)
    .bypassSetters(true)
    // Type-level randomizer (applies to all fields of this type)
    .randomize(String.class, () -> "fixed-value")
    // Field-level randomizer (applies to matching fields only)
    .randomize(
        FieldPredicates.named("email").and(FieldPredicates.inClass(Person.class)),
        new EmailRandomizer()
    )
    // Exclude specific fields
    .excludeField(FieldPredicates.named("password"))
    // Exclude entire types
    .excludeType(TypePredicates.inPackage("com.example.internal"))
    // Custom registry, policy, factory, provider
    .randomizerRegistry(new MyRegistry())
    .exclusionPolicy(new MyExclusionPolicy())
    .objectFactory(new MyObjectFactory())
    .randomizerProvider(new MyRandomizerProvider());
```

---

## 3. Extension Points

All extension points are interfaces in `org.jeasy.random.api`.

### `Randomizer<T>`

Core functional interface. Any lambda qualifies.

```java
@FunctionalInterface
public interface Randomizer<T> {
    T getRandomValue();
}

// Lambda usage
parameters.randomize(String.class, () -> "hello");
```

### `ContextAwareRandomizer<T>`

Receives the full randomization context before `getRandomValue()` is called.

```java
public interface ContextAwareRandomizer<T> extends Randomizer<T> {
    void setRandomizerContext(RandomizerContext context);
}
```

### `RandomizerContext`

Read-only view of in-flight state, passed to context-aware randomizers.

```java
public interface RandomizerContext {
    Class<?> getTargetType();               // top-level type passed to nextObject()
    Object getRootObject();                 // root object being built
    Object getCurrentObject();              // object currently being populated
    String getCurrentField();               // full dotted path, e.g. "person.address.city"
    int getCurrentRandomizationDepth();     // current recursion depth
    EasyRandomParameters getParameters();
}
```

### `RandomizerRegistry`

Groups randomizers together. Discovered via `ServiceLoader` (SPI) or registered via `EasyRandomParameters.randomizerRegistry()`.

```java
public interface RandomizerRegistry {
    void init(EasyRandomParameters parameters);
    Randomizer<?> getRandomizer(Field field);
    Randomizer<?> getRandomizer(Class<?> type);
}
```

### `RandomizerProvider`

Strategy for resolving randomizers. Default: `RegistriesRandomizerProvider`.

```java
public interface RandomizerProvider {
    Randomizer<?> getRandomizerByField(Field field, RandomizerContext context);
    <T> Randomizer<T> getRandomizerByType(Class<T> type, RandomizerContext context);
    void setRandomizerRegistries(Set<RandomizerRegistry> registries);
}
```

### `ExclusionPolicy`

Strategy for deciding what to skip.

```java
public interface ExclusionPolicy {
    boolean shouldBeExcluded(Field field, RandomizerContext context);
    boolean shouldBeExcluded(Class<?> type, RandomizerContext context);
}
```

Default (`DefaultExclusionPolicy`) always skips static fields and predicate-matched fields.

### `ObjectFactory`

Strategy for instantiating objects.

```java
public interface ObjectFactory {
    <T> T createInstance(Class<T> type, RandomizerContext context)
        throws ObjectCreationException;
}
```

Default (`ObjenesisObjectFactory`): tries no-arg constructor first; falls back to Objenesis. When `scanClasspathForConcreteTypes` is enabled, picks a random concrete subtype.

---

## 4. Annotations

All in `org.jeasy.random.annotation`.

### `@Exclude`

Skips a field entirely.

```java
class Person {
    private String name;
    @Exclude
    private int age; // never randomized
}
```

### `@Randomizer`

Declares a `Randomizer` implementation to use for a specific field. Supports typed constructor arguments via `@RandomizerArgument`.

```java
class Person {
    @Randomizer(EmailRandomizer.class)
    private String email;

    @Randomizer(
        value = IntegerRangeRandomizer.class,
        args = {
            @RandomizerArgument(value = "18", type = Integer.class),
            @RandomizerArgument(value = "99", type = Integer.class)
        }
    )
    private int age;
}
```

### `@Priority`

Placed on `RandomizerRegistry` implementations to control lookup order. Higher value = higher priority. User registries with `@Priority > 0` override all built-ins.

---

## 5. Field and Type Predicates

### `FieldPredicates` → `Predicate<Field>`

| Method                                            | Description                               |
|---------------------------------------------------|-------------------------------------------|
| `named(String pattern)`                           | Match field name against regex            |
| `ofType(Class<?>)`                                | Match exact declared type                 |
| `inClass(Class<?>)`                               | Match fields declared in a specific class |
| `isAnnotatedWith(Class<? extends Annotation>...)` | Match fields carrying annotation(s)       |
| `hasModifiers(int)`                               | Match fields with given `Modifier` bits   |

### `TypePredicates` → `Predicate<Class<?>>`

| Method                                            | Description                          |
|---------------------------------------------------|--------------------------------------|
| `named(String)`                                   | Match by fully-qualified name        |
| `ofType(Class<?>)`                                | Match exact type                     |
| `inPackage(String prefix)`                        | Match types in a package             |
| `isAnnotatedWith(Class<? extends Annotation>...)` | Match annotated types                |
| `isInterface()`                                   | Match interfaces                     |
| `isPrimitive()`                                   | Match primitives                     |
| `isAbstract()`                                    | Match abstract classes               |
| `isEnum()`                                        | Match enumerations                   |
| `isArray()`                                       | Match array types                    |
| `isAssignableFrom(Class<?>)`                      | Match subtypes                       |
| `hasModifiers(int)`                               | Match types with given modifier bits |

Both are composable via `and()`, `or()`, `negate()`.

---

## 6. Built-in Randomizers

### Realistic string data (backed by DataFaker) — `org.jeasy.random.randomizers`

All extend `FakerBasedRandomizer<String>` and accept `(long seed)` or `(long seed, Locale locale)`.

`CityRandomizer`, `CompanyRandomizer`, `CountryRandomizer`, `CreditCardNumberRandomizer`, `EmailRandomizer`, `FirstNameRandomizer`, `FullNameRandomizer`, `GenericStringRandomizer`,
`Ipv4AddressRandomizer`, `Ipv6AddressRandomizer`, `IsbnRandomizer`, `LastNameRandomizer`, `LatitudeRandomizer`, `LongitudeRandomizer`, `MacAddressRandomizer`, `ParagraphRandomizer`,
`PasswordRandomizer`, `PhoneNumberRandomizer`, `RegularExpressionRandomizer`, `SentenceRandomizer`, `StateRandomizer`, `StreetRandomizer`, `WordRandomizer`, `ZipCodeRandomizer`

### Misc — `org.jeasy.random.randomizers.misc`

| Randomizer              | Output              | Notes                            |
|-------------------------|---------------------|----------------------------------|
| `BooleanRandomizer`     | `Boolean`           |                                  |
| `ConstantRandomizer<T>` | `T`                 | Always returns the same value    |
| `EnumRandomizer<T>`     | `T extends Enum<T>` | Random enum constant             |
| `LocaleRandomizer`      | `Locale`            |                                  |
| `NullRandomizer`        | `null`              | Always null                      |
| `OptionalRandomizer<T>` | `Optional<T>`       |                                  |
| `SkipRandomizer`        | `null`              | Null Object — leaves field unset |
| `UUIDRandomizer`        | `UUID`              |                                  |

### Numbers — `org.jeasy.random.randomizers.number`

`ByteRandomizer`, `ShortRandomizer`, `IntegerRandomizer`, `LongRandomizer`, `FloatRandomizer`, `DoubleRandomizer`, `BigIntegerRandomizer`, `BigDecimalRandomizer`, `AtomicIntegerRandomizer`,
`AtomicLongRandomizer`, `NumberRandomizer`

### Ranges — `org.jeasy.random.randomizers.range`

All extend `AbstractRangeRandomizer<T>` and take `(T min, T max)` or `(T min, T max, long seed)`.

`ByteRangeRandomizer`, `ShortRangeRandomizer`, `IntegerRangeRandomizer`, `LongRangeRandomizer`, `FloatRangeRandomizer`, `DoubleRangeRandomizer`, `BigDecimalRangeRandomizer`,
`BigIntegerRangeRandomizer`, `DateRangeRandomizer`, `SqlDateRangeRandomizer`, `InstantRangeRandomizer`, `LocalDateRangeRandomizer`, `LocalDateTimeRangeRandomizer`, `LocalTimeRangeRandomizer`,
`OffsetDateTimeRangeRandomizer`, `OffsetTimeRangeRandomizer`, `ZonedDateTimeRangeRandomizer`, `YearRangeRandomizer`, `YearMonthRangeRandomizer`

### Date/time — `org.jeasy.random.randomizers.time`

`CalendarRandomizer`, `DateRandomizer`, `DurationRandomizer`, `GregorianCalendarRandomizer`, `InstantRandomizer`, `LocalDateRandomizer`, `LocalDateTimeRandomizer`, `LocalTimeRandomizer`,
`MonthDayRandomizer`, `OffsetDateTimeRandomizer`, `OffsetTimeRandomizer`, `PeriodRandomizer`, `SqlDateRandomizer`, `SqlTimeRandomizer`, `SqlTimestampRandomizer`, `TimeZoneRandomizer`,
`YearMonthRandomizer`, `YearRandomizer`, `ZoneIdRandomizer`, `ZoneOffsetRandomizer`, `ZonedDateTimeRandomizer`

### Collections — `org.jeasy.random.randomizers.collection`

| Randomizer             | Output       |
|------------------------|--------------|
| `ListRandomizer<T>`    | `List<T>`    |
| `SetRandomizer<T>`     | `Set<T>`     |
| `QueueRandomizer<T>`   | `Queue<T>`   |
| `EnumSetRandomizer<T>` | `EnumSet<T>` |
| `MapRandomizer<K,V>`   | `Map<K,V>`   |

### Network — `org.jeasy.random.randomizers.net`

`UriRandomizer`, `UrlRandomizer`

### Text — `org.jeasy.random.randomizers.text`

`CharacterRandomizer`, `StringRandomizer`, `StringDelegatingRandomizer`

---

## 7. Supported Java Types Out of the Box

### Primitives and boxed types

`boolean`/`Boolean`, `byte`/`Byte`, `short`/`Short`, `int`/`Integer`, `long`/`Long`, `float`/`Float`, `double`/`Double`, `char`/`Character`

### Standard types

`String`, `BigInteger`, `BigDecimal`, `AtomicInteger`, `AtomicLong`, `UUID`, `Locale`, `URI`, `URL`

### Legacy date/time

`java.util.Date`, `java.util.Calendar`, `java.util.GregorianCalendar`, `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`, `java.util.TimeZone`

### JSR 310 (Java 8 date/time)

`Duration`, `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `MonthDay`, `OffsetDateTime`, `OffsetTime`, `Period`, `Year`, `YearMonth`, `ZonedDateTime`, `ZoneId`, `ZoneOffset`

### Enums

Random constant selected from `EnumType.values()`.

### Arrays

Populated with randomized elements within `collectionSizeRange`.

### Collections (JCF)

`List`/`ArrayList`/`LinkedList`, `Set`/`HashSet`/`LinkedHashSet`/`TreeSet`, `Queue`/`ArrayDeque`/`PriorityQueue`, `Deque`. Parameterized collections are fully populated; raw types left empty.

### Maps

`Map`/`HashMap`/`LinkedHashMap`/`TreeMap`/`Hashtable`/`WeakHashMap`/`IdentityHashMap`/`EnumMap`

### Optional

`Optional<T>` — handled by `OptionalPopulator`.

### Records (Java 16+)

Components randomized individually; canonical constructor invoked.

### `java.lang.Class`

Always skipped (returns null) — see issue #280.

---

## 8. Registry Resolution Order

| Registry                           | Priority | Role                                           |
|------------------------------------|----------|------------------------------------------------|
| User-defined registries            | `> 0`    | Override everything                            |
| `ExclusionRandomizerRegistry`      | `0`      | Handles `@Exclude` and predicate exclusions    |
| `CustomRandomizerRegistry`         | `-1`     | Randomizers registered via API                 |
| `AnnotationRandomizerRegistry`     | `-1`     | `@Randomizer` annotations on fields            |
| `BeanValidationRandomizerRegistry` | `-2`     | Constraint-aware randomizers (separate module) |
| `TimeRandomizerRegistry`           | `-3`     | JSR 310 types                                  |
| `InternalRandomizerRegistry`       | `-4`     | Java built-ins (lowest priority / base layer)  |

Resolution stops at the first registry that returns a non-null randomizer.

---

## 9. Bean Validation Support

Separate module: `easy-random-bean-validation`.

Adds `BeanValidationRandomizerRegistry` (priority -2) that introspects `javax.validation.constraints.*` annotations and generates values satisfying those constraints.

**Supported constraints:** `@Size`, `@Min`, `@Max`, `@Past`, `@Future`, `@NotNull`, `@NotEmpty`, `@Positive`, `@Negative`, `@Email`, `@Pattern`, and others.

**Constraint precedence:** Bean validation constraints override global `EasyRandomParameters` (e.g., `collectionSizeRange`). Custom randomizers override bean validation.

**Not supported:** `@Digits`, meta-annotations combining multiple constraints, annotated type parameters (`Set<@Size(min=1) String>`).

---

## 10. Classpath Scanning for Abstract Types / Interfaces

```java
// Without scanning: abstract field = null
// With scanning: abstract field = random concrete subtype
EasyRandomParameters params = new EasyRandomParameters()
    .scanClasspathForConcreteTypes(true);

abstract class Animal {}
class Dog extends Animal {}
class Cat extends Animal {}

class Owner { Animal pet; }

Owner owner = new EasyRandom(params).nextObject(Owner.class);
// owner.pet is either a Dog or Cat instance
```

Uses [classgraph](https://github.com/classgraph/classgraph) at runtime.

---

## 11. Generics Support

**Works:**

- Simple concrete type parameters: `List<String>`, `Map<String, Integer>`
- Single-level type variables resolved from context
- Simple generic inheritance: `class StringList extends ArrayList<String>`

**Does not work:**

- Composite/nested generic collections: `List<List<String>>`, `Map<String, List<Integer>>`
- Complex generic hierarchies involving type erasure

Use a custom `Randomizer` for unsupported generic cases.

---

## 12. SPI / ServiceLoader Extension

`RandomizerRegistry` implementations are auto-discovered via Java's ServiceLoader. Add a file:

```
META-INF/services/org.jeasy.random.api.RandomizerRegistry
```

containing the fully-qualified class name of your registry.

---

## 13. Known Limitations

1. **No-arg constructor absent:** Objenesis bypasses constructor initialization code; `overrideDefaultInitialization=false` has no effect.
2. **Non-static inner classes:** Cannot be instantiated via constructor (requires enclosing instance). Use static nested classes instead.
3. **Type erasure:** Complex generic hierarchies beyond simple cases cannot be resolved at runtime.
4. **Nested generic collections:** `List<List<String>>` etc. — only outer collection is populated.
5. **Static fields:** Always skipped by `DefaultExclusionPolicy`.
6. **`javax.xml.datatype` types** (`XMLGregorianCalendar`, etc.): Not supported without custom randomizers.
7. **`@Digits` constraint:** Not handled by the bean validation module.
8. **Setter validation:** Business validation in setters throws `ObjectCreationException` unless `ignoreRandomizationErrors(true)` or `bypassSetters(true)` is set.
9. **Android:** Objenesis and classgraph are JVM-specific; not compatible with Dalvik/ART.

---

## 14. Design Patterns Used

| Pattern                 | Where                                                                                  |
|-------------------------|----------------------------------------------------------------------------------------|
| Builder / Fluent API    | `EasyRandomParameters` — all setters return `this`                                     |
| Strategy                | `Randomizer`, `ExclusionPolicy`, `ObjectFactory`, `RandomizerProvider` — all swappable |
| Registry                | `RandomizerRegistry` — groups randomizers keyed by field or type                       |
| Chain of Responsibility | `RegistriesRandomizerProvider` walks sorted registries until a match is found          |
| Null Object             | `SkipRandomizer` returns null to leave field unset                                     |
| Object Pool / Cache     | `RandomizationContext.populatedBeans` caches instances to break circular references    |
| Context Object          | `RandomizationContext` / `RandomizationContextStackItem` carry per-invocation state    |
| Template Method         | `AbstractRangeRandomizer<T>` — algorithm fixed, subclasses implement min/max/check     |
| ObjectMother            | The library itself is a reusable ObjectMother implementation                           |
| Decorator               | `ContextAwareRandomizer` extends `Randomizer` with context injection                   |
| Service Locator / SPI   | `ServiceLoader` discovers `RandomizerRegistry` implementations on the classpath        |

---

## 15. Runtime Dependencies

| Library        | Purpose                                                 |
|----------------|---------------------------------------------------------|
| **objenesis**  | Instantiates classes without no-arg constructors        |
| **classgraph** | Classpath scanning for concrete subtypes (when enabled) |
| **datafaker**  | Realistic fake data backing facade randomizers          |
| **slf4j-api**  | Logging (depth warnings, etc.)                          |

---

## 16. Community Extensions

| Extension            | What it adds                                     |
|----------------------|--------------------------------------------------|
| easy-random-vavr     | Vavr functional Java collection and option types |
| easy-random-protobuf | Protocol Buffers message types                   |
