# krandom end-to-end examples

Hands-on, runnable examples that show how to drive **krandom** to produce the
final JSON payload a real web page would submit. Each real-world form lives in
its own package so you can read one end to end:

| Package | Scenario | Locale |
|:---|:---|:---|
| `googlesignup` | "Create your Google Account" registration form | `en_GB` (UK) |
| `jobapplication` | A larger, nested job-application form | `de_DE` (Germany) |
| `spidregistration` | Italian **SPID** digital-identity enrolment (official AgID attribute set, real Codice Fiscale) | `it_IT` (Italy) |

Every example follows the same one-line shape:

1. **records** model the exact JSON the page POSTs;
2. a `fake(Locale, seed)` method fills them with a single call --
   `new ObjectFaker<>(Form.class, config).generate()` -- which resolves every
   field by name and type, **nested records and lists included**, seeded so the
   output is reproducible;
3. `toJson(...)` serializes to the final JSON via
   `KrandomJackson.newObjectMapper()`.

There is one pattern to remember: `new ObjectFaker<>(T.class, config)`, with an
optional `.ruleFor("field", ...)` to pin a specific field (the Google example
pins `agreeToTerms` to `true`).

## Run

```bash
./gradlew :examples-e2e:test           # verify every example produces valid JSON
```

Each form class also has a `main` that prints a sample payload.

## Add your own example

Create a new package under
`src/main/java/io/github/frikit/krandom/examples/e2e/<yourform>/`, model the
page's JSON as records, fill them with one `ObjectFaker` call, and add a case to
`E2eExamplesTest`. Pick whatever locale fits the scenario.
