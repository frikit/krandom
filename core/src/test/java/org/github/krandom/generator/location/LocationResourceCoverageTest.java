/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location resource and registry coverage")
class LocationResourceCoverageTest {

    @Test
    @DisplayName("city loader reads test resource and rejects missing path")
    void cityLoader() {
        String[] cities = CityResourceLoader.load("krandom/cities/test_cities.txt");
        assertArrayEquals(new String[]{"City One", "City Two", "City Three"}, cities);
        assertThrows(IllegalStateException.class, () -> CityResourceLoader.load("krandom/cities/missing.txt"));
    }

    @Test
    @DisplayName("city loader wraps IO failures")
    void cityLoaderIoFailure() {
        assertThrows(IllegalStateException.class, () -> CityResourceLoader.load(closeFailingStream(), "broken-city"));
    }

    @Test
    @DisplayName("country loader reads test resource and rejects missing path")
    void countryLoader() {
        String[] countries = CountryResourceLoader.load("krandom/countries/test_countries.txt");
        assertArrayEquals(new String[]{"Country One", "Country Two", "Country Three"}, countries);
        assertThrows(IllegalStateException.class, () -> CountryResourceLoader.load("krandom/countries/missing.txt"));
    }

    @Test
    @DisplayName("country loader wraps IO failures")
    void countryLoaderIoFailure() {
        assertThrows(IllegalStateException.class, () -> CountryResourceLoader.load(closeFailingStream(), "broken-country"));
    }

    @Test
    @DisplayName("state loader supports missing abbreviation lines")
    void stateLoaderMissingAbbreviation() {
        StateResourceLoader.StateData data = StateResourceLoader.load("krandom/states/test_states_missing_abbrev.txt");
        assertArrayEquals(new String[]{"Alpha", "Beta", "Gamma"}, data.states);
        assertArrayEquals(new String[]{"AL", "", ""}, data.abbreviations);
    }

    @Test
    @DisplayName("state loader rejects missing path")
    void stateLoaderMissingPath() {
        assertThrows(IllegalStateException.class, () -> StateResourceLoader.load("krandom/states/missing.txt"));
    }

    @Test
    @DisplayName("state loader wraps IO failures")
    void stateLoaderIoFailure() {
        assertThrows(IllegalStateException.class, () -> StateResourceLoader.load(closeFailingStream(), "broken-state"));
    }

    @Test
    @DisplayName("street loader wraps IO failures")
    void streetLoaderIoFailure() {
        assertThrows(IllegalStateException.class, () -> StreetAddressResourceLoader.load(closeFailingStream(), "broken-street"));
    }

    @Test
    @DisplayName("utility constructors are guarded")
    void utilityConstructors() throws Exception {
        assertUtilityConstructorThrows(CityResourceLoader.class);
        assertUtilityConstructorThrows(CountryResourceLoader.class);
        assertUtilityConstructorThrows(StreetAddressResourceLoader.class);
        assertUtilityConstructorThrows(StateResourceLoader.class);
        assertUtilityConstructorThrows(CityDataRegistry.class);
        assertUtilityConstructorThrows(StreetAddressDataRegistry.class);
    }

    private static void assertUtilityConstructorThrows(Class<?> type) throws Exception {
        Constructor<?> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }

    private static InputStream closeFailingStream() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("boom");
            }
        };
    }
}
