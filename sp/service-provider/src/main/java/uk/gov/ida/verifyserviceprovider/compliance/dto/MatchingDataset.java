package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.saml.domain.assertions.AuthnContext;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class MatchingDataset {
    @NotNull
    @Valid
    @JsonProperty
    protected MatchingAttribute firstName;

    @NotNull
    @Valid
    @JsonProperty
    protected MatchingAttribute middleNames;

    @NotNull
    @Valid
    @JsonProperty
    @NotEmpty
    protected List<MatchingAttribute> surnames;


    @JsonInclude(Include.NON_NULL)
    @Valid
    @JsonProperty
    protected MatchingAttribute gender;

    @NotNull
    @Valid
    @JsonProperty
    protected MatchingAttribute dateOfBirth;

    @Valid
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    protected List<MatchingAddress> addresses;

    @NotNull
    @Valid
    @JsonProperty
    protected AuthnContext levelOfAssurance;

    @NotNull
    @Valid
    @JsonProperty
    protected String persistentId;

    public MatchingDataset() {}

    public MatchingDataset(MatchingAttribute firstName,
                           MatchingAttribute middleNames,
                           List<MatchingAttribute> surnames,
                           MatchingAttribute gender,
                           MatchingAttribute dateOfBirth,
                           List<MatchingAddress> addresses,
                           AuthnContext levelOfAssurance,
                           String persistentId) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
        this.levelOfAssurance = levelOfAssurance;
        this.persistentId = persistentId;
    }

    public MatchingAttribute getFirstName() {
        return firstName;
    }

    public MatchingAttribute getMiddleNames() {
        return middleNames;
    }

    public List<MatchingAttribute> getSurnames() {
        return surnames;
    }

    public MatchingAttribute getGender() {
        return gender;
    }

    public MatchingAttribute getDateOfBirth() {
        return dateOfBirth;
    }

    public List<MatchingAddress> getAddresses() {
        return addresses;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getPersistentId() {
        return persistentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchingDataset that = (MatchingDataset) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(middleNames, that.middleNames) && Objects.equals(surnames, that.surnames) && Objects.equals(gender, that.gender) && Objects.equals(dateOfBirth, that.dateOfBirth) && Objects.equals(addresses, that.addresses) && levelOfAssurance == that.levelOfAssurance && Objects.equals(persistentId, that.persistentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleNames, surnames, gender, dateOfBirth, addresses, levelOfAssurance, persistentId);
    }

    @Override
    public String toString() {
        return "MatchingDataset{" +
                "firstName=" + firstName +
                ", middleNames=" + middleNames +
                ", surnames=" + surnames +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", addresses=" + addresses +
                ", levelOfAssurance=" + levelOfAssurance +
                ", persistentId='" + persistentId + '\'' +
                '}';
    }
}
