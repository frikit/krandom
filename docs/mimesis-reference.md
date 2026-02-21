# mimesis Reference

**Repository:** https://github.com/lk-geimfari/mimesis
**Website:** https://mimesis.name
**License:** MIT
**Latest stable:** 18.x (2024–2025 series)
**Python requirement:** >= 3.10
**PyPI package:** `mimesis`

```bash
pip install mimesis
```

---

## 1. Purpose and Overview

mimesis is a high-performance Python library for generating synthetic (fake) data. Primary design goals:

- **Speed** — significantly faster than Faker; locale data is loaded once per provider instance, no dynamic dispatch overhead on every call.
- **Type safety** — all public methods are fully annotated; every provider is a concrete Python class with discoverable methods.
- **Correctness** — data files are curated per locale (correct name gender agreement, postal code patterns, etc.).
- **Structured bulk generation** — the `Schema` + `Field` API turns mimesis into a mini ETL tool for producing thousands of realistic records in one call.

**Primary use cases:**
- Unit and integration test fixture generation
- Seeding development/staging databases with realistic data
- Creating datasets for ML experiments and data science demos
- Load-testing pipelines that require plausible domain data

---

## 2. Key Differentiators from Faker

| Aspect | mimesis | Faker |
|---|---|---|
| **Performance** | ~5-15x faster per call | Slower due to dynamic dispatch |
| **API style** | Concrete provider classes with typed methods | One `Faker` proxy via `__getattr__` |
| **Type annotations** | Complete; mypy-clean | Partial / stub-dependent |
| **Locale data loading** | Eager load once at instantiation | Lazy-loaded per call path |
| **Bulk generation** | First-class `Schema` + `Field` + `loop()` | Manual list comprehensions |
| **Custom providers** | Subclass `BaseProvider`; register on `Generic` | Subclass `BaseProvider`; add to `Faker` |
| **Seeding** | Per-provider `seed()` or `reseed()` | `Faker(seed=N)` |
| **Locale coverage** | ~50 locales (rich data for each) | ~80+ locales (uneven depth) |
| **Binary/file data** | Dedicated `BinaryFile` provider | Requires extensions |
| **Cryptographic data** | Dedicated `Cryptographic` provider | Spread across providers |
| **Hardware/device data** | Dedicated `Hardware` provider | No equivalent |
| **Scientific data** | `Science` provider (elements, SI units) | No equivalent |

---

## 3. Core API

### 3.1 `Locale`

```python
from mimesis.enums import Locale

Locale.EN      # English (default)
Locale.DE      # German
Locale.FR      # French
Locale.RU      # Russian
Locale.ZH      # Chinese (Simplified)
Locale.JA      # Japanese
Locale.PT_BR   # Portuguese (Brazil)
Locale.ES      # Spanish
```

### 3.2 Provider instantiation

```python
from mimesis import Person, Address
from mimesis.enums import Locale

p      = Person()                              # default locale (EN), unseeded
p_de   = Person(locale=Locale.DE)             # German names
p_seed = Person(locale=Locale.EN, seed=42)    # reproducible
p.reseed(99)                                   # reseed existing instance
```

Providers load their locale data file once at `__init__`. After that every method call is pure in-memory lookup.

### 3.3 `Generic` — all providers under one roof

```python
from mimesis import Generic
from mimesis.enums import Locale

g = Generic(locale=Locale.EN)

g.person.full_name()        # => 'Amanda Clarke'
g.address.city()            # => 'Austin'
g.internet.email()          # => 'a.clarke@example.com'
g.numeric.integer_number()  # => 42
g.text.word()               # => 'forest'
```

### 3.4 `Field`

`Field` is a callable wrapper around a single provider method — the building block for `Schema`.

```python
from mimesis import Field
from mimesis.enums import Locale

field = Field(locale=Locale.EN, seed=42)

field('person.full_name')                              # => 'Amanda Clarke'
field('address.city')                                  # => 'Austin'
field('numeric.integer_number', start=1, end=100)      # => 73
field('choice', items=[1, 2, 3])                       # => 2
```

### 3.5 `Schema`

