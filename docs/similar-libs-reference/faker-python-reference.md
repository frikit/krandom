# Python `faker` Library — Comprehensive Reference

> Repository: https://github.com/joke2k/faker
> Docs: https://faker.readthedocs.io/en/master/
> Version: faker 26.x (Python 3.8+)
> Purpose: reference for deciding which krandom features to implement next.

---

## 1. Purpose and Overview

**faker** (`joke2k/faker`) is a Python library that generates fake but realistic-looking data for tests, database seeding, anonymisation, and demonstrations. Originally a port of the PHP `Faker`
library.

Key characteristics:

- Single entry point: one `Faker` instance exposes every provider via attribute access.
- Provider architecture: every data category is a separate `BaseProvider` subclass.
- Locale-aware: most providers have locale-specific subclasses (e.g. `faker.providers.person.de_DE`).
- Seeding: reproducible output via `Faker.seed(n)` (class-level) or `faker.seed_instance(n)` (instance-level).
- Uniqueness: the `faker.unique` proxy raises `UniquenessException` after too many failed attempts.
- Extensible: arbitrary provider classes can be added via `fake.add_provider(MyProvider)`.

---

## 2. Installation

```bash
pip install faker
pip install faker==26.0.0   # pinned
```

No runtime dependencies beyond the Python standard library.

---

## 3. Core API

### 3.1 `Faker()` Constructor

```python
from faker import Faker

fake = Faker()                                    # default en_US
fake = Faker('de_DE')                             # single locale
fake = Faker(['en_US', 'de_DE', 'ja_JP'])         # multi-locale (picks at random)
fake = Faker(['en_US', 'de_DE'], weights=[0.7, 0.3])  # weighted
```

| Parameter       | Default   | Description                                    |
|-----------------|-----------|------------------------------------------------|
| `locale`        | `'en_US'` | BCP-47 locale string or list                   |
| `providers`     | `None`    | Override the full provider list                |
| `generator`     | `None`    | Custom `random.Random` (enables determinism)   |
| `weights`       | `None`    | Per-locale probabilities when a list is given  |
| `use_weighting` | `True`    | Whether locale-level value weights are applied |

### 3.2 Seeding

```python
Faker.seed(12345)           # class-level — affects ALL instances
fake.seed_instance(0)       # instance-level — only this instance
Faker.seed()                # reset to non-deterministic
fake.seed_instance()        # reset to non-deterministic
```

### 3.3 `unique` Proxy

```python
fake = Faker()
Faker.seed(0)
for _ in range(100):
    print(fake.unique.first_name())   # never repeats within this instance

fake.unique.clear()                   # reset the seen set
```

Raises `UniquenessException` after 1000 failed attempts (configurable).

---

## 4. All Providers, Grouped by Category

### 4.1 `address`

| Method                                   | Return | Notes                          |
|------------------------------------------|--------|--------------------------------|
| `address()`                              | `str`  | Full multi-line address        |
| `building_number()`                      | `str`  | e.g. `"42"`                    |
| `city()`                                 | `str`  | e.g. `"Springfield"`           |
| `city_prefix()`                          | `str`  | e.g. `"Lake"`                  |
| `city_suffix()`                          | `str`  | e.g. `"ville"`                 |
| `country()`                              | `str`  | e.g. `"Germany"`               |
| `country_code(representation='alpha-2')` | `str`  | ISO 3166-1 alpha-2 or alpha-3  |
| `current_country()`                      | `str`  | Country for the active locale  |
| `current_country_code()`                 | `str`  | ISO code for the active locale |
| `postcode()`                             | `str`  | Locale-aware postal code       |
| `secondary_address()`                    | `str`  | e.g. `"Apt. 12"`               |
| `state()`                                | `str`  | Full state/province name       |
| `state_abbr()`                           | `str`  | 2-letter abbreviation (en_US)  |
| `street_address()`                       | `str`  | Number + street name           |
| `street_name()`                          | `str`  | e.g. `"Main Street"`           |
| `street_suffix()`                        | `str`  | e.g. `"Street"`                |
| `zipcode()`                              | `str`  | Alias for `postcode()`         |

### 4.2 `automotive`

