# lorem Reference

**Repository:** https://github.com/mdeanda/lorem
**Author:** Miguel De Anda
**License:** MIT
**Status:** Active (latest: 2.2, August 2023)
**Maven coordinates:** `com.thedeanda:lorem:2.2`
**Package:** `com.thedeanda.lorem`

```xml
<!-- Maven -->
<dependency>
    <groupId>com.thedeanda</groupId>
    <artifactId>lorem</artifactId>
    <version>2.2</version>
</dependency>
```

```kotlin
// Gradle (Kotlin DSL)
implementation("com.thedeanda:lorem:2.2")
```

---

## 1. Purpose

lorem is a lightweight Java library for generating realistic placeholder data. Unlike easy-random (which populates entire object graphs) or Chance.js (a general JS toolkit), lorem focuses specifically
on **human-readable text and contact data** — names, addresses, lorem ipsum text, emails, phone numbers, and dates. Its data is loaded from classpath resource files, making every result pronounceable
and realistic-looking rather than random character sequences.

**Primary use cases:**

- Populating test fixtures with realistic names, addresses, and body text
- Generating seed data for databases and demo environments
- Creating HTML mockups and prototypes with plausible placeholder content

```java
Lorem lorem = LoremIpsum.getInstance();

String name    = lorem.getName();                      // 'Shirley Williams'
String email   = lorem.getEmail();                     // '[email protected]'
String address = lorem.getCity() + ", " + lorem.getStateAbbr() + " " + lorem.getZipCode();
String body    = lorem.getParagraphs(2, 5);
String html    = lorem.getHtmlParagraphs(1, 3);
```

---

## 2. Core API

### Interface: `Lorem`

The public contract. Declare variables against this interface to allow substitution or mocking.

```java
package com.thedeanda.lorem;

public interface Lorem {
    // Text
    String getWords(int count);
    String getWords(int min, int max);
    String getTitle(int count);
    String getTitle(int min, int max);
    String getParagraphs(int min, int max);
    String getHtmlParagraphs(int min, int max);

    // Person
    String getFirstName();
    String getFirstNameMale();
    String getFirstNameFemale();
    String getLastName();
    String getName();
    String getNameMale();
    String getNameFemale();

    // Contact
    String getEmail();
    String getPhone();
    String getUrl();

    // Location
    String getCity();
    String getStateAbbr();
    String getStateFull();
    String getZipCode();
    String getCountry();

    // Date/Time
    LocalDateTime getPriorDate(Duration maxDurationBeforeNow);
    LocalDateTime getFutureDate(Duration maxDurationFromNow);
}
```

### Class: `LoremIpsum`

Concrete implementation. Backed by classpath resource files.

```java
public class LoremIpsum implements Lorem {
    public LoremIpsum() { }
    public LoremIpsum(Long seed) { }       // null = unseeded
    public LoremIpsum(Random random) { }   // pass SecureRandom or any subclass

    public static LoremIpsum getInstance() { }  // shared singleton, thread-safe
}
```

| Constructor                      | When to use                                      |
|----------------------------------|--------------------------------------------------|
| `LoremIpsum()`                   | Default; uses `new Random()`                     |
| `LoremIpsum(42L)`                | Reproducible output; same seed = same sequence   |
| `LoremIpsum(new SecureRandom())` | Cryptographically secure randomness              |
| `LoremIpsum.getInstance()`       | Shared singleton; fastest for one-off generation |

> **Note:** The singleton is stateful. Its internal `Random` advances with every call, so it cannot be used for deterministic reproducibility across test runs. Use `new LoremIpsum(seed)` for that.

---

## 3. Text Generation

### `getWords(int count)`

Returns exactly `count` space-separated lorem ipsum words, trimmed. Words are drawn from an internal word list (`lorem.txt`).

```java
lorem.getWords(3)    // => 'lorem ipsum dolor'
lorem.getWords(1)    // => 'amet'
```

### `getWords(int min, int max)`

Returns a random number of words in `[min, max]`.

```java
lorem.getWords(5, 10)  // => 'consectetur adipiscing elit sed do eiusmod tempor'
```

### `getTitle(int count)`

Returns exactly `count` words formatted as a title. The first word and every word longer than 3 characters is capitalised.

```java
lorem.getTitle(4)    // => 'Lorem Ipsum Dolor sit'  (short words stay lowercase)
```

### `getTitle(int min, int max)`

Same capitalisation rules, random word count in `[min, max]`.

```java
lorem.getTitle(3, 6)
```

### `getParagraphs(int min, int max)`