```python
from mimesis import Field, Schema
from mimesis.enums import Locale

field = Field(locale=Locale.EN, seed=0)

schema = Schema(
    schema=lambda: {
        "id":      field("numeric.increment"),
        "name":    field("person.full_name"),
        "email":   field("person.email"),
        "age":     field("person.age", minimum=18, maximum=65),
        "city":    field("address.city"),
        "country": field("address.country"),
        "score":   field("numeric.float_number", start=0.0, end=1.0),
    },
    iterations=1000,
)

records = schema.create()       # list[dict] with 1000 records

# Memory-efficient streaming for large datasets
for record in schema.loop():
    db.insert(record)
```

### 3.6 Seeding

```python
Person.seed(42)          # class-level — affects all instances
p = Person()
p.reseed(42)             # instance-level
field = Field(locale=Locale.EN, seed=42)   # field-level (recommended for Schema)
```

---

## 4. Locale Support

~50 locales, each with curated JSON data files.

| Locale | Language / Region |
|---|---|
| `EN` | English (default) |
| `EN_AU` | English (Australia) |
| `EN_CA` | English (Canada) |
| `EN_GB` | English (UK) |
| `DE` | German |
| `DE_AT` | German (Austria) |
| `DE_CH` | German (Switzerland) |
| `FR` | French |
| `ES` | Spanish |
| `ES_MX` | Spanish (Mexico) |
| `PT` | Portuguese |
| `PT_BR` | Portuguese (Brazil) |
| `IT` | Italian |
| `RU` | Russian |
| `UK` | Ukrainian |
| `PL` | Polish |
| `CS` | Czech |
| `SK` | Slovak |
| `DA` | Danish |
| `SV` | Swedish |
| `NO` | Norwegian |
| `FI` | Finnish |
| `NL` | Dutch |
| `NL_BE` | Dutch (Belgium) |
| `TR` | Turkish |
| `JA` | Japanese |
| `ZH` | Chinese (Simplified) |
| `KO` | Korean |
| `RO` | Romanian |
| `HU` | Hungarian |
| `HR` | Croatian |
| `EL` | Greek |
| `FA` | Persian |
| `KK` | Kazakh |
| `LV` | Latvian |
| `LT` | Lithuanian |
| `ET` | Estonian |
| `IS` | Icelandic |
| `MK` | Macedonian |
| `SL` | Slovenian |
| `SQ` | Albanian |
| `SR` | Serbian |

Fallback: missing keys fall back to `EN` rather than raising an error.

---

## 5. Providers and Methods (Complete Reference)

### 5.1 Address

| Method | Returns | Example |
|---|---|---|
| `address()` | `str` | `'2341 Oak Street'` |
| `street_number(maximum=1400)` | `str` | `'482'` |
| `street_name()` | `str` | `'Oak'` |
| `street_suffix()` | `str` | `'Street'` |
| `avenue()` | `str` | `'Fifth Avenue'` |
| `calling_code()` | `str` | `'+1'` |
| `city()` | `str` | `'Austin'` |
| `state(abbr=False)` | `str` | `'Texas'` / `'TX'` |
| `country()` | `str` | `'United States'` |
| `country_code(fmt=CountryCode.A2)` | `str` | `'US'` / `'USA'` |
| `continent(code=False)` | `str` | `'North America'` |
| `coordinates(dms=False)` | `dict` | `{'latitude': 30.26, 'longitude': -97.74}` |
| `latitude()` | `float` | `30.265` |
| `longitude()` | `float` | `-97.743` |
| `postal_code()` | `str` | `'78701'` |
| `zip_code()` | `str` | Alias for `postal_code()` |
| `timezone()` | `str` | `'America/Chicago'` |

**CountryCode enum:** `A2`, `A3`, `NUMERIC`.

### 5.2 BinaryFile

Generates binary file content in memory (`bytes`). Useful for testing file-upload endpoints.