| Method            | Return | Notes                                      |
|-------------------|--------|--------------------------------------------|
| `license_plate()` | `str`  | Locale-aware plate number                  |
| `vin()`           | `str`  | 17-character Vehicle Identification Number |

### 4.3 `bank`

| Method           | Return | Notes                       |
|------------------|--------|-----------------------------|
| `aba()`          | `str`  | 9-digit ABA routing number  |
| `bank_country()` | `str`  | ISO 3166-1 alpha-2          |
| `bban()`         | `str`  | Basic Bank Account Number   |
| `iban()`         | `str`  | Full IBAN with check digits |
| `swift()`        | `str`  | SWIFT/BIC (8 or 11 chars)   |
| `swift8()`       | `str`  | Always 8-character SWIFT    |
| `swift11()`      | `str`  | Always 11-character SWIFT   |

```python
fake = Faker()
print(fake.iban())   # "GB29 NWBK 6016 1331 9268 19"
print(fake.swift())  # "HBOSDE3BXXX"
```

### 4.4 `barcode`

| Method                     | Return | Notes                            |
|----------------------------|--------|----------------------------------|
| `ean(length=13)`           | `str`  | EAN-8 or EAN-13 with check digit |
| `ean8()`                   | `str`  | 8-digit EAN                      |
| `ean13(leading_zero=None)` | `str`  | 13-digit EAN                     |
| `localized_ean(length=13)` | `str`  | EAN with locale country prefix   |
| `localized_ean8()`         | `str`  | 8-digit localised EAN            |
| `localized_ean13()`        | `str`  | 13-digit localised EAN           |

### 4.5 `color`

| Method              | Return  | Notes                           |
|---------------------|---------|---------------------------------|
| `color_name()`      | `str`   | Human name, e.g. `"DodgerBlue"` |
| `hex_color()`       | `str`   | `"#a1b2c3"`                     |
| `rgb_color()`       | `str`   | `"123,45,67"`                   |
| `rgb_css_color()`   | `str`   | `"rgb(123, 45, 67)"`            |
| `safe_color_name()` | `str`   | CSS named color, e.g. `"blue"`  |
| `safe_hex_color()`  | `str`   | Short hex, e.g. `"#ff0"`        |
| `hsl_color()`       | `str`   | `"hsl(120, 50%, 50%)"`          |
| `color_rgb()`       | `tuple` | `(R, G, B)` int tuple           |
| `color_rgb_float()` | `tuple` | `(r, g, b)` float 0..1          |
| `color_hsl()`       | `tuple` | `(H, S%, L%)` tuple             |

### 4.6 `company`

| Method             | Return | Notes                          |
|--------------------|--------|--------------------------------|
| `bs()`             | `str`  | Business-speak buzzword phrase |
| `catch_phrase()`   | `str`  | Corporate slogan               |
| `company()`        | `str`  | Company name                   |
| `company_suffix()` | `str`  | e.g. `"LLC"`, `"GmbH"`         |

### 4.7 `credit_card`

| Method                                                             | Return | Notes                   |
|--------------------------------------------------------------------|--------|-------------------------|
| `credit_card_expire(start='now', end='+10y', date_format='%m/%y')` | `str`  | Expiry date             |
| `credit_card_full(card_type=None)`                                 | `str`  | All fields in one block |
| `credit_card_number(card_type=None)`                               | `str`  | Luhn-valid PAN          |
| `credit_card_provider(card_type=None)`                             | `str`  | e.g. `"Visa"`           |
| `credit_card_security_code(card_type=None)`                        | `str`  | CVV/CVC                 |

Supported `card_type`: `'amex'`, `'diners'`, `'discover'`, `'jcb'`, `'mastercard'`, `'visa'`, `'visa13'`, `'visa16'`, `'visa19'`.

### 4.8 `currency`

| Method                       | Return | Notes                                      |
|------------------------------|--------|--------------------------------------------|
| `currency()`                 | `dict` | `{'code': 'EUR', 'name': 'Euro'}`          |
| `currency_code()`            | `str`  | ISO 4217 code, e.g. `"EUR"`                |
| `currency_name()`            | `str`  | e.g. `"Euro"`                              |
| `currency_symbol(code=None)` | `str`  | e.g. `"€"`                                 |
| `pricetag()`                 | `str`  | Locale-formatted price, e.g. `"$9,999.00"` |

