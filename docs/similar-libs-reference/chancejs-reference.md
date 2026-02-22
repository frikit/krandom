# Chance.js Reference

**Repository:** https://github.com/chancejs/chancejs
**Website:** https://chancejs.com
**Author:** Victor Quinn
**License:** MIT
**Status:** Active
**Runtime:** JavaScript / Node.js (browser and server)
**Install:** `npm install chance` / `<script src="chance.min.js">`

---

## 1. Purpose

Chance.js is a minimalist random data generator for JavaScript. It covers a wide range of domains — numerics, strings, dates, people, locations, finance, web, and more — with a single fluent API
object. It does **not** do object-graph randomization; instead, it provides building blocks for constructing test fixtures manually.

**Primary use cases:**

- Generating test data in JavaScript / Node.js test suites
- Seeded, reproducible randomization for deterministic tests
- Weighted and normally-distributed sampling
- Luhn-valid credit card numbers, SSNs, postal codes, UUIDs

```javascript
const Chance = require('chance');
const chance = new Chance();         // unseeded, crypto-random
const chance = new Chance(42);       // seeded, reproducible
const chance = new Chance(Math.random);  // custom RNG

chance.name();          // => 'Patrick Copeland'
chance.email();         // => '[email protected]'
chance.integer({min: 1, max: 100}); // => 47
```

---

## 2. Core Object

```javascript
const chance = new Chance(seed ?);
```

- `seed` can be a number, string, or custom RNG function.
- Without a seed, uses `Math.random()` (not cryptographically secure).
- The same seed always produces the same sequence.

---

## 3. Numbers

### `integer({min, max})`

| Option | Default                   | Description             |
|--------|---------------------------|-------------------------|
| `min`  | `Number.MIN_SAFE_INTEGER` | Lower bound (inclusive) |
| `max`  | `Number.MAX_SAFE_INTEGER` | Upper bound (inclusive) |

```javascript
chance.integer()                    // => -1841597286
chance.integer({min: 1, max: 10})   // => 7
```

### `natural({min, max, exclude})`

Non-negative integers only (≥ 0).

| Option    | Default                   | Description                             |
|-----------|---------------------------|-----------------------------------------|
| `min`     | `0`                       | Lower bound                             |
| `max`     | `Number.MAX_SAFE_INTEGER` | Upper bound                             |
| `exclude` | `[]`                      | Array of values to exclude from results |

```javascript
chance.natural()                            // => 2974305
chance.natural({min: 1, max: 20})           // => 13
chance.natural({exclude: [1, 2, 3]})        // never returns 1, 2, or 3
```

### `floating({min, max, fixed})`

| Option  | Default | Description              |
|---------|---------|--------------------------|
| `min`   | `-MAX`  | Lower bound              |
| `max`   | `MAX`   | Upper bound              |
| `fixed` | `4`     | Number of decimal places |

```javascript
chance.floating()                           // => -304534.44
chance.floating({min: 0, max: 1, fixed: 2}) // => 0.73
```

### `prime({min, max})`

Generates a random prime number within the range.

```javascript
chance.prime()                              // => 251
chance.prime({min: 2, max: 100})            // => 47
```

### `normal({mean, dev})`

Returns a normally-distributed random variate (Box-Muller transform).

| Option | Default | Description         |
|--------|---------|---------------------|
| `mean` | `0`     | Distribution centre |
| `dev`  | `1`     | Standard deviation  |

```javascript
chance.normal()                             // => 0.424
chance.normal({mean: 100, dev: 15})         // => 85.1   (IQ-like distribution)
```

---

## 4. Booleans and Strings

### `bool({likelihood})`

| Option       | Default | Description                      |
|--------------|---------|----------------------------------|
| `likelihood` | `50`    | Percent chance of `true` (0–100) |

```javascript
chance.bool()                               // => true
chance.bool({likelihood: 80})              // => true ~80% of the time
```

### `character({pool, alpha, numeric, symbols, casing})`

