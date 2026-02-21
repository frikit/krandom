# Code-Based Locale Architecture Design

**Date:** 2026-02-21  
**Approach:** Embed locale data in code as enums/constants, not external files  
**Philosophy:** Locale logic baked into generator configuration

---

## Core Design Principles

1. ✅ **No external file loading** - All locale data compiled into code
2. ✅ **Type-safe locale selection** - Use `java.util.Locale` or custom enum
3. ✅ **Follow existing patterns** - Constructor params, builders, enums with data
4. ✅ **Backward compatible** - Existing generators work without locale
5. ✅ **Composable** - Locale config passed through generator chain

---

## Architecture Overview

### Pattern 1: Enum-Based Locale Data (Recommended)

Following the existing `TitleResult` and `AgeGroup` pattern where enums contain both data and logic.

```kotlin
// Locale-specific name registry
enum class LocaleData(
    val locale: Locale,
    val firstNames: List<String>,
    val lastNames: List<String>,
    val titles: List<String>,
    val cities: List<String>?,
    val postalCodeFormat: String?
) {
    EN_US(
        locale = Locale.US,
        firstNames = listOf(
            "James", "John", "Robert", "Michael", "William", "David", "Richard",
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara"
        ),
        lastNames = listOf(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez"
        ),
        titles = listOf("Mr.", "Ms.", "Mrs.", "Dr.", "Prof."),
        cities = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix"),
        postalCodeFormat = "#####"
    ),
    
    DE_DE(
        locale = Locale.GERMANY,
        firstNames = listOf(
            "Hans", "Friedrich", "Karl", "Werner", "Helmut", "Walter",
            "Emma", "Sophie", "Anna", "Laura", "Lena", "Marie"
        ),
        lastNames = listOf(
            "Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer",
            "Wagner", "Becker", "Schulz", "Hoffmann", "Koch"
        ),
        titles = listOf("Herr", "Frau", "Dr.", "Prof."),
        cities = listOf("Berlin", "München", "Hamburg", "Frankfurt", "Köln"),
        postalCodeFormat = "#####"
    ),
    
    ES_ES(
        locale = Locale("es", "ES"),
        firstNames = listOf(
            "José", "Manuel", "Antonio", "Francisco", "Juan", "Pedro",
            "María", "Carmen", "Ana", "Isabel", "Dolores", "Pilar"
        ),
        lastNames = listOf(
            "García", "Rodríguez", "González", "Fernández", "López", "Martínez",
            "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez"
        ),
        titles = listOf("Sr.", "Sra.", "Dr.", "Dra."),
        cities = listOf("Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza"),
        postalCodeFormat = "#####"
    ),
    
    FR_FR(
        locale = Locale.FRANCE,
        firstNames = listOf(
            "Jean", "Pierre", "Michel", "André", "Philippe", "Jacques",
            "Marie", "Nathalie", "Isabelle", "Sophie", "Catherine", "Françoise"
        ),
        lastNames = listOf(
            "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard",
            "Petit", "Durand", "Leroy", "Moreau", "Simon"
        ),
        titles = listOf("M.", "Mme", "Dr.", "Prof."),
        cities = listOf("Paris", "Marseille", "Lyon", "Toulouse", "Nice"),
        postalCodeFormat = "#####"
    ),
    
    JA_JP(
        locale = Locale.JAPAN,
        firstNames = listOf(
            "太郎", "健太", "翔", "大輔", "拓也", "健",
            "花子", "美咲", "陽菜", "結衣", "さくら", "葵"
        ),
        lastNames = listOf(
            "佐藤", "鈴木", "高橋", "田中", "伊藤", "渡辺",
            "山本", "中村", "小林", "加藤", "吉田"
        ),
        titles = listOf("さん", "様", "先生", "博士"),
        cities = listOf("東京", "大阪", "横浜", "名古屋", "札幌"),
        postalCodeFormat = "###-####"
    );
    
    companion object {
        fun fromLocale(locale: Locale): LocaleData {
            return values().firstOrNull { 
                it.locale.language == locale.language && 
                it.locale.country == locale.country 
            } ?: EN_US // Default fallback
        }
        
        fun fromLocaleWithFallback(locale: Locale): LocaleData {
            // Try exact match (language + country)
            values().firstOrNull { 
                it.locale.language == locale.language && 
                it.locale.country == locale.country 
            }?.let { return it }
            
            // Try language-only match
            values().firstOrNull { 
                it.locale.language == locale.language 
            }?.let { return it }
            
            // Default to English
            return EN_US
        }
    }
}
```