### 4.9 `date_time`

| Method                                                              | Return      | Notes                                   |
|---------------------------------------------------------------------|-------------|-----------------------------------------|
| `am_pm()`                                                           | `str`       | `"AM"` or `"PM"`                        |
| `century()`                                                         | `str`       | e.g. `"XX"`                             |
| `date(pattern='%Y-%m-%d', end_datetime=None)`                       | `str`       | Formatted date string                   |
| `date_between(start_date='-30y', end_date='today')`                 | `date`      | Python `date` object                    |
| `date_of_birth(tzinfo=None, minimum_age=0, maximum_age=115)`        | `date`      | Age-bounded DOB                         |
| `date_time(tzinfo=None, end_datetime=None)`                         | `datetime`  | Python `datetime`                       |
| `date_time_between(start_date='-30y', end_date='now', tzinfo=None)` | `datetime`  |                                         |
| `day_of_month()`                                                    | `str`       | `"01"`–`"31"`                           |
| `day_of_week()`                                                     | `str`       | e.g. `"Monday"`                         |
| `future_date(end_date='+30d', tzinfo=None)`                         | `date`      |                                         |
| `future_datetime(end_date='+30d', tzinfo=None)`                     | `datetime`  |                                         |
| `iso8601(tzinfo=None, end_datetime=None, sep='T')`                  | `str`       | ISO 8601 timestamp                      |
| `month()`                                                           | `str`       | `"01"`–`"12"`                           |
| `month_name()`                                                      | `str`       | e.g. `"January"`                        |
| `past_date(start_date='-30d', tzinfo=None)`                         | `date`      |                                         |
| `past_datetime(start_date='-30d', tzinfo=None)`                     | `datetime`  |                                         |
| `time(pattern='%H:%M:%S', end_datetime=None)`                       | `str`       |                                         |
| `time_delta(end_datetime=None)`                                     | `timedelta` |                                         |
| `time_series(start_date='-30d', end_date='now', ...)`               | `generator` | Lazy time-series generator              |
| `timezone()`                                                        | `str`       | IANA tz name, e.g. `"America/New_York"` |
| `unix_time(end_datetime=None, start_datetime=None)`                 | `float`     | POSIX timestamp                         |
| `year()`                                                            | `str`       | `"1970"`–current year                   |

**Date offset shorthand:**

| Shorthand           | Meaning           |
|---------------------|-------------------|
| `'now'` / `'today'` | Current moment    |
| `'+10d'`            | 10 days in future |
| `'-2y'`             | 2 years in past   |
| `'+3h'`             | 3 hours ahead     |
| `'-30m'`            | 30 minutes ago    |

```python
fake = Faker()
print(fake.date_of_birth(minimum_age=18, maximum_age=65))
# datetime.date(1987, 4, 23)
print(fake.iso8601())
# "2024-11-30T08:15:42.123456"
```

### 4.10 `file`

| Method                                                             | Return | Notes                    |
|--------------------------------------------------------------------|--------|--------------------------|
| `file_extension(category=None)`                                    | `str`  | e.g. `"pdf"`             |
| `file_name(category=None, extension=None)`                         | `str`  | e.g. `"report.pdf"`      |
| `file_path(depth=1, category=None, extension=None, absolute=True)` | `str`  | Full path                |
| `mime_type(category=None)`                                         | `str`  | e.g. `"application/pdf"` |
| `unix_device(prefix=None)`                                         | `str`  | e.g. `"/dev/sda"`        |
| `unix_partition(prefix=None)`                                      | `str`  | e.g. `"/dev/sda1"`       |

`category` values: `'audio'`, `'image'`, `'office'`, `'text'`, `'video'`.

### 4.11 `geo`

| Method                                               | Return                    | Notes                      |
|------------------------------------------------------|---------------------------|----------------------------|
| `coordinate(center=None, radius=0.001)`              | `Decimal`                 | Single coordinate value    |
| `latitude()`                                         | `Decimal`                 | `-90.0` to `90.0`          |
| `latlng()`                                           | `tuple[Decimal, Decimal]` | `(lat, lng)`               |
| `local_latlng(country_code='US', coords_only=False)` | `tuple`                   | From real populated places |
| `location_on_land(coords_only=False)`                | `tuple`                   | Curated land-only dataset  |
| `longitude()`                                        | `Decimal`                 | `-180.0` to `180.0`        |

