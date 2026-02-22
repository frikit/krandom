# RandomLib Reference

**Repository:** https://github.com/ircmaxell/RandomLib
**Author:** Anthony Ferrara (ircmaxell@ircmaxell.com)
**License:** MIT
**Status:** Archived — active for legacy PHP; modern PHP 7+ applications should prefer `random_bytes()` / `random_int()` natively
**Composer package:** `ircmaxell/random-lib`
**PHP requirement:** >= 5.3.2
**Dependency:** `ircmaxell/security-lib ^1.1`

```bash
composer require ircmaxell/random-lib
```

---

## 1. Purpose

RandomLib generates cryptographically-quality random numbers and strings in PHP at configurable security strengths. It solves the pre-PHP-7 problem of inconsistent OS entropy APIs across platforms by
mixing output from every available source through a cryptographic combiner, providing defense-in-depth: even if one entropy source is weak or compromised, the combined output remains unpredictable as
long as any single source contributes genuine entropy.

**Primary use cases:**

- Password-reset tokens, CSRF tokens, session identifiers (MEDIUM strength)
- Cryptographic key material for long-lived secrets (HIGH strength)
- Non-critical random strings and integers (LOW strength)
- Cross-platform portable random generation on PHP 5.3–7.1

```php
$factory   = new RandomLib\Factory;
$generator = $factory->getMediumStrengthGenerator();

$bytes  = $generator->generate(32);                                         // 32 raw bytes
$int    = $generator->generateInt(1, 100);                                  // integer in [1, 100]
$token  = $generator->generateString(32, RandomLib\Generator::CHAR_ALNUM); // 32-char alphanumeric
$hex    = $generator->generateString(64, '0123456789abcdef');               // 64-char hex string
```

---

## 2. Architecture

Three cooperating abstractions compose every generator:

```
Factory
  └── getGenerator(Strength)
        ├── selects: Source[] (entropy collectors)
        └── selects: Mixer   (combiner)
              └── Generator(mixer, sources)
                    generate(size)
                      ├── source1.generate(size) ──┐
                      ├── source2.generate(size) ──┤─→ mixer.mix([...]) → bytes
                      └── sourceN.generate(size) ──┘
```

| Layer         | Role                                                                                   |
|---------------|----------------------------------------------------------------------------------------|
| **Source**    | Reads from a single OS/PHP entropy mechanism; returns raw bytes                        |
| **Mixer**     | Receives one byte string per source; combines them into a single output                |
| **Generator** | Orchestrates sources + mixer; exposes high-level `generate*` methods                   |
| **Factory**   | Auto-discovers available sources and mixers; selects by strength; constructs Generator |

### Strength levels (`SecurityLib\Strength`)

Five ordered levels used for filtering sources and mixers:

| Constant   | Ordinal | Notes                                                         |
|------------|---------|---------------------------------------------------------------|
| `VERYHIGH` | 5       | No components ship at this level                              |
| `HIGH`     | 4       | `/dev/random`, `random_bytes()`, `libsodium`, patched OpenSSL |
| `MEDIUM`   | 3       | Hash (sha512) mixer; `/dev/urandom`, CAPICOM, OpenSSL         |
| `LOW`      | 2       | XOR mixer; `mt_rand`, `uniqid`, microtime                     |
| `VERYLOW`  | 1       | `rand()`, XOR mixer without Suhosin                           |

---

## 3. `RandomLib\Generator`

The main public object. Holds a mixer and an array of sources.

### Constructor

```php
new Generator(\RandomLib\Mixer $mixer, array $sources)
```

### `generate(int $size): string`

Calls every registered source for `$size` bytes, passes all results to the mixer, returns the mixed byte string.

```php
$bytes = $generator->generate(32); // 32 raw binary bytes
```

### `generateInt(int $min = 0, int $max = PHP_INT_MAX): int`

Uniform random integer in `[$min, $max]`. Uses bit-masking + rejection sampling to avoid modulo bias. Handles ranges exceeding `PHP_INT_MAX` on 32-bit platforms.