| Option    | Default             | Description                       |
|-----------|---------------------|-----------------------------------|
| `pool`    | All printable ASCII | Custom character pool string      |
| `alpha`   | —                   | Restrict to alphabetic characters |
| `numeric` | —                   | Restrict to digits                |
| `symbols` | —                   | Restrict to symbols               |
| `casing`  | —                   | `'upper'` or `'lower'`            |

```javascript
chance.character()                          // => 'g'
chance.character({alpha: true})             // => 'Z'
chance.character({pool: 'aeiou'})           // => 'o'
```

### `string({length, pool, casing, alpha, numeric, symbols})`

| Option    | Default             | Description            |
|-----------|---------------------|------------------------|
| `length`  | 5–20 (random)       | Fixed string length    |
| `pool`    | All printable ASCII | Custom character pool  |
| `casing`  | —                   | `'upper'` or `'lower'` |
| `alpha`   | —                   | Alphabetic only        |
| `numeric` | —                   | Digits only            |
| `symbols` | —                   | Symbols only           |

```javascript
chance.string()                             // => 'ztZdM'
chance.string({length: 10, alpha: true})    // => 'uVnBePtKmo'
chance.string({pool: 'abc', length: 5})     // => 'cabba'
```

---

## 5. Person

### `name({middle, middle_initial, prefix, suffix, gender, nationality})`

| Option           | Default | Description                           |
|------------------|---------|---------------------------------------|
| `middle`         | false   | Include full middle name              |
| `middle_initial` | false   | Include middle initial only           |
| `prefix`         | false   | Include title prefix (e.g., `Doctor`) |
| `suffix`         | false   | Include suffix (e.g., `Esq.`)         |
| `gender`         | random  | `'male'` or `'female'`                |
| `nationality`    | `'en'`  | `'en'` (English) or `'it'` (Italian)  |

```javascript
chance.name()                               // => 'Dafi Vatemi'
chance.name({middle: true})                 // => 'Nelgatwu Powuku Heup'
chance.name({prefix: true, gender: 'male'}) // => 'Doctor Patrick Copeland'
chance.name({nationality: 'it'})            // => 'Roberta Mazzetti'
```

### `first({gender, nationality})`

Nationalities: `'us'` (United States), `'it'` (Italy).

```javascript
chance.first()                              // => 'Leila'
chance.first({gender: 'female'})            // => 'Emma'
chance.first({nationality: 'it'})           // => 'Alberto'
```

### `last()`

```javascript
chance.last()                               // => 'Baker'
```

### `prefix({gender})` / `suffix()`

```javascript
chance.prefix()                             // => 'Mr.'
chance.prefix({gender: 'female'})           // => 'Mrs.'
chance.suffix()                             // => 'Esq.'
```

### `age({type})`

| Type       | Range  |
|------------|--------|
| `'child'`  | 1–12   |
| `'teen'`   | 13–19  |
| `'adult'`  | 18–65  |
| `'senior'` | 65–100 |
| (none)     | 1–120  |

```javascript
chance.age()                                // => 34
chance.age({type: 'teen'})                  // => 17
```

### `gender()`

Returns `'Male'` or `'Female'`.

```javascript
chance.gender()                             // => 'Female'
```

### `ssn({ssnFour, dashes})`

| Option    | Default | Description               |
|-----------|---------|---------------------------|
| `ssnFour` | false   | Return only last 4 digits |
| `dashes`  | true    | Include dashes in output  |

```javascript
chance.ssn()                                // => '411-90-0070'
chance.ssn({ssnFour: true})                 // => '2938'
chance.ssn({dashes: false})                 // => '293839295'
```

### `birthday({type, string, american})`

```javascript
chance.birthday()                           // => Date object
chance.birthday({type: 'adult'})            // => date in adult age range
chance.birthday({string: true})             // => '5/27/1983'
```

---

## 6. Location

### `address({short_suffix})`

```javascript
chance.address()                            // => '5447 Bazpe Lane'
chance.address({short_suffix: true})        // => '536 Baner Rd'
```

### `city()`

```javascript
chance.city()                               // => 'Cowotba'
```

### `state({full, territories, armed_forces, country})`

