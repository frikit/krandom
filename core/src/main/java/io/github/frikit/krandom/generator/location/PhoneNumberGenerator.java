/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-specific phone numbers.
 *
 * <p>This generator creates realistic phone numbers that match the format conventions of each
 * supported locale. Phone numbers can be generated as formatted (with separators) or unformatted
 * (digits only), and where culturally relevant, as landline or mobile numbers.
 *
 * <p>Built-in support follows the full locale catalog used by the address and identity
 * generators. Supported built-in locales no longer silently fall back to US phone formats when
 * the locale catalog expands.
 *
 * <p>Example usage:
 * <pre>{@code
 *   // Default US locale, formatted
 *   PhoneNumberGenerator gen = new PhoneNumberGenerator();
 *   String phone = gen.generate();  // "(555) 123-4567"
 *   String unformatted = gen.generate(false);  // "5551234567"
 *
 *   // UK locale with mobile number
 *   PhoneNumberGenerator ukGen = new PhoneNumberGenerator(Locale.UK);
 *   String mobile = ukGen.generate(true, true);  // "07700 900123"
 *   String landline = ukGen.generate(true, false);  // "020 7946 0958"
 *
 *   // Seeded for reproducibility
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .locale(Locale.GERMANY)
 *       .seed(42L)
 *       .build();
 *   PhoneNumberGenerator deGen = new PhoneNumberGenerator(config);
 *   String nummer = deGen.generate();  // Reproducible German phone number
 * }</pre>
 */
public final class PhoneNumberGenerator implements Generator<String> {

    private static final Map<String, String> COUNTRY_CALLING_CODES = Map.ofEntries(
        Map.entry("US", "+1"),
        Map.entry("GB", "+44"),
        Map.entry("AU", "+61"),
        Map.entry("DE", "+49"),
        Map.entry("FR", "+33"),
        Map.entry("ES", "+34"),
        Map.entry("IT", "+39"),
        Map.entry("BR", "+55"),
        Map.entry("JP", "+81"),
        Map.entry("CN", "+86"),
        Map.entry("NL", "+31"),
        Map.entry("PL", "+48"),
        Map.entry("RU", "+7"),
        Map.entry("KR", "+82"),
        Map.entry("TR", "+90"),
        Map.entry("SE", "+46"),
        Map.entry("NO", "+47"),
        Map.entry("CZ", "+420"),
        Map.entry("SA", "+966"),
        Map.entry("IN", "+91")
    );

    // US area codes (realistic, avoiding 555)
    private static final int[] US_AREA_CODES = {
        212, 213, 214, 215, 216, 217, 218, 219, 220, 223, 224, 225, 228, 229, 231, 234,
        239, 240, 248, 251, 252, 253, 254, 256, 260, 262, 267, 269, 270, 272, 274, 276,
        281, 301, 302, 303, 304, 305, 307, 308, 309, 310, 312, 313, 314, 315, 316, 317,
        318, 319, 320, 321, 323, 325, 330, 331, 334, 336, 337, 339, 341, 346, 347, 351,
        352, 360, 361, 364, 380, 385, 386, 401, 402, 404, 405, 406, 407, 408, 409, 410,
        412, 413, 414, 415, 417, 419, 423, 424, 425, 430, 432, 434, 435, 440, 442, 443,
        458, 463, 469, 470, 475, 478, 479, 480, 484, 501, 502, 503, 504, 505, 507, 508,
        509, 510, 512, 513, 515, 516, 517, 518, 520, 530, 531, 534, 539, 540, 541, 551,
        559, 561, 562, 563, 564, 567, 570, 571, 573, 574, 575, 580, 585, 586, 601, 602,
        603, 605, 606, 607, 608, 609, 610, 612, 614, 615, 616, 617, 618, 619, 620, 623,
        626, 628, 629, 630, 631, 636, 640, 641, 646, 650, 651, 657, 660, 661, 662, 667,
        669, 678, 681, 682, 701, 702, 703, 704, 706, 707, 708, 712, 713, 714, 715, 716,
        717, 718, 719, 720, 724, 725, 727, 730, 731, 732, 734, 737, 740, 743, 747, 754,
        757, 760, 762, 763, 765, 769, 770, 772, 773, 774, 775, 779, 781, 785, 786, 801,
        802, 803, 804, 805, 806, 808, 810, 812, 813, 814, 815, 816, 817, 818, 828, 830,
        831, 832, 843, 845, 847, 848, 850, 854, 856, 857, 858, 859, 860, 862, 863, 864,
        865, 870, 872, 878, 901, 903, 904, 906, 907, 908, 909, 910, 912, 913, 914, 915,
        916, 917, 918, 919, 920, 925, 928, 929, 930, 931, 934, 936, 937, 938, 940, 941,
        947, 949, 951, 952, 954, 956, 959, 970, 971, 972, 973, 975, 978, 979, 980, 984,
        985, 989
    };

