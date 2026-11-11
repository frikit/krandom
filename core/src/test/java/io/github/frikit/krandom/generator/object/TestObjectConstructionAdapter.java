/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Test-only service provider for the core construction-adapter contract. */
public final class TestObjectConstructionAdapter implements ObjectConstructionAdapter {

    static int calls;

    /** The subclasses this adapter declares for types that the JVM does not see as sealed. */
    private static final Map<Class<?>, List<Class<?>>> SEALED_SUBCLASSES = Map.of(
            ObjectGeneratorSealedTypeTest.AdapterSealed.class,
            List.of(ObjectGeneratorSealedTypeTest.AdapterLeaf.class),
            ObjectGeneratorSealedTypeTest.AdapterEmpty.class,
            List.of(),
            ObjectGeneratorSealedTypeTest.AdapterUnrelated.class,
            List.of(ObjectGeneratorSealedTypeTest.AdapterLeaf.class),
            ObjectGeneratorSealedTypeTest.AdapterSelf.class,
            List.of(ObjectGeneratorSealedTypeTest.AdapterSelf.class));

    @Override
    public boolean supports(Class<?> type) {
        return type == ObjectConstructionPolicyTest.AdapterFixture.class
                || type == ObjectConstructionPolicyTest.AdapterBodyFixture.class
                || type == ObjectConstructionPolicyTest.AdapterDefaultFixture.class
                || type == ObjectConstructionPolicyTest.AdapterShadowFixture.class
                || type == ObjectConstructionPolicyTest.AdapterSingletonFixture.class
                || type == ObjectConstructionPolicyTest.AdapterCompanionHolder.Companion.class
                || type == ObjectConstructionPolicyTest.AdapterSharedView.class
                || type == ObjectConstructionPolicyTest.InitHost.Child.class
                || type == ObjectConstructionPolicyTest.ExclusionAdapterFixture.class
                || type == ObjectConstructionPolicyTest.NullAdapterFixture.class
                || type == ObjectConstructionPolicyTest.WrongTypeAdapterFixture.class
                || type == ObjectConstructionPolicyTest.FailingAdapterFixture.class
                || type == ObjectConstructionPolicyTest.StructuredFailingAdapterFixture.class
                || SEALED_SUBCLASSES.containsKey(type)
                || type == ObjectGeneratorSealedTypeTest.AdapterJvmSealed.class
                || type.getName().equals("NamelessCode");
    }

    @Override
    public Optional<List<Class<?>>> sealedSubclasses(Class<?> type) {
        return Optional.ofNullable(SEALED_SUBCLASSES.get(type));
    }

    @Override
    public Object construct(ObjectConstructionContext<?> context) {
        calls++;
        context.getType();
        context.getPath();
        context.getOwnerType();
        context.getDepth();
        if (context.getType() == ObjectConstructionPolicyTest.NullAdapterFixture.class) {
            return null;
        }
        if (context.getType() == ObjectConstructionPolicyTest.WrongTypeAdapterFixture.class) {
            return "wrong-type";
        }
        if (context.getType() == ObjectConstructionPolicyTest.FailingAdapterFixture.class) {
            throw new IllegalStateException("adapter failed");
        }
        if (context.getType() == ObjectConstructionPolicyTest.StructuredFailingAdapterFixture.class) {
            throw new ObjectGenerationException("structured adapter failure");
        }
        if (context.getType().getName().equals("NamelessCode")) {
            return constructNameless(context);
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterDefaultFixture.class) {
            return constructWithDefault(context);
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterShadowFixture.class) {
            boolean overridden = context.hasExplicitOverride("code", int.class);
            Object code = context.generate(
                    int.class,
                    int.class,
                    "code",
                    ObjectConstructionPolicyTest.AdapterShadowFixture.class
                            .getDeclaredConstructors()[0]
                            .getParameters()[0]);
            return new ObjectConstructionPolicyTest.AdapterShadowFixture((Integer) code, overridden);
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterBodyFixture.class) {
            return new ObjectConstructionPolicyTest.AdapterBodyFixture(
                    (String) context.generate(String.class, String.class, "value", null));
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterSingletonFixture.class) {
            return ObjectConstructionPolicyTest.AdapterSingletonFixture.INSTANCE;
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterCompanionHolder.Companion.class) {
            return ObjectConstructionPolicyTest.AdapterCompanionHolder.Companion;
        }
        if (context.getType() == ObjectConstructionPolicyTest.AdapterSharedView.class) {
            return ObjectConstructionPolicyTest.AdapterSharedView.INSTANCE;
        }
        if (context.getType() == ObjectConstructionPolicyTest.InitHost.Child.class) {
            return new ObjectConstructionPolicyTest.InitHost.Child();
        }
        if (context.getType() == ObjectConstructionPolicyTest.ExclusionAdapterFixture.class) {
            return new ObjectConstructionPolicyTest.ExclusionAdapterFixture(
                    context.isExcluded("own"), context.isExcluded("inherited"), context.isExcluded("missing"));
        }
        boolean explicitlyOverridden = context.hasExplicitOverride("value", String.class);
        String value = (String) context.generate(String.class, String.class, "value", null);
        return new ObjectConstructionPolicyTest.AdapterFixture(value, explicitlyOverridden);
    }

    /**
     * Keeps a default argument unless an override targets its parameter, as the Kotlin adapter does. A parameter
     * that no field backs, such as {@code unbacked}, has no field for a predicate override to match.
     */
    private static Object constructWithDefault(ObjectConstructionContext<?> context) {
        Constructor<?> constructor =
                ObjectConstructionPolicyTest.AdapterDefaultFixture.class.getDeclaredConstructors()[0];
        boolean unbackedOverridden = context.hasExplicitOverride("unbacked", String.class);
        String name = context.hasExplicitOverride("name", String.class)
                ? (String) context.generate(
                        String.class, String.class, "name", constructor.getParameters()[0])
                : "default";
        Object inner = context.generate(
                ObjectConstructionPolicyTest.AdapterInner.class,
                ObjectConstructionPolicyTest.AdapterInner.class,
                "inner",
                constructor.getParameters()[2]);
        return new ObjectConstructionPolicyTest.AdapterDefaultFixture(
                name, unbackedOverridden, (ObjectConstructionPolicyTest.AdapterInner) inner);
    }

    /**
     * Names the parameters of a class compiled without them, as the Kotlin adapter does from metadata. The second
     * argument is read from a parameter of another class, which keeps the field its own name gives it.
     */
    private static Object constructNameless(ObjectConstructionContext<?> context) {
        try {
            Constructor<?> constructor = context.getType().getDeclaredConstructors()[0];
            Object code = context.generate(
                    String.class, String.class, "code", constructor.getParameters()[0]);
            Object note = context.generate(
                    String.class,
                    String.class,
                    "note",
                    StringBuilder.class.getConstructor(String.class).getParameters()[0]);
            return constructor.newInstance(code, note);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