| Option         | Default | Description                              |
|----------------|---------|------------------------------------------|
| `full`         | false   | Return full name instead of abbreviation |
| `territories`  | false   | Include US territories (Guam, etc.)      |
| `armed_forces` | false   | Include Armed Forces regions             |
| `country`      | `'us'`  | `'us'` or `'it'`                         |

```javascript
chance.state()                              // => 'AK'
chance.state({full: true})                  // => 'Florida'
chance.state({territories: true, full: true}) // => 'Guam'
chance.state({country: 'it', full: true})   // => 'Toscana'
```

### `zip({plusfour})`

```javascript
chance.zip()                                // => '90210'
chance.zip({plusfour: true})                // => '01035-1838'
```

### `postal()`

Canadian postal codes.

```javascript
chance.postal()                             // => 'K1A 0B1'
```

### `postcode()`

UK postcodes.

```javascript
chance.postcode()                           // => 'SW1A 2AA'
```

### `country({full})`

```javascript
chance.country()                            // => 'DE'
chance.country({full: true})                // => 'Germany'
```

### `phone({formatted, country, mobile})`

Supported countries: `'us'`, `'uk'`, `'fr'`.

| Option      | Default | Description                              |
|-------------|---------|------------------------------------------|
| `formatted` | true    | Include formatting (parentheses, dashes) |
| `country`   | `'us'`  | Target country                           |
| `mobile`    | false   | Generate mobile (UK only)                |

```javascript
chance.phone()                              // => '(494) 927-2152'
chance.phone({formatted: false})            // => '2617613391'
chance.phone({country: 'fr'})              // => '01 60 44 92 67'
chance.phone({country: 'uk', mobile: true}) // => '07624 321221'
```

### `areacode()`

US area codes only.

```javascript
chance.areacode()                           // => '(789)'
```

### `latitude({fixed, min, max})`

| Option  | Default | Description      |
|---------|---------|------------------|
| `fixed` | 5       | Decimal places   |
| `min`   | -90     | Minimum latitude |
| `max`   | 90      | Maximum latitude |

```javascript
chance.latitude()                           // => 57.99514
chance.latitude({fixed: 7})                 // => -29.6443133
chance.latitude({min: 38.7, max: 38.9})     // => 38.82358
```

### `longitude({fixed, min, max})`

Same signature as `latitude()`, range -180 to 180.

```javascript
chance.longitude()                          // => -101.56823
```

### `coordinates()`

Returns a `"lat, lon"` string.

```javascript
chance.coordinates()                        // => '35.12423, -80.12345'
```

### `altitude({fixed, min, max})` / `depth({fixed, min, max})`

`altitude` — metres above sea level (default range 0–8848, Mount Everest).
`depth` — metres below sea level (returns negative values, default range 0 to -10994, Mariana Trench).

```javascript
chance.altitude()                           // => 3451.23456
chance.depth()                              // => -2314.45678
```

### `geohash({length})`

Encodes a random location as a geohash string (default 7 characters).

```javascript
chance.geohash()                            // => 'gbsuv7z'
```

---

## 7. Finance

### `cc({type})`

Generates a Luhn-valid credit card number.

| Option | Description                                                                                                        |
|--------|--------------------------------------------------------------------------------------------------------------------|
| `type` | Card type by long name (`'Visa'`, `'Mastercard'`, `'American Express'`) or short name (`'visa'`, `'mc'`, `'amex'`) |

```javascript
chance.cc()                                 // => '6304038511073827'
chance.cc({type: 'Mastercard'})             // => '5171206237468496'
chance.cc({type: 'mc'})                     // => '5103820202214116'
```

### `cc_type({name})`

Returns a credit card type object `{name, short_name, prefix, length}`.

```javascript
chance.cc_type()                            // => { name: 'Visa', short_name: 'visa', prefix: '4', length: 16 }
chance.cc_type({name: true})                // => 'Mastercard'
```

### `currency()`

```javascript
chance.currency()                           // => { code: 'TVD', name: 'Tuvalu Dollar' }
```

### `currency_pair()`

Returns two different currency objects as an array (useful for FX rate simulation).

```javascript
chance.currency_pair()                      // => [{ code: 'EUR', ... }, { code: 'JPY', ... }]
```

