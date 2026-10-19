/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static io.github.frikit.krandom.generator.VocabularyValidators.array;
import static io.github.frikit.krandom.generator.VocabularyValidators.bloodTypeDistribution;
import static io.github.frikit.krandom.generator.VocabularyValidators.label;
import static io.github.frikit.krandom.generator.VocabularyValidators.nullableEntries;
import static io.github.frikit.krandom.generator.VocabularyValidators.professionArrays;
import static io.github.frikit.krandom.generator.VocabularyValidators.pronounSets;
import static io.github.frikit.krandom.generator.VocabularyValidators.textValues;
import static io.github.frikit.krandom.generator.VocabularyValidators.twelveTextValues;
import static io.github.frikit.krandom.generator.VocabularyValidators.universityData;

import io.github.frikit.krandom.generator.commerce.CommerceDataProvider;
import io.github.frikit.krandom.generator.commerce.CommerceDataRegistry;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataProvider;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataProvider;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataRegistry;
import io.github.frikit.krandom.generator.file.DirectoryNameDataProvider;
import io.github.frikit.krandom.generator.file.DirectoryNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankAccountDataProvider;
import io.github.frikit.krandom.generator.finance.BankAccountDataRegistry;
import io.github.frikit.krandom.generator.finance.BankNameDataProvider;
import io.github.frikit.krandom.generator.finance.BankNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankTypeDataProvider;
import io.github.frikit.krandom.generator.finance.BankTypeDataRegistry;
import io.github.frikit.krandom.generator.finance.FinancialTermDataProvider;
import io.github.frikit.krandom.generator.finance.FinancialTermDataRegistry;
import io.github.frikit.krandom.generator.location.AddressPurposeDataProvider;
import io.github.frikit.krandom.generator.location.AddressPurposeDataRegistry;
import io.github.frikit.krandom.generator.location.AddressTypeDataProvider;
import io.github.frikit.krandom.generator.location.AddressTypeDataRegistry;
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
import io.github.frikit.krandom.generator.text.TextWordDataProvider;
import io.github.frikit.krandom.generator.text.TextWordDataRegistry;
import io.github.frikit.krandom.generator.user.BloodTypeDataProvider;
import io.github.frikit.krandom.generator.user.BloodTypeDataRegistry;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataProvider;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataProvider;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyNameDataProvider;
import io.github.frikit.krandom.generator.user.CompanyNameDataRegistry;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataProvider;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.HobbyDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataRegistry;
import io.github.frikit.krandom.generator.user.IndustryDataProvider;
import io.github.frikit.krandom.generator.user.IndustryDataRegistry;
import io.github.frikit.krandom.generator.user.JobFieldDataProvider;
import io.github.frikit.krandom.generator.user.JobFieldDataRegistry;
import io.github.frikit.krandom.generator.user.JobTypeDataProvider;
import io.github.frikit.krandom.generator.user.JobTypeDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.MaritalStatusDataProvider;
import io.github.frikit.krandom.generator.user.MaritalStatusDataRegistry;
import io.github.frikit.krandom.generator.user.NationalityDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataRegistry;
import io.github.frikit.krandom.generator.user.PositionDataProvider;
import io.github.frikit.krandom.generator.user.PositionDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.PronounDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataRegistry;
import io.github.frikit.krandom.generator.user.SeniorityDataProvider;
import io.github.frikit.krandom.generator.user.SeniorityDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.user.UniversityDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdRegistry;
import io.github.frikit.krandom.generator.weather.WeatherDataProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataRegistry;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Identifies one vocabulary (first names, cities, bank names, ...) together with the provider
 * contract that supplies its data.
 *
 * <p>The built-in vocabularies are the constants of this class and {@link #all()} lists them.
 * {@link DataRegistryContext} resolves and registers providers by vocabulary:
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .register(Vocabulary.CITY, new MyCityProvider())
 *     .build();
 * CityDataProvider cities = context.provider(Vocabulary.CITY, Locale.US);
 * }</pre>
 *
 * <p>Every built-in vocabulary except {@link #UNIVERSITY} is backed by a read-only global registry
 * of bundled data that a non-isolated context falls back to. University data is scoped only: it
 * comes from a direct registration or a verified local data pack, never from bundled data.
 * Consumers that write their own generators can define further
 * vocabularies with {@link #custom(String, Class)}; those have no bundled data and live only in the
 * contexts they are registered in.
 *
 * <p>Vocabularies compare by identity, so keep a custom key in a constant and share it between the
 * code that registers providers and the code that looks them up.
 *
 * @param <P> provider contract that supplies the vocabulary's data
 */
public final class Vocabulary<P extends LocaleDataProvider> {

    /** Given names by gender. */
    public static final Vocabulary<FirstNameDataProvider> FIRST_NAME = builtIn(
            "firstName",
            FirstNameDataProvider.class,
            FirstNameDataRegistry::forLocale,
            FirstNameDataRegistry::registeredKeys,
            provider -> {
                array("maleFirstNames", provider.getMaleFirstNames());
                array("femaleFirstNames", provider.getFemaleFirstNames());
            });

    /** Family names. */
    public static final Vocabulary<LastNameDataProvider> LAST_NAME = builtIn(
            "lastName",
            LastNameDataProvider.class,
            LastNameDataRegistry::forLocale,
            LastNameDataRegistry::registeredKeys,
            provider -> array("lastNames", provider.getLastNames()));

    /** Gender labels. */
    public static final Vocabulary<GenderDataProvider> GENDER = builtIn(
            "gender",
            GenderDataProvider.class,
            GenderDataRegistry::forLocale,
            GenderDataRegistry::registeredKeys,
            provider -> {
                label("maleLabel", provider.getMaleLabel());
                label("femaleLabel", provider.getFemaleLabel());
            });

    /** Honorific titles. */
    public static final Vocabulary<TitleDataProvider> TITLE = builtIn(
            "title",
            TitleDataProvider.class,
            TitleDataRegistry::forLocale,
            TitleDataRegistry::registeredKeys,
            provider -> array("titles", provider.getTitles()));

    /** Name suffixes. */
    public static final Vocabulary<SuffixDataProvider> SUFFIX = builtIn(
            "suffix",
            SuffixDataProvider.class,
            SuffixDataRegistry::forLocale,
            SuffixDataRegistry::registeredKeys,
            provider -> array("suffixes", provider.getSuffixes()));

    /** Weighted professions. */
    public static final Vocabulary<ProfessionDataProvider> PROFESSION = builtIn(
            "profession",
            ProfessionDataProvider.class,
            ProfessionDataRegistry::forLocale,
            ProfessionDataRegistry::registeredKeys,
            provider -> professionArrays(provider.getProfessions(), provider.getWeights()));

    /** City names. */
    public static final Vocabulary<CityDataProvider> CITY = builtIn(
            "city",
            CityDataProvider.class,
            CityDataRegistry::forLocale,
            CityDataRegistry::registeredKeys,
            provider -> array("cities", provider.getCities()));

    /** States or regions with their abbreviations. */
    public static final Vocabulary<StateDataProvider> STATE = builtIn(
            "state",
            StateDataProvider.class,
            StateDataRegistry::forLocale,
            StateDataRegistry::registeredKeys,
            provider -> {
                array("states", provider.getStates());
                nullableEntries("abbreviations", provider.getAbbreviations());
            });

    /** Country names. */
    public static final Vocabulary<CountryDataProvider> COUNTRY = builtIn(
            "country",
            CountryDataProvider.class,
            CountryDataRegistry::forLocale,
            CountryDataRegistry::registeredKeys,
            provider -> array("countries", provider.getCountries()));

    /** Street names and street types. */
    public static final Vocabulary<StreetAddressDataProvider> STREET_ADDRESS = builtIn(
            "streetAddress",
            StreetAddressDataProvider.class,
            StreetAddressDataRegistry::forLocale,
            StreetAddressDataRegistry::registeredKeys,
            provider -> {
                array("streetNames", provider.getStreetNames());
                array("streetTypesShort", provider.getStreetTypesShort());
                array("streetTypesLong", provider.getStreetTypesLong());
            });

    /** Address types by property use: residential, commercial, and so on. */
    public static final Vocabulary<AddressTypeDataProvider> ADDRESS_TYPE = builtIn(
            "addressType",
            AddressTypeDataProvider.class,
            AddressTypeDataRegistry::forLocale,
            AddressTypeDataRegistry::registeredKeys,
            provider -> textValues("addressTypes", provider.getAddressTypes()));

    /** Address purposes, what an address is used for: home, billing, shipping, and so on. */
    public static final Vocabulary<AddressPurposeDataProvider> ADDRESS_PURPOSE = builtIn(
            "addressPurpose",
            AddressPurposeDataProvider.class,
            AddressPurposeDataRegistry::forLocale,
            AddressPurposeDataRegistry::registeredKeys,
            provider -> textValues("addressPurposes", provider.getAddressPurposes()));

    /**
     * National identity-number formats. Unlike every other vocabulary, a registration also serves
     * every locale of the same country, because identity numbers are national rather than linguistic.
     */
    public static final Vocabulary<NationalIdProvider> NATIONAL_ID = new Vocabulary<>(
            "nationalId",
            NationalIdProvider.class,
            NationalIdRegistry::forLocale,
            NationalIdRegistry::registeredKeys,
            provider -> {},
            true,
            true);

    /** Weather conditions. */
    public static final Vocabulary<WeatherDataProvider> WEATHER = builtIn(
            "weather",
            WeatherDataProvider.class,
            WeatherDataRegistry::forLocale,
            WeatherDataRegistry::registeredKeys,
            provider -> textValues("conditions", provider.getConditions()));

    /** Measurement units. */
    public static final Vocabulary<MeasurementDataProvider> MEASUREMENT = builtIn(
            "measurement",
            MeasurementDataProvider.class,
            MeasurementDataRegistry::forLocale,
            MeasurementDataRegistry::registeredKeys,
            provider -> textValues("units", provider.getUnits()));

    /** Financial terms. */
    public static final Vocabulary<FinancialTermDataProvider> FINANCIAL_TERM = builtIn(
            "financialTerm",
            FinancialTermDataProvider.class,
            FinancialTermDataRegistry::forLocale,
            FinancialTermDataRegistry::registeredKeys,
            provider -> textValues("terms", provider.getTerms()));

    /** Restaurant types. */
    public static final Vocabulary<RestaurantTypeDataProvider> RESTAURANT_TYPE = builtIn(
            "restaurantType",
            RestaurantTypeDataProvider.class,
            RestaurantTypeDataRegistry::forLocale,
            RestaurantTypeDataRegistry::registeredKeys,
            provider -> textValues("types", provider.getTypes()));

    /** Hobbies. */
    public static final Vocabulary<HobbyDataProvider> HOBBY = builtIn(
            "hobby",
            HobbyDataProvider.class,
            HobbyDataRegistry::forLocale,
            HobbyDataRegistry::registeredKeys,
            provider -> textValues("hobbies", provider.getHobbies()));

    /** Nationalities. */
    public static final Vocabulary<NationalityDataProvider> NATIONALITY = builtIn(
            "nationality",
            NationalityDataProvider.class,
            NationalityDataRegistry::forLocale,
            NationalityDataRegistry::registeredKeys,
            provider -> textValues("nationalities", provider.getNationalities()));

    /** Pronoun sets in subject/object form. */
    public static final Vocabulary<PronounDataProvider> PRONOUN = builtIn(
            "pronoun",
            PronounDataProvider.class,
            PronounDataRegistry::forLocale,
            PronounDataRegistry::registeredKeys,
            provider -> pronounSets(provider.getPronounSets()));

    /** Weighted blood types. */
    public static final Vocabulary<BloodTypeDataProvider> BLOOD_TYPE = builtIn(
            "bloodType",
            BloodTypeDataProvider.class,
            BloodTypeDataRegistry::forLocale,
            BloodTypeDataRegistry::registeredKeys,
            provider -> bloodTypeDistribution(provider.getTypes(), provider.getWeights()));

    /** The twelve Chinese zodiac animals. */
    public static final Vocabulary<ChineseZodiacDataProvider> CHINESE_ZODIAC = builtIn(
            "chineseZodiac",
            ChineseZodiacDataProvider.class,
            ChineseZodiacDataRegistry::forLocale,
            ChineseZodiacDataRegistry::registeredKeys,
            provider -> twelveTextValues("animals", provider.getAnimals()));

    /** The twelve zodiac signs. */
    public static final Vocabulary<ZodiacDataProvider> ZODIAC = builtIn(
            "zodiac",
            ZodiacDataProvider.class,
            ZodiacDataRegistry::forLocale,
            ZodiacDataRegistry::registeredKeys,
            provider -> twelveTextValues("signs", provider.getSigns()));

    /**
     * Universities. There is no bundled data: a provider is registered directly or through a
     * verified local data pack ({@link DataRegistryContext.Builder#registerDataPack}), and even a
     * non-isolated context never falls back to global data.
     */
    public static final Vocabulary<UniversityDataProvider> UNIVERSITY = scopedOnly(
            "university", UniversityDataProvider.class, provider -> universityData(provider.getUniversities()));

    /** Industries. */
    public static final Vocabulary<IndustryDataProvider> INDUSTRY = builtIn(
            "industry",
            IndustryDataProvider.class,
            IndustryDataRegistry::forLocale,
            IndustryDataRegistry::registeredKeys,
            provider -> textValues("industries", provider.getIndustries()));

    /** Job fields. */
    public static final Vocabulary<JobFieldDataProvider> JOB_FIELD = builtIn(
            "jobField",
            JobFieldDataProvider.class,
            JobFieldDataRegistry::forLocale,
            JobFieldDataRegistry::registeredKeys,
            provider -> textValues("jobFields", provider.getJobFields()));

    /** Seniority levels. */
    public static final Vocabulary<SeniorityDataProvider> SENIORITY = builtIn(
            "seniority",
            SeniorityDataProvider.class,
            SeniorityDataRegistry::forLocale,
            SeniorityDataRegistry::registeredKeys,
            provider -> textValues("seniorities", provider.getSeniorities()));

    /** Job positions. */
    public static final Vocabulary<PositionDataProvider> POSITION = builtIn(
            "position",
            PositionDataProvider.class,
            PositionDataRegistry::forLocale,
            PositionDataRegistry::registeredKeys,
            provider -> textValues("positions", provider.getPositions()));

    /** Educational attainment levels. */
    public static final Vocabulary<EducationalAttainmentDataProvider> EDUCATIONAL_ATTAINMENT = builtIn(
            "educationalAttainment",
            EducationalAttainmentDataProvider.class,
            EducationalAttainmentDataRegistry::forLocale,
            EducationalAttainmentDataRegistry::registeredKeys,
            provider -> textValues("educationalAttainments", provider.getEducationalAttainments()));

    /** Marital statuses. */
    public static final Vocabulary<MaritalStatusDataProvider> MARITAL_STATUS = builtIn(
            "maritalStatus",
            MaritalStatusDataProvider.class,
            MaritalStatusDataRegistry::forLocale,
            MaritalStatusDataRegistry::registeredKeys,
            provider -> textValues("maritalStatuses", provider.getMaritalStatuses()));

    /** Employment types. */
    public static final Vocabulary<JobTypeDataProvider> JOB_TYPE = builtIn(
            "jobType",
            JobTypeDataProvider.class,
            JobTypeDataRegistry::forLocale,
            JobTypeDataRegistry::registeredKeys,
            provider -> textValues("jobTypes", provider.getJobTypes()));

    /** Company name parts and formats. */
    public static final Vocabulary<CompanyNameDataProvider> COMPANY_NAME = builtIn(
            "companyName",
            CompanyNameDataProvider.class,
            CompanyNameDataRegistry::forLocale,
            CompanyNameDataRegistry::registeredKeys,
            provider -> {
                textValues("prefixes", provider.getPrefixes());
                textValues("nouns", provider.getNouns());
                textValues("suffixes", provider.getSuffixes());
                label("nameFormat", provider.getNameFormat());
                label("legalNameFormat", provider.getLegalNameFormat());
            });

    /** Company buzzword parts and format. */
    public static final Vocabulary<CompanyBuzzwordDataProvider> COMPANY_BUZZWORD = builtIn(
            "companyBuzzword",
            CompanyBuzzwordDataProvider.class,
            CompanyBuzzwordDataRegistry::forLocale,
            CompanyBuzzwordDataRegistry::registeredKeys,
            provider -> {
                textValues("verbs", provider.getVerbs());
                textValues("adjectives", provider.getAdjectives());
                textValues("nouns", provider.getNouns());
                label("format", provider.getFormat());
            });

    /** Company catch-phrase parts and format. */
    public static final Vocabulary<CompanyCatchPhraseDataProvider> COMPANY_CATCH_PHRASE = builtIn(
            "companyCatchPhrase",
            CompanyCatchPhraseDataProvider.class,
            CompanyCatchPhraseDataRegistry::forLocale,
            CompanyCatchPhraseDataRegistry::registeredKeys,
            provider -> {
                textValues("adjectives", provider.getAdjectives());
                textValues("nouns", provider.getNouns());
                textValues("taglines", provider.getTaglines());
                label("format", provider.getFormat());
            });

    /** Words for generated text. */
    public static final Vocabulary<TextWordDataProvider> TEXT_WORD = builtIn(
            "textWord",
            TextWordDataProvider.class,
            TextWordDataRegistry::forLocale,
            TextWordDataRegistry::registeredKeys,
            provider -> textValues("words", provider.getWords()));

    /** Commerce product vocabulary and formats. */
    public static final Vocabulary<CommerceDataProvider> COMMERCE = builtIn(
            "commerce",
            CommerceDataProvider.class,
            CommerceDataRegistry::forLocale,
            CommerceDataRegistry::registeredKeys,
            provider -> {
                textValues("adjectives", provider.getAdjectives());
                textValues("materials", provider.getMaterials());
                textValues("products", provider.getProducts());
                textValues("departments", provider.getDepartments());
                textValues("colors", provider.getColors());
                label("productNameFormat", provider.getProductNameFormat());
                label("productDescriptionFormat", provider.getProductDescriptionFormat());
            });

    /** Database column names. */
    public static final Vocabulary<DatabaseColumnDataProvider> DATABASE_COLUMN = builtIn(
            "databaseColumn",
            DatabaseColumnDataProvider.class,
            DatabaseColumnDataRegistry::forLocale,
            DatabaseColumnDataRegistry::registeredKeys,
            provider -> textValues("columns", provider.getColumns()));

    /** Directory names. */
    public static final Vocabulary<DirectoryNameDataProvider> DIRECTORY_NAME = builtIn(
            "directoryName",
            DirectoryNameDataProvider.class,
            DirectoryNameDataRegistry::forLocale,
            DirectoryNameDataRegistry::registeredKeys,
            provider -> textValues("directoryNames", provider.getDirectoryNames()));

    /** Bank names. */
    public static final Vocabulary<BankNameDataProvider> BANK_NAME = builtIn(
            "bankName",
            BankNameDataProvider.class,
            BankNameDataRegistry::forLocale,
            BankNameDataRegistry::registeredKeys,
            provider -> textValues("bankNames", provider.getBankNames()));

    /** Bank types. */
    public static final Vocabulary<BankTypeDataProvider> BANK_TYPE = builtIn(
            "bankType",
            BankTypeDataProvider.class,
            BankTypeDataRegistry::forLocale,
            BankTypeDataRegistry::registeredKeys,
            provider -> textValues("bankTypes", provider.getBankTypes()));

    /** Bank account names and transaction types. */
    public static final Vocabulary<BankAccountDataProvider> BANK_ACCOUNT = builtIn(
            "bankAccount",
            BankAccountDataProvider.class,
            BankAccountDataRegistry::forLocale,
            BankAccountDataRegistry::registeredKeys,
            provider -> {
                textValues("accountNames", provider.getAccountNames());
                textValues("transactionTypes", provider.getTransactionTypes());
            });

    private static final List<Vocabulary<?>> ALL = List.of(
            FIRST_NAME,
            LAST_NAME,
            GENDER,
            TITLE,
            SUFFIX,
            PROFESSION,
            CITY,
            STATE,
            COUNTRY,
            STREET_ADDRESS,
            ADDRESS_TYPE,
            ADDRESS_PURPOSE,
            NATIONAL_ID,
            WEATHER,
            MEASUREMENT,
            FINANCIAL_TERM,
            RESTAURANT_TYPE,
            HOBBY,
            NATIONALITY,
            PRONOUN,
            BLOOD_TYPE,
            CHINESE_ZODIAC,
            ZODIAC,
            UNIVERSITY,
            INDUSTRY,
            JOB_FIELD,
            SENIORITY,
            POSITION,
            EDUCATIONAL_ATTAINMENT,
            MARITAL_STATUS,
            JOB_TYPE,
            COMPANY_NAME,
            COMPANY_BUZZWORD,
            COMPANY_CATCH_PHRASE,
            TEXT_WORD,
            COMMERCE,
            DATABASE_COLUMN,
            DIRECTORY_NAME,
            BANK_NAME,
            BANK_TYPE,
            BANK_ACCOUNT);

    private final String name;
    private final Class<P> providerType;
    private final Function<Locale, @Nullable P> globalLookup;
    private final Supplier<Set<String>> globalKeys;
    private final Consumer<P> validator;
    private final boolean countryIndexed;
    private final boolean builtInData;

    private Vocabulary(
            String name,
            Class<P> providerType,
            Function<Locale, @Nullable P> globalLookup,
            Supplier<Set<String>> globalKeys,
            Consumer<P> validator,
            boolean countryIndexed,
            boolean builtInData) {
        this.name = name;
        this.providerType = providerType;
        this.globalLookup = globalLookup;
        this.globalKeys = globalKeys;
        this.validator = validator;
        this.countryIndexed = countryIndexed;
        this.builtInData = builtInData;
    }

    private static <P extends LocaleDataProvider> Vocabulary<P> builtIn(
            String name,
            Class<P> providerType,
            Function<Locale, @Nullable P> globalLookup,
            Supplier<Set<String>> globalKeys,
            Consumer<P> validator) {
        return new Vocabulary<>(name, providerType, globalLookup, globalKeys, validator, false, true);
    }

    private static <P extends LocaleDataProvider> Vocabulary<P> scopedOnly(
            String name, Class<P> providerType, Consumer<P> validator) {
        return new Vocabulary<>(name, providerType, locale -> null, Set::of, validator, false, false);
    }

    /**
     * Defines a vocabulary of your own, for example for a custom generator that reads locale-specific
     * data from a provider type you declare.
     *
     * <p>A custom vocabulary has no bundled data and no validation beyond a non-null provider and
     * locale. Keys compare by identity: two calls with the same name create two unrelated
     * vocabularies, so keep the result in a constant.
     *
     * @param name display name used by {@link #toString()}
     * @param providerType provider contract of the vocabulary
     * @param <P> provider contract of the vocabulary
     * @return a new vocabulary key
     */
    public static <P extends LocaleDataProvider> Vocabulary<P> custom(String name, Class<P> providerType) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(providerType, "providerType");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new Vocabulary<>(name, providerType, locale -> null, Set::of, provider -> {}, false, false);
    }

    /**
     * Returns the built-in vocabularies in declaration order.
     *
     * @return immutable list of the built-in vocabularies
     */
    public static List<Vocabulary<?>> all() {
        return ALL;
    }

    /**
     * Returns the vocabulary name, such as {@code firstName}.
     *
     * @return vocabulary name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the provider contract that supplies this vocabulary's data.
     *
     * @return provider interface
     */
    public Class<P> providerType() {
        return providerType;
    }

    /**
     * Reports whether bundled data backs this vocabulary, so that a non-isolated context can resolve
     * it without any registration. This is {@code false} for {@link #UNIVERSITY} and for custom
     * vocabularies.
     *
     * @return true when a global registry of bundled data exists
     */
    public boolean hasBuiltInData() {
        return builtInData;
    }

    @Nullable P globalProvider(Locale locale) {
        return globalLookup.apply(locale);
    }

    Set<String> globalKeys() {
        return globalKeys.get();
    }

    boolean countryIndexed() {
        return countryIndexed;
    }

    /**
     * Checks a provider before it is registered: it and its locale must be non-null and its data
     * must satisfy the vocabulary's rules.
     */
    void validate(P provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        validator.accept(provider);
    }

    @Override
    public String toString() {
        return name;
    }
}
