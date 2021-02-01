package uk.gov.ida.rp.testrp.builders;

import uk.gov.ida.rp.testrp.domain.AccessToken;

public class AccessTokenBuilder {

    private String value;

    AccessTokenBuilder() {
        value = "default-access-token";
    }

    public static AccessTokenBuilder anAccessToken() {
        return new AccessTokenBuilder();
    }

    public AccessToken build() {
        return new AccessToken(value);
    }

    public AccessTokenBuilder withValue(String value) {
        this.value = value;
        return this;
    }
}
