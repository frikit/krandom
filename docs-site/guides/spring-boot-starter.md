---
layout: page
title: Spring Boot Starter
permalink: /guides/spring-boot-starter/
---

# Spring Boot Starter

Use `krandom-spring-boot-starter` when a Spring Boot 4.x application should get a shared kRandom configuration and ready-to-inject generators from application properties.

The starter is built against Spring Boot 4.x and exposes `spring-boot-autoconfigure` transitively, so consumer applications must be on Spring Boot 4.x as well.

## Dependency

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-spring-boot-starter:2.3.0")
}
```

Latest version: see [GitHub Releases](https://github.com/frikit/krandom/releases).

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

### Replay, clock, and safety properties

```properties
# replay a recorded recipe (mutually exclusive with krandom.seed / krandom.locale)
krandom.recipe=base64:Zm9ybWF0PWtyYW5kb20t...

# pin generation time
krandom.clock=2026-01-01T00:00:00Z
krandom.clock-zone=Europe/Berlin

# explicit safety and construction policies (relaxed enum names)
krandom.banking-safety-policy=realistic-unclassified
krandom.national-id-safety-policy=realistic-unclassified
krandom.object-construction-policy=safe
```

Invalid combinations fail at context startup with an actionable message: a recipe combined with
`krandom.seed`/`krandom.locale`, a `krandom.clock-zone` without `krandom.clock`, or malformed
recipe/clock values.

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

## The `@KrandomTest` Slice

`@KrandomTest` is a self-contained test slice: it bootstraps the Spring TestContext framework by
itself (no extra `@ExtendWith` or `@SpringBootTest`), disables full application
auto-configuration, and starts a context containing only the three kRandom beans above.

```java
import io.github.frikit.krandom.spring.KrandomTest;
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@KrandomTest
@TestPropertySource(properties = "krandom.seed=42")
class UserFixtureTest {

    @Autowired
    KrandomObjectFakerFactory factory;

    @Test
    void generatesSeededUsers() {
        UserDto user = factory.generator(UserDto.class).generate();
        assertNotNull(user.getFirstName());
    }
}
```

Like Spring Boot's own slices, `@KrandomTest` needs a `@SpringBootConfiguration` class — your
application class in a parent package, or a nested `@SpringBootConfiguration` in the test — and
the Spring Boot test libraries on the test classpath (`spring-boot-starter-test` provides them).
`krandom.*` properties bind the same way as in the full application context.
