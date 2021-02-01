package uk.gov.ida.rp.testrp.contract;

public class UnknownUserCreationResponseDto {

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final UnknownUserCreationResponseDto SUCCESS_RESPONSE = new UnknownUserCreationResponseDto(SUCCESS);
    public static final UnknownUserCreationResponseDto FAILURE_RESPONSE = new UnknownUserCreationResponseDto(FAILURE);

    private String result;

    @SuppressWarnings("unused")
    private UnknownUserCreationResponseDto() {
        //Needed by JAXB
    }

    public UnknownUserCreationResponseDto(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

}