| Method | Returns | Notes |
|---|---|---|
| `audio(file_type=AudioFile.MP3)` | `bytes` | Fake audio bytes |
| `video(file_type=VideoFile.MP4)` | `bytes` | Fake video bytes |
| `image(file_type=ImageFile.PNG)` | `bytes` | Fake image bytes |
| `document(file_type=DocumentFile.PDF)` | `bytes` | Fake document bytes |
| `compressed(file_type=CompressedFile.ZIP)` | `bytes` | Fake archive bytes |

### 5.3 Code

| Method | Returns | Example |
|---|---|---|
| `isbn(fmt=ISBNFormat.ISBN13)` | `str` | `'978-3-16-148410-0'` |
| `ean(fmt=EANFormat.EAN13)` | `str` | `'4006381333931'` |
| `imei()` | `str` | `'354688080223012'` (15 digits, Luhn-valid) |
| `pin(mask='####')` | `str` | `'7382'` |
| `locale_code()` | `str` | `'en-US'` |
| `issn(mask='####-####')` | `str` | `'0378-5955'` |

### 5.4 Choice

```python
choice.choice(items=['red', 'green', 'blue'])                           # single item
choice.choice(items=range(10), length=3, unique=True)                   # 3 unique
choice.choice(items=['a', 'b', 'c'], weights=[0.7, 0.2, 0.1])          # weighted
```

When `length=1` returns a single element; when `length > 1` returns a list.

### 5.5 Cryptographic

Uses `secrets` module internally.

| Method | Returns | Example |
|---|---|---|
| `uuid(version=4)` | `str` | `'550e8400-e29b-41d4-a716-446655440000'` |
| `uuid_object()` | `UUID` | `uuid.UUID('550e8400-...')` |
| `token_hex(entropy=32)` | `str` | 64-char hex string |
| `token_bytes(entropy=32)` | `bytes` | Raw random bytes |
| `token_urlsafe(entropy=32)` | `str` | URL-safe base64 token |
| `hash(algorithm=Algorithm.SHA256)` | `str` | Hex digest |
| `mnemonic_phrase(length=12)` | `str` | BIP-39 mnemonic phrase |

**Algorithm enum:** `SHA1`, `SHA224`, `SHA256`, `SHA384`, `SHA512`, `MD5`, `BLAKE2B`, `BLAKE2S`.

### 5.6 Date

| Method | Returns | Example |
|---|---|---|
| `date(start=2000, end=2035)` | `datetime.date` | `date(2018, 7, 14)` |
| `formatted_date(fmt='%Y-%m-%d', ...)` | `str` | `'2018-07-14'` |
| `year(minimum=1990, maximum=2035)` | `int` | `2021` |
| `month()` | `str` | `'July'` |
| `day_of_week()` | `str` | `'Wednesday'` |
| `day_of_month()` | `int` | `14` |
| `periodicity()` | `str` | `'weekly'` |
| `century()` | `str` | `'XXI'` |
| `season()` | `str` | `'Summer'` |

### 5.7 Datetime

| Method | Returns | Example |
|---|---|---|
| `datetime(start=2000, end=2035, timezone=None)` | `datetime.datetime` | `datetime(2019, 3, 15, 14, 32, 7)` |
| `formatted_datetime(fmt='%Y-%m-%dT%H:%M:%S', ...)` | `str` | `'2019-03-15T14:32:07'` |
| `date(start=2000, end=2035)` | `datetime.date` | `date(2019, 3, 15)` |
| `time()` | `datetime.time` | `time(14, 32, 7)` |
| `timestamp(fmt=TimestampFormat.POSIX)` | `int\|str` | `1552659127` |
| `timezone(region=None)` | `str` | `'America/New_York'` |
| `gmt_offset()` | `str` | `'UTC+05:30'` |
| `duration(min_hours=1, max_hours=24)` | `timedelta` | `timedelta(hours=7, minutes=14)` |

**TimestampFormat enum:** `POSIX` (int), `ISO_8601` (str).

### 5.8 Development

