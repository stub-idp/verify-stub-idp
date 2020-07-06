package stubidp.stubidp.repositories;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;
import stubidp.saml.utils.core.domain.AddressFactory;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class HardCodedTestUserList {

    private HardCodedTestUserList() {
    }

    static List<DatabaseIdpUser> getHardCodedTestUsers(String idpFriendlyId) {

        List<DatabaseIdpUser> sacredUsers = new ArrayList<>();

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId,
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Jack")),
                Collections.singletonList(createMdsValue("Cornelius")),
                Collections.singletonList(createMdsValue("Bauer")),
                Optional.of(createMdsValue(Gender.MALE)),
                Collections.singletonList(createMdsValue(dateToInstant("1984-02-29"))),
                List.of(new AddressFactory().createNoDates(Collections.singletonList("1 Two St"), "1A 2BC", null, null, true),
                        new AddressFactory().create(Collections.singletonList("221b Baker St."), "W4 1SH", null, null, dateToInstant("2007-09-27"), dateToInstant("2007-09-28"), true),
                        new AddressFactory().create(Collections.singletonList("1 Goose Lane"), "M1 2FG", null, null, dateToInstant("2006-09-29"), dateToInstant("2006-09-08"), false)
                ),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-other",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Martin")),
                Collections.singletonList(createMdsValue("Seamus")),
                Collections.singletonList(createMdsValue("McFly")),
                Optional.of(createMdsValue(Gender.FEMALE)),
                Collections.singletonList(createMdsValue(dateToInstant("1968-06-12"))),
                Collections.singletonList(new AddressFactory().createNoDates(Collections.singletonList("1 Two St"), "1A 2BC", null, null, true)),
                AuthnContext.LEVEL_2));


        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-new",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Jack")),
                Collections.emptyList(),
                Collections.singletonList(createMdsValue("Griffin")),
                Optional.of(createMdsValue(Gender.NOT_SPECIFIED)),
                Collections.singletonList(createMdsValue(dateToInstant("1983-06-21"))),
                List.of(new AddressFactory().create(Collections.singletonList("Lion's Head Inn"), "1A 2BC", null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), null, true),
                        new AddressFactory().create(Collections.singletonList("Ye Olde Inn"), "1A 2BB", null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(3).toInstant(), Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), false)),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-c3",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("J")),
                Collections.emptyList(),   //No middle names that we could find. :)
                List.of(createMdsValue("Moriarti"), new MatchingDatasetValue<>("Barnes", dateToInstant("2006-09-29"), dateToInstant("2006-09-08"), true)),
                Optional.of(createMdsValue(Gender.NOT_SPECIFIED)),
                Collections.singletonList(createMdsValue(dateToInstant("1822-11-27"))),
                Collections.singletonList(new AddressFactory().createNoDates(Collections.singletonList("10 Two St"), "1A 2BC", null, null, true)),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-ec3",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Martin")),
                Collections.emptyList(),
                Collections.singletonList(createMdsValue("Riggs")),
                Optional.empty(),
                Collections.singletonList(createMdsValue(dateToInstant("1970-04-12"))),
                Collections.emptyList(),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-complete",
                UUID.randomUUID().toString(),
                "bar",
                List.of(new MatchingDatasetValue<>("Jack", Instant.now(), Instant.now(), true),
                        new MatchingDatasetValue<>("Spud", Instant.now(), Instant.now(), true)),
                List.of(new MatchingDatasetValue<>("Cornelius", Instant.now(), Instant.now(), true),
                        new MatchingDatasetValue<>("Aurelius", Instant.now(), Instant.now(), true)),
                List.of(new MatchingDatasetValue<>("Bauer", Instant.now(), Instant.now(), true),
                        new MatchingDatasetValue<>("Superman", Instant.now().atZone(ZoneId.of("UTC")).minusDays(5).toInstant(), Instant.now().atZone(ZoneId.of("UTC")).minusDays(3).toInstant(), true)),
                Optional.of(new MatchingDatasetValue<>(Gender.MALE, Instant.now(), Instant.now(), true)),
                List.of(new MatchingDatasetValue<>(dateToInstant("1984-02-29"), Instant.now(), Instant.now(), true),
                        new MatchingDatasetValue<>(dateToInstant("1984-03-01"), Instant.now(), Instant.now(), true)),
                List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                        new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-loa1",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(new MatchingDatasetValue<>("Jessica", Instant.now(), null, false)),
                Collections.singletonList(new MatchingDatasetValue<>("", Instant.now(), null, false)),
                Collections.singletonList(new MatchingDatasetValue<>("Rabbit", Instant.now(), null, false)),
                Optional.of(new MatchingDatasetValue<>(Gender.FEMALE, Instant.now(), null, false)),
                Collections.singletonList(new MatchingDatasetValue<>(dateToInstant("1960-03-23"), Instant.now(), null, false)),

                List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), null, false),
                        new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), false)),
                AuthnContext.LEVEL_1));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-loa2",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(new MatchingDatasetValue<>("Roger", Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>("", Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>("Rabbit", Instant.now(), Instant.now(), true)),
                Optional.of(new MatchingDatasetValue<>(Gender.MALE, Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>(dateToInstant("1958-04-09"), Instant.now(), Instant.now(), true)),

                List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                        new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-loa3",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(new MatchingDatasetValue<>("Apollo", Instant.now(), null, true)),
                Collections.singletonList(new MatchingDatasetValue<>("", Instant.now(), null, true)),
                Collections.singletonList(new MatchingDatasetValue<>("Eagle", Instant.now(), null, true)),
                Optional.of(new MatchingDatasetValue<>(Gender.FEMALE, Instant.now(), null, true)),
                Collections.singletonList(new MatchingDatasetValue<>(dateToInstant("1969-07-20"), Instant.now(), null, true)),

                List.of(new AddressFactory().create(Collections.singletonList("1 Four St"), "1A 2BD", "Something", "dummy uprn", Instant.now(), null, true),
                        new AddressFactory().create(Collections.singletonList("2 Five St"), "1B 2RD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)),
                AuthnContext.LEVEL_3));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-loax",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(new MatchingDatasetValue<>("Bugs", Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>("", Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>("Nummy", Instant.now(), Instant.now(), true)),
                Optional.of(new MatchingDatasetValue<>(Gender.MALE, Instant.now(), Instant.now(), true)),
                Collections.singletonList(new MatchingDatasetValue<>(dateToInstant("1958-04-09"), Instant.now(), Instant.now(), true)),

                List.of(new AddressFactory().create(Collections.singletonList("1 Two St"), "1A 2BC", "Something", "dummy uprn", Instant.now(), Instant.now(), true),
                        new AddressFactory().create(Collections.singletonList("2 Three St"), "1B 2CD", "Something else", "dummy second uprn", Instant.now(), Instant.now(), true)),
                AuthnContext.LEVEL_X));

        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-emoji",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("😀")),
                Collections.singletonList(createMdsValue("😎")),
                Collections.singletonList(createMdsValue("🙃")),
                Optional.of(createMdsValue(Gender.FEMALE)),
                Collections.singletonList(createMdsValue(dateToInstant("1968-06-12"))),
                Collections.singletonList(new AddressFactory().createNoDates(List.of("🏠"), "🏘", null, null, true)),
                AuthnContext.LEVEL_2));

        // this user matches one user in the example local matching service
        // https://github.com/alphagov/verify-local-matching-service-example/blob/b135523be4c156b5f6e4fc0b3b3f94bcfbef9f75/src/main/resources/db/migration/V2__Populate_With_Test_Data.sql#L31
        sacredUsers.add(new DatabaseIdpUser(
                idpFriendlyId + "-elms",
                UUID.randomUUID().toString(),
                "bar",
                Collections.singletonList(createMdsValue("Joe")),
                Collections.emptyList(),
                Collections.singletonList(createMdsValue("Bloggs")),
                Optional.of(createMdsValue(Gender.NOT_SPECIFIED)),
                Collections.singletonList(createMdsValue(dateToInstant("1970-01-01"))),
                List.of(new AddressFactory().create(List.of("The White Chapel Building, 10 Whitechapel High St", "London", "United Kingdom"), "E1 8DX",
                        null, null, Instant.now().atZone(ZoneId.of("UTC")).minusYears(1).toInstant(), null, true)),
                AuthnContext.LEVEL_2));

        sacredUsers.forEach(DatabaseIdpUser::hashPassword);
        return sacredUsers;
    }

    static List<DatabaseEidasUser> getHardCodedCountryTestUsers(String idpFriendlyId) {

        List<DatabaseEidasUser> sacredUsers = new ArrayList<>();

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId,
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Jack"),
                Optional.empty(),
                createMdsValue("Bauer"),
                Optional.empty(),
                createMdsValue(dateToInstant("1984-02-29")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-other",
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Martin"),
                Optional.empty(),
                createMdsValue("McFly"),
                Optional.empty(),
                createMdsValue(dateToInstant("1968-06-12")),
                AuthnContext.LEVEL_2));

        // These names contain characters from ISO/IEC 8859-15 which we regard as Latin.
        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-accents",
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Šarlota"),
                Optional.empty(),
                createMdsValue("Snježana"),
                Optional.empty(),
                createMdsValue(dateToInstant("1978-06-12")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-nonlatin",
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Georgios"),
                Optional.of(createMdsValue("Γεώργιος")),
                createMdsValue("Panathinaikos"),
                Optional.of(createMdsValue("Παναθηναϊκός")),
                createMdsValue(dateToInstant("1967-06-12")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-new",
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Jack"),
                Optional.empty(),
                createMdsValue("Griffin"),
                Optional.empty(),
                createMdsValue(dateToInstant("1983-06-21")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-c3",
                UUID.randomUUID().toString(), "bar",
                createMdsValue("J"),
                Optional.empty(),
                createMdsValue("Surname"),
                Optional.empty(),
                createMdsValue(dateToInstant("1822-11-27")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-ec3",
                 UUID.randomUUID().toString(),
                "bar",
                createMdsValue("Martin"),
                Optional.empty(),
                createMdsValue("Riggs"),
                Optional.empty(),
                createMdsValue(dateToInstant("1970-04-12")),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-complete",
                UUID.randomUUID().toString(),
                "bar",
                new MatchingDatasetValue<>("Jack", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>("Bauer", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>(dateToInstant("1984-02-29"), Instant.now(), Instant.now(), true),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-loa1",
                UUID.randomUUID().toString(),
                "bar",
                new MatchingDatasetValue<>("Jessica", Instant.now(), null, false),
                Optional.empty(),
                new MatchingDatasetValue<>("Rabbit", Instant.now(), null, false),
                Optional.empty(),
                new MatchingDatasetValue<>(dateToInstant("1960-03-23"), Instant.now(), null, false),
                AuthnContext.LEVEL_1));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-loa2",
                UUID.randomUUID().toString(),
                "bar",
                new MatchingDatasetValue<>("Roger", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>("Rabbit", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>(dateToInstant("1958-04-09"), Instant.now(), Instant.now(), true),
                AuthnContext.LEVEL_2));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-loa3",
                UUID.randomUUID().toString(),
                "bar",
                new MatchingDatasetValue<>("Apollo", Instant.now(), null, true),
                Optional.empty(),
                new MatchingDatasetValue<>("Eagle", Instant.now(), null, true),
                Optional.empty(),
                new MatchingDatasetValue<>(dateToInstant("1969-07-20"), Instant.now(), null, true),
                AuthnContext.LEVEL_3));

        sacredUsers.add(new DatabaseEidasUser(
                idpFriendlyId + "-loax",
                UUID.randomUUID().toString(),
                "bar",
                new MatchingDatasetValue<>("Bugs", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>("Nummy", Instant.now(), Instant.now(), true),
                Optional.empty(),
                new MatchingDatasetValue<>(dateToInstant("1958-04-09"), Instant.now(), Instant.now(), true),
                AuthnContext.LEVEL_X));

        sacredUsers.add(new DatabaseEidasUser(idpFriendlyId + "-emoji",
                UUID.randomUUID().toString(),
                "bar",
                createMdsValue("😀"),
                Optional.of(createMdsValue("GRINNING FACE")),
                createMdsValue("🙃"),
                Optional.of(createMdsValue("UPSIDE-DOWN FACE")),
                createMdsValue(dateToInstant("1968-06-12")),
                AuthnContext.LEVEL_2));

        sacredUsers.forEach(DatabaseEidasUser::hashPassword);
        return sacredUsers;
    }

    private static Instant dateToInstant(String date) {
        return BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(date);
    }

    private static <T> MatchingDatasetValue<T> createMdsValue(T value) {
        if (value == null) {
            return null;
        }

        return new MatchingDatasetValue<>(value, Instant.now().atZone(ZoneId.of("UTC")).minusDays(1).toInstant(), null, true);
    }
}
