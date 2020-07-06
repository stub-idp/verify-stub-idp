package stubidp.stubidp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mindrot.jbcrypt.BCrypt;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.exceptions.UnHashedPasswordException;
import stubidp.stubidp.security.BCryptHelper;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonSerialize
@JsonInclude(value=NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseIdpUser implements Serializable {
    private final String username;
    private final String persistentId;
    private String password;
    private final List<SimpleMdsValue<String>> firstnames;
    private final List<SimpleMdsValue<String>> middleNames;
    private final List<SimpleMdsValue<String>> surnames;
    private final Optional<SimpleMdsValue<Gender>> gender;
    private final List<SimpleMdsValue<Instant>> dateOfBirths;
    private final List<Address> addresses;
    private final AuthnContext levelOfAssurance;

    @JsonCreator
    public DatabaseIdpUser(
        @JsonProperty("username") String username,
        @JsonProperty("persistentId") String persistentId,
        @JsonProperty("password") String password,
        @JsonProperty("firstnames") List<SimpleMdsValue<String>> firstnames,
        @JsonProperty("middleNames") List<SimpleMdsValue<String>> middleNames,
        @JsonProperty("surnames") List<SimpleMdsValue<String>> surnames,
        @JsonProperty("gender") Optional<SimpleMdsValue<Gender>> gender,
        @JsonProperty("dateOfBirths") List<SimpleMdsValue<Instant>> dateOfBirths,
        @JsonProperty("addresses") List<Address> addresses,
        @JsonProperty("levelOfAssurance") AuthnContext levelOfAssurance) {

        this.username = username;
        this.persistentId = persistentId;
        this.password = password;
        this.hashPassword();
        this.firstnames = firstnames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirths = dateOfBirths;
        this.addresses = addresses;
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

    public List<SimpleMdsValue<String>> getFirstnames() {
        return firstnames;
    }

    public List<SimpleMdsValue<String>> getMiddleNames() {
        return middleNames;
    }

    public List<SimpleMdsValue<String>> getSurnames() {
        return surnames;
    }

    public Optional<SimpleMdsValue<Gender>> getGender() {
        return gender;
    }

    public List<SimpleMdsValue<Instant>> getDateOfBirths() {
        return dateOfBirths;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    // Based on the implementation in IdpAssertionToAssertionTransformer
    @JsonIgnore
    public Address getCurrentAddress() {
        if (addresses.isEmpty()) {
            return null;
        }
        return addresses.get(0);
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseIdpUser)) return false;
        DatabaseIdpUser idpUser = (DatabaseIdpUser) o;
        return Objects.equals(username, idpUser.username) &&
            Objects.equals(persistentId, idpUser.persistentId) &&
            Objects.equals(password, idpUser.password) &&
            Objects.equals(firstnames, idpUser.firstnames) &&
            Objects.equals(middleNames, idpUser.middleNames) &&
            Objects.equals(surnames, idpUser.surnames) &&
            Objects.equals(gender, idpUser.gender) &&
            Objects.equals(dateOfBirths, idpUser.dateOfBirths) &&
            Objects.equals(addresses, idpUser.addresses) &&
            levelOfAssurance == idpUser.levelOfAssurance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, persistentId, password, firstnames, middleNames, surnames, gender, dateOfBirths, addresses, levelOfAssurance);
    }

    @Override
    public String toString() {
        return "DatabaseIdpUser{" +
            "username='" + username + '\'' +
            ", persistentId='" + persistentId + '\'' +
            ", password='" + password + '\'' +
            ", firstnames=" + firstnames +
            ", middleNames=" + middleNames +
            ", surnames=" + surnames +
            ", gender=" + gender +
            ", dateOfBirths=" + dateOfBirths +
            ", addresses=" + addresses +
            ", levelOfAssurance=" + levelOfAssurance +
            '}';
    }

    public void hashPassword() {
        if(!BCryptHelper.alreadyCrypted(this.password)) {
            this.password = BCrypt.hashpw(this.password, BCrypt.gensalt());
        }
    }
}