| Method | Returns | Example |
|---|---|---|
| `software_license()` | `str` | `'MIT'` |
| `version(calver=False, pre_release=False)` | `str` | `'3.14.159'` |
| `calver()` | `str` | `'2024.7.1'` |
| `os()` | `str` | `'Linux'` |
| `programming_language()` | `str` | `'Python'` |
| `system_quality_attribute()` | `str` | `'Scalability'` |
| `boolean()` | `bool` | `True` |
| `stage()` | `str` | `'beta'` |
| `dsn(dsn_type=DSNType.POSTGRES)` | `str` | `'postgresql://user:pass@host:5432/db'` |

### 5.9 Finance

| Method | Returns | Example |
|---|---|---|
| `company()` | `str` | `'Acme Corp'` |
| `company_type(abbr=False)` | `str` | `'LLC'` |
| `currency()` | `str` | `'US Dollar'` |
| `currency_iso_code()` | `str` | `'USD'` |
| `currency_symbol()` | `str` | `'$'` |
| `price(minimum=10.0, maximum=1000.0)` | `float` | `249.99` |
| `price_in_btc(minimum=0.0001, maximum=2.0)` | `float` | `0.00724` |
| `stock_exchange()` | `str` | `'NASDAQ'` |
| `stock_name()` | `str` | `'Apple Inc.'` |
| `stock_ticker()` | `str` | `'AAPL'` |

### 5.10 Food

| Method | Returns | Example |
|---|---|---|
| `dish()` | `str` | `'Spaghetti Carbonara'` |
| `drink()` | `str` | `'Latte'` |
| `fruit()` | `str` | `'Mango'` |
| `vegetable()` | `str` | `'Broccoli'` |
| `spice()` | `str` | `'Cumin'` |
| `mushroom()` | `str` | `'Portobello'` |

### 5.11 Hardware

| Method | Returns | Example |
|---|---|---|
| `cpu()` | `str` | `'Intel Core i9-13900K'` |
| `cpu_frequency()` | `str` | `'3.80GHz'` |
| `cpu_codename()` | `str` | `'Raptor Lake'` |
| `ram_size()` | `str` | `'32GB'` |
| `ram_type()` | `str` | `'DDR5'` |
| `ssd_or_hdd()` | `str` | `'SSD'` |
| `graphics()` | `str` | `'NVIDIA RTX 4090'` |
| `manufacturer()` | `str` | `'ASUS'` |
| `phone_model()` | `str` | `'iPhone 15 Pro'` |
| `resolution()` | `str` | `'1920x1080'` |
| `screen_size()` | `str` | `'27"'` |

### 5.12 Internet

| Method | Returns | Example |
|---|---|---|
| `email(domains=None, unique=False)` | `str` | `'a.clarke@example.com'` |
| `hostname(tld_type=TLDType.CCTLD, subdomains=None)` | `str` | `'mail.example.co.uk'` |
| `ip_v4()` | `str` | `'192.168.1.100'` |
| `ip_v4_object()` | `IPv4Address` | `IPv4Address('192.168.1.100')` |
| `ip_v4_with_port(port_range=PortRange.ALL)` | `str` | `'192.168.1.100:8080'` |
| `ip_v6()` | `str` | `'2001:db8::1'` |
| `ip_v6_object()` | `IPv6Address` | `IPv6Address('2001:db8::1')` |
| `mac_address()` | `str` | `'00:1A:2B:3C:4D:5E'` |
| `uri(scheme=URIScheme.HTTPS, ...)` | `str` | `'https://example.com/path'` |
| `url()` | `str` | `'https://example.com'` |
| `slug(parts=2)` | `str` | `'quick-brown'` |
| `query_string(length=5)` | `str` | `'?foo=bar&baz=qux'` |
| `http_method()` | `str` | `'GET'` |
| `http_status_code()` | `int` | `200` |
| `http_status_message()` | `str` | `'200 OK'` |
| `http_request_headers()` | `dict` | `{'Accept': 'application/json', ...}` |
| `http_response_headers()` | `dict` | `{'Content-Type': 'text/html', ...}` |
| `tld(tld_type=TLDType.GTLD)` | `str` | `'.com'` |
| `user_agent()` | `str` | `'Mozilla/5.0 ...'` |
| `port(port_range=PortRange.ALL)` | `int` | `8443` |
| `public_dns()` | `str` | `'8.8.8.8'` |
| `content_type(mime_type=MimeType.TEXT)` | `str` | `'text/html; charset=utf-8'` |
| `network_protocol(layer=Layer.APPLICATION)` | `str` | `'HTTP'` |

