/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import io.github.frikit.krandom.generator.object.Fake;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.schema.SchemaParser;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Sweeps every route to a generated email address, so that a new one is covered without a test of its
 * own: in every supported locale, under each email-domain and network-safety policy, an address that
 * krandom generates must pass Hibernate Validator's {@code @Email}, checked through the Bean Validation
 * API as a consumer's bean would be. An address is a string whose generator method, record component,
 * map key, schema property or object field is named for mail, other than a domain or a provider. The
 * generator catalog is searched for such routes once, and each route is then drawn from in every
 * configuration.
 */
@DisplayName("Generated email addresses")
class GeneratedEmailValidityTest {

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
    private static final List<Long> SEEDS = List.of(1L, 7L, 42L, 2026L, -3L, 987_654_321L);
    private static final int DRAWS = 8;

    /** The routes that must be among those found, so that a renamed route cannot drop out unnoticed. */
    private static final Set<String> KNOWN_ROUTES = Set.of(
            "ofEmail.generate",
            "ofEmail.generateCompanyEmail",
            "ofEmail.generateFreeEmail",
            "ofEmail.generateSafeEmail",
            "ofEmail.generateUnique",
            "ofCompanyEmail.generate",
            "ofContactInfo.generate",
            "ofCompanyInfo.generate",
            "ofSimpleProfile.generate",
            "ofProfile.generate",
            "ofPersonInfo.generate",
            "ofInvoiceInfo.generate",
            "ofOrderInfo.generate",
            "ofPaymentInfo.generate",
            "ofShipmentInfo.generate",
            "session.email",
            "schema",
            "object:Person",
            "object:Constrained");

    @Test
    @DisplayName("pass Hibernate Validator's @Email in every locale and under every policy")
    void passHibernateValidatorsEmailCheckInEveryLocaleAndUnderEveryPolicy() {
        List<Route> routes = routes();
        Map<String, String> rejected = new TreeMap<>();
        Set<String> refused = new TreeSet<>();
        Set<String> drawn = new TreeSet<>();
        for (Locale locale : SupportedLocale.locales()) {
            for (EmailDomainPolicy emailPolicy : EmailDomainPolicy.values()) {
                for (NetworkSafetyPolicy networkPolicy : NetworkSafetyPolicy.values()) {
                    for (long seed : SEEDS) {
                        GeneratorConfig config = config(emailPolicy, networkPolicy)
                                .locale(locale)
                                .seed(seed)
                                .build();
                        String where = locale + "/" + emailPolicy + "/" + networkPolicy + "/" + seed;
                        for (Route route : routes) {
                            try {
                                draw(route, config, address -> {
                                    drawn.add(route.surface());
                                    if (!passesEmailCheck(address)) {
                                        rejected.putIfAbsent(route.surface(), where + ": " + address);
                                    }
                                });
                            } catch (Refused refusal) {
                                refused.add(route.surface() + " under " + emailPolicy + ": " + refusal.getMessage());
                            }
                        }
                    }
                }
            }
        }

        assertEquals(Map.of(), rejected);
        assertEquals(surfaces(routes), drawn);
        assertEquals(expectedRefusals(routes), refused);
    }

    @Test
    @DisplayName("are found on every known route")
    void areFoundOnEveryKnownRoute() {
        Set<String> found = surfaces(routes());

        assertTrue(found.containsAll(KNOWN_ROUTES), () -> "missing: " + difference(KNOWN_ROUTES, found));
    }

    @Test
    @DisplayName("are told apart from what only names mail")
    void areToldApartFromWhatOnlyNamesMail() {
        assertTrue(isAddressName("generateCompanyEmail"));
        assertTrue(isAddressName("mail"));
        assertFalse(isAddressName("generateFreeEmailDomain"));
        assertFalse(isAddressName("emailProvider"));
        assertFalse(isAddressName("username"));
        assertFalse(passesEmailCheck(""));
        assertFalse(passesEmailCheck("a..b@example.com"));
        assertTrue(passesEmailCheck("a.b@example.com"));
    }

    private static boolean passesEmailCheck(String address) {
        // @Email accepts the empty string, which is no address.
        return address.indexOf('@') > 0
                && VALIDATOR.validate(new Address(address)).isEmpty();
    }

    /** One route to addresses: what a configuration builds, and how to draw a value from it. */
    private record Route(String surface, Function<GeneratorConfig, Supplier<@Nullable Object>> source) {}