### Pattern 2: Generator Implementation

```kotlin
// Updated FirstName generator with locale support
class FirstName(
    private val locale: Locale = Locale.US,
    private val random: Random = Random.Default
) : Generator<String> {
    
    private val localeData by lazy { 
        LocaleData.fromLocaleWithFallback(locale) 
    }
    
    override fun generate(): String {
        return localeData.firstNames.random(random)
    }
    
    // Builder pattern for Java compatibility
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
    
    class Builder {
        private var locale: Locale = Locale.US
        private var random: Random = Random.Default
        
        fun locale(locale: Locale) = apply { this.locale = locale }
        fun seed(seed: Long) = apply { this.random = Random(seed) }
        
        fun build() = FirstName(locale, random)
    }
}

// Updated SurName generator
class SurName(
    private val locale: Locale = Locale.US,
    private val random: Random = Random.Default
) : Generator<String> {
    
    private val localeData by lazy { 
        LocaleData.fromLocaleWithFallback(locale) 
    }
    
    override fun generate(): String {
        return localeData.lastNames.random(random)
    }
}
```

### Pattern 3: Locale-Aware Configuration Object

```kotlin
// Centralized locale configuration
data class LocaleConfig(
    val locale: Locale = Locale.US,
    val seed: Long? = null
) {
    val random: Random by lazy { 
        seed?.let { Random(it) } ?: Random.Default 
    }
    
    val localeData: LocaleData by lazy { 
        LocaleData.fromLocaleWithFallback(locale) 
    }
}

// Usage in generators
class FirstName(
    private val config: LocaleConfig = LocaleConfig()
) : Generator<String> {
    
    // Convenience constructor
    constructor(locale: Locale) : this(LocaleConfig(locale = locale))
    
    override fun generate(): String {
        return config.localeData.firstNames.random(config.random)
    }
}
```

---

## API Usage Examples

### Kotlin API

```kotlin
// Simple usage with default (English)
val firstName = FirstName().generate()

// With specific locale
val germanName = FirstName(locale = Locale.GERMANY).generate()

// With configuration
val config = LocaleConfig(locale = Locale.JAPAN, seed = 12345L)
val japaneseName = FirstName(config).generate()

// Generate list
val names = FirstName(Locale.FRANCE).generateList(10)

// Stream API
FirstName(Locale.GERMANY)
    .stream()
    .limit(100)
    .toList()
```

### Java API

```java
// Simple usage
FirstName gen = new FirstName();
String name = gen.generate();

// With locale
FirstName germanGen = new FirstName(Locale.GERMANY);
String germanName = germanGen.generate();

// Builder pattern
FirstName gen = FirstName.builder()
    .locale(Locale.JAPAN)
    .seed(12345L)
    .build();
List<String> names = gen.generateList(10);

// Configuration object
LocaleConfig config = new LocaleConfig(Locale.FRANCE, 12345L);
FirstName frenchGen = new FirstName(config);
```

---

## Extended Patterns

### Pattern 4: Locale-Specific Logic (Not Just Data)