### 4.12 `internet`

| Method                                                          | Return | Notes                              |
|-----------------------------------------------------------------|--------|------------------------------------|
| `company_email()`                                               | `str`  | Uses a real-looking company domain |
| `domain_name(levels=1)`                                         | `str`  | e.g. `"example.com"`               |
| `email(domain=None, safe=True)`                                 | `str`  | Generic email address              |
| `free_email()`                                                  | `str`  | `@gmail.com`, `@yahoo.com`, etc.   |
| `free_email_domain()`                                           | `str`  | e.g. `"gmail.com"`                 |
| `hostname(levels=1)`                                            | `str`  | e.g. `"mail.example.com"`          |
| `http_method()`                                                 | `str`  | `"GET"`, `"POST"`, etc.            |
| `http_status_code(...)`                                         | `int`  | e.g. `200`, `404`                  |
| `ipv4(network=False, address_class=None, private=None)`         | `str`  | e.g. `"192.168.1.1"`               |
| `ipv4_private(network=False, address_class=None)`               | `str`  | RFC 1918 private address           |
| `ipv4_public(network=False, address_class=None)`                | `str`  | Public (routable) address          |
| `ipv6(network=False)`                                           | `str`  | Full IPv6 address                  |
| `mac_address(multicast=False)`                                  | `str`  | e.g. `"00:1A:2B:3C:4D:5E"`         |
| `port_number(is_system=False, is_user=False, is_dynamic=False)` | `int`  | TCP/UDP port                       |
| `safe_email()`                                                  | `str`  | Always `@example.{com,org,net}`    |
| `slug(value=None)`                                              | `str`  | URL slug, e.g. `"my-blog-post"`    |
| `tld()`                                                         | `str`  | e.g. `"com"`                       |
| `uri()`                                                         | `str`  | Full URI with path                 |
| `url(schemes=None)`                                             | `str`  | Full URL                           |
| `user_name()`                                                   | `str`  | Login-safe username                |

### 4.13 `isbn`

| Method                  | Return | Notes                          |
|-------------------------|--------|--------------------------------|
| `isbn10(separator='-')` | `str`  | 10-digit ISBN with check digit |
| `isbn13(separator='-')` | `str`  | 13-digit ISBN with check digit |

### 4.14 `job`

| Method  | Return | Notes                      |
|---------|--------|----------------------------|
| `job()` | `str`  | e.g. `"Software Engineer"` |

### 4.15 `lorem`

| Method                                                                      | Return      | Notes                  |
|-----------------------------------------------------------------------------|-------------|------------------------|
| `paragraph(nb_sentences=3, variable_nb_sentences=True, ext_word_list=None)` | `str`       | A block of text        |
| `paragraphs(nb=3, ext_word_list=None)`                                      | `list[str]` | Multiple paragraphs    |
| `sentence(nb_words=6, variable_nb_words=True, ext_word_list=None)`          | `str`       | One sentence           |
| `sentences(nb=3, ext_word_list=None)`                                       | `list[str]` | Multiple sentences     |
| `text(max_nb_chars=200, ext_word_list=None)`                                | `str`       | Block up to char limit |
| `texts(nb_texts=3, max_nb_chars=200, ext_word_list=None)`                   | `list[str]` |                        |
| `word(ext_word_list=None)`                                                  | `str`       | Single word            |
| `words(nb=6, ext_word_list=None, unique=False)`                             | `list[str]` | List of words          |

### 4.16 `misc`

