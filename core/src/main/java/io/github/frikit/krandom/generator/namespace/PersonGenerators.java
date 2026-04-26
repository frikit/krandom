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
    public FullNameGenerator fullName(Locale locale) { return new FullNameGenerator(locale); }

    public FirstNameGenerator firstName() { return new FirstNameGenerator(config); }
    public FirstNameGenerator firstName(Locale locale) { return new FirstNameGenerator(locale); }

    public LastNameGenerator lastName() { return new LastNameGenerator(config); }
    public LastNameGenerator lastName(Locale locale) { return new LastNameGenerator(locale); }

    public MiddleNameGenerator middleName() { return new MiddleNameGenerator(config); }
    public MiddleNameGenerator middleName(Locale locale) { return new MiddleNameGenerator(locale); }

    public EmailGenerator email() { return new EmailGenerator(config); }
    public EmailGenerator email(Locale locale) { return new EmailGenerator(locale); }

    public UsernameGenerator username() { return new UsernameGenerator(config); }
    public UsernameGenerator username(Locale locale) { return new UsernameGenerator(locale); }

    public PasswordGenerator password() { return new PasswordGenerator(); }

    public AgeGenerator age() { return new AgeGenerator(); }
    public AgeGenerator age(AgeType type) { return new AgeGenerator(type); }

    public BirthdayGenerator birthday() { return new BirthdayGenerator(config.getLocale()); }

    public GenderGenerator gender() { return new GenderGenerator(config); }

    public ProfessionGenerator profession() { return new ProfessionGenerator(config); }
    public ProfessionGenerator profession(Locale locale) { return new ProfessionGenerator(locale); }

    public NationalIdGenerator nationalId(Locale locale) { return new NationalIdGenerator(locale); }

    public AvatarUrlGenerator avatarUrl() { return new AvatarUrlGenerator(); }

    public SocialHandleGenerator socialHandle() { return new SocialHandleGenerator(config); }

    public SocialProfileGenerator socialProfile() { return new SocialProfileGenerator(config); }

    public PersonInfoGenerator personInfo() { return new PersonInfoGenerator(config); }

    public ContactInfoGenerator contactInfo() { return new ContactInfoGenerator(config); }

    public SimpleProfileGenerator simpleProfile() { return new SimpleProfileGenerator(config); }

    public ProfileGenerator profile() { return new ProfileGenerator(config); }

    public MaritalStatusGenerator maritalStatus() { return new MaritalStatusGenerator(); }

    public EducationalAttainmentGenerator educationalAttainment() { return new EducationalAttainmentGenerator(); }

    public CompanyNameGenerator companyName() { return new CompanyNameGenerator(config); }
    public CompanyNameGenerator companyName(Locale locale) { return new CompanyNameGenerator(GeneratorConfig.builder().locale(locale).build()); }

    public CompanyEmailGenerator companyEmail() { return new CompanyEmailGenerator(config); }

    public CompanyInfoGenerator companyInfo() { return new CompanyInfoGenerator(config); }

    public CompanyUrlGenerator companyUrl() { return new CompanyUrlGenerator(config); }

    public CompanyBuzzwordGenerator companyBuzzword() { return new CompanyBuzzwordGenerator(); }

    public CompanyCatchPhraseGenerator companyCatchPhrase() { return new CompanyCatchPhraseGenerator(); }

    public JobInfoGenerator jobInfo() { return new JobInfoGenerator(config); }

    public JobTypeGenerator jobType() { return new JobTypeGenerator(); }

    public JobFieldGenerator jobField() { return new JobFieldGenerator(); }

    public SeniorityGenerator seniority() { return new SeniorityGenerator(); }

    public PositionGenerator position() { return new PositionGenerator(); }

    public IndustryGenerator industry() { return new IndustryGenerator(); }
}
