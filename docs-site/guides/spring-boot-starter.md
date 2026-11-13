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
    implementation("io.github.frikit:krandom-spring-boot-starter:3.1.0")
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
krandom.network-safety-policy=test-safe-reserved
krandom.object-construction-policy=safe
```

`krandom.recipe` is exclusive (2.6+): the recipe already records the seed, locale, clock, string
and collection bounds, object settings, and safety and construction policies, so every other
`krandom.*` generation property above is rejected next to it instead of silently overriding part of
the recorded configuration. The error names each conflicting property; blank values count as unset.

Invalid combinations fail at context startup with an actionable message: a recipe combined with any
property it defines, a `krandom.clock-zone` without `krandom.clock`, or malformed recipe/clock
values.

When the resulting configuration is seed-owned, the starter logs its effective recipe at `INFO` on
startup, including a ready-to-paste `krandom.recipe=base64:...` value. Without `krandom.clock` the
recorded clock instant is the startup time; pin `krandom.clock` when replay must reproduce
time-relative values.

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

### Seeded factories and named streams

Every `fakers.generator(type)` and `fakers.faker(type)` call starts a new instance on the
configuration's root seed. With `krandom.seed` set, each call therefore restarts the same sequence,
and a request handler that creates a generator per request returns the same object every time.
Reuse one generator for a sequence of distinct objects, or name a child stream (2.6+):

```java
UserDto user = fakers.generator(UserDto.class, "request-" + requestId).generate();
```

A named stream keeps every other setting and uses the child seed
`GenerationRecipe.deriveChildSeed(seed, streamName)`, so the same name always reproduces the same
sequence while different names are independent. An unseeded configuration ignores the name.

Composite generators give each child generator its own stream by default (2.11+), as described in
[composite child streams]({{ '/guides/primitives-and-determinism/' | relative_url }}#composite-child-streams).
The starter has no property for that policy: to get the earlier child streams back, define your own
`GeneratorConfig` bean with `childStreamPolicy(ChildStreamPolicy.LEGACY)`, or replay a `krandom.recipe`
that records `setting.child-stream-policy=LEGACY`.

### Registering providers

Register custom providers once, when the hub bean is created, with a `KrandomProviderCustomizer`
bean (2.6+). Customizers run in `@Order` order before the hub is injected anywhere:

```java
@Bean
KrandomProviderCustomizer orderNumbers() {
    return hub -> hub.register("order.number",
                               config -> Generators.ofProviderTemplate("ORD-#####", config));
}
```

Tests should declare customizers in a `@TestConfiguration` rather than calling `hub.register(...)`
in `@BeforeEach`: the Spring TestContext framework caches the context, so the same hub is shared by
later tests and a second registration fails with `Provider already registered`. If a test must
replace a provider at runtime, register it with `ConflictPolicy.REPLACE`. Customizers apply only to
the auto-configured hub; an application-defined `ProviderHub` bean is used as is.

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

Also like those slices, `@KrandomTest` registers a type-exclude filter (2.6+): the `@Service`,
`@Repository`, `@Component`, and `@Configuration` classes found by your
`@SpringBootApplication`'s component scan are not loaded, because with auto-configuration disabled
they would fail on missing infrastructure such as data sources or task executors. Add the
application beans a test needs explicitly:

```java
@KrandomTest
@Import(PricingService.class)
class PricingFixtureTest {

    @Autowired
    PricingService pricing;
}
```

A nested `@TestConfiguration` works as well and is the natural place for `KrandomProviderCustomizer`
beans.

## Jackson version

The starter itself does not depend on Jackson. Spring Boot 4 defaults to **Jackson 3**
(`tools.jackson`), and the separate `krandom-jackson` module is a Jackson 3 module too. When Spring
Boot auto-configures Jackson (`spring-boot-jackson`), it builds a `JsonMapper` and registers every
`JacksonModule` bean with it, so a `KrandomJacksonModule` bean makes Boot's mapper serialize kRandom
schemas as JSON Schema:

```java
@Configuration(proxyBeanMethods = false)
class KrandomJacksonConfiguration {

    @Bean
    KrandomJacksonModule krandomJacksonModule() {
        return new KrandomJacksonModule();
    }
}
```

Outside Spring, `KrandomJackson.newJsonMapper()` builds such a mapper. kRandom 2.10.0 is the last
release whose `krandom-jackson` targets Jackson 2. See
[Jackson Integration]({{ '/guides/jackson-integration/' | relative_url }}).
