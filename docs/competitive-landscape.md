# Competitive Landscape

**Reviewed:** 2026-08-27

This is a product-direction summary, not an API-parity promise. Competitor surfaces change; use the
linked official documentation for their current APIs and the kRandom guides for runnable examples.

| Project | Strength to respect | kRandom direction | Migration guide |
|:---|:---|:---|:---|
| [DataFaker](https://www.datafaker.net/documentation/providers/) | Broad provider catalog, expressions, schemas, and multiple transformers | Prefer curated test-safe providers, coherent fixtures, replay, local verified packs, and explicit safety metadata over catalog count | [Guide](migration/from-datafaker.md) |
| [Instancio](https://www.instancio.org/user-guide/) | Rich selector model, reusable models, precedence rules, and JUnit integration | Complete one strict Java/Kotlin fixture-control model and value-sanitized explainability without hidden global state | [Guide](migration/from-instancio.md) |
| [Easy Random](https://github.com/j-easy/easy-random) | Small object-generation entry point and familiar customization model | Preserve one-call object generation while providing semantic defaults, replay, constraints, and maintained integrations | [Guide](migration/from-easyrandom.md) |
| [JavaFaker](https://github.com/DiUS/java-faker) | Familiar legacy Faker API | Keep a direct migration path to maintained kRandom coordinates and safety-aware configuration | [Guide](migration/from-javafaker.md) |

## Current product gaps worth funding

- One documented precedence and scoping model across typed paths, predicates, collections, and
  reusable object models.
- Strict diagnostics for unused, ambiguous, and shadowed fixture rules.
- Value-sanitized explainability and portable failure replay across standalone, JUnit, Kotest,
  Kotlin, Spring, object, and schema entry points.
- A generalized, offline, verified data-pack contract and external extension compatibility kit.
- More clean-consumer and real-project migration evidence.

Provider-count parity, runtime classpath scanning, live network data, and unmaintained novelty
catalogs are not core goals. Detailed priorities and the v3 release gate are in the
[`Product Roadmap`](development/market-leadership-roadmap.md).
