# Chance.js Feature Parity Analysis

## Library Overview

- **Name**: Chance.js
- **Language**: JavaScript / Node.js
- **Version Analyzed**: Latest (2024+)
- **GitHub**: https://github.com/chancejs/chancejs
- **Website**: https://chancejs.com
- **License**: MIT
- **Key Strengths**: Mobile-first support, weighted random, natural language generation, extensive options parameters, minimalist fluent API

## Executive Summary

Chance.js is a minimalist yet powerful random data generator for JavaScript with unique strengths in **weighted random selection**, **normally-distributed values**, **mobile device support**, and *
*rich parameterization**. Unlike object-graph generators, it provides building blocks for manual fixture construction with a clean fluent API. Key differentiators include:

- **Weighted random** - `weighted(['heads', 'tails'], [7, 3])` for biased selection
- **Normal distribution** - Box-Muller transform for realistic statistical data
- **Mobile-first** - UK/US/FR mobile phone formats, device-specific data
- **Rich options** - Every method supports extensive parameter customization
- **Helper methods** - `n()`, `unique()`, `pickset()`, `shuffle()` for collection operations
- **Natural language** - Sentence/paragraph generation with syllable control
- **Reproducible seeding** - Full deterministic test support
- **Likelihood control** - `bool({likelihood: 80})` for probability-based generation

---

## Feature Categories

### 1. NUMBERS

| Feature                 | Chance.js Support                | krandom Status | Implementation Priority | Notes                                |
|-------------------------|----------------------------------|----------------|-------------------------|--------------------------------------|
| **Integer Generation**  |
| Random integer          | ✅ `integer({min, max})`          | ✅ Yes          | ✓ DONE                  | `Generators.ofInt()`                 |
| Natural numbers (≥0)    | ✅ `natural({min, max, exclude})` | ❌ No           | HIGH                    | Unique: exclude array                |
| Prime numbers           | ✅ `prime({min, max})`            | ❌ No           | MEDIUM                  | Generates actual primes              |
| **Floating Point**      |
| Random float            | ✅ `floating({min, max, fixed})`  | ✅ Yes          | ✓ DONE                  | `Generators.ofFloat()`               |
| Fixed decimal places    | ✅ `fixed` parameter              | ❌ No           | MEDIUM                  | `floating({fixed: 2})`               |
| **Statistical**         |
| Normal distribution     | ✅ `normal({mean, dev})`          | ❌ No           | HIGH                    | Box-Muller transform, unique feature |
| Standard deviation      | ✅ `dev` parameter                | ❌ No           | HIGH                    | Control distribution spread          |
| **Exclusion**           |
| Exclude specific values | ✅ `natural({exclude: [1,2,3]})`  | ❌ No           | MEDIUM                  | Never return specific numbers        |

### 2. BOOLEANS & BASIC TYPES

| Feature               | Chance.js Support                                           | krandom Status | Implementation Priority | Notes                            |
|-----------------------|-------------------------------------------------------------|----------------|-------------------------|----------------------------------|
| **Boolean**           |
| Random boolean        | ✅ `bool()`                                                  | ✅ Yes          | ✓ DONE                  | `Generators.ofBoolean()`         |
| Weighted boolean      | ✅ `bool({likelihood: 80})`                                  | ❌ No           | HIGH                    | Return true 80% of time - UNIQUE |
| **Characters**        |
| Random character      | ✅ `character({pool, alpha, numeric, symbols, casing})`      | ❌ No           | MEDIUM                  | Extensive options                |
| Custom character pool | ✅ `character({pool: 'aeiou'})`                              | ❌ No           | MEDIUM                  | Select from custom set           |
| Alpha only            | ✅ `character({alpha: true})`                                | ❌ No           | MEDIUM                  | A-Z only                         |
| Numeric only          | ✅ `character({numeric: true})`                              | ❌ No           | MEDIUM                  | 0-9 only                         |
| Symbols only          | ✅ `character({symbols: true})`                              | ❌ No           | LOW                     | Special chars                    |
| Case control          | ✅ `character({casing: 'upper'/'lower'})`                    | ❌ No           | MEDIUM                  | Force case                       |
| **Strings**           |
| Random string         | ✅ `string({length, pool, casing, alpha, numeric, symbols})` | ✅ Yes          | ✓ DONE                  | `Generators.ofString()`          |
| Variable length       | ✅ Default 5-20 random                                       | ❌ No           | MEDIUM                  | No fixed length required         |
| Fixed length          | ✅ `string({length: 10})`                                    | ✅ Yes          | ✓ DONE                  |                                  |
| Custom pool           | ✅ `string({pool: 'abc', length: 5})`                        | ❌ No           | MEDIUM                  | 'cabba'                          |
| Alpha strings         | ✅ `string({alpha: true})`                                   | ❌ No           | MEDIUM                  | Letters only                     |
| Numeric strings       | ✅ `string({numeric: true})`                                 | ❌ No           | MEDIUM                  | Digits as string                 |

### 3. PERSON IDENTITY