**Enums:** `TLDType` (CCTLD, GTLD, GEOTLD, UTLD, STLD), `PortRange` (ALL, WELL_KNOWN, REGISTERED, DYNAMIC), `MimeType` (TEXT, IMAGE, AUDIO, VIDEO, APPLICATION), `Layer` (APPLICATION, TRANSPORT, NETWORK, DATA_LINK), `URIScheme` (HTTP, HTTPS, FTP, FTPS, SFTP, WS, WSS).

### 5.13 Numeric

| Method | Returns | Example |
|---|---|---|
| `integer_number(start=-1000, end=1000)` | `int` | `347` |
| `float_number(start=-1000.0, end=1000.0, precision=15)` | `float` | `347.831` |
| `complex_number(...)` | `complex` | `(3.14+2.71j)` |
| `decimal_number(start=-1000.0, end=1000.0)` | `Decimal` | `Decimal('347.83')` |
| `matrix(m=3, n=3, num_type=NumType.FLOAT, ...)` | `list[list]` | `[[0.1, 0.5], ...]` |
| `integers(start=1, end=1000, n=10)` | `list[int]` | `[3, 47, 102, ...]` |
| `floats(start=0.0, end=1.0, n=10)` | `list[float]` | `[0.3, 0.71, ...]` |
| `decimals(start=0.0, end=1.0, n=10)` | `list[Decimal]` | `[Decimal('0.47'), ...]` |
| `increment(accumulator=None)` | `int` | `1, 2, 3, ...` |

### 5.14 Path

| Method | Returns | Example |
|---|---|---|
| `root()` | `str` | `'/'` or `'C:\\'` |
| `home()` | `str` | `'/home/alice'` |
| `user()` | `str` | `'alice'` |
| `users_folder()` | `str` | `'/home/alice'` |
| `dev_dir()` | `str` | `'/home/alice/Development'` |
| `project_dir()` | `str` | `'/home/alice/Development/myproject'` |

### 5.15 Payment

| Method | Returns | Example |
|---|---|---|
| `credit_card_number(card_type=CardType.VISA)` | `str` | `'4532015112830366'` |
| `credit_card_expiration_date(minimum=16, maximum=25)` | `str` | `'09/26'` |
| `cvv()` | `str` | `'741'` |
| `credit_card_owner()` | `dict` | `{'owner': 'ALICE SMITH', 'number': '...', ...}` |
| `credit_card_network()` | `str` | `'Visa'` |
| `paypal()` | `str` | `'alice@paypal.example.com'` |
| `bitcoin_address()` | `str` | `'1A1zP1eP5Q...'` |
| `ethereum_address()` | `str` | `'0x5aAeb6...'` |

**CardType enum:** `VISA`, `MASTER_CARD`, `AMEX`, `DISCOVER`.

### 5.16 Person

| Method | Returns | Example |
|---|---|---|
| `first_name(gender=None)` | `str` | `'Alice'` |
| `last_name(gender=None)` | `str` | `'Clarke'` |
| `full_name(gender=None, reverse=False)` | `str` | `'Alice Clarke'` |
| `title(gender=None, title_type=None)` | `str` | `'Dr.'` |
| `username(mask=None, drange=(1900, 2023))` | `str` | `'alice_clarke'` |
| `email(domains=None, unique=False)` | `str` | `'alice@example.com'` |
| `password(length=8, hashed=False)` | `str` | `'xK!9mPq3'` |
| `telephone(mask=None)` | `str` | `'+1 (555) 234-5678'` |
| `age(minimum=1, maximum=90)` | `int` | `34` |
| `birthday(min_year=1980, max_year=2023)` | `datetime.date` | `date(1991, 6, 23)` |
| `gender()` | `str` | `'Female'` |
| `gender_symbol()` | `str` | `'♀'` |
| `height(minimum=1.5, maximum=2.1)` | `str` | `'1.72m'` |
| `weight(minimum=38, maximum=150)` | `int` | `72` |
| `blood_type()` | `str` | `'A+'` |
| `nationality(gender=None)` | `str` | `'American'` |
| `occupation()` | `str` | `'Software Engineer'` |
| `political_views()` | `str` | `'Liberal'` |
| `worldview()` | `str` | `'Humanism'` |
| `academic_degree()` | `str` | `'Bachelor'` |
| `language()` | `str` | `'English'` |
| `university()` | `str` | `'MIT'` |
| `identifier(mask='##-##/##')` | `str` | `'42-17/89'` |
| `ssn()` | `str` | `'123-45-6789'` |