### `dollar({max})` / `euro({max})`

| Option | Default | Description               |
|--------|---------|---------------------------|
| `max`  | 10000   | Upper bound of the amount |

```javascript
chance.dollar()                             // => '$2560.27'
chance.dollar({max: 20})                    // => '$15.23'
chance.euro()                               // => '€1842.56'
```

### `exp({future})` / `exp_month({future})` / `exp_year({future})`

Credit card expiration. `future: true` guarantees a future date.

```javascript
chance.exp()                                // => '03/23'
chance.exp_month()                          // => '07'
chance.exp_year()                           // => '2026'
chance.exp_month({future: true})            // => a month in current or future year
```

---

## 8. Web

### `email({domain})`

```javascript
chance.email()                              // => '[email protected]'
chance.email({domain: 'example.com'})       // => '[email protected]'
```

### `domain({tld})`

```javascript
chance.domain()                             // => 'onaro.net'
chance.domain({tld: 'ie'})                  // => 'gotaujo.ie'
```

### `tld()`

```javascript
chance.tld()                                // => 'com'
```

### `url({protocol, domain, domain_prefix, path, extensions})`

| Option          | Default  | Description                              |
|-----------------|----------|------------------------------------------|
| `protocol`      | `'http'` | URL scheme                               |
| `domain`        | random   | Fixed domain                             |
| `domain_prefix` | random   | Fixed subdomain                          |
| `path`          | random   | Fixed path                               |
| `extensions`    | —        | Array to pick random file extension from |

```javascript
chance.url()                                // => 'http://vanogsi.io/pateliivi'
chance.url({protocol: 'ftp'})               // => 'ftp://mibfu.nr/kardate'
chance.url({domain: 'example.com'})         // => 'http://example.com/hob'
chance.url({extensions: ['gif', 'jpg']})    // => 'http://vagjiup.gov/img.jpg'
```

### `ip()` / `ipv6()`

```javascript
chance.ip()                                 // => '95.187.217.4'
chance.ipv6()                               // => '2407:d300:a0:4900:15a0:a83:b6fc:3736'
```

### `color({format, grayscale, casing})`

| Option      | Default   | Description                            |
|-------------|-----------|----------------------------------------|
| `format`    | `'hex'`   | `'hex'`, `'shorthex'`, `'rgb'`, `'0x'` |
| `grayscale` | false     | Restrict to grey shades                |
| `casing`    | `'lower'` | `'upper'` for uppercase hex digits     |

```javascript
chance.color()                              // => '#79c157'
chance.color({format: 'rgb'})               // => 'rgb(110,52,164)'
chance.color({format: 'shorthex'})          // => '#60f'
chance.color({grayscale: true})             // => '#e2e2e2'
```

### `twitter()`

Returns a random Twitter handle (with `@` prefix).

```javascript
chance.twitter()                            // => '@dafivatemin'
```

### `avatar({type, fileExtension, protocol, email})`

Returns a URL to a Gravatar-style avatar or a data URI.

```javascript
chance.avatar()                             // => 'https://www.gravatar.com/avatar/...'
```

### `company()` / `profession({ranked})`

```javascript
chance.company()                            // => 'Jombo LLC'
chance.profession()                         // => 'Photographer'
chance.profession({ranked: true})           // => a common profession (biased toward frequent jobs)
```

---

## 9. Time

### `date({string, american, year, month, day})`

Returns a JavaScript `Date` object by default, or a string.

| Option     | Default | Description                                 |
|------------|---------|---------------------------------------------|
| `string`   | false   | Return as formatted string                  |
| `american` | true    | `MM/DD/YYYY` (true) vs `DD/MM/YYYY` (false) |
| `year`     | random  | Fix the year                                |
| `month`    | random  | Fix month (0-indexed, like `Date`)          |
| `day`      | random  | Fix day of month                            |

```javascript
chance.date()                               // => Sat Apr 09 2072 ...
chance.date({string: true})                 // => '5/27/2078'
chance.date({string: true, american: false}) // => '27/5/2078'
chance.date({year: 1990})                   // => a date in 1990
```

