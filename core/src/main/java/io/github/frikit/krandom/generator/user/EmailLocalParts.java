/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds lowercase ASCII email local-part tokens from locale names.
 *
 * <p>Latin-script names lose their diacritics; German-language names expand umlauts to
 * {@code ae}/{@code oe}/{@code ue}. Cyrillic names follow common Russian, the official Ukrainian
 * (2010), or the official Bulgarian (2009) romanization, and Greek names follow a simplified
 * ELOT 743 table. A name containing a letter of any other script is not truncated: it is replaced by
 * a romanized name shipped for the locale's language (Japanese, Chinese, Korean, Arabic, Hebrew,
 * Hindi, Thai) or, for other languages, by an English name.
 */
final class EmailLocalParts {

    private static final String ROMANIZED_NAMES = "krandom/names/romanized/";
    private static final String DEFAULT_ROMANIZED_PREFIX = "en_US";
    private static final Map<String, String> ROMANIZED_PREFIX_BY_LANGUAGE = Map.of(
        "ja", "ja_JP", "zh", "zh_CN", "ko", "ko_KR", "ar", "ar_SA", "he", "he_IL", "hi", "hi_IN", "th", "th_TH");

    private static final Map<Integer, String> LATIN = table("ßss", "æae", "œoe", "øo", "łl", "đd", "ðd", "þth", "ıi");
    private static final Map<Integer, String> RUSSIAN = table(
        "аa", "бb", "вv", "гg", "дd", "еe", "ёe", "жzh", "зz", "иi", "йy", "кk", "лl", "мm", "нn", "оo", "пp",
        "рr", "сs", "тt", "уu", "фf", "хkh", "цts", "чch", "шsh", "щshch", "ъ", "ыy", "ь", "эe", "юyu", "яya");
    private static final Map<Integer, String> UKRAINIAN = override(RUSSIAN, "гh", "ґg", "єie", "иy", "іi", "їi",
                                                                   "йi", "юiu", "яia");
    private static final Map<Integer, String> UKRAINIAN_WORD_INITIAL = table("єye", "їyi", "йy", "юyu", "яya");
    private static final Map<Integer, String> BULGARIAN = override(RUSSIAN, "хh", "щsht", "ъa", "ьy");
    private static final Map<Integer, String> GREEK = table(
        "αa", "βv", "γg", "δd", "εe", "ζz", "ηi", "θth", "ιi", "κk", "λl", "μm", "νn", "ξx", "οo", "πp", "ρr",
        "σs", "ςs", "τt", "υy", "φf", "χch", "ψps", "ωo");
    private static final Map<String, String> GREEK_DIGRAPHS = Map.of("ου", "ou", "αυ", "av", "ευ", "ev", "γγ", "ng");
    private static final Map<String, String[]> FALLBACK_NAMES = new ConcurrentHashMap<>();

    private EmailLocalParts() {
    }

    /**
     * Returns the ASCII local-part token for one name, drawing a romanized fallback name from
     * {@code random} only when the name cannot be romanized.
     */
    static String token(String name, Locale locale, boolean firstName, Random random) {
        String romanized = romanize(name, locale);
        if (!romanized.isEmpty()) {
            return romanized;
        }
        String[] fallback = fallbackNames(locale, firstName);
        return fallback[random.nextInt(fallback.length)];
    }

    /**
     * Romanizes a name to lowercase ASCII letters and digits, or returns an empty string when it
     * contains a letter outside the supported transliterations.
     */
    static String romanize(String name, Locale locale) {
        String language = locale.getLanguage();
        String text = Normalizer.normalize(name, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        if ("de".equals(language)) {
            text = text.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");
        }
        String cyrillicFree = transliterateCyrillic(text, language);
        String unmarked = Normalizer.normalize(cyrillicFree, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return transliterateRemaining(unmarked);
    }

    static String[] fallbackNames(Locale locale, boolean firstName) {
        String prefix = ROMANIZED_PREFIX_BY_LANGUAGE.getOrDefault(locale.getLanguage(), DEFAULT_ROMANIZED_PREFIX);
        String resource = ROMANIZED_NAMES + (firstName ? "first/" : "last/") + prefix + ".txt";
        return FALLBACK_NAMES.computeIfAbsent(resource, LocaleTextResourceLoader::load);
    }

    private static String transliterateCyrillic(String text, String language) {
        Map<Integer, String> table = switch (language) {
            case "uk" -> UKRAINIAN;
            case "bg" -> BULGARIAN;
            default -> RUSSIAN;
        };
        boolean ukrainian = "uk".equals(language);
        StringBuilder out = new StringBuilder(text.length() + 8);
        boolean wordStart = true;
        int index = 0;
        while (index < text.length()) {
            int codePoint = text.codePointAt(index);
            String initial = ukrainian && wordStart ? UKRAINIAN_WORD_INITIAL.get(codePoint) : null;
            String mapped = initial != null ? initial : table.get(codePoint);
            if (mapped != null) {
                out.append(mapped);
            } else {
                out.appendCodePoint(codePoint);
            }
            wordStart = Character.isWhitespace(codePoint);
            index += Character.charCount(codePoint);
        }
        return out.toString();
    }

    private static String transliterateRemaining(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            String digraph = GREEK_DIGRAPHS.get(text.substring(index, Math.min(index + 2, text.length())));
            if (digraph != null) {
                out.append(digraph);
                index += 2;
                continue;
            }
            int codePoint = text.codePointAt(index);
            String mapped = mapCodePoint(codePoint);
            if (mapped != null) {
                out.append(mapped);
            } else if (Character.isLetter(codePoint)) {
                return "";
            }
            index += Character.charCount(codePoint);
        }
        return out.toString();
    }

    private static String mapCodePoint(int codePoint) {
        if (codePoint < 0x80) {
            return Character.isLetterOrDigit(codePoint) ? Character.toString(codePoint) : "";
        }
        String greek = GREEK.get(codePoint);
        return greek != null ? greek : LATIN.get(codePoint);
    }

    private static Map<Integer, String> table(String... entries) {
        return override(Map.of(), entries);
    }

    private static Map<Integer, String> override(Map<Integer, String> base, String... entries) {
        Map<Integer, String> table = new HashMap<>(base);
        for (String entry : entries) {
            table.put(entry.codePointAt(0), entry.substring(Character.charCount(entry.codePointAt(0))));
        }
        return Map.copyOf(table);
    }
}
