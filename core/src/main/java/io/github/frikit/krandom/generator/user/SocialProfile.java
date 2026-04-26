/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

/**
 * Social profile fixture.
 *
 * @param platform   profile platform key (for example {@code "github"})
 * @param handle     social handle
 * @param profileUrl profile URL
 * @param displayName display name
 * @param bio        short biography line
 */
public record SocialProfile(
    String platform,
    String handle,
    String profileUrl,
    String displayName,
    String bio
) {
}
