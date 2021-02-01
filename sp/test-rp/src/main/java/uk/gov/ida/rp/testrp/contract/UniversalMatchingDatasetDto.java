package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversalMatchingDatasetDto {
    private List<UniversalAddressDto> addresses = new ArrayList<>();;
    private SimpleMdsValueDto<LocalDate> dateOfBirth;
    private SimpleMdsValueDto<GenderDto> gender;
    private TransliterableMdsValueDto firstName;
    private TransliterableMdsValueDto middleNames;
    private List<TransliterableMdsValueDto> surnames = new ArrayList<>();;

    @SuppressWarnings("unused")
    private UniversalMatchingDatasetDto() {
        // Needed for Json deserialisation
    }

    public UniversalMatchingDatasetDto(List<UniversalAddressDto> addresses,
                                       SimpleMdsValueDto<LocalDate> dateOfBirth,
                                       TransliterableMdsValueDto firstName,
                                       TransliterableMdsValueDto middleNames,
                                       List<TransliterableMdsValueDto> surnames,
                                       SimpleMdsValueDto<GenderDto> gender) {
        this.addresses = addresses;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.gender = gender;
        this.surnames = surnames;
    }

    public List<UniversalAddressDto> getAddresses() {
        return addresses;
    }

    public SimpleMdsValueDto<LocalDate> getDateOfBirth() {
        return dateOfBirth;
    }

    public SimpleMdsValueDto<String> getFirstName() {
        return firstName;
    }

    public SimpleMdsValueDto<GenderDto> getGender() {
        return gender;
    }

    public List<TransliterableMdsValueDto> getSurnames() {
        return surnames;
    }

    public TransliterableMdsValueDto getMiddleNames() {
        return middleNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversalMatchingDatasetDto that = (UniversalMatchingDatasetDto) o;
        return Objects.equals(addresses, that.addresses) && Objects.equals(dateOfBirth, that.dateOfBirth) && Objects.equals(gender, that.gender) && Objects.equals(firstName, that.firstName) && Objects.equals(middleNames, that.middleNames) && Objects.equals(surnames, that.surnames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addresses, dateOfBirth, gender, firstName, middleNames, surnames);
    }

    @Override
    public String toString() {
        return "UniversalMatchingDatasetDto{" +
                "addresses=" + addresses +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", firstName=" + firstName +
                ", middleNames=" + middleNames +
                ", surnames=" + surnames +
                '}';
    }
}

