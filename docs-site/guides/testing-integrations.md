---
layout: page
title: Testing Integrations
permalink: /guides/testing-integrations/
---

# Testing Integrations

Use `GeneratorConfig` as the root test configuration, then create short-lived generators inside test
fixtures. This keeps locale, seed, object depth, and registry choices easy to audit.

## JUnit fixtures

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
Map<String, Object> row = orders.generate();

assert row.get("email").toString().contains("@");
```

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

For file-style fixtures, generate rows through `Schema` and serialize with Jackson:

```java
ObjectMapper mapper = KrandomJackson.newObjectMapper();

Field field = Generators.ofField(Locale.US);
Schema schema = Generators.ofSchema(Locale.US, Map.of(
        "order", field.bind("commerce.order_info"),
        "payment", field.bind("finance.payment_info")
));

String json = mapper.writeValueAsString(schema.generate());
```

`KrandomJackson.newObjectMapper()` also serializes `Schema` itself as JSON Schema, which is useful
when tests need both data rows and a contract document.