    private static void draw(Route route, GeneratorConfig config, Consumer<String> sink) {
        Supplier<@Nullable Object> values = route.source().apply(config);
        for (int draw = 0; draw < DRAWS; draw++) {
            addresses(route.surface(), values.get(), sink);
        }
    }

    /** Free-mail providers are real mailboxes, which the reserved policy rules out. */
    private static Set<String> expectedRefusals(List<Route> routes) {
        Set<String> expected = new TreeSet<>();
        for (String surface : surfaces(routes)) {
            if (surface.endsWith(".generateFreeEmail")) {
                expected.add(surface + " under " + EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS + ": "
                        + IllegalStateException.class.getSimpleName());
            }
        }
        return expected;
    }

    /** Reports each address in a value: the value itself, its record components, map entries and elements. */
    private static void addresses(String name, @Nullable Object value, Consumer<String> sink) {
        if (value instanceof String text) {
            if (isAddressName(name)) {
                sink.accept(text);
            }
        } else if (value instanceof Record record) {
            for (RecordComponent component : record.getClass().getRecordComponents()) {
                addresses(component.getName(), invoke(component.getAccessor(), record), sink);
            }
        } else if (value instanceof Map<?, ?> map) {
            map.forEach((key, entry) -> addresses(String.valueOf(key), entry, sink));
        } else if (value instanceof Iterable<?> items) {
            items.forEach(item -> addresses(name, item, sink));
        }
    }

