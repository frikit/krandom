/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.commerce.OrderInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ProductInfoGenerator;
import io.github.frikit.krandom.generator.commerce.ShipmentInfoGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.DurationGenerator;
import io.github.frikit.krandom.generator.datetime.InstantGenerator;
import io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.TimezoneGenerator;
import io.github.frikit.krandom.generator.datetime.ZonedDateTimeGenerator;
import io.github.frikit.krandom.generator.finance.AbaRoutingGenerator;
import io.github.frikit.krandom.generator.finance.BankAccountGenerator;
import io.github.frikit.krandom.generator.finance.BankCountryGenerator;
import io.github.frikit.krandom.generator.finance.BankNameGenerator;
import io.github.frikit.krandom.generator.finance.BankTypeGenerator;
import io.github.frikit.krandom.generator.finance.BbanGenerator;
import io.github.frikit.krandom.generator.finance.BicGenerator;
import io.github.frikit.krandom.generator.finance.CardExpirationGenerator;
import io.github.frikit.krandom.generator.finance.CreditCardGenerator;
import io.github.frikit.krandom.generator.finance.CryptoAddressGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.CusipGenerator;
import io.github.frikit.krandom.generator.finance.EinGenerator;
import io.github.frikit.krandom.generator.finance.IbanGenerator;
import io.github.frikit.krandom.generator.finance.IsinGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.EanGenerator;
import io.github.frikit.krandom.generator.identifier.HashGenerator;
import io.github.frikit.krandom.generator.identifier.IdentifierMaskGenerator;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.identifier.UpcGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CoordinatesGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.HttpMethodGenerator;
import io.github.frikit.krandom.generator.network.HttpStatusCodeGenerator;
import io.github.frikit.krandom.generator.network.IPGenerator;
import io.github.frikit.krandom.generator.network.IPv4Generator;
import io.github.frikit.krandom.generator.network.IPv6Generator;
import io.github.frikit.krandom.generator.network.MacAddressGenerator;
import io.github.frikit.krandom.generator.network.PortGenerator;
import io.github.frikit.krandom.generator.network.SlugGenerator;
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.network.UriGenerator;
import io.github.frikit.krandom.generator.network.UserAgentGenerator;
import io.github.frikit.krandom.generator.text.LoremIpsumGenerator;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.SyllableGenerator;
import io.github.frikit.krandom.generator.text.TemplateStringGenerator;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.AgeGenerator;
import io.github.frikit.krandom.generator.user.AgeType;
import io.github.frikit.krandom.generator.user.AvatarUrlGenerator;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.EducationalAttainmentGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobFieldGenerator;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.MaritalStatusGenerator;
import io.github.frikit.krandom.generator.user.MiddleNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PositionGenerator;
import io.github.frikit.krandom.generator.user.ProfessionGenerator;
import io.github.frikit.krandom.generator.user.SeniorityGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import io.github.frikit.krandom.generator.user.BirthdayGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FluentNamespaceTest {

    private final GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();

    // ── Person namespace ─────────────────────────────────────────────────────

    @Test
    @DisplayName("person namespace generates all values")
    void personNamespace() {
        PersonGenerators p = Generators.person();
        assertNotNull(p.fullName().generate());
        assertNotNull(p.firstName().generate());
        assertNotNull(p.lastName().generate());
        assertNotNull(p.middleName().generate());
        assertNotNull(p.email().generate());
        assertNotNull(p.username().generate());
        assertNotNull(p.password().generate());
        assertNotNull(p.age().generate());
        assertNotNull(p.age(AgeType.ADULT).generate());
        assertNotNull(p.birthday().generate());
        assertNotNull(p.gender().generate());
        assertNotNull(p.profession().generate());
        assertNotNull(p.nationalId(Locale.US).generate());
        assertNotNull(p.avatarUrl().generate());
        assertNotNull(p.socialHandle().generate());
        assertNotNull(p.socialProfile().generate());
        assertNotNull(p.personInfo().generate());
        assertNotNull(p.contactInfo().generate());
        assertNotNull(p.simpleProfile().generate());
        assertNotNull(p.profile().generate());
        assertNotNull(p.maritalStatus().generate());
        assertNotNull(p.educationalAttainment().generate());
        assertNotNull(p.companyName().generate());
        assertNotNull(p.companyEmail().generate());
        assertNotNull(p.companyInfo().generate());
        assertNotNull(p.companyUrl().generate());
        assertNotNull(p.companyBuzzword().generate());
        assertNotNull(p.companyCatchPhrase().generate());
        assertNotNull(p.jobInfo().generate());
        assertNotNull(p.jobType().generate());
        assertNotNull(p.jobField().generate());
        assertNotNull(p.seniority().generate());
        assertNotNull(p.position().generate());
        assertNotNull(p.industry().generate());
    }

    @Test
    @DisplayName("person namespace with locale")
    void personNamespaceWithLocale() {
        PersonGenerators p = Generators.person(config);
        assertNotNull(p.fullName(Locale.GERMANY).generate());
        assertNotNull(p.firstName(Locale.FRANCE).generate());
        assertNotNull(p.lastName(Locale.JAPAN).generate());
        assertNotNull(p.middleName(Locale.US).generate());
        assertNotNull(p.email(Locale.UK).generate());
        assertNotNull(p.username(Locale.CANADA).generate());
        assertNotNull(p.profession(Locale.ITALY).generate());
        assertNotNull(p.companyName(Locale.GERMANY).generate());
    }

    // ── Finance namespace ────────────────────────────────────────────────────

    @Test
    @DisplayName("finance namespace generates all values")
    void financeNamespace() {
        FinanceGenerators f = Generators.finance();
        assertNotNull(f.creditCard().generate());
        assertNotNull(f.creditCardInfo().generate());
        assertNotNull(f.cardExpiration().generate());
        assertNotNull(f.currency().generate());
        assertNotNull(f.currencyPair().generate());
        assertNotNull(f.money().generate());
        assertNotNull(f.bankAccount().generate());
        assertNotNull(f.bankName().generate());
        assertNotNull(f.bankType().generate());
        assertNotNull(f.bankInfo().generate());
        assertNotNull(f.bankCountry().generate());
        assertNotNull(f.bic().generate());
        assertNotNull(f.bban().generate());
        assertNotNull(f.iban().generate());
        assertNotNull(f.abaRouting().generate());
        assertNotNull(f.isin().generate());
        assertNotNull(f.cusip().generate());
        assertNotNull(f.ein().generate());
        assertNotNull(f.cryptoAddress().generate());
        assertNotNull(f.invoiceInfo().generate());
        assertNotNull(f.paymentInfo().generate());
    }

    @Test
    @DisplayName("finance namespace with locale")
    void financeNamespaceWithLocale() {
        assertNotNull(Generators.finance().money(Locale.JAPAN).generate());
        assertNotNull(Generators.finance(config).money().generate());
    }

    // ── Location namespace ───────────────────────────────────────────────────

    @Test
    @DisplayName("location namespace generates all values")
    void locationNamespace() {
        LocationGenerators l = Generators.location();
        assertNotNull(l.city().generate());
        assertNotNull(l.state().generate());
        assertNotNull(l.country().generate());
        assertNotNull(l.postalCode().generate());
        assertNotNull(l.phoneNumber().generate());
        assertNotNull(l.streetAddress().generate());
        assertNotNull(l.addressInfo().generate());
        assertNotNull(l.coordinates().generate());
    }

    @Test
    @DisplayName("location namespace with locale")
    void locationNamespaceWithLocale() {
        LocationGenerators l = Generators.location();
        assertNotNull(l.city(Locale.FRANCE).generate());
        assertNotNull(l.state(Locale.US).generate());
        assertNotNull(l.country(Locale.GERMANY).generate());
        assertNotNull(l.postalCode(Locale.UK).generate());
        assertNotNull(l.phoneNumber(Locale.JAPAN).generate());
        assertNotNull(l.streetAddress(Locale.ITALY).generate());
        assertNotNull(l.addressInfo(Locale.CANADA).generate());
        assertNotNull(Generators.location(config).city().generate());
    }

    // ── Network namespace ────────────────────────────────────────────────────

    @Test
    @DisplayName("network namespace generates all values")
    void networkNamespace() {
        NetworkGenerators n = Generators.network();
        assertNotNull(n.ipv4().generate());
        assertNotNull(n.ipv6().generate());
        assertNotNull(n.ip().generate());
        assertNotNull(n.macAddress().generate());
        assertNotNull(n.domain().generate());
        assertNotNull(n.hostname().generate());
        assertNotNull(n.url().generate());
        assertNotNull(n.uri().generate());
        assertNotNull(n.port().generate());
        assertNotNull(n.slug().generate());
        assertNotNull(n.userAgent().generate());
        assertNotNull(n.httpStatusCode().generate());
        assertNotNull(n.httpMethod().generate());
        assertNotNull(Generators.network(config).ipv4().generate());
    }

    // ── Text namespace ───────────────────────────────────────────────────────

    @Test
    @DisplayName("text namespace generates all values")
    void textNamespace() {
        TextGenerators t = Generators.text();
        assertNotNull(t.loremIpsum().generate());
        assertNotNull(t.loremIpsum(LoremIpsumGenerator.Mode.SENTENCE).generate());
        assertNotNull(t.word().generate());
        assertNotNull(t.syllable().generate());
        assertNotNull(t.sentence().generate());
        assertNotNull(t.paragraph().generate());
        assertNotNull(t.text().generate());
        assertNotNull(t.template("Hello {{name}}").generate());
        assertNotNull(t.template("Test {{x}}", 42L).generate());
        assertNotNull(Generators.text(config).word().generate());
    }

    // ── Commerce namespace ───────────────────────────────────────────────────

    @Test
    @DisplayName("commerce namespace generates all values")
    void commerceNamespace() {
        CommerceGenerators c = Generators.commerce();
        assertNotNull(c.commerce().generate());
        assertNotNull(c.product().generate());
        assertNotNull(c.order().generate());
        assertNotNull(c.shipment().generate());
    }

    @Test
    @DisplayName("commerce namespace with locale")
    void commerceNamespaceWithLocale() {
        CommerceGenerators c = Generators.commerce();
        assertNotNull(c.product(Locale.GERMANY).generate());
        assertNotNull(c.order(Locale.FRANCE).generate());
        assertNotNull(c.shipment(Locale.JAPAN).generate());
        assertNotNull(Generators.commerce(config).product().generate());
    }

    // ── Identifier namespace ─────────────────────────────────────────────────

    @Test
    @DisplayName("identifier namespace generates all values")
    void identifierNamespace() {
        IdentifierGenerators id = Generators.identifier();
        assertNotNull(id.uuid().generate());
        assertNotNull(id.hash().generate());
        assertNotNull(id.isbn().generate());
        assertNotNull(id.isbn(IsbnGenerator.IsbnType.ISBN_13).generate());
        assertNotNull(id.ean().generate());
        assertNotNull(id.upc().generate());
        assertNotNull(id.mask().generate());
        assertNotNull(Generators.identifier(config).uuid().generate());
    }

    // ── DateTime namespace ───────────────────────────────────────────────────

    @Test
    @DisplayName("datetime namespace generates all values")
    void datetimeNamespace() {
        DateTimeGenerators dt = Generators.datetime();
        assertNotNull(dt.localDate().generate());
        assertNotNull(dt.localDateTime().generate());
        assertNotNull(dt.instant().generate());
        assertNotNull(dt.zonedDateTime().generate());
        assertNotNull(dt.duration().generate());
        assertNotNull(dt.timezone().generate());
        assertNotNull(Generators.datetime(config).localDate().generate());
    }

    @Test
    @DisplayName("config namespaces propagate seeded config to config-aware generators")
    void configNamespacesPropagateSeededConfig() {
        GeneratorConfig seeded = GeneratorConfig.builder()
            .locale(Locale.GERMANY)
            .seed(987654321L)
            .build();

        DateTimeGenerators datetime = Generators.datetime(seeded);
        assertSameFirstValue(new DateGenerator(seeded), datetime.localDate());
        assertSameFirstValue(new LocalDateTimeGenerator(seeded), datetime.localDateTime());
        assertSameFirstValue(new InstantGenerator(seeded), datetime.instant());
        assertSameFirstValue(new ZonedDateTimeGenerator(seeded), datetime.zonedDateTime());
        assertSameFirstValue(new DurationGenerator(seeded), datetime.duration());
        assertSameFirstValue(new TimezoneGenerator(seeded), datetime.timezone());

        TextGenerators text = Generators.text(seeded);
        assertSameFirstValue(new LoremIpsumGenerator(seeded), text.loremIpsum());
        assertSameFirstValue(new LoremIpsumGenerator(LoremIpsumGenerator.Mode.PARAGRAPH, seeded),
                             text.loremIpsum(LoremIpsumGenerator.Mode.PARAGRAPH));
        assertSameFirstValue(new WordGenerator(seeded), text.word());
        assertSameFirstValue(new SyllableGenerator(seeded), text.syllable());
        assertSameFirstValue(new SentenceGenerator(seeded), text.sentence());
        assertSameFirstValue(new ParagraphGenerator(seeded), text.paragraph());
        assertSameFirstValue(new TextGenerator(seeded), text.text());
        assertSameFirstValue(new TemplateStringGenerator("??-####", seeded), text.template("??-####"));

        NetworkGenerators network = Generators.network(seeded);
        assertSameFirstValue(new IPv4Generator(seeded), network.ipv4());
        assertSameFirstValue(new IPv6Generator(seeded), network.ipv6());
        assertSameFirstValue(new IPGenerator(seeded), network.ip());
        assertSameFirstValue(new MacAddressGenerator(seeded), network.macAddress());
        assertSameFirstValue(new DomainGenerator(seeded), network.domain());
        assertSameFirstValue(new HostnameGenerator(seeded), network.hostname());
        assertSameFirstValue(new URLGenerator(seeded), network.url());
        assertSameFirstValue(new UriGenerator(seeded), network.uri());
        assertSameFirstValue(new PortGenerator(seeded), network.port());
        assertSameFirstValue(new SlugGenerator(seeded), network.slug());
        assertSameFirstValue(new UserAgentGenerator(seeded), network.userAgent());
        assertSameFirstValue(new HttpStatusCodeGenerator(seeded), network.httpStatusCode());
        assertSameFirstValue(new HttpMethodGenerator(seeded), network.httpMethod());

        IdentifierGenerators identifier = Generators.identifier(seeded);
        assertSameFirstValue(new UUIDGenerator(seeded), identifier.uuid());
        assertSameFirstValue(new HashGenerator(seeded), identifier.hash());
        assertSameFirstValue(new IsbnGenerator(seeded), identifier.isbn());
        assertSameFirstValue(new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, seeded),
                             identifier.isbn(IsbnGenerator.IsbnType.ISBN_10));
        assertSameFirstValue(new EanGenerator(seeded), identifier.ean());
        assertSameFirstValue(new UpcGenerator(seeded), identifier.upc());
        assertSameFirstValue(new IdentifierMaskGenerator(seeded), identifier.mask());

        assertSameFirstValue(new CommerceGenerator(seeded), Generators.commerce(seeded).commerce());
        assertSameFirstValue(new CoordinatesGenerator(seeded), Generators.location(seeded).coordinates());

        FinanceGenerators finance = Generators.finance(seeded);
        assertSameFirstValue(new CreditCardGenerator(seeded), finance.creditCard());
        assertSameFirstValue(new CardExpirationGenerator(seeded), finance.cardExpiration());
        assertSameFirstValue(new CurrencyGenerator(seeded), finance.currency());
        assertSameFirstValue(new BankAccountGenerator(seeded), finance.bankAccount());
        assertSameFirstValue(new BankNameGenerator(seeded), finance.bankName());
        assertSameFirstValue(new BankTypeGenerator(seeded), finance.bankType());
        assertSameFirstValue(new BankCountryGenerator(seeded), finance.bankCountry());
        assertSameFirstValue(new BicGenerator(seeded), finance.bic());
        assertSameFirstValue(new BbanGenerator(seeded), finance.bban());
        assertSameFirstValue(new IbanGenerator(seeded), finance.iban());
        assertSameFirstValue(new AbaRoutingGenerator(seeded), finance.abaRouting());
        assertSameFirstValue(new IsinGenerator(seeded), finance.isin());
        assertSameFirstValue(new CusipGenerator(seeded), finance.cusip());
        assertSameFirstValue(new EinGenerator(seeded), finance.ein());
        assertSameFirstValue(new CryptoAddressGenerator(seeded), finance.cryptoAddress());

        PersonGenerators person = Generators.person(seeded);
        assertSameFirstValue(new PasswordGenerator(seeded), person.password());
        assertSameFirstValue(new AvatarUrlGenerator(seeded), person.avatarUrl());
        assertSameFirstValue(new MaritalStatusGenerator(seeded), person.maritalStatus());
        assertSameFirstValue(new EducationalAttainmentGenerator(seeded), person.educationalAttainment());
        assertSameFirstValue(new CompanyBuzzwordGenerator(seeded), person.companyBuzzword());
        assertSameFirstValue(new CompanyCatchPhraseGenerator(seeded), person.companyCatchPhrase());
        assertSameFirstValue(new JobTypeGenerator(seeded), person.jobType());
        assertSameFirstValue(new JobFieldGenerator(seeded), person.jobField());
        assertSameFirstValue(new SeniorityGenerator(seeded), person.seniority());
        assertSameFirstValue(new PositionGenerator(seeded), person.position());
        assertSameFirstValue(new IndustryGenerator(seeded), person.industry());
    }

    @Test
    @DisplayName("locale namespace overloads preserve seeded config")
    void localeNamespaceOverloadsPreserveSeededConfig() {
        GeneratorConfig seeded = GeneratorConfig.builder()
            .locale(Locale.US)
            .seed(2468L)
            .build();
        GeneratorConfig german = seeded.toBuilder().locale(Locale.GERMANY).build();
        GeneratorConfig french = seeded.toBuilder().locale(Locale.FRANCE).build();
        GeneratorConfig uk = seeded.toBuilder().locale(Locale.UK).build();
        GeneratorConfig japan = seeded.toBuilder().locale(Locale.JAPAN).build();
        GeneratorConfig italy = seeded.toBuilder().locale(Locale.ITALY).build();
        GeneratorConfig canada = seeded.toBuilder().locale(Locale.CANADA).build();

        PersonGenerators person = Generators.person(seeded);
        assertSameFirstValue(new FullNameGenerator(german), person.fullName(Locale.GERMANY));
        assertSameFirstValue(new FirstNameGenerator(french), person.firstName(Locale.FRANCE));
        assertSameFirstValue(new LastNameGenerator(japan), person.lastName(Locale.JAPAN));
        assertSameFirstValue(new MiddleNameGenerator(german), person.middleName(Locale.GERMANY));
        assertSameFirstValue(new EmailGenerator(uk), person.email(Locale.UK));
        assertSameFirstValue(new UsernameGenerator(canada), person.username(Locale.CANADA));
        assertSameFirstValue(new ProfessionGenerator(italy), person.profession(Locale.ITALY));
        assertSameFirstValue(new NationalIdGenerator(german), person.nationalId(Locale.GERMANY));
        assertSameFirstValue(new CompanyNameGenerator(german), person.companyName(Locale.GERMANY));

        LocationGenerators location = Generators.location(seeded);
        assertSameFirstValue(new CityGenerator(french), location.city(Locale.FRANCE));
        assertSameFirstValue(new StateGenerator(seeded), location.state(Locale.US));
        assertSameFirstValue(new CountryGenerator(german), location.country(Locale.GERMANY));
        assertSameFirstValue(new PostalCodeGenerator(uk), location.postalCode(Locale.UK));
        assertSameFirstValue(new PhoneNumberGenerator(japan), location.phoneNumber(Locale.JAPAN));
        assertSameFirstValue(new StreetAddressGenerator(italy), location.streetAddress(Locale.ITALY));
        assertSameFirstValue(new AddressInfoGenerator(canada), location.addressInfo(Locale.CANADA));

        CommerceGenerators commerce = Generators.commerce(seeded);
        assertSameFirstValue(new ProductInfoGenerator(german), commerce.product(Locale.GERMANY));
        assertSameFirstValue(new OrderInfoGenerator(french), commerce.order(Locale.FRANCE));
        assertSameFirstValue(new ShipmentInfoGenerator(japan), commerce.shipment(Locale.JAPAN));

        assertSameFirstValue(new MoneyGenerator(japan), Generators.finance(seeded).money(Locale.JAPAN));
    }

    @Test
    @DisplayName("person namespace age and birthday preserve seeded config")
    void personNamespaceAgeAndBirthdayPreserveSeededConfig() {
        GeneratorConfig seeded = GeneratorConfig.builder()
            .locale(Locale.GERMANY)
            .seed(13579L)
            .build();
        PersonGenerators person = Generators.person(seeded);

        assertSameFirstValue(new AgeGenerator(seeded), person.age());
        assertSameFirstValue(new AgeGenerator(AgeType.ADULT, seeded), person.age(AgeType.ADULT));
        assertSameFirstValue(new BirthdayGenerator(seeded), person.birthday());
    }

    // ── Default constructors ─────────────────────────────────────────────────

    @Test
    @DisplayName("namespace classes have working default constructors")
    void defaultConstructors() {
        assertNotNull(new PersonGenerators().fullName().generate());
        assertNotNull(new FinanceGenerators().creditCard().generate());
        assertNotNull(new LocationGenerators().city().generate());
        assertNotNull(new NetworkGenerators().ipv4().generate());
        assertNotNull(new TextGenerators().word().generate());
        assertNotNull(new CommerceGenerators().product().generate());
        assertNotNull(new IdentifierGenerators().uuid().generate());
        assertNotNull(new DateTimeGenerators().localDate().generate());
    }

    private static <T> void assertSameFirstValue(Generator<T> expected, Generator<T> actual) {
        assertEquals(expected.generate(), actual.generate());
    }
}
