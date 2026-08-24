/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PropertyPath")
class PropertyPathTest {

    @Test
    @DisplayName("extracts bean and record accessor names")
    void extractsAccessorNames() {
        assertEquals("name", PropertyPath.of(Person::getName).path());
        assertEquals("active", PropertyPath.of(Person::isActive).path());
        assertEquals("city", PropertyPath.of(Address::city).path());
        assertEquals("URL", PropertyPath.of(Person::getURL).path());
        assertEquals("x", PropertyPath.of(Person::getX).path());
        assertEquals("get", PropertyPath.of(Person::get).path());
        assertEquals("getaway", PropertyPath.of(Person::getaway).path());
        assertEquals("name", PropertyPath.of(Named::getName).path());
    }

    @Test
    @DisplayName("composes nested typed paths")
    void composesNestedPaths() {
        PropertyPath<Person, String> city = PropertyPath.of(Person::getAddress).then(Address::city);

        assertEquals("address.city", city.path());
        assertEquals("address.city", city.toString());
    }

    @Test
    @DisplayName("has value semantics")
    void hasValueSemantics() {
        PropertyPath<Person, String> name = PropertyPath.of(Person::getName);
        PropertyPath<Person, String> sameName = PropertyPath.of(Person::getName);
        PropertyPath<Person, String> url = PropertyPath.of(Person::getURL);

        assertEquals(name, name);
        assertEquals(name, sameName);
        assertEquals(name.hashCode(), sameName.hashCode());
        assertNotEquals(name, url);
        assertNotEquals(name, "name");
    }

    @Test
    @DisplayName("rejects arbitrary lambdas because they do not identify a property")
    void rejectsArbitraryLambdas() {
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> PropertyPath.of((PropertySelector<Person, String>) person -> person.getName().trim()));

        assertTrue(failure.getMessage().contains("method reference"));
    }

    @Test
    @DisplayName("rejects static methods and accessor names containing a dollar sign")
    void rejectsInvalidMethodReferences() {
        assertThrows(IllegalArgumentException.class, () -> PropertyPath.of(Person::staticName));
        assertThrows(IllegalArgumentException.class, () -> PropertyPath.of(Person::value$alias));
    }

    @Test
    @DisplayName("rejects selectors without serialized method-reference metadata")
    void rejectsSelectorWithoutSerializedLambda() {
        PropertySelector<Person, String> selector = new PropertySelector<>() {
            @Override
            public String apply(Person person) {
                return person.getName();
            }
        };

        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class, () -> PropertyPath.of(selector));

        assertTrue(failure.getMessage().contains("unbound accessor method reference"));
    }

    @Test
    @DisplayName("rejects replacement metadata that is not a serialized lambda")
    void rejectsInvalidReplacementMetadata() {
        PropertySelector<Person, String> selector = new PropertySelector<>() {
            @Override
            public String apply(Person person) {
                return person.getName();
            }

            @SuppressWarnings("unused")
            private Object writeReplace() {
                return "not lambda metadata";
            }
        };

        assertThrows(IllegalArgumentException.class, () -> PropertyPath.of(selector));
    }

    @Test
    @DisplayName("reports failures while reading replacement metadata")
    void reportsReplacementMetadataFailure() {
        PropertySelector<Person, String> selector = new PropertySelector<>() {
            @Override
            public String apply(Person person) {
                return person.getName();
            }

            @SuppressWarnings("unused")
            private Object writeReplace() {
                throw new IllegalStateException("broken metadata");
            }
        };

        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class, () -> PropertyPath.of(selector));

        assertEquals("broken metadata", failure.getCause().getMessage());
    }

    interface Named {
        String getName();
    }

    static final class Person {
        String name;
        boolean active;
        Address address;

        String getName() {
            return name;
        }

        boolean isActive() {
            return active;
        }

        Address getAddress() {
            return address;
        }

        String getURL() {
            return "https://example.com";
        }

        String getX() {
            return "x";
        }

        String get() {
            return name;
        }

        String getaway() {
            return name;
        }

        String value$alias() {
            return name;
        }

        static String staticName(Person person) {
            return person.name;
        }
    }

    record Address(String city) {
    }
}
