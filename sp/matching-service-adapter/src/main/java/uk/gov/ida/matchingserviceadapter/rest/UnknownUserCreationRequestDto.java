package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

import java.util.Objects;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnknownUserCreationRequestDto {
    private String hashedPid;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused") //Required by JAXB
    private UnknownUserCreationRequestDto() {}

    public UnknownUserCreationRequestDto(String hashedPid, LevelOfAssuranceDto levelOfAssurance) {
        this.hashedPid = hashedPid;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getHashedPid() {
        return hashedPid;
    }

    public LevelOfAssuranceDto getLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnknownUserCreationRequestDto that = (UnknownUserCreationRequestDto) o;
        return Objects.equals(hashedPid, that.hashedPid) && levelOfAssurance == that.levelOfAssurance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedPid, levelOfAssurance);
    }
}