| Method                                                                                   | Return           | Notes                      |
|------------------------------------------------------------------------------------------|------------------|----------------------------|
| `binary(length=1048576)`                                                                 | `bytes`          | Random bytes               |
| `boolean(chance_of_getting_true=50)`                                                     | `bool`           | Weighted boolean           |
| `csv(header=None, data_columns=..., num_rows=10, ...)`                                   | `str`            | CSV-formatted string       |
| `json(data_columns=None, num_rows=10, indent=None)`                                      | `str`            | JSON string                |
| `md5(raw_output=False)`                                                                  | `str\|bytes`     | MD5 hex digest             |
| `null_boolean()`                                                                         | `bool\|None`     | `True`, `False`, or `None` |
| `password(length=10, special_chars=True, digits=True, upper_case=True, lower_case=True)` | `str`            | Random password            |
| `sha1(raw_output=False)`                                                                 | `str\|bytes`     | SHA-1 hex digest           |
| `sha256(raw_output=False)`                                                               | `str\|bytes`     | SHA-256 hex digest         |
| `uuid4(cast_to=str)`                                                                     | `str\|uuid.UUID` | UUID version 4             |

### 4.17 `passport`

| Method                                       | Return | Notes                       |
|----------------------------------------------|--------|-----------------------------|
| `passport_full()`                            | `dict` | Complete passport data dict |
| `passport_gender(gender=None)`               | `str`  | `"M"`, `"F"`, or `"X"`      |
| `passport_mrz(birthday=None)`                | `str`  | Machine-readable zone line  |
| `passport_number()`                          | `str`  | e.g. `"A1234567"`           |
| `passport_owner(gender=None, birthday=None)` | `dict` | Owner fields                |

### 4.18 `person`

| Method                   | Return | Notes                             |
|--------------------------|--------|-----------------------------------|
| `first_name()`           | `str`  | Any gender                        |
| `first_name_female()`    | `str`  | Female first name                 |
| `first_name_male()`      | `str`  | Male first name                   |
| `first_name_nonbinary()` | `str`  | Gender-neutral first name         |
| `last_name()`            | `str`  | Any gender surname                |
| `last_name_female()`     | `str`  |                                   |
| `last_name_male()`       | `str`  |                                   |
| `last_name_nonbinary()`  | `str`  |                                   |
| `name()`                 | `str`  | Full name (prefix + first + last) |
| `name_female()`          | `str`  |                                   |
| `name_male()`            | `str`  |                                   |
| `prefix()`               | `str`  | e.g. `"Dr."`, `"Mrs."`            |
| `suffix()`               | `str`  | e.g. `"Jr."`, `"PhD"`             |

### 4.19 `phone_number`

| Method                   | Return | Notes                         |
|--------------------------|--------|-------------------------------|
| `country_calling_code()` | `str`  | e.g. `"+1"`                   |
| `msisdn()`               | `str`  | 14-digit MSISDN               |
| `phone_number()`         | `str`  | Locale-formatted phone number |

### 4.20 `profile`

| Method                           | Return | Notes                  |
|----------------------------------|--------|------------------------|
| `profile(fields=None, sex=None)` | `dict` | Full user profile dict |
| `simple_profile(sex=None)`       | `dict` | Minimal profile        |

Full `profile()` includes: `job`, `company`, `ssn`, `residence`, `current_location`, `blood_group`, `website`, `username`, `name`, `sex`, `address`, `mail`, `birthdate`.

### 4.21 `python`

| Method                                                      | Return    | Notes                 |
|-------------------------------------------------------------|-----------|-----------------------|
| `pybool(truth_probability=50)`                              | `bool`    | Weighted boolean      |
| `pydecimal(left_digits=None, right_digits=None, ...)`       | `Decimal` |                       |
| `pydict(nb_elements=10, ...)`                               | `dict`    |                       |
| `pyfloat(left_digits=None, right_digits=None, ...)`         | `float`   |                       |
| `pyint(min_value=0, max_value=9999, step=1)`                | `int`     |                       |
| `pylist(nb_elements=10, ...)`                               | `list`    |                       |
| `pystr(min_chars=None, max_chars=20, prefix='', suffix='')` | `str`     |                       |
| `pystr_format(string_format='?#-###...')`                   | `str`     | Template-based string |
| `pytuple(nb_elements=10, ...)`                              | `tuple`   |                       |

### 4.22 `ssn`

| Method          | Return | Notes                                     |
|-----------------|--------|-------------------------------------------|
| `ssn()`         | `str`  | US SSN or TIN                             |
| `ein()`         | `str`  | Employer Identification Number            |
| `invalid_ssn()` | `str`  | Intentionally invalid SSN                 |
| `itin()`        | `str`  | Individual Taxpayer Identification Number |

