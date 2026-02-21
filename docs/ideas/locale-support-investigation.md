# Locale Support Investigation & Design Proposal

**Date:** 2026-02-21  
**Status:** Investigation Phase  
**Purpose:** Design locale-aware data generation for krandom

---

## Executive Summary

**Current State:** ❌ Zero locale support - all data is generic/mixed international  
**Goal:** Support `java.util.Locale` for locale-specific random data generation  
**Primary Use Cases:** Currency, postal codes, names, addresses, phone formats, character sets

---

## 1. Current State Analysis

### 1.1 Existing Resources

**Data Files:**
```
core/src/main/resources/person/
├── names.txt      (560KB) - Mixed international, comma-separated
└── surnames.txt   (1.3MB) - Mixed international, comma-separated
```

**Format:** Plain text, newline-separated entries
```
John,Jane,José,李明,Hans,Иван,محمد
```

**Loading Mechanism:**
```kotlin
// ResourceResolver.kt
fun getResourceContent(resourcePath: String): String {
    Files.walk(Paths.get(baseResourcePath))
        .filter { it.toString().contains(resourcePath) }
        .findFirst()
        .map { Files.readString(it) }
        .orElseThrow()
}

// GenericUserGenerator.kt
private fun <T> initCache(...): List<T> {
    val content = ResourceResolver.getResourceContent(resourcePath)
    return content.split("\n")
        .map(String::trim)
        .filter(String::isNotEmpty)
        .map(transform)
}
```

**Problem:** No locale parameter, mixed-locale data, no fallback mechanism

### 1.2 Current Generators

All generators are **locale-agnostic:**

| Generator | Data Source | Locale Impact |
|-----------|-------------|---------------|
| `FirstName()` | `names.txt` | ⚠️ Returns mixed cultures |
| `SurName()` | `surnames.txt` | ⚠️ Returns mixed cultures |
| `Email()` | Composed from names | ⚠️ Inherited from names |
| `Username()` | Composed from names | ⚠️ Inherited from names |
| `Gender()` | Hardcoded enum | ✅ Universal |
| `Age()` | Random int | ✅ Universal |
| `BirthDay()` | Random date | ✅ Universal |
| `Title()` | Hardcoded list | ⚠️ English-only |
| `SocialSecurityNumber()` | Random digits | ⚠️ US-format only |

---

## 2. Industry Analysis: How Other Libraries Handle Locales

### 2.1 DataFaker (Java) — Most Relevant ⭐

**Locale API:**
```java
// Constructor with Locale
Faker faker = new Faker(new Locale("de"));
Faker faker = new Faker(new Locale("es", "MX"));

// Usage
String name = faker.name().firstName();  // German name
String city = faker.address().city();     // German city
```

**Resource Structure:**
```
src/main/resources/
├── en.yml
├── de.yml
├── fr.yml
├── ja.yml
├── es.yml
├── es-MX.yml   (locale variant)
└── ...         (60+ locales)
```

**YAML Format:**
```yaml
# de.yml
de:
  faker:
    name:
      first_name: [Hans, Friedrich, Emma, Sophie, Lukas]
      last_name: [Müller, Schmidt, Schneider, Fischer, Weber]
      title:
        descriptor: [Herr, Frau, Dr.]
    address:
      city: [Berlin, München, Hamburg, Frankfurt]
      postcode: ["#####"]  # Pattern
      state: [Bayern, Hessen, Sachsen]
```

**Fallback Mechanism:**
```java
// If de.yml missing "address.building_number":
// 1. Try parent locale (de → en)
// 2. Use English data
// 3. Throw if English missing (mandatory data)
```

**Custom Data:**
```java
faker.addPath(Locale.ENGLISH, Path.of("/custom/en.yml"));
```

### 2.2 Bogus (.NET) — 70+ Locales

**Locale API:**
```csharp
var faker = new Faker("ko");  // Korean
var name = new DataSets.Name("ru");  // Russian

// Dynamic switching
name["en"].LastName();  // Override to English
```

**Resource Structure:**
```
data_extend/
├── en/
│   └── name.json
├── de/
│   └── name.json
├── ko/
│   └── name.json
└── ...
```

**JSON Format:**
```json
{
  "name": {
    "first_name": ["Hans", "Friedrich", "Emma"],
    "last_name": ["Müller", "Schmidt"]
  }
}
```

**Fallback:** Transparent fallback to English for missing keys

### 2.3 Mimesis (Python) — 50+ Locales