Returns a random number of paragraphs in `[min, max]`. Paragraphs are separated by `\n`. Each paragraph contains 2–6 sentences; each sentence starts with a capital word and ends with `. `.

```java
lorem.getParagraphs(2, 4)
// => "Lorem ipsum dolor sit amet....\n\nConsectetur adipiscing elit...."
```

### `getHtmlParagraphs(int min, int max)`

Same as `getParagraphs` but each paragraph is wrapped in `<p>...</p>`.

```java
lorem.getHtmlParagraphs(1, 3)
// => "<p>Lorem ipsum dolor....</p><p>Consectetur adipiscing....</p>"
```

---

## 4. Person

### `getName()` / `getNameMale()` / `getNameFemale()`

Returns a randomly combined first name + last name.

| Method            | First name pool         |
|-------------------|-------------------------|
| `getName()`       | Male or female (random) |
| `getNameMale()`   | Male only               |
| `getNameFemale()` | Female only             |

```java
lorem.getName()         // => 'Shirley Williams'
lorem.getNameMale()     // => 'James Anderson'
lorem.getNameFemale()   // => 'Patricia Thompson'
```

### `getFirstName()` / `getFirstNameMale()` / `getFirstNameFemale()`

First name only (no surname).

```java
lorem.getFirstName()        // => 'Shirley'
lorem.getFirstNameMale()    // => 'James'
lorem.getFirstNameFemale()  // => 'Patricia'
```

### `getLastName()`

Surname only, from the surnames resource list.

```java
lorem.getLastName()         // => 'Williams'
```

---

## 5. Contact Information

### `getEmail()`

Format: `firstname.lastname@example.com`. Both components are lowercased; spaces within a name are replaced by `.`. The domain is always `example.com` (RFC 2606 reserved — safe for testing).

```java
lorem.getEmail()   // => '[email protected]'
lorem.getEmail()   // => '[email protected]'
```

### `getPhone()`

Format: `(X##) X##-####` where `X` ∈ 1–9 and `#` ∈ 0–9. US-style NANP format.

```java
lorem.getPhone()   // => '(800) 555-1212'
lorem.getPhone()   // => '(312) 943-8671'
```

### `getUrl()`

Returns a search engine URL with a random lorem ipsum word as the query. Cycles randomly through: Google, Bing, Yahoo, DuckDuckGo.

```java
lorem.getUrl()   // => 'https://www.google.com/#q=lorem'
lorem.getUrl()   // => 'https://duckduckgo.com/?q=amet'
```

---

## 6. Location

All location data is US-centric (cities, states, ZIP codes). Countries are international.

### `getCity()`

Random US city name from `cities.txt`.

```java
lorem.getCity()   // => 'San Francisco'
```

### `getStateAbbr()` / `getStateFull()`

US state abbreviation or full name.

```java
lorem.getStateAbbr()   // => 'CA'
lorem.getStateFull()   // => 'California'
```

### `getZipCode()`

Random 5-digit string. Syntactically valid; **not** guaranteed to be a real US ZIP code.

```java
lorem.getZipCode()   // => '90210'
```

### `getCountry()`

Random country name from `countries.txt` (international list).

```java
lorem.getCountry()   // => 'Germany'
```

---

## 7. Date and Time

Both methods return `java.time.LocalDateTime`. Precision is seconds (sub-second is not preserved).

### `getPriorDate(Duration maxDurationBeforeNow)`

Returns a random `LocalDateTime` in the range `[now - maxDuration, now]`.

```java
LocalDateTime past = lorem.getPriorDate(Duration.ofDays(365));
// => some point in the last year
```

### `getFutureDate(Duration maxDurationFromNow)`

Returns a random `LocalDateTime` in the range `[now, now + maxDuration]`.

```java
LocalDateTime future = lorem.getFutureDate(Duration.ofHours(48));
// => some point in the next 48 hours
```

---

## 8. Backing Data Files

All data is loaded from classpath resources under `com/thedeanda/lorem/`.

| File               | Used by                                                      |
|--------------------|--------------------------------------------------------------|
| `lorem.txt`        | `getWords`, `getTitle`, `getParagraphs`, `getHtmlParagraphs` |
| `male_names.txt`   | `getFirstNameMale`, `getNameMale`                            |
| `female_names.txt` | `getFirstNameFemale`, `getNameFemale`                        |
| `surnames.txt`     | `getLastName`, `getName`, `getEmail`                         |
| `cities.txt`       | `getCity`                                                    |
| `state_abbr.txt`   | `getStateAbbr`                                               |
| `state_full.txt`   | `getStateFull`                                               |
| `countries.txt`    | `getCountry`                                                 |

These files are loaded once at construction time. To customise the data pool, subclass `LoremIpsum` and override the relevant method.