```kotlin
enum class LocaleData(/* ... */) {
    EN_US(/* ... */) {
        override fun formatFullName(first: String, last: String): String {
            return "$first $last"
        }
        
        override fun formatAddress(
            street: String, city: String, postal: String
        ): String {
            return "$street, $city, $postal"
        }
    },
    
    JA_JP(/* ... */) {
        override fun formatFullName(first: String, last: String): String {
            return "$last $first"  // Last name first in Japanese
        }
        
        override fun formatAddress(
            street: String, city: String, postal: String
        ): String {
            return "〒$postal $city $street"  // Japanese format
        }
    };
    
    // Abstract methods for locale-specific logic
    abstract fun formatFullName(first: String, last: String): String
    abstract fun formatAddress(street: String, city: String, postal: String): String
    
    fun generatePostalCode(random: Random): String {
        return postalCodeFormat?.map { char ->
            when (char) {
                '#' -> random.nextInt(0, 10).toString()
                else -> char.toString()
            }
        }?.joinToString("") ?: ""
    }
}
```

### Pattern 5: Separate Domain Enums (Alternative to Single Enum)

```kotlin
// Separate enums for different data types
enum class PersonNameLocale(val locale: Locale, val names: List<String>) {
    EN_US_NAMES(Locale.US, listOf("James", "John", "Mary")),
    DE_DE_NAMES(Locale.GERMANY, listOf("Hans", "Emma", "Sophie")),
    ES_ES_NAMES(Locale("es", "ES"), listOf("José", "María"));
    
    companion object {
        fun forLocale(locale: Locale): PersonNameLocale {
            return values().firstOrNull { 
                it.locale.language == locale.language 
            } ?: EN_US_NAMES
        }
    }
}

enum class AddressLocale(
    val locale: Locale, 
    val cities: List<String>,
    val postalFormat: String
) {
    EN_US_ADDRESS(Locale.US, listOf("New York", "LA"), "#####"),
    DE_DE_ADDRESS(Locale.GERMANY, listOf("Berlin", "München"), "#####");
}
```

### Pattern 6: Sealed Class Approach (Maximum Type Safety)

```kotlin
sealed class LocaleData {
    abstract val locale: Locale
    abstract val firstNames: List<String>
    abstract val lastNames: List<String>
    
    object EnglishUS : LocaleData() {
        override val locale = Locale.US
        override val firstNames = listOf("James", "John", "Mary", "Patricia")
        override val lastNames = listOf("Smith", "Johnson", "Williams")
    }
    
    object German : LocaleData() {
        override val locale = Locale.GERMANY
        override val firstNames = listOf("Hans", "Friedrich", "Emma")
        override val lastNames = listOf("Müller", "Schmidt", "Schneider")
    }
    
    companion object {
        private val registry = listOf(EnglishUS, German)
        
        fun forLocale(locale: Locale): LocaleData {
            return registry.firstOrNull { 
                it.locale.language == locale.language 
            } ?: EnglishUS
        }
    }
}
```

---

## Integration with ObjectGenerator

```kotlin
// ObjectGenerator with locale support
class ObjectGenerator<T>(
    private val clazz: Class<T>,
    private val localeConfig: LocaleConfig = LocaleConfig()
) : Generator<T> {
    
    // Use locale when creating user-related generators
    override fun generate(): T {
        val instance = clazz.getDeclaredConstructor().newInstance()
        
        clazz.declaredFields.forEach { field ->
            field.isAccessible = true
            val value = when (field.type) {
                String::class.java -> when (field.name.lowercase()) {
                    "firstname" -> FirstName(localeConfig).generate()
                    "lastname" -> SurName(localeConfig).generate()
                    "city" -> City(localeConfig).generate()
                    else -> StringGenerator().generate()
                }
                Int::class.java -> IntGenerator().generate()
                // ... other types
                else -> null
            }
            field.set(instance, value)
        }
        
        return instance
    }
}

// Usage
data class Person(
    var firstName: String = "",
    var lastName: String = "",
    var city: String = ""
)

val germanPerson = ObjectGenerator(
    Person::class.java,
    LocaleConfig(locale = Locale.GERMANY)
).generate()
```

---

## Fluent API Pattern (Optional)

