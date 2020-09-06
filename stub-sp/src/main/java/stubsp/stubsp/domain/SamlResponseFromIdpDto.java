package stubsp.stubsp.domain;

import stubsp.stubsp.services.ResponseStatus;

import java.util.Objects;

public class SamlResponseFromIdpDto {
    private final ResponseStatus responseStatus;
    private final String jsonResponse;
    private final String xmlResponse;
    private final String relayState;

    public SamlResponseFromIdpDto(ResponseStatus responseStatus, String jsonResponse, String xmlResponse, String relayState) {

        this.responseStatus = responseStatus;
        this.jsonResponse = jsonResponse;
        this.xmlResponse = xmlResponse;
        this.relayState = relayState;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public String getXmlResponse() {
        return xmlResponse;
    }

    public String getRelayState() {
        return relayState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SamlResponseFromIdpDto that = (SamlResponseFromIdpDto) o;
        return responseStatus == that.responseStatus &&
                Objects.equals(jsonResponse, that.jsonResponse) &&
                Objects.equals(xmlResponse, that.xmlResponse) &&
                Objects.equals(relayState, that.relayState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseStatus, jsonResponse, xmlResponse, relayState);
    }
}
