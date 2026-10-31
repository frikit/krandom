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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.HashSet;
import java.util.List;
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

        private static Set<String> distinct(String regex) {
            Set<String> values = new HashSet<>();
            RegexGenerator generator = new RegexGenerator(regex, seeded(1L));
            for (int i = 0; i < 2_000; i++) {
                values.add(generator.generate());
            }
            return values;
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
                    // Flags
                    Arguments.of("(?i)abc", "Unsupported inline flag"),
                    Arguments.of("(?i:abc)", "Unsupported inline flag"),
                    Arguments.of("(?-i)abc", "Unsupported inline flag"),
                    Arguments.of("(?s).", "Unsupported inline flag"),
                    Arguments.of("(?m)^a$", "Unsupported inline flag"),
                    Arguments.of("(?x)a b", "Unsupported inline flag"),
                    Arguments.of("(?u)a", "Unsupported inline flag"),
                    Arguments.of("(?U)\\w", "Unsupported inline flag"),
                    Arguments.of("(?d).", "Unsupported inline flag"),
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
                    Arguments.of("\\Qa.b\\E", "Unsupported escape \\Q"),
                    Arguments.of("\\Qa.b", "Unsupported escape \\Q"),
                    Arguments.of("\\p{Lu}", "Unsupported character property \\p"),
                    Arguments.of("\\P{Lu}", "Unsupported character property \\P"),
                    Arguments.of("\\p{Alpha}", "Unsupported character property \\p"),
                    // Classes
                    Arguments.of("[\\p{Lu}]", "Unsupported character property \\p"),
                    Arguments.of("[\\h]", "Unsupported escape \\h"),
                    Arguments.of("[\\Qa\\E]", "Unsupported escape \\Q"),
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
