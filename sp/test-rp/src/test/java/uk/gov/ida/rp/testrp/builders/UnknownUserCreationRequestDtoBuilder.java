package uk.gov.ida.rp.testrp.builders;

import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;

public class UnknownUserCreationRequestDtoBuilder {

    private String hashedPid = "random";

    public static UnknownUserCreationRequestDtoBuilder anUnknownUserCreationRequestDto() {
        return new UnknownUserCreationRequestDtoBuilder();
    }

    public UnknownUserCreationRequestDto build() {
        return new UnknownUserCreationRequestDto(
                hashedPid,
                LevelOfAssuranceDto.LEVEL_2);
    }

    public UnknownUserCreationRequestDtoBuilder withHashedPid(String hashedPid) {
        this.hashedPid = hashedPid;
        return this;
    }
}
