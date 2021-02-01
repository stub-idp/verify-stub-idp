package uk.gov.ida.rp.testrp.builders;

import uk.gov.ida.rp.testrp.contract.Cycle3DatasetDto;
import uk.gov.ida.rp.testrp.contract.UniversalMatchingDatasetDto;
import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;

import java.util.Optional;

import static uk.gov.ida.rp.testrp.builders.UniversalMatchingDatasetDtoBuilder.aUniversalMatchingDatasetDto;

public class MatchingServiceRequestDtoBuilder {

    private String hashedPid = "random";
    private UniversalMatchingDatasetDto matchingDataset = aUniversalMatchingDatasetDto().build();
    private Optional<Cycle3DatasetDto> cycle3Dataset = Optional.empty();
    private String matchId;

    public static MatchingServiceRequestDtoBuilder aMatchingServiceRequestDto() {
        return new MatchingServiceRequestDtoBuilder();
    }

    public MatchingServiceRequestDto build() {
        return new MatchingServiceRequestDto(
            matchingDataset,
            cycle3Dataset,
            hashedPid,
            matchId,
            LevelOfAssuranceDto.LEVEL_2);
    }

    public MatchingServiceRequestDtoBuilder withHashedPid(String hashedPid) {
        this.hashedPid = hashedPid;
        return this;
    }

    public MatchingServiceRequestDtoBuilder withMatchingDataset(final UniversalMatchingDatasetDto matchingDataset) {
        this.matchingDataset = matchingDataset;
        return this;
    }

    public MatchingServiceRequestDtoBuilder withCycle3Dataset(final Cycle3DatasetDto cycle3Dataset) {
        this.cycle3Dataset = Optional.ofNullable(cycle3Dataset);
        return this;
    }

    public MatchingServiceRequestDtoBuilder withMatchId(String matchId) {
        this.matchId = matchId;
        return this;
    }
}