| Feature               | Chance.js Support                                                       | krandom Status | Implementation Priority | Notes                        |
|-----------------------|-------------------------------------------------------------------------|----------------|-------------------------|------------------------------|
| **Names**             |
| Full name             | ✅ `name({middle, middle_initial, prefix, suffix, gender, nationality})` | ✅ Partial      | HIGH                    | krandom lacks options        |
| First name            | ✅ `first({gender, nationality})`                                        | ✅ Yes          | ✓ DONE                  | `FirstName`                  |
| Last name             | ✅ `last()`                                                              | ✅ Yes          | ✓ DONE                  | `SurName`                    |
| Middle name           | ✅ `name({middle: true})`                                                | ❌ No           | MEDIUM                  | Full middle name             |
| Middle initial        | ✅ `name({middle_initial: true})`                                        | ❌ No           | MEDIUM                  | 'J.' only                    |
| Name prefix           | ✅ `prefix({gender})`                                                    | ❌ No           | MEDIUM                  | Mr., Mrs., Dr.               |
| Name suffix           | ✅ `suffix()`                                                            | ❌ No           | MEDIUM                  | Esq., Jr., Sr., III          |
| Gender-specific names | ✅ `first({gender: 'male'/'female'})`                                    | ❌ No           | HIGH                    | Gender-aware                 |
| Nationality support   | ✅ `name({nationality: 'en'/'it'})`                                      | ❌ No           | MEDIUM                  | English/Italian              |
| US nationality        | ✅ `first({nationality: 'us'})`                                          | ❌ No           | MEDIUM                  | American names               |
| Italian nationality   | ✅ `first({nationality: 'it'})`                                          | ❌ No           | LOW                     | Alberto, Roberta             |
| **Demographics**      |
| Age                   | ✅ `age({type: 'child'/'teen'/'adult'/'senior'})`                        | ✅ Yes          | ✓ DONE                  | Type-based ranges            |
| Age ranges            | ✅ child(1-12), teen(13-19), adult(18-65), senior(65-100)                | ❌ No           | MEDIUM                  | Predefined ranges            |
| Gender                | ✅ `gender()`                                                            | ✅ Yes          | ✓ DONE                  | 'Male'/'Female'              |
| Birthday              | ✅ `birthday({type, string, american})`                                  | ✅ Yes          | ✓ DONE                  | `BirthDay`                   |
| Birthday as string    | ✅ `birthday({string: true})`                                            | ❌ No           | MEDIUM                  | '5/27/1983'                  |
| American format       | ✅ `birthday({american: true})`                                          | ❌ No           | LOW                     | MM/DD/YYYY                   |
| Type-based birthday   | ✅ `birthday({type: 'adult'})`                                           | ❌ No           | MEDIUM                  | Age-appropriate              |
| **ID Numbers**        |
| SSN                   | ✅ `ssn({ssnFour, dashes})`                                              | ✅ Yes          | ✓ DONE                  | `SocialSecurityNumber`       |
| Last 4 SSN            | ✅ `ssn({ssnFour: true})`                                                | ❌ No           | MEDIUM                  | '2938' only                  |
| SSN format control    | ✅ `ssn({dashes: false})`                                                | ❌ No           | MEDIUM                  | '293839295' vs '411-90-0070' |

### 4. LOCATION & ADDRESS

