/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import io.github.frikit.krandom.generator.user.AvatarUrlGenerator;
import io.github.frikit.krandom.generator.user.CompanyInfo;
import io.github.frikit.krandom.generator.user.CompanyInfoGenerator;
import io.github.frikit.krandom.generator.user.CompanyUrlGenerator;
import io.github.frikit.krandom.generator.user.ProfileGenerator;
import io.github.frikit.krandom.generator.user.SocialProfile;
import io.github.frikit.krandom.generator.user.SocialProfileGenerator;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@DisplayName("Network safety policy")
class NetworkSafetyPolicyTest {

    private static final int DRAWS = 500;
    private static final Pattern TEST_NET_ADDRESS =
            Pattern.compile("(?:192\\.0\\.2|198\\.51\\.100|203\\.0\\.113)\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4])");
    private static final Pattern RESERVED_DOMAIN = Pattern.compile("[a-z]+\\.test");
    private static final Pattern RESERVED_HOSTNAME = Pattern.compile("[a-z]+\\.[a-z]+\\.test");
    private static final Pattern RESERVED_URL = Pattern.compile("(?:https?|ftp|wss?)://[a-z]+\\.test");
    private static final InetAddressValidator ADDRESSES = InetAddressValidator.getInstance();

    /**
     * Output under {@link NetworkSafetyPolicy#REALISTIC_UNCLASSIFIED} for seed 42 and {@link Locale#US},
     * unless named otherwise, recorded from the implementation before the policy existed (commit
     * ed844edd). One fresh generator per entry, four draws each.
     */
    private static final Map<String, List<String>> REALISTIC_GOLDEN = Map.ofEntries(
            Map.entry("ipv4", List.of("218.13.174.12", "82.241.70.181", "223.23.231.115", "140.97.70.176")),
            Map.entry("ipv4 public", List.of("161.13.174.12", "32.241.70.181", "204.23.231.115", "32.97.70.176")),
            Map.entry(
                    "ipv4 private", List.of("192.168.13.174", "192.168.79.241", "192.168.181.170", "192.168.231.115")),
            Map.entry(
                    "ipv4 cidr",
                    List.of("4.174.12.79/14", "40.181.170.23/12", "172.228.70.176/8", "122.200.255.235/18")),
            Map.entry(
                    "ipv6",
                    List.of(
                            "ba41:dfe:aee7:c45:4f08:f12b:46ee:b52c",
                            "aa61:1761:e743:7385:5e68:61b3:4697:b0bf",
                            "76b2:c318:c86c:ff88:eb59:26e6:6fbd:7096",
                            "bff9:ee3d:62f6:cc5c:2d68:268a:9827:569b")),
            Map.entry(
                    "ipv6 cidr",
                    List.of(
                            "dfe:aee7:c45:4f08:f12b:46ee:b52c:aa61/34",
                            "e743:7385:5e68:61b3:4697:b0bf:76b2:c318/55",
                            "ff88:eb59:26e6:6fbd:7096:bff9:ee3d:62f6/67",
                            "2d68:268a:9827:569b:35b3:402b:d372:5d7b/68")),
            Map.entry(
                    "ip",
                    List.of(
                            "218.13.174.12",
                            "ba41:dfe:aee7:c45:4f08:f12b:46ee:b52c",
                            "82.241.70.181",
                            "aa61:1761:e743:7385:5e68:61b3:4697:b0bf")),
            Map.entry(
                    "mac", List.of("BA:0D:AE:0C:4F:F1", "46:B5:AA:17:E7:73", "5E:61:46:B0:76:C3", "C8:FF:EB:26:6F:70")),
            Map.entry("domain", List.of("digital.us", "vertexsync.org", "code.us", "connecttech.online")),
            Map.entry("domain de", List.of("digital.de", "vertexsync.org", "code.de", "connecttech.online")),
            Map.entry("domain ar", List.of("digital.online", "syncnet.tech", "cloudflow.com", "makerconnect.tech")),
            Map.entry("domain tld", List.of("us", "com", "app", "us")),
            Map.entry("hostname", List.of("m.digital.us", "www.vertexsync.org", "m.code.us", "www.connecttech.online")),
            Map.entry(
                    "url",
                    List.of("http://digital.us", "ws://vertexsync.org", "ws://code.us", "wss://connecttech.online")),
            Map.entry(
                    "url path query",
                    List.of(
                            "http://digital.us/files?id=525&id=918&offset=93",
                            "ftp://vertexsync.org/list/contact/list?type=170&status=209&page=863",
                            "https://code.us/admin?format=458",
                            "ftp://connecttech.online/docs?order=85")),
            Map.entry(
                    "uri",
                    List.of(
                            "http://digital.us/files?id=525&id=918&offset=93",
                            "ftp://vertexsync.org/list/contact/list",
                            "ftp://code.us/docs?format=400&sort=226",
                            "ws://connecttech.online/v1/docs/profile")),
            Map.entry(
                    "company url",
                    List.of(
                            "http://www.digital.us",
                            "https://www.vertexsync.org",
                            "http://www.code.us",
                            "https://www.connecttech.online")),
            Map.entry(
                    "company url from name",
                    List.of(
                            "https://www.advancedindustries.us",
                            "https://www.unitedanalytics.com",
                            "https://www.globalnetworks.app",
                            "https://www.advancedassociates.us")),
            Map.entry(
                    "avatar",
                    List.of(
                            "https://ui-avatars.com/api/?name=vsmith553&size=256",
                            "https://api.dicebear.com/8.x/identicon/svg?seed=kking774",
                            "https://api.dicebear.com/8.x/identicon/svg?seed=mia_carter",
                            "https://ui-avatars.com/api/?name=samanthacook825&size=256")),
            Map.entry(
                    "social link",
                    List.of(
                            "https://github.com/vsmith553",
                            "https://linkedin.com/kking774",
                            "https://x.com/mia_carter",
                            "https://github.com/samanthacook825")),
            Map.entry(
                    "profile website",
                    List.of("http://digital.us", "ws://vertexsync.org", "ws://code.us", "wss://connecttech.online")));