### `year({min, max})` / `month({raw})` / `hour({twentyfour})`

```javascript
chance.year()                               // => 2051
chance.year({min: 2000, max: 2030})         // => 2017
chance.month()                              // => 'October'
chance.month({raw: true})                   // => { name: 'October', short_name: 'Oct', numeric: 10 }
chance.hour()                               // => 11
chance.hour({twentyfour: true})             // => 23
```

### `minute()` / `second()` / `millisecond()` / `ampm()` / `timestamp()`

```javascript
chance.minute()                             // => 37
chance.second()                             // => 12
chance.millisecond()                        // => 742
chance.ampm()                               // => 'pm'
chance.timestamp()                          // => 1482975167  (Unix seconds)
```

---

## 10. Text

### `word({syllables, length})`

| Option      | Description                                 |
|-------------|---------------------------------------------|
| `syllables` | Number of syllables (default 2)             |
| `length`    | Exact character count (overrides syllables) |

```javascript
chance.word()                               // => 'nufraw'
chance.word({syllables: 4})                 // => 'pugilefe'
chance.word({length: 10})                   // => 'tavapiwoze'
```

### `syllable({length})`

Single pronounceable syllable. Default length 2 or 3.

```javascript
chance.syllable()                           // => 'ko'
```

### `sentence({words})`

Starts with a capital letter, ends with a period. Default 12–18 words.

```javascript
chance.sentence()                           // => 'Witpevze mappos isoletu fo res bi geow.'
chance.sentence({words: 5})                 // => 'Waddik jeasmov cakgilta ficub up.'
```

### `paragraph({sentences})`

Default 3–7 sentences.

```javascript
chance.paragraph()                          // => multi-sentence paragraph
chance.paragraph({sentences: 3})            // => exactly 3 sentences
```

---

## 11. Miscellaneous

### `guid({version})`

| Option    | Default | Description                      |
|-----------|---------|----------------------------------|
| `version` | `5`     | `4` (random) or `5` (name-based) |

```javascript
chance.guid()                               // => 'f0d8368d-85e2-54fb-73c4-2d60374295e3'
chance.guid({version: 4})                   // => 'c71f58e3-34af-43c0-b405-2764d6947d21'
```

### `hash({length, casing})`

Default: 40-character lowercase hex (matches git commit hash length).

```javascript
chance.hash()                               // => 'e5162f27da96ed8e1ae51def1ba643b91d2581d8'
chance.hash({length: 15})                   // => 'c28f57cb599ada4'
chance.hash({casing: 'upper'})              // => '3F2EB3FB85D88984C1EC4F46A3DBE740B5E0E56E'
```

### `coin()`

```javascript
chance.coin()                               // => 'heads'
```

### `rpg(pattern, {sum})`

Pattern: `NdS` where N = number of dice, S = sides.

```javascript
chance.rpg('3d10')                          // => [1, 6, 9]
chance.rpg('5d6')                           // => [3, 1, 2, 5, 2]
chance.rpg('3d10', {sum: true})             // => 16
```

---

## 12. Helpers

### `n(fn, count, options)`

Call any Chance method `count` times and return the results as an array.

```javascript
chance.n(chance.integer, 5, {min: 1, max: 10})  // => [4, 7, 1, 9, 3]
```

### `unique(fn, count, options)`

Like `n()`, but guarantees all values are distinct. Throws `RangeError` if pool is too small.

```javascript
chance.unique(chance.state, 5)              // => ['SC', 'WA', 'CO', 'TX', 'ND']
chance.unique(chance.integer, 10, {min: 0, max: 100})  // => 10 distinct integers
```

Custom comparator for object uniqueness:

```javascript
chance.unique(chance.currency, 2, {
    comparator: (arr, val) => arr.reduce((acc, item) => acc || item.code === val.code, false)
});
```

### `pick(arr)` / `pickset(arr, n)` / `shuffle(arr)`

```javascript
chance.pick(['a', 'b', 'c'])                // => 'b'
chance.pickset(['a', 'b', 'c'], 2)          // => ['c', 'a']
chance.shuffle([1, 2, 3, 4])               // => [3, 1, 4, 2]
```

