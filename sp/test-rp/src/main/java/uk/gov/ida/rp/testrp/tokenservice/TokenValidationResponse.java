package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenValidationResponse {

    private boolean valid;

    @SuppressWarnings("unused")
    public TokenValidationResponse() {
        // needed for JAXB
    }

    public TokenValidationResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