| Feature            | Chance.js Support                        | krandom Status | Implementation Priority | Notes                   |
|--------------------|------------------------------------------|----------------|-------------------------|-------------------------|
| **Street Address** |
| Full address       | ✅ `address({short_suffix})`              | ❌ No           | HIGH                    | '5447 Bazpe Lane'       |
| Short suffix       | ✅ `address({short_suffix: true})`        | ❌ No           | MEDIUM                  | 'Rd' vs 'Road'          |
| **City**           |
| City name          | ✅ `city()`                               | ❌ No           | HIGH                    | Random city names       |
| **State/Province** |
| State abbreviation | ✅ `state()`                              | ❌ No           | HIGH                    | 'AK', 'CA', 'TX'        |
| State full name    | ✅ `state({full: true})`                  | ❌ No           | HIGH                    | 'Florida', 'Alaska'     |
| US territories     | ✅ `state({territories: true})`           | ❌ No           | MEDIUM                  | Guam, Puerto Rico       |
| Armed forces       | ✅ `state({armed_forces: true})`          | ❌ No           | LOW                     | Military regions        |
| Country support    | ✅ `state({country: 'us'/'it'})`          | ❌ No           | MEDIUM                  | US or Italian states    |
| Italian regions    | ✅ `state({country: 'it', full: true})`   | ❌ No           | LOW                     | 'Toscana'               |
| **Postal Codes**   |
| US ZIP             | ✅ `zip()`                                | ❌ No           | HIGH                    | '90210'                 |
| ZIP+4              | ✅ `zip({plusfour: true})`                | ❌ No           | MEDIUM                  | '01035-1838'            |
| Canadian postal    | ✅ `postal()`                             | ❌ No           | MEDIUM                  | 'K1A 0B1'               |
| UK postcode        | ✅ `postcode()`                           | ❌ No           | MEDIUM                  | 'SW1A 2AA'              |
| **Country**        |
| Country code       | ✅ `country()`                            | ❌ No           | HIGH                    | 'DE', 'FR', 'US'        |
| Country full name  | ✅ `country({full: true})`                | ❌ No           | HIGH                    | 'Germany'               |
| **Phone Numbers**  |
| Phone number       | ✅ `phone({formatted, country, mobile})`  | ❌ No           | HIGH                    | Multi-country support   |
| US format          | ✅ `phone()`                              | ❌ No           | HIGH                    | '(494) 927-2152'        |
| Unformatted        | ✅ `phone({formatted: false})`            | ❌ No           | MEDIUM                  | '2617613391'            |
| UK phone           | ✅ `phone({country: 'uk'})`               | ❌ No           | MEDIUM                  | International           |
| UK mobile          | ✅ `phone({country: 'uk', mobile: true})` | ❌ No           | MEDIUM                  | '07624 321221' - UNIQUE |
| French phone       | ✅ `phone({country: 'fr'})`               | ❌ No           | MEDIUM                  | '01 60 44 92 67'        |
| US area code       | ✅ `areacode()`                           | ❌ No           | MEDIUM                  | '(789)'                 |
| **Coordinates**    |
| Latitude           | ✅ `latitude({fixed, min, max})`          | ❌ No           | MEDIUM                  | 57.99514                |
| Longitude          | ✅ `longitude({fixed, min, max})`         | ❌ No           | MEDIUM                  | -101.56823              |
| Decimal precision  | ✅ `latitude({fixed: 7})`                 | ❌ No           | MEDIUM                  | Control precision       |
| Range restriction  | ✅ `latitude({min: 38.7, max: 38.9})`     | ❌ No           | MEDIUM                  | Bounded coordinates     |
| Coordinates pair   | ✅ `coordinates()`                        | ❌ No           | MEDIUM                  | '35.12423, -80.12345'   |
| Altitude           | ✅ `altitude({fixed, min, max})`          | ❌ No           | LOW                     | 0-8848m (Mt Everest)    |
| Depth              | ✅ `depth({fixed, min, max})`             | ❌ No           | LOW                     | 0 to -10994m (Mariana)  |
| Geohash            | ✅ `geohash({length})`                    | ❌ No           | LOW                     | 'gbsuv7z' (7 chars)     |

### 5. FINANCE

| Feature             | Chance.js Support                         | krandom Status | Implementation Priority | Notes                                |
|---------------------|-------------------------------------------|----------------|-------------------------|--------------------------------------|
| **Credit Cards**    |
| Credit card number  | ✅ `cc({type})`                            | ✅ Partial      | HIGH                    | Luhn-valid                           |
| Visa                | ✅ `cc({type: 'Visa'/'visa'})`             | ❌ No           | HIGH                    | Long/short names                     |
| Mastercard          | ✅ `cc({type: 'Mastercard'/'mc'})`         | ❌ No           | HIGH                    | Multiple formats                     |
| American Express    | ✅ `cc({type: 'American Express'/'amex'})` | ❌ No           | HIGH                    | 15-digit                             |
| Card type object    | ✅ `cc_type()`                             | ❌ No           | MEDIUM                  | {name, short_name, prefix, length}   |
| Card type by name   | ✅ `cc_type({name: true})`                 | ❌ No           | LOW                     | Return name only                     |
| **Currency**        |
| Currency object     | ✅ `currency()`                            | ❌ No           | MEDIUM                  | {code: 'TVD', name: 'Tuvalu Dollar'} |
| Currency pair       | ✅ `currency_pair()`                       | ❌ No           | MEDIUM                  | FX rate simulation - UNIQUE          |
| Dollar amount       | ✅ `dollar({max})`                         | ❌ No           | HIGH                    | '$2560.27'                           |
| Euro amount         | ✅ `euro({max})`                           | ❌ No           | MEDIUM                  | '€1842.56'                           |
| Max amount control  | ✅ `dollar({max: 20})`                     | ❌ No           | MEDIUM                  | '$15.23'                             |
| **Card Expiration** |
| Expiration date     | ✅ `exp({future})`                         | ❌ No           | HIGH                    | '03/23'                              |
| Future expiration   | ✅ `exp({future: true})`                   | ❌ No           | HIGH                    | Guaranteed future                    |
| Expiration month    | ✅ `exp_month({future})`                   | ❌ No           | MEDIUM                  | '07'                                 |
| Expiration year     | ✅ `exp_year({future})`                    | ❌ No           | MEDIUM                  | '2026'                               |

### 6. WEB & INTERNET

