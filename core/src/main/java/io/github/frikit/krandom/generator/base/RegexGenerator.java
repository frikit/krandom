/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Generates random strings that match a regular expression in {@link java.util.regex.Pattern} syntax.
 *
 * <p><b>Contract:</b> for any pattern, either every generated value satisfies
 * {@code Pattern.compile(pattern).matcher(value).matches()}, or the constructor throws
 * {@link IllegalArgumentException}. A malformed pattern fails with {@code java.util.regex}'s own
 * {@link java.util.regex.PatternSyntaxException}, and a valid pattern this class cannot honour fails
 * with a message naming the construct and its position. A value never silently fails to match.
 *
 * <p><b>Matching:</b> a value matches the whole pattern, so {@code ^} and {@code $} add nothing to
 * it: they are zero-width assertions that hold at the start and the end of the value. A value that
 * matches as a whole is also found by {@code find()}, so it satisfies an unanchored search such as
 * JSON Schema's {@code pattern}. No flags apply, as {@code Pattern.compile(pattern)} takes none.
 *
 * <p><b>Supported syntax</b>
 * <ul>
 *   <li>Literals: any character that is not special, including supplementary characters</li>
 *   <li>Escaped characters: a backslash before anything but an ASCII letter or digit, such as
 *       {@code \.}, plus {@code \t}, {@code \n}, {@code \r}, {@code \f}, {@code \a}, {@code \e},
 *       {@code \xhh}, {@code \x{h...h}} and <code>&#92;uhhhh</code></li>
 *   <li>Any character: {@code .} draws from printable ASCII</li>
 *   <li>Predefined classes: {@code \d}, {@code \w}, {@code \s}, and their negations {@code \D},
 *       {@code \W} and {@code \S}, which draw from printable ASCII</li>
 *   <li>Character classes: {@code [abc]}, {@code [a-z]} and {@code [^abc]}, with the escapes above
 *       as members or range ends. A negated class draws from printable ASCII</li>
 *   <li>Quantifiers: {@code ?}, {@code *}, {@code +}, {@code {n}}, {@code {n,}} and {@code {n,m}},
 *       and their lazy forms, which match the same whole values. Stated counts are capped at
 *       10&nbsp;000</li>
 *   <li>Groups and alternation: {@code (a|b)}, {@code (?:a|b)}, {@code (?<name>a|b)}, and {@code |}
 *       at any level</li>
 *   <li>Assertions: {@code ^}, {@code \A}, {@code $}, {@code \Z}, {@code \z} and {@code \b}, where
 *       they hold on every value the pattern can generate. That is checked at construction, so
 *       {@code \bfoo\b} and {@code \b(foo|bar)\b} are accepted, while {@code a^b},
 *       {@code \b[a-z -]+\b} and {@code \b\w*\b}, whose values could start with a space or be
 *       empty, are rejected</li>
 * </ul>
 *
 * <p><b>Rejected at construction</b>
 * <ul>
 *   <li>Lookahead, lookbehind, atomic groups and backreferences</li>
 *   <li>Inline flags such as {@code (?i)} and {@code (?s:a)}</li>
 *   <li>Possessive quantifiers, such as {@code a++}, and a quantifier with nothing to repeat, such as
 *       {@code a{2}{3}}, which {@code java.util.regex} accepts and reads as repeating nothing</li>
 *   <li>Other escapes: {@code \B}, {@code \G}, {@code \b{g}}, {@code \R}, {@code \X}, {@code \h},
 *       {@code \H}, {@code \v}, {@code \V}, {@code \p}, {@code \P}, {@code \N}, {@code \c}, octal
 *       {@code \0} and quoting with {@code \Q}</li>
 *   <li>In a character class: {@code &&} intersection, a nested class, a supplementary character,
 *       and negating all of printable ASCII</li>
 *   <li>A surrogate on its own or in an escape, as two of them could join up in a generated value.
 *       Write a supplementary character as itself or as {@code \x{h...h}}</li>
 * </ul>
 *
 * <p><b>Open-ended repetition:</b> {@code *}, {@code +} and {@code {n,}} have no upper bound in the
 * pattern, so generation gives them one. They repeat at most 10 times, or {@code n} times when that
 * is more. A {@link GeneratorConfig} whose minimum string length is above 5 raises 10 to twice that
 * length, up to 10&nbsp;000, so that a generated string can reach it. This is why {@code ^[a-z]+$}
 * can meet a JSON Schema {@code minLength} of 20.
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
 * String fixed = new RegexGenerator("\\d{4}", GeneratorConfig.builder().seed(42L).build()).generate();
 * }</pre>
 *
 * <p><b>Integration with ObjectGenerator</b>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .objectOverride(String.class, new RegexGenerator("\\d{3}-\\d{4}"))
 *     .build();
 * }</pre>
 *
 * <p><b>Thread Safety:</b>
 * This generator is <em>not</em> thread-safe; create one instance per thread, or guard with external synchronisation.
 */
