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

## Structured person fixture

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(20260303L)
        .build();

PersonInfo person = Generators.ofPersonInfo(cfg).generate();
CompanyInfo company = Generators.ofCompanyInfo(cfg).generate();
JobInfo job = Generators.ofJobInfo(cfg).generate();
ProductInfo product = Generators.ofProductInfo(cfg).generate();
BankInfo bank = Generators.ofBankInfo(cfg).generate();
CreditCardInfo card = Generators.ofCreditCardInfo(cfg).generate();

assert person.username().equals(person.contact().email().split("@")[0]);
assert company.email().endsWith(company.website().replace("https://www.", ""));
assert job.title().contains(job.profession());
assert product.upc().matches("\\d{12}");
assert bank.routingNumber().matches("\\d{9}");
assert card.exp().matches("\\d{2}/\\d{2}");
```

## Batch order data with schema

```java
Field f = Generators.ofField(cfg);

Schema orders = Generators.ofSchema(cfg, Map.of(
        "orderId", f.bind("code.uuid"),
        "customer", f.bind("person.full_name"),
        "email", f.bind("person.email"),
        "currency", f.bind("finance.currency"),
        "amount", f.bind("finance.money"),
        "shipTo", f.bind("address.street_address")
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