    // UK area codes for landlines
    private static final String[] UK_LANDLINE_CODES = {
        "020", "0121", "0131", "0141", "0151", "0161", "0191", "01202", "01223", "01224",
        "01225", "01226", "01233", "01234", "01235", "01236", "01237", "01256", "01268",
        "01270", "01273", "01274", "01275", "01276", "01277", "01278", "01279", "01284",
        "01293", "01302", "01303", "01305", "01306", "01307", "01308", "01322", "01325",
        "01326", "01332", "01333", "01334", "01335", "01342", "01344", "01352", "01353"
    };

    // UK mobile prefixes
    private static final String[] UK_MOBILE_PREFIXES = {
        "07400", "07401", "07402", "07500", "07501", "07502", "07700", "07701", "07702",
        "07800", "07801", "07802", "07900", "07901", "07902"
    };

    // Australian area codes (landline)
    private static final String[] AU_LANDLINE_CODES = {
        "02", "03", "07", "08"
    };

    // Australian mobile prefixes
    private static final String[] AU_MOBILE_PREFIXES = {
        "0400", "0401", "0402", "0403", "0404", "0405", "0406", "0407", "0408", "0409",
        "0410", "0411", "0412", "0413", "0414", "0415", "0416", "0417", "0418", "0419",
        "0420", "0421", "0422", "0423", "0424", "0425", "0426", "0427", "0428", "0429",
        "0430", "0431", "0432", "0433", "0434", "0435", "0436", "0437", "0438", "0439",
        "0450", "0451", "0452", "0453", "0454", "0455", "0456", "0457", "0458", "0459",
        "0460", "0461", "0462", "0463", "0464", "0465", "0466", "0467", "0468", "0469",
        "0470", "0471", "0472", "0473", "0474", "0475", "0476", "0477", "0478", "0479",
        "0480", "0481", "0482", "0483", "0484", "0485", "0486", "0487", "0488", "0489",
        "0490", "0491", "0492", "0493", "0494", "0495", "0496", "0497", "0498", "0499"
    };

    // German area codes (landline)
    private static final String[] DE_LANDLINE_CODES = {
        "030", "040", "069", "089", "0211", "0221", "0228", "0231", "0251", "0261",
        "0341", "0351", "0361", "0421", "0511", "0611", "0621", "0711", "0821", "0911"
    };

    // German mobile prefixes
    private static final String[] DE_MOBILE_PREFIXES = {
        "0150", "0151", "0152", "0157", "0159", "0160", "0162", "0163", "0170", "0171",
        "0172", "0173", "0174", "0175", "0176", "0177", "0178", "0179"
    };

    // French area codes (landline)
    private static final String[] FR_LANDLINE_PREFIXES = {
        "01", "02", "03", "04", "05"
    };

    // French mobile prefixes
    private static final String[] FR_MOBILE_PREFIXES = {
        "06", "07"
    };

    // Spanish area codes (landline)
    private static final String[] ES_LANDLINE_CODES = {
        "91", "93", "95", "96", "94", "92", "98", "97"
    };

    // Spanish mobile prefixes
    private static final String[] ES_MOBILE_PREFIXES = {
        "600", "601", "602", "603", "604", "605", "606", "607", "608", "609",
        "610", "611", "612", "613", "614", "615", "616", "617", "618", "619",
        "620", "621", "622", "623", "624", "625", "626", "627", "628", "629",
        "630", "631", "632", "633", "634", "635", "636", "637", "638", "639",
        "640", "641", "642", "643", "644", "645", "646", "647", "648", "649",
        "650", "651", "652", "653", "654", "655", "656", "657", "658", "659",
        "660", "661", "662", "663", "664", "665", "666", "667", "668", "669",
        "670", "671", "672", "673", "674", "675", "676", "677", "678", "679",
        "680", "681", "682", "683", "684", "685", "686", "687", "688", "689",
        "690", "691", "692", "693", "694", "695", "696", "697", "698", "699",
        "700", "701", "702", "703", "704", "705", "706", "707", "708", "709"
    };

