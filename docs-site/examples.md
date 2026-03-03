---
layout: page
title: Examples
permalink: /examples/
---

# End-to-End Examples

## Test user fixture

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(20260303L)
        .build();

FullNameGenerator names = new FullNameGenerator(cfg);
EmailGenerator emails = new EmailGenerator(cfg);
PhoneNumberGenerator phones = new PhoneNumberGenerator(cfg);
StreetAddressGenerator addresses = new StreetAddressGenerator(cfg);

Map<String, Object> user = Map.of(
        "id", Generators.ofUuid().generate().toString(),
        "name", names.generate(),
        "email", emails.generate(),
        "phone", phones.generate(),
        "address", addresses.generate()
);
```

## Batch order data with schema

```java
Field f = Generators.ofField(Locale.US);

Schema orders = Generators.ofSchema(Locale.US, Map.of(
        "orderId", f.bind("code.uuid4"),
        "customer", f.bind("person.full_name"),
        "email", f.bind("person.email"),
        "currency", f.bind("finance.currency_iso_code"),
        "amount", f.bind("finance.price"),
        "shipTo", f.bind("address.street")
));

List<Map<String, Object>> batch = orders.generateBatch(50);
```

## Mixed random strategy in tests

```java
Generator<Integer> stableIds = Generators.ofInt(1000, 9999, 77L);
Generator<String> uniqueEmails = Generators.unique(Generators.ofEmail());

for (int i = 0; i < 20; i++) {
    int id = stableIds.generate();
    String email = uniqueEmails.generate();
    // assert / save
}
```
