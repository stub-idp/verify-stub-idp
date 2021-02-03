package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class UniversalMatchingDatasetDto extends MatchingDatasetDto {

    private Optional<List<UniversalAddressDto>> addresses = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    private UniversalMatchingDatasetDto() {
        super();
    }

    public UniversalMatchingDatasetDto(
            Optional<TransliterableMdsValueDto> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<TransliterableMdsValueDto> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth,
            Optional<List<UniversalAddressDto>> addresses) {
        super(firstName, middleNames, surnames, gender, dateOfBirth);

        this.addresses = addresses;
    }

    public Optional<List<UniversalAddressDto>> getAddresses() {
        return addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UniversalMatchingDatasetDto that = (UniversalMatchingDatasetDto) o;
        return Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addresses);
    }
}