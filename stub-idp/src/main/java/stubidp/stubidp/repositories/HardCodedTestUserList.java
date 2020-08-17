package stubidp.stubidp.repositories;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static stubidp.stubidp.domain.DatabaseEidasUserBuilder.aDatabaseEidasUser;
import static stubidp.stubidp.domain.DatabaseIdpUserBuilder.aDatabaseIdpUser;

final class HardCodedTestUserList {

    private HardCodedTestUserList() {}

    static List<DatabaseIdpUser> getHardCodedTestUsers(String idpFriendlyId) {
        return List.of(
                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId)
                        .withPassword("bar")
                        .withFirstname("Jack")
                        .withMiddlename("Cornelius")
                        .withSurname("Bauer")
                        .withGender(Gender.MALE)
                        .withDateOfBirth("1984-02-29")
                        .withAddresses(List.of(new AddressFactory().createNoDates(Collections.singletonList("1 Two St"), "1A 2BC", null, null, true),
                                new AddressFactory().create(Collections.singletonList("221b Baker St."), "W4 1SH", null, null, dateToInstant("2007-09-27"), dateToInstant("2007-09-28"), true),
                                new AddressFactory().create(Collections.singletonList("1 Goose Lane"), "M1 2FG", null, null, dateToInstant("2006-09-29"), dateToInstant("2006-09-08"), false)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-other")
                        .withPassword("bar")
                        .withFirstname("Martin")
                        .withMiddlename("Seamus")
                        .withSurname("McFly")
                        .withGender(Gender.FEMALE)
                        .withDateOfBirth("1968-06-12")
                        .withAddress(new AddressFactory().createNoDates(Collections.singletonList("1 Two St"), "1A 2BC", null, null, true))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-new")
                        .withPassword("bar")
                        .withFirstname("Jack")
                        .withSurname("Griffin")
                        .withGender(Gender.NOT_SPECIFIED)
                        .withDateOfBirth("1983-06-21")
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("Lion's Head Inn"), "1A 2BC", null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), null, true),
                                new AddressFactory().create(Collections.singletonList("Ye Olde Inn"), "1A 2BB", null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(3).toInstant(), Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), false)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-c3")
                        .withPassword("bar")
                        .withFirstname("J")
                        .withSurnames(List.of(createCurrentMdsValue("Moriarti", true),
                                new SimpleMdsValue<>("Barnes", dateToInstant("2006-09-29"), dateToInstant("2006-09-08"), true)))
                        .withGender(Gender.NOT_SPECIFIED)
                        .withDateOfBirth("1822-11-27")
                        .withAddress(new AddressFactory().createNoDates(Collections.singletonList("10 Two St"), "1A 2BC", null, null, true))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-ec3")
                        .withPassword("bar")
                        .withFirstname("Martin")
                        .withSurname("Riggs")
                        .withDateOfBirth("1970-04-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-complete")
                        .withPassword("bar")
                        .withFirstnames(List.of(createCurrentMdsValue("Jack", true),
                                createOldMdsValue("Spud", true)))
                        .withMiddlenames(List.of(createCurrentMdsValue("Cornelius", true),
                                createOldMdsValue("Aurelius", true)))
                        .withSurnames(List.of(createCurrentMdsValue("Bauer", true),
                                createOldMdsValue("Superman", true)))
                        .withGender(Gender.MALE)
                        .withDatesOfBirth(List.of(createCurrentMdsValue(dateToInstant("1984-02-29"), true),
                                createOldMdsValue(dateToInstant("1984-03-01"), true)))
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                                new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-loa1")
                        .withPassword("bar")
                        .withFirstname("Jessica", false)
                        .withMiddlename("", false)
                        .withSurname("Rabbit", false)
                        .withGender(Gender.FEMALE, false)
                        .withDateOfBirth("1960-03-23", false)
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), null, false),
                                new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), false)))
                        .withAuthnContext(AuthnContext.LEVEL_1)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-loa2")
                        .withPassword("bar")
                        .withFirstname("Roger")
                        .withMiddlename("")
                        .withSurname("Rabbit")
                        .withGender(Gender.MALE)
                        .withDateOfBirth("1958-04-09")
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                                new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-loa3")
                        .withPassword("bar")
                        .withFirstname("Apollo")
                        .withMiddlename("")
                        .withSurname("Eagle")
                        .withGender(Gender.FEMALE)
                        .withDateOfBirth("1969-07-20")
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("1 Four St"), "1A 2BD", "Something", "dummy uprn", Instant.now(), null, true),
                                new AddressFactory().create(Collections.singletonList("2 Five St"), "1B 2RD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)))
                        .withAuthnContext(AuthnContext.LEVEL_3)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-loax")
                        .withPassword("bar")
                        .withFirstname("Bugs")
                        .withMiddlename("")
                        .withSurname("Nummy")
                        .withGender(Gender.MALE)
                        .withDateOfBirth("1958-04-09")
                        .withAddresses(List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                                new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)))
                        .withAuthnContext(AuthnContext.LEVEL_X)
                        .build(),

                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-emoji")
                        .withPassword("bar")
                        .withFirstname("😀")
                        .withMiddlename("😎")
                        .withSurname("🙃")
                        .withGender(Gender.FEMALE)
                        .withDateOfBirth("1968-06-12")
                        .withAddresses(Collections.singletonList(new AddressFactory().createNoDates(List.of("🏠"), "🏘", null, null, true)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                // this user matches one user in the example local matching service
                // https://github.com/alphagov/verify-local-matching-service-example/blob/b135523be4c156b5f6e4fc0b3b3f94bcfbef9f75/src/main/resources/db/migration/V2__Populate_With_Test_Data.sql#L31
                aDatabaseIdpUser()
                        .withUsername(idpFriendlyId + "-elms")
                        .withPassword("bar")
                        .withFirstname("Joe")
                        .withSurname("Bloggs")
                        .withGender(Gender.NOT_SPECIFIED)
                        .withDateOfBirth("1970-01-01")
                        .withAddresses(List.of(new AddressFactory().create(List.of("The White Chapel Building, 10 Whitechapel High St", "London", "United Kingdom"), "E1 8DX",
                                null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), null, true)))
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build()
        );
    }

    static List<DatabaseEidasUser> getHardCodedCountryTestUsers(String idpFriendlyId) {
        return List.of(aDatabaseEidasUser()
                        .withUsername(idpFriendlyId)
                        .withPassword("bar")
                        .withFirstname("Jack")
                        .withSurname("Bauer")
                        .withDateOfBirth("1984-02-29")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-other")
                        .withPassword("bar")
                        .withFirstname("Martin")
                        .withSurname("McFly")
                        .withDateOfBirth("1968-06-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                // These names contain characters from ISO/IEC 8859-15 which we regard as Latin.
                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-accents")
                        .withPassword("bar")
                        .withFirstname("Šarlota")
                        .withSurname("Snježana")
                        .withDateOfBirth("1978-06-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-nonlatin")
                        .withPassword("bar")
                        .withFirstname("Georgios")
                        .withNonLatinFirstname("Γεώργιος")
                        .withSurname("Panathinaikos")
                        .withNonLatinSurname("Παναθηναϊκός")
                        .withDateOfBirth("1967-06-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-new")
                        .withPassword("bar")
                        .withFirstname("Jack")
                        .withSurname("Griffin")
                        .withDateOfBirth("1983-06-21")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-c3")
                        .withPassword("bar")
                        .withFirstname("J")
                        .withSurname("Surname")
                        .withDateOfBirth("1822-11-27")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-ec3")
                        .withPassword("bar")
                        .withFirstname("Martin")
                        .withSurname("Riggs")
                        .withDateOfBirth("1970-04-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-complete")
                        .withPassword("bar")
                        .withFirstname("Jack")
                        .withSurname("Bauer")
                        .withDateOfBirth("1984-02-29")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-loa1")
                        .withPassword("bar")
                        .withFirstname("Jessica", false)
                        .withSurname("Rabbit", false)
                        .withDateOfBirth("1960-03-23", false)
                        .withAuthnContext(AuthnContext.LEVEL_1)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-loa2")
                        .withPassword("bar")
                        .withFirstname("Roger")
                        .withSurname("Rabbit")
                        .withDateOfBirth("1958-04-09")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-loa3")
                        .withPassword("bar")
                        .withFirstname("Apollo")
                        .withSurname("Eagle")
                        .withDateOfBirth("1969-07-20")
                        .withAuthnContext(AuthnContext.LEVEL_3)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-loax")
                        .withPassword("bar")
                        .withFirstname("Bugs")
                        .withSurname("Nummy")
                        .withDateOfBirth("1958-04-09")
                        .withAuthnContext(AuthnContext.LEVEL_X)
                        .build(),

                aDatabaseEidasUser()
                        .withUsername(idpFriendlyId + "-emoji")
                        .withPassword("bar")
                        .withFirstname("😀")
                        .withNonLatinFirstname("GRINNING FACE")
                        .withSurname("🙃")
                        .withNonLatinSurname("UPSIDE-DOWN FACE")
                        .withDateOfBirth("1968-06-12")
                        .withAuthnContext(AuthnContext.LEVEL_2)
                        .build()
        );
    }

    private static Instant dateToInstant(String date) {
        return BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(date);
    }

    private static <T> SimpleMdsValue<T> createCurrentMdsValue(T value, boolean verified) {
        return new SimpleMdsValue<>(value, Instant.now().atZone(ZoneId.of("UTC")).minusDays(1).toInstant(), null, verified);
    }

    private static <T> SimpleMdsValue<T> createOldMdsValue(T value, boolean verified) {
        return new SimpleMdsValue<>(value, Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant(), Instant.now().atZone(ZoneId.of("UTC")).minusDays(1).toInstant(), verified);
    }
}
