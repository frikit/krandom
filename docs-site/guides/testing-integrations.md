---
layout: page
title: Testing Integrations
permalink: /guides/testing-integrations/
---

# Testing Integrations

Use `GeneratorConfig` as the root test configuration, then create short-lived generators inside test
fixtures. This keeps locale, seed, object depth, and registry choices easy to audit.

## JUnit fixtures

For per-test seed management with automatic failure-seed reporting, use the dedicated
`krandom-junit` extension — see the [JUnit Extension]({{ '/guides/junit-extension/' | relative_url }})
guide. The manual pattern below remains the right fit when one shared config drives many fixtures:

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(20260424L)
        .build();

Field field = Generators.ofField(cfg);
Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("id", field.bind("code.uuid"));
fields.put("email", field.bind("person.email"));
fields.put("amount", field.bind("finance.money"));

Schema orders = Generators.ofSchema(cfg, fields);
Map<String, @Nullable Object> row = orders.nextRecord();

assert String.valueOf(row.get("email")).contains("@");
```

`nextRecord()` is new in 3.1.0; on 3.0.x, call `generate()`, which returns the same record.

## Spring-style fixture factory

```java
final class TestFixtureFactory {

    private final GeneratorConfig config;

    TestFixtureFactory(Locale locale, long seed) {
        this.config = GeneratorConfig.builder()
                .locale(locale)
                .seed(seed)
                .build();
    }

    OrderDto order() {
        return Generators.ofObject(OrderDto.class, config).generate();
    }

    PaymentInfo payment() {
        return Generators.ofPaymentInfo(config).generate();
    }
}
```

Create one factory per test class or test method if deterministic sequence isolation matters. Do not
share one mutable generator instance across parallel tests.

## JSON fixtures

For file-style fixtures, generate rows through `Schema` and serialize with Jackson 3:

```java
JsonMapper mapper = KrandomJackson.newJsonMapper();

Field field = Generators.ofField(Locale.US);
Schema schema = Generators.ofSchema(Locale.US, Map.of(
        "order", field.bind("commerce.order_info"),
        "payment", field.bind("finance.payment_info")
));

String json = mapper.writeValueAsString(schema.nextRecord());
```

As above, on 3.0.x call `generate()`, which returns the same record as `nextRecord()`.

`KrandomJackson.newJsonMapper()` also serializes `Schema` itself as JSON Schema, which is useful
when tests need both data rows and a contract document.
