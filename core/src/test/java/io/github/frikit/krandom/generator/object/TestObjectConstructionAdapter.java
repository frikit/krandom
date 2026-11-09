/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
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
                || type == ObjectConstructionPolicyTest.ExclusionAdapterFixture.class
                || type == ObjectConstructionPolicyTest.NullAdapterFixture.class
                || type == ObjectConstructionPolicyTest.WrongTypeAdapterFixture.class
                || type == ObjectConstructionPolicyTest.FailingAdapterFixture.class
                || type == ObjectConstructionPolicyTest.StructuredFailingAdapterFixture.class
                || SEALED_SUBCLASSES.containsKey(type)
                || type == ObjectGeneratorSealedTypeTest.AdapterJvmSealed.class;
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
        if (context.getType() == ObjectConstructionPolicyTest.ExclusionAdapterFixture.class) {
            return new ObjectConstructionPolicyTest.ExclusionAdapterFixture(
                    context.isExcluded("own"), context.isExcluded("inherited"), context.isExcluded("missing"));
        }
        boolean explicitlyOverridden = context.hasExplicitOverride("value", String.class);
        String value = (String) context.generate(String.class, String.class, "value", null);
        return new ObjectConstructionPolicyTest.AdapterFixture(value, explicitlyOverridden);
    }
}