| Feature           | Chance.js Support                                            | krandom Status | Implementation Priority | Notes                         |
|-------------------|--------------------------------------------------------------|----------------|-------------------------|-------------------------------|
| **Email**         |
| Email address     | ✅ `email({domain})`                                          | ❌ No           | HIGH                    | '[email protected]'           |
| Custom domain     | ✅ `email({domain: 'example.com'})`                           | ❌ No           | HIGH                    | '[email protected]'           |
| **Domain & URL**  |
| Domain name       | ✅ `domain({tld})`                                            | ❌ No           | HIGH                    | 'onaro.net'                   |
| Custom TLD        | ✅ `domain({tld: 'ie'})`                                      | ❌ No           | MEDIUM                  | 'gotaujo.ie'                  |
| TLD only          | ✅ `tld()`                                                    | ❌ No           | MEDIUM                  | 'com', 'org', 'net'           |
| Full URL          | ✅ `url({protocol, domain, domain_prefix, path, extensions})` | ❌ No           | HIGH                    | Rich options                  |
| Custom protocol   | ✅ `url({protocol: 'ftp'})`                                   | ❌ No           | MEDIUM                  | 'ftp://...'                   |
| Fixed domain      | ✅ `url({domain: 'example.com'})`                             | ❌ No           | MEDIUM                  | Control domain                |
| Domain prefix     | ✅ `url({domain_prefix: 'api'})`                              | ❌ No           | MEDIUM                  | Subdomain control             |
| Fixed path        | ✅ `url({path: '/api/v1'})`                                   | ❌ No           | MEDIUM                  | Control path                  |
| File extensions   | ✅ `url({extensions: ['gif','jpg']})`                         | ❌ No           | MEDIUM                  | Random file type              |
| **IP Addresses**  |
| IPv4              | ✅ `ip()`                                                     | ✅ Yes          | ✓ DONE                  | '95.187.217.4'                |
| IPv6              | ✅ `ipv6()`                                                   | ❌ No           | HIGH                    | '2407:d300:a0:4900:...'       |
| **Colors**        |
| Color hex         | ✅ `color({format: 'hex'})`                                   | ❌ No           | MEDIUM                  | '#79c157'                     |
| Short hex         | ✅ `color({format: 'shorthex'})`                              | ❌ No           | MEDIUM                  | '#60f'                        |
| RGB format        | ✅ `color({format: 'rgb'})`                                   | ❌ No           | MEDIUM                  | 'rgb(110,52,164)'             |
| 0x format         | ✅ `color({format: '0x'})`                                    | ❌ No           | LOW                     | Hex number                    |
| Grayscale         | ✅ `color({grayscale: true})`                                 | ❌ No           | LOW                     | '#e2e2e2'                     |
| Case control      | ✅ `color({casing: 'upper'})`                                 | ❌ No           | LOW                     | Uppercase hex                 |
| **Social**        |
| Twitter handle    | ✅ `twitter()`                                                | ❌ No           | LOW                     | '@dafivatemin'                |
| Avatar URL        | ✅ `avatar({type, fileExtension, protocol, email})`           | ❌ No           | LOW                     | Gravatar URLs                 |
| **Business**      |
| Company name      | ✅ `company()`                                                | ❌ No           | MEDIUM                  | 'Jombo LLC'                   |
| Profession        | ✅ `profession({ranked})`                                     | ❌ No           | MEDIUM                  | Job titles                    |
| Ranked profession | ✅ `profession({ranked: true})`                               | ❌ No           | MEDIUM                  | Biased toward common - UNIQUE |

### 7. TIME & DATES

| Feature             | Chance.js Support                              | krandom Status | Implementation Priority | Notes                       |
|---------------------|------------------------------------------------|----------------|-------------------------|-----------------------------|
| **Date Objects**    |
| Random date         | ✅ `date({string, american, year, month, day})` | ❌ No           | HIGH                    | Date object or string       |
| Date as string      | ✅ `date({string: true})`                       | ❌ No           | HIGH                    | '5/27/2078'                 |
| American format     | ✅ `date({american: true})`                     | ❌ No           | MEDIUM                  | MM/DD/YYYY                  |
| European format     | ✅ `date({american: false})`                    | ❌ No           | MEDIUM                  | DD/MM/YYYY                  |
| Fixed year          | ✅ `date({year: 1990})`                         | ❌ No           | MEDIUM                  | Date in specific year       |
| Fixed month         | ✅ `date({month: 5})`                           | ❌ No           | MEDIUM                  | 0-indexed like Date()       |
| Fixed day           | ✅ `date({day: 15})`                            | ❌ No           | MEDIUM                  | Day of month                |
| **Date Components** |
| Year                | ✅ `year({min, max})`                           | ❌ No           | MEDIUM                  | Random year with range      |
| Month name          | ✅ `month()`                                    | ❌ No           | MEDIUM                  | 'October'                   |
| Month object        | ✅ `month({raw: true})`                         | ❌ No           | MEDIUM                  | {name, short_name, numeric} |
| Hour (12-hour)      | ✅ `hour()`                                     | ❌ No           | MEDIUM                  | 1-12                        |
| Hour (24-hour)      | ✅ `hour({twentyfour: true})`                   | ❌ No           | MEDIUM                  | 0-23                        |
| Minute              | ✅ `minute()`                                   | ❌ No           | MEDIUM                  | 0-59                        |
| Second              | ✅ `second()`                                   | ❌ No           | MEDIUM                  | 0-59                        |
| Millisecond         | ✅ `millisecond()`                              | ❌ No           | MEDIUM                  | 0-999                       |
| AM/PM               | ✅ `ampm()`                                     | ❌ No           | LOW                     | 'am'/'pm'                   |
| Unix timestamp      | ✅ `timestamp()`                                | ❌ No           | MEDIUM                  | 1482975167                  |

