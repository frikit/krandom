---
layout: page
title: Writing extension modules
permalink: /guides/writing-modules/
---

# Writing extension modules

Use a `KRandomModule` when one domain provider must work consistently across provider lookup,
schema generation, and semantic object generation. Modules are explicit and scoped to one
`GeneratorConfig`: kRandom does not scan the classpath or mutate a process-wide registry.

```java
final class ShippingModule implements KRandomModule {
    @Override
    public String id() {
        return "acme.shipping";
    }

    @Override
    public void configure(KRandomModuleContext context) {
        context.registerProvider(
            ProviderDescriptor.builder("shipping.tracking", TrackingGenerator.class,
                                       TrackingGenerator::new)
                .aliases("tracking")
                .semanticKeys("trackingcode")
                .schemaProjection(ProviderSchemaProjection
                    .builder("shipping.tracking_code",
                             (TrackingGenerator provider, GeneratorConfig config) ->
                                 provider.generate())
                    .aliases("tracking_code")
                    .build())
                .build());
        context.registerSemanticAliases("trackingcode", "trackingCode");
    }
}

GeneratorConfig config = GeneratorConfig.builder()
        .locale(Locale.UK)
        .install(new ShippingModule())
        .build();
```

The resulting configuration makes the contribution available through `ProviderHub`,
`FieldLookup`, and provider-backed semantic fields in `ObjectGenerator`/`ObjectFaker`.
`toBuilder()` preserves installed modules.

## Module rules

- Give every module a stable, namespaced ID.
- Use namespaced canonical provider and schema names; aliases are conveniences, not ownership.
- Declare safety metadata conservatively. Unclassified means the module makes no claim.
- Keep `configure()` deterministic and free of global side effects; it runs for each built or
  derived configuration.
- Treat conflicts as configuration errors. Duplicate module IDs, provider keys/aliases, schema
  references/aliases, and ambiguous semantic aliases fail during `build()`.
- Do not perform network access during module configuration or generation unless the application
  explicitly owns that behavior. First-party kRandom modules are local and offline by default.

Use direct `ProviderHub.register()` only for a short-lived, hub-local provider that does not need
schema or object-generation integration. Use a module for reusable library or organization-wide
extensions.
