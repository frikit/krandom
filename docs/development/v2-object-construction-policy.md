# V2 Object Construction Policy

**Status:** Complete
**Scope:** Java object construction in `ObjectGenerator`

## Goal

Object generation must create instances under an explicit, diagnosable policy. Safe generation runs
constructors and preserves their invariants. Constructor bypass remains available only as a named
unsafe compatibility choice; it is never an invisible fallback.

## Policy contract

- `SAFE_CONSTRUCTORS` is the default.
- Records use their canonical constructor.
- Plain classes prefer an accessible no-argument constructor.
- Without a no-argument constructor, one unambiguous declared constructor may be resolved from the
  same type/annotation pipeline used for fields.
- Multiple candidate constructors require a factory override or explicit unsafe bypass.
- `UNSAFE_CONSTRUCTOR_BYPASS` preserves the legacy Objenesis fallback when no no-argument
  constructor exists. It may skip initializers and invariants and is deliberately named as unsafe.
- Existing field initializers are preserved unless `objectOverrideDefaultInitialization(true)` is
  configured.

## Stage 1: Public policy and default

**Goal:** Add one construction-policy setting to the root configuration and make safe construction
the default.

**Success Criteria:** The policy round-trips through builders/config copies; null is rejected; the
default is `SAFE_CONSTRUCTORS`; unsafe bypass requires an explicit builder call.

**Tests:** Root and object-config mapping tests plus API compatibility verification.

**Status:** Complete

`ObjectConstructionPolicy` is now public and defaults to `SAFE_CONSTRUCTORS` in `GeneratorConfig`.
The policy survives builder copies and the internal object-configuration projection. Null policies
are rejected. `UNSAFE_CONSTRUCTOR_BYPASS` must be selected explicitly.

## Stage 2: Deterministic constructor selection

**Goal:** Invoke constructors with generated arguments instead of bypassing them.

**Success Criteria:** Records remain canonical; no-argument constructors remain preferred; one
declared constructor is resolved deterministically; private constructors work when the package is
open; ambiguous, abstract, interface, local, anonymous, and non-static inner types fail contextually.

**Tests:** Constructor invariants, annotations on parameters, inherited mutable fields, private and
throwing constructors, ambiguity, abstract/interface roots, and nested failures.

**Status:** Complete

No-argument constructors remain first choice. When one is absent, safe mode invokes one declared
constructor and resolves its parameters through `FieldGeneratorResolver`; private constructors and
parameter annotations therefore use the same type and constraint rules as fields. Ambiguous
constructor sets fail with root construction context naming `SAFE_CONSTRUCTORS`. Unsafe mode keeps
the legacy no-constructor path and deliberately skips the declared constructor.

Constructor parameter annotations are normalized through the same Bean Validation contract as
fields. Abstract, interface, array, primitive, enum, annotation, local, anonymous, and non-static
inner root types fail before allocation with the selected policy in their construction cause. The
anonymous-type path uses its binary name so structured failure context is never blank.

## Stage 3: Factories and unwritable state

**Goal:** Give callers a safe escape hatch and reject objects that cannot be completed under the
selected policy.

**Success Criteria:** Explicit root/nested factories win before reflection; final state is supplied
through constructors/factories in safe mode; default field initialization is preserved unless the
existing override option is enabled; unsafe allocation is named in relevant diagnostics.

**Tests:** Root/nested factories, final fields, initialized fields, unsafe compatibility behavior,
strict/lenient diagnostics, and cycles.

**Status:** Complete

Existing plain and contextual type overrides now serve as factories for root objects as well as
nested fields. Contextual factories receive `"$root"`, the requested type, and depth zero and win
over plain factories. Factory output must be non-null and assignable; failures are categorized as
`CUSTOM_GENERATOR`, with strict throwing or lenient null plus a sanitized diagnostic. This permits
safe construction of interface/abstract roots without adding a duplicate factory registry.

Safe constructor paths preserve final state established by constructors, while factory-created
immutable values bypass reflection entirely. Mutable-field accessibility is preflighted before
constructor invocation. Constructor/default initializers remain untouched unless
`objectOverrideDefaultInitialization(true)` is selected. Unsafe bypass remains an explicit policy
that may skip final-state invariants.

## Stage 4: Consumer and JPMS contract

**Goal:** Publish the selection table and verify it from consumer-shaped tests.

**Success Criteria:** Public documentation names every path and risk; required `opens` behavior is
explicit; Java consumer tests cover safe/unsafe construction from locally published artifacts; the
master plan and migration notes match implementation.

**Tests:** Full Java 21 pre-commit gate, API compatibility, JPMS consumer compilation/runtime checks,
and deterministic construction-policy matrix.

**Status:** Complete

The public object-generation guide now publishes the full selection matrix, factory contract,
unsafe risk, initializer behavior, and qualified `opens` requirement. `krandom-core` has an
explicit `io.github.frikit.krandom` descriptor so its Java/platform/runtime requirements resolve
without leaking implementation dependencies into consumer module descriptors.

Two executable Java 21 named-module consumers verify the boundary against the locally published
artifact. The open consumer runs a private constructor and populates a private field. The closed
consumer receives sanitized `REFLECTION` context and the exact
`opens <package> to io.github.frikit.krandom;` directive. Core resource access is anchored to the
owning class, so packaged locale datasets remain readable under strong encapsulation.
