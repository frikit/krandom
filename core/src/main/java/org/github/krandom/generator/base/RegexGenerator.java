/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates random strings that conform to a simplified regular-expression pattern.
 *
 * <p><b>Supported syntax</b>
 * <ul>
 *   <li>Literals: any character not listed below</li>
 *   <li>Escape sequences: {@code \d} (digit), {@code \w} (word), {@code \s} (whitespace),
 *       and their uppercase negations ({@code \D}, {@code \W}, {@code \S}); also
 *       {@code \.}, {@code \-}, {@code \\}, {@code \n}, {@code \r}, {@code \t}</li>
 *   <li>Character classes: {@code [abc]}, {@code [a-z]}, {@code [^abc]} (negated),
 *       mixed ranges and literals</li>
 *   <li>Quantifiers: {@code ?} (0–1), {@code +} (1–10), {@code *} (0–10),
 *       {@code {n}}, {@code {n,m}}</li>
 *   <li>Groups and alternation: {@code (a|b|c)}</li>
 *   <li>Anchors: {@code ^} and {@code $} are silently ignored</li>
 * </ul>
 *
 * <p><b>Usage</b>
 * <pre>{@code
 * // 3-digit number
 * String code = new RegexGenerator("\\d{3}").generate();      // e.g. "472"
 *
 * // 5 uppercase letters
 * String upper = new RegexGenerator("[A-Z]{5}").generate();   // e.g. "KQTBM"
 *
 * // yes or no
 * String yn = new RegexGenerator("(yes|no)").generate();      // "yes" or "no"
 *
 * // Seeded for reproducibility
 * String fixed = new RegexGenerator("\\d{4}", 42L).generate();
 * }</pre>
 *
 * <p><b>Integration with ObjectGenerator</b>
 * <pre>{@code
 * ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *     .override(String.class, new RegexGenerator("\\d{3}-\\d{4}"))
 *     .build();
 * }</pre>
 *
 * <p>Unsupported or malformed patterns throw {@link IllegalArgumentException} at construction time.
 *
 * <p><b>Thread Safety:</b>
 * This generator is <em>not</em> thread-safe; create one instance per thread, or guard with external synchronisation.
 */
public final class RegexGenerator implements Generator<String> {

    // ── Pre-built character pools ─────────────────────────────────────────────

    private static final char[] DIGITS;
    private static final char[] NON_DIGITS;
    private static final char[] WORD_CHARS;
    private static final char[] NON_WORD_CHARS;
    private static final char[] WHITESPACE = { ' ', '\t', '\n', '\r' };
    private static final char[] NON_WHITESPACE;
    private static final Map<String, SequenceNode> PARSED_PATTERNS = new ConcurrentHashMap<>();

    static {
        DIGITS = "0123456789".toCharArray();

        StringBuilder wordBuf = new StringBuilder();
        for (char c = 'a'; c <= 'z'; c++) wordBuf.append(c);
        for (char c = 'A'; c <= 'Z'; c++) wordBuf.append(c);
        for (char c = '0'; c <= '9'; c++) wordBuf.append(c);
        wordBuf.append('_');
        WORD_CHARS = wordBuf.toString().toCharArray();

        Set<Character> wordSet = new HashSet<>();
        for (char c : WORD_CHARS) wordSet.add(c);

        Set<Character> digitSet = new HashSet<>();
        for (char c : DIGITS) digitSet.add(c);

        Set<Character> wsSet = new HashSet<>();
        for (char c : WHITESPACE) wsSet.add(c);

        List<Character> nonDigits = new ArrayList<>();
        List<Character> nonWord = new ArrayList<>();
        List<Character> nonWs = new ArrayList<>();
        for (char c = 0x20; c < 0x7f; c++) {
            if (!digitSet.contains(c)) nonDigits.add(c);
            if (!wordSet.contains(c)) nonWord.add(c);
            if (!wsSet.contains(c)) nonWs.add(c);
        }
        NON_DIGITS = toArray(nonDigits);
        NON_WORD_CHARS = toArray(nonWord);
        NON_WHITESPACE = toArray(nonWs);
    }

    /**
     * The compiled root node (always a {@link SequenceNode}).
     */
    private final SequenceNode root;

    // ── Node sealed hierarchy ─────────────────────────────────────────────────
    private final Random       random;


