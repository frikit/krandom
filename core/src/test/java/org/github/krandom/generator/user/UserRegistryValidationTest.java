/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("User registry validation")
class UserRegistryValidationTest {

    @Test
    @DisplayName("FirstNameDataRegistry rejects empty and blank name arrays")
    void firstNameRegistryRejectsInvalidArrays() {
        Locale locale = Locale.of("qa", "QA");
        assertThrows(IllegalArgumentException.class, () -> FirstNameDataRegistry.register(new FirstNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[0];
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { "Alice" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> FirstNameDataRegistry.register(new FirstNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[] { "Adam" };
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { " " };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> FirstNameDataRegistry.register(new FirstNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[] { null };
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { "Alice" };
            }
        }));
    }

    @Test
    @DisplayName("LastNameDataRegistry rejects empty and blank arrays")
    void lastNameRegistryRejectsInvalidArrays() {
        Locale locale = Locale.of("qb", "QB");
        assertThrows(IllegalArgumentException.class, () -> LastNameDataRegistry.register(new LastNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> LastNameDataRegistry.register(new LastNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[] { " " };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> LastNameDataRegistry.register(new LastNameDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[] { null };
            }
        }));
    }

    @Test
    @DisplayName("GenderDataRegistry rejects blank labels")
    void genderRegistryRejectsBlankLabels() {
        Locale locale = Locale.of("qc", "QC");
        assertThrows(IllegalArgumentException.class, () -> GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return " ";
            }

            @Override
            public String getFemaleLabel() {
                return "Female";
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return "Male";
            }

            @Override
            public String getFemaleLabel() {
                return "";
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> GenderDataRegistry.register(new GenderDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return null;
            }

            @Override
            public String getFemaleLabel() {
                return "Female";
            }
        }));
    }

    @Test
    @DisplayName("TitleDataRegistry rejects null, empty and blank title arrays")
    void titleRegistryRejectsInvalidArrays() {
        Locale locale = Locale.of("qd", "QD");
        assertThrows(NullPointerException.class, () -> TitleDataRegistry.register(new TitleDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return null;
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> TitleDataRegistry.register(new TitleDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> TitleDataRegistry.register(new TitleDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[] { "Dr", " " };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> TitleDataRegistry.register(new TitleDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[] { "Dr", null };
            }
        }));
    }

    @Test
    @DisplayName("SuffixDataRegistry rejects null, empty and blank suffix arrays")
    void suffixRegistryRejectsInvalidArrays() {
        Locale locale = Locale.of("qe", "QE");
        assertThrows(NullPointerException.class, () -> SuffixDataRegistry.register(new SuffixDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return null;
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> SuffixDataRegistry.register(new SuffixDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> SuffixDataRegistry.register(new SuffixDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[] { "Jr.", " " };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> SuffixDataRegistry.register(new SuffixDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[] { "Jr.", null };
            }
        }));
    }
}