### 4.23 `user_agent`

| Method         | Return | Notes                    |
|----------------|--------|--------------------------|
| `android()`    | `str`  | Android UA string        |
| `chrome(...)`  | `str`  | Chrome UA                |
| `firefox()`    | `str`  | Firefox UA               |
| `ios()`        | `str`  | iOS UA string            |
| `opera()`      | `str`  | Opera UA                 |
| `safari()`     | `str`  | Safari UA                |
| `user_agent()` | `str`  | Any browser UA at random |

---

## 5. Locale Support

~80 locales supported. Notable ones:

| Locale  | Language / Region                     |
|---------|---------------------------------------|
| `en_US` | English (United States) — **default** |
| `en_GB` | English (UK)                          |
| `de_DE` | German                                |
| `fr_FR` | French (France)                       |
| `es_ES` | Spanish (Spain)                       |
| `es_MX` | Spanish (Mexico)                      |
| `pt_BR` | Portuguese (Brazil)                   |
| `zh_CN` | Chinese (Simplified)                  |
| `zh_TW` | Chinese (Traditional)                 |
| `ja_JP` | Japanese                              |
| `ko_KR` | Korean                                |
| `ru_RU` | Russian                               |
| `ar_SA` | Arabic (Saudi Arabia)                 |
| `hi_IN` | Hindi (India)                         |
| `it_IT` | Italian                               |
| `pl_PL` | Polish                                |
| `tr_TR` | Turkish                               |
| `uk_UA` | Ukrainian                             |
| `sv_SE` | Swedish                               |
| `nl_NL` | Dutch                                 |

Full list of ~80 locales covers Europe, Asia, Middle East, Latin America, Africa, Oceania.

---

## 6. Custom Providers

```python
from faker.providers import BaseProvider
from faker import Faker

class ColourProvider(BaseProvider):
    COLOURS = ['Crimson', 'Cerulean', 'Burnt Sienna']

    def paint_colour(self) -> str:
        return self.random_element(self.COLOURS)

fake = Faker()
fake.add_provider(ColourProvider)
print(fake.paint_colour())   # "Cerulean"
```

**`BaseProvider` utility methods:**

| Method                                                 | Description                |
|--------------------------------------------------------|----------------------------|
| `random_element(elements)`                             | Pick one item uniformly    |
| `random_elements(elements, length=None, unique=False)` | Pick multiple items        |
| `random_int(min=0, max=9999, step=1)`                  | Random integer             |
| `numerify(text='###')`                                 | Replace `#` with digits    |
| `lexify(text='????')`                                  | Replace `?` with letters   |
| `bothify(text='## ??')`                                | Replace both `#` and `?`   |
| `hexify(text='^^^^')`                                  | Replace `^` with hex chars |

---

## 7. faker vs Mimesis Comparison

| Dimension           | faker                                        | Mimesis                            |
|---------------------|----------------------------------------------|------------------------------------|
| Architecture        | Provider proxy `Faker` instance              | `Schema` DSL + `Field` + `Generic` |
| Performance         | Moderate (dynamic proxy)                     | Faster (direct calls)              |
| Unique values       | `fake.unique.<method>()` proxy               | `Schema(iterators=True)`           |
| Seeding             | `Faker.seed(n)` / `fake.seed_instance(n)`    | `random.seed(n)` on stdlib         |
| Custom providers    | `fake.add_provider(MyProvider)`              | Subclass `BaseDataProvider`        |
| Structured output   | `profile()`, `json()`, `csv()`               | `Schema` with nested `Field`       |
| Type coverage       | Broader (passport, barcode, automotive, UA…) | More perf-optimised core set       |
| Maturity / adoption | Very high (27k+ stars, 10+ years)            | High (4k+ stars)                   |
| Locale count        | ~80                                          | ~40                                |

---

## 8. Mapping to krandom

