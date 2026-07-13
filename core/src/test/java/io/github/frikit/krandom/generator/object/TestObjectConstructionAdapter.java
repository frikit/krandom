/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;

/** Test-only service provider for the core construction-adapter contract. */
public final class TestObjectConstructionAdapter implements ObjectConstructionAdapter {

    static int calls;

    @Override
    public boolean supports(Class<?> type) {
        return type == ObjectConstructionPolicyTest.AdapterFixture.class
               || type == ObjectConstructionPolicyTest.NullAdapterFixture.class
               || type == ObjectConstructionPolicyTest.WrongTypeAdapterFixture.class
               || type == ObjectConstructionPolicyTest.FailingAdapterFixture.class
               || type == ObjectConstructionPolicyTest.StructuredFailingAdapterFixture.class;
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
        boolean explicitlyOverridden = context.hasExplicitOverride("value", String.class);
        String value = (String) context.generate(String.class, String.class, "value", null);
        return new ObjectConstructionPolicyTest.AdapterFixture(value, explicitlyOverridden);
    }
}
