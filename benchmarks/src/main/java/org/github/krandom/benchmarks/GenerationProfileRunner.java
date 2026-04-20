/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.base.RegexGenerator;
import org.github.krandom.generator.object.ObjectGenerationSemanticMode;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Macro profiling utility for large-scale generation loops.
 *
 * <p>Designed for CI-optional runs and local performance tracking.
 */
public final class GenerationProfileRunner {

    private static final int[] RUN_SIZES = { 1_000, 10_000, 100_000, 1_000_000, 10_000_000 };
    private static final NumberFormat US_INT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private GenerationProfileRunner() {
    }

    public static void main(String[] args) {
        System.out.println("kRandom macro profile");
        System.out.println("====================");

        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(7L)
                                                .build();
        GeneratorConfig semanticObjectConfig = GeneratorConfig.builder()
                                                              .locale(Locale.US)
                                                              .seed(7L)
                                                              .objectMaxDepth(4)
                                                              .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                              .objectNullProbability(0.0)
                                                              .objectOptionalEmptyProbability(0.0)
                                                              .objectUniqueFields("id", "email", "username")
                                                              .objectUniquenessMaxAttempts(2_000)
                                                              .build();
        Schema schema = benchmarkSchema(config);
        CountingAppendable jsonlSink = new CountingAppendable();
        CountingAppendable csvSink = new CountingAppendable();

        profileCase("first-name", new FirstNameGenerator(config));
        profileCase("regex-ssn", new RegexGenerator("\\d{3}-\\d{2}-\\d{4}", 7L));
        profileCase("object-simple-user", Generators.ofObject(BenchmarkFixtures.SimpleUser.class));
        profileCase("object-semantic-customer", Generators.ofObject(BenchmarkFixtures.SemanticCustomer.class, semanticObjectConfig));
        profileCase("unique-email", Generators.unique(new EmailGenerator(config), 2_000));
        profileTask("schema-batch-250", () -> schema.generateBatch(250));
        profileTask("schema-jsonl-stream-250", () -> writeJsonLines(schema, jsonlSink, 250));
        profileTask("schema-csv-stream-250", () -> writeCsv(schema, csvSink, 250));
    }

    private static void profileCase(String label, Generator<?> generator) {
        profileTask(label, generator::generate);
    }

    private static void profileTask(String label, Runnable task) {
        System.out.println();
        System.out.println("Case: " + label);
        System.out.printf("%-10s %-14s %-14s%n", "count", "ops/sec", "heap-delta-mb");

        for (int runSize : RUN_SIZES) {
            // Warmup to reduce first-run noise.
            for (int i = 0; i < 10_000; i++) {
                task.run();
            }

            long beforeMemory = usedMemoryBytes();
            long started = System.nanoTime();
            for (int i = 0; i < runSize; i++) {
                task.run();
            }
            long elapsedNanos = System.nanoTime() - started;
            long afterMemory = usedMemoryBytes();

            double opsPerSecond = runSize / (elapsedNanos / 1_000_000_000.0);
            double heapDeltaMb = (afterMemory - beforeMemory) / (1024.0 * 1024.0);
            String formattedCount = US_INT_FORMAT.format(runSize);
            System.out.printf(Locale.US, "%-10s %-14.2f %-14.2f%n", formattedCount, opsPerSecond, heapDeltaMb);
        }
    }

    private static long usedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static Schema benchmarkSchema(GeneratorConfig config) {
        Field field = Generators.ofField(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("orderId", field.bind("code.uuid"));
        fields.put("customer", field.bind("person.full_name"));
        fields.put("email", field.bind("person.email"));
        fields.put("phone", field.bind("address.phone_number"));
        fields.put("currency", field.bind("finance.currency"));
        fields.put("amount", field.bind("finance.money"));
        fields.put("shipTo", field.bind("address.street_address"));
        fields.put("city", field.bind("address.city"));
        fields.put("state", field.bind("address.state"));
        fields.put("postalCode", field.bind("address.postal_code"));
        fields.put("createdAt", field.bind("datetime.timestamp"));
        fields.put("notes", field.bind("text.sentence"));
        return Generators.ofSchema(config, fields);
    }

    private static void writeJsonLines(Schema schema, CountingAppendable sink, int count) {
        try {
            sink.reset();
            schema.writeJsonLines(sink, count);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("CountingAppendable should not throw IOException", e);
        }
    }

    private static void writeCsv(Schema schema, CountingAppendable sink, int count) {
        try {
            sink.reset();
            schema.writeCsv(sink, count);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("CountingAppendable should not throw IOException", e);
        }
    }

    private static final class CountingAppendable implements Appendable {

        private long length;

        void reset() {
            length = 0L;
        }

        @Override
        public Appendable append(CharSequence csq) {
            length += csq == null ? 4 : csq.length();
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            CharSequence effective = csq == null ? "null" : csq;
            if (effective.length() < end) {
                throw new IndexOutOfBoundsException("append range exceeds sequence length");
            }
            length += end - start;
            return this;
        }

        @Override
        public Appendable append(char c) {
            length++;
            return this;
        }
    }
}