```kotlin
// Fluent generator factory
class KRandom(private val config: LocaleConfig = LocaleConfig()) {
    
    fun person() = PersonGenerators(config)
    fun address() = AddressGenerators(config)
    fun primitives() = PrimitiveGenerators(config)
    
    class PersonGenerators(private val config: LocaleConfig) {
        fun firstName() = FirstName(config)
        fun lastName() = SurName(config)
        fun fullName() = FullName(config)
        fun email() = Email(config)
    }
    
    class AddressGenerators(private val config: LocaleConfig) {
        fun city() = City(config)
        fun postalCode() = PostalCode(config)
    }
}

// Usage
val krandom = KRandom(LocaleConfig(locale = Locale.GERMANY))
val name = krandom.person().firstName().generate()
val city = krandom.address().city().generate()

// Or chain
val germanData = KRandom(LocaleConfig(Locale.GERMANY))
    .person()
    .firstName()
    .generateList(10)
```

---

## Extensibility: Custom Locale Data

```kotlin
// Allow users to register custom locales
object LocaleRegistry {
    private val customLocales = mutableMapOf<Locale, LocaleData>()
    
    fun register(localeData: LocaleData) {
        customLocales[localeData.locale] = localeData
    }
    
    fun get(locale: Locale): LocaleData {
        return customLocales[locale] 
            ?: LocaleData.fromLocaleWithFallback(locale)
    }
}

// User creates custom locale
val customSpanish = LocaleData.ES_ES.copy(
    firstNames = listOf("Custom", "Names", "Here")
)
LocaleRegistry.register(customSpanish)
```

---

## Data Management Strategy

### How to Maintain Large Name Lists in Code

**Option 1: Separate Kotlin files per locale**
```kotlin
// LocaleData_EN_US.kt
internal val EN_US_FIRST_NAMES = listOf(
    "James", "John", "Robert", "Michael", "William", "David",
    // ... 100+ names
)

internal val EN_US_LAST_NAMES = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones",
    // ... 100+ surnames
)

// LocaleData.kt
enum class LocaleData(/* ... */) {
    EN_US(
        locale = Locale.US,
        firstNames = EN_US_FIRST_NAMES,
        lastNames = EN_US_LAST_NAMES,
        // ...
    )
}
```

**Option 2: Code generation from CSV**
```bash
# Build script generates Kotlin code from CSV files
# names_en_us.csv → LocaleData_EN_US.kt
./gradlew generateLocaleData
```

**Option 3: Compressed string literals**
```kotlin
// Store as comma-separated string, split on first use
private const val EN_US_NAMES_DATA = """
    James,John,Robert,Michael,William,David,Richard,Joseph,Thomas,Charles
"""

val EN_US_FIRST_NAMES by lazy { 
    EN_US_NAMES_DATA.split(",").map { it.trim() } 
}
```

---

## Performance Considerations

1. **Lazy initialization** - Lists created only when locale used
2. **Immutable lists** - Share across instances (memory efficient)
3. **No I/O overhead** - All data in compiled bytecode
4. **Fast access** - Direct enum lookup, no file parsing

---

## Recommended Approach

✅ **Pattern 1 (Enum-Based)** for initial implementation:
- Simple, type-safe, follows existing patterns
- Easy to extend with new locales
- Built-in fallback logic
- No external dependencies

✅ **Pattern 3 (LocaleConfig)** for consistency:
- Centralized configuration
- Easy to pass through generator chains
- Supports future extensions (timezone, currency, etc.)

✅ **Separate files** for large datasets:
- Keep LocaleData.kt readable
- One file per locale with constants
- Import into main enum

---

## Migration Path

1. ✅ Create `LocaleData` enum with 5 initial locales
2. ✅ Create `LocaleConfig` data class
3. ✅ Update `FirstName`, `SurName` to accept `LocaleConfig`
4. ✅ Add new generators: `City`, `PostalCode`, `PhoneNumber`
5. ✅ Update `ObjectGenerator` to use `LocaleConfig`
6. ✅ Add builder patterns for Java compatibility

---

## Next Steps

Would you like me to:
1. Implement the `LocaleData` enum with initial locale data?
2. Update existing generators (`FirstName`, `SurName`) to use this pattern?
3. Create the `LocaleConfig` and fluent API classes?
4. Build a prototype with German + English locales?
