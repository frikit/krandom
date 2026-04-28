/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.*;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;

import java.util.Locale;

/**
 * Fluent namespace for person-related generators.
 *
 * <p>Usage: {@code Generators.person().fullName().generate()}
 */
public final class PersonGenerators {

    private final GeneratorConfig config;

    public PersonGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public PersonGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public FullNameGenerator fullName() { return new FullNameGenerator(config); }
    public FullNameGenerator fullName(Locale locale) { return new FullNameGenerator(withLocale(locale)); }

    public FirstNameGenerator firstName() { return new FirstNameGenerator(config); }
    public FirstNameGenerator firstName(Locale locale) { return new FirstNameGenerator(withLocale(locale)); }

    public LastNameGenerator lastName() { return new LastNameGenerator(config); }
    public LastNameGenerator lastName(Locale locale) { return new LastNameGenerator(withLocale(locale)); }

    public MiddleNameGenerator middleName() { return new MiddleNameGenerator(config); }
    public MiddleNameGenerator middleName(Locale locale) { return new MiddleNameGenerator(withLocale(locale)); }

    public EmailGenerator email() { return new EmailGenerator(config); }
    public EmailGenerator email(Locale locale) { return new EmailGenerator(withLocale(locale)); }

    public UsernameGenerator username() { return new UsernameGenerator(config); }
    public UsernameGenerator username(Locale locale) { return new UsernameGenerator(withLocale(locale)); }

    public PasswordGenerator password() { return new PasswordGenerator(config); }

    public AgeGenerator age() { return new AgeGenerator(config); }
    public AgeGenerator age(AgeType type) { return new AgeGenerator(type, config); }

    public BirthdayGenerator birthday() { return new BirthdayGenerator(config); }

    public GenderGenerator gender() { return new GenderGenerator(config); }

    public ProfessionGenerator profession() { return new ProfessionGenerator(config); }
    public ProfessionGenerator profession(Locale locale) { return new ProfessionGenerator(withLocale(locale)); }

    public NationalIdGenerator nationalId(Locale locale) { return new NationalIdGenerator(withLocale(locale)); }

    public AvatarUrlGenerator avatarUrl() { return new AvatarUrlGenerator(config); }

    public SocialHandleGenerator socialHandle() { return new SocialHandleGenerator(config); }

    public SocialProfileGenerator socialProfile() { return new SocialProfileGenerator(config); }

    public PersonInfoGenerator personInfo() { return new PersonInfoGenerator(config); }

    public ContactInfoGenerator contactInfo() { return new ContactInfoGenerator(config); }

    public SimpleProfileGenerator simpleProfile() { return new SimpleProfileGenerator(config); }

    public ProfileGenerator profile() { return new ProfileGenerator(config); }

    public MaritalStatusGenerator maritalStatus() { return new MaritalStatusGenerator(config); }

    public EducationalAttainmentGenerator educationalAttainment() { return new EducationalAttainmentGenerator(config); }

    public CompanyNameGenerator companyName() { return new CompanyNameGenerator(config); }
    public CompanyNameGenerator companyName(Locale locale) { return new CompanyNameGenerator(withLocale(locale)); }

    public CompanyEmailGenerator companyEmail() { return new CompanyEmailGenerator(config); }

    public CompanyInfoGenerator companyInfo() { return new CompanyInfoGenerator(config); }

    public CompanyUrlGenerator companyUrl() { return new CompanyUrlGenerator(config); }

    public CompanyBuzzwordGenerator companyBuzzword() { return new CompanyBuzzwordGenerator(config); }

    public CompanyCatchPhraseGenerator companyCatchPhrase() { return new CompanyCatchPhraseGenerator(config); }

    public JobInfoGenerator jobInfo() { return new JobInfoGenerator(config); }

    public JobTypeGenerator jobType() { return new JobTypeGenerator(config); }

    public JobFieldGenerator jobField() { return new JobFieldGenerator(config); }

    public SeniorityGenerator seniority() { return new SeniorityGenerator(config); }

    public PositionGenerator position() { return new PositionGenerator(config); }

    public IndustryGenerator industry() { return new IndustryGenerator(config); }

    private GeneratorConfig withLocale(Locale locale) {
        return config.toBuilder().locale(locale).build();
    }
}
