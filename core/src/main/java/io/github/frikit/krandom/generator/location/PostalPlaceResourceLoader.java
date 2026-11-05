/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads postal places from a classpath resource.
 *
 * <p>Each row is a city, its region and its postal codes, separated by tabs, with the postal codes
 * separated by commas. The region is named exactly as the locale's states file names it, and takes
 * that file's code. Blank lines and lines starting with {@code #} are ignored:
 *
 * <pre>
 * # Postal places - English (US)
 * Grand Rapids	Michigan	49503,49504,49505
 * </pre>
 */
final class PostalPlaceResourceLoader {

    private PostalPlaceResourceLoader() {}

    /**
     * Loads the places of a classpath resource.
     *
     * @param resourcePath path relative to the classpath root
     * @param regions      the locale's regions and their codes
     * @return the places, in file order; never empty
     * @throws IllegalStateException if the resource is missing, unreadable, empty, or has a row that
     *                               is malformed or names an unknown region
     */
    static List<PostalPlace> load(String resourcePath, StateResourceLoader.StateData regions) {
        InputStream is = PostalPlaceResourceLoader.class.getResourceAsStream("/" + resourcePath);
        if (is == null) {
            throw new IllegalStateException("Postal place resource not found: " + resourcePath);
        }
        return load(is, resourcePath, regions);
    }

    static List<PostalPlace> load(InputStream is, String resourcePath, StateResourceLoader.StateData regions) {
        Map<String, String> codeByRegion = new HashMap<>();
        for (int i = 0; i < regions.states.length; i++) {
            codeByRegion.put(regions.states[i], regions.abbreviations[i]);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<PostalPlace> places = new ArrayList<>();
            int lineNumber = 0;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lineNumber++;
                if (!line.isBlank() && !line.startsWith("#")) {
                    places.add(place(line, resourcePath + ":" + lineNumber, codeByRegion));
                }
            }
            if (places.isEmpty()) {
                throw new IllegalStateException("Postal place resource has no places: " + resourcePath);
            }
            return List.copyOf(places);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read postal place resource: " + resourcePath, e);
        }
    }

    private static PostalPlace place(String line, String location, Map<String, String> codeByRegion) {
        String[] fields = line.split("\t", -1);
        if (fields.length != 3) {
            throw new IllegalStateException(
                    location + ": expected a city, a region and postal codes separated by tabs: " + line);
        }
        String regionCode = codeByRegion.get(fields[1]);
        if (regionCode == null) {
            throw new IllegalStateException(location + ": region '" + fields[1] + "' is not in the states file");
        }
        try {
            return new PostalPlace(fields[0], fields[1], regionCode, List.of(fields[2].split(",", -1)));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(location + ": " + e.getMessage(), e);
        }
    }
}