**Gender enum:** `MALE`, `FEMALE`.

### 5.17 Science

| Method | Returns | Example |
|---|---|---|
| `chemical_element(raw=False)` | `str\|dict` | `'Oxygen'` / `{'name': 'Oxygen', 'symbol': 'O', 'atomic_number': 8}` |
| `measure(prefixed=False)` | `str` | `'metre'` / `'kilometre'` |
| `metric_prefix(sign=PrefixSign.POSITIVE)` | `str` | `'kilo'` |
| `physical_quantity()` | `str` | `'force'` |

### 5.18 Text

| Method | Returns | Example |
|---|---|---|
| `word()` | `str` | `'forest'` |
| `words(quantity=5)` | `list[str]` | `['apple', 'road', ...]` |
| `sentence()` | `str` | `'The quick brown fox...'` |
| `sentences(quantity=5)` | `list[str]` | `['Sentence one.', ...]` |
| `title()` | `str` | `'The Forest Path'` |
| `text(quantity=5)` | `str` | Multi-sentence paragraph |
| `answer()` | `str` | `'Yes'` |
| `quote()` | `str` | Famous quote |
| `color()` | `str` | `'Crimson'` |
| `hex_color()` | `str` | `'#FF5733'` |
| `rgb_color()` | `tuple` | `(255, 87, 51)` |
| `level()` | `str` | `'critical'` |
| `alphabet(lower_case=False)` | `list[str]` | `['A', 'B', ..., 'Z']` |

### 5.19 Transport

| Method | Returns | Example |
|---|---|---|
| `vehicle()` | `str` | `'Toyota Camry'` |
| `manufacturer()` | `str` | `'Ford'` |
| `car()` | `str` | Alias for `vehicle()` |
| `airplane(model_mask='###')` | `str` | `'Boeing 737'` |
| `truck(model_mask='#### ##')` | `str` | `'Volvo FH16'` |

---

## 6. Schema / Bulk Generation API

### 6.1 Basic pattern

```python
from mimesis import Field, Schema
from mimesis.enums import Locale

field = Field(locale=Locale.EN, seed=42)

schema = Schema(
    schema=lambda: {
        "user_id":   field("numeric.increment"),
        "username":  field("person.username"),
        "email":     field("person.email"),
        "password":  field("person.password", length=12),
        "first":     field("person.first_name"),
        "age":       field("person.age", minimum=18, maximum=70),
        "city":      field("address.city"),
        "ip":        field("internet.ip_v4"),
        "created":   field("datetime.formatted_datetime", fmt="%Y-%m-%d"),
        "is_active": field("development.boolean"),
    },
    iterations=10_000,
)
users = schema.create()
```

### 6.2 Streaming large datasets

```python
schema = Schema(schema=lambda: {...}, iterations=1_000_000)
with open("data.jsonl", "w") as f:
    for record in schema.loop():   # generator — one record at a time
        f.write(json.dumps(record) + "\n")
```

### 6.3 Pandas integration

```python
import pandas as pd
df = pd.DataFrame(schema.create())
```

---

## 7. Custom Providers

```python
from mimesis.providers.base import BaseProvider
from mimesis import Generic
from mimesis.enums import Locale

class GamingProvider(BaseProvider):
    class Meta:
        name = "gaming"

    _GENRES = ["RPG", "FPS", "RTS", "MOBA", "Puzzle"]
    _PLATFORMS = ["PC", "PlayStation 5", "Xbox Series X", "Nintendo Switch"]

    def genre(self) -> str:
        return self.random.choice(self._GENRES)

    def platform(self) -> str:
        return self.random.choice(self._PLATFORMS)

g = Generic(locale=Locale.EN)
g.add_provider(GamingProvider)
g.gaming.genre()      # => 'RPG'
g.gaming.platform()   # => 'Nintendo Switch'
```

