# JDK Locale Support & GeneratorConfig Integration

**Date:** 2026-02-21
**Objective:** Add Locale support to existing `GeneratorConfig` class

---

## JDK Locale Overview

### Total Available Locales: **748**

The JDK provides 748 locale variants across all languages. Here are the top languages by variant count:

| Language Code | Language Name | Variants | Examples                          |
|---------------|---------------|----------|-----------------------------------|
| **en**        | English       | 106      | en_US, en_GB, en_CA, en_AU, en_IN |
| **fr**        | French        | 47       | fr_FR, fr_CA, fr_BE, fr_CH        |
| **ar**        | Arabic        | 29       | ar_SA, ar_EG, ar_AE, ar_MA        |
| **es**        | Spanish       | 29       | es_ES, es_MX, es_AR, es_CO        |
| **sr**        | Serbian       | 15       | sr_RS (Cyrillic/Latin)            |
| **zh**        | Chinese       | 14       | zh_CN, zh_TW, zh_HK, zh_SG        |
| **pt**        | Portuguese    | 13       | pt_BR, pt_PT, pt_AO               |
| **de**        | German        | 8        | de_DE, de_AT, de_CH               |
| **nl**        | Dutch         | 8        | nl_NL, nl_BE                      |
| **ru**        | Russian       | 7        | ru_RU, ru_UA, ru_KZ               |
| **it**        | Italian       | 5        | it_IT, it_CH, it_SM               |
| **ja**        | Japanese      | 1        | ja_JP                             |
| **ko**        | Korean        | 1        | ko_KR                             |
| **hi**        | Hindi         | 2        | hi_IN                             |
| **tr**        | Turkish       | 2        | tr_TR, tr_CY                      |

### Most Common Locales for krandom (Priority)

| Priority  | Locale  | Display Name            | Usage                    |
|-----------|---------|-------------------------|--------------------------|
| 🔴 High   | `en_US` | English (United States) | Default, global standard |
| 🔴 High   | `de_DE` | German (Germany)        | Europe, business         |
| 🔴 High   | `es_ES` | Spanish (Spain)         | Europe                   |
| 🔴 High   | `fr_FR` | French (France)         | Europe                   |
| 🔴 High   | `ja_JP` | Japanese (Japan)        | Asia                     |
| 🟡 Medium | `zh_CN` | Chinese (China)         | Asia                     |
| 🟡 Medium | `pt_BR` | Portuguese (Brazil)     | South America            |
| 🟡 Medium | `ru_RU` | Russian (Russia)        | Eastern Europe           |
| 🟡 Medium | `it_IT` | Italian (Italy)         | Europe                   |
| 🟡 Medium | `ko_KR` | Korean (South Korea)    | Asia                     |
| 🟡 Medium | `ar_SA` | Arabic (Saudi Arabia)   | Middle East              |
| 🟢 Low    | `nl_NL` | Dutch (Netherlands)     | Europe                   |
| 🟢 Low    | `sv_SE` | Swedish (Sweden)        | Europe                   |
| 🟢 Low    | `pl_PL` | Polish (Poland)         | Europe                   |
| 🟢 Low    | `tr_TR` | Turkish (Turkey)        | Europe/Asia              |

---

## Existing GeneratorConfig Analysis

### Current Structure

```java
public final class GeneratorConfig {

    private final OptionalLong seed;
    private final Charset      charset;
    private final int          minStringLength;
    private final int          maxStringLength;
    private final int          minCollectionSize;
    private final int          maxCollectionSize;

    // Builder pattern with fluent API
}
```

### Current Capabilities

- ✅ Seed for reproducibility
- ✅ Charset configuration (UTF-8, ASCII, etc.)
- ✅ String length bounds
- ✅ Collection size bounds

### What's Missing

- ❌ **Locale configuration** for locale-aware data generation
- ❌ Timezone configuration
- ❌ Currency configuration
- ❌ Number format configuration

---

## Proposed Enhancement: Add Locale to GeneratorConfig

### Option 1: Direct Locale Field (Recommended)

