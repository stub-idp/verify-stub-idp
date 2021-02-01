package uk.gov.ida.rp.testrp.contract;

public enum GenderDto {
    FEMALE("Female"),
    MALE("Male"),
    NOT_SPECIFIED("Not Specified");

    private String value;

    GenderDto(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