**Locale API:**
```python
from mimesis import Person
from mimesis.enums import Locale

person = Person(locale=Locale.DE)
name = person.first_name()  # German name
```

**Strategy:** Eager loading - all locale data loaded at instantiation  
**Performance:** 5-15x faster than Faker due to single load

### 2.4 Faker (Python) — 80+ Locales

**Locale API:**
```python
from faker import Faker

# Single locale
fake = Faker('de_DE')

# Multi-locale (random selection)
fake = Faker(['en_US', 'de_DE', 'ja_JP'])
```

**Strategy:** Lazy loading per provider call  
**Providers:** Modular per locale (`faker.providers.person.de_DE`)

---

## 3. Comparative Analysis

### 3.1 Data Format Comparison

| Format | Pros | Cons | Used By |
|--------|------|------|---------|
| **YAML** | Human-readable, comments, hierarchical, Java standard | Parsing overhead, library dependency | DataFaker |
| **JSON** | Lightweight, universal, fast parsing | No comments, less readable | Bogus, Mimesis |
| **TXT/CSV** | Simplest, no parsing, fastest | No structure, no metadata | krandom (current) |
| **Properties** | Java native, simple | Flat structure, limited data types | — |

**Recommendation:** **YAML** for Java ecosystem alignment, or **TXT** for minimal migration

### 3.2 Loading Strategy Comparison

| Strategy | Pros | Cons | Used By |
|----------|------|------|---------|
| **Lazy + Cache** | Memory efficient, fast after first load | Slight first-call delay | krandom, DataFaker |
| **Eager loading** | Predictable performance, all-or-nothing | High startup memory | Mimesis |
| **Per-call lazy** | Flexible, minimal memory | Repeated I/O overhead | Faker (Python) |

**Recommendation:** **Lazy + Cache** (current approach is optimal)

### 3.3 Fallback Strategy Comparison

| Library | Fallback Chain | Behavior |
|---------|----------------|----------|
| DataFaker | `locale → parent → en → error` | `es_MX → es → en` |
| Bogus | `locale → en → error` | Silent fallback |
| Mimesis | `locale → error` | Explicit locale requirement |
| krandom | None | N/A (no locales yet) |

**Recommendation:** `locale → en → error` (2-level fallback)

---

## 4. Design Proposal for krandom

### 4.1 API Design

#### Option A: Constructor Parameter (Recommended)
```kotlin
// Kotlin API
val faker = KRandom(Locale.GERMAN)
val firstName = faker.person().firstName()

// Java API
KRandom faker = new KRandom(Locale.GERMAN);
String firstName = faker.person().firstName();

// Current style with locale
val firstName = FirstName(locale = Locale.GERMAN).randomData()
```

#### Option B: Per-Generator Parameter
```kotlin
val firstName = FirstName().withLocale(Locale.GERMAN).randomData()
val city = City().withLocale(Locale.JAPANESE).randomData()
```

**Decision:** **Option A** - Consistent with DataFaker, cleaner API

### 4.2 Resource Structure

#### Proposed File Organization
```
core/src/main/resources/
├── locales/
│   ├── en/
│   │   ├── person_names.txt
│   │   ├── person_surnames.txt
│   │   ├── person_titles.txt
│   │   ├── address_cities.txt
│   │   ├── address_states.txt
│   │   ├── address_streets.txt
│   │   └── postal_code_format.txt
│   ├── de/
│   │   ├── person_names.txt
│   │   ├── person_surnames.txt
│   │   ├── person_titles.txt
│   │   ├── address_cities.txt
│   │   └── address_states.txt
│   ├── ja/
│   │   ├── person_names.txt
│   │   ├── person_surnames.txt
│   │   └── address_cities.txt
│   └── fallback/ → symlink to en/
└── universal/  (locale-independent data)
    ├── lorem_words.txt
    └── colors.txt
```

**Naming Convention:** `{category}_{type}.txt`  
**Encoding:** UTF-8 for international character support

#### File Format (TXT - Phase 1)
```
# de/person_names.txt
Hans
Friedrich
Emma
Sophie
Lukas
Anna
```

**Advantages:**
- Minimal migration from current format
- No new dependencies
- Fast loading
- Easy to edit/extend

#### Optional: YAML Format (Phase 2)
```yaml
# de/person.yml
person:
  names:
    male: [Hans, Friedrich, Lukas]
    female: [Emma, Sophie, Anna]
  surnames: [Müller, Schmidt, Schneider]
  titles:
    formal: [Herr, Frau, Dr.]
```

