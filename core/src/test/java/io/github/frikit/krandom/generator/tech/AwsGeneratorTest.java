/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AwsGenerator")
class AwsGeneratorTest {

    private static final Set<String> REGIONS = Set.of(
        "us-east-1", "us-west-2", "eu-west-1", "eu-central-1",
        "ap-southeast-1", "ap-northeast-1", "sa-east-1", "ca-central-1");

    @RepeatedTest(200)
    @DisplayName("generate and region return known AWS regions")
    void regionMembership() {
        AwsGenerator aws = new AwsGenerator();
        assertTrue(REGIONS.contains(aws.generate()));
        assertTrue(REGIONS.contains(aws.region()));
    }

    @RepeatedTest(200)
    @DisplayName("instanceId matches the EC2 instance-id format")
    void instanceIdFormat() {
        assertTrue(new AwsGenerator().instanceId().matches("i-[0-9a-f]{17}"));
    }

    @RepeatedTest(200)
    @DisplayName("s3Bucket matches the bucket-name format and length bounds")
    void s3BucketFormat() {
        String bucket = new AwsGenerator().s3Bucket();
        assertTrue(bucket.matches("[a-z0-9-]{3,20}"), bucket);
        assertTrue(bucket.length() >= 3 && bucket.length() <= 20, bucket);
    }

    @Test
    @DisplayName("region output shows variety over many draws")
    void regionVariety() {
        AwsGenerator aws = new AwsGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(aws.region());
        }
        assertTrue(seen.size() > 1, "expected more than one distinct region");
    }

    @Test
    @DisplayName("s3Bucket reaches both its minimum and maximum length over many draws")
    void s3BucketLengthExtremes() {
        AwsGenerator aws = new AwsGenerator(GeneratorConfig.builder().seed(42L).build());
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 5000; i++) {
            int len = aws.s3Bucket().length();
            min = Math.min(min, len);
            max = Math.max(max, len);
        }
        assertEquals(3, min);
        assertEquals(20, max);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new AwsGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new AwsGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new AwsGenerator(null));
    }

    @Test
    @DisplayName("facade ofAws produces valid regions and is seed-reproducible")
    void facade() {
        assertTrue(REGIONS.contains(Generators.ofAws().generate()));
        assertEquals(
            Generators.ofAws(GeneratorConfig.builder().seed(1L).build()).generateList(10),
            Generators.ofAws(GeneratorConfig.builder().seed(1L).build()).generateList(10));
    }
}
