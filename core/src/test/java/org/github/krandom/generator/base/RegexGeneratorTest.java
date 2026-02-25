/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegexGenerator")
class RegexGeneratorTest {

    private static final int SAMPLES = 50;

    // ── Digit class ───────────────────────────────────────────────────────────

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
                    assertTrue(Character.isDigit(c),
                            "expected digit but got '" + c + "' in: " + s);
                }
            }
        }
    }

    // ── Uppercase character class ─────────────────────────────────────────────

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
                    assertTrue(c >= 'A' && c <= 'Z',
                            "expected uppercase A-Z but got '" + c + "' in: " + s);
                }
            }
        }
    }

    // ── Alternation ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("(yes|no) — alternation")
    class Alternation {

        @Test
        @DisplayName("generates only 'yes' or 'no'")
        void onlyYesOrNo() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("(yes|no)").generate();
                assertTrue(s.equals("yes") || s.equals("no"),
                        "expected 'yes' or 'no', got: " + s);
            }
        }

        @Test
        @DisplayName("both alternatives appear over many calls")
        void bothAlternativesAppear() {
            Set<String> seen = new HashSet<>();
            RegexGenerator gen = new RegexGenerator("(yes|no)");
            for (int i = 0; i < 100; i++) seen.add(gen.generate());
            assertEquals(Set.of("yes", "no"), seen,
                    "both 'yes' and 'no' must appear");
        }
    }

    // ── Word chars with + ─────────────────────────────────────────────────────

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
                    assertTrue(Character.isLetterOrDigit(c) || c == '_',
                            "non-word character '" + c + "' in: " + s);
                }
            }
        }
    }

    // ── Optional quantifier ───────────────────────────────────────────────────

    @Test
    @DisplayName("\\d? produces either 0 or 1 digits")
    void optionalQuantifier() {
        RegexGenerator gen = new RegexGenerator("\\d?");
        for (int i = 0; i < SAMPLES; i++) {
            String s = gen.generate();
            assertTrue(s.isEmpty() || (s.length() == 1 && Character.isDigit(s.charAt(0))),
                    "\\d? must produce 0 or 1 digit, got: " + s);
        }
    }

    // ── Literal concatenation ─────────────────────────────────────────────────

    @Test
    @DisplayName("literal pattern produces the literal string")
    void literalPattern() {
        for (int i = 0; i < SAMPLES; i++) {
            assertEquals("hello", new RegexGenerator("hello").generate());
        }
    }

    // ── Mixed pattern ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("\\d{3}-\\d{4} produces NNN-NNNN format")
    void phoneLikePattern() {
        for (int i = 0; i < SAMPLES; i++) {
            String s = new RegexGenerator("\\d{3}-\\d{4}").generate();
            assertEquals(8, s.length(), "expected 8 chars, got: " + s);
            assertEquals('-', s.charAt(3), "expected '-' at index 3, got: " + s);
        }
    }

    // ── Anchors ignored ───────────────────────────────────────────────────────

    @Test
    @DisplayName("anchors ^ and $ are silently ignored")
    void anchorsIgnored() {
        for (int i = 0; i < SAMPLES; i++) {
            String s = new RegexGenerator("^\\d{2}$").generate();
            assertEquals(2, s.length(), "expected 2 digits, got: " + s);
            for (char c : s.toCharArray()) assertTrue(Character.isDigit(c));
        }
    }

    // ── Seeded reproducibility ────────────────────────────────────────────────

    @Test
    @DisplayName("same seed produces same output on every call")
    void seededReproducibility() {
        RegexGenerator g1 = new RegexGenerator("\\d{4}[A-Z]{3}", 99L);
        RegexGenerator g2 = new RegexGenerator("\\d{4}[A-Z]{3}", 99L);
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generate(), g2.generate(), "seeded generators must produce identical output");
        }
    }

    // ── Range quantifier {n,m} ────────────────────────────────────────────────

    @Test
    @DisplayName("{2,5} quantifier produces strings with length in [2,5]")
    void rangeQuantifier() {
        RegexGenerator gen = new RegexGenerator("[a-z]{2,5}");
        for (int i = 0; i < SAMPLES; i++) {
            int len = gen.generate().length();
            assertTrue(len >= 2 && len <= 5, "expected length in [2,5], got: " + len);
        }
    }

    // ── Escape sequences ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Escape sequences")
    class EscapeSequences {

        @Test
        @DisplayName("\\D produces a non-digit character")
        void nonDigitEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("\\D").generate();
                assertEquals(1, s.length(), "\\D must produce exactly 1 char");
                assertFalse(Character.isDigit(s.charAt(0)),
                        "\\D must not produce a digit, got: " + s);
            }
        }

        @Test
        @DisplayName("\\W produces a non-word character")
        void nonWordEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\W").generate().charAt(0);
                assertFalse(Character.isLetterOrDigit(c) || c == '_',
                        "\\W must not produce a word char, got: " + c);
            }
        }

        @Test
        @DisplayName("\\s produces a whitespace character")
        void whitespaceEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\s").generate().charAt(0);
                assertTrue(c == ' ' || c == '\t' || c == '\n' || c == '\r',
                        "\\s must produce whitespace, got: '" + c + "'");
            }
        }

        @Test
        @DisplayName("\\S produces a non-whitespace character")
        void nonWhitespaceEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                char c = new RegexGenerator("\\S").generate().charAt(0);
                assertFalse(c == ' ' || c == '\t' || c == '\n' || c == '\r',
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

        @Test
        @DisplayName("unknown escape (\\q) is treated as the literal character q")
        void unknownEscape() {
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals("q", new RegexGenerator("\\q").generate(),
                        "unknown escape must produce the literal character");
            }
        }
    }

    // ── Character class variants ──────────────────────────────────────────────

    @Nested
    @DisplayName("Character class variants")
    class CharClassVariants {

        @Test
        @DisplayName("[abc] (literal char class) produces only a, b, or c")
        void literalCharClass() {
            for (int i = 0; i < SAMPLES; i++) {
                String s = new RegexGenerator("[abc]").generate();
                assertEquals(1, s.length());
                assertTrue(s.equals("a") || s.equals("b") || s.equals("c"),
                        "expected a/b/c, got: " + s);
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
                assertFalse("aeiou".indexOf(c) >= 0,
                        "negated class must exclude vowels, got: " + c);
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

    // ── Additional quantifiers ────────────────────────────────────────────────

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
        @DisplayName("{n} with no closing brace is treated as exact repetition")
        void exactQuantifierUnclosed() {
            // \d{3 (no '}') — treated the same as \d{3}
            RegexGenerator gen = new RegexGenerator("\\d{3");
            for (int i = 0; i < SAMPLES; i++) {
                String s = gen.generate();
                assertEquals(3, s.length(), "unclosed {3 must produce exactly 3 chars, got: " + s);
                for (char c : s.toCharArray()) assertTrue(Character.isDigit(c));
            }
        }

        @Test
        @DisplayName("{n,m} with no closing brace is treated as range repetition")
        void rangeQuantifierUnclosed() {
            // \d{3,5 (no '}') — treated the same as \d{3,5}
            RegexGenerator gen = new RegexGenerator("\\d{3,5");
            for (int i = 0; i < SAMPLES; i++) {
                int len = gen.generate().length();
                assertTrue(len >= 3 && len <= 5,
                        "unclosed {3,5 must give 3-5 chars, got: " + len);
            }
        }
    }

    // ── Unclosed character class ──────────────────────────────────────────────

    @Test
    @DisplayName("[abc (no closing ']') is treated as [abc] — picks a, b, or c")
    void unclosedCharClassTreatedAsIfClosed() {
        // The closing ']' is optional; unclosed class behaves like the closed version.
        RegexGenerator gen = new RegexGenerator("[abc");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String s = gen.generate();
            assertEquals(1, s.length(), "must produce exactly 1 char, got: " + s);
            assertTrue(s.equals("a") || s.equals("b") || s.equals("c"),
                    "must produce a, b, or c; got: " + s);
            seen.add(s);
        }
        assertEquals(Set.of("a", "b", "c"), seen, "all three chars must appear");
    }

    @Test
    @DisplayName("[ with nothing after it throws IllegalArgumentException (empty class)")
    void bracketAtEndOfPatternThrows() {
        // pos >= src.length() after consuming '[' — short-circuits the '^' check,
        // then pool stays empty → IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("["));
    }

    @Test
    @DisplayName("[a-] — dash immediately before ']' is a literal hyphen, not a range")
    void dashBeforeClosingBracketIsLiteral() {
        // src.charAt(pos + 1) == ']' makes the range condition false → '-' is a literal
        RegexGenerator gen = new RegexGenerator("[a-]");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(gen.generate());
        assertEquals(Set.of("a", "-"), seen, "[a-] must produce only 'a' or '-'");
    }

    // ── Single-alternative group ──────────────────────────────────────────────

    @Test
    @DisplayName("(hello) — single-alternative group returns SequenceNode, not AlternationNode")
    void singleAlternativeGroup() {
        // alts.size() == 1 → returns alts.get(0) directly (no AlternationNode wrapper)
        RegexGenerator gen = new RegexGenerator("(hello)");
        for (int i = 0; i < SAMPLES; i++) {
            assertEquals("hello", gen.generate(), "single-alternative group must produce 'hello'");
        }
    }

    // ── Quantifier edge cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("{n, at end of pattern throws IllegalArgumentException (no max number)")
    void openEndedQuantifierAtEndOfPatternThrows() {
        // After consuming ',', pos >= src.length() → ternary short-circuits to parseNumber() → throws
        assertThrows(IllegalArgumentException.class, () -> new RegexGenerator("\\d{3,"));
    }

    @Test
    @DisplayName("{n,m} with literal after max (no '}') — '}' consumption is skipped")
    void rangeQuantifierWithLiteralAfterMax() {
        // After parsing max '2', pos is at 'x' (not '}') → no '}' consumed, literal parsed next
        RegexGenerator gen = new RegexGenerator("\\d{1,2x");
        for (int i = 0; i < SAMPLES; i++) {
            String s = gen.generate();
            assertTrue(s.endsWith("x"), "must end with 'x', got: " + s);
            assertTrue(s.length() == 2 || s.length() == 3,
                    "1–2 digits + 'x' must give length 2 or 3, got: " + s.length());
        }
    }

    @Test
    @DisplayName("{n} with literal after count (no '}') — '}' consumption is skipped")
    void exactQuantifierWithLiteralAfterCount() {
        // After parsing count '3', pos is at 'x' (not '}') → no '}' consumed, literal parsed next
        RegexGenerator gen = new RegexGenerator("\\d{3x");
        for (int i = 0; i < SAMPLES; i++) {
            String s = gen.generate();
            assertEquals(4, s.length(), "3 digits + 'x' must give length 4, got: " + s);
            assertEquals('x', s.charAt(3), "last char must be 'x', got: " + s);
        }
    }

    // ── Invalid patterns → IllegalArgumentException ───────────────────────────

    @Test
    @DisplayName("unclosed group throws IllegalArgumentException")
    void unclosedGroupThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexGenerator("(abc|def"));
    }

    @Test
    @DisplayName("trailing backslash throws IllegalArgumentException")
    void trailingBackslashThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexGenerator("\\d{3}\\"));
    }

    @Test
    @DisplayName("unexpected closing paren at top level throws IllegalArgumentException")
    void unexpectedClosingParenThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexGenerator("abc)def"));
    }

    @Test
    @DisplayName("{ with no digits (bad quantifier) throws IllegalArgumentException")
    void badQuantifierNoDigitsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegexGenerator("\\d{abc}"));
    }

    @Test
    @DisplayName("null pattern throws NullPointerException")
    void nullPatternThrows() {
        assertThrows(NullPointerException.class, () -> new RegexGenerator(null));
    }
}