**Advantages:**
- Gender-aware data
- Metadata support
- Hierarchical organization

### 4.3 Core Classes

#### LocaleResolver
```kotlin
object LocaleResolver {
    private const val DEFAULT_LOCALE = "en"
    
    /**
     * Resolves resource path with fallback logic
     * @param locale Target locale (e.g., de_DE)
     * @param resourceType Resource type (e.g., person_names)
     * @return Path chain: [de_DE, de, en]
     */
    fun resolveResourcePath(
        locale: Locale, 
        resourceType: String
    ): List<String> {
        val language = locale.language
        val country = locale.country
        
        return buildList {
            // Try full locale: de_DE
            if (country.isNotEmpty()) {
                add("locales/${language}_${country}/${resourceType}.txt")
            }
            // Try language only: de
            add("locales/${language}/${resourceType}.txt")
            // Fallback to English
            add("locales/${DEFAULT_LOCALE}/${resourceType}.txt")
        }
    }
    
    /**
     * Load resource with fallback
     */
    fun loadResourceWithFallback(
        locale: Locale,
        resourceType: String
    ): String {
        val paths = resolveResourcePath(locale, resourceType)
        
        for (path in paths) {
            try {
                return ResourceResolver.getResourceContent(path)
            } catch (e: Exception) {
                // Continue to next fallback
            }
        }
        
        throw IllegalStateException(
            "Resource not found: $resourceType for locale $locale (tried: $paths)"
        )
    }
}
```

#### LocaleAwareGenerator
```kotlin
abstract class LocaleAwareGenerator<T>(
    protected val locale: Locale = Locale.ENGLISH
) : Generator<T> {
    
    protected fun loadLocaleData(resourceType: String): List<String> {
        val content = LocaleResolver.loadResourceWithFallback(locale, resourceType)
        return content.split("\n")
            .map(String::trim)
            .filter(String::isNotEmpty)
    }
}
```

#### Updated FirstName Generator
```kotlin
class FirstName(
    private val locale: Locale = Locale.ENGLISH,
    random: Random = Random.Default
) : LocaleAwareGenerator<String>(locale) {
    
    companion object {
        // Cache per locale
        private val cache = ConcurrentHashMap<Locale, List<String>>()
    }
    
    override fun generate(): String {
        val names = cache.getOrPut(locale) {
            loadLocaleData("person_names")
        }
        return names.random(random)
    }
}
```

### 4.4 What Needs Locale Support?

#### High Priority (Phase 1)
| Generator | Locale-Specific? | Resource Type |
|-----------|------------------|---------------|
| `FirstName` | ✅ | `person_names` |
| `SurName` | ✅ | `person_surnames` |
| `Title` | ✅ | `person_titles` |
| `City` | ✅ | `address_cities` |
| `State` | ✅ | `address_states` |
| `PostalCode` | ✅ | `address_postal_format` |
| `PhoneNumber` | ✅ | `phone_format` |

#### Universal (No Locale Needed)
| Generator | Reason |
|-----------|--------|
| `Age` | Numbers are universal |
| `Gender` | Enum values universal |
| `Email` | Internet standard (but names are locale-aware) |
| `Username` | Internet standard |
| `IPv4` / `IPv6` | RFC standards |
| `UUID` | RFC standard |
| `BooleanGenerator` | Universal |
| Primitives | Universal |

### 4.5 Migration Path

#### Phase 1: Foundation (Week 1)
1. ✅ Create `LocaleResolver` class
2. ✅ Create `LocaleAwareGenerator` base class
3. ✅ Restructure resources into `locales/en/` folder
4. ✅ Add English data (migrate current files)
5. ✅ Update `FirstName`, `SurName`, `Title` to use locale

#### Phase 2: Core Locales (Week 2)
6. ✅ Add 5 common locales:
   - `de` (German)
   - `fr` (French)
   - `es` (Spanish)
   - `ja` (Japanese)
   - `zh` (Chinese)
7. ✅ Source data from public datasets (census, etc.)

#### Phase 3: Address Support (Week 3)
8. ✅ Implement `City`, `State`, `PostalCode` generators
9. ✅ Add locale-specific address data

#### Phase 4: Patterns & Formats (Week 4)
10. ✅ Implement `PhoneNumber` with format patterns
11. ✅ Add postal code format patterns per locale
12. ✅ Add currency symbols and formats

---

## 5. Data Sources

