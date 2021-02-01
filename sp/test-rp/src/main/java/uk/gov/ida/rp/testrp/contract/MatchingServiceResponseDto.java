package uk.gov.ida.rp.testrp.contract;

import java.util.Objects;

public class MatchingServiceResponseDto {
    public static final String MATCH = "match";
    public static final String NO_MATCH = "no-match";
    public static final MatchingServiceResponseDto MATCH_RESPONSE = new MatchingServiceResponseDto(MATCH);
    public static final MatchingServiceResponseDto NO_MATCH_RESPONSE = new MatchingServiceResponseDto(NO_MATCH);

    private String result;

    @SuppressWarnings("unused")
    private MatchingServiceResponseDto() {
        //Needed by JAXB
    }

    public MatchingServiceResponseDto(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchingServiceResponseDto that = (MatchingServiceResponseDto) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }
}