    /**
     * Creates a regex generator using {@link SecureRandom}.
     *
     * @param pattern the simplified regex pattern; must not be {@code null}
     * @throws IllegalArgumentException if the pattern contains unsupported or malformed syntax
     */
    public RegexGenerator(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        this.random = new SecureRandom();
        this.root = parsePattern(pattern);
    }


    /**
     * Creates a seeded regex generator for reproducible output.
     *
     * @param pattern the simplified regex pattern; must not be {@code null}
     * @param seed    PRNG seed for deterministic output
     * @throws IllegalArgumentException if the pattern contains unsupported or malformed syntax
     */
    public RegexGenerator(String pattern, long seed) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        this.random = new Random(seed);
        this.root = parsePattern(pattern);
    }

    static int parsedPatternCacheSize() {
        return PARSED_PATTERNS.size();
    }

    static void clearParsedPatternCacheForTests() {
        PARSED_PATTERNS.clear();
    }

    private static SequenceNode parsePattern(String pattern) {
        return PARSED_PATTERNS.computeIfAbsent(pattern, RegexGenerator::parsePatternUncached);
    }

    private static SequenceNode parsePatternUncached(String pattern) {
        return new Parser(pattern).parse();
    }

    private static char[] toArray(List<Character> list) {
        char[] arr = new char[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    /**
     * {@inheritDoc}
     *
     * @return a random string matching the pattern; never {@code null}
     */
    @Override
    public String generate() {
        return generateNode(root);
    }

    private String generateNode(Node node) {
        return switch (node) {
            case LiteralNode l -> String.valueOf(l.c());
            case CharClassNode cc -> String.valueOf(cc.pool()[random.nextInt(cc.pool().length)]);
            case SequenceNode s -> {
                StringBuilder sb = new StringBuilder();
                for (Node n : s.nodes()) sb.append(generateNode(n));
                yield sb.toString();
            }
            case AlternationNode alt -> {
                SequenceNode chosen = alt.alts().get(random.nextInt(alt.alts().size()));
                yield generateNode(chosen);
            }
            case QuantifierNode q -> {
                int count = q.min() == q.max()
                            ? q.min()
                            : q.min() + random.nextInt(q.max() - q.min() + 1);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) sb.append(generateNode(q.child()));
                yield sb.toString();
            }
        };
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private sealed interface Node
        permits LiteralNode, CharClassNode, SequenceNode, AlternationNode, QuantifierNode {

    }


    private record LiteralNode(char c) implements Node {

    }

    // ── Constructors ──────────────────────────────────────────────────────────


    private record CharClassNode(char[] pool) implements Node {

    }


    private record SequenceNode(List<Node> nodes) implements Node {

    }

    // ── Generator<String> ─────────────────────────────────────────────────────


    /**
     * Picks one of several alternative sequences at random.
     */
    private record AlternationNode(List<SequenceNode> alts) implements Node {

    }

    // ── Generation helpers ────────────────────────────────────────────────────


    private record QuantifierNode(Node child, int min, int max) implements Node {

    }

    // ── Parser ────────────────────────────────────────────────────────────────


    /**
     * Recursive-descent parser that converts a pattern string into a node tree.
     * Mutable only during the parse phase (construction time).
     */
    private static final class Parser {

        private final String src;
        private       int    pos = 0;

        Parser(String src) {
            this.src = src;
        }

        /**
         * Entry point — returns the root {@link SequenceNode} for the whole pattern.
         */
        SequenceNode parse() {
            SequenceNode result = new SequenceNode(List.copyOf(parseSequence()));
            if (pos < src.length()) {
                throw new IllegalArgumentException(
                    "Unexpected character '" + src.charAt(pos) + "' at position " + pos
                    + " in pattern: " + src);
            }
            return result;
        }

        // ── Sequence (concatenation) ──────────────────────────────────────────

        /**
         * Parses zero or more atoms, stopping at end-of-string, {@code |}, or {@code )}.
         */
        private List<Node> parseSequence() {
            List<Node> nodes = new ArrayList<>();
            while (pos < src.length()
                   && src.charAt(pos) != '|'
                   && src.charAt(pos) != ')') {
                Node atom = parseAtom();
                if (atom != null) {
                    nodes.add(parseQuantifier(atom));
                }
            }
            return nodes;
        }

        // ── Atom ──────────────────────────────────────────────────────────────

        private Node parseAtom() {
            char c = src.charAt(pos);

            if (c == '(') {
                pos++;
                List<SequenceNode> alts = new ArrayList<>();
                alts.add(new SequenceNode(List.copyOf(parseSequence())));
                while (pos < src.length() && src.charAt(pos) == '|') {
                    pos++;
                    alts.add(new SequenceNode(List.copyOf(parseSequence())));
                }
                if (pos >= src.length()) {
                    throw new IllegalArgumentException("Unclosed group '(' in pattern: " + src);
                }
                pos++; // consume ')'
                if (alts.size() == 1) return alts.get(0);
                return new AlternationNode(List.copyOf(alts));
            }

            if (c == '[') {
                return parseCharClass();
            }

            if (c == '\\') {
                return parseEscape();
            }

            // Anchors — silently ignored; no node emitted
            if (c == '^' || c == '$') {
                pos++;
                return null;
            }

            pos++;
            return new LiteralNode(c);
        }

        // ── Character class [abc] / [a-z] / [^abc] ───────────────────────────

        private Node parseCharClass() {
            pos++; // consume '['
            boolean negated = pos < src.length() && src.charAt(pos) == '^';
            if (negated) pos++;

            List<Character> pool = new ArrayList<>();
            while (pos < src.length() && src.charAt(pos) != ']') {
                char c1 = src.charAt(pos++);
                if (pos + 1 < src.length()
                    && src.charAt(pos) == '-'
                    && src.charAt(pos + 1) != ']') {
                    pos++; // consume '-'
                    char c2 = src.charAt(pos++);
                    for (char c = c1; c <= c2; c++) pool.add(c);
                } else {
                    pool.add(c1);
                }
            }
            if (pos < src.length()) pos++; // consume ']'

            if (negated) {
                Set<Character> excludeSet = new HashSet<>(pool);
                pool.clear();
                for (char c = 0x20; c < 0x7f; c++) {
                    if (!excludeSet.contains(c)) pool.add(c);
                }
            }

            if (pool.isEmpty()) {
                throw new IllegalArgumentException("Empty character class in pattern: " + src);
            }
            return new CharClassNode(toArray(pool));
        }

        // ── Escape sequences \d \w etc. ───────────────────────────────────────

        private Node parseEscape() {
            pos++; // consume '\'
            if (pos >= src.length()) {
                throw new IllegalArgumentException("Trailing backslash in pattern: " + src);
            }
            char c = src.charAt(pos++);
            return switch (c) {
                case 'd' -> new CharClassNode(DIGITS);
                case 'D' -> new CharClassNode(NON_DIGITS);
                case 'w' -> new CharClassNode(WORD_CHARS);
                case 'W' -> new CharClassNode(NON_WORD_CHARS);
                case 's' -> new CharClassNode(WHITESPACE);
                case 'S' -> new CharClassNode(NON_WHITESPACE);
                case '.' -> new LiteralNode('.');
                case '-' -> new LiteralNode('-');
                case '\\' -> new LiteralNode('\\');
                case 'n' -> new LiteralNode('\n');
                case 'r' -> new LiteralNode('\r');
                case 't' -> new LiteralNode('\t');
                default -> new LiteralNode(c); // unknown escape: treat as literal
            };
        }

        // ── Quantifiers ?, +, *, {n}, {n,m} ──────────────────────────────────

        private Node parseQuantifier(Node node) {
            if (pos >= src.length()) return node;
            char c = src.charAt(pos);
            return switch (c) {
                case '?' -> {
                    pos++;
                    yield new QuantifierNode(node, 0, 1);
                }
                case '+' -> {
                    pos++;
                    yield new QuantifierNode(node, 1, 10);
                }
                case '*' -> {
                    pos++;
                    yield new QuantifierNode(node, 0, 10);
                }
                case '{' -> {
                    pos++; // consume '{'
                    int min = parseNumber();
                    if (pos < src.length() && src.charAt(pos) == ',') {
                        pos++; // consume ','
                        int max = pos < src.length() && src.charAt(pos) == '}'
                                  ? 10  // {n,} — cap unbounded at 10
                                  : parseNumber();
                        if (pos < src.length() && src.charAt(pos) == '}') pos++;
                        yield new QuantifierNode(node, min, max);
                    } else {
                        if (pos < src.length() && src.charAt(pos) == '}') pos++;
                        yield new QuantifierNode(node, min, min);
                    }
                }
                default -> node;
            };
        }

        private int parseNumber() {
            int start = pos;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            if (pos == start) {
                throw new IllegalArgumentException(
                    "Expected number at position " + pos + " in pattern: " + src);
            }
            return Integer.parseInt(src.substring(start, pos));
        }
    }
}
