package uk.gov.ida.rp.testrp.contract;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MatchingServiceRequestDto {

    private UniversalMatchingDatasetDto matchingDataset;
    private Optional<Cycle3DatasetDto> cycle3Dataset = Optional.empty();
    private String hashedPid;
    private String matchId;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused")
    private MatchingServiceRequestDto() {
        // Needed by JAXB
    }

    public MatchingServiceRequestDto(
        UniversalMatchingDatasetDto matchingDataset,
        Optional<Cycle3DatasetDto> cycle3Dataset,
        String hashedPid,
        String matchId,
        LevelOfAssuranceDto levelOfAssurance) {

        this.matchingDataset = matchingDataset;
        this.cycle3Dataset = cycle3Dataset;
        this.hashedPid = hashedPid;
        this.matchId = matchId;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getHashedPid() {
        return hashedPid;
    }

    // this is for an interface
    @SuppressWarnings("unused")
    public String getMatchId() {
        return matchId;
    }

    public Optional<Cycle3DatasetDto> getCycle3Dataset() {
        return cycle3Dataset;
    }

    public LevelOfAssuranceDto getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public UniversalMatchingDatasetDto getMatchingDataset() {
        return matchingDataset;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString() {
        return "MatchingServiceRequestDto{" +
                "matchingDataset=" + matchingDataset +
                ", cycle3Dataset=" + cycle3Dataset +
                ", hashedPid='" + hashedPid + '\'' +
                ", matchId='" + matchId + '\'' +
                ", levelOfAssurance=" + levelOfAssurance +
                '}';
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