    // Italian area codes (landline)
    private static final String[] IT_LANDLINE_CODES = {
        "02", "06", "010", "011", "015", "019", "030", "031", "035", "039", "040",
        "041", "045", "049", "050", "051", "055", "059", "070", "071", "075", "079",
        "080", "081", "085", "089", "090", "091", "095", "099"
    };

    // Italian mobile prefixes
    private static final String[] IT_MOBILE_PREFIXES = {
        "320", "321", "322", "323", "324", "327", "328", "329", "330", "331", "333",
        "334", "335", "336", "337", "338", "339", "340", "342", "343", "344", "345",
        "346", "347", "348", "349", "350", "351", "360", "361", "362", "363", "366",
        "368", "370", "371", "373", "377", "380", "383", "388", "389", "390", "391",
        "392", "393", "397", "398", "399"
    };

    // Brazilian area codes
    private static final String[] BR_AREA_CODES = {
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "21", "22", "24", "27",
        "28", "31", "32", "33", "34", "35", "37", "38", "41", "42", "43", "44", "45",
        "46", "47", "48", "49", "51", "53", "54", "55", "61", "62", "63", "64", "65",
        "66", "67", "68", "69", "71", "73", "74", "75", "77", "79", "81", "82", "83",
        "84", "85", "86", "87", "88", "89", "91", "92", "93", "94", "95", "96", "97",
        "98", "99"
    };

    // Japanese area codes (landline)
    private static final String[] JP_LANDLINE_CODES = {
        "03", "04", "06", "011", "022", "043", "044", "045", "048", "052", "058",
        "072", "075", "082", "092", "096", "098"
    };

    // Japanese mobile prefixes
    private static final String[] JP_MOBILE_PREFIXES = {
        "070", "080", "090"
    };

    // Chinese area codes (landline)
    private static final String[] CN_LANDLINE_CODES = {
        "010", "020", "021", "022", "023", "024", "025", "027", "028", "029"
    };

    // Chinese mobile prefixes
    private static final String[] CN_MOBILE_PREFIXES = {
        "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
        "145", "147", "149", "150", "151", "152", "153", "155", "156", "157",
        "158", "159", "166", "170", "171", "172", "173", "175", "176", "177",
        "178", "180", "181", "182", "183", "184", "185", "186", "187", "188",
        "189", "190", "191", "192", "193", "195", "196", "197", "198", "199"
    };

    private static final String[] NL_LANDLINE_CODES = {
        "010", "013", "015", "020", "023", "024", "026", "030", "035", "036",
        "038", "040", "043", "045", "046", "050", "053", "055", "058", "070",
        "071", "072", "073", "074", "075", "076", "077", "078", "079"
    };

    private static final String[] NL_MOBILE_PREFIXES = {
        "061", "062", "063", "064", "065", "066", "068"
    };

    private static final String[] PL_LANDLINE_CODES = {
        "12", "17", "22", "29", "32", "42", "52", "58", "61", "71", "81", "91"
    };

    private static final String[] PL_MOBILE_PREFIXES = {
        "450", "500", "501", "510", "512", "515", "517", "519", "530", "531",
        "535", "537", "570", "572", "600", "602", "606", "660", "666", "690",
        "696", "720", "721", "722", "730", "731", "732", "780", "790", "799",
        "880", "888"
    };

    private static final String[] RU_LANDLINE_CODES = {
        "383", "391", "401", "423", "473", "481", "495", "499", "812", "831"
    };

    private static final String[] RU_MOBILE_PREFIXES = {
        "901", "903", "904", "905", "906", "909", "910", "915", "916", "917",
        "918", "919", "921", "926", "927", "928", "929", "931", "950", "951",
        "952", "953", "960", "961", "962", "963", "964", "965", "966", "967",
        "968", "969", "977", "978", "980", "981", "982", "983", "984", "985",
        "986", "987", "988", "989", "991", "992", "993", "995", "996", "999"
    };

    private static final String[] KR_LANDLINE_CODES = {
        "02", "031", "032", "033", "041", "042", "043", "044",
        "051", "052", "053", "054", "055", "061", "062", "063", "064"
    };

    private static final String[] KR_MOBILE_PREFIXES = {
        "010", "011", "016", "017", "018", "019"
    };

    private static final String[] TR_LANDLINE_CODES = {
        "0212", "0216", "0224", "0232", "0242", "0258", "0312", "0322", "0352", "0388"
    };

