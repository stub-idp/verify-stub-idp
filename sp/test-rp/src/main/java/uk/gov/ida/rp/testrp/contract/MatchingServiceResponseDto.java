package uk.gov.ida.rp.testrp.contract;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
