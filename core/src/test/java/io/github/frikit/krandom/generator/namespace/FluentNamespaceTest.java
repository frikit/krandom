/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.text.LoremIpsumGenerator;
import io.github.frikit.krandom.generator.user.AgeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

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
}