    private static final String[] TR_MOBILE_PREFIXES = {
        "0505", "0530", "0531", "0532", "0533", "0534", "0541", "0542", "0543", "0544",
        "0551", "0552", "0553", "0554"
    };

    private static final String[] SE_LANDLINE_CODES = {
        "08", "010", "011", "013", "018", "019", "021", "023", "026", "031",
        "033", "036", "040", "042", "046", "054", "060", "063", "090"
    };

    private static final String[] SE_MOBILE_PREFIXES = {
        "070", "072", "073", "076", "079"
    };

    private static final String[] NO_LANDLINE_CODES = {
        "21", "22", "23", "24", "32", "33", "35", "37", "38", "51",
        "52", "53", "55", "56", "57", "61", "62", "63", "64", "69",
        "71", "72", "73", "74", "75", "76", "77", "78", "79"
    };

    private static final String[] NO_MOBILE_PREFIXES = {
        "412", "413", "414", "450", "451", "452", "453", "454", "455", "456",
        "457", "458", "459", "906", "907", "908", "909", "911", "912", "913",
        "914", "915", "916", "917", "918", "919", "920", "921", "922", "923",
        "924", "925", "926", "927", "928", "929", "930", "931", "932", "933",
        "934", "935", "936", "937", "938", "939", "940", "941", "942", "943",
        "944", "945", "946", "947", "948", "949", "950", "951", "952", "953",
        "954", "955", "956", "957", "958", "959", "960", "961", "962", "963",
        "964", "965", "966", "967", "968", "969", "970", "971", "972", "973",
        "974", "975", "976", "977", "978", "979", "980", "981", "982", "983",
        "984", "985", "986", "987", "988", "989"
    };

    private static final String[] CZ_LANDLINE_PREFIXES = {
        "2", "3", "4", "5"
    };

    private static final String[] CZ_MOBILE_PREFIXES = {
        "601", "602", "603", "604", "605", "606", "607", "608", "702", "720",
        "721", "722", "723", "724", "725", "726", "727", "728", "729", "730",
        "731", "732", "733", "734", "735", "736", "737", "739"
    };

    private static final String[] SA_LANDLINE_CODES = {
        "011", "012", "013", "014", "016", "017"
    };

    private static final String[] SA_MOBILE_PREFIXES = {
        "050", "053", "054", "055", "056", "057", "058", "059"
    };

    private static final String[] IN_LANDLINE_CODES = {
        "011", "020", "022", "033", "040", "044", "0471", "0484", "079", "080"
    };

    private final GeneratorConfig config;
    private final Random          random;
    private final Locale          locale;

    /**
     * Creates a generator using US locale with default config.
     */
    public PhoneNumberGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public PhoneNumberGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining the phone number format; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    public PhoneNumberGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(
            Objects.requireNonNull(locale, "locale must not be null")
        ).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a formatted phone number in the locale's default style.
     * For locales with both mobile and landline, generates either type randomly.
     */
    @Override
    public String generate() {
        return generate(true);
    }

    /**
     * Generates a telephone number using locale-default formatting.
     * Mimesis-style alias for {@link #generate()}.
     *
     * @return generated telephone number
     */
    public String generateTelephone() {
        return generate();
    }

    /**
     * Generates a phone number with optional formatting.
     *
     * @param formatted {@code true} for formatted output with separators,
     *                  {@code false} for digits only
     * @return a phone number string
     */
    public String generate(boolean formatted) {
        // For locales with mobile/landline distinction, randomly choose
        boolean mobile = random.nextBoolean();
        return generate(formatted, mobile);
    }

    /**
     * Generates a telephone number with explicit formatting and number-type controls.
     * Mimesis-style alias for {@link #generate(boolean, boolean)}.
     *
     * @param formatted true for formatted output
     * @param mobile    true for mobile, false for landline
     * @return generated telephone number
     */
    public String generateTelephone(boolean formatted, boolean mobile) {
        return generate(formatted, mobile);
    }