```php
$int = $generator->generateInt(1, 6);     // die roll
$id  = $generator->generateInt(0, 99999); // 5-digit ID
```

Throws `\RangeException` if `$min > $max`.

### `generateString(int $length, int|string $characters = ''): string`

Uniform random string of `$length` characters drawn from `$characters`. Accepts either a `Generator::CHAR_*` bitmask or an explicit character string. Uses rejection sampling for equal probability per
character regardless of pool size.

```php
// Built-in charset constants (bitmask)
$generator->generateString(16, Generator::CHAR_ALNUM);
$generator->generateString(32);                                   // default: CHAR_BASE64
$generator->generateString(20, Generator::CHAR_LOWER_HEX);

// Explicit charset string
$generator->generateString(32, '0123456789abcdef');
$generator->generateString(10, 'abc');
```

### `getMixer(): \RandomLib\Mixer`

### `getSources(): array`

### Character-set constants

| Constant         | Value | Characters                                    |
|------------------|-------|-----------------------------------------------|
| `CHAR_UPPER`     | 1     | `A-Z`                                         |
| `CHAR_LOWER`     | 2     | `a-z`                                         |
| `CHAR_ALPHA`     | 3     | `A-Za-z`                                      |
| `CHAR_DIGITS`    | 4     | `0-9`                                         |
| `CHAR_ALNUM`     | 7     | `A-Za-z0-9`                                   |
| `CHAR_UPPER_HEX` | 8     | `0-9A-F`                                      |
| `CHAR_LOWER_HEX` | 16    | `0-9a-f`                                      |
| `CHAR_BASE64`    | 32    | `A-Za-z0-9+/`                                 |
| `CHAR_SYMBOLS`   | 64    | `!@#$%^&*()`                                  |
| `CHAR_BRACKETS`  | 128   | `()[]{}<>`                                    |
| `CHAR_PUNCT`     | 256   | Full printable punctuation                    |
| `EASY_TO_READ`   | 512   | Excludes ambiguous chars: `B8G6I1l\|0OQDS5Z2` |

Constants are combinable with bitwise OR:

```php
// alphanumeric, easy to read (no 0/O, 1/l, etc.)
$generator->generateString(12, Generator::CHAR_ALNUM | Generator::EASY_TO_READ);

// uppercase hex
$generator->generateString(32, Generator::CHAR_UPPER_HEX);
```

---

## 4. `RandomLib\Factory`

Entry point. Auto-discovers all mixer and source classes in `lib/RandomLib/Mixer/` and `lib/RandomLib/Source/`, registers those whose `test()` / `isSupported()` returns `true`, then builds a
`Generator` matched to the requested strength.

### Quick constructors

```php
$factory = new RandomLib\Factory;

$gen = $factory->getMediumStrengthGenerator(); // recommended default
$gen = $factory->getLowStrengthGenerator();
$gen = $factory->getHighStrengthGenerator();   // throws on PHP 7.2+ (no HIGH mixer available)
```

### `getGenerator(\SecurityLib\Strength $strength): Generator`

Selects sources whose strength >= `$strength` and the best available mixer at `$strength` (with fallback to the next lower level if no exact match exists). Throws `\RuntimeException` if no sources or
no mixer qualify.

```php
use SecurityLib\Strength;
$gen = $factory->getGenerator(new Strength(Strength::MEDIUM));
```

### Registration methods

```php
$factory->registerMixer('myMixer', MyApp\Mixer\ChaCha20::class);
$factory->registerSource('mySource', MyApp\Source\HardwareRng::class);

$factory->getMixers();   // ['Hash' => '...', 'XorMixer' => '...', ...]
$factory->getSources();  // ['URandom' => '...', 'OpenSSL' => '...', ...]
```

### How source/mixer selection works

1. `findSources($strength)` — keeps sources where `getStrength() >= $strength` AND `isSupported() === true`
2. `findMixer($strength)` — first tries exact match; falls back to the next lower strength until one is found
3. Both results are passed to `new Generator($mixer, $sources)` with fresh instance creation