public final class RegexGenerator implements Generator<String> {

    // ── Pre-built character pools ─────────────────────────────────────────────

    private static final char[] PRINTABLE;
    private static final char[] DIGITS;
    private static final char[] NON_DIGITS;
    private static final char[] WORD_CHARS;
    private static final char[] NON_WORD_CHARS;
    private static final char[] WHITESPACE = {' ', '\t', '\n', '\r'};
    private static final char[] NON_WHITESPACE;
    private static final Map<String, Node> PARSED_PATTERNS = new ConcurrentHashMap<>();

    /**
     * Upper bound on explicit {@code {n}} / {@code {n,m}} repetition counts. Patterns requesting
     * more are rejected at construction time so a single quantifier cannot drive unbounded output
     * expansion (e.g. {@code a{2000000000}} attempting a multi-gigabyte string).
     */
    static final int MAX_REPETITIONS = 10_000;

    /** How many times {@code *}, {@code +} and {@code {n,}} repeat, unless the configuration asks for more. */
    private static final int OPEN_ENDED_REPETITIONS = 10;

    /** The upper count of a quantifier with no upper bound. Each generator resolves it to its own limit. */
    private static final int OPEN_ENDED = Integer.MAX_VALUE;

    // What can sit beside a zero-width assertion, as a set of bits. The edge is the value's start or end.
    private static final int EDGE = 1;
    private static final int WORD = 2;
    private static final int OTHER = 4;

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