### 8. TEXT & NATURAL LANGUAGE

| Feature          | Chance.js Support             | krandom Status | Implementation Priority | Notes                   |
|------------------|-------------------------------|----------------|-------------------------|-------------------------|
| **Words**        |
| Random word      | ✅ `word({syllables, length})` | ❌ No           | HIGH                    | Natural-looking words   |
| Syllable control | ✅ `word({syllables: 4})`      | ❌ No           | HIGH                    | 'pugilefe' - UNIQUE     |
| Length control   | ✅ `word({length: 10})`        | ❌ No           | HIGH                    | Exact character count   |
| Syllable         | ✅ `syllable({length})`        | ❌ No           | MEDIUM                  | Single syllable 'ko'    |
| **Sentences**    |
| Random sentence  | ✅ `sentence({words})`         | ❌ No           | HIGH                    | Capitalized, punctuated |
| Word count       | ✅ `sentence({words: 5})`      | ❌ No           | HIGH                    | Exact word count        |
| Default range    | ✅ 12-18 words                 | ❌ No           | MEDIUM                  | Variable by default     |
| **Paragraphs**   |
| Random paragraph | ✅ `paragraph({sentences})`    | ❌ No           | HIGH                    | Multiple sentences      |
| Sentence count   | ✅ `paragraph({sentences: 3})` | ❌ No           | MEDIUM                  | Exact sentence count    |
| Default range    | ✅ 3-7 sentences               | ❌ No           | MEDIUM                  | Variable by default     |

### 9. IDENTIFIERS & HASHES

| Feature          | Chance.js Support           | krandom Status | Implementation Priority | Notes              |
|------------------|-----------------------------|----------------|-------------------------|--------------------|
| **UUIDs/GUIDs**  |
| GUID v5          | ✅ `guid()` default          | ❌ No           | HIGH                    | Name-based         |
| GUID v4          | ✅ `guid({version: 4})`      | ❌ No           | HIGH                    | Random UUID        |
| Version control  | ✅ `guid({version: 4/5})`    | ❌ No           | HIGH                    | Flexible versions  |
| **Hashes**       |
| Hash string      | ✅ `hash({length, casing})`  | ✅ Yes          | ✓ DONE                  | `HexHashGenerator` |
| Default 40 chars | ✅ Git commit length         | ✅ Yes          | ✓ DONE                  | SHA-1 compatible   |
| Custom length    | ✅ `hash({length: 15})`      | ❌ No           | MEDIUM                  | Variable length    |
| Case control     | ✅ `hash({casing: 'upper'})` | ❌ No           | MEDIUM                  | Uppercase hex      |

### 10. MISCELLANEOUS GENERATORS

| Feature           | Chance.js Support            | krandom Status | Implementation Priority | Notes                   |
|-------------------|------------------------------|----------------|-------------------------|-------------------------|
| **Coin Flip**     |
| Coin flip         | ✅ `coin()`                   | ✅ Yes          | ✓ DONE                  | 'heads'/'tails' vs enum |
| **Dice & RPG**    |
| Dice notation     | ✅ `rpg('3d10')`              | ✅ Partial      | ✓ DONE                  | NdS pattern             |
| Dice array        | ✅ Returns `[1, 6, 9]`        | ✅ Yes          | ✓ DONE                  | Individual rolls        |
| Dice sum          | ✅ `rpg('3d10', {sum: true})` | ❌ No           | MEDIUM                  | Total of rolls          |
| Flexible notation | ✅ '5d6', '3d10', etc.        | ✅ Yes          | ✓ DONE                  | Standard RPG            |

### 11. HELPER METHODS (UNIQUE TO CHANCE.JS)