---

## 5. Mixer Implementations

All mixers implement `RandomLib\Mixer`:

```php
interface Mixer {
    public static function getStrength(): \SecurityLib\Strength;
    public static function test(): bool;
    public function mix(array $parts): string;
}
```

`mix(array $parts)` receives an array of equal-length byte strings (one per source) and returns a single byte string of the same length.

### `Mixer\Hash` — MEDIUM strength

Always available. Uses HMAC with a configurable hash algorithm (default: sha512) per RFC 4086 section 5.2.

```php
new Hash(string $hash = 'sha512')
```

- Block size: hash output length (64 bytes for sha512)
- `mixParts1(A, B)` = `HMAC-SHA512(A, key=B)`
- `mixParts2(A, B)` = `HMAC-SHA512(B, key=A)`

The two operations use swapped key/data roles, preventing cancellation.

### `Mixer\McryptRijndael128` — HIGH strength

Requires the `mcrypt` extension. Removed from PHP 7.2+; not available on modern PHP.

- Cipher: Rijndael-128 (AES-128) in ECB mode
- `mixParts1`: encrypts `part1` with key derived from `part2`
- `mixParts2`: decrypts `part1` with key derived from `part2`

```php
// Only reachable on PHP <= 7.1 with mcrypt extension
```

### `Mixer\XorMixer` — VERYLOW strength

Always available. Both mixing operations are plain bitwise XOR. Used as the fallback when no stronger mixer is available.

- Block size: 64 bytes
- `mixParts1(A, B)` = `A ^ B`
- `mixParts2(A, B)` = `A ^ B`

Not cryptographically meaningful alone; the combined source pool still provides entropy through the XOR accumulation.

### `AbstractMixer` mixing loop (RFC 4086 § 5.2)

All mixers share the same loop in `AbstractMixer::mix()`:

1. Pad all parts to a multiple of `getPartSize()`
2. For each position block `i`:
    - If `i` is even: `state ^= mixParts1(state, part[i])`
    - If `i` is odd: `state ^= mixParts2(state, part[i])`
3. Return `state` trimmed to the original length

The output is at least as strong as the strongest contributing source, provided the mixer is at least that strong.

---

## 6. Source Implementations

All sources implement `RandomLib\Source`:

```php
interface Source {
    public static function getStrength(): \SecurityLib\Strength;
    public static function isSupported(): bool;
    public function generate(int $size): string;
}
```

`generate($size)` must always return exactly `$size` bytes, padding with null bytes if necessary.

### HIGH strength sources

#### `Source\RandomBytes`

- **Mechanism:** `random_bytes()` (PHP 7.0+)
- **Platform:** cross-platform
- **Available:** `function_exists('random_bytes')`
- **Notes:** Preferred source on PHP 7+. Delegates to the OS CSPRNG.

#### `Source\Sodium`

- **Mechanism:** `\Sodium\randombytes_buf()` (old PECL libsodium)
- **Platform:** wherever libsodium PECL is installed
- **Available:** `function_exists('Sodium\\randombytes_buf')`
- **Notes:** Targets the pre-PHP-7.2 PECL extension. PHP 7.2+ bundled sodium uses `sodium_randombytes_buf` (different function name — not covered by this source).

#### `Source\Random`

- **Mechanism:** reads `/dev/random`
- **Platform:** POSIX (Linux, macOS, BSDs)
- **Available:** `file_exists('/dev/random')`
- **Notes:** Blocks when the kernel entropy pool is exhausted. Slower than URandom but theoretically higher entropy during pool replenishment.

#### `Source\OpenSSL`

- **Mechanism:** `openssl_random_pseudo_bytes()`
- **Platform:** cross-platform (wherever OpenSSL extension is loaded)
- **Available:** `function_exists('openssl_random_pseudo_bytes')`
- **Strength:** HIGH on patched PHP (>= 5.4.44, >= 5.5.28, >= 5.6.12); MEDIUM on older unpatched versions