```java
public final class GeneratorConfig {

    private final OptionalLong seed;
    private final Charset      charset;
    private final int          minStringLength;
    private final int          maxStringLength;
    private final int          minCollectionSize;
    private final int          maxCollectionSize;
    private final Locale       locale;  // NEW FIELD

    private GeneratorConfig(Builder b) {
        this.seed = b.seed;
        this.charset = b.charset;
        this.minStringLength = b.minStringLength;
        this.maxStringLength = b.maxStringLength;
        this.minCollectionSize = b.minCollectionSize;
        this.maxCollectionSize = b.maxCollectionSize;
        this.locale = b.locale;  // NEW
    }

    public Locale getLocale() {
        return locale;
    }  // NEW ACCESSOR

    public static final class Builder {

        private OptionalLong seed              = OptionalLong.empty();
        private Charset      charset           = StandardCharsets.US_ASCII;
        private int          minStringLength   = 5;
        private int          maxStringLength   = 20;
        private int          minCollectionSize = 1;
        private int          maxCollectionSize = 10;
        private Locale       locale            = Locale.US;  // NEW, default to US

        /** Set the locale for locale-aware generators (names, addresses, etc.) */
        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale");
            return this;
        }

        public GeneratorConfig build() {
            return new GeneratorConfig(this);
        }
    }
}
```

### Usage Examples

```java
// Default (English US)
GeneratorConfig config = GeneratorConfig.defaults();

// German locale
GeneratorConfig configDE = GeneratorConfig.builder()
                                          .locale(Locale.GERMANY)
                                          .seed(12345L)
                                          .build();

// Japanese with custom string length
GeneratorConfig configJA = GeneratorConfig.builder()
                                          .locale(Locale.JAPAN)
                                          .stringLength(3, 10)
                                          .charset(StandardCharsets.UTF_8)
                                          .build();

// Spanish Mexico
GeneratorConfig configMX = GeneratorConfig.builder()
                                          .locale(Locale.of("es", "MX"))
                                          .build();
```

---

## Integration with Generators

### Pattern 1: Generators Accept GeneratorConfig

```kotlin
// Updated FirstName generator
class FirstName(
    private val config: GeneratorConfig = GeneratorConfig.defaults()
) : Generator<String> {

    private val localeData by lazy {
        LocaleData.fromLocale(config.locale)
    }

    private val random by lazy {
        config.seed.isPresent
        ? Random(config.seed.asLong)
        : Random.Default
    }

    override fun generate(): String {
        return localeData.firstNames.random(random)
    }

    // Convenience constructor (backward compatible)
    constructor(locale: Locale) : this(
        GeneratorConfig.builder().locale(locale).build()
    )
}
```

### Pattern 2: Factory Methods

```java
// Generator factory that uses config
public class Generators {

    public static Generator<String> firstName(GeneratorConfig config) {
        return new FirstName(config);
    }

    public static Generator<String> surname(GeneratorConfig config) {
        return new SurName(config);
    }

    public static Generator<String> city(GeneratorConfig config) {
        return new City(config);
    }
}


// Usage
GeneratorConfig germanConfig = GeneratorConfig.builder()
                                              .locale(Locale.GERMANY)
                                              .seed(42L)
                                              .build();

String germanName = Generators.firstName(germanConfig).generate();
String germanCity = Generators.city(germanConfig).generate();
```

### Pattern 3: ObjectGenerator Integration

