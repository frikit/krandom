/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataProvider;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.krandom.generator.finance.FinancialTermDataProvider;
import io.github.frikit.krandom.generator.finance.FinancialTermDataRegistry;
import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CityDataRegistry;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataRegistry;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StateDataRegistry;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataRegistry;
import io.github.frikit.krandom.generator.measurement.MeasurementDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementDataRegistry;
import io.github.frikit.krandom.generator.user.BloodTypeDataProvider;
import io.github.frikit.krandom.generator.user.BloodTypeDataRegistry;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.HobbyDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.NationalityDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.PronounDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.user.ZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdRegistry;
import io.github.frikit.krandom.generator.weather.WeatherDataProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Legacy data-registry deprecations")
class LegacyRegistryDeprecationTest {

    @Test
    @DisplayName("no process-wide mutation entry point survives the v2 removal")
    void globalMutationMethodsAreRemovedInV2() {
        for (LegacyMutation mutation : mutations()) {
            assertThrows(NoSuchMethodException.class, mutation::method, mutation::description);
        }
    }

    @Test
    @DisplayName("registries expose no public static register or append methods at all")
    void registriesExposeNoPublicMutationMethods() {
        for (LegacyMutation mutation : mutations()) {
            for (Method method : mutation.registry().getMethods()) {
                boolean mutator = Modifier.isStatic(method.getModifiers())
                    && (method.getName().equals("register") || method.getName().equals("append"));
                assertFalse(mutator,
                    () -> mutation.registry().getSimpleName() + " must not expose " + method.getName());
            }
        }
    }

    private static List<LegacyMutation> mutations() {
        return List.of(
            mutation(RestaurantTypeDataRegistry.class, "register", RestaurantTypeDataProvider.class),
            mutation(FinancialTermDataRegistry.class, "register", FinancialTermDataProvider.class),
            mutation(CityDataRegistry.class, "register", CityDataProvider.class),
            mutation(CountryDataRegistry.class, "register", CountryDataProvider.class),
            mutation(StateDataRegistry.class, "register", StateDataProvider.class),
            mutation(StreetAddressDataRegistry.class, "register", StreetAddressDataProvider.class),
            mutation(MeasurementDataRegistry.class, "register", MeasurementDataProvider.class),
            mutation(BloodTypeDataRegistry.class, "register", BloodTypeDataProvider.class),
            mutation(ChineseZodiacDataRegistry.class, "register", ChineseZodiacDataProvider.class),
            mutation(FirstNameDataRegistry.class, "register", FirstNameDataProvider.class),
            mutation(GenderDataRegistry.class, "register", GenderDataProvider.class),
            mutation(HobbyDataRegistry.class, "register", HobbyDataProvider.class),
            mutation(LastNameDataRegistry.class, "register", LastNameDataProvider.class),
            mutation(NationalityDataRegistry.class, "register", NationalityDataProvider.class),
            mutation(ProfessionDataRegistry.class, "register", ProfessionDataProvider.class),
            mutation(ProfessionDataRegistry.class, "append", Locale.class, String[].class, int[].class),
            mutation(ProfessionDataRegistry.class, "append", Locale.class, String[].class),
            mutation(PronounDataRegistry.class, "register", PronounDataProvider.class),
            mutation(SuffixDataRegistry.class, "register", SuffixDataProvider.class),
            mutation(TitleDataRegistry.class, "register", TitleDataProvider.class),
            mutation(ZodiacDataRegistry.class, "register", ZodiacDataProvider.class),
            mutation(NationalIdRegistry.class, "register", NationalIdProvider.class),
            mutation(WeatherDataRegistry.class, "register", WeatherDataProvider.class)
        );
    }

    private static LegacyMutation mutation(Class<?> registry, String name, Class<?>... parameterTypes) {
        return new LegacyMutation(registry, name, parameterTypes);
    }

    private record LegacyMutation(Class<?> registry, String name, Class<?>... parameterTypes) {

        Method method() throws NoSuchMethodException {
            return registry.getMethod(name, parameterTypes);
        }

        String description() {
            return registry.getSimpleName() + "#" + name;
        }
    }
}