---

## 9. Practical Usage Patterns

### Singleton — one-off generation

```java
Lorem lorem = LoremIpsum.getInstance();

String fullName = lorem.getName();
String email    = lorem.getEmail();
String city     = lorem.getCity();
String state    = lorem.getStateAbbr();
String zip      = lorem.getZipCode();
String body     = lorem.getParagraphs(2, 5);
```

### Seeded — reproducible tests

```java
Lorem lorem = new LoremIpsum(42L);
String name = lorem.getName();  // same result every run
```

### Custom Random — cryptographically secure

```java
Lorem lorem = new LoremIpsum(new SecureRandom());
```

### HTML content generation

```java
Lorem lorem = LoremIpsum.getInstance();
String article = "<h1>" + lorem.getTitle(4, 8) + "</h1>"
               + lorem.getHtmlParagraphs(3, 6);
```

### Date range generation

```java
Lorem lorem = LoremIpsum.getInstance();
LocalDateTime createdAt  = lorem.getPriorDate(Duration.ofDays(730));  // last 2 years
LocalDateTime expiresAt  = lorem.getFutureDate(Duration.ofDays(90));  // next 3 months
```

---

## 10. Version History

| Version | Date     | Notable changes                                   |
|---------|----------|---------------------------------------------------|
| 2.2     | Aug 2023 | Dependency updates; current Maven Central release |
| 2.1     | Sep 2016 | Fixed email addresses that could contain spaces   |
| 2.0     | Oct 2015 | Greatly expanded word list                        |
| 1.2     | Jun 2014 | —                                                 |
| 1.1     | May 2014 | —                                                 |
| 1.0     | Mar 2014 | Initial release                                   |

Selected commit milestones:

- **Nov 2019** — added `getPriorDate` / `getFutureDate`
- **Jan 2018** — added `LoremIpsum(Random random)` constructor for `SecureRandom` support
- **Aug 2020** — fixed `getParagraphs` single-space sentence separator

---

## 11. Comparison with krandom

| Feature                      | lorem                         | krandom                      |
|------------------------------|-------------------------------|------------------------------|
| Language                     | Java                          | Kotlin / Java                |
| First / last name            | ✅ (male/female pools)         | ✅                            |
| Full name                    | ✅                             | ✅                            |
| Email                        | ✅ (`@example.com`)            | ✅ (generated)                |
| Phone                        | ✅ (US NANP)                   | —                            |
| City                         | ✅                             | —                            |
| State (abbr + full)          | ✅                             | —                            |
| Country                      | ✅                             | —                            |
| ZIP code                     | ✅                             | —                            |
| Lorem ipsum text             | ✅ (words, titles, paragraphs) | —                            |
| HTML paragraph output        | ✅                             | —                            |
| Date range generation        | ✅ (`LocalDateTime`)           | ✅ (`BirthDay`)               |
| Gender-specific names        | ✅                             | ✅                            |
| Age                          | —                             | ✅                            |
| SSN                          | —                             | ✅                            |
| Primitives (int/float/bool…) | —                             | ✅                            |
| Dice / coin                  | —                             | ✅                            |
| Fibonacci / Luhn             | —                             | ✅                            |
| IPv4                         | —                             | ✅                            |
| Hash                         | —                             | ✅                            |
| Object-graph population      | —                             | ✅ (`ObjectGenerator`)        |
| Seeded reproducibility       | ✅                             | Partial (per-generator seed) |
| Singleton access             | ✅                             | —                            |

### Methods in lorem with direct krandom equivalents

| lorem                  | krandom                           |
|------------------------|-----------------------------------|
| `lorem.getFirstName()` | `FirstName.generate()`            |
| `lorem.getLastName()`  | `SurName.generate()`              |
| `lorem.getName()`      | `GenericUserGenerator.fullName()` |
| `lorem.getEmail()`     | `Email.generate()`                |

---

## 12. Potential Additions for krandom Inspired by lorem

| Category   | lorem methods                                                         | krandom gap                                             |
|------------|-----------------------------------------------------------------------|---------------------------------------------------------|
| Location   | `getCity`, `getStateAbbr`, `getStateFull`, `getZipCode`, `getCountry` | All missing                                             |
| Contact    | `getPhone`                                                            | Missing                                                 |
| Text       | `getWords`, `getTitle`, `getParagraphs`, `getHtmlParagraphs`          | All missing                                             |
| Web        | `getUrl`                                                              | Missing                                                 |
| Date range | `getPriorDate(Duration)`, `getFutureDate(Duration)`                   | `BirthDay` exists; general date-range generator missing |
