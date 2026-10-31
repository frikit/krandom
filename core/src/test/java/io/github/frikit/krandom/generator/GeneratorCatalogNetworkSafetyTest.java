/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import io.github.frikit.krandom.generator.object.Fake;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.schema.SchemaParser;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Sweeps every generator factory and the other routes to a generator, so that a new generator is
 * covered without a test of its own: under the network contract, no URL host, email domain, IP literal
 * or dotted name in the text of a generated value may be a real destination.
 */
@DisplayName("Network destinations of the whole generator catalog")
class GeneratorCatalogNetworkSafetyTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
    private static final List<Locale> LOCALES = Stream.of(
                    "en-US", "en-GB", "en-AU", "de-DE", "fr-FR", "es-ES", "it-IT", "pt-BR", "ja-JP", "zh-CN", "tr-TR",
                    "ar-SA", "hi-IN")
            .map(Locale::forLanguageTag)
            .toList();
    private static final List<Long> SEEDS = List.of(42L, 7L);
    private static final int DRAWS = 8;

    private static final List<Method> FACTORIES = Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getParameterCount() == 1 && method.getParameterTypes()[0] == GeneratorConfig.class)
            .sorted(Comparator.comparing(Method::getName))
            .toList();

    /**
     * What looks like a destination but is deliberately exempt, and why. Every entry must turn up in the
     * sweep, so that the list cannot outlive its reason.
     */
    private static final Map<String, Pattern> ALLOWED = allowed();

    private static Map<String, Pattern> allowed() {
        Map<String, Pattern> allowed = new LinkedHashMap<>();
        // UserAgentGenerator and HttpFixture: a browser version such as Chrome/124.0.0.0, not an address.
        allowed.put("browser version", Pattern.compile("\\b[A-Za-z]+/\\d+\\.\\d+\\.\\d+\\.\\d+\\b"));
        // UserAgentGenerator: constants that identify a crawler by its vendor's page, as such a client does.
        allowed.put(
                "crawler page",
                Pattern.compile("\\+http://(?:www\\.google\\.com/bot\\.html"
                        + "|www\\.bing\\.com/bingbot\\.htm|duckduckgo\\.com/duckduckbot\\.html)"));
        // PaymentInfoGenerator: a payment processor's trading name, not an address to connect to.
        allowed.put("processor name", Pattern.compile("\\bCheckout\\.com\\b"));
        return allowed;
    }

    private static final Pattern URL_HOST =
            Pattern.compile("[A-Za-z][A-Za-z0-9+.-]*://(?:[^/?#@\\s]*@)?(\\[[^]]*]|[^/?#:\\s,\\]})\"'<>]+)");
    private static final Pattern EMAIL_DOMAIN =
            Pattern.compile("[A-Za-z0-9._%+'-]+@((?:[A-Za-z0-9-]+\\.)+[A-Za-z0-9-]+)");
    private static final Pattern IPV4 =
            Pattern.compile("(?<![\\d.])(\\d{1,3}(?:\\.\\d{1,3}){3})(?:/(\\d{1,2}))?(?![\\d.])");
    private static final Pattern IPV6 =
            Pattern.compile("(?<![\\p{Alnum}:.])([0-9A-Fa-f:]*:[0-9A-Fa-f:]*)(?:/(\\d{1,3}))?(?![\\p{Alnum}:.])");
    /**
     * A name with no scheme around it counts only under a common TLD: dotted text is everywhere else.
     * That leaves out {@code co}, because a username can be {@code nihat.co}.
     */
    private static final Pattern DOTTED_NAME = Pattern.compile(
            "(?<![A-Za-z0-9._%+/@-])((?:[A-Za-z0-9-]+\\.)+"
                    + "(?:com|net|org|io|dev|app|tech|online|site|xyz|pro|us|uk|au|de|fr|es|it|br|jp|cn"
                    + "|edu|gov|info|biz|ai|me|tv|ca|ru|nl|eu|in))(?![A-Za-z0-9-]|\\.[A-Za-z0-9])",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RESERVED_NAME = Pattern.compile(
            "(?:.+\\.)?example\\.(?:com|net|org)|(?:.+\\.)?(?:test|example|invalid|localhost)",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> TEST_NETS = Set.of("192.0.2", "198.51.100", "203.0.113");

    @Test
    @DisplayName("no generator names a real destination")
    void noGeneratorNamesARealDestination() {
        assertEquals(
                NetworkSafetyPolicy.TEST_SAFE_RESERVED,
                GeneratorConfig.defaults().getNetworkSafetyPolicy());
        assertEquals(Map.of(), sweep(defaultConfig(), finding -> true));
    }

    @Test
    @DisplayName("a realistic email policy does not let email domains into URLs")
    void aRealisticEmailPolicyDoesNotLetEmailDomainsIntoUrls() {
        Map<String, Set<String>> violations = sweep(
                defaultConfig().emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED),
                finding -> finding.kind() != Kind.EMAIL_DOMAIN);
        violations.keySet().removeIf(surface -> surface.endsWith("generateFreeEmailDomain"));

        assertEquals(Map.of(), violations);
    }

    @Test
    @DisplayName("the realistic network policy fails the sweep on every producer")
    void theRealisticNetworkPolicyFailsTheSweepOnEveryProducer() {
        Set<String> surfaces = new TreeSet<>();
        sweep(realisticConfig(), finding -> true)
                .keySet()
                .forEach(surface -> surfaces.add(surface.replaceFirst("^[^/]+/\\d+/", "")));

        assertTrue(
                surfaces.containsAll(List.of(
                        "ofIPv4.generate",
                        "ofIPv4.generateCidr",
                        "ofIPv4.generatePublic",
                        "ofIPv4.generatePrivate",
                        "ofIPv6.generate",
                        "ofIPv6.generateCidr",
                        "ofIP.generate",
                        "ofDomain.generate",
                        "ofHostname.generate",
                        "ofUrl.generate",
                        "ofUrl.generateWithPathAndQuery",
                        "ofUri.generate",
                        "ofUriObject.generate",
                        "ofUrlObject.generate",
                        "ofCompanyUrl.generate",
                        "ofCompanyUrl.generateFromCompanyName",
                        "ofAvatarUrl.generate",
                        "ofSocialProfile.generate",
                        "ofProfile.generate",
                        "network.ipv4.generate",
                        "network.domain.generate",
                        "network.url.generate",
                        "person.avatarUrl.generate",
                        "person.companyUrl.generate",
                        "person.socialProfile.generate",
                        "hub:internet.url.generate",
                        "hub:internet.domain.generate",
                        "hub:internet.hostname.generate",
                        "hub:company.url.generate",
                        "schema.xuri",
                        "schema.xhostname",
                        "schema.xipv4",
                        "schema.xipv6",
                        "schema.website",
                        "object:Site",
                        "object:CompanyNameUrl",
                        "object:DomainUrl")),
                surfaces.toString());
    }

    @Test
    @DisplayName("the scanner finds every kind of real destination")
    void theScannerFindsEveryKindOfRealDestination() {
        assertEquals(List.of("real.com"), values("https://real.com/x"));
        assertEquals(List.of("real.com"), values("ftp://user@real.com:21/x?q=1"));
        assertEquals(List.of("digital.xxx"), values("[ws://digital.xxx, ok]"));
        assertEquals(List.of("218.13.174.12"), values("http://218.13.174.12:8080/"));
        assertEquals(List.of("[2001:4860::8888]"), values("https://[2001:4860::8888]:443/"));
        assertEquals(List.of("gmail.com"), values("a.b@gmail.com"));
        assertEquals(List.of("8.8.8.8"), values("dns=8.8.8.8, ttl=1"));
        assertEquals(List.of("10.0.0.1"), values("ip 10.0.0.1"));
        assertEquals(List.of("192.0.2.1/8"), values("192.0.2.1/8"));
        assertEquals(List.of("2001:4860::8888"), values("2001:4860::8888"));
        assertEquals(List.of("ba41:dfe:aee7:c45:4f08:f12b:46ee:b52c"), values("ba41:dfe:aee7:c45:4f08:f12b:46ee:b52c"));
        assertEquals(List.of("2001:db8::1/16"), values("2001:db8::1/16"));
        assertEquals(List.of("digital.us"), values("digital.us"));
        assertEquals(List.of("m.digital.us"), values("host=m.digital.us, port=1"));
        assertEquals(List.of("github.com"), values("see github.com/vsmith553"));
    }

    @Test
    @DisplayName("the scanner accepts reserved destinations and what only resembles one")
    void theScannerAcceptsReservedDestinationsAndWhatOnlyResemblesOne() {
        for (String text : List.of(
                "https://www.digital.test/files?id=525",
                "https://github.test/vsmith553",
                "digital.test",
                "m.digital.test",
                "jsmith@acme.test",
                "a@example.com",
                "https://www.example.org",
                "example.net",
                "192.0.2.1",
                "198.51.100.254/30",
                "203.0.113.0/24",
                "2001:db8:1:2:3:4:5:6",
                "2001:db8::/32",
                "https://[2001:db8::1]:443/",
                "http://192.0.2.1/",
                "Mozilla/5.0 Chrome/124.0.0.0 Safari/537.36",
                "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                "Checkout.com",
                "A1:B2:C3:D4:E5:F6",
                "12:34:56",
                "999.1.2.3",
                "v1.2.3",
                "kyle.jenkins",
                "notes-684.svg",
                "Foo::bar",
                "@vsmith553",
                "Ab@cd")) {
            assertEquals(List.of(), values(text), text);
        }
    }

    @Test
    @DisplayName("every allow-list entry is in use")
    void everyAllowListEntryIsInUse() {
        Set<String> used = new TreeSet<>();
        sweepTexts(defaultConfig(), (surface, text) -> {
            ALLOWED.forEach((reason, pattern) -> {
                if (pattern.matcher(text).find()) {
                    used.add(reason);
                }
            });
        });

        assertEquals(new TreeSet<>(ALLOWED.keySet()), used);
    }

    private static List<String> values(String text) {
        return destinationsIn(text).stream().map(Finding::value).toList();
    }

    private static GeneratorConfig.Builder realisticConfig() {
        return defaultConfig().networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    /** The default network policy, with the families that fail closed opened, so that their values are swept too. */
    private static GeneratorConfig.Builder defaultConfig() {
        return GeneratorConfig.builder()
                .clock(CLOCK)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .businessTaxIdentifierSafetyPolicy(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .securitiesIdentifierSafetyPolicy(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    private static Map<String, Set<String>> sweep(GeneratorConfig.Builder base, Predicate<Finding> counted) {
        Map<String, Set<String>> violations = new TreeMap<>();
        sweepTexts(base, (surface, text) -> {
            for (Finding finding : destinationsIn(text)) {
                if (counted.test(finding)) {
                    violations
                            .computeIfAbsent(surface, ignored -> new TreeSet<>())
                            .add(finding.value());
                }
            }
        });
        return violations;
    }

    private static void sweepTexts(GeneratorConfig.Builder base, BiConsumer<String, String> sink) {
        for (Locale locale : LOCALES) {
            for (long seed : SEEDS) {
                GeneratorConfig config = base.locale(locale).seed(seed).build();
                BiConsumer<String, String> scoped =
                        (surface, text) -> sink.accept(locale + "/" + seed + "/" + surface, text);
                sweepFactories(config, scoped);
                sweepProviders(config, scoped);
                sweepSchema(config, scoped);
                sweepObjects(config, scoped);
            }
        }
    }

    private static void sweepFactories(GeneratorConfig config, BiConsumer<String, String> sink) {
        for (Method factory : FACTORIES) {
            Object target;
            try {
                target = factory.invoke(null, config);
            } catch (InvocationTargetException | IllegalAccessException refused) {
                continue;
            }
            if (target instanceof Generator<?>) {
                sweepGenerator(factory.getName(), target, sink);
            } else if (target != null && target.getClass().getPackageName().endsWith(".namespace")) {
                sweepNamespace(factory.getName(), target, sink);
            }
        }
    }

    private static void sweepNamespace(String name, Object namespace, BiConsumer<String, String> sink) {
        for (Method method : namespace.getClass().getMethods()) {
            if (method.getParameterCount() == 0 && Generator.class.isAssignableFrom(method.getReturnType())) {
                try {
                    sweepGenerator(name + "." + method.getName(), method.invoke(namespace), sink);
                } catch (InvocationTargetException | IllegalAccessException refused) {
                    continue;
                }
            }
        }
    }

    private static void sweepProviders(GeneratorConfig config, BiConsumer<String, String> sink) {
        ProviderHub hub = Generators.ofProviderHub(config);
        for (String name : hub.providerNames()) {
            Object provider;
            try {
                provider = hub.get(name);
            } catch (RuntimeException refused) {
                continue;
            }
            sweepGenerator("hub:" + name, provider, sink);
        }
    }

    private static void sweepGenerator(String surface, Object generator, BiConsumer<String, String> sink) {
        List<Method> methods = Arrays.stream(generator.getClass().getMethods())
                .filter(method -> method.getName().startsWith("generate"))
                .filter(method -> method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isBridge())
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        for (Method method : methods) {
            method.setAccessible(true);
            for (int draw = 0; draw < DRAWS; draw++) {
                try {
                    sink.accept(surface + "." + method.getName(), text(method.invoke(generator)));
                } catch (InvocationTargetException | IllegalAccessException refused) {
                    break;
                }
            }
        }
    }

    private static void sweepSchema(GeneratorConfig config, BiConsumer<String, String> sink) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String format : List.of("uri", "url", "hostname", "ipv4", "ipv6", "email", "idn-email")) {
            properties.put("x" + format.replace("-", ""), Map.of("type", "string", "format", format));
        }
        for (String name :
                List.of("url", "website", "domain", "hostname", "ip", "companyUrl", "company_url", "email")) {
            properties.put(name, Map.of("type", "string"));
        }
        Map<String, Object> schema = Map.of("type", "object", "properties", properties);
        for (Map<String, Object> record :
                SchemaParser.fromJsonSchema(schema, config).generateBatch(DRAWS)) {
            record.forEach((name, value) -> sink.accept("schema." + name, text(value)));
        }
    }

    private static void sweepObjects(GeneratorConfig config, BiConsumer<String, String> sink) {
        for (Class<?> type : List.of(
                Site.class,
                CompanyNameUrl.class,
                CompanyNameDomain.class,
                CompanyNameEmailUrl.class,
                DomainUrl.class,
                UrlEmail.class,
                CompanyUrlEmail.class,
                Containers.class,
                Annotated.class,
                MutableSite.class)) {
            Generator<?> generator = Generators.ofObject(type, config);
            for (int draw = 0; draw < DRAWS; draw++) {
                sink.accept("object:" + type.getSimpleName(), text(generator.generate()));
            }
        }
    }

    private static String text(@Nullable Object value) {
        if (value instanceof Object[] array) {
            return Arrays.deepToString(array);
        }
        if (value != null && value.getClass().isArray()) {
            return Arrays.deepToString(new Object[] {value});
        }
        return String.valueOf(value);
    }

    private static List<Finding> destinationsIn(String text) {
        String remaining = text;
        for (Pattern allowed : ALLOWED.values()) {
            remaining = allowed.matcher(remaining).replaceAll(" ");
        }
        List<Finding> findings = new ArrayList<>();
        remaining = scan(URL_HOST, remaining, match -> {
            String host = match.group(1);
            if (!isReservedHost(host)) {
                findings.add(new Finding(Kind.URL_HOST, host));
            }
        });
        remaining = scan(EMAIL_DOMAIN, remaining, match -> {
            if (!RESERVED_NAME.matcher(match.group(1)).matches()) {
                findings.add(new Finding(Kind.EMAIL_DOMAIN, match.group(1)));
            }
        });
        remaining = scan(IPV4, remaining, match -> {
            if (isIpv4(match.group(1)) && !isTestNet(match.group(1), match.group(2))) {
                findings.add(new Finding(Kind.IP, match.group()));
            }
        });
        remaining = scan(IPV6, remaining, match -> {
            if (isIpv6(match.group(1)) && !isDocumentationPrefix(match.group(1), match.group(2))) {
                findings.add(new Finding(Kind.IP, match.group()));
            }
        });
        scan(DOTTED_NAME, remaining, match -> {
            if (!RESERVED_NAME.matcher(match.group(1)).matches()) {
                findings.add(new Finding(Kind.NAME, match.group(1)));
            }
        });
        return findings;
    }

    /** Reports each match, and returns the text without them, so that a later pattern does not see them again. */
    private static String scan(Pattern pattern, String text, Consumer<Matcher> report) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            report.accept(matcher);
        }
        return matcher.replaceAll(" ");
    }

    private static boolean isReservedHost(String host) {
        if (host.startsWith("[")) {
            String address = host.substring(1, host.length() - 1);
            return isIpv6(address) && isDocumentationPrefix(address, null);
        }
        return isIpv4(host)
                ? isTestNet(host, null)
                : RESERVED_NAME.matcher(host).matches();
    }

    private static boolean isIpv4(String text) {
        return InetAddressValidator.getInstance().isValidInet4Address(text);
    }

    private static boolean isIpv6(String text) {
        return InetAddressValidator.getInstance().isValidInet6Address(text);
    }

    /** Whether an address, with its prefix if it has one, stays inside an RFC 5737 block. */
    private static boolean isTestNet(String address, @Nullable String prefix) {
        return (prefix == null || Integer.parseInt(prefix) >= 24)
                && TEST_NETS.contains(address.substring(0, address.lastIndexOf('.')));
    }

    /** Whether an address, with its prefix if it has one, stays inside the RFC 3849 prefix. */
    private static boolean isDocumentationPrefix(String address, @Nullable String prefix) {
        try {
            byte[] bytes = InetAddress.getByName(address).getAddress();
            return (prefix == null || Integer.parseInt(prefix) >= 32)
                    && bytes[0] == 0x20
                    && bytes[1] == 0x01
                    && bytes[2] == 0x0d
                    && bytes[3] == (byte) 0xb8;
        } catch (UnknownHostException notAnAddress) {
            throw new IllegalStateException(notAnAddress);
        }
    }

    private enum Kind {
        URL_HOST,
        EMAIL_DOMAIN,
        IP,
        NAME
    }

    private record Finding(Kind kind, String value) {}

    record Site(
            String url,
            String website,
            String homepage,
            String link,
            String domain,
            String hostname,
            String companyUrl,
            String companyWebsite,
            String businessWebsite,
            String companyName,
            String email,
            String companyEmail,
            String firstName,
            String lastName,
            URI uri,
            URL urlObject,
            String ipAddress,
            String ipv4,
            String ipv6,
            String macAddress,
            String avatarUrl,
            String host,
            String endpoint) {}

    record CompanyNameUrl(String companyName, String companyUrl) {}

    record CompanyNameDomain(String companyName, String domain) {}

    record CompanyNameEmailUrl(String companyName, String companyEmail, String companyUrl) {}

    record DomainUrl(String domain, String url) {}

    record UrlEmail(String url, String email, String firstName, String lastName) {}

    record CompanyUrlEmail(String companyUrl, String companyEmail) {}

    record Containers(List<URI> uris, Set<URI> uriSet, Map<String, URL> urlsByName, Optional<URI> optionalUri) {}

    record Annotated(
            @Fake("url") String contact,
            @Fake("domain") String mailHost,
            @Fake("hostname") String server) {}

    static class MutableSite {

        String url;
        String domain;
        String companyName;
        String companyUrl;
        URI uri;
        URL urlObject;

        @Override
        public String toString() {
            return url + " " + domain + " " + companyName + " " + companyUrl + " " + uri + " " + urlObject;
        }
    }
}