    private static GeneratorConfig reserved(long seed) {
        return GeneratorConfig.builder()
                .seed(seed)
                .networkSafetyPolicy(NetworkSafetyPolicy.TEST_SAFE_RESERVED)
                .build();
    }

    private static GeneratorConfig realistic(Locale locale) {
        return GeneratorConfig.builder()
                .seed(42L)
                .locale(locale)
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();
    }

    private static <T> void assertEveryDraw(Supplier<T> draw, Function<T, String> text, Pattern shape) {
        for (int i = 0; i < DRAWS; i++) {
            String value = text.apply(draw.get());
            assertTrue(shape.matcher(value).matches(), value);
        }
    }

    private static void assertEveryDraw(Supplier<String> draw, Pattern shape) {
        assertEveryDraw(draw, Function.identity(), shape);
    }

    @Test
    @DisplayName("the realistic policy keeps the recorded output draw for draw")
    void theRealisticPolicyKeepsTheRecordedOutputDrawForDraw() {
        Map<String, Supplier<?>> draws = Map.ofEntries(
                Map.entry("ipv4", new IPv4Generator(realistic(Locale.US))::generate),
                Map.entry("ipv4 public", new IPv4Generator(realistic(Locale.US))::generatePublic),
                Map.entry("ipv4 private", new IPv4Generator(realistic(Locale.US))::generatePrivate),
                Map.entry("ipv4 cidr", new IPv4Generator(realistic(Locale.US))::generateCidr),
                Map.entry("ipv6", new IPv6Generator(realistic(Locale.US))::generate),
                Map.entry("ipv6 cidr", new IPv6Generator(realistic(Locale.US))::generateCidr),
                Map.entry("ip", new IPGenerator(realistic(Locale.US))::generate),
                Map.entry("mac", new MacAddressGenerator(realistic(Locale.US))::generate),
                Map.entry("domain", new DomainGenerator(realistic(Locale.US))::generate),
                Map.entry("domain de", new DomainGenerator(realistic(Locale.GERMANY))::generate),
                Map.entry("domain ar", new DomainGenerator(realistic(Locale.forLanguageTag("ar-SA")))::generate),
                Map.entry("domain tld", new DomainGenerator(realistic(Locale.US))::getTLD),
                Map.entry("hostname", new HostnameGenerator(realistic(Locale.US))::generate),
                Map.entry("url", new URLGenerator(realistic(Locale.US))::generate),
                Map.entry("url path query", new URLGenerator(realistic(Locale.US))::generateWithPathAndQuery),
                Map.entry("uri", new UriGenerator(realistic(Locale.US))::generate),
                Map.entry("company url", new CompanyUrlGenerator(realistic(Locale.US))::generate),
                Map.entry(
                        "company url from name",
                        new CompanyUrlGenerator(realistic(Locale.US))::generateFromCompanyName),
                Map.entry("avatar", new AvatarUrlGenerator(realistic(Locale.US))::generate),
                Map.entry("social link", socialLinks(new SocialProfileGenerator(realistic(Locale.US)))),
                Map.entry("profile website", profileWebsites(new ProfileGenerator(realistic(Locale.US)))));

        assertEquals(REALISTIC_GOLDEN.keySet(), draws.keySet());
        draws.forEach((name, draw) -> {
            List<String> actual = List.of(
                    draw.get().toString(),
                    draw.get().toString(),
                    draw.get().toString(),
                    draw.get().toString());
            assertEquals(REALISTIC_GOLDEN.get(name), actual, name);
        });
    }