`self.random` is the seeded `Random` instance shared within the provider.

---

## 8. Performance Notes

| Library | Time (10k `full_name()` calls, M1) | Relative |
|---|---|---|
| mimesis | ~0.08 s | 1x baseline |
| Faker | ~0.8–1.2 s | ~10-15x slower |

**Key design factors:**
1. Eager locale data load in `__init__` — no I/O during generation
2. No `__getattr__` dynamic dispatch
3. Single `random.Random` instance per provider
4. Flat `list[str]` / `dict[str, list[str]]` data structures — O(1) selection
5. No ORM or serialisation overhead

**Thread safety:** seeded providers are **not thread-safe** (shared `random.Random`). Create one provider per thread.

---

## 9. Comparison / Mapping to krandom

### 9.1 Implemented in krandom

| mimesis method | krandom class | Notes |
|---|---|---|
| `Numeric.integer_number()` | `IntGenerator` | Bounded, seeded |
| `Numeric.float_number()` | `FloatGenerator`, `DoubleGenerator` | |
| `Numeric.integer_number()` (short/long/byte) | `ShortGenerator`, `LongGenerator`, `ByteGenerator` | |
| `Development.boolean()` | `BooleanGenerator` | |
| `Text.word()` (char) | `CharGenerator` | Character only |
| `Text.word()` / `sentence()` | `StringGenerator` | Raw random chars, no vocab |
| `Person.first_name()` | `FirstName.kt` | EN names only |
| `Person.last_name()` | `SurName.kt` | EN names only |
| `Person.full_name()` | `KRandomUser` | Combined |
| `Person.username()` | `Username.kt` | |
| `Person.age()` | `Age.kt` | |
| `Person.gender()` | `Gender.kt` | |
| `Person.email()` | `Email.kt` | |
| `Person.title()` | `Title.kt` | |
| `Person.birthday()` | `BirthDay.kt` | |
| `Person.ssn()` | `SocialSecurityNumber.kt` | US format |
| `Internet.ip_v4()` | `IPv4Generator.java` | RFC 791 unicast |
| `Internet.ip_v6()` | `IPv6Generator.java` | RFC 4291 / RFC 5952 |
| `Cryptographic.hash()` (hex) | `HexHashGenerator.kt` | Hex only |
| `Code.imei()` (Luhn-valid) | `LuhnGenerator.java` | 10-digit only |
| Fibonacci sequence | `FibonacciGenerator.java` | No mimesis equivalent |
| Dice simulation | `DiceGenerator.java` (D4–D20) | No mimesis equivalent |
| Coin flip | `CoinGenerator.java` | No mimesis equivalent |
| POJO population | `ObjectGenerator.java` | No mimesis equivalent |

### 9.2 Gaps — Tier 1 (low effort, pure computation)

| mimesis method | Category | Description |
|---|---|---|
| `Numeric.decimal_number()` | Numeric | `BigDecimal` analog |
| `Numeric.matrix()` | Numeric | 2D numeric arrays |
| `Numeric.increment()` | Numeric | Auto-incrementing counter |
| `Text.hex_color()` | Text | `#RRGGBB` hex color |
| `Text.rgb_color()` | Text | RGB triple |
| `Development.version()` | Development | Semantic version string |
| `Choice.choice()` with weights | Utility | Weighted random from list |
| `Internet.ip_v4_with_port()` | Network | `IPv4:port` string |
| `Internet.mac_address()` | Network | MAC address |
| `Internet.port()` | Network | Port number with range |
| `Internet.http_status_code()` | Web | HTTP status integer |
| `Internet.http_method()` | Web | GET, POST, DELETE, ... |
| `Internet.slug()` | Web | URL-safe slug |
| `Code.pin()` | Code | N-digit numeric PIN |
| `Payment.cvv()` | Finance | 3-4 digit CVV |
| `Payment.credit_card_expiration_date()` | Finance | MM/YY string |
| `Finance.price()` | Finance | Monetary amount |