### 5.1 Open Datasets for Locale Data

| Data Type | Source | License |
|-----------|--------|---------|
| **Names** | Behind the Name, Census data | Public domain |
| **Surnames** | National census bureaus | Public domain |
| **Cities** | GeoNames.org | CC BY 4.0 |
| **Addresses** | OpenStreetMap | ODbL |
| **Postal Codes** | Wikipedia, postal authority docs | Various |
| **Phone Formats** | ITU E.164 standard docs | Public |

### 5.2 Example: German Data Collection
```bash
# German first names (from statistics)
names_de=(Hans Friedrich Emma Sophie Lukas Anna Felix Laura)

# German surnames (most common)
surnames_de=(Müller Schmidt Schneider Fischer Weber Meyer Wagner)

# German cities
cities_de=(Berlin München Hamburg Frankfurt Köln Stuttgart)
```

---

## 6. Implementation Checklist

### 6.1 Core Infrastructure
- [ ] Create `LocaleResolver` utility class
- [ ] Create `LocaleAwareGenerator<T>` base class
- [ ] Update `ResourceResolver` to handle locale paths
- [ ] Implement locale-based caching in `GenericUserGenerator`
- [ ] Add `Locale` parameter to existing generators (optional, default=EN)

### 6.2 Resource Migration
- [ ] Create `locales/` folder structure
- [ ] Move `names.txt` → `locales/en/person_names.txt`
- [ ] Move `surnames.txt` → `locales/en/person_surnames.txt`
- [ ] Ensure UTF-8 encoding for all resource files

### 6.3 Testing
- [ ] Unit tests for `LocaleResolver` fallback logic
- [ ] Unit tests for each locale (en, de, fr, es, ja)
- [ ] Integration tests for generator + locale combinations
- [ ] Performance tests for caching efficiency

### 6.4 Documentation
- [ ] Update README with locale usage examples
- [ ] Document supported locales
- [ ] Document how to add custom locales
- [ ] Document fallback behavior

---

## 7. Open Questions

### 7.1 Technical Decisions
1. **YAML vs TXT?** 
   - TXT for Phase 1 (minimal change)
   - YAML for Phase 2 (if gender-awareness needed)

2. **Lazy vs Eager loading per locale?**
   - Keep lazy (current approach works well)

3. **Cache eviction strategy?**
   - Simple: never evict (locales are small)
   - Advanced: LRU cache with size limit

4. **Thread safety?**
   - Use `ConcurrentHashMap` for caches

### 7.2 Product Decisions
1. **How many locales in v1.0?**
   - Proposal: 10 locales (en, de, fr, es, it, ja, zh, ru, pt, ko)

2. **Support locale variants? (en_US vs en_GB)**
   - Proposal: Yes, with fallback `en_US → en`

3. **Allow custom locale data?**
   - Proposal: Phase 2 feature (like DataFaker's `addPath`)

---

## 8. Performance Considerations

### 8.1 Memory Impact
**Current:**
- `names.txt`: 560KB → ~10,000 names in memory when cached
- `surnames.txt`: 1.3MB → ~25,000 surnames in memory

**With 10 locales:**
- 10 × 560KB = 5.6MB names
- 10 × 1.3MB = 13MB surnames
- **Total:** ~20MB for all person data (acceptable)

### 8.2 Optimization Strategies
1. **Lazy loading:** Load locale data only when requested
2. **Soft references:** Allow GC to reclaim unused locales
3. **Compressed resources:** Use gzip for large data files
4. **On-demand streaming:** For very large datasets (>10MB)

---

## 9. Recommended Next Steps

1. **Prototype `LocaleResolver`** — Build core fallback logic
2. **Migrate English data** — Restructure current resources
3. **Update 3 generators** — FirstName, SurName, Title as proof of concept
4. **Add German locale** — Validate fallback works
5. **Write integration tests** — Ensure all combinations work
6. **Gather community feedback** — Before committing to data format

---

## 10. References

- [DataFaker GitHub](https://github.com/datafaker-net/datafaker)
- [Bogus GitHub](https://github.com/bchavez/Bogus)
- [Java Locale Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Locale.html)
- [IETF BCP 47 Language Tags](https://www.rfc-editor.org/rfc/rfc5646.html)
- [GeoNames.org](http://www.geonames.org/)
- [Unicode CLDR](https://cldr.unicode.org/)

---

**Last Updated:** 2026-02-21  
**Authors:** krandom development team  
**Status:** Ready for implementation
