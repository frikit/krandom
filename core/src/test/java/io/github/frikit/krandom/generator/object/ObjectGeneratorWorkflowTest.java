/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.core.model.Address;
import io.github.frikit.krandom.generator.core.model.CircularNode;
import io.github.frikit.krandom.generator.core.model.Person;
import io.github.frikit.krandom.generator.core.model.PersonRecord;
import io.github.frikit.krandom.generator.core.model.PersonWithArrays;
import io.github.frikit.krandom.generator.core.model.PersonWithCollections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("object generator workflows")
class ObjectGeneratorWorkflowTest {

    @Test
    @DisplayName("single object workflow uses object generation")
    void singleObjectWorkflowUsesObjectGeneration() {
        Person person = Generators.ofObject(Person.class).generate();

        assertPopulatedPerson(person);
    }

    @Test
    @DisplayName("bulk object workflow uses generateList(size) and stream().limit(size)")
    void bulkObjectWorkflowUsesGeneratedListsAndStreams() {
        ObjectGenerator<Person> generator = Generators.ofObject(Person.class);

        List<Person> generatedList = generator.generateList(5);
        assertEquals(5, generatedList.size());
        generatedList.forEach(this::assertPopulatedPerson);

        List<Person> streamed = generator.stream().limit(3).toList();
        assertEquals(3, streamed.size());
        streamed.forEach(this::assertPopulatedPerson);
    }

    @Test
    @DisplayName("seeded object generation is repeatable with GeneratorConfig")
    void seededObjectGenerationIsRepeatable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(123L)
                                                .stringLength(6, 6)
                                                .collectionSize(2, 2)
                                                .build();

        Person first = Generators.ofObject(Person.class, config).generate();
        Person second = Generators.ofObject(Person.class, config).generate();

        assertEquals(snapshot(first), snapshot(second));
    }

    @Test
    @DisplayName("records are populated through the canonical constructor")
    void recordsAreGeneratedNatively() {
        PersonRecord record = Generators.ofObject(PersonRecord.class).generate();

        assertNotNull(record);
        assertNotNull(record.firstName());
        assertFalse(record.firstName().isEmpty());
        assertNotNull(record.lastName());
        assertNotNull(record.status());
        assertNotNull(record.address());
        assertNotNull(record.address().getStreet());
    }

    @Test
    @DisplayName("nested objects, arrays, collections, maps, and optionals are populated")
    void containerAndNestedObjectFeaturesAreGeneratedNatively() {
        PersonWithArrays arrays = Generators.ofObject(PersonWithArrays.class, fixedContainerConfig()).generate();
        assertNotNull(arrays.getTags());
        assertEquals(2, arrays.getTags().length);
        assertNotNull(arrays.getTags()[0]);
        assertNotNull(arrays.getScores());
        assertEquals(2, arrays.getScores().length);
        assertNotNull(arrays.getAddresses());
        assertEquals(2, arrays.getAddresses().length);
        assertNotNull(arrays.getAddresses()[0].getStreet());

        PersonWithCollections collections =
            Generators.ofObject(PersonWithCollections.class, fixedContainerConfig()).generate();
        assertNotNull(collections.getHobbies());
        assertEquals(2, collections.getHobbies().size());
        assertNotNull(collections.getHobbies().get(0));
        assertNotNull(collections.getRoles());
        assertFalse(collections.getRoles().isEmpty());
        assertNotNull(collections.getAttributes());
        assertFalse(collections.getAttributes().isEmpty());

        OptionalHolder optionals = Generators.ofObject(OptionalHolder.class, fixedContainerConfig()).generate();
        assertNotNull(optionals.name);
        assertTrue(optionals.name.isPresent());
        assertNotNull(optionals.address);
        assertTrue(optionals.address.isPresent());
        assertNotNull(optionals.address.orElseThrow().getStreet());
    }

    @Test
    @DisplayName("circular references are bounded by native pool and depth handling")
    void circularReferencesAndDepthAreBoundedNatively() {
        GeneratorConfig circularConfig = GeneratorConfig.builder()
                                                        .objectPoolSize(0)
                                                        .build();

        CircularNode node = assertDoesNotThrow(
            () -> Generators.ofObject(CircularNode.class, circularConfig).generate());
        assertNotNull(node);
        assertNotNull(node.getName());

        GeneratorConfig depthConfig = GeneratorConfig.builder()
                                                     .objectMaxDepth(1)
                                                     .build();
        DepthRoot root = Generators.ofObject(DepthRoot.class, depthConfig).generate();
        assertNotNull(root.middle);
        assertNull(root.middle.leaf, "nested objects beyond max depth should be cut off");
    }

    @Test
    @DisplayName("native object generation advances state across repeated calls")
    void reusedObjectGeneratorAdvancesStateAcrossCalls() {
        ObjectGenerator<Person> generator = Generators.ofObject(Person.class, GeneratorConfig.builder().seed(42L).build());

        Person first = generator.generate();
        Person second = generator.generate();

        assertNotEquals(snapshot(first), snapshot(second));
    }

    private GeneratorConfig fixedContainerConfig() {
        return GeneratorConfig.builder()
                              .seed(99L)
                              .stringLength(4, 4)
                              .collectionSize(2, 2)
                              .build();
    }

    private void assertPopulatedPerson(Person person) {
        assertNotNull(person);
        assertNotNull(person.getFirstName());
        assertFalse(person.getFirstName().isEmpty());
        assertNotNull(person.getLastName());
        assertNotNull(person.getStatus());
        assertNotNull(person.getAddress());
        assertNotNull(person.getAddress().getStreet());
    }

    private List<Object> snapshot(Person person) {
        Address address = person.getAddress();
        List<Object> values = new ArrayList<>();
        values.add(person.getFirstName());
        values.add(person.getLastName());
        values.add(person.getAge());
        values.add(person.getSalary());
        values.add(person.isActive());
        values.add(person.getStatus());
        values.add(address != null ? address.getStreet() : null);
        values.add(address != null ? address.getHouseNumber() : null);
        values.add(address != null ? address.getPostCode() : null);
        values.add(address != null ? address.getGeoId() : null);
        values.add(address != null ? address.getLatitude() : null);
        values.add(address != null ? address.getLongitude() : null);
        values.add(address != null ? address.getCountryCode() : null);
        return List.copyOf(values);
    }

    static class OptionalHolder {

        Optional<String>  name;
        Optional<Address> address;
    }

    static class DepthRoot {

        DepthMiddle middle;
    }

    static class DepthMiddle {

        DepthLeaf leaf;
    }

    static class DepthLeaf {

        String value;
    }
}
