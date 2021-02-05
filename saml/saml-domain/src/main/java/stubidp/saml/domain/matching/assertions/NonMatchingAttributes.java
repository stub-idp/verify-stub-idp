package stubidp.saml.domain.matching.assertions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import stubidp.saml.domain.assertions.Gender;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NonMatchingAttributes {

    protected final List<NonMatchingTransliterableAttribute> firstNames;
    protected final List<NonMatchingVerifiableAttribute<String>> middleNames;
    protected final List<NonMatchingTransliterableAttribute> surnames;
    protected final List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth;
    protected final NonMatchingVerifiableAttribute<Gender> gender;
    protected final List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses;

    @JsonCreator
    public NonMatchingAttributes(
            @JsonProperty("firstNames") List<NonMatchingTransliterableAttribute> firstNames,
            @JsonProperty("middleNames") List<NonMatchingVerifiableAttribute<String>> middleNames,
            @JsonProperty("surnames") List<NonMatchingTransliterableAttribute> surnames,
            @JsonProperty("datesOfBirth") List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth,
            @JsonProperty("gender") NonMatchingVerifiableAttribute<Gender> gender,
            @JsonProperty("addresses") List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses) {
        this.firstNames = firstNames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.datesOfBirth = datesOfBirth;
        this.gender = gender;
        this.addresses = addresses;
    }

    public List<NonMatchingTransliterableAttribute> getFirstNames() {
        return firstNames;
    }

    public List<NonMatchingVerifiableAttribute<String>> getMiddleNames() {
        return middleNames;
    }

    public List<NonMatchingTransliterableAttribute> getSurnames() {
        return surnames;
    }

    public List<NonMatchingVerifiableAttribute<LocalDate>> getDatesOfBirth() {
        return datesOfBirth;
    }

    public NonMatchingVerifiableAttribute<Gender> getGender() {
        return gender;
    }

    public List<NonMatchingVerifiableAttribute<NonMatchingAddress>> getAddresses() {
        return addresses;
    }

    public static String combineAttributeValues(List<? extends NonMatchingVerifiableAttribute<String>> attributes) {
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(NonMatchingVerifiableAttribute::getValue)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonMatchingAttributes that = (NonMatchingAttributes) o;
        return Objects.equals(firstNames, that.firstNames) && Objects.equals(middleNames, that.middleNames) && Objects.equals(surnames, that.surnames) && Objects.equals(datesOfBirth, that.datesOfBirth) && Objects.equals(gender, that.gender) && Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstNames, middleNames, surnames, datesOfBirth, gender, addresses);
    }

    @Override
    public String toString() {
        return String.format(
                "Attributes{ firstNames=%s, middleNames=%s, surnames=%s, datesOfBirth=%s, gender=%s, addresses=%s}",
                firstNames, middleNames, surnames, datesOfBirth, gender, addresses);
    }
}