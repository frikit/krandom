/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class SchemaOutputBenchmark {

    private static final int RECORD_COUNT = 250;

    @State(Scope.Thread)
    public static class SchemaState {
        public final Schema schema;
        public final CountingAppendable jsonlSink = new CountingAppendable();
        public final CountingAppendable csvSink = new CountingAppendable();

        public SchemaState() {
            GeneratorConfig config = GeneratorConfig.builder()
                                                   .locale(Locale.US)
                                                   .seed(7L)
                                                   .build();
            Field field = Generators.ofField(config);
            Map<String, org.github.krandom.generator.schema.SchemaValueProvider> fields = new LinkedHashMap<>();
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
            this.schema = Generators.ofSchema(config, fields);
        }
    }

    @Benchmark
    public List<Map<String, Object>> largeSchemaBatch(SchemaState state) {
        return state.schema.generateBatch(RECORD_COUNT);
    }

    @Benchmark
    public String largeSchemaJsonLines(SchemaState state) {
        return state.schema.toJsonLines(RECORD_COUNT);
    }

    @Benchmark
    public long streamingJsonLines(SchemaState state) throws IOException {
        state.jsonlSink.reset();
        state.schema.writeJsonLines(state.jsonlSink, RECORD_COUNT);
        return state.jsonlSink.length();
    }

    @Benchmark
    public long streamingCsv(SchemaState state) throws IOException {
        state.csvSink.reset();
        state.schema.writeCsv(state.csvSink, RECORD_COUNT);
        return state.csvSink.length();
    }

    private static final class CountingAppendable implements Appendable {

        private long length;

        void reset() {
            length = 0L;
        }

        long length() {
            return length;
        }

        @Override
        public Appendable append(CharSequence csq) {
            length += csq == null ? 4 : csq.length();
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            CharSequence effective = csq == null ? "null" : csq;
            length += end - start;
            if (effective.length() < end) {
                throw new IndexOutOfBoundsException("append range exceeds sequence length");
            }
            return this;
        }

        @Override
        public Appendable append(char c) {
            length++;
            return this;
        }
    }
}