    private static boolean isAddressName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("mail") && !lower.contains("domain") && !lower.contains("provider");
    }

    private static List<Route> routes() {
        Map<String, Route> routes = new TreeMap<>();
        for (EmailDomainPolicy emailPolicy : EmailDomainPolicy.values()) {
            for (NetworkSafetyPolicy networkPolicy : NetworkSafetyPolicy.values()) {
                GeneratorConfig probe = config(emailPolicy, networkPolicy)
                        .locale(Locale.US)
                        .seed(42L)
                        .build();
                for (Route candidate : catalogRoutes(probe)) {
                    if (!routes.containsKey(candidate.surface()) && yieldsAddresses(candidate, probe)) {
                        routes.put(candidate.surface(), candidate);
                    }
                }
            }
        }
        List<Route> all = new ArrayList<>(routes.values());
        all.add(new Route("session.email", config -> Krandom.of(config)::email));
        all.add(new Route("schema", config -> schemaRecords(config)));
        for (Class<?> type : List.of(
                Person.class,
                Contact.class,
                Employee.class,
                CompanySite.class,
                LongCompany.class,
                Web.class,
                Constrained.class)) {
            all.add(new Route("object:" + type.getSimpleName(), config -> objects(type, config)));
        }
        return all;
    }

    private static boolean yieldsAddresses(Route candidate, GeneratorConfig probe) {
        List<String> found = new ArrayList<>();
        try {
            addresses(candidate.surface(), candidate.source().apply(probe).get(), found::add);
        } catch (Refused refused) {
            return false;
        }
        return !found.isEmpty();
    }

    /**
     * Every generate method of every generator class that a factory, namespace or hub provider builds from
     * the configuration. A class reached on several of them is drawn from once, under its factory's name.
     */
    private static List<Route> catalogRoutes(GeneratorConfig probe) {
        List<Route> candidates = new ArrayList<>();
        Set<String> seen = new TreeSet<>();
        List<Method> factories = Arrays.stream(Generators.class.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method ->
                        method.getParameterCount() == 1 && method.getParameterTypes()[0] == GeneratorConfig.class)
                .sorted(Comparator.comparing(Method::getName))
                .toList();
        List<Method> namespaces = new ArrayList<>();
        for (Method factory : factories) {
            Object target = build(() -> invoke(factory, null, probe));
            if (target instanceof Generator<?>) {
                candidates.addAll(generateRoutes(
                        factory.getName(), target.getClass(), config -> invoke(factory, null, config), seen));
            } else if (target != null && target.getClass().getPackageName().endsWith(".namespace")) {
                namespaces.add(factory);
            }
        }
        for (Method factory : namespaces) {
            Object namespace = invoke(factory, null, probe);
            for (Method member : namespace.getClass().getMethods()) {
                if (member.getParameterCount() == 0 && Generator.class.isAssignableFrom(member.getReturnType())) {
                    Object generator = build(() -> invoke(member, namespace));
                    if (generator != null) {
                        candidates.addAll(generateRoutes(
                                factory.getName() + "." + member.getName(),
                                generator.getClass(),
                                config -> invoke(member, invoke(factory, null, config)),
                                seen));
                    }
                }
            }
        }
        ProviderHub hub = Generators.ofProviderHub(probe);
        for (String name : hub.providerNames()) {
            Object provider = build(() -> provider(hub, name));
            if (provider != null) {
                candidates.addAll(generateRoutes(
                        "hub:" + name,
                        provider.getClass(),
                        config -> provider(Generators.ofProviderHub(config), name),
                        seen));
            }
        }
        return candidates;
    }

    private static Object provider(ProviderHub hub, String name) {
        try {
            return hub.get(name);
        } catch (RuntimeException refused) {
            throw new Refused(refused);
        }
    }

    private static List<Route> generateRoutes(
            String surface, Class<?> type, Function<GeneratorConfig, @Nullable Object> generatorFor, Set<String> seen) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().startsWith("generate"))
                .filter(method -> method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isBridge())
                .sorted(Comparator.comparing(Method::getName))
                .filter(method -> seen.add(type.getName() + "#" + method.getName()))
                .map(method -> new Route(surface + "." + method.getName(), config -> {
                    Object generator = generatorFor.apply(config);
                    return () -> invoke(method, generator);
                }))
                .toList();
    }

    private static Supplier<@Nullable Object> schemaRecords(GeneratorConfig config) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("formatEmail", Map.of("type", "string", "format", "email"));
        properties.put("formatIdnEmail", Map.of("type", "string", "format", "idn-email"));
        for (String name : List.of("email", "emailAddress", "email_address", "companyEmail", "company_email")) {
            properties.put(name, Map.of("type", "string"));
        }
        return SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", properties), config)::generateRecord;
    }

    private static Supplier<@Nullable Object> objects(Class<?> type, GeneratorConfig config) {
        return Generators.ofObject(type, config)::generate;
    }

    private static @Nullable Object build(Supplier<@Nullable Object> builder) {
        try {
            return builder.get();
        } catch (Refused refused) {
            return null;
        }
    }

    private static @Nullable Object invoke(Method method, @Nullable Object target, Object... arguments) {
        try {
            method.setAccessible(true);
            return method.invoke(target, arguments);
        } catch (InvocationTargetException refused) {
            throw new Refused(refused.getCause());
        } catch (IllegalAccessException inaccessible) {
            throw new IllegalStateException(inaccessible);
        }
    }

    private static Set<String> surfaces(List<Route> routes) {
        Set<String> surfaces = new TreeSet<>();
        routes.forEach(route -> surfaces.add(route.surface()));
        return surfaces;
    }

    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }

    /** The default policies, with the families that fail closed opened, as in the catalog's network sweep. */
    private static GeneratorConfig.Builder config(EmailDomainPolicy emailPolicy, NetworkSafetyPolicy networkPolicy) {
        return GeneratorConfig.builder()
                .clock(CLOCK)
                .emailDomainPolicy(emailPolicy)
                .networkSafetyPolicy(networkPolicy)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .businessTaxIdentifierSafetyPolicy(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .securitiesIdentifierSafetyPolicy(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED);
    }

    /** A generator or factory that refused, carrying the refusal's type as its message. */
    private static final class Refused extends RuntimeException {

        Refused(Throwable cause) {
            super(cause.getClass().getSimpleName(), cause);
        }
    }

    /** A consumer's bean: the address is checked by the validator, not by matching it here. */
    static final class Address {

        @Email private final String value;

        Address(String value) {
            this.value = value;
        }
    }

    record Person(String firstName, String lastName, String email) {}

    record Contact(String fullName, String emailAddress) {}

    record Employee(String firstName, String lastName, String companyName, String email, String businessEmail) {}

    record CompanySite(String companyName, String companyUrl, String companyEmail) {}

    /** A company name longer than the 63 characters a domain label may hold, from which its email is derived. */
    record LongCompany(@Size(min = 64, max = 100) String companyName, String companyEmail) {}

    record Web(String url, String domain, String email, String corporateEmail) {}

    record Constrained(
            @Email String primaryMail,
            @Email @Size(min = 14, max = 18) String backupMail,
            @Fake("email") String mailbox) {}
}