```java
// ObjectGenerator already has ObjectGeneratorConfig
// We extend it to accept GeneratorConfig for locale awareness


public class ObjectGenerator<T> implements Generator<T> {

    private final Class<T>              clazz;
    private final ObjectGeneratorConfig objectConfig;
    private final GeneratorConfig       generatorConfig;  // NEW

    public ObjectGenerator(
        Class<T> clazz,
        ObjectGeneratorConfig objectConfig,
        GeneratorConfig generatorConfig  // NEW
    ) {
        this.clazz = clazz;
        this.objectConfig = objectConfig;
        this.generatorConfig = generatorConfig;  // NEW
    }

    // Convenience constructor (backward compatible)
    public ObjectGenerator(Class<T> clazz) {
        this(clazz, ObjectGeneratorConfig.defaults(), GeneratorConfig.defaults());
    }

    @Override
    public T generate() {
        // Use generatorConfig.getLocale() when creating user generators
        T instance = instantiate(clazz);
        populateFields(instance, 0);
        return instance;
    }

    private void populateFields(Object obj, int depth) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            // Check field override first
            Optional<Generator<?>> fieldGen = objectConfig.getFieldOverride(
                obj.getClass(), field.getName()
            );

            if (fieldGen.isPresent()) {
                setField(obj, field, fieldGen.get().generate());
                continue;
            }

            // Use locale-aware generators based on field name
            Generator<?> gen = getGeneratorForField(field);
            setField(obj, field, gen.generate());
        }
    }

    private Generator<?> getGeneratorForField(Field field) {
        String fieldName = field.getName().toLowerCase();

        // Locale-aware field detection
        if (fieldName.contains("firstname") || fieldName.equals("fname")) {
            return new FirstName(generatorConfig);  // Uses locale from config
        }
        if (fieldName.contains("lastname") || fieldName.equals("lname")) {
            return new SurName(generatorConfig);
        }
        if (fieldName.contains("city")) {
            return new City(generatorConfig);
        }

        // Fallback to type-based generation
        return getDefaultGenerator(field.getType());
    }
}
```

### Usage with ObjectGenerator

```java
// Person POJO
public class Person {

    private String firstName;
    private String lastName;
    private String city;
    private int    age;
}


// Generate German person
GeneratorConfig germanConfig = GeneratorConfig.builder()
                                              .locale(Locale.GERMANY)
                                              .build();

ObjectGenerator<Person> generator = new ObjectGenerator<>(
    Person.class,
    ObjectGeneratorConfig.defaults(),
    germanConfig
);

Person germanPerson = generator.generate();
// firstName: "Hans", lastName: "Müller", city: "Berlin"

// Generate Japanese person
GeneratorConfig japaneseConfig = GeneratorConfig.builder()
                                                .locale(Locale.JAPAN)
                                                .build();

Person japanesePerson = new ObjectGenerator<>(
    Person.class,
    ObjectGeneratorConfig.defaults(),
    japaneseConfig
).generate();
// firstName: "太郎", lastName: "佐藤", city: "東京"
```

---

## LocaleData Enum Implementation

```kotlin
enum class LocaleData(
    val locale: Locale,
    val firstNames: List<String>,
    val lastNames: List<String>,
    val cities: List<String>
) {
    EN_US(
        locale = Locale.US,
        firstNames = EN_US_FIRST_NAMES,  // Defined in separate file
        lastNames = EN_US_LAST_NAMES,
        cities = EN_US_CITIES
    ),

    DE_DE(
        locale = Locale.GERMANY,
        firstNames = DE_DE_FIRST_NAMES,
        lastNames = DE_DE_LAST_NAMES,
        cities = DE_DE_CITIES
    ),

    ES_ES(
        locale = Locale("es", "ES"),
        firstNames = ES_ES_FIRST_NAMES,
        lastNames = ES_ES_LAST_NAMES,
        cities = ES_ES_CITIES
    ),

    FR_FR(
        locale = Locale.FRANCE,
        firstNames = FR_FR_FIRST_NAMES,
        lastNames = FR_FR_LAST_NAMES,
        cities = FR_FR_CITIES
    ),

    JA_JP(
        locale = Locale.JAPAN,
        firstNames = JA_JP_FIRST_NAMES,
        lastNames = JA_JP_LAST_NAMES,
        cities = JA_JP_CITIES
    );

    companion object {
        /**
         * Get locale data with fallback logic:
         * 1. Try exact match (language + country)
         * 2. Try language-only match
         * 3. Default to EN_US
         */
        fun fromLocale(locale: Locale): LocaleData {
            // Exact match
            values().firstOrNull {
                it.locale.language == locale.language &&
                        it.locale.country == locale.country
            }?.let { return it }

            // Language-only match
            values().firstOrNull {
                it.locale.language == locale.language
            }?.let { return it }

            // Default fallback
            return EN_US
        }
    }
}
```

---

## Backward Compatibility

### Maintaining Existing API

All existing code continues to work without changes:

```java
// Old code - still works
FirstName gen = new FirstName();
String name = gen.generate();

// New code - with locale
GeneratorConfig config = GeneratorConfig.builder()
                                        .locale(Locale.GERMANY)
                                        .build();
FirstName germanGen = new FirstName(config);
String germanName = germanGen.generate();
```