### MEDIUM strength sources

#### `Source\URandom`

- **Mechanism:** reads `/dev/urandom`
- **Platform:** POSIX
- **Available:** `file_exists('/dev/urandom')`
- **Notes:** Non-blocking kernel CSPRNG. The standard choice on Linux/macOS.

#### `Source\CAPICOM`

- **Mechanism:** `CAPICOM.Utilities` COM object → `GetRandom()`
- **Platform:** Windows only
- **Available:** `class_exists('COM')`
- **Notes:** Windows equivalent of `/dev/urandom`. Uses the OS cryptographic API via COM. Falls back to null bytes on COM failure.

#### `Source\MTRand`

- **Mechanism:** `mt_rand()` XORed in pairs per byte
- **Platform:** cross-platform
- **Available:** always
- **Strength:** MEDIUM if Suhosin patch is active (`S_ALL` defined); LOW otherwise
- **Notes:** Mersenne Twister PRNG. Weak standalone; contributes to the combined pool.

### LOW strength sources

#### `Source\MicroTime`

- **Mechanism:** Repeated SHA-512 of `microtime()` + process state + counter
- **Platform:** cross-platform
- **Available:** always
- **Notes:** `final` class. Constructor harvests process-level entropy: `posix_times()`, `zend_thread_id()`, `getmypid()`, `memory_get_usage()`, `$_ENV`, `$_SERVER`, backtrace depth. Only emits the
  first 8 of 64 SHA-512 bytes per iteration to avoid leaking internal state. Calls `gc_collect_cycles()` for timing jitter.

#### `Source\UniqID`

- **Mechanism:** `uniqid($result, true)` (with `more_entropy=true`)
- **Platform:** cross-platform
- **Available:** always
- **Notes:** Accumulates `uniqid` output until `$size` bytes are collected. The `more_entropy` flag appends an LCG float, slightly increasing entropy over plain `uniqid()`.

#### `Source\Rand`

- **Mechanism:** `rand()` XORed in pairs per byte: `chr((rand() ^ rand()) % 256)`
- **Platform:** cross-platform
- **Available:** always
- **Strength:** LOW with Suhosin (`S_ALL`); VERYLOW otherwise
- **Notes:** PHP's `libc rand()`. Weakest PRNG in the library.

### Source / mixer capability matrix

| Component | Class                     | Strength      | Platform | Requires           |
|-----------|---------------------------|---------------|----------|--------------------|
| Mixer     | `Mixer\Hash`              | MEDIUM        | all      | —                  |
| Mixer     | `Mixer\McryptRijndael128` | HIGH          | all      | mcrypt (PHP ≤ 7.1) |
| Mixer     | `Mixer\XorMixer`          | VERYLOW       | all      | —                  |
| Source    | `Source\RandomBytes`      | HIGH          | all      | PHP 7.0+           |
| Source    | `Source\Sodium`           | HIGH          | all      | libsodium PECL     |
| Source    | `Source\Random`           | HIGH          | POSIX    | —                  |
| Source    | `Source\OpenSSL`          | HIGH / MEDIUM | all      | openssl ext        |
| Source    | `Source\URandom`          | MEDIUM        | POSIX    | —                  |
| Source    | `Source\CAPICOM`          | MEDIUM        | Windows  | COM ext            |
| Source    | `Source\MTRand`           | MEDIUM / LOW  | all      | —                  |
| Source    | `Source\MicroTime`        | LOW           | all      | —                  |
| Source    | `Source\UniqID`           | LOW           | all      | —                  |
| Source    | `Source\Rand`             | LOW / VERYLOW | all      | —                  |

---

## 7. Effective Generator Composition by Strength

### `getLowStrengthGenerator()`

- Mixer selected: `XorMixer` (VERYLOW — Factory falls back to best available)
- Sources included: all available sources with strength >= LOW
- Typical pool: `Rand`, `MTRand`, `UniqID`, `MicroTime` + stronger if present
- Use case: non-security-critical tokens, test data, session hints

