/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("RegexGenerator")
class RegexGeneratorTest {

    private static final int SAMPLES = 50;

    // ── Digit class ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("\\d? produces either 0 or 1 digits")
    void optionalQuantifier() {
        RegexGenerator gen = new RegexGenerator("\\d?");
        for (int i = 0; i < SAMPLES; i++) {
            String s = gen.generate();
            assertTrue(
                    s.isEmpty() || (s.length() == 1 && Character.isDigit(s.charAt(0))),
                    "\\d? must produce 0 or 1 digit, got: " + s);
        }
    }

    // ── Uppercase character class ─────────────────────────────────────────────

    @Test
    @DisplayName("literal pattern produces the literal string")
    void literalPattern() {
        for (int i = 0; i < SAMPLES; i++) {
            assertEquals("hello", new RegexGenerator("hello").generate());
        }
    }

    // ── Alternation ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("\\d{3}-\\d{4} produces NNN-NNNN format")
    void phoneLikePattern() {
        for (int i = 0; i < SAMPLES; i++) {
            String s = new RegexGenerator("\\d{3}-\\d{4}").generate();
            assertEquals(8, s.length(), "expected 8 chars, got: " + s);
            assertEquals('-', s.charAt(3), "expected '-' at index 3, got: " + s);
        }
    }

    // ── Word chars with + ─────────────────────────────────────────────────────

    @Test
    @DisplayName("anchors ^ and $ add no characters")
    void anchorsAddNoCharacters() {
        for (int i = 0; i < SAMPLES; i++) {
            String s = new RegexGenerator("^\\d{2}$").generate();
            assertEquals(2, s.length(), "expected 2 digits, got: " + s);
            for (char c : s.toCharArray()) assertTrue(Character.isDigit(c));
        }
    }

    // ── Optional quantifier ───────────────────────────────────────────────────

