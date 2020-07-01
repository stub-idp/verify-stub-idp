package stubidp.stubidp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.stubidp.exceptions.UnHashedPasswordException;
import stubidp.stubidp.security.BCryptHelper;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonSerialize
@JsonInclude(value=NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseEidasUser implements Serializable {
    private final String username;
    private final String persistentId;
    private String password;
    private final MatchingDatasetValue<String> firstname;
    private final Optional<MatchingDatasetValue<String>> nonLatinFirstname;
    private final MatchingDatasetValue<String> surname;
    private final Optional<MatchingDatasetValue<String>> nonLatinSurname;
    private final MatchingDatasetValue<Instant> dateOfBirth;
    private final AuthnContext levelOfAssurance;

    @JsonCreator
    public DatabaseEidasUser(
        @JsonProperty("username") String username,
        @JsonProperty("persistentId") String persistentId,
        @JsonProperty("password") String password,
        @JsonProperty("firstname") MatchingDatasetValue<String> firstname,
        @JsonProperty("firstnameNonLatin") Optional<MatchingDatasetValue<String>> nonLatinFirstname,
        @JsonProperty("surname") MatchingDatasetValue<String> surname,
        @JsonProperty("surnameNonLatin") Optional<MatchingDatasetValue<String>> nonLatinSurname,
        @JsonProperty("dateOfBirth") MatchingDatasetValue<Instant> dateOfBirth,
        @JsonProperty("levelOfAssurance") AuthnContext levelOfAssurance) {

        this.username = username;
        this.persistentId = persistentId;
        this.password = password;
        this.hashPassword();
        this.firstname = firstname;
        this.nonLatinFirstname = nonLatinFirstname;
        this.surname = surname;
        this.nonLatinSurname = nonLatinSurname;
        this.dateOfBirth = dateOfBirth;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getUsername() {
        return username;
    }

    public String getPersistentId() {
        return persistentId;
    }

    public String getPassword() {
        if(BCryptHelper.alreadyCrypted(this.password)) {
            return this.password;
        }
        throw new UnHashedPasswordException(this.getUsername());
    }

    public MatchingDatasetValue<String> getFirstname() {
        return firstname;
    }

    public Optional<MatchingDatasetValue<String>> getNonLatinFirstname() {
        return nonLatinFirstname;
    }

    public MatchingDatasetValue<String> getSurname() {
        return surname;
    }

    public Optional<MatchingDatasetValue<String>> getNonLatinSurname() {
        return nonLatinSurname;
    }

    public MatchingDatasetValue<Instant> getDateOfBirth() {
        return dateOfBirth;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseEidasUser that = (DatabaseEidasUser) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(persistentId, that.persistentId) &&
                Objects.equals(password, that.password) &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(nonLatinFirstname, that.nonLatinFirstname) &&
                Objects.equals(surname, that.surname) &&
                Objects.equals(nonLatinSurname, that.nonLatinSurname) &&
                Objects.equals(dateOfBirth, that.dateOfBirth) &&
                levelOfAssurance == that.levelOfAssurance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, persistentId, password, firstname, nonLatinFirstname, surname, nonLatinSurname, dateOfBirth, levelOfAssurance);
    }

    @Override
    public String toString() {
        return "DatabaseEidasUser{" +
                "username='" + username + '\'' +
                ", persistentId='" + persistentId + '\'' +
                ", password='" + password + '\'' +
                ", firstname=" + firstname +
                ", nonLatinFirstname=" + nonLatinFirstname +
                ", surname=" + surname +
                ", nonLatinSurname=" + nonLatinSurname +
                ", dateOfBirth=" + dateOfBirth +
                ", levelOfAssurance=" + levelOfAssurance +
                '}';
    }

    public void hashPassword() {
        if(!BCryptHelper.alreadyCrypted(this.password)) {
            this.password = BCrypt.hashpw(this.password, BCrypt.gensalt());
        }
    }
}
