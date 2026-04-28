---
layout: page
title: Spring Boot Starter
permalink: /guides/spring-boot-starter/
---

# Spring Boot Starter

Use `krandom-spring-boot-starter` when a Spring Boot 3.x application should get a shared kRandom configuration and ready-to-inject generators from application properties.

## Dependency

After the public release is cut, use Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-spring-boot-starter:<version>")
}
```

Until then, verify starter consumers with the Maven-local workflow from the repository.

## Application Properties

All properties are optional. If omitted, kRandom uses US locale defaults and a non-deterministic random source.

```properties
krandom.seed=42
krandom.locale=en-US
krandom.object-max-depth=3
krandom.object-null-probability=0.1
krandom.min-string-length=2
krandom.max-string-length=24
krandom.min-collection-size=1
krandom.max-collection-size=4
```

`krandom.locale` accepts BCP 47 tags such as `en-US` and underscore tags such as `en_US`.

## Injected Beans

The starter registers these beans only when the application has not already provided one:

- `GeneratorConfig`
- `ProviderHub`
- `KrandomObjectFakerFactory`

## Usage

```java
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import org.springframework.stereotype.Service;

@Service
class DemoDataService {
    private final ProviderHub providers;
    private final KrandomObjectFakerFactory fakers;

    DemoDataService(ProviderHub providers, KrandomObjectFakerFactory fakers) {
        this.providers = providers;
        this.fakers = fakers;
    }

    String email() {
        return providers.get("person.email", Generator.class).generate().toString();
    }

    UserDto user() {
        return fakers.generator(UserDto.class).generate();
    }
}
```

Override any bean when an application needs a custom `GeneratorConfig`, `ProviderHub`, or object faker factory.
