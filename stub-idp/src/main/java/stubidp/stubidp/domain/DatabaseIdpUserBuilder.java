package stubidp.stubidp.domain;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabaseIdpUserBuilder {
    private String username;
    private final String persistentId = UUID.randomUUID().toString();
    private String password;
    private List<SimpleMdsValue<String>> firstnames = List.of();
    private List<SimpleMdsValue<String>> middleNames = List.of();
    private List<SimpleMdsValue<String>> surnames = List.of();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private List<SimpleMdsValue<LocalDate>> datesOfBirth = List.of();
    private List<Address> addresses = List.of();
    private AuthnContext authnContext;
    
    private DatabaseIdpUserBuilder() {}
    
    public static DatabaseIdpUserBuilder aDatabaseIdpUser() {
        return new DatabaseIdpUserBuilder();
    }

    public DatabaseIdpUser build() {
        if(null == username || null == password || null == authnContext) {
            throw new RuntimeException("not all required values are set");
        }
        DatabaseIdpUser databaseIdpUser = new DatabaseIdpUser(username, persistentId, password, firstnames, middleNames, surnames, gender, datesOfBirth, addresses, authnContext);
        databaseIdpUser.hashPassword();
        return databaseIdpUser;
    }

    public DatabaseIdpUserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public DatabaseIdpUserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public DatabaseIdpUserBuilder withFirstname(String firstname) {
        return withFirstname(firstname, true);
    }

    public DatabaseIdpUserBuilder withFirstname(String firstname, boolean verified) {
        this.firstnames = Collections.singletonList(createCurrentMdsValue(firstname, verified));
        return this;
    }

    public DatabaseIdpUserBuilder withFirstnames(List<SimpleMdsValue<String>> firstnames) {
        this.firstnames = firstnames;
        return this;
    }

    public DatabaseIdpUserBuilder withMiddlename(String middlename) {
        return withMiddlename(middlename, true);
    }

    public DatabaseIdpUserBuilder withMiddlename(String middlename, boolean verified) {
        this.middleNames = Collections.singletonList(createCurrentMdsValue(middlename, verified));
        return this;
    }

    public DatabaseIdpUserBuilder withMiddlenames(List<SimpleMdsValue<String>> middlenames) {
        this.middleNames = middlenames;
        return this;
    }

    public DatabaseIdpUserBuilder withSurname(String surname) {
        return withSurname(surname, true);
    }

    public DatabaseIdpUserBuilder withSurname(String surname, boolean verified) {
        this.surnames = Collections.singletonList(createCurrentMdsValue(surname, verified));
        return this;
    }

    public DatabaseIdpUserBuilder withSurnames(List<SimpleMdsValue<String>> surnames) {
        this.surnames = surnames;
        return this;
    }

    public DatabaseIdpUserBuilder withGender(Gender gender) {
        return withGender(gender, true);
    }

    public DatabaseIdpUserBuilder withGender(Gender gender, boolean verified) {
        this.gender = Optional.of(createCurrentMdsValue(gender, verified));
        return this;
    }

    public DatabaseIdpUserBuilder withDateOfBirth(String dateOfBirth) {
        return withDateOfBirth(dateOfBirth, true);
    }

    public DatabaseIdpUserBuilder withDateOfBirth(String dateOfBirth, boolean verified) {
        this.datesOfBirth = Collections.singletonList(createCurrentMdsValue(dateToInstant(dateOfBirth), verified));
        return this;
    }

    public DatabaseIdpUserBuilder withDatesOfBirth(List<SimpleMdsValue<LocalDate>> datesOfBirth) {
        this.datesOfBirth = datesOfBirth;
        return this;
    }

    public DatabaseIdpUserBuilder withAddress(Address address) {
        this.addresses = List.of(address);
        return this;
    }

    public DatabaseIdpUserBuilder withAddresses(List<Address> addresses) {
        this.addresses = addresses;
        return this;
    }

    public DatabaseIdpUserBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = authnContext;
        return this;
    }

    private static LocalDate dateToInstant(String date) {
        return LocalDate.parse(date);
    }

    private static <T> SimpleMdsValue<T> createCurrentMdsValue(T value, boolean verified) {
        return new SimpleMdsValue<>(value, LocalDate.now().minusDays(1), null, verified);
    }
}
