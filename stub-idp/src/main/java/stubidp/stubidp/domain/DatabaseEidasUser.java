package stubidp.stubidp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;
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
    private final SimpleMdsValue<String> firstname;
    private final Optional<SimpleMdsValue<String>> nonLatinFirstname;
    private final SimpleMdsValue<String> surname;
    private final Optional<SimpleMdsValue<String>> nonLatinSurname;
    private final SimpleMdsValue<Instant> dateOfBirth;
    private final AuthnContext levelOfAssurance;

    @JsonCreator
    public DatabaseEidasUser(
        @JsonProperty("username") String username,
        @JsonProperty("persistentId") String persistentId,
        @JsonProperty("password") String password,
        @JsonProperty("firstname") SimpleMdsValue<String> firstname,
        @JsonProperty("firstnameNonLatin") Optional<SimpleMdsValue<String>> nonLatinFirstname,
        @JsonProperty("surname") SimpleMdsValue<String> surname,
        @JsonProperty("surnameNonLatin") Optional<SimpleMdsValue<String>> nonLatinSurname,
        @JsonProperty("dateOfBirth") SimpleMdsValue<Instant> dateOfBirth,
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

    public SimpleMdsValue<String> getFirstname() {
        return firstname;
    }

    public Optional<SimpleMdsValue<String>> getNonLatinFirstname() {
        return nonLatinFirstname;
    }

    public SimpleMdsValue<String> getSurname() {
        return surname;
    }

    public Optional<SimpleMdsValue<String>> getNonLatinSurname() {
        return nonLatinSurname;
    }

    public SimpleMdsValue<Instant> getDateOfBirth() {
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