    @Test
    @DisplayName("same seed produces same output on every call")
    void seededReproducibility() {
        RegexGenerator g1 = new RegexGenerator("\\d{4}[A-Z]{3}", seeded(99L));
        RegexGenerator g2 = new RegexGenerator("\\d{4}[A-Z]{3}", seeded(99L));
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generate(), g2.generate(), "seeded generators must produce identical output");
        }
    }

    @Test
    @DisplayName("parsed pattern cache reuses parse tree across instances")
    void parsedPatternCacheReused() {
        RegexGenerator.clearParsedPatternCacheForTests();
        assertEquals(0, RegexGenerator.parsedPatternCacheSize());

        new RegexGenerator("\\d{4}[A-Z]{3}");
        assertEquals(1, RegexGenerator.parsedPatternCacheSize());

        new RegexGenerator("\\d{4}[A-Z]{3}");
        assertEquals(1, RegexGenerator.parsedPatternCacheSize());

        new RegexGenerator("[a-z]{2,5}");
        assertEquals(2, RegexGenerator.parsedPatternCacheSize());
    }

    @Test
    @DisplayName("cache clear removes previously parsed patterns")
    void cacheClear() {
        RegexGenerator.clearParsedPatternCacheForTests();
        new RegexGenerator("\\d{2}");
        assertEquals(1, RegexGenerator.parsedPatternCacheSize());
        RegexGenerator.clearParsedPatternCacheForTests();
        assertEquals(0, RegexGenerator.parsedPatternCacheSize());
    }

    // ── Literal concatenation ─────────────────────────────────────────────────

    @Test
    @DisplayName("{2,5} quantifier produces strings with length in [2,5]")
    void rangeQuantifier() {
        RegexGenerator gen = new RegexGenerator("[a-z]{2,5}");
        for (int i = 0; i < SAMPLES; i++) {
            int len = gen.generate().length();
            assertTrue(len >= 2 && len <= 5, "expected length in [2,5], got: " + len);
        }
    }

    // ── Malformed patterns ────────────────────────────────────────────────────

    @Test
    @DisplayName("[ with nothing after it throws IllegalArgumentException")
    void bracketAtEndOfPatternThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("["));
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("[a-] — dash immediately before ']' is a literal hyphen, not a range")
    void dashBeforeClosingBracketIsLiteral() {
        // src.charAt(pos + 1) == ']' makes the range condition false → '-' is a literal
        RegexGenerator gen = new RegexGenerator("[a-]");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        assertEquals(Set.of("a", "-"), seen, "[a-] must produce only 'a' or '-'");
    }

    // ── Range quantifier {n,m} ────────────────────────────────────────────────

    @Test
    @DisplayName("(hello) — single-alternative group returns SequenceNode, not AlternationNode")
    void singleAlternativeGroup() {
        // alts.size() == 1 → returns alts.get(0) directly (no AlternationNode wrapper)
        RegexGenerator gen = new RegexGenerator("(hello)");
        for (int i = 0; i < SAMPLES; i++) {
            assertEquals("hello", gen.generate(), "single-alternative group must produce 'hello'");
        }
    }

    // ── Escape sequences ──────────────────────────────────────────────────────

    @Test
    @DisplayName("{n, at end of pattern throws IllegalArgumentException (no max number)")
    void openEndedQuantifierAtEndOfPatternThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("\\d{3,"));
    }

    // ── Character class variants ──────────────────────────────────────────────

    @Test
    @DisplayName("unclosed group throws IllegalArgumentException")
    void unclosedGroupThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(abc|def"));
    }

    @Test
    @DisplayName("trailing backslash throws IllegalArgumentException")
    void trailingBackslashThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("\\d{3}\\"));
    }

    @Test
    @DisplayName("unexpected closing paren at top level throws IllegalArgumentException")
    void unexpectedClosingParenThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("abc)def"));
    }

    // ── Single-alternative group ──────────────────────────────────────────────

    @Test
    @DisplayName("{ with no digits (bad quantifier) throws IllegalArgumentException")
    void badQuantifierNoDigitsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("\\d{abc}"));
    }

    // ── Quantifier edge cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("null pattern throws NullPointerException")
    void nullPatternThrows() {
        assertThrows(NullPointerException.class, () -> new RegexGenerator(null));
    }

    @Nested
    @DisplayName("\\d{3} — exactly 3 digits")
    class DigitExact {

        @Test
        @DisplayName("returns a non-null, 3-character string")
        void nonNullLength() {
            String s = new RegexGenerator("\\d{3}").generate();
            assertNotNull(s);
            assertEquals(3, s.length(), "expected length 3, got: " + s);
        }

        @Test
        @DisplayName("all characters are ASCII digits")
        void allDigits() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("\\d{3}").generate();
                for (char c : s.toCharArray()) {
                    assertTrue(Character.isDigit(c), "expected digit but got '" + c + "' in: " + s);
                }
            }
        }
    }

    @Nested
    @DisplayName("[A-Z]{5} — exactly 5 uppercase letters")
    class UppercaseExact {

        @Test
        @DisplayName("returns exactly 5 characters")
        void length5() {
            String s = new RegexGenerator("[A-Z]{5}").generate();
            assertEquals(5, s.length(), "expected length 5, got: " + s);
        }

        @Test
        @DisplayName("all characters are uppercase letters")
        void allUppercase() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("[A-Z]{5}").generate();
                for (char c : s.toCharArray()) {
                    assertTrue(c >= 'A' && c <= 'Z', "expected uppercase A-Z but got '" + c + "' in: " + s);
                }
            }
        }
    }

    // ── Invalid patterns → IllegalArgumentException ───────────────────────────

    @Nested
    @DisplayName("(yes|no) — alternation")
    class Alternation {

        @Test
        @DisplayName("generates only 'yes' or 'no'")
        void onlyYesOrNo() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("(yes|no)").generate();
                assertTrue(s.equals("yes") || s.equals("no"), "expected 'yes' or 'no', got: " + s);
            }
        }

        @Test
        @DisplayName("both alternatives appear over many calls")
        void bothAlternativesAppear() {
            Set<String> seen = new HashSet<>();
            RegexGenerator gen = new RegexGenerator("(yes|no)");
            for (int i = 0; i < 100; i++) seen.add(gen.generate());
            assertEquals(Set.of("yes", "no"), seen, "both 'yes' and 'no' must appear");
        }
    }

    @Nested
    @DisplayName("\\w+ — one or more word characters")
    class WordPlus {

        @Test
        @DisplayName("result is non-empty")
        void nonEmpty() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("\\w+").generate();
                assertFalse(s.isEmpty(), "\\w+ must produce at least one character");
            }
        }

        @Test
        @DisplayName("all characters are word characters (a-z, A-Z, 0-9, _)")
        void allWordChars() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("\\w+").generate();
                for (char c : s.toCharArray()) {
                    assertTrue(Character.isLetterOrDigit(c) || c == '_', "non-word character '" + c + "' in: " + s);
                }
            }
        }
    }

    @Nested
    @DisplayName("Escape sequences")
    class EscapeSequences {

        @Test
        @DisplayName("\\D produces a non-digit character")
        void nonDigitEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("\\D").generate();
                assertEquals(1, s.length(), "\\D must produce exactly 1 char");
                assertFalse(Character.isDigit(s.charAt(0)), "\\D must not produce a digit, got: " + s);
            }
        }

        @Test
        @DisplayName("\\W produces a non-word character")
        void nonWordEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\W").generate().charAt(0);
                assertFalse(Character.isLetterOrDigit(c) || c == '_', "\\W must not produce a word char, got: " + c);
            }
        }

        @Test
        @DisplayName("\\s produces a whitespace character")
        void whitespaceEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\s").generate().charAt(0);
                assertTrue(
                        c == ' ' || c == '\t' || c == '\n' || c == '\r',
                        "\\s must produce whitespace, got: '" + c + "'");
            }
        }

        @Test
        @DisplayName("\\S produces a non-whitespace character")
        void nonWhitespaceEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\S").generate().charAt(0);
                assertFalse(
                        c == ' ' || c == '\t' || c == '\n' || c == '\r',
                        "\\S must not produce whitespace, got: '" + c + "'");
            }
        }

        @Test
        @DisplayName("\\. produces a literal dot")
        void dotEscape() {
            assertEquals(".", new RegexGenerator("\\.").generate());
        }

        @Test
        @DisplayName("\\- produces a literal hyphen")
        void hyphenEscape() {
            assertEquals("-", new RegexGenerator("\\-").generate());
        }

        @Test
        @DisplayName("\\\\ produces a literal backslash")
        void backslashEscape() {
            assertEquals("\\", new RegexGenerator("\\\\").generate());
        }

        @Test
        @DisplayName("\\n produces a newline character")
        void newlineEscape() {
            assertEquals("\n", new RegexGenerator("\\n").generate());
        }

        @Test
        @DisplayName("\\r produces a carriage-return character")
        void crEscape() {
            assertEquals("\r", new RegexGenerator("\\r").generate());
        }

        @Test
        @DisplayName("\\t produces a tab character")
        void tabEscape() {
            assertEquals("\t", new RegexGenerator("\\t").generate());
        }
    }

    @Nested
    @DisplayName("Character class variants")
    class CharClassVariants {

        @Test
        @DisplayName("[abc] (literal char class) produces only a, b, or c")
        void literalCharClass() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("[abc]").generate();
                assertEquals(1, s.length());
                assertTrue(s.equals("a") || s.equals("b") || s.equals("c"), "expected a/b/c, got: " + s);
            }
        }

        @Test
        @DisplayName("[abc] — all three alternatives appear over many calls")
        void literalCharClassAllAppear() {
            var seen = new HashSet<String>();
            RegexGenerator gen = new RegexGenerator("[abc]");
            for (int i = 0; i < 200; i++) seen.add(gen.generate());
            assertEquals(Set.of("a", "b", "c"), seen);
        }

        @Test
        @DisplayName("[^aeiou] produces no vowels")
        void negatedCharClass() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("[^aeiou]").generate().charAt(0);
                assertFalse("aeiou".indexOf(c) >= 0, "negated class must exclude vowels, got: " + c);
            }
        }

        @Test
        @DisplayName("negated class [^aeiou] produces printable non-vowel characters")
        void negatedCharClassPrintable() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("[^aeiou]").generate().charAt(0);
                assertTrue(c >= 0x20 && c < 0x7f, "must be printable ASCII, got: " + (int) c);
            }
        }

        @Test
        @DisplayName("empty char class [] throws IllegalArgumentException")
        void emptyCharClassThrows() {
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("[]"));
        }
    }

    @Nested
    @DisplayName("Additional quantifiers")
    class AdditionalQuantifiers {

        @Test
        @DisplayName("* quantifier produces 0–10 characters")
        void starQuantifier() {
            RegexGenerator gen = new RegexGenerator("[a-z]*");
            for (int i = 0; i < SAMPLES; i++) {
                int len = gen.generate().length();
                assertTrue(len >= 0 && len <= 10, "* must give 0-10 chars, got: " + len);
            }
        }

        @Test
        @DisplayName("{n,} (unbounded) caps at 10 repetitions")
        void unboundedQuantifier() {
            RegexGenerator gen = new RegexGenerator("\\d{2,}");
            for (int i = 0; i < SAMPLES; i++) {
                int len = gen.generate().length();
                assertTrue(len >= 2 && len <= 10, "{2,} must give 2-10 chars, got: " + len);
            }
        }

        @Test
        @DisplayName("*, + and {n,} repeat at most 10 times by default, or n times when n is more")
        void openEndedDefaultLimit() {
            assertEquals(10, maxLength(new RegexGenerator("a+", seeded(1L))));
            assertEquals(10, maxLength(new RegexGenerator("a*", seeded(1L))));
            assertEquals(10, maxLength(new RegexGenerator("a{2,}", seeded(1L))));
            assertEquals(10, maxLength(new RegexGenerator("a+")));
            assertEquals("a".repeat(10), new RegexGenerator("a{10,}").generate());
            assertEquals("a".repeat(20), new RegexGenerator("a{20,}").generate());
        }

        @Test
        @DisplayName("the configured minimum string length raises the open-ended limit to twice itself")
        void openEndedLimitFollowsMinimumStringLength() {
            GeneratorConfig config =
                    GeneratorConfig.builder().seed(1L).stringLength(20, 25).build();
            assertEquals(40, maxLength(new RegexGenerator("a+", config)));
            assertEquals(40, maxLength(new RegexGenerator("a{30,}", config)));
            assertEquals("a".repeat(50), new RegexGenerator("a{50,}", config).generate());

            GeneratorConfig defaultLength =
                    GeneratorConfig.builder().seed(1L).stringLength(5, 25).build();
            assertEquals(10, maxLength(new RegexGenerator("a+", defaultLength)));
            GeneratorConfig shorter =
                    GeneratorConfig.builder().seed(1L).stringLength(1, 2).build();
            assertEquals(10, maxLength(new RegexGenerator("a+", shorter)));
        }

        @Test
        @DisplayName("the open-ended limit stops at the repetition cap")
        void openEndedLimitStopsAtRepetitionCap() {
            GeneratorConfig config = GeneratorConfig.builder()
                    .seed(1L)
                    .stringLength(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1)
                    .build();
            int cap = RegexGenerator.MAX_REPETITIONS;
            assertEquals(
                    cap,
                    new RegexGenerator("a{" + cap + ",}", config).generate().length());
            // A minimum this large must not overflow into a small limit either.
            assertTrue(maxLength(new RegexGenerator("a+", config), 20) > 10);
        }

        private static int maxLength(RegexGenerator generator) {
            return maxLength(generator, 5_000);
        }

        private static int maxLength(RegexGenerator generator, int draws) {
            int longest = 0;
            for (int i = 0; i < draws; i++) {
                longest = Math.max(longest, generator.generate().length());
            }
            return longest;
        }
    }

    // ── The matching contract ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Every value matches or construction fails")
    class MatchingContract {

        private static final int SEEDS = 5;
        private static final int DRAWS = 100;

        /** Draws values across several seeds and has java.util.regex itself judge each one. */
        private static void assertValuesMatch(
                String regex, int seeds, int draws, LongFunction<Generator<String>> generatorForSeed) {
            Pattern compiled = Pattern.compile(regex);
            for (long seed = 0; seed < seeds; seed++) {
                Generator<String> generator = generatorForSeed.apply(seed);
                for (int draw = 0; draw < draws; draw++) {
                    String value = generator.generate();
                    assertTrue(
                            compiled.matcher(value).matches(),
                            () -> "\"" + value + "\" does not match the pattern " + regex);
                }
            }
        }

        private static void assertEveryValueMatches(String regex, int seeds, int draws) {
            assertValuesMatch(regex, seeds, draws, seed -> new RegexGenerator(regex, seeded(seed)));
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    // The reported cases
                    "^(?:abc)$",
                    "\\bfoo\\b",
                    "[\\d]",
                    "a{20,}",
                    "a|b",
                    "^[a-z]+$",
                    // Groups
                    "(abc)",
                    "(a|b)c",
                    "(?:a|b)c",
                    "(?<name>abc)",
                    "(?<n1>a|b)+-(?<n2>c)",
                    "()",
                    "(?:)",
                    "(|a)",
                    "(a|)",
                    // Anchors
                    "",
                    "^",
                    "$",
                    "^$",
                    "^abc",
                    "abc$",
                    "^abc$",
                    "\\Aabc\\z",
                    "abc\\Z",
                    "^^a$$",
                    "(^a|b)c",
                    "a(b$|c$)",
                    "^a$|^b$",
                    "(^a$)|(^b$)",
                    "(?:^|,)x",
                    "x(?:,|$)",
                    "^*a",
                    "a$*",
                    "(^)+a",
                    "(^a)?b",
                    "a(b$)?",
                    "(?:^)*(?:a|b)(?:$)*",
                    // Word boundaries
                    "\\bfoo",
                    "foo\\b",
                    "\\b(foo|bar)\\b",
                    "\\b\\d+\\b",
                    "\\b\\w+\\b",
                    "\\b[a-z]+-[a-z]+\\b",
                    "a\\b-",
                    "-\\ba",
                    "^\\bfoo\\b$",
                    "(?:\\bfoo\\b-)+",
                    "\\b{2}a",
                    "(?:a\\b-|-\\bb)",
                    "\\b_\\b",
                    // Escapes outside a class
                    "\\d\\D\\w\\W\\s\\S",
                    "\\.\\-\\\\\\/\\@\\_\\(\\)\\[\\]\\{\\}\\|\\?\\*\\+\\^\\$",
                    "\\t\\n\\r\\f\\a\\e",
                    "\\x41",
                    "\\x{41}",
                    "\\x{1D11E}+",
                    "\\u0041",
                    "\\u00e9{2}",
                    "\uD834\uDD1E{2}",
                    "\\\uD834\uDD1E",
                    "\\\u00e9",
                    "\uE000",
                    // Any character
                    ".",
                    "a.c",
                    ".{5}",
                    ".*",
                    ".+?",
                    // Classes
                    "[abc]",
                    "[a-c]",
                    "[^abc]",
                    "[\\D]",
                    "[\\w]",
                    "[\\W]",
                    "[\\s]",
                    "[\\S]",
                    "[^\\d]",
                    "[^\\D]",
                    "[^\\w]",
                    "[^\\W]",
                    "[^\\s]",
                    "[^\\S]",
                    "[\\w.-]+",
                    "[\\.]",
                    "[\\-]",
                    "[a\\-z]",
                    "[\\]]",
                    "[\\[]",
                    "[\\\\]",
                    "[\\^]",
                    "[\\t\\n]",
                    "[\\x41-\\x43]",
                    "[\\u0041-\\u0043]",
                    "[\\x{41}]",
                    "[]a]",
                    "[^]a]",
                    "[a]]",
                    "[a-]",
                    "[-a]",
                    "[^-a]",
                    "[^^]",
                    "[a^]",
                    "[a-z-9]",
                    "[+--]",
                    "[--a]",
                    "[\\d-x]",
                    "[\\d-\\w]",
                    "[\\w-]",
                    "[a&b]",
                    "[&]",
                    "[!-&]",
                    "[!-&&a]",
                    "[(|)*+?{}$.]",
                    "[\u00e0-\u00ff]",
                    "[\u0430-\u044f]",
                    "[\\u4e00-\\u9fa5]+",
                    "[\\uFFFE-\\uFFFF]",
                    "[￾-￿]",
                    "[\\u0000-\\uFFFF]{5}",
                    "[\\uD7FF-\\uE000]{5}",
                    "[^@\\s]+@[^@\\s]+\\.[a-z]{2,}",
                    // Quantifiers
                    "a?",
                    "a*",
                    "a+",
                    "a{3}",
                    "a{3,}",
                    "a{3,5}",
                    "a{0}",
                    "a{0,0}",
                    "a{0,}",
                    "a{007}",
                    "a??",
                    "a*?",
                    "a+?",
                    "a{3}?",
                    "a{3,}?",
                    "a{3,5}?",
                    "(ab)+",
                    "(a|b){2,3}",
                    "(a?)+",
                    "(a?){2}",
                    "(|a){3}",
                    "(a|b?){2}",
                    "(a*)*",
                    "(abc){0}",
                    // Alternation
                    "a|b|c",
                    "|a",
                    "a|",
                    "|",
                    "(a|b)|c",
                    "^a|b$",
                    "abc|def|",
                    "(?:a|b)|(?:c|d)",
                    // Characters that are only special elsewhere
                    "}",
                    "]",
                    "a}",
                    "a]",
                    ",:=!#\"",
                    " ",
                    "&",
                    "-",
                    "<>",
                    // Case-insensitive matching, and where it starts and stops
                    "(?i)abc",
                    "(?i:abc)",
                    "(?-i)abc",
                    "a(?i)b|c",
                    "(a(?i)b)c",
                    "(?i:a)b",
                    "(?i)a(?-i)b",
                    "(?i)a(?-i:b)c",
                    "(?i)(a|(?-i)b)c",
                    "(?)a",
                    "(?-)a",
                    "(?i-)a",
                    "(?i-i)a",
                    "(?ii)a",
                    "a(?i)",
                    "(a(?i))b",
                    "(?i)",
                    "(?i:)",
                    "(?i:a)+",
                    "(?i)(yes|no)",
                    "(?i)[a-c]+",
                    "(?i)[aA]{3}",
                    "(?i)[Z-a]{3}",
                    "(?i)[^a-z]{3}",
                    "(?i)[^K]{3}",
                    "(?i)[^\\w]",
                    "(?i)[^\\W]{3}",
                    "(?i)\\w\\d\\s\\D\\W\\S.",
                    "(?i)\\x41\\u0062\\x{63}",
                    "(?i)[\\x41-\\x43]{3}",
                    "(?i)\u00e9\u00c9",
                    "(?i)k\u212a",
                    "(?i)\uD834\uDD1E",
                    "(?i)\\bfoo\\b",
                    "(?i)^[a-z]+$",
                    // Character properties
                    "\\p{Lower}\\p{Upper}\\p{ASCII}\\p{Alpha}\\p{Digit}\\p{Alnum}",
                    "\\p{Punct}\\p{Graph}\\p{Print}\\p{Blank}\\p{Space}\\p{XDigit}",
                    "\\p{L}\\p{Lu}\\p{Ll}\\p{N}\\p{Nd}",
                    "\\p{IsAlphabetic}\\p{IsDigit}\\p{IsLatin}",
                    "\\pL\\pN\\PL\\PN",
                    "\\pLu",
                    "\\p{L}+",
                    "\\p{Lu}\\p{Ll}{2,8}",
                    "[\\p{L}\\d_-]+",
                    "[\\p{L}-z]+",
                    "[^\\p{L}]+",
                    "[^\\P{L}]+",
                    "[\\P{Alpha}a]+",
                    "[\\P{ASCII}a]",
                    "[^\\P{ASCII}]",
                    "\\P{L}+",
                    "\\P{Graph}",
                    "\\P{Lower}\\P{Upper}\\P{Punct}\\P{XDigit}\\P{IsLatin}",
                    "\\b\\p{Alpha}+\\b",
                    "(?i)\\p{Lower}\\p{Upper}\\p{Lu}\\p{Ll}\\p{L}\\p{IsLatin}",
                    "(?i)\\P{Lower}\\P{Upper}\\P{Lu}\\P{Ll}\\P{L}\\P{XDigit}",
                    "(?i)[^\\p{Lower}][^\\p{Lu}][^\\P{Lower}][^\\P{Ll}]",
                    "(?i)[\\P{Lower}\\d][\\p{Ll}\\d]",
                    // Quoting
                    "\\Qa.b\\E",
                    "\\Qa.b",
                    "\\Q(a|b)*\\E",
                    "\\Qab\\E+",
                    "\\Qab\\E{2,3}",
                    "x\\Q\\E+",
                    "x\\Q\\E\\Q\\E{2}",
                    "\\Q\\E",
                    "\\Q1\\E",
                    "\\Q12\\E{2}",
                    "\\Q\\\\E",
                    "\\Qa\\",
                    "\\Q\\Q\\E",
                    "\\\\Q",
                    "\\\\\\Qa.\\E",
                    "\\Q\\Ea\\Q\\E",
                    "\\Q$^\\E",
                    "\\Q \t\\E+",
                    "\\Q\u00e9\uD834\uDD1E\\E{2}",
                    "(\\Qa|b\\E)",
                    "\\Qa\\E\\Qb\\E+",
                    "\\Qa\\E\\.\\Qb",
                    "[\\Qa-z\\E]",
                    "[\\Qa\\E-z]",
                    "[a\\Q-\\Ez]",
                    "[\\Q^]\\E]",
                    "[^\\Q-]\\E]",
                    "(?i)\\Qab\\E",
                    "\\Q(?i)\\Ea",
                })
        @DisplayName("every generated value matches a supported pattern")
        void supportedPatternsAlwaysMatch(String regex) {
            assertEveryValueMatches(regex, SEEDS, DRAWS);
        }

        @Test
        @DisplayName("the supported cases generate what their pattern means, not just something that matches")
        void supportedPatternsKeepTheirVariety() {
            assertEquals(Set.of("abc"), distinct("^(?:abc)$"));
            assertEquals(Set.of("foo"), distinct("\\bfoo\\b"));
            assertEquals(Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), distinct("[\\d]"));
            assertEquals(Set.of("a", "b"), distinct("a|b"));
            assertEquals(Set.of("a", "b"), distinct("^a$|^b$"));
            assertEquals(Set.of("", "a"), distinct("|a"));
            assertEquals(Set.of("ac", "bc"), distinct("(?:a|b)c"));
            assertEquals(Set.of("foo", "bar"), distinct("\\b(foo|bar)\\b"));
            assertEquals(Set.of("]", "a"), distinct("[]a]"));
            assertEquals(Set.of("a", "-", "z"), distinct("[a\\-z]"));
            assertEquals(Set.of("\t", "\n"), distinct("[\\t\\n]"));
            assertEquals(Set.of("A", "B", "C"), distinct("[\\x41-\\x43]"));
            assertEquals(Set.of("\uD834\uDD1E\uD834\uDD1E"), distinct("\uD834\uDD1E{2}"));

            Set<String> wordsDotsAndDashes = distinct("[\\w.-]");
            assertTrue(wordsDotsAndDashes.containsAll(Set.of("a", "Z", "0", "_", ".", "-")));
            assertFalse(wordsDotsAndDashes.contains("\\"));

            assertTrue(distinct(".").size() > 90, "'.' must draw from all of printable ASCII");
            assertEquals(Set.of("\uFFFE", "\uFFFF"), distinct("[\\uFFFE-\\uFFFF]"));
            assertEquals(Set.of("\uFFFE", "\uFFFF"), distinct("[\uFFFE-\uFFFF]"));
            assertFalse(
                    distinct("[\\uD7FF-\\uE000]").stream().anyMatch(value -> Character.isSurrogate(value.charAt(0))),
                    "a range across the surrogates must skip them");
        }

        @Test
        @DisplayName("the match check itself fails on a value that does not match")
        void matchCheckDetectsAMismatch() {
            assertThrows(AssertionError.class, () -> assertValuesMatch("a", SEEDS, DRAWS, seed -> () -> "b"));

            // A rare wrong value is enough: the check judges every draw of every seed.
            int[] draws = {0};
            int lastDraw = SEEDS * DRAWS;
            assertThrows(
                    AssertionError.class,
                    () -> assertValuesMatch("a", SEEDS, DRAWS, seed -> () -> ++draws[0] == lastDraw ? "b" : "a"));
            assertEquals(lastDraw, draws[0]);
        }

        @ParameterizedTest
        @MethodSource("unsupportedConstructs")
        @DisplayName("a construct that cannot be honoured fails at construction, and the message names it")
        void unsupportedConstructsFailAtConstruction(String regex, String construct) {
            // Each of these is valid java.util.regex syntax, so the rejection is about generating from it.
            Pattern.compile(regex);

            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(regex));

            assertTrue(exception.getMessage().contains(construct), exception.getMessage());
            assertTrue(exception.getMessage().contains(regex), exception.getMessage());
        }

        static Stream<Arguments> unsupportedConstructs() {
            return Stream.of(
                    // Groups
                    Arguments.of("(?=a)a", "Unsupported lookahead at position 0"),
                    Arguments.of("x(?!b)a", "Unsupported lookahead at position 1"),
                    Arguments.of("(?<=a)b", "Unsupported lookbehind"),
                    Arguments.of("(?<!b)a", "Unsupported lookbehind"),
                    Arguments.of("(?>a|ab)c", "Unsupported atomic group"),
                    Arguments.of("(a)\\1", "Unsupported backreference \\1"),
                    Arguments.of("(?<n>a)\\k<n>", "Unsupported backreference \\k"),
                    // Flags other than i, named, whether turned on or off
                    Arguments.of("(?s).", "Unsupported inline flag s at position 0"),
                    Arguments.of("(?m)^a$", "Unsupported inline flag m at position 0"),
                    Arguments.of("(?x)a b", "Unsupported inline flag x at position 0"),
                    Arguments.of("(?u)a", "Unsupported inline flag u at position 0"),
                    Arguments.of("(?U)\\w", "Unsupported inline flag U at position 0"),
                    Arguments.of("(?d).", "Unsupported inline flag d at position 0"),
                    Arguments.of("(?c)a", "Unsupported inline flag c at position 0"),
                    Arguments.of("(?iu)a", "Unsupported inline flag u at position 0"),
                    Arguments.of("(?-s)a", "Unsupported inline flag s at position 0"),
                    Arguments.of("(?i-x)a", "Unsupported inline flag x at position 0"),
                    Arguments.of("x(?s:a)", "Unsupported inline flag s at position 1"),
                    Arguments.of("(?i)(?is:a)", "Unsupported inline flag s at position 4"),
                    Arguments.of("a(?ss)", "Unsupported inline flag s at position 1"),
                    Arguments.of("a(?sm)", "Unsupported inline flags s, m at position 1"),
                    Arguments.of("(?ims-dux:a)", "Unsupported inline flags m, s, d, u, x at position 0"),
                    Arguments.of("(?i){2}a", "Unsupported quantifier with nothing to repeat at position 4"),
                    // Quantifiers
                    Arguments.of("a?+", "Unsupported possessive quantifier"),
                    Arguments.of("a*+", "Unsupported possessive quantifier"),
                    Arguments.of("a++", "Unsupported possessive quantifier at position 1"),
                    Arguments.of("a{3}+", "Unsupported possessive quantifier"),
                    Arguments.of("a{3,}+", "Unsupported possessive quantifier"),
                    Arguments.of("a{3,5}+", "Unsupported possessive quantifier"),
                    Arguments.of("a{2}{3}", "Unsupported quantifier with nothing to repeat at position 4"),
                    Arguments.of("a+{3}", "Unsupported quantifier with nothing to repeat"),
                    Arguments.of("{2}", "Unsupported quantifier with nothing to repeat"),
                    Arguments.of("{2}a", "Unsupported quantifier with nothing to repeat"),
                    Arguments.of("a{10001}", "exceeds the maximum supported value 10000"),
                    Arguments.of("a{1,10001}", "exceeds the maximum supported value 10000"),
                    Arguments.of("a{10001,}", "exceeds the maximum supported value 10000"),
                    // Assertions that a generated value cannot always meet
                    Arguments.of("a^b", "Unsupported ^ that cannot hold on every value at position 1"),
                    Arguments.of("a*^b", "Unsupported ^"),
                    Arguments.of("a(^b)", "Unsupported ^"),
                    Arguments.of("x(^|y)", "Unsupported ^"),
                    Arguments.of("(^a)+", "Unsupported ^"),
                    Arguments.of("(a|^b)+", "Unsupported ^"),
                    Arguments.of("a\\Ab", "Unsupported \\A"),
                    Arguments.of("a$b", "Unsupported $ that cannot hold on every value at position 1"),
                    Arguments.of("b$a*", "Unsupported $"),
                    Arguments.of("(a$|b)c", "Unsupported $"),
                    Arguments.of("(a$)+", "Unsupported $"),
                    Arguments.of("(a$|b)+", "Unsupported $"),
                    Arguments.of("a$^b", "Unsupported $"),
                    Arguments.of("a\\zb", "Unsupported \\z"),
                    Arguments.of("a\\Zb", "Unsupported \\Z"),
                    Arguments.of("\\b", "Unsupported \\b that cannot hold on every value at position 0"),
                    Arguments.of("a\\bb", "Unsupported \\b"),
                    Arguments.of("-\\b-", "Unsupported \\b"),
                    Arguments.of("\\b-", "Unsupported \\b"),
                    Arguments.of("-\\b", "Unsupported \\b"),
                    Arguments.of("\\ba?", "Unsupported \\b"),
                    Arguments.of("a?\\b", "Unsupported \\b"),
                    Arguments.of("\\b\\w*\\b", "Unsupported \\b"),
                    Arguments.of("\\b[a-z -]+\\b", "Unsupported \\b"),
                    Arguments.of("(?:a|-)\\bb", "Unsupported \\b"),
                    Arguments.of("(?:\\bfoo\\b)+", "Unsupported \\b"),
                    Arguments.of("\\b\u00e9", "Unsupported \\b"),
                    Arguments.of("\\b[a\u00e9]", "Unsupported \\b"),
                    Arguments.of("a\\b\u0080", "Unsupported \\b"),
                    Arguments.of("\\b{g}", "Unsupported grapheme boundary"),
                    // Escapes with no generated meaning here
                    Arguments.of("\\B", "Unsupported escape \\B"),
                    Arguments.of("\\Gabc", "Unsupported escape \\G"),
                    Arguments.of("\\h", "Unsupported escape \\h"),
                    Arguments.of("\\H", "Unsupported escape \\H"),
                    Arguments.of("\\v", "Unsupported escape \\v"),
                    Arguments.of("\\V", "Unsupported escape \\V"),
                    Arguments.of("\\R", "Unsupported escape \\R"),
                    Arguments.of("\\X", "Unsupported escape \\X"),
                    Arguments.of("\\011", "Unsupported escape \\0"),
                    Arguments.of("\\cA", "Unsupported escape \\c"),
                    Arguments.of("\\N{DIGIT ONE}", "Unsupported escape \\N"),
                    // Properties outside the documented set, including other spellings java.util.regex accepts
                    Arguments.of("\\p{IsLower}", "Unsupported character property \\p{IsLower} at position 0"),
                    Arguments.of("\\p{Islatin}", "Unsupported character property \\p{Islatin} at position 0"),
                    Arguments.of("\\p{IsL}", "Unsupported character property \\p{IsL} at position 0"),
                    Arguments.of("\\p{gc=Lu}", "Unsupported character property \\p{gc=Lu} at position 0"),
                    Arguments.of("\\p{InGreek}", "Unsupported character property \\p{InGreek} at position 0"),
                    Arguments.of("\\p{javaLowerCase}", "Unsupported character property \\p{javaLowerCase}"),
                    Arguments.of("\\p{Cntrl}", "Unsupported character property \\p{Cntrl}"),
                    Arguments.of("\\p{Lt}", "Unsupported character property \\p{Lt}"),
                    Arguments.of("a\\pM", "Unsupported character property \\pM at position 1"),
                    Arguments.of("\\PM", "Unsupported character property \\PM at position 0"),
                    Arguments.of("\\P{Sc}", "Unsupported character property \\P{Sc}"),
                    Arguments.of(
                            "\\P{ASCII}",
                            "Unsupported character property \\P{ASCII} that excludes all of printable ASCII at position 0"),
                    Arguments.of("(?i)\\P{Print}", "Unsupported character property \\P{Print} that excludes all"),
                    // Positions inside and after a quote are those of the pattern as written
                    Arguments.of("\\Qab\\E(?=c)", "Unsupported lookahead at position 6"),
                    Arguments.of("\\Q.\\E\\b", "Unsupported \\b that cannot hold on every value at position 5"),
                    Arguments.of("\\Q1\\E(?<=a)", "Unsupported lookbehind at position 5"),
                    Arguments.of("\\Qa(\\E\uD834", "Unsupported surrogate at position 6"),
                    Arguments.of("\\Qa\uDD1E\\E", "Unsupported surrogate at position 3"),
                    // Classes
                    Arguments.of("[a\\p{InGreek}]", "Unsupported character property \\p{InGreek} at position 2"),
                    Arguments.of("[\\P{ASCII}]", "Unsupported character class that excludes all of printable ASCII"),
                    Arguments.of(
                            "x[\\P{Print}\\P{ASCII}]",
                            "Unsupported character class that excludes all of printable ASCII at position 1"),
                    Arguments.of("[^\\p{Print}]", "Unsupported negated character class that excludes all"),
                    Arguments.of("[^\\P{Lower}\\p{Lower}]", "Unsupported negated character class that excludes all"),
                    Arguments.of("(?i)[^\\P{Lower}A-Z]", "Unsupported negated character class that excludes all"),
                    Arguments.of("[\\h]", "Unsupported escape \\h"),
                    Arguments.of("[\\Q\uD834\uDD1E\\E]", "Unsupported supplementary character in a character class"),
                    Arguments.of("[a-c[x-z]]", "Unsupported nested character class"),
                    Arguments.of("[+-[x]]", "Unsupported nested character class"),
                    Arguments.of("[a-z&&[def]]", "Unsupported character class intersection"),
                    Arguments.of("[a-z&&[^m-p]]", "Unsupported character class intersection"),
                    Arguments.of("[&&a]", "Unsupported character class intersection"),
                    Arguments.of("[a&&]", "Unsupported character class intersection"),
                    Arguments.of("[\uD834\uDD1E]", "Unsupported supplementary character"),
                    Arguments.of("[a-\uD834\uDD1E]", "Unsupported supplementary character"),
                    Arguments.of("[\\x{1D11E}]", "Unsupported supplementary character"),
                    Arguments.of("[^ -~]", "Unsupported negated character class"),
                    Arguments.of("[^\\x00-\\x7F]", "Unsupported negated character class"),
                    // Surrogates, which could join up in generated text
                    Arguments.of("\\uD834", "Unsupported surrogate"),
                    Arguments.of("\\uD834\\uDD1E", "Unsupported surrogate"),
                    Arguments.of("\uD834", "Unsupported surrogate"),
                    Arguments.of("\uDD1E", "Unsupported surrogate"),
                    Arguments.of("(\uD834|\uDD1E)+", "Unsupported surrogate"),
                    Arguments.of("[\\uD800-\\uDFFF]", "Unsupported surrogate"));
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "a{5,2}",
                    "[abc",
                    "(abc",
                    "abc)",
                    "\\",
                    "[",
                    "[]",
                    "[^]",
                    "a{",
                    "{",
                    "\\d{3",
                    "\\d{3,",
                    "\\d{3,5",
                    "\\d{1,2x",
                    "\\d{3x",
                    "a{,3}",
                    "a{99999999999}",
                    "a{1234567890123456789}",
                    "*a",
                    "+a",
                    "?a",
                    "a**",
                    "a+++",
                    "a{2}*",
                    "\\q",
                    "\\E",
                    "[z-a]",
                    "[a-\\d]",
                    "[\\b]",
                    "(?q)a",
                    "(?<1a>b)",
                    "(?i-s-m)a",
                    "\\p",
                    "\\p{}",
                    "\\p{L",
                    "\\p{lower}",
                    "[a-\\pL]",
                    "[a-\\p{L}]",
                    "\\x4\\Q1\\E",
                    "\\Qa\\E\\",
                })
        @DisplayName("a malformed pattern fails at construction with java.util.regex's own error")
        void malformedPatternsFailAtConstruction(String regex) {
            assertThrows(PatternSyntaxException.class, () -> new RegexGenerator(regex));
            assertThrows(PatternSyntaxException.class, () -> new RegexGenerator(regex, seeded(1L)));
        }

        @Test
        @DisplayName("a rejected pattern is not cached, so it fails every time")
        void rejectedPatternIsNotCached() {
            RegexGenerator.clearParsedPatternCacheForTests();
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(?=a)a"));
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(?=a)a"));
            assertEquals(0, RegexGenerator.parsedPatternCacheSize());
        }

        @ParameterizedTest
        @MethodSource("seededOutputOfTheEarlierParser")
        @DisplayName("a pattern the earlier parser got right keeps its seeded values")
        void seededOutputIsKept(String regex, List<String> expected) {
            RegexGenerator generator = new RegexGenerator(regex, seeded(42L));
            assertEquals(expected, generator.generateList(expected.size()));
        }

        static Stream<Arguments> seededOutputOfTheEarlierParser() {
            return Stream.of(
                    Arguments.of("\\d{4}[A-Z]{3}", List.of("0384ARN", "8932AIG", "6260LZO")),
                    Arguments.of("[a-z]{2,5}", List.of("hwma", "nqdpa", "igu")),
                    Arguments.of("(yes|no)-\\d", List.of("no-3", "no-4", "yes-5")),
                    Arguments.of("[^aeiou]{3}", List.of("t#2", "L 9", "%Fs")),
                    Arguments.of("\\w+", List.of("m", "r2zYMIMM1", "ai7QHCS")),
                    Arguments.of("\\D\\W\\S", List.of("f!=", "G)d", " @z")),
                    Arguments.of("^[a-z]+$", List.of("h", "marnqdpaa", "guewilz")),
                    Arguments.of("\\d{2,}", List.of("3840558932", "6262603903", "3310876060")),
                    Arguments.of("[a-c]?z", List.of("az", "cz", "z")),
                    Arguments.of("(ab|c)*", List.of("abcababcabc", "abcababababcabcc", "cabababccabcab")),
                    // A repeated class member keeps its extra weight.
                    Arguments.of("[aab]{4}", List.of("baab", "aabb", "abbb")));
        }

        private static final String[] ATOMS = {
            "a", "b", "-", " ", "_", "1", ".", "\\d", "\\w", "\\s", "\\D", "\\W", "\\S", "[ab]", "[a -]", "[^a]",
            "[\\d-]", "[\\W]", "^", "$", "\\b", "\\A", "\\z", "\\Z",
        };
        private static final String[] ATOM_QUANTIFIERS = {
            "", "", "", "", "?", "*", "+", "{2}", "{0,2}", "{1,}", "{0}", "??", "*?", "+?", "{1,2}?",
        };
        // No open-ended group: java.util.regex can take years to check a value against ((\w+)*)*.
        private static final String[] GROUP_QUANTIFIERS = {"", "", "?", "{2}", "{0,2}", "{0}", "??", "{1,2}?"};

        private static String randomPattern(Random random, int depth) {
            StringBuilder pattern = new StringBuilder();
            int alternatives = random.nextInt(4) == 0 ? 2 : 1;
            for (int alternative = 0; alternative < alternatives; alternative++) {
                if (alternative > 0) {
                    pattern.append('|');
                }
                int terms = random.nextInt(4);
                for (int term = 0; term < terms; term++) {
                    if (depth > 0 && random.nextInt(4) == 0) {
                        pattern.append(random.nextBoolean() ? "(" : "(?:")
                                .append(randomPattern(random, depth - 1))
                                .append(')')
                                .append(GROUP_QUANTIFIERS[random.nextInt(GROUP_QUANTIFIERS.length)]);
                    } else {
                        pattern.append(ATOMS[random.nextInt(ATOMS.length)])
                                .append(ATOM_QUANTIFIERS[random.nextInt(ATOM_QUANTIFIERS.length)]);
                    }
                }
            }
            return pattern.toString();
        }

        @Test
        @DisplayName("a random pattern is either rejected by name or generates only matching values")
        void randomPatternsAlwaysMatchOrFailAtConstruction() {
            Random random = new Random(20261031L);
            int accepted = 0;
            int rejected = 0;
            for (int i = 0; i < 5_000; i++) {
                String regex = randomPattern(random, 3);
                try {
                    new RegexGenerator(regex);
                    accepted++;
                } catch (IllegalArgumentException exception) {
                    assertTrue(exception.getMessage().startsWith("Unsupported "), exception.getMessage());
                    rejected++;
                    continue;
                }
                assertEveryValueMatches(regex, 2, 25);
            }
            // Neither branch above can have gone quiet.
            assertTrue(accepted > 2_000, "accepted " + accepted);
            assertTrue(rejected > 200, "rejected " + rejected);
        }
    }

    // ── Case-insensitive matching ─────────────────────────────────────────────

    @Nested
    @DisplayName("(?i)")
    class CaseInsensitive {

        @Test
        @DisplayName("an ASCII letter takes either case, and every value matches")
        void asciiLettersTakeEitherCase() {
            assertEquals(Set.of("ab", "aB", "Ab", "AB"), distinct("(?i)ab"));
            assertEquals(Set.of("ab", "aB", "Ab", "AB"), distinct("(?i:ab)"));
            assertEquals(Set.of("a", "A"), distinct("(?i)\\x41|(?i)\\u0061"));
            assertEquals(Set.of("1-_"), distinct("(?i)1-_"));
            assertEquals(Set.of("a", "b", "c", "A", "B", "C"), distinct("(?i)[a-c]"));
            assertEquals(Set.of("a", "A"), distinct("(?i)[aA]"));
            assertEquals(Set.of("a", "A"), distinct("(?i)\\Qa\\E"));
        }

        @Test
        @DisplayName("as java.util.regex does without UNICODE_CASE, only ASCII letters fold")
        void onlyAsciiLettersFold() {
            String smallEAcute = Character.toString(0xE9);
            String capitalEAcute = Character.toString(0xC9);
            assertEquals(Set.of(smallEAcute), distinct("(?i)" + smallEAcute));
            assertEquals(Set.of(capitalEAcute), distinct("(?i)[" + capitalEAcute + "]"));
            // Under UNICODE_CASE, k would also match the Kelvin sign and s the long s.
            assertEquals(Set.of("k", "K"), distinct("(?i)k"));
            assertEquals(Set.of("s", "S"), distinct("(?i)s"));
            Set<String> letters = distinct("(?i)[a-z]");
            assertEquals(52, letters.size());
            assertFalse(letters.contains(Character.toString(0x212A)));
            assertFalse(letters.contains(Character.toString(0x17F)));
        }

        @Test
        @DisplayName("a flag holds to the end of its group, across |, and (?flags:X) only inside X")
        void flagsKeepToTheirGroup() {
            assertEquals(Set.of("ab", "aB", "c", "C"), distinct("a(?i)b|c"));
            assertEquals(Set.of("abc", "aBc"), distinct("(a(?i)b)c"));
            assertEquals(Set.of("ab", "Ab"), distinct("(?i:a)b"));
            assertEquals(Set.of("ab", "Ab"), distinct("(?i)a(?-i)b"));
            assertEquals(Set.of("abc", "Abc", "abC", "AbC"), distinct("(?i)a(?-i:b)c"));
            assertEquals(Set.of("ac", "Ac", "aC", "AC", "bc", "bC"), distinct("(?i)(a|(?-i)b)c"));
            assertEquals(Set.of("ab"), distinct("(a(?i))b"));
        }

        @Test
        @DisplayName("flag groups with nothing to set, or that set and clear i, change nothing")
        void flagGroupsThatChangeNothing() {
            assertEquals(Set.of("a"), distinct("(?)a"));
            assertEquals(Set.of("a"), distinct("(?-)a"));
            assertEquals(Set.of("a"), distinct("(?i-i)a"));
            assertEquals(Set.of("a"), distinct("(?-i)a"));
            assertEquals(Set.of("a", "A"), distinct("(?ii)a"));
            assertEquals(Set.of("a", "A"), distinct("(?i-)a"));
            assertEquals(Set.of("a"), distinct("a(?i)"));
            assertEquals(Set.of(""), distinct("(?i)"));
            assertEquals(Set.of(""), distinct("(?i:)"));
        }

        @Test
        @DisplayName("a negated class leaves out both cases of each letter it names")
        void negatedClassesLeaveOutBothCases() {
            Set<String> withoutK = distinct("(?i)[^K]");
            assertFalse(withoutK.contains("K") || withoutK.contains("k"));
            assertEquals(93, withoutK.size());
            assertTrue(distinct("[^K]").contains("k"));

            Set<String> noLetters = distinct("(?i)[^a-z]");
            assertTrue(
                    noLetters.stream().noneMatch(value -> Character.isLetter(value.charAt(0))), noLetters.toString());
            assertEquals(95 - 52, noLetters.size());
            assertTrue(distinct("[^a-z]").contains("A"));
        }

        @Test
        @DisplayName("predefined classes and . are the same under (?i)")
        void predefinedClassesAreUnchanged() {
            assertEquals(distinct("\\w"), distinct("(?i)\\w"));
            assertEquals(distinct("\\W"), distinct("(?i)\\W"));
            assertEquals(distinct("\\D"), distinct("(?i)\\D"));
            assertEquals(distinct("."), distinct("(?i)."));
        }
    }

    // ── Character properties ──────────────────────────────────────────────────

    @Nested
    @DisplayName("\\p{...} and \\P{...}")
    class CharacterProperties {

        private static final Map<String, String> ALPHABETS = RegexGenerator.propertyAlphabets();

        private static String range(int first, int last) {
            StringBuilder chars = new StringBuilder();
            for (int c = first; c <= last; c++) chars.append((char) c);
            return chars.toString();
        }

        private static Set<String> printableMatching(String regex) {
            Pattern pattern = Pattern.compile(regex);
            Set<String> matching = new HashSet<>();
            for (char c = 0x20; c < 0x7f; c++) {
                if (pattern.matcher(String.valueOf(c)).matches()) matching.add(String.valueOf(c));
            }
            return matching;
        }

        @Test
        @DisplayName("the supported names are the documented ones, spelled as documented")
        void supportedNames() {
            assertEquals(
                    List.of(
                            "Lower",
                            "Upper",
                            "ASCII",
                            "Alpha",
                            "Digit",
                            "Alnum",
                            "Punct",
                            "Graph",
                            "Print",
                            "Blank",
                            "Space",
                            "XDigit",
                            "L",
                            "Lu",
                            "Ll",
                            "N",
                            "Nd",
                            "IsAlphabetic",
                            "IsDigit",
                            "IsLatin"),
                    List.copyOf(ALPHABETS.keySet()));
        }

        @Test
        @DisplayName("each property draws from its documented alphabet")
        void documentedAlphabets() {
            String latin =
                    range('A', 'Z') + range('a', 'z') + range(0xC0, 0xD6) + range(0xD8, 0xF6) + range(0xF8, 0xFF);
            String letters =
                    latin + range(0x391, 0x3A1) + range(0x3A3, 0x3A9) + range(0x3B1, 0x3C9) + range(0x410, 0x44F);
            String decimalDigits = range('0', '9') + range(0x660, 0x669) + range(0x966, 0x96F);
            String printable = range(0x20, 0x7E);
            assertEquals(range('a', 'z'), ALPHABETS.get("Lower"));
            assertEquals(range('A', 'Z'), ALPHABETS.get("Upper"));
            assertEquals(printable, ALPHABETS.get("ASCII"));
            assertEquals(range('a', 'z') + range('A', 'Z'), ALPHABETS.get("Alpha"));
            assertEquals("0123456789", ALPHABETS.get("Digit"));
            assertEquals(range('a', 'z') + range('A', 'Z') + "0123456789", ALPHABETS.get("Alnum"));
            assertEquals("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", ALPHABETS.get("Punct"));
            assertEquals(range(0x21, 0x7E), ALPHABETS.get("Graph"));
            assertEquals(printable, ALPHABETS.get("Print"));
            assertEquals(" \t", ALPHABETS.get("Blank"));
            assertEquals(" \t\n\u000B\f\r", ALPHABETS.get("Space"));
            assertEquals("0123456789abcdefABCDEF", ALPHABETS.get("XDigit"));
            assertEquals(letters, ALPHABETS.get("L"));
            assertEquals(
                    range('A', 'Z')
                            + range(0xC0, 0xD6)
                            + range(0xD8, 0xDE)
                            + range(0x391, 0x3A1)
                            + range(0x3A3, 0x3A9)
                            + range(0x410, 0x42F),
                    ALPHABETS.get("Lu"));
            assertEquals(
                    range('a', 'z') + range(0xDF, 0xF6) + range(0xF8, 0xFF) + range(0x3B1, 0x3C9) + range(0x430, 0x44F),
                    ALPHABETS.get("Ll"));
            assertEquals(decimalDigits + range(0xB2, 0xB3) + range(0xB9, 0xB9) + range(0xBC, 0xBE), ALPHABETS.get("N"));
            assertEquals(decimalDigits, ALPHABETS.get("Nd"));
            assertEquals(letters, ALPHABETS.get("IsAlphabetic"));
            assertEquals(decimalDigits, ALPHABETS.get("IsDigit"));
            assertEquals(latin, ALPHABETS.get("IsLatin"));
        }

        @Test
        @DisplayName("java.util.regex matches every sampled character to its property, with and without (?i)")
        void everySampledCharacterMatches() {
            ALPHABETS.forEach((name, alphabet) -> {
                Pattern plain = Pattern.compile("\\p{" + name + "}");
                Pattern folded = Pattern.compile("(?i)\\p{" + name + "}");
                for (char c : alphabet.toCharArray()) {
                    String value = String.valueOf(c);
                    assertTrue(plain.matcher(value).matches(), () -> "\\p{" + name + "} does not match U+" + hex(c));
                    assertTrue(
                            folded.matcher(value).matches(), () -> "(?i)\\p{" + name + "} does not match U+" + hex(c));
                }
            });
        }

        @Test
        @DisplayName("each sample holds every printable ASCII character its property matches, so a negation is exact")
        void samplesHoldEveryPrintableAsciiMember() {
            ALPHABETS.forEach((name, alphabet) ->
                    assertEquals(printableMatching("\\p{" + name + "}"), printableIn(alphabet), name));
        }

        private static Set<String> printableIn(String alphabet) {
            Set<String> printable = new HashSet<>();
            for (char c : alphabet.toCharArray()) {
                if (c >= 0x20 && c < 0x7f) printable.add(String.valueOf(c));
            }
            return printable;
        }

        @Test
        @DisplayName("every form of every property draws exactly what java.util.regex matches among what it samples")
        void everyFormDrawsWhatItMatches() {
            ALPHABETS.forEach((name, alphabet) -> {
                for (String flags : List.of("", "(?i)")) {
                    Set<String> sample = new HashSet<>();
                    for (char c : alphabet.toCharArray()) {
                        sample.add(String.valueOf(c));
                        if (!flags.isEmpty() && Character.isLetter(c) && c < 0x80) {
                            sample.add(String.valueOf((char) (c ^ 0x20)));
                        }
                    }
                    for (String form : List.of("\\p{%s}", "[\\p{%s}]")) {
                        String regex = flags + form.formatted(name);
                        assertEquals(sample, distinct(regex, 10_000), regex);
                        assertEveryValueMatches(regex);
                    }
                    for (String form : List.of("\\P{%s}", "[\\P{%s}]", "[^\\p{%s}]", "[^\\P{%s}]")) {
                        String regex = flags + form.formatted(name);
                        Set<String> expected = printableMatching(regex);
                        if (expected.isEmpty()) {
                            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(regex), regex);
                        } else {
                            assertEquals(expected, distinct(regex, 10_000), regex);
                        }
                    }
                }
            });
        }

        @Test
        @DisplayName("the one-letter forms are the braced ones")
        void oneLetterForms() {
            assertEquals(distinct("\\p{L}"), distinct("\\pL"));
            assertEquals(distinct("\\p{N}"), distinct("\\pN"));
            assertEquals(distinct("\\P{L}"), distinct("\\PL"));
            assertEquals(distinct("\\P{N}"), distinct("\\PN"));
            // As in java.util.regex, \pLu is \pL followed by u.
            assertTrue(distinct("\\pLu").stream().allMatch(value -> value.length() == 2 && value.endsWith("u")));
        }

        private static String hex(char c) {
            return Integer.toHexString(c).toUpperCase(java.util.Locale.ROOT);
        }
    }

    // ── Quoting ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("\\Q...\\E")
    class Quoting {

        @Test
        @DisplayName("quoted text is literal, to \\E or to the end of the pattern")
        void quotedTextIsLiteral() {
            assertEquals(Set.of("a.b"), distinct("\\Qa.b\\E"));
            assertEquals(Set.of("a.b"), distinct("\\Qa.b"));
            assertEquals(Set.of("(a|b)*"), distinct("\\Q(a|b)*\\E"));
            assertEquals(Set.of("[^a]{2}"), distinct("\\Q[^a]{2}\\E"));
            assertEquals(Set.of("12"), distinct("\\Q12\\E"));
            assertEquals(Set.of("$^"), distinct("\\Q$^\\E"));
            assertEquals(Set.of(""), distinct("\\Q\\E"));
            assertEquals(Set.of("(?i)a"), distinct("\\Q(?i)\\Ea"));
        }

        @Test
        @DisplayName("a quantifier after \\E repeats the last quoted character, or what is before an empty quote")
        void quantifierAfterAQuote() {
            assertEquals(Set.of("ab", "abb"), distinct("\\Qab\\E{1,2}"));
            assertEquals(Set.of("x", "xx"), distinct("x\\Q\\E{1,2}"));
            // java.util.regex reads this as \x312{1,2}12{0,1}.
            assertEquals(Set.of("121", "1212", "1221", "12212"), distinct("\\Q12\\E{1,2}12\\Q\\E{0,1}"));
        }

        @Test
        @DisplayName("backslashes inside a quote are literal, except the one that ends it")
        void backslashesInAQuote() {
            assertEquals(Set.of("\\"), distinct("\\Q\\\\E"));
            assertEquals(Set.of("a\\"), distinct("\\Qa\\"));
            assertEquals(Set.of("\\Q"), distinct("\\Q\\Q\\E"));
            assertEquals(Set.of("\\Q"), distinct("\\\\Q"));
            assertEquals(Set.of("\\a."), distinct("\\\\\\Qa.\\E"));
        }

        @Test
        @DisplayName("in a class, quoted characters are members, and the one after a quote can end a range")
        void quotingInAClass() {
            assertEquals(Set.of("a", "-", "c"), distinct("[\\Qa-c\\E]"));
            assertEquals(Set.of("a", "b", "c"), distinct("[\\Qa\\E-c]"));
            assertEquals(Set.of("a", "-", "c"), distinct("[a\\Q-\\Ec]"));
            assertEquals(Set.of("^", "]"), distinct("[\\Q^]\\E]"));
        }
    }

    // ── Size limits ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Size limits")
    class SizeLimits {

        private static final String NESTED = "((a{10000}){10000}){10000}";

        @Test
        @DisplayName("nested repetition is rejected at construction, quickly and without running out of memory")
        void nestedRepetitionIsRejectedQuickly() {
            IllegalArgumentException exception = assertTimeoutPreemptively(
                    Duration.ofSeconds(10),
                    () -> assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(NESTED)));
            assertEquals(
                    "Generated values could be longer than the limit of 1000000 characters, with *, + and {n,}"
                            + " repeating up to 10 times, in pattern: " + NESTED,
                    exception.getMessage());
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(NESTED, seeded(1L)));
        }

        @Test
        @DisplayName("repetition that generates nothing is rejected for the steps it would take")
        void emptyRepetitionIsRejectedForItsSteps() {
            String empty = "(((){10000}){10000}){10000}";
            IllegalArgumentException exception = assertTimeoutPreemptively(
                    Duration.ofSeconds(10),
                    () -> assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(empty)));
            assertEquals(
                    "Generating a value could take more than the limit of 10000000 steps, with *, + and {n,}"
                            + " repeating up to 10 times, in pattern: " + empty,
                    exception.getMessage());
            assertTrue(assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("((^){10000}){10000}"))
                    .getMessage()
                    .contains("10000000 steps"));
        }

        @Test
        @DisplayName("a value of exactly the limit is generated, and one character more is rejected")
        void lengthLimitIsExact() {
            String value = new RegexGenerator("(a{1000}){1000}").generate();
            assertEquals(RegexGenerator.MAX_LENGTH, value.length());
            assertTrue(Pattern.compile("(a{1000}){1000}").matcher(value).matches());
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(a{1000}){1000}a"));
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(a{1000}){1001}"));
            // Length is counted in UTF-16 chars, two for a supplementary character.
            assertEquals(
                    RegexGenerator.MAX_LENGTH,
                    new RegexGenerator("(\\x{1D11E}{1000}){500}").generate().length());
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(\\x{1D11E}{1000}){501}"));
            // The longest alternative counts.
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(a|(b{1000}){1001})"));
        }

        @Test
        @DisplayName("steps are one for each node each time it is generated, up to the limit")
        void stepLimitIsExact() {
            // 2 + 1002 * 9980 = 9 999 962 steps: the pattern's sequence, the outer quantifier, and 9980 times
            // the group's sequence, the inner quantifier and its 1000 empty sequences.
            assertEquals("", new RegexGenerator("((?:){1000}){9980}").generate());
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("((?:){1000}){9981}"));
            assertEquals("", new RegexGenerator("(?:(?:a|){0}){10000}").generate());
        }

        @Test
        @DisplayName("open-ended repetition counts at the limit the configuration gives it")
        void configuredRepetitionCounts() {
            // (a+){1000} is 1000 times as long as a+ repeats, so it fits while a+ repeats up to 1000 times.
            int length = new RegexGenerator("(a+){1000}", seeded(1L)).generate().length();
            assertTrue(length >= 1_000 && length <= 10_000, "length " + length);
            int raised = new RegexGenerator("(a+){1000}", minimumLength(500))
                    .generate()
                    .length();
            assertTrue(raised > 10_000 && raised <= RegexGenerator.MAX_LENGTH, "length " + raised);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new RegexGenerator("(a+){1000}", minimumLength(501)));
            assertEquals(
                    "Generated values could be longer than the limit of 1000000 characters, with *, + and {n,}"
                            + " repeating up to 1002 times, in pattern: (a+){1000}",
                    exception.getMessage());
            // A pattern with no open-ended repetition, or one that fits at the largest, fits under any configuration.
            GeneratorConfig longest = minimumLength(Integer.MAX_VALUE - 1);
            assertEquals(
                    10_000, new RegexGenerator("a{10000}", longest).generate().length());
            assertEquals(
                    10_000, new RegexGenerator("a{10000,}", longest).generate().length());
            assertEquals(
                    10_000,
                    new RegexGenerator("(a{100}){100}", longest).generate().length());
        }

        @Test
        @DisplayName("a pattern too large at every configuration is not cached, one too large only for some is")
        void caching() {
            RegexGenerator.clearParsedPatternCacheForTests();
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(NESTED));
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator(NESTED));
            assertEquals(0, RegexGenerator.parsedPatternCacheSize());

            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(a+){1000}", minimumLength(501)));
            assertEquals(1, RegexGenerator.parsedPatternCacheSize());
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("(a+){1000}", minimumLength(501)));
            assertTrue(new RegexGenerator("(a+){1000}", minimumLength(500))
                    .generate()
                    .startsWith("a"));
            assertEquals(1, RegexGenerator.parsedPatternCacheSize());
        }

        private static GeneratorConfig minimumLength(int length) {
            return GeneratorConfig.builder()
                    .seed(1L)
                    .stringLength(length, length)
                    .build();
        }
    }

    private static Set<String> distinct(String regex) {
        return distinct(regex, 2_000);
    }

    /** Draws values from one seed, checking each against java.util.regex, and returns the distinct ones. */
    private static Set<String> distinct(String regex, int draws) {
        Pattern compiled = Pattern.compile(regex);
        Set<String> values = new HashSet<>();
        RegexGenerator generator = new RegexGenerator(regex, seeded(1L));
        for (int i = 0; i < draws; i++) {
            String value = generator.generate();
            assertTrue(
                    compiled.matcher(value).matches(), () -> "\"" + value + "\" does not match the pattern " + regex);
            values.add(value);
        }
        return values;
    }

    private static void assertEveryValueMatches(String regex) {
        Pattern compiled = Pattern.compile(regex);
        for (long seed = 0; seed < 5; seed++) {
            RegexGenerator generator = new RegexGenerator(regex, seeded(seed));
            for (int draw = 0; draw < 100; draw++) {
                String value = generator.generate();
                assertTrue(compiled.matcher(value).matches(), () -> "\"" + value + "\" does not match " + regex);
            }
        }
    }

    // ── Repetition cap (DoS guard) ────────────────────────────────────────────

    @Nested
    @DisplayName("Repetition cap")
    class RepetitionCapTest {

        @Test
        @DisplayName("{n} at the cap is allowed")
        void atCapAllowed() {
            String s = new RegexGenerator("a{" + RegexGenerator.MAX_REPETITIONS + "}").generate();
            assertEquals(RegexGenerator.MAX_REPETITIONS, s.length());
        }

        @Test
        @DisplayName("{n} above the cap is rejected at construction")
        void exactCountAboveCapThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new RegexGenerator("a{" + (RegexGenerator.MAX_REPETITIONS + 1) + "}"));
        }

        @Test
        @DisplayName("{n,m} max above the cap is rejected at construction")
        void rangeMaxAboveCapThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new RegexGenerator("a{1," + (RegexGenerator.MAX_REPETITIONS + 1) + "}"));
        }

        @Test
        @DisplayName("absurdly long digit run is rejected without overflowing")
        void hugeCountThrows() {
            assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("a{1234567890123456789}"));
        }
    }

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).build();
    }
}
