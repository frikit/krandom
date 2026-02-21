# Documentation

## Feature Parity

Comparison of krandom capabilities vs similar libraries:

- [feature-parity-bogus.md](feature-parity/feature-parity-bogus.md) - Detailed comparison with Bogus (.NET)

## Similar Libraries Reference

Comparative references and API deep-dives for popular random/fake data libraries across languages. Each doc covers: purpose, installation, core API, provider catalogue, and a gap analysis against kRandom.

| Doc | Language | Library | Notes |
|:---|:---:|:---|:---|
| [easy-random-reference.md](similar-libs-reference/easy-random-reference.md) | Java | [easy-random](https://github.com/j-easy/easy-random) | Object-graph population; annotation-driven |
| [datafaker-reference.md](similar-libs-reference/datafaker-reference.md) | Java | [DataFaker](https://github.com/datafaker-net/datafaker) | Successor to JavaFaker; 200+ providers, 60+ locales |
| [chancejs-reference.md](similar-libs-reference/chancejs-reference.md) | JavaScript | [Chance.js](https://chancejs.com) | Seeded; broad primitive and domain coverage |
| [lorem-reference.md](similar-libs-reference/lorem-reference.md) | JavaScript | [lorem (mdeanda)](https://github.com/mdeanda/lorem) | Lightweight Lorem Ipsum generator |
| [faker-python-reference.md](similar-libs-reference/faker-python-reference.md) | Python | [Faker](https://github.com/joke2k/faker) | 80+ locales; 23 provider groups |
| [mimesis-reference.md](similar-libs-reference/mimesis-reference.md) | Python | [Mimesis](https://github.com/lk-geimfari/mimesis) | ~10–15× faster than Faker; Schema bulk API |
| [bogus-dotnet-reference.md](similar-libs-reference/bogus-dotnet-reference.md) | C# | [Bogus](https://github.com/bchavez/Bogus) | Fluent `Faker<T>`; 70+ locales |
| [gofakeit-reference.md](similar-libs-reference/gofakeit-reference.md) | Go | [gofakeit](https://github.com/brianvoe/gofakeit) | 310+ functions; struct tag generation |
| [fake-rs-reference.md](similar-libs-reference/fake-rs-reference.md) | Rust | [fake-rs](https://github.com/cksac/fake-rs) | `Fake` / `Dummy` traits; derive macro |
| [randomlib-reference.md](similar-libs-reference/randomlib-reference.md) | PHP | [RandomLib](https://github.com/ircmaxell/RandomLib) | Cryptographic-strength random bytes |

## Implementation Documentation

Architecture and implementation details:

- [code-based-locale-architecture.md](ideas/code-based-locale-architecture.md) - Design for embedded locale support
- [generatorconfig-locale-integration.md](ideas/generatorconfig-locale-integration.md) - Locale integration in GeneratorConfig
- [locale-support-investigation.md](ideas/locale-support-investigation.md) - Research on locale implementation patterns
- [locale-implementation-phase1-summary.md](ideas/locale-implementation-phase1-summary.md) - Summary of locale field addition
- [title-generator-implementation-summary.md](ideas/title-generator-implementation-summary.md) - TitleGenerator implementation details
