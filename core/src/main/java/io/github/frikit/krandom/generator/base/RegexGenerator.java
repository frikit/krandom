/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * JSON Schema's {@code pattern}. The pattern is compiled with no flags, so the only flag that applies
 * is an inline {@code (?i)}.
 *
 * <p><b>Supported syntax</b>
 * <ul>
 *   <li>Literals: any character that is not special, including supplementary characters</li>
 *   <li>Escaped characters: a backslash before anything but an ASCII letter or digit, such as
 *       {@code \.}, plus {@code \t}, {@code \n}, {@code \r}, {@code \f}, {@code \a}, {@code \e},
 *       {@code \xhh}, {@code \x{h...h}} and <code>&#92;uhhhh</code></li>
 *   <li>Quoting: {@code \Q...\E}, and {@code \Q} to the end of the pattern, read as
 *       {@code java.util.regex} reads them: a quantifier after {@code \E} repeats the last quoted
 *       character, so {@code \Qab\E+} generates {@code ab}, {@code abb} and so on</li>
 *   <li>Any character: {@code .} draws from printable ASCII</li>
 *   <li>Predefined classes: {@code \d}, {@code \w}, {@code \s}, and their negations {@code \D},
 *       {@code \W} and {@code \S}, which draw from printable ASCII</li>
 *   <li>Character properties: {@code \p{Name}} and its negation {@code \P{Name}} for the names
 *       listed below, and the one-letter forms {@code \pL}, {@code \pN}, {@code \PL} and
 *       {@code \PN}. A property draws from a fixed alphabet. A negated property draws from the
 *       printable ASCII characters outside it, so {@code \P{ASCII}} and {@code \P{Print}}, which
 *       leave none, are rejected</li>
 *   <li>Character classes: {@code [abc]}, {@code [a-z]} and {@code [^abc]}, with the escapes and
 *       properties above as members, and the escapes as range ends. A negated class draws from
 *       printable ASCII</li>
 *   <li>Quantifiers: {@code ?}, {@code *}, {@code +}, {@code {n}}, {@code {n,}} and {@code {n,m}},
 *       and their lazy forms, which match the same whole values. Stated counts are capped at
 *       10&nbsp;000</li>
 *   <li>Groups and alternation: {@code (a|b)}, {@code (?:a|b)}, {@code (?<name>a|b)}, and {@code |}
 *       at any level</li>
 *   <li>Case-insensitive matching: {@code (?i)} and {@code (?-i)}, which hold to the end of the
 *       enclosing group, and {@code (?i:X)} and {@code (?-i:X)}, which hold inside it. As in
 *       {@code java.util.regex} without {@code UNICODE_CASE}, only ASCII letters match their other
 *       case, so a generated ASCII letter can take either case, and every other character keeps
 *       the one written. A negated class or property under {@code (?i)} leaves out both cases of
 *       each letter it excludes</li>
 *   <li>Assertions: {@code ^}, {@code \A}, {@code $}, {@code \Z}, {@code \z} and {@code \b}, where
 *       they hold on every value the pattern can generate. That is checked at construction, so
 *       {@code \bfoo\b} and {@code \b(foo|bar)\b} are accepted, while {@code a^b},
 *       {@code \b[a-z -]+\b} and {@code \b\w*\b}, whose values could start with a space or be
 *       empty, are rejected. A non-ASCII character can count as a word character or not, so a
 *       {@code \b} beside one is rejected</li>
 * </ul>
 *
 * <p><b>Character properties.</b> The ASCII ones draw from what they match: {@code Lower}
 * ({@code a-z}), {@code Upper} ({@code A-Z}), {@code Alpha}, {@code Digit} ({@code 0-9}),
 * {@code Alnum}, {@code Punct} (the 32 ASCII punctuation characters), {@code Graph}
 * (U+0021-U+007E), {@code Print} (U+0020-U+007E), {@code Blank} (space and tab), {@code Space}
 * (space, {@code \t}, {@code \n}, U+000B, {@code \f} and {@code \r}), {@code XDigit}
 * ({@code 0-9a-fA-F}) and {@code ASCII}, which draws from printable ASCII. The Unicode ones draw
 * from a fixed sample of what they match:
 * <ul>
 *   <li>{@code L} and {@code IsAlphabetic}: {@code A-Z}, {@code a-z}, the Latin-1 letters
 *       U+00C0-U+00D6, U+00D8-U+00F6 and U+00F8-U+00FF, the Greek letters U+0391-U+03A1,
 *       U+03A3-U+03A9 and U+03B1-U+03C9, and the Cyrillic letters U+0410-U+044F</li>
 *   <li>{@code Lu}: the uppercase ones, {@code A-Z}, U+00C0-U+00D6, U+00D8-U+00DE,
 *       U+0391-U+03A1, U+03A3-U+03A9 and U+0410-U+042F</li>
 *   <li>{@code Ll}: the lowercase ones, {@code a-z}, U+00DF-U+00F6, U+00F8-U+00FF,
 *       U+03B1-U+03C9 and U+0430-U+044F</li>
 *   <li>{@code IsLatin}: {@code A-Z}, {@code a-z}, U+00C0-U+00D6, U+00D8-U+00F6 and
 *       U+00F8-U+00FF</li>
 *   <li>{@code Nd} and {@code IsDigit}: {@code 0-9}, the Arabic-Indic digits U+0660-U+0669 and
 *       the Devanagari digits U+0966-U+096F</li>
 *   <li>{@code N}: the {@code Nd} sample, and U+00B2, U+00B3, U+00B9 and U+00BC-U+00BE</li>
 * </ul>
 * Names are spelled as here. Any other property, including another spelling that
 * {@code java.util.regex} accepts, such as {@code \p{IsLower}}, {@code \p{Islatin}} or
 * {@code \p{gc=Lu}}, is rejected at construction with a message naming it.
 *
 * <p><b>Rejected at construction</b>
 * <ul>
 *   <li>Lookahead, lookbehind, atomic groups and backreferences</li>
 *   <li>Inline flags other than {@code i}, such as {@code (?s)}, {@code (?u)} and {@code (?-m)},
 *       with a message naming each one</li>
 *   <li>Possessive quantifiers, such as {@code a++}, and a quantifier with nothing to repeat, such as
 *       {@code a{2}{3}} or {@code (?i){2}}, which {@code java.util.regex} accepts and reads as
 *       repeating nothing</li>
 *   <li>Other escapes: {@code \B}, {@code \G}, {@code \b{g}}, {@code \R}, {@code \X}, {@code \h},
 *       {@code \H}, {@code \v}, {@code \V}, {@code \N}, {@code \c} and octal {@code \0}</li>
 *   <li>In a character class: {@code &&} intersection, a nested class, a supplementary character,
 *       and negating all of printable ASCII</li>
 *   <li>A surrogate on its own or in an escape, as two of them could join up in a generated value.
 *       Write a supplementary character as itself or as {@code \x{h...h}}</li>
 *   <li>A pattern too large to generate, as described below</li>
 * </ul>
 *
 * <p><b>Open-ended repetition:</b> {@code *}, {@code +} and {@code {n,}} have no upper bound in the
 * pattern, so generation gives them one. They repeat at most 10 times, or {@code n} times when that
 * is more. A {@link GeneratorConfig} whose minimum string length is above 5 raises 10 to twice that
 * length, up to 10&nbsp;000, so that a generated string can reach it. This is why {@code ^[a-z]+$}
 * can meet a JSON Schema {@code minLength} of 20.
 *
 * <p><b>Size limits:</b> the constructor works out, from the pattern and the repetition limits
 * above, the longest value the pattern can generate and the most steps generating one can take. It
 * rejects the pattern, naming it and the limit, if a value could be longer than 1&nbsp;000&nbsp;000
 * characters, counted as UTF-16 {@code char}s, or take more than 10&nbsp;000&nbsp;000 steps, where
 * each element of the pattern, such as a character, class, assertion, group or quantifier, counts
 * one step each time it is generated. A value is built in a single buffer, so these bound the memory
 * as well as the time generating it takes. For example {@code (a{1000}){1000}} generates
 * 1&nbsp;000&nbsp;000 characters and is accepted, {@code ((a{10000}){10000}){10000}} is rejected
 * rather than run out of memory, and {@code (((){10000}){10000}){10000}}, which generates nothing, is
 * rejected rather than loop for hours. The limits are fixed and no setting raises them: a pattern over
 * them has to repeat less, or, when the configuration's minimum string length raised the open-ended
 * limit, run with a smaller one.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 * // 3-digit number
 * String code = new RegexGenerator("\\d{3}").generate();      // e.g. "472"
 *
 * // 5 uppercase letters
 * String upper = new RegexGenerator("[A-Z]{5}").generate();   // e.g. "KQTBM"
 *
 * // yes or no, in any case
 * String yn = new RegexGenerator("(?i)(yes|no)").generate();  // e.g. "yEs" or "NO"
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

    /** What each supported {@code \p{...}} name draws from. */
    private static final Map<String, char[]> PROPERTIES;

    private static final Map<String, ParsedPattern> PARSED_PATTERNS = new ConcurrentHashMap<>();

    /**
     * Upper bound on explicit {@code {n}} / {@code {n,m}} repetition counts. Patterns requesting
     * more are rejected at construction time, before the size limits below are worked out.
     */
    static final int MAX_REPETITIONS = 10_000;

    /** The most UTF-16 {@code char}s a generated value can have. */
    static final int MAX_LENGTH = 1_000_000;

    /** The most steps, one for each node each time it is generated, that generating a value can take. */
    static final int MAX_STEPS = 10_000_000;

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

        // Each sample lists its ranges as pairs of their first and last code points.
        char[] latin = ranges('A', 'Z', 'a', 'z', 0xC0, 0xD6, 0xD8, 0xF6, 0xF8, 0xFF);
        char[] letters = ranges(
                'A', 'Z', 'a', 'z', 0xC0, 0xD6, 0xD8, 0xF6, 0xF8, 0xFF, 0x391, 0x3A1, 0x3A3, 0x3A9, 0x3B1, 0x3C9, 0x410,
                0x44F);
        char[] decimalDigits = ranges('0', '9', 0x660, 0x669, 0x966, 0x96F);
        Map<String, char[]> properties = new LinkedHashMap<>();
        properties.put("Lower", ranges('a', 'z'));
        properties.put("Upper", ranges('A', 'Z'));
        properties.put("ASCII", PRINTABLE);
        properties.put("Alpha", ranges('a', 'z', 'A', 'Z'));
        properties.put("Digit", DIGITS);
        properties.put("Alnum", ranges('a', 'z', 'A', 'Z', '0', '9'));
        properties.put("Punct", ranges('!', '/', ':', '@', '[', '`', '{', '~'));
        properties.put("Graph", ranges('!', '~'));
        properties.put("Print", PRINTABLE);
        properties.put("Blank", ranges(' ', ' ', '\t', '\t'));
        properties.put("Space", ranges(' ', ' ', '\t', '\r'));
        properties.put("XDigit", ranges('0', '9', 'a', 'f', 'A', 'F'));
        properties.put("L", letters);
        properties.put("Lu", ranges('A', 'Z', 0xC0, 0xD6, 0xD8, 0xDE, 0x391, 0x3A1, 0x3A3, 0x3A9, 0x410, 0x42F));
        properties.put("Ll", ranges('a', 'z', 0xDF, 0xF6, 0xF8, 0xFF, 0x3B1, 0x3C9, 0x430, 0x44F));
        properties.put("N", ranges('0', '9', 0x660, 0x669, 0x966, 0x96F, 0xB2, 0xB3, 0xB9, 0xB9, 0xBC, 0xBE));
        properties.put("Nd", decimalDigits);
        properties.put("IsAlphabetic", letters);
        properties.put("IsDigit", decimalDigits);
        properties.put("IsLatin", latin);
        PROPERTIES = Collections.unmodifiableMap(properties);
    }

    /**
     * The compiled root node.
     */
    private final Node root;

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
     * @throws IllegalArgumentException if the pattern is malformed, uses a construct this class cannot
     *     honour, or exceeds the size limits
     */
    public RegexGenerator(String pattern) {
        this(Objects.requireNonNull(pattern, "pattern must not be null"), new Random(), OPEN_ENDED_REPETITIONS);
    }

    /**
     * Creates a regex generator whose randomness comes from the configuration, so a seeded
     * configuration reproduces the same strings. A minimum string length above 5 raises how far
     * {@code *}, {@code +} and {@code {n,}} repeat, as the class documentation describes, and the size
     * limits apply to the raised repetition.
     *
     * @param pattern the regular expression; must not be {@code null}
     * @param config  generator configuration; must not be {@code null}
     * @throws IllegalArgumentException if the pattern is malformed, uses a construct this class cannot
     *     honour, or exceeds the size limits under this configuration
     */
    public RegexGenerator(String pattern, GeneratorConfig config) {
        this(
                Objects.requireNonNull(pattern, "pattern must not be null"),
                Objects.requireNonNull(config, "config must not be null").createRandom(),
                (int) Math.min(MAX_REPETITIONS, Math.max(2L * config.getMinStringLength(), OPEN_ENDED_REPETITIONS)));
    }

    private RegexGenerator(String pattern, Random random, int openEndedLimit) {
        ParsedPattern parsed = PARSED_PATTERNS.computeIfAbsent(pattern, RegexGenerator::parsePatternUncached);
        if (openEndedLimit > parsed.largestOpenEndedLimit()) {
            throw tooLarge(pattern, parsed.root(), openEndedLimit);
        }
        this.root = parsed.root();
        this.random = random;
        this.openEndedLimit = openEndedLimit;
    }

    static int parsedPatternCacheSize() {
        return PARSED_PATTERNS.size();
    }

    static void clearParsedPatternCacheForTests() {
        PARSED_PATTERNS.clear();
    }

    /** The supported {@code \p{...}} names, each with the characters it draws from, for tests. */
    static Map<String, String> propertyAlphabets() {
        Map<String, String> alphabets = new LinkedHashMap<>();
        PROPERTIES.forEach((name, alphabet) -> alphabets.put(name, new String(alphabet)));
        return alphabets;
    }

    private static ParsedPattern parsePatternUncached(String pattern) {
        Pattern.compile(pattern); // A malformed pattern fails here, with java.util.regex's own message.
        Node root = new Parser(pattern).parse();
        if (!fits(root, OPEN_ENDED_REPETITIONS)) {
            throw tooLarge(pattern, root, OPEN_ENDED_REPETITIONS);
        }
        return new ParsedPattern(root, largestFittingLimit(root));
    }

    private static char[] toArray(List<Character> list) {
        char[] arr = new char[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    /** Expands ranges, each given as its first and last code point, in order. */
    private static char[] ranges(int... bounds) {
        StringBuilder chars = new StringBuilder();
        for (int i = 0; i < bounds.length; i += 2) {
            for (int c = bounds[i]; c <= bounds[i + 1]; c++) chars.append((char) c);
        }
        return chars.toString().toCharArray();
    }

    private static boolean isAsciiLetter(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /** The other case of an ASCII letter. */
    private static char otherCase(char letter) {
        return (char) (letter ^ 0x20);
    }

    /**
     * {@inheritDoc}
     *
     * @return a random string matching the pattern; never {@code null}
     */
    @Override
    public String generate() {
        StringBuilder value = new StringBuilder();
        append(root, value);
        return value.toString();
    }

    // Every node appends to the one buffer, so nothing larger than the value is built along the way.
    private void append(Node node, StringBuilder value) {
        if (node instanceof LiteralNode literal) {
            value.appendCodePoint(literal.codePoint());
        } else if (node instanceof CharClassNode charClass) {
            value.append(charClass.pool()[random.nextInt(charClass.pool().length)]);
        } else if (node instanceof AssertionNode) {
            // An assertion matches a position and adds no text.
        } else if (node instanceof SequenceNode sequence) {
            for (Node n : sequence.nodes()) append(n, value);
        } else if (node instanceof AlternationNode alternation) {
            append(alternation.alts().get(random.nextInt(alternation.alts().size())), value);
        } else {
            QuantifierNode q = (QuantifierNode) node;
            int max = maxCount(q, openEndedLimit);
            int count = q.min() == max ? q.min() : q.min() + random.nextInt(max - q.min() + 1);
            for (int i = 0; i < count; i++) append(q.child(), value);
        }
    }

    private static int maxCount(QuantifierNode q, int openEndedLimit) {
        return q.max() == OPEN_ENDED ? Math.max(q.min(), openEndedLimit) : q.max();
    }

    // ── Size limits ───────────────────────────────────────────────────────────

    /**
     * The most UTF-16 {@code char}s a node's text can have and the most steps generating it can take,
     * each capped just above its limit, which keeps every sum and product well inside a {@code long}.
     */
    private record Cost(long length, long steps) {}

    private static Cost cost(Node node, int openEndedLimit) {
        if (node instanceof LiteralNode literal) {
            return new Cost(Character.charCount(literal.codePoint()), 1);
        }
        if (node instanceof CharClassNode) {
            return new Cost(1, 1);
        }
        if (node instanceof AssertionNode) {
            return new Cost(0, 1);
        }
        if (node instanceof SequenceNode sequence) {
            long length = 0;
            long steps = 1;
            for (Node n : sequence.nodes()) {
                Cost part = cost(n, openEndedLimit);
                length = capped(length + part.length(), MAX_LENGTH);
                steps = capped(steps + part.steps(), MAX_STEPS);
            }
            return new Cost(length, steps);
        }
        if (node instanceof AlternationNode alternation) {
            long length = 0;
            long steps = 0;
            for (SequenceNode alt : alternation.alts()) {
                Cost choice = cost(alt, openEndedLimit);
                length = Math.max(length, choice.length());
                steps = Math.max(steps, choice.steps());
            }
            return new Cost(length, capped(1 + steps, MAX_STEPS));
        }
        QuantifierNode q = (QuantifierNode) node;
        long count = maxCount(q, openEndedLimit);
        Cost each = cost(q.child(), openEndedLimit);
        return new Cost(capped(count * each.length(), MAX_LENGTH), capped(1 + count * each.steps(), MAX_STEPS));
    }

    private static long capped(long value, long limit) {
        return Math.min(value, limit + 1);
    }

    private static boolean fits(Node root, int openEndedLimit) {
        Cost cost = cost(root, openEndedLimit);
        return cost.length() <= MAX_LENGTH && cost.steps() <= MAX_STEPS;
    }

    /**
     * The largest open-ended repetition, up to {@link #MAX_REPETITIONS}, at which the pattern stays
     * within the size limits. It is at least the default, as the parse has checked that.
     */
    private static int largestFittingLimit(Node root) {
        if (fits(root, MAX_REPETITIONS)) {
            return MAX_REPETITIONS;
        }
        // A larger limit never makes a value shorter or quicker, so a binary search finds the boundary.
        int fitting = OPEN_ENDED_REPETITIONS;
        int failing = MAX_REPETITIONS;
        while (failing - fitting > 1) {
            int middle = (fitting + failing) >>> 1;
            if (fits(root, middle)) {
                fitting = middle;
            } else {
                failing = middle;
            }
        }
        return fitting;
    }

    private static IllegalArgumentException tooLarge(String pattern, Node root, int openEndedLimit) {
        String problem = cost(root, openEndedLimit).length() > MAX_LENGTH
                ? "Generated values could be longer than the limit of " + MAX_LENGTH + " characters"
                : "Generating a value could take more than the limit of " + MAX_STEPS + " steps";
        return new IllegalArgumentException(
                problem + ", with *, + and {n,} repeating up to " + openEndedLimit + " times, in pattern: " + pattern);
    }

    // ── Nodes ─────────────────────────────────────────────────────────────────

    /** A parsed pattern, and the largest open-ended repetition its values stay within the size limits at. */
    private record ParsedPattern(Node root, int largestOpenEndedLimit) {}

    /**
     * A node of a parsed pattern. Code that handles each kind tests for them in the order of this
     * {@code permits} clause and treats a node that is none of the first five as a {@link QuantifierNode}.
     */
    private sealed interface Node
            permits LiteralNode, CharClassNode, AssertionNode, SequenceNode, AlternationNode, QuantifierNode {}

    private record LiteralNode(int codePoint) implements Node {}

    private record CharClassNode(char[] pool) implements Node {}

    /**
     * A zero-width assertion, as written in the pattern. It generates nothing: the parser has proved it
     * holds wherever this tree can put it.
     */
    private record AssertionNode(String text, int position) implements Node {}

    private record SequenceNode(List<Node> nodes) implements Node {}

    /**
     * Picks one of several alternative sequences at random.
     */
    private record AlternationNode(List<SequenceNode> alts) implements Node {}

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

        /** The letters {@code java.util.regex} reads as inline flags, and the {@code -} that turns them off. */
        private static final String FLAG_CHARS = "idmsuxUc-";

        /** The pattern as written, for messages. */
        private final String pattern;

        /** The pattern with its {@code \Q...\E} quoting rewritten as escapes, which is what is parsed. */
        private final String src;

        /** Where each character of {@link #src} comes from in the pattern, and its length at the end. */
        private final int[] origins;

        private int pos = 0;

        /** Whether {@code (?i)} is in force at {@link #pos}. */
        private boolean caseInsensitive = false;

        Parser(String pattern) {
            this.pattern = pattern;
            StringBuilder text = new StringBuilder(pattern.length());
            // Quoting at most doubles a character, and a digit that opens a quote pays for its \x3 with
            // the \Q before it, so the result is never more than twice as long as the pattern.
            int[] from = new int[2 * pattern.length() + 1];
            unquote(pattern, text, from);
            this.src = text.toString();
            this.origins = from;
        }

        /**
         * Rewrites {@code \Q...\E} quoting as {@code java.util.regex} does before it parses, in
         * {@code Pattern.RemoveQEQuoting}: a quoted letter, digit or non-ASCII character stays as it is,
         * except that a digit opening a quote becomes {@code \x3} and the digit, so that it cannot extend
         * an escape before the quote, and any other quoted character gets a backslash. Text outside a
         * quote is copied, an escape as a pair, so that an escaped {@code \Q} stays as written.
         */
        private static void unquote(String pattern, StringBuilder text, int[] from) {
            boolean quoted = false;
            boolean quoteStart = false;
            int i = 0;
            while (i < pattern.length()) {
                int at = i;
                char c = pattern.charAt(i++);
                if (c >= 0x80 || isAsciiLetter(c)) {
                    emit(text, from, c, at);
                } else if (c >= '0' && c <= '9') {
                    if (quoteStart) {
                        emit(text, from, '\\', at);
                        emit(text, from, 'x', at);
                        emit(text, from, '3', at);
                    }
                    emit(text, from, c, at);
                } else if (c != '\\') {
                    if (quoted) {
                        emit(text, from, '\\', at);
                    }
                    emit(text, from, c, at);
                } else if (quoted) {
                    if (i < pattern.length() && pattern.charAt(i) == 'E') {
                        i++;
                        quoted = false;
                    } else {
                        emit(text, from, '\\', at);
                        emit(text, from, '\\', at);
                    }
                } else if (pattern.charAt(i) == 'Q') { // A pattern that compiles cannot end in a lone backslash.
                    i++;
                    quoted = true;
                    quoteStart = true;
                    continue;
                } else {
                    emit(text, from, c, at);
                    emit(text, from, pattern.charAt(i), i);
                    i++;
                }
                quoteStart = false;
            }
            from[text.length()] = pattern.length();
        }

        private static void emit(StringBuilder text, int[] from, char c, int origin) {
            from[text.length()] = origin;
            text.append(c);
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
                    "Unsupported " + construct + " at position " + origins[position] + " in pattern: " + pattern);
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
            return alts.size() == 1 ? alts.get(0) : new AlternationNode(List.copyOf(alts));
        }

        /**
         * Parses zero or more atoms, stopping at end-of-string, {@code |}, or {@code )}.
         */
        private SequenceNode parseSequence() {
            List<Node> nodes = new ArrayList<>();
            while (pos < src.length() && src.charAt(pos) != '|' && src.charAt(pos) != ')') {
                if (!parseInlineFlags()) {
                    nodes.add(parseQuantifier(parseAtom()));
                }
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
                default -> literal(parseChar());
            };
        }

        /**
         * A literal character. Under {@code (?i)} an ASCII letter can take either case, as
         * {@code java.util.regex} matches it in either, and nothing else changes.
         */
        private Node literal(int codePoint) {
            return caseInsensitive && isAsciiLetter(codePoint)
                    ? new CharClassNode(new char[] {(char) codePoint, otherCase((char) codePoint)})
                    : new LiteralNode(codePoint);
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

        // ── Group (a|b) / (?:a|b) / (?<name>a|b) / (?i:a|b) ───────────────────

        private Node parseGroup() {
            int start = pos;
            // As in java.util.regex, flags set inside a group end with it.
            boolean outerCaseInsensitive = caseInsensitive;
            pos++; // consume '('
            if (src.charAt(pos) == '?') {
                parseGroupPrefix(start);
            }
            Node body = parseAlternatives();
            pos++; // consume ')'
            caseInsensitive = outerCaseInsensitive;
            return body;
        }

        /**
         * Consumes what follows {@code (} in the group forms that only change what is captured or which
         * flags apply inside, and rejects every other form, as each of those changes what the group
         * matches.
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
                default -> {
                    // (?flags:X), as parseSequence has already taken each (?flags) that stands alone.
                    int end = flagsEnd(pos + 1);
                    caseInsensitive = applyFlags(start, pos + 1, end);
                    pos = end + 1; // consume ':'
                }
            }
        }

        /**
         * Consumes a group such as {@code (?i)} that only sets flags, which then hold to the end of the
         * enclosing group, and reports whether there was one. Nothing repeats it: {@code java.util.regex}
         * reads a quantifier after it as repeating nothing.
         */
        private boolean parseInlineFlags() {
            if (!src.startsWith("(?", pos)) {
                return false;
            }
            int end = flagsEnd(pos + 2);
            if (src.charAt(end) != ')') {
                return false;
            }
            caseInsensitive = applyFlags(pos, pos + 2, end);
            pos = end + 1;
            return true;
        }

        private int flagsEnd(int from) {
            int end = from;
            while (FLAG_CHARS.indexOf(src.charAt(end)) >= 0) {
                end++;
            }
            return end;
        }

        /**
         * Returns whether case-insensitive matching is on after the flags in {@code [from, end)}, and
         * rejects every flag but {@code i}, naming each one.
         */
        private boolean applyFlags(int groupStart, int from, int end) {
            boolean on = caseInsensitive;
            boolean turningOff = false;
            List<String> others = new ArrayList<>();
            for (int i = from; i < end; i++) {
                String flag = String.valueOf(src.charAt(i));
                if (flag.equals("-")) {
                    turningOff = true;
                } else if (flag.equals("i")) {
                    on = !turningOff;
                } else if (!others.contains(flag)) {
                    others.add(flag);
                }
            }
            if (!others.isEmpty()) {
                throw unsupported(
                        (others.size() == 1 ? "inline flag " : "inline flags ") + String.join(", ", others),
                        groupStart);
            }
            return on;
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

            if (!negated) {
                if (pool.isEmpty()) {
                    // Only a negated property, such as \P{ASCII}, adds nothing.
                    throw unsupported("character class that excludes all of printable ASCII", start);
                }
                return classNode(toArray(pool));
            }
            Set<Character> excludeSet = new HashSet<>(pool);
            if (caseInsensitive) {
                // java.util.regex matches either case of a member letter, so the class leaves out both.
                for (char c : pool) {
                    if (isAsciiLetter(c)) excludeSet.add(otherCase(c));
                }
            }
            pool.clear();
            for (char c : PRINTABLE) {
                if (!excludeSet.contains(c)) pool.add(c);
            }
            if (pool.isEmpty()) {
                throw unsupported("negated character class that excludes all of printable ASCII", start);
            }
            return new CharClassNode(toArray(pool));
        }

        /**
         * A node drawing from the pool. Under {@code (?i)} each ASCII letter's other case joins the pool,
         * unless it is there already.
         */
        private CharClassNode classNode(char[] pool) {
            if (!caseInsensitive) {
                return new CharClassNode(pool);
            }
            Set<Character> present = new HashSet<>();
            for (char c : pool) present.add(c);
            StringBuilder chars = new StringBuilder().append(pool);
            for (char c : pool) {
                if (isAsciiLetter(c) && present.add(otherCase(c))) {
                    chars.append(otherCase(c));
                }
            }
            return new CharClassNode(chars.toString().toCharArray());
        }

        /**
         * Adds one predefined class, property, range or character to the pool. The pool keeps source
         * order and repeats, so that a class keeps the values it has always drawn for a seed.
         */
        private void parseClassItem(List<Character> pool) {
            if (src.startsWith("&&", pos)) {
                throw unsupported("character class intersection &&", pos);
            }
            if (src.charAt(pos) == '[') {
                throw unsupported("nested character class", pos);
            }
            char[] members = src.charAt(pos) == '\\' ? parseClassEscape() : null;
            if (members != null) {
                for (char c : members) pool.add(c);
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

        // ── Escape sequences \d \w \p{L} etc. ─────────────────────────────────

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

        /**
         * Consumes a predefined class or a property, with {@code pos} at its backslash, and returns what it
         * draws from, or returns {@code null} and consumes nothing for any other escape.
         */
        private char @org.jspecify.annotations.Nullable [] parseClassEscape() {
            char escaped = src.charAt(pos + 1);
            if (escaped == 'p' || escaped == 'P') {
                return parseProperty();
            }
            char[] predefined = predefinedClass(escaped);
            if (predefined != null) {
                pos += 2;
            }
            return predefined;
        }

        /**
         * Parses {@code \p{Name}}, {@code \P{Name}}, {@code \pL} or {@code \PL}, with {@code pos} at its
         * backslash, and returns what it draws from.
         */
        private char[] parseProperty() {
            int start = pos;
            boolean negated = src.charAt(pos + 1) == 'P';
            pos += 2;
            String name;
            if (src.charAt(pos) == '{') {
                int close = src.indexOf('}', pos);
                name = src.substring(pos + 1, close);
                pos = close + 1;
            } else {
                // Every one-letter name java.util.regex knows is an ASCII letter.
                name = src.substring(pos, pos + 1);
                pos++;
            }
            char[] alphabet = PROPERTIES.get(name);
            if (alphabet == null) {
                throw unsupported("character property " + src.substring(start, pos), start);
            }
            if (!negated) {
                return alphabet;
            }
            // Each sample holds every printable ASCII character its property matches, so this is exact.
            Set<Character> excluded = new HashSet<>();
            for (char c : alphabet) {
                excluded.add(c);
                // Under (?i), java.util.regex counts a letter in the property if either case is.
                if (caseInsensitive && isAsciiLetter(c)) excluded.add(otherCase(c));
            }
            // It can leave nothing, which only matters where nothing else in a class adds to it.
            List<Character> pool = new ArrayList<>();
            for (char c : PRINTABLE) {
                if (!excluded.contains(c)) pool.add(c);
            }
            return toArray(pool);
        }

        private Node parseEscape() {
            int start = pos;
            char[] members = parseClassEscape();
            if (members != null) {
                if (members.length == 0) {
                    throw unsupported(
                            "character property " + src.substring(start, pos) + " that excludes all of printable ASCII",
                            start);
                }
                return classNode(members);
            }
            char escaped = src.charAt(pos + 1);
            return switch (escaped) {
                case 'A', 'Z', 'z', 'b' -> {
                    pos += 2;
                    // Only \b can be followed by this: elsewhere java.util.regex rejects "{g}" as a repetition.
                    if (src.startsWith("{g}", pos)) {
                        throw unsupported("grapheme boundary \\b{g}", start);
                    }
                    yield new AssertionNode("\\" + escaped, start);
                }
                default -> literal(parseChar());
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
                        + " exceeds the maximum supported value " + MAX_REPETITIONS + " in pattern: " + pattern);
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
            if (node instanceof LiteralNode || node instanceof CharClassNode) {
                // Neither holds an assertion.
            } else if (node instanceof AssertionNode assertion) {
                if (!holds(assertion.text(), before, after)) {
                    throw unsupported(assertion.text() + " that cannot hold on every value", assertion.position());
                }
            } else if (node instanceof SequenceNode sequence) {
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
            } else if (node instanceof AlternationNode alternation) {
                for (SequenceNode alt : alternation.alts()) checkAssertions(alt, before, after);
            } else {
                QuantifierNode q = (QuantifierNode) node;
                // A repeated body also sits beside its own text.
                boolean repeats = q.max() > 1;
                checkAssertions(
                        q.child(),
                        repeats ? before | kinds(q.child(), true, before) : before,
                        repeats ? after | kinds(q.child(), false, after) : after);
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
            if (node instanceof LiteralNode literal) {
                return kindOf(literal.codePoint());
            }
            if (node instanceof CharClassNode charClass) {
                int seen = 0;
                for (char c : charClass.pool()) seen |= kindOf(c);
                return seen;
            }
            if (node instanceof AssertionNode) {
                return beyond;
            }
            if (node instanceof SequenceNode sequence) {
                List<Node> nodes = sequence.nodes();
                int seen = beyond;
                // In order for the last character, and from the end for the first.
                for (int i = 0; i < nodes.size(); i++) {
                    seen = kinds(nodes.get(last ? i : nodes.size() - 1 - i), last, seen);
                }
                return seen;
            }
            if (node instanceof AlternationNode alternation) {
                int seen = 0;
                for (SequenceNode alt : alternation.alts()) seen |= kinds(alt, last, beyond);
                return seen;
            }
            QuantifierNode q = (QuantifierNode) node;
            return q.min() == 0 ? kinds(q.child(), last, beyond) | beyond : kinds(q.child(), last, beyond);
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