    /**
     * Generates a phone number with control over formatting and mobile/landline type.
     *
     * <p>The {@code mobile} parameter affects output for locales where there's a
     * cultural distinction (UK, AU, FR, ES, IT, etc.). For locales without this
     * distinction (US, BR), the parameter is ignored.
     *
     * @param formatted {@code true} for formatted output, {@code false} for digits only
     * @param mobile    {@code true} for mobile number, {@code false} for landline
     *                  (where applicable)
     * @return a phone number string
     */
    public String generate(boolean formatted, boolean mobile) {
        String localeKey = getLocaleKey(locale);

        return switch (localeKey) {
            case "en_US" -> generateUSPhone(formatted);
            case "en" -> generateUSPhone(formatted);
            case "en_GB" -> generateUKPhone(formatted, mobile);
            case "en_AU" -> generateAustralianPhone(formatted, mobile);
            case "de_DE" -> generateGermanPhone(formatted, mobile);
            case "de" -> generateGermanPhone(formatted, mobile);
            case "fr_FR" -> generateFrenchPhone(formatted, mobile);
            case "fr" -> generateFrenchPhone(formatted, mobile);
            case "es_ES" -> generateSpanishPhone(formatted, mobile);
            case "es" -> generateSpanishPhone(formatted, mobile);
            case "it_IT" -> generateItalianPhone(formatted, mobile);
            case "it" -> generateItalianPhone(formatted, mobile);
            case "pt_BR" -> generateBrazilianPhone(formatted, mobile);
            case "pt" -> generateBrazilianPhone(formatted, mobile);
            case "ja_JP" -> generateJapanesePhone(formatted, mobile);
            case "ja" -> generateJapanesePhone(formatted, mobile);
            case "zh_CN" -> generateChinesePhone(formatted, mobile);
            case "zh" -> generateChinesePhone(formatted, mobile);
            case "nl_NL", "nl" -> generateDutchPhone(formatted, mobile);
            case "pl_PL", "pl" -> generatePolishPhone(formatted, mobile);
            case "ru_RU", "ru" -> generateRussianPhone(formatted, mobile);
            case "ko_KR", "ko" -> generateKoreanPhone(formatted, mobile);
            case "tr_TR", "tr" -> generateTurkishPhone(formatted, mobile);
            case "sv_SE", "sv" -> generateSwedishPhone(formatted, mobile);
            case "nb_NO", "nb", "no_NO", "no" -> generateNorwegianPhone(formatted, mobile);
            case "cs_CZ", "cs" -> generateCzechPhone(formatted, mobile);
            case "ar_SA", "ar" -> generateSaudiPhone(formatted, mobile);
            case "hi_IN", "hi" -> generateIndianPhone(formatted, mobile);
            default -> generateUSPhone(formatted); // Default to US format
        };
    }

    /**
     * Returns the locale this generator is configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the locale country calling code (for example, {@code +1}, {@code +44}).
     *
     * @return country calling code
     */
    public String generateCountryCallingCode() {
        return COUNTRY_CALLING_CODES.getOrDefault(locale.getCountry(), "+1");
    }

    /**
     * Generates an MSISDN-like numeric string (14-15 digits, country code + subscriber digits).
     *
     * @return MSISDN digits only
     */
    public String generateMsisdn() {
        String callingCode = generateCountryCallingCode().replace("+", "");
        int targetLength = 14 + random.nextInt(2); // 14 or 15
        int subscriberDigits = Math.max(1, targetLength - callingCode.length());
        StringBuilder msisdn = new StringBuilder(targetLength);
        msisdn.append(callingCode);
        for (int i = 0; i < subscriberDigits; i++) {
            msisdn.append(random.nextInt(10));
        }
        return msisdn.toString();
    }

