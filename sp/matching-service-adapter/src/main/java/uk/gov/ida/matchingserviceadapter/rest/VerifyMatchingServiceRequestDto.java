package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.util.Objects;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VerifyMatchingServiceRequestDto extends MatchingServiceRequestDto {

    private VerifyMatchingDatasetDto matchingDataset;

    @SuppressWarnings("unused")//Needed by JAXB
    private VerifyMatchingServiceRequestDto() { super(); }

    public VerifyMatchingServiceRequestDto(
            VerifyMatchingDatasetDto matchingDataset,
            Optional<Cycle3DatasetDto> cycle3Dataset,
            String hashedPid,
            String matchId,
            LevelOfAssuranceDto levelOfAssurance) {

        super(cycle3Dataset, hashedPid, matchId, levelOfAssurance);
        this.matchingDataset = matchingDataset;
    }

    public VerifyMatchingDatasetDto getMatchingDataset() {
        return matchingDataset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VerifyMatchingServiceRequestDto that = (VerifyMatchingServiceRequestDto) o;
        return Objects.equals(matchingDataset, that.matchingDataset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), matchingDataset);
    }
}
