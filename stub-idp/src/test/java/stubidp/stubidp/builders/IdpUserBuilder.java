package stubidp.stubidp.builders;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.stubidp.domain.DatabaseIdpUser;
import stubidp.stubidp.domain.MatchingDatasetValue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_1;

public class IdpUserBuilder {

    private String username = "default-username";
    private String persistentId = UUID.randomUUID().toString();
    private String password = "default-password";
    private List<MatchingDatasetValue<String>> firstnames = singletonList(new MatchingDatasetValue<>(
            "default-first-name",
            Instant.now().atZone(ZoneId.of("UTC")).minusYears(20).toInstant(),
            null,
            true
    ));
    private List<MatchingDatasetValue<String>> middleNames = singletonList(new MatchingDatasetValue<>(
            "default-middle-name",
            Instant.now().atZone(ZoneId.of("UTC")).minusYears(20).toInstant(),
            null,
            true
    ));
    private List<MatchingDatasetValue<String>> surnames = singletonList(new MatchingDatasetValue<>(
            "default-surname",
            Instant.now().atZone(ZoneId.of("UTC")).minusYears(20).toInstant(),
            null,
            true
    ));
    private Optional<MatchingDatasetValue<Gender>> gender = Optional.empty();
    private List<MatchingDatasetValue<Instant>> dateOfBirths = singletonList(new MatchingDatasetValue<>(
            Instant.now().atZone(ZoneId.of("UTC")).minusYears(20).toInstant(),
            Instant.now().atZone(ZoneId.of("UTC")).minusYears(20).toInstant(),
            null,
            true
    ));
    private List<Address> addresses = emptyList();
    private AuthnContext levelOfAssurance = LEVEL_1;

    public static IdpUserBuilder anIdpUser() {
        return new IdpUserBuilder();
    }

    public static DatabaseIdpUser anyIdpUser() {
        return anIdpUser().build();
    }

    public DatabaseIdpUser build() {
        return new DatabaseIdpUser(
                username,
                persistentId,
                password,
                firstnames,
                middleNames,
                surnames,
                gender,
                dateOfBirths,
                addresses,
                levelOfAssurance
        );
    }

    public IdpUserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public IdpUserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
}