| Feature                | Chance.js Support                           | krandom Status | Implementation Priority | Notes                        |
|------------------------|---------------------------------------------|----------------|-------------------------|------------------------------|
| **Repeat Generation**  |
| n() method             | ✅ `n(fn, count, options)`                   | ❌ No           | HIGH                    | Call method N times - UNIQUE |
| Example                | ✅ `n(chance.integer, 5, {min: 1, max: 10})` | ❌ No           | HIGH                    | [4, 7, 1, 9, 3]              |
| **Unique Values**      |
| unique() method        | ✅ `unique(fn, count, options)`              | ❌ No           | HIGH                    | No duplicates - UNIQUE       |
| Example                | ✅ `unique(chance.state, 5)`                 | ❌ No           | HIGH                    | 5 distinct states            |
| RangeError             | ✅ Throws if pool too small                  | ❌ No           | HIGH                    | Smart validation             |
| Custom comparator      | ✅ `{comparator: (arr, val) => ...}`         | ❌ No           | MEDIUM                  | Object uniqueness            |
| **Collection Helpers** |
| pick()                 | ✅ `pick(['a','b','c'])`                     | ❌ No           | HIGH                    | Random element               |
| pickset()              | ✅ `pickset(['a','b','c'], 2)`               | ❌ No           | HIGH                    | Random N elements            |
| shuffle()              | ✅ `shuffle([1,2,3,4])`                      | ❌ No           | MEDIUM                  | Randomize array              |
| **Weighted Random**    |
| weighted()             | ✅ `weighted(values, weights)`               | ❌ No           | HIGH                    | Biased selection - UNIQUE    |
| Example                | ✅ `weighted(['heads','tails'], [7,3])`      | ❌ No           | HIGH                    | 70% heads                    |

---

## ADVANCED FEATURES

### Seeding & Reproducibility

| Feature              | Chance.js                         | krandom | Priority | Implementation Notes    |
|----------------------|-----------------------------------|---------|----------|-------------------------|
| **Seeding**          |
| Constructor seed     | ✅ `new Chance(42)`                | ✅ Yes   | ✓ DONE   | Most generators support |
| String seed          | ✅ `new Chance('my-seed')`         | ❌ No    | MEDIUM   | String-based seeding    |
| Custom RNG           | ✅ `new Chance(Math.random)`       | ❌ No    | LOW      | Function as seed        |
| Deterministic output | ✅ Same seed = same sequence       | ✅ Yes   | ✓ DONE   | Full reproducibility    |
| Re-seed              | ✅ `chance.seed(42)`               | ❌ No    | MEDIUM   | Reset to specific seed  |
| Unseeded mode        | ✅ `new Chance()` uses Math.random | ✅ Yes   | ✓ DONE   | Default behavior        |

### Options & Parameterization

| Feature                | Chance.js                        | krandom | Priority | Implementation Notes                                |
|------------------------|----------------------------------|---------|----------|-----------------------------------------------------|
| **Rich Options**       |
| Extensive parameters   | ✅ Every method has options       | ❌ No    | HIGH     | name({middle, prefix, suffix, gender, nationality}) |
| Default values         | ✅ Sensible defaults              | ✅ Yes   | ✓ DONE   | Works without options                               |
| Option combinations    | ✅ Multiple options work together | ❌ No    | MEDIUM   | Composable parameters                               |
| **Likelihood Control** |
| Boolean likelihood     | ✅ `bool({likelihood: 80})`       | ❌ No    | HIGH     | Probability-based - UNIQUE                          |
| **Format Control**     |
| String format          | ✅ `date({string: true})`         | ❌ No    | MEDIUM   | String vs object output                             |
| Number format          | ✅ `floating({fixed: 2})`         | ❌ No    | MEDIUM   | Decimal precision                                   |
| Formatted output       | ✅ `phone({formatted: false})`    | ❌ No    | MEDIUM   | With/without formatting                             |

### Mobile & Device Support

| Feature           | Chance.js                                | krandom | Priority | Implementation Notes    |
|-------------------|------------------------------------------|---------|----------|-------------------------|
| **Mobile Phones** |
| UK mobile         | ✅ `phone({country: 'uk', mobile: true})` | ❌ No    | MEDIUM   | '07624 321221' - UNIQUE |
| US mobile         | ✅ Standard format                        | ❌ No    | MEDIUM   | Mobile detection        |
| French mobile     | ✅ `phone({country: 'fr'})`               | ❌ No    | LOW      | International mobile    |
| **Multi-Country** |
| US support        | ✅ Default                                | ❌ No    | HIGH     | Primary market          |
| UK support        | ✅ Full support                           | ❌ No    | MEDIUM   | Phone, postcode         |
| French support    | ✅ Phone numbers                          | ❌ No    | LOW      | Basic coverage          |
| Italian support   | ✅ Names, states                          | ❌ No    | LOW      | Locale-specific         |

### Statistical Features

| Feature                | Chance.js                        | krandom | Priority | Implementation Notes          |
|------------------------|----------------------------------|---------|----------|-------------------------------|
| **Distributions**      |
| Normal distribution    | ✅ `normal({mean, dev})`          | ❌ No    | HIGH     | Box-Muller transform - UNIQUE |
| Mean control           | ✅ `mean` parameter               | ❌ No    | HIGH     | Distribution center           |
| Std deviation          | ✅ `dev` parameter                | ❌ No    | HIGH     | Distribution spread           |
| IQ-like data           | ✅ `normal({mean: 100, dev: 15})` | ❌ No    | MEDIUM   | Realistic distributions       |
| **Weighted Selection** |
| Weighted arrays        | ✅ `weighted(['a','b'], [7,3])`   | ❌ No    | HIGH     | Biased random - UNIQUE        |
| Integer weights        | ✅ Any positive integers          | ❌ No    | HIGH     | Flexible weighting            |

### Natural Language Features