### `getMediumStrengthGenerator()`

- Mixer selected: `Hash` (sha512, MEDIUM)
- Sources included: all available sources with strength >= MEDIUM
- Typical pool on Linux PHP 7+: `URandom`, `OpenSSL`, `RandomBytes`
- Typical pool on Windows: `CAPICOM`, `OpenSSL`
- Use case: **recommended default** — CSRF tokens, password-reset links, salts, API keys

### `getHighStrengthGenerator()`

- Mixer selected: `McryptRijndael128` (HIGH) — requires `mcrypt`
- Sources included: only HIGH-strength sources
- Typical pool: `RandomBytes`, `Random`, `OpenSSL` (patched), `Sodium`
- **Caveat:** throws `RuntimeException` on PHP 7.2+ because `mcrypt` was removed and no HIGH mixer ships as replacement. Not usable on modern PHP.

---

## 8. Extending the Library

### Custom Source

```php
namespace MyApp\Source;

use RandomLib\AbstractSource;
use SecurityLib\Strength;

class HardwareRng extends AbstractSource
{
    public static function getStrength(): Strength
    {
        return new Strength(Strength::HIGH);
    }

    public static function isSupported(): bool
    {
        return file_exists('/dev/hwrng');
    }

    public function generate($size): string
    {
        $fp = fopen('/dev/hwrng', 'rb');
        $data = fread($fp, $size);
        fclose($fp);
        return str_pad($data, $size, chr(0));
    }
}

// Registration
$factory = new \RandomLib\Factory;
$factory->registerSource('hwrng', \MyApp\Source\HardwareRng::class);
```

### Custom Mixer

```php
namespace MyApp\Mixer;

use RandomLib\AbstractMixer;
use SecurityLib\Strength;

class MyMixer extends AbstractMixer
{
    public static function getStrength(): Strength
    {
        return new Strength(Strength::HIGH);
    }

    public static function test(): bool
    {
        return true;
    }

    protected function getPartSize(): int
    {
        return 32;
    }

    protected function mixParts1(string $p1, string $p2): string
    {
        return hash_hmac('sha256', $p1, $p2, true);
    }

    protected function mixParts2(string $p1, string $p2): string
    {
        return hash_hmac('sha256', $p2, $p1, true);
    }
}
```

---

## 9. Security Design Notes

**Defense in depth — source combination.**
The mixer receives one byte string from every source. The RFC 4086 mixing algorithm guarantees the output is at least as strong as the strongest source, provided the mixer itself is at least that
strong. A compromised or weak source does not reduce security below the level of the remaining sources.

**Rejection sampling for uniform distribution.**
Both `generateInt` and `generateString` use bit-masking and rejection loops rather than modulo reduction, preventing the bias that occurs when the range does not evenly divide the raw random space.

**Internal state hiding in `MicroTime`.**
Only the first 8 of 64 bytes from each SHA-512 round are emitted. This prevents reconstructing the internal state from observed outputs even when the hash function is known.

**OpenSSL version gating.**
The `OpenSSL` source downgrades to MEDIUM strength on unpatched PHP versions where `openssl_random_pseudo_bytes` had known seeding issues, ensuring the Factory does not include it in HIGH-strength
generator construction on those platforms.

**Modern PHP note.**
On PHP 7+, `random_bytes()` and `random_int()` provide cryptographically secure randomness natively and are preferred over this library. RandomLib's primary value on modern PHP is the
`generateString()` API and the multi-source defense-in-depth architecture, not the underlying entropy generation.

---

## 10. Full Public API Summary

