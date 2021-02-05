package stubidp.stubidp.domain;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class DatabaseEidasUserBuilder {
    private String username;
    private final String persistentId = UUID.randomUUID().toString();
    private String password;
    private SimpleMdsValue<String> firstname;
    private Optional<SimpleMdsValue<String>> nonLatinFirstname = Optional.empty();
    private SimpleMdsValue<String> surname;
    private Optional<SimpleMdsValue<String>> nonLatinSurname = Optional.empty();
    private SimpleMdsValue<LocalDate> dateOfBirth;
    private AuthnContext authnContext;

    private DatabaseEidasUserBuilder() {}
    
    public static DatabaseEidasUserBuilder aDatabaseEidasUser() {
        return new DatabaseEidasUserBuilder();
    }
    
    public DatabaseEidasUser build() {
        if(null == username || null == password || null == firstname || null == surname || null == dateOfBirth || null == authnContext) {
            throw new RuntimeException("not all required values are set");
        }
        DatabaseEidasUser databaseEidasUser = new DatabaseEidasUser(username, persistentId, password, firstname, nonLatinFirstname, surname, nonLatinSurname, dateOfBirth, authnContext);
        databaseEidasUser.hashPassword();
        return databaseEidasUser;
    }

    public DatabaseEidasUserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public DatabaseEidasUserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public DatabaseEidasUserBuilder withFirstname(String firstname) {
        return withFirstname(firstname, true);
    }

    public DatabaseEidasUserBuilder withFirstname(String firstname, boolean verified) {
        this.firstname = createCurrentMdsValue(firstname, verified);
        return this;
    }

    public DatabaseEidasUserBuilder withNonLatinFirstname(String nonLatinFirstname) {
        this.nonLatinFirstname = Optional.ofNullable(createCurrentMdsValue(nonLatinFirstname, true));
        return this;
    }

    public DatabaseEidasUserBuilder withSurname(String surname) {
        return withSurname(surname, true);
    }

    public DatabaseEidasUserBuilder withSurname(String surname, boolean verified) {
        this.surname = createCurrentMdsValue(surname, verified);
        return this;
    }

    public DatabaseEidasUserBuilder withNonLatinSurname(String nonLatinSurname) {
        this.nonLatinSurname = Optional.ofNullable(createCurrentMdsValue(nonLatinSurname, true));
        return this;
    }

    public DatabaseEidasUserBuilder withDateOfBirth(String dateOfBirth) {
        return withDateOfBirth(dateOfBirth, true);
    }

    public DatabaseEidasUserBuilder withDateOfBirth(String dateOfBirth, boolean verified) {
        this.dateOfBirth = createCurrentMdsValue(dateToInstant(dateOfBirth), verified);
        return this;
    }

    public DatabaseEidasUserBuilder withAuthnContext(AuthnContext authnContext) {
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