| faker provider / method                                                | krandom equivalent                                        | Status                        |
|------------------------------------------------------------------------|-----------------------------------------------------------|-------------------------------|
| **person**                                                             |                                                           |                               |
| `first_name()`                                                         | `user/FirstName.kt`                                       | Implemented                   |
| `last_name()`                                                          | `user/SurName.kt`                                         | Implemented                   |
| `name()` (full)                                                        | No facade combining prefix + first + last                 | Gap                           |
| `prefix()` / `suffix()`                                                | `user/Title.kt` — `Title<TitleResult>` (no name suffixes) | Partial                       |
| `first_name_female()` / `first_name_male()`                            | None                                                      | Gap                           |
| **ssn**                                                                |                                                           |                               |
| `ssn()`                                                                | `user/SocialSecurityNumber.kt`                            | Implemented                   |
| `ein()`                                                                | None                                                      | Gap                           |
| **internet**                                                           |                                                           |                               |
| `email()`                                                              | `user/Email.kt`                                           | Implemented                   |
| `ipv4()`                                                               | `generator/network/IPv4Generator.java`                    | Implemented                   |
| `ipv6()`                                                               | `generator/network/IPv6Generator.java`                    | Implemented                   |
| `user_name()`                                                          | `user/Username.kt`                                        | Implemented                   |
| `url()`                                                                | None                                                      | Gap                           |
| `mac_address()`                                                        | None                                                      | Gap                           |
| `slug()`                                                               | None                                                      | Gap                           |
| `http_status_code()`                                                   | None                                                      | Gap                           |
| `port_number()`                                                        | None                                                      | Gap                           |
| **date_time**                                                          |                                                           |                               |
| `date_of_birth()`                                                      | `user/BirthDay.kt`                                        | Partial (no age-based bounds) |
| `date_time()` / `iso8601()`                                            | None                                                      | Gap                           |
| `unix_time()`                                                          | None                                                      | Gap                           |
| `timezone()`                                                           | None                                                      | Gap                           |
| **misc**                                                               |                                                           |                               |
| `uuid4()`                                                              | `HexHashGenerator(32)` (not a proper UUID)                | Partial                       |
| `md5()`                                                                | `HexHashGenerator(32)`                                    | Implemented                   |
| `sha1()` / `sha256()`                                                  | None                                                      | Gap                           |
| `password()`                                                           | None                                                      | Gap                           |
| `boolean()`                                                            | `BooleanGenerator.java`                                   | Implemented (unweighted)      |
| **python**                                                             |                                                           |                               |
| `pyint()`                                                              | `IntGenerator.java`                                       | Implemented                   |
| `pyfloat()`                                                            | `FloatGenerator.java`                                     | Implemented                   |
| `pystr()`                                                              | `StringGenerator.java`                                    | Implemented                   |
| `pybool()`                                                             | `BooleanGenerator.java`                                   | Implemented                   |
| **bank**                                                               |                                                           |                               |
| `iban()`                                                               | None                                                      | Gap                           |
| `swift()`                                                              | None                                                      | Gap                           |
| **credit_card**                                                        |                                                           |                               |
| `credit_card_number()`                                                 | `LuhnGenerator.java` (10 digits, no BIN)                  | Partial                       |
| `credit_card_expire()` / `credit_card_security_code()`                 | None                                                      | Gap                           |
| **address**                                                            |                                                           |                               |
| `city()` / `country()` / `postcode()` / `street_address()` / `state()` | None                                                      | Gap                           |
| **color**                                                              |                                                           |                               |
| `hex_color()` / `rgb_color()` / `color_name()`                         | None                                                      | Gap                           |
| **company**                                                            |                                                           |                               |
| `company()` / `catch_phrase()`                                         | None                                                      | Gap                           |
| **lorem**                                                              |                                                           |                               |
| `word()` / `sentence()` / `paragraph()`                                | None                                                      | Gap                           |
| **geo**                                                                |                                                           |                               |
| `latitude()` / `longitude()` / `latlng()`                              | None                                                      | Gap                           |
| **automotive**                                                         |                                                           |                               |
| `vin()` / `license_plate()`                                            | None                                                      | Gap                           |
| **barcode**                                                            |                                                           |                               |
| `ean13()` / `isbn10()` / `isbn13()`                                    | None                                                      | Gap                           |
| **currency**                                                           |                                                           |                               |
| `currency_code()` / `pricetag()`                                       | None                                                      | Gap                           |
| **file**                                                               |                                                           |                               |
| `file_name()` / `mime_type()` / `file_path()`                          | None                                                      | Gap                           |
| **job**                                                                |                                                           |                               |
| `job()`                                                                | None                                                      | Gap                           |
| **phone_number**                                                       |                                                           |                               |
| `phone_number()`                                                       | None                                                      | Gap                           |
| **user_agent**                                                         |                                                           |                               |
| `user_agent()` / `chrome()` / `firefox()`                              | None                                                      | Gap                           |
| **krandom-unique (no faker equivalent)**                               |                                                           |                               |
| Natural / prime / composite numbers                                    | `NaturalNumberGenerator.kt`                               | krandom-unique                |
| Fibonacci                                                              | `FibonacciGenerator.java`                                 | krandom-unique                |
| Dice (D4–D20)                                                          | `DiceGenerator.java`                                      | krandom-unique                |
| Coin flip                                                              | `CoinGenerator.java`                                      | krandom-unique                |
| Object-graph population                                                | `ObjectGenerator.java`                                    | krandom-unique                |
| Luhn algorithm                                                         | `LuhnGenerator.java`                                      | krandom-unique                |