### API Cleanup Strategy (Optional)

```java
// Option: remove locale-less constructors in favor of config
public FirstName() {
    this(GeneratorConfig.defaults());
}

// Preferred constructor
public FirstName(GeneratorConfig config) {
    this.config = config;
}
```

---

## Migration Checklist

### Phase 1: Core Infrastructure

- [ ] Add `locale` field to `GeneratorConfig`
- [ ] Add `locale(Locale)` method to `GeneratorConfig.Builder`
- [ ] Set default locale to `Locale.US`
- [ ] Update tests for `GeneratorConfig`

### Phase 2: LocaleData Enum

- [ ] Create `LocaleData` enum with 5 locales (EN_US, DE_DE, ES_ES, FR_FR, JA_JP)
- [ ] Create separate constant files for each locale's data
- [ ] Implement `fromLocale()` with fallback logic
- [ ] Add unit tests for locale resolution

### Phase 3: Generator Updates

- [ ] Update `FirstName` to accept `GeneratorConfig`
- [ ] Update `SurName` to accept `GeneratorConfig`
- [ ] Update `Email` to use locale-aware names
- [ ] Update `Username` to use locale-aware names
- [ ] Create new `City` generator
- [ ] Create new `PostalCode` generator

### Phase 4: ObjectGenerator Integration

- [ ] Add `GeneratorConfig` parameter to `ObjectGenerator`
- [ ] Update field detection logic for locale-aware generators
- [ ] Add integration tests with various locales
- [ ] Update documentation with examples

### Phase 5: Additional Locales

- [ ] Add Chinese (zh_CN)
- [ ] Add Portuguese (pt_BR)
- [ ] Add Russian (ru_RU)
- [ ] Add Italian (it_IT)
- [ ] Add Korean (ko_KR)

---

## Benefits of Using GeneratorConfig

### 1. Centralized Configuration

- Single source of truth for all generator settings
- Easy to pass through generator chains
- Consistent API across all generators

### 2. Future Extensibility

Can add more config without breaking changes:

```java
public Builder timezone(ZoneId timezone) { ...}

public Builder currency(Currency currency) { ...}

public Builder numberFormat(NumberFormat format) { ...}
```

### 3. Consistency

- Follows existing pattern in krandom
- Similar to `ObjectGeneratorConfig`
- Builder pattern already familiar to users

### 4. Testability

```java
// Easy to create test configs
GeneratorConfig testConfig = GeneratorConfig.builder()
                                            .locale(Locale.GERMANY)
                                            .seed(12345L)  // Deterministic for tests
                                            .build();
```

---

## Recommended Implementation Order

1. ✅ **Update `GeneratorConfig`** - Add locale field (1 day)
2. ✅ **Create `LocaleData` enum** - With 5 locales (2 days)
3. ✅ **Update 2-3 generators** - FirstName, SurName as POC (2 days)
4. ✅ **Add tests** - Config + generators (1 day)
5. ✅ **Update `ObjectGenerator`** - Integrate config (2 days)
6. ✅ **Documentation** - Examples and migration guide (1 day)
7. ✅ **Expand locales** - Add 5 more (ongoing)

**Total:** ~2 weeks for full implementation

---

## Open Questions

1. **Default locale** - Use `Locale.US` or `Locale.getDefault()`?
    - Recommendation: `Locale.US` for consistency across environments

2. **Locale variants** - Support `en_GB` vs `en_US` differences?
    - Phase 2 - Start with language-level, add variants later

3. **Custom locale data** - Allow users to register custom locales?
    - Phase 3 - Add `LocaleDataRegistry` for extensibility

4. **Thread safety** - Cache locale data per locale?
    - Use `lazy` initialization per enum value (already thread-safe)

---

## Summary

✅ **JDK provides 748 locales** - Use standard `java.util.Locale`
✅ **Add locale to existing `GeneratorConfig`** - Don't create new config
✅ **Use enum-based `LocaleData`** - Code-based, no file loading
✅ **Maintain backward compatibility** - Existing code still works
✅ **Follow existing patterns** - Builder, config, lazy loading

**Next Step:** Implement locale field in `GeneratorConfig` + create `LocaleData` enum with 5 initial locales.