| Feature                | Chance.js                | krandom | Priority | Implementation Notes          |
|------------------------|--------------------------|---------|----------|-------------------------------|
| **Pronounceable Text** |
| Syllable generation    | ✅ `syllable()`           | ❌ No    | MEDIUM   | Natural-sounding - UNIQUE     |
| Word generation        | ✅ `word({syllables: 4})` | ❌ No    | HIGH     | Syllable-based words - UNIQUE |
| Sentence structure     | ✅ Capital + period       | ❌ No    | HIGH     | Proper formatting             |
| Paragraph structure    | ✅ Multiple sentences     | ❌ No    | HIGH     | Natural paragraphs            |
| Variable length        | ✅ Default ranges         | ❌ No    | MEDIUM   | 12-18 words, 3-7 sentences    |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CORE GAPS (Must Have) - 10 days

1. **Email Generation** (1 day) - Essential for testing
    - `email({domain})` with custom domain support
2. **UUID/GUID Generation** (1 day) - Common identifier need
    - `guid({version: 4/5})` with version control
3. **Boolean Likelihood** (1 day) - Unique Chance.js feature
    - `bool({likelihood: 80})` for weighted booleans
4. **Natural Language** (3 days) - High-value feature
    - `word({syllables, length})` with syllable control
    - `sentence({words})` with capitalization
    - `paragraph({sentences})` for text blocks
5. **Helper Methods** (2 days) - Core utility
    - `n(fn, count, options)` for repeated generation
    - `unique(fn, count, options)` for distinct values
    - `pick()`, `pickset()`, `shuffle()` for collections
6. **Weighted Random** (1 day) - Unique differentiator
    - `weighted(values, weights)` for biased selection
7. **Normal Distribution** (1 day) - Statistical feature
    - `normal({mean, dev})` with Box-Muller transform

### Phase 2: LOCATION & WEB (Must Have) - 8 days

1. **Address Components** (2 days)
    - `address({short_suffix})` for street addresses
    - `city()` for city names
    - `state({full, territories, country})` with rich options
    - `zip({plusfour})`, `postal()`, `postcode()` for postal codes
2. **Country Support** (1 day)
    - `country({full})` for country codes/names
3. **Phone Numbers** (2 days)
    - `phone({formatted, country, mobile})` with multi-country
    - `areacode()` for US area codes
    - UK/US/FR support with mobile detection
4. **Coordinates** (1 day)
    - `latitude({fixed, min, max})`, `longitude()`, `coordinates()`
    - `altitude()`, `depth()`, `geohash()` for advanced geo
5. **URL/Domain** (2 days)
    - `domain({tld})`, `tld()`, `url({protocol, domain, path, extensions})`
    - Rich URL parameterization

### Phase 3: FINANCE & TEXT (Should Have) - 7 days

1. **Credit Cards** (2 days)
    - `cc({type})` with Visa/MC/Amex support
    - `cc_type()` for card metadata
    - `exp({future})`, `exp_month()`, `exp_year()` for expiration
2. **Currency** (1 day)
    - `currency()`, `currency_pair()` for FX simulation
    - `dollar({max})`, `euro({max})` for formatted amounts
3. **Enhanced Names** (2 days)
    - `name({middle, middle_initial, prefix, suffix, gender, nationality})`
    - `prefix({gender})`, `suffix()` for title components
    - Gender-specific and nationality support
4. **Date Components** (2 days)
    - `date({string, american, year, month, day})` with rich options
    - `year({min, max})`, `month({raw})`, `hour({twentyfour})`
    - `timestamp()` for Unix time

### Phase 4: ENHANCEMENTS (Nice to Have) - 5 days

1. **Character Generators** (1 day)
    - `character({pool, alpha, numeric, symbols, casing})`
2. **String Enhancements** (1 day)
    - Custom pool support for `string({pool: 'abc'})`
3. **Color Generators** (1 day)
    - `color({format, grayscale, casing})` with multiple formats
4. **Business Data** (1 day)
    - `company()`, `profession({ranked})` with ranking
5. **Advanced Geo** (1 day)
    - Range-restricted coordinates, geohash support

---

## KEY DIFFERENTIATORS

### Chance.js Unique Strengths (vs krandom)

1. **Weighted Random** - `weighted(['a','b'], [7,3])` for biased selection (NO EQUIVALENT)
2. **Normal Distribution** - Box-Muller transform for realistic statistical data (NO EQUIVALENT)
3. **Likelihood Control** - `bool({likelihood: 80})` for probability-based booleans (NO EQUIVALENT)
4. **Helper Methods** - `n()`, `unique()`, `pick()`, `pickset()`, `shuffle()` (NO EQUIVALENT)
5. **Syllable-Based Words** - `word({syllables: 4})` for natural-looking text (NO EQUIVALENT)
6. **Rich Options** - Extensive parameterization on every method (PARTIAL)
7. **Mobile Detection** - `phone({mobile: true})` for mobile-specific formats (NO EQUIVALENT)
8. **Currency Pairs** - `currency_pair()` for FX simulation (NO EQUIVALENT)
9. **Ranked Professions** - `profession({ranked: true})` for biased selection (NO EQUIVALENT)
10. **Exclude Arrays** - `natural({exclude: [1,2,3]})` to skip specific values (NO EQUIVALENT)
11. **Natural Language** - Sentence/paragraph with proper capitalization and punctuation (NO EQUIVALENT)
12. **Format Flexibility** - `date({string: true})`, `ssn({dashes: false})` for output control (PARTIAL)