---

## 9. Recommended Next Implementations for krandom

### Tier 1 — High value, simple (data-file backed)

| Feature                           | faker method                                   | Notes                    |
|-----------------------------------|------------------------------------------------|--------------------------|
| Phone number                      | `phone_number()`                               | E.164 US format minimum  |
| Country code / name               | `country_code()` / `country()`                 | ISO 3166-1 CSV           |
| Job title                         | `job()`                                        | Static word list         |
| Company name                      | `company()`                                    | Word list + suffix       |
| City / postal code                | `city()` / `postcode()`                        | US cities CSV            |
| Street address                    | `street_address()`                             | Number + street + suffix |
| Color generators                  | `hex_color()` / `rgb_color()` / `color_name()` | CSS named colors         |
| Lorem word / sentence / paragraph | `word()` / `sentence()` / `paragraph()`        | Standard lorem word list |

### Tier 2 — Medium complexity

| Feature                  | faker method              | Notes                                                |
|--------------------------|---------------------------|------------------------------------------------------|
| Full name facade         | `name()`                  | Compose `Title` + `FirstName` + `SurName` + `Suffix` |
| Password generator       | `password()`              | Policy params: min digits, specials, upper           |
| UUID generator           | `uuid4()`                 | Wrap `java.util.UUID.randomUUID()`                   |
| SHA1 / SHA256            | `sha1()` / `sha256()`     | Add to `HexHashGenerator` via `MessageDigest`        |
| URL / domain name        | `url()` / `domain_name()` | Compose TLD + domain word + scheme                   |
| MAC address              | `mac_address()`           | 6 hex octets                                         |
| IBAN                     | `iban()`                  | ISO 7064 MOD-97 check digits                         |
| EAN-13 / EAN-8           | `ean13()` / `ean8()`      | Check digit algorithm                                |
| ISBN-10 / ISBN-13        | `isbn10()` / `isbn13()`   | Check digit algorithm                                |
| Credit card BIN prefixes | `credit_card_number()`    | Extend `LuhnGenerator` with Visa/MC/Amex             |

### Tier 3 — Higher complexity

| Feature                       | faker method                             | Notes                                            |
|-------------------------------|------------------------------------------|--------------------------------------------------|
| `LocalDate` / `ZonedDateTime` | `date_between()` / `date_time_between()` | Refactor `BirthDay` to `java.time`               |
| Latitude / longitude          | `latitude()` / `latlng()`                | `double` in valid range                          |
| Profile composite             | `profile()`                              | Compose all user generators into a record        |
| User-Agent                    | `user_agent()`                           | Static list of real UA strings                   |
| Weighted generator            | faker's weighted locale selection        | `WeightedGenerator<T>` wrapper                   |
| Unique generator              | `fake.unique` proxy                      | `UniqueGenerator<T>` wrapping any `Generator<T>` |
| Locale support                | All 80 locales                           | Locale prefix in `ResourcePathHolder`            |
| VIN                           | `vin()`                                  | 17-char with check digit per ISO 3779            |
