/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.base.DigitGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RandomSourceContractTest's concurrent-caller test on virtual threads, which need Java 21. */
@DisplayName("random source contract across generator families, on virtual threads")
class RandomSourceVirtualThreadTest {

    @Test
    @DisplayName("shared Random stays coherent but logical replay follows concurrent call order")
    void sharedRandomIsSafeButReplayDependsOnCallOrder() throws Exception {
        int callCount = 64;
        GeneratorConfig expectedConfig =
                GeneratorConfig.builder().random(new Random(42L)).build();
        List<String> expected = new DigitGenerator(expectedConfig).generateList(callCount);

        GeneratorConfig concurrentConfig =
                GeneratorConfig.builder().random(new Random(42L)).build();
        DigitGenerator shared = new DigitGenerator(concurrentConfig);
        CountDownLatch ready = new CountDownLatch(callCount);
        List<CountDownLatch> permits = new ArrayList<>(callCount);
        for (int i = 0; i < callCount; i++) {
            permits.add(new CountDownLatch(1));
        }

        List<String> observed = new ArrayList<>(Collections.nCopies(callCount, null));
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> calls = new ArrayList<>(callCount);
            for (int i = 0; i < callCount; i++) {
                int logicalIndex = i;
                calls.add(executor.submit(() -> {
                    ready.countDown();
                    permits.get(logicalIndex).await();
                    return shared.generate();
                }));
            }

            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS), "concurrent callers did not become ready");
                for (int i = callCount - 1; i >= 0; i--) {
                    permits.get(i).countDown();
                    observed.set(i, calls.get(i).get(5, TimeUnit.SECONDS));
                }
            } finally {
                permits.forEach(CountDownLatch::countDown);
            }
        }

        List<String> expectedInReverseCallOrder = new ArrayList<>(expected);
        Collections.reverse(expectedInReverseCallOrder);
        assertEquals(expectedInReverseCallOrder, observed);
    }
}