### krandom Unique Strengths (vs Chance.js)

1. **Kotlin-First** - Type-safe, idiomatic Kotlin API
2. **ObjectGenerator** - Generate complex object graphs (Chance.js is manual only)
3. **Fibonacci** - Dedicated Fibonacci number generator
4. **Better Test Coverage** - 99%+ coverage
5. **Cleaner Architecture** - More maintainable codebase
6. **JVM Interop** - Works with Java/Scala/Kotlin

---

## EFFORT ESTIMATES

### Phase 1: Core Gaps - 10 days

- Email generation: **1 day**
- UUID/GUID with versions: **1 day**
- Boolean likelihood: **1 day**
- Natural language (word/sentence/paragraph): **3 days**
- Helper methods (n/unique/pick/pickset/shuffle): **2 days**
- Weighted random: **1 day**
- Normal distribution: **1 day**

### Phase 2: Location & Web - 8 days

- Address components: **2 days**
- Country support: **1 day**
- Phone numbers (multi-country, mobile): **2 days**
- Coordinates (lat/lon/geo): **1 day**
- URL/domain with rich options: **2 days**

### Phase 3: Finance & Text - 7 days

- Credit cards (types, expiration): **2 days**
- Currency (objects, pairs, formatted): **1 day**
- Enhanced names (options): **2 days**
- Date components: **2 days**

### Phase 4: Enhancements - 5 days

- Character generators: **1 day**
- String enhancements: **1 day**
- Color generators: **1 day**
- Business data: **1 day**
- Advanced geo: **1 day**

### **TOTAL: ~30 days** (6 weeks)

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

- ✅ **Easy**: Basic generators (numbers, booleans, strings) - already done
- ✅ **Moderate**: Email, UUID, addresses, phones, dates
- ⚠️ **Moderate-Hard**: Normal distribution (need Box-Muller), syllable generation
- ⚠️ **Hard**: Helper methods (n, unique) need reflection or lambda support
- ⚠️ **Complex**: Weighted random needs algorithm implementation

### Kotlin-Specific Opportunities

1. **Extension Functions** - `List.pick()`, `List.pickset()`, `List.shuffle()`
2. **Inline Reified** - Type-safe `unique<T>()` with generics
3. **Sealed Classes** - Better type safety for card types, currencies
4. **Data Classes** - Clean currency/card type objects
5. **DSL Builders** - Fluent API for complex generators
6. **Coroutines** - Async generation for large datasets
7. **Operator Overloading** - Natural syntax for weighted selection

### Recommended Approach

1. **Port core concepts** - Weighted random, normal distribution, helpers
2. **Enhance with Kotlin** - Extension functions, sealed classes, DSLs
3. **Keep simplicity** - Don't over-engineer
4. **Focus on API** - Great developer experience
5. **Add type safety** - Leverage Kotlin's type system
6. **Skip legacy** - No need for JavaScript compatibility
7. **Document well** - Examples for all features

---

## CONCLUSION

Chance.js offers **unique features** that krandom lacks, particularly in:

### Top Priority Focus Areas

1. **Weighted Random & Normal Distribution** - Statistical capabilities
2. **Helper Methods** - `n()`, `unique()`, collection operations
3. **Natural Language Generation** - Syllable-based words, sentences, paragraphs
4. **Rich Options Parameterization** - Extensive parameter support on all methods
5. **Email & UUID** - Essential missing generators
6. **Location Data** - Address, city, state, phone, coordinates
7. **Finance Enhancement** - Credit cards, currency pairs, formatted amounts

### Skip/Low Priority

1. **Multi-nationality** - Low ROI, complex to maintain
2. **Italian/French** - Focus on English first
3. **RPG dice sum** - Minor enhancement to existing Dice
4. **Social/Entertainment** - Twitter handles, avatars (low value)

### Strategic Recommendation

**Implement 60% of Chance.js features** focusing on:

- Statistical features (weighted, normal distribution) - HIGH VALUE
- Helper methods (n, unique, pick) - HIGH LEVERAGE
- Natural language (word, sentence, paragraph) - HIGH DEMAND
- Core data (email, UUID, addresses, phones) - HIGH USAGE
- Rich parameterization - BETTER UX

**Maintain krandom advantages**:

- Kotlin-first design with type safety
- ObjectGenerator for complex graphs
- Clean architecture and test coverage
- JVM ecosystem integration

**Target outcome**: krandom becomes the **most developer-friendly** random data generator for JVM with **unique statistical capabilities** and **natural language generation** not found in other JVM
libraries.
