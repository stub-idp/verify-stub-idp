package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class MatchingDatasetDto {

    private Optional<TransliterableMdsValueDto> firstName = Optional.empty();
    private Optional<SimpleMdsValueDto<String>> middleNames = Optional.empty();
    private List<TransliterableMdsValueDto> surnames = new ArrayList<>();
    private Optional<SimpleMdsValueDto<GenderDto>> gender = Optional.empty();
    private Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    protected MatchingDatasetDto() {
    }

    public MatchingDatasetDto(
            Optional<TransliterableMdsValueDto> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<TransliterableMdsValueDto> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth) {

        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    public Optional<TransliterableMdsValueDto> getFirstName() {
        return firstName;
    }

    public Optional<SimpleMdsValueDto<String>> getMiddleNames() {
        return middleNames;
    }

    public List<TransliterableMdsValueDto> getSurnames() {
        return surnames;
    }

    public Optional<SimpleMdsValueDto<GenderDto>> getGender() {
        return gender;
    }

    public Optional<SimpleMdsValueDto<LocalDate>> getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchingDatasetDto that = (MatchingDatasetDto) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(middleNames, that.middleNames) && Objects.equals(surnames, that.surnames) && Objects.equals(gender, that.gender) && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleNames, surnames, gender, dateOfBirth);
    }
}