### 9.3 Gaps — Tier 2 (medium effort, data files needed)

| mimesis method | Category | Description |
|---|---|---|
| `Address.city()` | Address | City name |
| `Address.country()` / `country_code()` | Address | Country name / ISO code |
| `Address.postal_code()` | Address | Postal/zip code |
| `Address.state()` | Address | State/province name |
| `Address.street_name()` / `address()` | Address | Street / full address |
| `Address.latitude()` / `longitude()` | Address | Geographic coordinates |
| `Address.timezone()` | Address | IANA timezone string |
| `Internet.url()` | Web | Full URL |
| `Internet.user_agent()` | Web | Browser UA string |
| `Internet.content_type()` | Web | MIME content-type |
| `Payment.credit_card_number()` | Finance | Luhn-valid multi-network |
| `Payment.bitcoin_address()` | Finance | P2PKH Bitcoin address |
| `Code.isbn()` | Code | ISBN-13 with check digit |
| `Code.ean()` | Code | EAN-13 barcode |
| `Finance.company()` / `currency_iso_code()` | Finance | Company / currency data |
| `Finance.stock_ticker()` | Finance | Ticker symbol |
| `Datetime.datetime()` / `timestamp()` | Date/Time | Full datetime / Unix timestamp |
| `Date.date()` / `Date.year()` | Date/Time | Calendar date / year |
| `Text.word()` (vocabulary) | Text | Real word from word list |
| `Text.sentence()` | Text | Real sentence |
| `Text.color()` | Text | Color name |
| `Development.programming_language()` / `os()` | Development | Tech name lists |

### 9.4 Gaps — Tier 3 (high effort / niche)

| mimesis | Category | Notes |
|---|---|---|
| `Science.*` | Science | Chemical elements, SI units |
| `Hardware.*` | Hardware | CPU/GPU/phone model strings |
| `Transport.*` | Transport | Car / aircraft models |
| `BinaryFile.*` | Binary | Fake binary file bytes |
| `Cryptographic.mnemonic_phrase()` | Crypto | BIP-39 mnemonic |
| `Person.blood_type()` / `nationality()` | Person | Additional person fields |
| `Path.*` | Filesystem | Path string generation |
| Full locale support | All | Multi-locale data files |

### 9.5 Schema / bulk generation gap

mimesis `Schema` + `Field` generates declarative heterogeneous records. krandom has `Generator.generateList(n)` (scalar, one type) and `ObjectGenerator<T>` (reflection-based POJO). No equivalent to `Schema` for structured multi-field records exists in krandom.

---

## 10. Quick-start Cheatsheet

```python
from mimesis import Generic, Field, Schema, Person, Address, Internet
from mimesis.enums import Locale, Gender

# Single-provider usage
p = Person(locale=Locale.EN, seed=42)
a = Address(locale=Locale.EN)
i = Internet()

print(p.full_name())                    # 'Amanda Clarke'
print(p.email())                        # 'a.clarke@example.com'
print(p.age(minimum=18, maximum=65))    # 34
print(a.city())                         # 'Austin'
print(a.coordinates())                  # {'latitude': 30.26, 'longitude': -97.74}
print(i.ip_v4())                        # '192.168.23.45'
print(i.url())                          # 'https://example.com'

# Generic
g = Generic(locale=Locale.DE)
print(g.person.first_name(gender=Gender.FEMALE))  # 'Klara'
print(g.address.city())                            # 'Munchen'
print(g.finance.currency_iso_code())               # 'EUR'

# Schema / bulk generation
field = Field(locale=Locale.EN, seed=0)
schema = Schema(
    schema=lambda: {
        "id":      field("numeric.increment"),
        "name":    field("person.full_name"),
        "email":   field("person.email"),
        "country": field("address.country"),
        "score":   field("numeric.float_number", start=0.0, end=100.0),
    },
    iterations=1000,
)
records = schema.create()    # list[dict], 1000 items
```