    private static Supplier<String> socialLinks(SocialProfileGenerator generator) {
        return () -> generator.generate().profileUrl();
    }

    private static Supplier<String> profileWebsites(ProfileGenerator generator) {
        return () -> generator.generate().website();
    }

    @Test
    @DisplayName("IPv4 addresses are the 762 host addresses of the RFC 5737 blocks")
    void ipv4AddressesAreTheHostAddressesOfTheDocumentationBlocks() {
        IPv4Generator generator = new IPv4Generator(reserved(1L));
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 20_000; i++) {
            String address = generator.generate();
            assertTrue(TEST_NET_ADDRESS.matcher(address).matches(), address);
            seen.add(address);
        }

        assertEquals(762, seen.size());
    }

    @Test
    @DisplayName("public and private IPv4 methods fail closed, and work once realistic addresses are opted in")
    void publicAndPrivateIpv4MethodsFailClosed() {
        IPv4Generator generator = new IPv4Generator(reserved(1L));
        for (Executable call : List.<Executable>of(generator::generatePublic, generator::generatePrivate)) {
            IllegalStateException error = assertThrows(IllegalStateException.class, call);
            assertTrue(
                    error.getMessage().contains("networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)"),
                    error.getMessage());
        }

        IPv4Generator realistic = new IPv4Generator(realistic(Locale.US));
        assertTrue(IPv4Generator.isPrivate(realistic.generatePrivate()));
        assertFalse(IPv4Generator.isPrivate(realistic.generatePublic()));
    }

    @Test
    @DisplayName("IPv4 CIDRs stay entirely inside one RFC 5737 block")
    void ipv4CidrsStayInsideOneBlock() {
        IPv4Generator generator = new IPv4Generator(reserved(2L));
        Set<Integer> prefixes = new HashSet<>();
        for (int i = 0; i < DRAWS; i++) {
            String cidr = generator.generateCidr();
            String[] parts = cidr.split("/");
            int prefix = Integer.parseInt(parts[1]);
            prefixes.add(prefix);
            assertTrue(TEST_NET_ADDRESS.matcher(parts[0]).matches(), cidr);
            assertTrue(prefix >= 24 && prefix <= 30, cidr);
        }

        assertEquals(Set.of(24, 25, 26, 27, 28, 29, 30), prefixes);
    }

    @Test
    @DisplayName("IPv6 addresses and CIDRs stay inside RFC 3849's prefix, in the generator's usual notation")
    void ipv6StaysInsideTheDocumentationPrefix() {
        IPv6Generator generator = new IPv6Generator(reserved(3L));
        Pattern address = Pattern.compile("2001:db8(?::[0-9a-f]{1,4}){6}");
        assertEveryDraw(generator::generate, address);

        Set<Integer> prefixes = new HashSet<>();
        for (int i = 0; i < DRAWS * 10; i++) {
            String[] parts = generator.generateCidr().split("/");
            int prefix = Integer.parseInt(parts[1]);
            prefixes.add(prefix);
            assertTrue(address.matcher(parts[0]).matches(), parts[0]);
            assertTrue(ADDRESSES.isValidInet6Address(parts[0]), parts[0]);
            assertTrue(prefix >= 32 && prefix <= 128, parts[1]);
        }

        assertEquals(97, prefixes.size());
    }

    @Test
    @DisplayName("the IP generator keeps both families reserved")
    void theIpGeneratorKeepsBothFamiliesReserved() {
        IPGenerator generator = new IPGenerator(reserved(4L));
        Pattern either = Pattern.compile(TEST_NET_ADDRESS.pattern() + "|2001:db8(?::[0-9a-f]{1,4}){6}");

        assertEveryDraw(generator::generate, either);
        assertEveryDraw(generator::generateIPv4, TEST_NET_ADDRESS);
        assertEveryDraw(generator::generateIPv6, Pattern.compile("2001:db8(?::[0-9a-f]{1,4}){6}"));
    }

    @Test
    @DisplayName("generated names are under .test, whatever the locale, with the usual variety")
    void generatedNamesAreUnderTest() {
        for (Locale locale : List.of(Locale.US, Locale.GERMANY, Locale.forLanguageTag("ar-SA"))) {
            DomainGenerator domains = new DomainGenerator(GeneratorConfig.builder()
                    .seed(5L)
                    .locale(locale)
                    .networkSafetyPolicy(NetworkSafetyPolicy.TEST_SAFE_RESERVED)
                    .build());
            assertEveryDraw(domains::generate, RESERVED_DOMAIN);
            assertEveryDraw(domains::generateDomainName, RESERVED_DOMAIN);
        }

        DomainGenerator domains = new DomainGenerator(reserved(5L));
        Set<String> names = new HashSet<>();
        for (int i = 0; i < DRAWS; i++) {
            names.add(domains.generate());
        }
        assertTrue(names.size() > 200, "only " + names.size() + " names");
    }

    @Test
    @DisplayName("a domain's own vocabulary is unchanged: a TLD or a label alone is no destination")
    void aDomainsOwnVocabularyIsUnchanged() {
        DomainGenerator reserved = new DomainGenerator(reserved(6L));
        DomainGenerator realistic = new DomainGenerator(realistic(Locale.US));

        assertEquals("us", reserved.getLocaleTLD());
        assertFalse(reserved.getPopularTLD().isBlank());
        assertFalse(reserved.getTLD().equals("test"));
        assertEquals(realistic.getLocaleTLD(), reserved.getLocaleTLD());
        assertFalse(reserved.generateName().contains("."));
    }

    @Test
    @DisplayName("an explicit TLD or destination is used verbatim")
    void anExplicitTldOrDestinationIsUsedVerbatim() {
        assertTrue(new DomainGenerator(reserved(7L)).generate("com").endsWith(".com"));

        URLGenerator urls = new URLGenerator(reserved(7L));
        assertTrue(urls.generateWithDomain("corp.com").endsWith("://corp.com"));
        assertTrue(urls.generateWithOptions(new URLGenerator.URLOptions("https", "corp.com", "api", null, null, false))
                .equals("https://api.corp.com"));
    }

    @Test
    @DisplayName("a supplied subdomain, scheme, path or size does not choose a destination")
    void aSuppliedDetailDoesNotChooseADestination() {
        Pattern subdomainHost = Pattern.compile("api\\.[a-z]+\\.test");
        assertEveryDraw(() -> new HostnameGenerator(reserved(8L)).generate("api"), subdomainHost);

        URLGenerator urls = new URLGenerator(reserved(8L));
        assertEveryDraw(() -> urls.generate("https"), Pattern.compile("https://[a-z]+\\.test"));
        assertEveryDraw(
                () -> urls.generateWithDomainPrefix("api"),
                Pattern.compile("(?:https?|ftp|wss?)://api\\.[a-z]+\\.test"));
        assertEveryDraw(
                () -> urls.generateWithFixedPath("/v1/users"),
                Pattern.compile("(?:https?|ftp|wss?)://[a-z]+\\.test/v1/users"));
        assertEveryDraw(() -> urls.generateWithOptions(new URLGenerator.URLOptions()), RESERVED_URL);

        CompanyUrlGenerator companies = new CompanyUrlGenerator(reserved(8L));
        assertEquals("https://www.acmelabs.test", companies.generateFromCompanyName("Acme Labs"));
        assertEquals("https://www.company.test", companies.generateFromCompanyName("---"));

        AvatarUrlGenerator avatars = new AvatarUrlGenerator(reserved(8L));
        assertEveryDraw(() -> hostOf(avatars.generate("jsmith", 64)), Pattern.compile("[a-z.-]+\\.test"));
    }

    @Test
    @DisplayName("a supplied scheme or subdomain that would end the host is rejected, unless realistic")
    void aSuppliedSchemeOrSubdomainThatWouldEndTheHostIsRejected() {
        URLGenerator urls = new URLGenerator(reserved(8L));
        HostnameGenerator hosts = new HostnameGenerator(reserved(8L));
        for (Executable call : List.<Executable>of(
                () -> urls.generate("https://real.com/#"),
                () -> urls.generateWithDomainPrefix("real.com/"),
                () -> urls.generateWithDomainPrefix("real.com#"),
                () -> urls.generateWithOptions(
                        new URLGenerator.URLOptions("https://real.com/#", null, null, null, null, false)),
                () -> hosts.generate("real.com/"))) {
            assertThrows(IllegalArgumentException.class, call);
        }

        assertTrue(urls.generateWithDomainPrefix("api.v2").matches("[a-z]+://api\\.v2\\.[a-z]+\\.test"));
        assertEquals(
                "x+y://real.com/.corp.com",
                urls.generateWithOptions(
                        new URLGenerator.URLOptions("x+y", "corp.com", "real.com/", null, null, false)));
        assertTrue(new URLGenerator(realistic(Locale.US))
                .generateWithDomainPrefix("real.com/")
                .contains("://real.com/."));
        assertTrue(new HostnameGenerator(realistic(Locale.US))
                .generate("real.com/")
                .startsWith("real.com/."));
    }

    @Test
    @DisplayName("URLs keep their shape under reserved names")
    void urlsKeepTheirShape() {
        URLGenerator urls = new URLGenerator(reserved(9L));
        assertEveryDraw(urls::generate, RESERVED_URL);
        assertEveryDraw(urls::generateUrl, RESERVED_URL);
        assertEveryDraw(urls::generateWithPath, Pattern.compile(RESERVED_URL.pattern() + "(?:/[a-z0-9]+){1,3}"));
        assertEveryDraw(
                urls::generateWithPathAndQuery,
                Pattern.compile(RESERVED_URL.pattern() + "(?:/[a-z0-9]+){1,3}\\?[a-z]+=\\d+(?:&[a-z]+=\\d+){0,2}"));

        UriGenerator uris = new UriGenerator(reserved(9L));
        assertEveryDraw(() -> hostOf(uris.generate()), RESERVED_DOMAIN);
        assertEveryDraw(() -> hostOf(uris.generateWithPath()), RESERVED_DOMAIN);
        assertEveryDraw(() -> hostOf(uris.generateWithQuery()), RESERVED_DOMAIN);

        HostnameGenerator hosts = new HostnameGenerator(reserved(9L));
        assertEveryDraw(hosts::generate, RESERVED_HOSTNAME);

        Generator<URI> uriObjects = Generators.ofUriObject(reserved(9L));
        assertEveryDraw(uriObjects::generate, URI::getHost, RESERVED_DOMAIN);
        Generator<java.net.URL> urlObjects = Generators.ofUrlObject(reserved(9L));
        assertEveryDraw(urlObjects::generate, java.net.URL::getHost, RESERVED_DOMAIN);
    }

    @Test
    @DisplayName("company URLs, avatar URLs and social links keep their shape under reserved hosts")
    void companyAvatarAndSocialUrlsKeepTheirShape() {
        CompanyUrlGenerator companies = new CompanyUrlGenerator(reserved(10L));
        assertEveryDraw(companies::generate, Pattern.compile("https?://www\\.[a-z]+\\.test"));
        assertEveryDraw(companies::generateFromCompanyName, Pattern.compile("https://www\\.[a-z0-9]+\\.test"));

        Pattern avatar = Pattern.compile("https://(?:api\\.dicebear\\.test/8\\.x/identicon/svg\\?seed=[^&]+"
                + "|robohash\\.test/[^/]+\\.png\\?size=128x128"
                + "|ui-avatars\\.test/api/\\?name=[^&]+&size=128)");
        AvatarUrlGenerator avatars = new AvatarUrlGenerator(reserved(10L));
        Set<String> hosts = new HashSet<>();
        for (int i = 0; i < DRAWS; i++) {
            String url = avatars.generate(128);
            assertTrue(avatar.matcher(url).matches(), url);
            hosts.add(hostOf(url));
        }
        assertEquals(Set.of("api.dicebear.test", "robohash.test", "ui-avatars.test"), hosts);

        SocialProfileGenerator social = new SocialProfileGenerator(reserved(10L));
        for (int i = 0; i < DRAWS; i++) {
            SocialProfile profile = social.generate();
            String slug = profile.handle().replaceFirst("^@", "");
            assertEquals("https://" + profile.platform() + ".test/" + slug, profile.profileUrl());
        }
    }

    @Test
    @DisplayName("composites keep reserved websites, even beside a realistic email")
    void compositesKeepReservedWebsites() {
        assertEveryDraw(
                new ProfileGenerator(reserved(11L))::generate, profile -> hostOf(profile.website()), RESERVED_DOMAIN);
        assertEveryDraw(
                new CompanyInfoGenerator(reserved(11L))::generate,
                CompanyInfo::website,
                Pattern.compile("https://www\\.[a-z0-9]+\\.test"));

        CompanyInfoGenerator companies = new CompanyInfoGenerator(GeneratorConfig.builder()
                .seed(11L)
                .networkSafetyPolicy(NetworkSafetyPolicy.TEST_SAFE_RESERVED)
                .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                .build());
        boolean realisticEmail = false;
        for (int i = 0; i < DRAWS; i++) {
            CompanyInfo company = companies.generate();
            String emailDomain = company.email().substring(company.email().indexOf('@') + 1);
            String label = emailDomain.substring(0, emailDomain.lastIndexOf('.'));
            assertEquals("https://www." + label + ".test", company.website());
            realisticEmail |= !emailDomain.endsWith(".test");
        }
        assertTrue(realisticEmail);
    }

    @Test
    @DisplayName("MAC addresses are locally administered and unicast, in every format")
    void macAddressesAreLocallyAdministeredAndUnicast() {
        MacAddressGenerator generator = new MacAddressGenerator(reserved(12L));
        Set<Integer> firstOctets = new HashSet<>();
        for (int i = 0; i < DRAWS; i++) {
            for (String mac : List.of(generator.generate(), generator.generate('-'), generator.generateLowercase())) {
                int first = Integer.parseInt(mac.substring(0, 2), 16);
                assertEquals(0x02, first & 0x03, mac);
                firstOctets.add(first);
            }
        }

        assertEquals(64, firstOctets.size());
    }

    @Test
    @DisplayName("a unique IPv4 generator uses the reserved addresses, then fails rather than leave them")
    void aUniqueIpv4GeneratorFailsRatherThanLeaveTheReservedAddresses() {
        Generator<String> unique = Generators.unique(new IPv4Generator(reserved(13L)));
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 762; i++) {
            String address = unique.generate();
            assertTrue(TEST_NET_ADDRESS.matcher(address).matches(), address);
            seen.add(address);
        }

        assertEquals(762, seen.size());
        assertThrows(IllegalStateException.class, unique::generate);
    }

    @Test
    @DisplayName("few retries can fail before every reserved address is used, and never leave them")
    void fewRetriesCanFailBeforeEveryAddressIsUsed() {
        Generator<String> unique = Generators.unique(new IPv4Generator(reserved(14L)), 3);
        Set<String> seen = new HashSet<>();
        try {
            for (int i = 0; i < 762; i++) {
                String address = unique.generate();
                assertTrue(TEST_NET_ADDRESS.matcher(address).matches(), address);
                seen.add(address);
            }
            fail("expected to run out of retries before the 762nd address");
        } catch (IllegalStateException outOfRetries) {
            assertTrue(seen.size() < 762, "used " + seen.size());
        }
    }

    @Test
    @DisplayName("a requested unique object member keeps to the reserved addresses and fails when they run out")
    void aRequestedUniqueObjectMemberFailsWhenTheAddressesRunOut() {
        GeneratorConfig config = reserved(15L).toBuilder()
                .objectOverride(Device.class, "address", new IPv4Generator(reserved(15L)))
                .objectUniqueField("address")
                .objectUniquenessMaxAttempts(100_000)
                .build();
        Generator<Device> devices = Generators.ofObject(Device.class, config);

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 762; i++) {
            String address = devices.generate().address();
            assertTrue(TEST_NET_ADDRESS.matcher(address).matches(), address);
            seen.add(address);
        }

        assertEquals(762, seen.size());
        assertThrows(ObjectGenerationException.class, devices::generate);
    }

    @Test
    @DisplayName("provider expressions keep generated destinations reserved")
    void providerExpressionsKeepDestinationsReserved() {
        GeneratorConfig config = reserved(16L);

        assertEveryDraw(
                () -> Generators.ofDataFakerExpression("#{internet.domain_name}", config)
                        .generate(),
                RESERVED_DOMAIN);
        assertEveryDraw(
                () -> hostOf(Generators.ofDataFakerExpression("#{internet.url}", config)
                        .generate()),
                RESERVED_DOMAIN);
    }

    private static String hostOf(String url) {
        return URI.create(url).getHost();
    }

    record Device(String address) {}
}