        List<Character> printable = new ArrayList<>();
        List<Character> nonDigits = new ArrayList<>();
        List<Character> nonWord = new ArrayList<>();
        List<Character> nonWs = new ArrayList<>();
        for (char c = 0x20; c < 0x7f; c++) {
            printable.add(c);
            if (!digitSet.contains(c)) nonDigits.add(c);
            if (!wordSet.contains(c)) nonWord.add(c);
            if (!wsSet.contains(c)) nonWs.add(c);
        }
        PRINTABLE = toArray(printable);
        NON_DIGITS = toArray(nonDigits);
        NON_WORD_CHARS = toArray(nonWord);
        NON_WHITESPACE = toArray(nonWs);
    }

    /**
     * The compiled root node.
     */
    private final Node root;

    // ── Node sealed hierarchy ─────────────────────────────────────────────────
    private final Random random;

    /**
     * How many times this generator repeats an open-ended quantifier. It is kept here because parsed
     * patterns are shared between generators with different configurations.
     */
    private final int openEndedLimit;

    /**
     * Creates a regex generator using the default fast PRNG.
     *
     * @param pattern the regular expression; must not be {@code null}
     * @throws IllegalArgumentException if the pattern is malformed or uses a construct this class cannot honour
     */
    public RegexGenerator(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        this.random = new Random();
        this.openEndedLimit = OPEN_ENDED_REPETITIONS;
        this.root = parsePattern(pattern);
    }

    /**
     * Creates a regex generator whose randomness comes from the configuration, so a seeded
     * configuration reproduces the same strings. A minimum string length above 5 raises how far
     * {@code *}, {@code +} and {@code {n,}} repeat, as the class documentation describes.
     *
     * @param pattern the regular expression; must not be {@code null}
     * @param config  generator configuration; must not be {@code null}
     * @throws IllegalArgumentException if the pattern is malformed or uses a construct this class cannot honour
     */
    public RegexGenerator(String pattern, GeneratorConfig config) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
        this.openEndedLimit = Math.clamp(2L * config.getMinStringLength(), OPEN_ENDED_REPETITIONS, MAX_REPETITIONS);
        this.root = parsePattern(pattern);
    }

    static int parsedPatternCacheSize() {
        return PARSED_PATTERNS.size();
    }

    static void clearParsedPatternCacheForTests() {
        PARSED_PATTERNS.clear();
    }

    private static Node parsePattern(String pattern) {
        return PARSED_PATTERNS.computeIfAbsent(pattern, RegexGenerator::parsePatternUncached);
    }

    private static Node parsePatternUncached(String pattern) {
        Pattern.compile(pattern); // A malformed pattern fails here, with java.util.regex's own message.
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
            case LiteralNode l -> Character.toString(l.codePoint());
            case CharClassNode cc -> String.valueOf(cc.pool()[random.nextInt(cc.pool().length)]);
            case AssertionNode a -> "";
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
                int max = q.max() == OPEN_ENDED ? Math.max(q.min(), openEndedLimit) : q.max();
                int count = q.min() == max ? q.min() : q.min() + random.nextInt(max - q.min() + 1);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) sb.append(generateNode(q.child()));
                yield sb.toString();
            }
        };
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private sealed interface Node
            permits LiteralNode, CharClassNode, AssertionNode, SequenceNode, AlternationNode, QuantifierNode {}

    private record LiteralNode(int codePoint) implements Node {}

    // ── Constructors ──────────────────────────────────────────────────────────

    private record CharClassNode(char[] pool) implements Node {}

    /**
     * A zero-width assertion, as written in the pattern. It generates nothing: the parser has proved it
     * holds wherever this tree can put it.
     */
    private record AssertionNode(String text, int position) implements Node {}

    private record SequenceNode(List<Node> nodes) implements Node {}

    // ── Generator<String> ─────────────────────────────────────────────────────

    /**
     * Picks one of several alternative sequences at random.
     */
    private record AlternationNode(List<SequenceNode> alts) implements Node {}

    // ── Generation helpers ────────────────────────────────────────────────────

    private record QuantifierNode(Node child, int min, int max) implements Node {}

    // ── Parser ────────────────────────────────────────────────────────────────

    /**
     * Recursive-descent parser that converts a pattern string into a node tree.
     * Mutable only during the parse phase (construction time).
     *
     * <p>{@code java.util.regex} has already accepted the pattern, so this parser does not look for
     * malformed syntax. Its job is to refuse what it does not model: every construct gets a node that
     * generates matching text or fails here, and nothing unknown is read as a literal.
     */
    private static final class Parser {

        private final String src;
        private int pos = 0;

        Parser(String src) {
            this.src = src;
        }

        /**
         * Entry point — returns the root node for the whole pattern.
         */
        Node parse() {
            Node root = parseAlternatives();
            checkAssertions(root, EDGE, EDGE);
            return root;
        }

        private IllegalArgumentException unsupported(String construct, int position) {
            return new IllegalArgumentException(
                    "Unsupported " + construct + " at position " + position + " in pattern: " + src);
        }

        // ── Alternation and sequence (concatenation) ──────────────────────────

        /**
         * Parses one or more sequences separated by {@code |}. A lone sequence stays as it is, so it
         * draws no random number to choose itself.
         */
        private Node parseAlternatives() {
            List<SequenceNode> alts = new ArrayList<>();
            alts.add(parseSequence());
            while (pos < src.length() && src.charAt(pos) == '|') {
                pos++;
                alts.add(parseSequence());
            }
            return alts.size() == 1 ? alts.getFirst() : new AlternationNode(List.copyOf(alts));
        }

        /**
         * Parses zero or more atoms, stopping at end-of-string, {@code |}, or {@code )}.
         */
        private SequenceNode parseSequence() {
            List<Node> nodes = new ArrayList<>();
            while (pos < src.length() && src.charAt(pos) != '|' && src.charAt(pos) != ')') {
                nodes.add(parseQuantifier(parseAtom()));
            }
            return new SequenceNode(List.copyOf(nodes));
        }

        // ── Atom ──────────────────────────────────────────────────────────────

        private Node parseAtom() {
            char c = src.charAt(pos);
            return switch (c) {
                case '(' -> parseGroup();
                case '[' -> parseCharClass();
                case '\\' -> parseEscape();
                case '.' -> {
                    pos++;
                    yield new CharClassNode(PRINTABLE);
                }
                case '^', '$' -> new AssertionNode(String.valueOf(c), pos++);
                // java.util.regex accepts this and reads it as repeating nothing, unlike every other use of '{'.
                case '{' -> throw unsupported("quantifier with nothing to repeat", pos);
                default -> new LiteralNode(parseChar());
            };
        }

        /**
         * Consumes one literal character, written as itself or as an escape, and returns its code point.
         */
        private int parseChar() {
            int start = pos;
            int c = src.codePointAt(pos);
            pos += Character.charCount(c);
            int value = c == '\\' ? parseEscapedChar(start) : c;
            if (Character.getType(value) == Character.SURROGATE) {
                throw unsupported("surrogate", start);
            }
            return value;
        }

        // ── Group (a|b) / (?:a|b) / (?<name>a|b) ──────────────────────────────

        private Node parseGroup() {
            int start = pos;
            pos++; // consume '('
            if (src.charAt(pos) == '?') {
                parseGroupPrefix(start);
            }
            Node body = parseAlternatives();
            pos++; // consume ')'
            return body;
        }

        /**
         * Consumes what follows {@code (} in the two group forms that only change what is captured, and
         * rejects every other form, as each of those changes what the group matches.
         */
        private void parseGroupPrefix(int start) {
            switch (src.charAt(pos + 1)) {
                case ':' -> pos += 2;
                case '<' -> {
                    char next = src.charAt(pos + 2);
                    if (next == '=' || next == '!') {
                        throw unsupported("lookbehind", start);
                    }
                    pos = src.indexOf('>', pos) + 1;
                }
                case '=', '!' -> throw unsupported("lookahead", start);
                case '>' -> throw unsupported("atomic group", start);
                default -> throw unsupported("inline flag", start);
            }
        }

        // ── Character class [abc] / [a-z] / [^abc] ───────────────────────────

        private Node parseCharClass() {
            int start = pos;
            pos++; // consume '['
            boolean negated = src.charAt(pos) == '^';
            if (negated) pos++;

            List<Character> pool = new ArrayList<>();
            // Look for the end only after an item, as a ']' straight after the opening is a literal.
            do {
                parseClassItem(pool);
            } while (src.charAt(pos) != ']');
            pos++; // consume ']'

            if (negated) {
                Set<Character> excludeSet = new HashSet<>(pool);
                pool.clear();
                for (char c : PRINTABLE) {
                    if (!excludeSet.contains(c)) pool.add(c);
                }
                if (pool.isEmpty()) {
                    throw unsupported("negated character class that excludes all of printable ASCII", start);
                }
            }
            return new CharClassNode(toArray(pool));
        }

        /**
         * Adds one predefined class, range or character to the pool. The pool keeps source order and
         * repeats, so that a class keeps the values it has always drawn for a seed.
         */
        private void parseClassItem(List<Character> pool) {
            if (src.startsWith("&&", pos)) {
                throw unsupported("character class intersection &&", pos);
            }
            if (src.charAt(pos) == '[') {
                throw unsupported("nested character class", pos);
            }
            char[] predefined = src.charAt(pos) == '\\' ? predefinedClass(src.charAt(pos + 1)) : null;
            if (predefined != null) {
                pos += 2;
                for (char c : predefined) pool.add(c);
                return;
            }
            char low = parseClassChar();
            if (src.charAt(pos) == '-' && src.charAt(pos + 1) != ']' && src.charAt(pos + 1) != '[') {
                pos++; // consume '-'
                char high = parseClassChar();
                for (int c = low; c <= high; c++) {
                    // Two surrogates side by side would read as one character, so they are not drawn.
                    if (!Character.isSurrogate((char) c)) pool.add((char) c);
                }
            } else {
                pool.add(low);
            }
        }

        private char parseClassChar() {
            int start = pos;
            int c = parseChar();
            if (Character.isSupplementaryCodePoint(c)) {
                throw unsupported("supplementary character in a character class", start);
            }
            return (char) c;
        }

        // ── Escape sequences \d \w etc. ───────────────────────────────────────

        private static char @org.jspecify.annotations.Nullable [] predefinedClass(char escaped) {
            return switch (escaped) {
                case 'd' -> DIGITS;
                case 'D' -> NON_DIGITS;
                case 'w' -> WORD_CHARS;
                case 'W' -> NON_WORD_CHARS;
                case 's' -> WHITESPACE;
                case 'S' -> NON_WHITESPACE;
                default -> null;
            };
        }

        private Node parseEscape() {
            int start = pos;
            char escaped = src.charAt(pos + 1);
            char[] predefined = predefinedClass(escaped);
            if (predefined != null) {
                pos += 2;
                return new CharClassNode(predefined);
            }
            return switch (escaped) {
                case 'A', 'Z', 'z', 'b' -> {
                    pos += 2;
                    // Only \b can be followed by this: elsewhere java.util.regex rejects "{g}" as a repetition.
                    if (src.startsWith("{g}", pos)) {
                        throw unsupported("grapheme boundary \\b{g}", start);
                    }
                    yield new AssertionNode("\\" + escaped, start);
                }
                default -> new LiteralNode(parseChar());
            };
        }

        /**
         * Returns the code point that an escape stands for, with {@code pos} just after its backslash.
         */
        private int parseEscapedChar(int start) {
            int c = src.codePointAt(pos);
            pos += Character.charCount(c);
            return switch (c) {
                case 't' -> '\t';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 'f' -> '\f';
                case 'a' -> '\u0007';
                case 'e' -> '\u001B';
                case 'x' -> {
                    boolean braced = src.charAt(pos) == '{';
                    int from = braced ? pos + 1 : pos;
                    int to = braced ? src.indexOf('}', pos) : pos + 2;
                    pos = braced ? to + 1 : to;
                    yield Integer.parseInt(src, from, to, 16);
                }
                case 'u' -> {
                    pos += 4;
                    yield Integer.parseInt(src, pos - 4, pos, 16);
                }
                default -> {
                    if (c < 0x80 && Character.isLetterOrDigit(c)) {
                        throw unsupported(describeEscape(c), start);
                    }
                    yield c; // A backslash before anything else makes it a literal.
                }
            };
        }

        private static String describeEscape(int c) {
            String construct = switch (c) {
                case '1', '2', '3', '4', '5', '6', '7', '8', '9', 'k' -> "backreference";
                case 'p', 'P' -> "character property";
                default -> "escape";
            };
            return construct + " \\" + Character.toString(c);
        }

        // ── Quantifiers ?, +, *, {n}, {n,}, {n,m} ─────────────────────────────

        private Node parseQuantifier(Node node) {
            if (pos == src.length()) return node;
            int start = pos;
            int min;
            int max;
            switch (src.charAt(pos)) {
                case '?' -> {
                    pos++;
                    min = 0;
                    max = 1;
                }
                case '+' -> {
                    pos++;
                    min = 1;
                    max = OPEN_ENDED;
                }
                case '*' -> {
                    pos++;
                    min = 0;
                    max = OPEN_ENDED;
                }
                case '{' -> {
                    pos++; // consume '{'
                    min = parseCount();
                    max = min;
                    if (src.charAt(pos) == ',') {
                        pos++; // consume ','
                        max = src.charAt(pos) == '}' ? OPEN_ENDED : parseCount();
                    }
                    pos++; // consume '}'
                }
                default -> {
                    return node;
                }
            }
            char suffix = pos < src.length() ? src.charAt(pos) : '\0';
            if (suffix == '+') {
                // It refuses to give back what it took, so a value built from it may not match: a*+a.
                throw unsupported("possessive quantifier", start);
            }
            if (suffix == '?') {
                pos++; // A lazy quantifier matches the same whole values as a greedy one.
            }
            return new QuantifierNode(node, min, max);
        }

        private int parseCount() {
            int start = pos;
            while (Character.isDigit(src.charAt(pos))) pos++;
            int count = Integer.parseInt(src, start, pos, 10); // java.util.regex has checked that it fits.
            if (count > MAX_REPETITIONS) {
                throw new IllegalArgumentException("Quantifier repetition " + count
                        + " exceeds the maximum supported value " + MAX_REPETITIONS + " in pattern: " + src);
            }
            return count;
        }

        // ── Zero-width assertions ─────────────────────────────────────────────

        /**
         * Proves that every assertion under {@code node} holds on every value the tree can generate, or
         * rejects it. {@code before} and {@code after} are what can sit on either side of the node. A
         * pair that never occurs together still counts, so this can reject a pattern whose values would
         * all match, but it never accepts one with a value that would not.
         */
        private void checkAssertions(Node node, int before, int after) {
            switch (node) {
                case LiteralNode literal -> {}
                case CharClassNode charClass -> {}
                case AssertionNode assertion -> {
                    if (!holds(assertion.text(), before, after)) {
                        throw unsupported(assertion.text() + " that cannot hold on every value", assertion.position());
                    }
                }
                case SequenceNode sequence -> {
                    List<Node> nodes = sequence.nodes();
                    int[] following = new int[nodes.size()];
                    int seen = after;
                    for (int i = nodes.size() - 1; i >= 0; i--) {
                        following[i] = seen;
                        seen = kinds(nodes.get(i), false, seen);
                    }
                    seen = before;
                    for (int i = 0; i < nodes.size(); i++) {
                        checkAssertions(nodes.get(i), seen, following[i]);
                        seen = kinds(nodes.get(i), true, seen);
                    }
                }
                case AlternationNode alternation -> {
                    for (SequenceNode alt : alternation.alts()) checkAssertions(alt, before, after);
                }
                case QuantifierNode q -> {
                    // A repeated body also sits beside its own text.
                    boolean repeats = q.max() > 1;
                    checkAssertions(
                            q.child(),
                            repeats ? before | kinds(q.child(), true, before) : before,
                            repeats ? after | kinds(q.child(), false, after) : after);
                }
            }
        }

        private static boolean holds(String assertion, int before, int after) {
            return switch (assertion) {
                case "^", "\\A" -> before == EDGE;
                case "$", "\\Z", "\\z" -> after == EDGE;
                default -> (before == WORD && (after & WORD) == 0) || (after == WORD && (before & WORD) == 0);
            };
        }

        /**
         * Returns what can be the last character of the node's text, or the first when {@code last} is
         * false. Text that can be empty lets {@code beyond}, what lies past the node, show through.
         */
        private static int kinds(Node node, boolean last, int beyond) {
            return switch (node) {
                case LiteralNode literal -> kindOf(literal.codePoint());
                case CharClassNode charClass -> {
                    int seen = 0;
                    for (char c : charClass.pool()) seen |= kindOf(c);
                    yield seen;
                }
                case AssertionNode assertion -> beyond;
                case SequenceNode sequence -> {
                    int seen = beyond;
                    for (Node n : last ? sequence.nodes() : sequence.nodes().reversed()) {
                        seen = kinds(n, last, seen);
                    }
                    yield seen;
                }
                case AlternationNode alternation -> {
                    int seen = 0;
                    for (SequenceNode alt : alternation.alts()) seen |= kinds(alt, last, beyond);
                    yield seen;
                }
                case QuantifierNode q ->
                    q.min() == 0 ? kinds(q.child(), last, beyond) | beyond : kinds(q.child(), last, beyond);
            };
        }

        /**
         * Only ASCII has a fixed answer: {@code \b} can also count a non-ASCII character as part of a
         * word, depending on what is next to it, so such a character could be either.
         */
        private static int kindOf(int c) {
            if (c >= 0x80) return WORD | OTHER;
            return Character.isLetterOrDigit(c) || c == '_' ? WORD : OTHER;
        }
    }
}
