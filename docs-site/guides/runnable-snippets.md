---
layout: page
title: Runnable Snippets
permalink: /guides/runnable-snippets/
---

# Runnable Snippets

The snippets below are copy/paste-ready and target high-usage generator areas.

## Object generation

```java
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;

record Address(String city, String country) {}
record User(String name, Address address) {}

GeneratorConfig config = GeneratorConfig.builder()
        .objectMaxDepth(3)
        .build();

ObjectGenerator<User> generator = Generators.ofObject(User.class, config);
User user = generator.generate();
System.out.println(user);
```

## User data

```java
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;

GeneratorConfig config = GeneratorConfig.builder()
        .seed(42L)
        .build();

String name = new FullNameGenerator(config).generate();
String email = new EmailGenerator(config).generate();
System.out.println(name + " <" + email + ">");
```

## Location data

```java
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;

GeneratorConfig config = GeneratorConfig.builder()
        .locale(java.util.Locale.of("de", "DE"))
        .seed(42L)
        .build();

System.out.println(new CityGenerator(config).generate());
System.out.println(new CountryGenerator(config).generate());
```

## Finance data

```java
import io.github.frikit.krandom.generator.finance.CreditCardGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;

String amount = new MoneyGenerator().generate();
String card = new CreditCardGenerator().generate();

System.out.println(amount);
System.out.println(card);
```
