package uk.gov.ida.rp.testrp.contract;

public class UnknownUserCreationRequestDto {

    private String hashedPid;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused")
    private UnknownUserCreationRequestDto() {
        //Needed by JAXB
    }

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
}