    /**
     * Generates a phone number from a custom format template.
     *
     * <p>Template placeholders:
     * <ul>
     *   <li>{@code #} -> random digit {@code 0-9}</li>
     * </ul>
     *
     * <p>Example: {@code "###-###-####"}.
     * Bogus-style alias: {@code phoneNumberFormat()} equivalent.
     *
     * @param format template string; must not be {@code null} or blank
     * @return formatted phone value
     */
    public String generateWithFormat(String format) {
        Objects.requireNonNull(format, "format must not be null");
        if (format.isBlank()) {
            throw new IllegalArgumentException("format must not be blank");
        }
        StringBuilder out = new StringBuilder(format.length());
        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            out.append(ch == '#' ? (char) ('0' + random.nextInt(10)) : ch);
        }
        return out.toString();
    }

    /**
     * Generates a telephone number from a mask template.
     * Mimesis-style alias for {@link #generateWithFormat(String)}.
     *
     * @param mask template string using {@code #} placeholders
     * @return formatted value
     */
    public String generateTelephone(String mask) {
        return generateWithFormat(mask);
    }

    // ── Format generators ─────────────────────────────────────────────────────

    private String generateUSPhone(boolean formatted) {
        int areaCode = US_AREA_CODES[random.nextInt(US_AREA_CODES.length)];
        int exchange = 200 + random.nextInt(800); // 200-999
        int number = random.nextInt(10000);

        if (formatted) {
            // Randomly choose between two common US formats
            if (random.nextBoolean()) {
                return String.format("(%03d) %03d-%04d", areaCode, exchange, number);
            } else {
                return String.format("%03d-%03d-%04d", areaCode, exchange, number);
            }
        }
        return String.format("%03d%03d%04d", areaCode, exchange, number);
    }

    private String generateUKPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = UK_MOBILE_PREFIXES[random.nextInt(UK_MOBILE_PREFIXES.length)];
            int number = random.nextInt(1000000);

            if (formatted) {
                return String.format("%s %06d", prefix, number);
            }
            return prefix + String.format("%06d", number);
        } else {
            String areaCode = UK_LANDLINE_CODES[random.nextInt(UK_LANDLINE_CODES.length)];
            int numberLength = areaCode.equals("020") ? 8 :
                               (areaCode.length() == 5 ? 6 : 7);
            int maxNumber = (int) Math.pow(10, numberLength);
            int number = random.nextInt(maxNumber);

            if (formatted) {
                if (areaCode.equals("020")) {
                    // London format: 020 7946 0958
                    int part1 = number / 10000;
                    int part2 = number % 10000;
                    return String.format("%s %04d %04d", areaCode, part1, part2);
                } else if (areaCode.length() == 5) {
                    // 5-digit area code: 01202 123456
                    return String.format("%s %0" + numberLength + "d", areaCode, number);
                } else {
                    // 4-digit area code: 0161 496 0123
                    int part1 = number / 10000;
                    int part2 = number % 10000;
                    return String.format("%s %03d %04d", areaCode, part1, part2);
                }
            }
            return areaCode + String.format("%0" + numberLength + "d", number);
        }
    }

    private String generateAustralianPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = AU_MOBILE_PREFIXES[random.nextInt(AU_MOBILE_PREFIXES.length)];
            int number = random.nextInt(1000000);

            if (formatted) {
                int part1 = number / 1000;
                int part2 = number % 1000;
                return String.format("%s %03d %03d", prefix, part1, part2);
            }
            return prefix + String.format("%06d", number);
        } else {
            String areaCode = AU_LANDLINE_CODES[random.nextInt(AU_LANDLINE_CODES.length)];
            int number = random.nextInt(100000000);

            if (formatted) {
                int part1 = number / 10000;
                int part2 = number % 10000;
                return String.format("%s %04d %04d", areaCode, part1, part2);
            }
            return areaCode + String.format("%08d", number);
        }
    }

    private String generateGermanPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = DE_MOBILE_PREFIXES[random.nextInt(DE_MOBILE_PREFIXES.length)];
            int number = random.nextInt(100000000);

            if (formatted) {
                return String.format("%s %08d", prefix, number);
            }
            return prefix + String.format("%08d", number);
        } else {
            String areaCode = DE_LANDLINE_CODES[random.nextInt(DE_LANDLINE_CODES.length)];
            int number = random.nextInt(100000000);

            if (formatted) {
                return String.format("%s %08d", areaCode, number);
            }
            return areaCode + String.format("%08d", number);
        }
    }

    private String generateFrenchPhone(boolean formatted, boolean mobile) {
        String prefix;
        if (mobile) {
            prefix = FR_MOBILE_PREFIXES[random.nextInt(FR_MOBILE_PREFIXES.length)];
        } else {
            prefix = FR_LANDLINE_PREFIXES[random.nextInt(FR_LANDLINE_PREFIXES.length)];
        }

        int part2 = 10 + random.nextInt(90);
        int part3 = 10 + random.nextInt(90);
        int part4 = 10 + random.nextInt(90);
        int part5 = 10 + random.nextInt(90);

        if (formatted) {
            return String.format("%s %02d %02d %02d %02d", prefix, part2, part3, part4, part5);
        }
        return String.format("%s%02d%02d%02d%02d", prefix, part2, part3, part4, part5);
    }

    private String generateSpanishPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = ES_MOBILE_PREFIXES[random.nextInt(ES_MOBILE_PREFIXES.length)];
            int part2 = 10 + random.nextInt(90);
            int part3 = 10 + random.nextInt(90);
            int part4 = 10 + random.nextInt(90);

            if (formatted) {
                return String.format("%s %02d %02d %02d", prefix, part2, part3, part4);
            }
            return String.format("%s%02d%02d%02d", prefix, part2, part3, part4);
        } else {
            String areaCode = ES_LANDLINE_CODES[random.nextInt(ES_LANDLINE_CODES.length)];
            int part2 = 100 + random.nextInt(900);
            int part3 = 10 + random.nextInt(90);
            int part4 = 10 + random.nextInt(90);

            if (formatted) {
                return String.format("%s %03d %02d %02d", areaCode, part2, part3, part4);
            }
            return String.format("%s%03d%02d%02d", areaCode, part2, part3, part4);
        }
    }

    private String generateItalianPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = IT_MOBILE_PREFIXES[random.nextInt(IT_MOBILE_PREFIXES.length)];
            int number = random.nextInt(10000000);

            if (formatted) {
                int part1 = number / 10000;
                int part2 = number % 10000;
                return String.format("%s %03d %04d", prefix, part1, part2);
            }
            return prefix + String.format("%07d", number);
        } else {
            String areaCode = IT_LANDLINE_CODES[random.nextInt(IT_LANDLINE_CODES.length)];
            int numberDigits = 10 - areaCode.length();
            int maxNumber = (int) Math.pow(10, numberDigits);
            int number = random.nextInt(maxNumber);

            if (formatted) {
                int part1 = number / 10000;
                int part2 = number % 10000;
                return String.format("%s %04d %04d", areaCode, part1, part2);
            }
            return areaCode + String.format("%0" + numberDigits + "d", number);
        }
    }

    private String generateBrazilianPhone(boolean formatted, boolean mobile) {
        String areaCode = BR_AREA_CODES[random.nextInt(BR_AREA_CODES.length)];

        if (mobile) {
            // Mobile: 9 digits (9xxxx-xxxx)
            int part1 = 90000 + random.nextInt(10000);
            int part2 = random.nextInt(10000);

            if (formatted) {
                return String.format("(%s) %d-%04d", areaCode, part1, part2);
            }
            return String.format("%s%d%04d", areaCode, part1, part2);
        } else {
            // Landline: 8 digits (3xxx-xxxx or 2xxx-xxxx)
            int firstDigit = random.nextBoolean() ? 2 : 3;
            int part1 = firstDigit * 1000 + random.nextInt(1000);
            int part2 = random.nextInt(10000);

            if (formatted) {
                return String.format("(%s) %04d-%04d", areaCode, part1, part2);
            }
            return String.format("%s%04d%04d", areaCode, part1, part2);
        }
    }

    private String generateJapanesePhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = JP_MOBILE_PREFIXES[random.nextInt(JP_MOBILE_PREFIXES.length)];
            int part2 = 1000 + random.nextInt(9000);
            int part3 = random.nextInt(10000);

            if (formatted) {
                return String.format("%s-%04d-%04d", prefix, part2, part3);
            }
            return String.format("%s%04d%04d", prefix, part2, part3);
        } else {
            String areaCode = JP_LANDLINE_CODES[random.nextInt(JP_LANDLINE_CODES.length)];
            int part2 = 1000 + random.nextInt(9000);
            int part3 = random.nextInt(10000);

            if (formatted) {
                return String.format("%s-%04d-%04d", areaCode, part2, part3);
            }
            return String.format("%s%04d%04d", areaCode, part2, part3);
        }
    }

    private String generateChinesePhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = CN_MOBILE_PREFIXES[random.nextInt(CN_MOBILE_PREFIXES.length)];
            int part2 = 1000 + random.nextInt(9000);
            int part3 = random.nextInt(10000);

            if (formatted) {
                return String.format("%s %04d %04d", prefix, part2, part3);
            }
            return String.format("%s%04d%04d", prefix, part2, part3);
        } else {
            String areaCode = CN_LANDLINE_CODES[random.nextInt(CN_LANDLINE_CODES.length)];
            int number = random.nextInt(100000000);

            if (formatted) {
                return String.format("%s-%08d", areaCode, number);
            }
            return String.format("%s%08d", areaCode, number);
        }
    }

    private String generateDutchPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = NL_MOBILE_PREFIXES[random.nextInt(NL_MOBILE_PREFIXES.length)];
            String suffix = digits(7);
            if (formatted) {
                return prefix + " " + suffix;
            }
            return prefix + suffix;
        }
        String areaCode = NL_LANDLINE_CODES[random.nextInt(NL_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return areaCode + " " + suffix;
        }
        return areaCode + suffix;
    }

    private String generatePolishPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = PL_MOBILE_PREFIXES[random.nextInt(PL_MOBILE_PREFIXES.length)];
            String suffix = digits(6);
            if (formatted) {
                return prefix + " " + suffix.substring(0, 3) + " " + suffix.substring(3);
            }
            return prefix + suffix;
        }
        String areaCode = PL_LANDLINE_CODES[random.nextInt(PL_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return areaCode + " " + suffix.substring(0, 3) + " " + suffix.substring(3, 5) + " " + suffix.substring(5);
        }
        return areaCode + suffix;
    }

    private String generateRussianPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? RU_MOBILE_PREFIXES[random.nextInt(RU_MOBILE_PREFIXES.length)]
                        : RU_LANDLINE_CODES[random.nextInt(RU_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return prefix + " " + suffix.substring(0, 3) + "-" + suffix.substring(3, 5) + "-" + suffix.substring(5);
        }
        return prefix + suffix;
    }

    private String generateKoreanPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? KR_MOBILE_PREFIXES[random.nextInt(KR_MOBILE_PREFIXES.length)]
                        : KR_LANDLINE_CODES[random.nextInt(KR_LANDLINE_CODES.length)];
        String suffix = digits(8);
        if (formatted) {
            return prefix + "-" + suffix.substring(0, 4) + "-" + suffix.substring(4);
        }
        return prefix + suffix;
    }

    private String generateTurkishPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? TR_MOBILE_PREFIXES[random.nextInt(TR_MOBILE_PREFIXES.length)]
                        : TR_LANDLINE_CODES[random.nextInt(TR_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return prefix + " " + suffix.substring(0, 3) + " " + suffix.substring(3, 5) + " " + suffix.substring(5);
        }
        return prefix + suffix;
    }

    private String generateSwedishPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? SE_MOBILE_PREFIXES[random.nextInt(SE_MOBILE_PREFIXES.length)]
                        : SE_LANDLINE_CODES[random.nextInt(SE_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return prefix + "-" + suffix.substring(0, 3) + " " + suffix.substring(3, 5) + " " + suffix.substring(5);
        }
        return prefix + suffix;
    }

    private String generateNorwegianPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? NO_MOBILE_PREFIXES[random.nextInt(NO_MOBILE_PREFIXES.length)]
                        : NO_LANDLINE_CODES[random.nextInt(NO_LANDLINE_CODES.length)];
        String suffix = mobile ? digits(5) : digits(6);
        if (formatted) {
            if (mobile) {
                return prefix + " " + suffix.substring(0, 2) + " " + suffix.substring(2);
            }
            return prefix + " " + suffix.substring(0, 2) + " " + suffix.substring(2, 4) + " " + suffix.substring(4);
        }
        return prefix + suffix;
    }

    private String generateCzechPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? CZ_MOBILE_PREFIXES[random.nextInt(CZ_MOBILE_PREFIXES.length)]
                        : CZ_LANDLINE_PREFIXES[random.nextInt(CZ_LANDLINE_PREFIXES.length)] + digits(2);
        String suffix = digits(6);
        if (formatted) {
            return prefix + " " + suffix.substring(0, 3) + " " + suffix.substring(3);
        }
        return prefix + suffix;
    }

    private String generateSaudiPhone(boolean formatted, boolean mobile) {
        String prefix = mobile
                        ? SA_MOBILE_PREFIXES[random.nextInt(SA_MOBILE_PREFIXES.length)]
                        : SA_LANDLINE_CODES[random.nextInt(SA_LANDLINE_CODES.length)];
        String suffix = digits(7);
        if (formatted) {
            return prefix + " " + suffix.substring(0, 3) + " " + suffix.substring(3);
        }
        return prefix + suffix;
    }

    private String generateIndianPhone(boolean formatted, boolean mobile) {
        if (mobile) {
            String prefix = String.valueOf(6 + random.nextInt(4)) + digits(4);
            String suffix = digits(5);
            if (formatted) {
                return prefix + " " + suffix;
            }
            return prefix + suffix;
        }
        String areaCode = IN_LANDLINE_CODES[random.nextInt(IN_LANDLINE_CODES.length)];
        String suffix = digits(8);
        if (formatted) {
            return areaCode + " " + suffix;
        }
        return areaCode + suffix;
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private String digits(int count) {
        return String.format("%0" + count + "d", random.nextInt((int) Math.pow(10, count)));
    }

    private String getLocaleKey(Locale loc) {
        String language = loc.getLanguage();
        String country = loc.getCountry();

        if (!country.isEmpty()) {
            return language + "_" + country;
        }
        return language;
    }
}