```php
// Factory
new RandomLib\Factory()
$factory->getMediumStrengthGenerator(): Generator
$factory->getLowStrengthGenerator(): Generator
$factory->getHighStrengthGenerator(): Generator          // throws on PHP 7.2+
$factory->getGenerator(SecurityLib\Strength): Generator
$factory->registerMixer(string $name, string $class): void
$factory->registerSource(string $name, string $class): void
$factory->getMixers(): array
$factory->getSources(): array

// Generator
new RandomLib\Generator(Mixer $mixer, array $sources)
$gen->generate(int $size): string
$gen->generateInt(int $min = 0, int $max = PHP_INT_MAX): int
$gen->generateString(int $length, int|string $characters = ''): string
$gen->getMixer(): Mixer
$gen->getSources(): array

// Mixer interface
Mixer::getStrength(): SecurityLib\Strength    // static
Mixer::test(): bool                           // static
$mixer->mix(array $parts): string

// Source interface
Source::getStrength(): SecurityLib\Strength   // static
Source::isSupported(): bool                   // static
$source->generate(int $size): string

// Mixer constructors
new Mixer\Hash(string $hash = 'sha512')

// Source constructors
new Source\Sodium(bool $useLibsodium = true)
```

---

## 11. Comparison with krandom

| Feature                               | RandomLib (PHP)                 | krandom (Kotlin/Java)                |
|---------------------------------------|---------------------------------|--------------------------------------|
| Language                              | PHP 5.3+                        | Kotlin 2.1 / Java 21                 |
| Primitives (int, float, bool, string) | `generateInt`, `generateString` | `Generators.ofInt()`, etc.           |
| Typed random string generation        | ✅ (charset constants + bitmask) | ✅ (`StringGenerator.Builder`)        |
| Multi-source entropy mixing           | ✅ (core architecture)           | No                                   |
| Configurable security strength        | ✅ (LOW / MEDIUM / HIGH)         | No (always SecureRandom)             |
| Custom sources / mixers               | ✅ (register any class)          | No                                   |
| Object-graph population               | No                              | ✅ (`ObjectGenerator`)                |
| Seeded reproducibility                | No                              | Partial (per-generator seed)         |
| Luhn-valid strings                    | No                              | ✅ (`LuhnGenerator`)                  |
| Fibonacci sequences                   | No                              | ✅ (`FibonacciGenerator`)             |
| Dice / coin                           | No                              | ✅ (`DiceGenerator`, `CoinGenerator`) |
| IPv4                                  | No                              | ✅ (`IPv4Random`)                     |
| Person data (name, SSN, etc.)         | No                              | ✅ (Kotlin layer)                     |
| Hash generation                       | No                              | ✅ (`HexHashGenerator`)               |
| Natural / prime numbers               | No                              | ✅ (`NaturalNumberGenerator`)         |

### Equivalent generation patterns

| RandomLib                                             | krandom                                                                                  |
|-------------------------------------------------------|------------------------------------------------------------------------------------------|
| `$gen->generateInt(0, 100)`                           | `Generators.ofInt(0, 101).generate()`                                                    |
| `$gen->generateString(32, Generator::CHAR_ALNUM)`     | `Generators.ofString(StringGenerator.builder().length(32)).generate()`                   |
| `$gen->generateString(16, Generator::CHAR_LOWER_HEX)` | `Generators.ofString(StringGenerator.builder().length(16).lowercase().hex()).generate()` |
| `$gen->generate(32)` (raw bytes)                      | `Generators.ofByte().generateList(32)`                                                   |

---

## 12. Potential Additions for krandom Inspired by RandomLib

| Feature                         | RandomLib approach                                      | krandom gap                                                                           |
|---------------------------------|---------------------------------------------------------|---------------------------------------------------------------------------------------|
| Typed charset constants         | `CHAR_ALNUM`, `CHAR_UPPER_HEX`, `EASY_TO_READ` bitmasks | `StringGenerator.Builder` has basic pools but no bitmask API or `EASY_TO_READ` filter |
| Uniform `generateInt(min, max)` | Rejection sampling, modulo-bias-free                    | Already handled in `AbstractBoundedGenerator`                                         |
| Configurable strength tiers     | LOW / MEDIUM / HIGH generator factory                   | No equivalent — always uses `SecureRandom`                                            |
| Multi-source entropy pooling    | Core differentiator                                     | No equivalent                                                                         |
