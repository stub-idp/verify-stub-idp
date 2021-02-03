package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VerifyMatchingDatasetDto extends MatchingDatasetDto {

    private List<VerifyAddressDto> addresses = new ArrayList<>();

    @SuppressWarnings("unused") // needed for JAXB
    private VerifyMatchingDatasetDto() {
        super();
    }

    public VerifyMatchingDatasetDto(
            Optional<TransliterableMdsValueDto> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<TransliterableMdsValueDto> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth,
            List<VerifyAddressDto> addresses) {
        super(firstName, middleNames, surnames, gender, dateOfBirth);

        this.addresses = addresses;
    }

    public List<VerifyAddressDto> getAddresses() {
        return addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VerifyMatchingDatasetDto that = (VerifyMatchingDatasetDto) o;
        return Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addresses);
    }
}