### `weighted(values, weights)`

Weighted random selection. `weights` is an array of numbers with the same length as `values`.

```javascript
chance.weighted(['heads', 'tails'], [7, 3]) // => 'heads' ~70% of the time
```

---

## 13. Seeding and Reproducibility

```javascript
// Same seed → same sequence
const a = new Chance(1);
const b = new Chance(1);
a.integer() === b.integer();  // => true

// Reset to same seed to replay
chance.seed(42);
```

---

## 14. Comparison with krandom

| Feature                          | Chance.js                 | krandom                      |
|----------------------------------|---------------------------|------------------------------|
| Language                         | JavaScript                | Kotlin / Java                |
| Object graph generation          | No                        | Yes (`ObjectGenerator`)      |
| Seeded reproducibility           | Yes                       | Partial (per-generator seed) |
| Normally-distributed values      | Yes                       | No                           |
| Weighted random                  | Yes                       | No                           |
| Luhn-valid credit cards          | Yes                       | Yes (`LuhnGenerator`)        |
| Fibonacci numbers                | No                        | Yes (`FibonacciGenerator`)   |
| Dice / RPG notation              | Yes                       | Yes (`Dice`, DiceType enum)  |
| Coin flip                        | Yes (`'heads'`/`'tails'`) | Yes (enum)                   |
| IPv4 / IPv6                      | Yes                       | IPv4 only                    |
| Person data (name, SSN, etc.)    | Yes                       | Yes (Kotlin layer)           |
| Finance (CC, currency, dollar)   | Yes                       | Partial (Luhn only)          |
| Location (address, city, state)  | Yes                       | No                           |
| Text generation (sentence, para) | Yes                       | No                           |
| GUID / UUID                      | Yes                       | No                           |
| Bean/annotation-driven           | No                        | No                           |
| JVM interop                      | No                        | Yes                          |

### Generators in krandom with direct Chance.js equivalents

| krandom                        | Chance.js                             |
|--------------------------------|---------------------------------------|
| `Generators.ofInt(min, max)`   | `chance.integer({min, max})`          |
| `Generators.ofFloat(min, max)` | `chance.floating({min, max})`         |
| `Generators.ofBoolean()`       | `chance.bool()`                       |
| `Generators.ofString()`        | `chance.string()`                     |
| `Generators.ofFibonacci()`     | — (no equivalent)                     |
| `Generators.ofLuhn()`          | `chance.cc()`                         |
| `NaturalNumberGenerator`       | `chance.natural()` / `chance.prime()` |
| `HexHashGenerator`             | `chance.hash()`                       |
| `IPv4Random`                   | `chance.ip()`                         |
| `Dice`                         | `chance.rpg()`                        |
| `Coin`                         | `chance.coin()`                       |
| `Email`                        | `chance.email()`                      |
| `SocialSecurityNumber`         | `chance.ssn()`                        |
| `FirstName` / `SurName`        | `chance.first()` / `chance.last()`    |
| `Age`                          | `chance.age()`                        |
| `Gender`                       | `chance.gender()`                     |
| `BirthDay`                     | `chance.birthday()`                   |

---

## 15. Potential Additions Inspired by Chance.js

Features present in Chance.js that krandom does not yet cover:

| Category | Methods                                                                                                                                              |
|----------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Location | `city`, `state`, `country`, `zip`, `postal`, `postcode`, `phone`, `areacode`, `latitude`, `longitude`, `coordinates`, `altitude`, `depth`, `geohash` |
| Finance  | `cc` types (Visa/MC/Amex etc.), `currency`, `dollar`, `euro`, `exp` dates                                                                            |
| Web      | `domain`, `url`, `tld`, `ipv6`, `color`, `twitter`, `avatar`, `company`, `profession`                                                                |
| Text     | `sentence`, `paragraph`, `syllable`                                                                                                                  |
| Time     | `year`, `month`, `hour`, `minute`, `second`, `timestamp`                                                                                             |
| Misc     | `guid`, `normal` distribution, `weighted`, `unique`, `rpg` notation                                                                                  |
