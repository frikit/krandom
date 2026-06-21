/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates Amazon Web Services (AWS) resource identifiers such as regions, EC2 instance IDs, and
 * S3 bucket names.
 *
 * <p>{@link #generate()} returns a random AWS region, e.g. {@code "us-east-1"}.
 *
 * <pre>{@code
 *   AwsGenerator aws = new AwsGenerator();
 *   String region  = aws.region();     // e.g. "eu-west-1"
 *   String instance = aws.instanceId(); // e.g. "i-0a1b2c3d4e5f60718"
 *   String bucket   = aws.s3Bucket();   // e.g. "k7x-data-bucket"
 * }</pre>
 */
public final class AwsGenerator implements Generator<String> {

    private static final String[] REGIONS = {
        "us-east-1", "us-west-2", "eu-west-1", "eu-central-1",
        "ap-southeast-1", "ap-northeast-1", "sa-east-1", "ca-central-1"
    };

    private static final String HEX = "0123456789abcdef";

    private static final String BUCKET_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-";

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public AwsGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public AwsGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random AWS region.
     *
     * @return a region such as {@code "us-east-1"}; never {@code null}
     */
    @Override
    public String generate() {
        return region();
    }

    /**
     * Generates a uniformly random AWS region.
     *
     * @return a region such as {@code "eu-central-1"}; never {@code null}
     */
    public String region() {
        return REGIONS[random.nextInt(REGIONS.length)];
    }

    /**
     * Generates an EC2 instance ID: the prefix {@code "i-"} followed by 17 lowercase hex characters
     * (matching {@code i-[0-9a-f]{17}}).
     *
     * @return an instance ID such as {@code "i-0a1b2c3d4e5f60718"}; never {@code null}
     */
    public String instanceId() {
        StringBuilder sb = new StringBuilder(19);
        sb.append("i-");
        for (int i = 0; i < 17; i++) {
            sb.append(HEX.charAt(random.nextInt(HEX.length())));
        }
        return sb.toString();
    }

    /**
     * Generates an S3 bucket name: a lowercase alphanumeric-and-hyphen string of 3 to 20 characters
     * (matching {@code [a-z0-9-]{3,20}}).
     *
     * @return a bucket name such as {@code "k7x-data-bucket"}; never {@code null}
     */
    public String s3Bucket() {
        int length = 3 + random.nextInt(18); // 3..20 inclusive
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BUCKET_CHARS.charAt(random.nextInt(BUCKET_CHARS.length())));
        }
        return sb.toString();
    }
}
