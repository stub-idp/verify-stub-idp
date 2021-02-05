package stubidp.stubidp.builders;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseIdpUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static stubidp.saml.domain.assertions.AuthnContext.LEVEL_1;

public class IdpUserBuilder {

    private String username = "default-username";
    private final String persistentId = UUID.randomUUID().toString();
    private String password = "default-password";
    private final List<SimpleMdsValue<String>> firstnames = singletonList(new SimpleMdsValue<>(
            "default-first-name",
            LocalDate.now().minusYears(20),
            null,
            true
    ));
    private final List<SimpleMdsValue<String>> middleNames = singletonList(new SimpleMdsValue<>(
            "default-middle-name",
            LocalDate.now().minusYears(20),
            null,
            true
    ));
    private final List<SimpleMdsValue<String>> surnames = singletonList(new SimpleMdsValue<>(
            "default-surname",
            LocalDate.now().minusYears(20),
            null,
            true
    ));
    private final Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private final List<SimpleMdsValue<LocalDate>> dateOfBirths = singletonList(new SimpleMdsValue<>(
            LocalDate.now().minusYears(20),
            LocalDate.now().minusYears(20),
            null,
            true
    ));
    private final List<Address> addresses = emptyList();
    private final AuthnContext levelOfAssurance = LEVEL_1;

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
