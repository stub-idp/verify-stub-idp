package stubsp.stubsp.services;

import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubsp.stubsp.saml.response.SamlResponseDecrypter;

import javax.inject.Inject;

public class SamlResponseService {

    private final SamlResponseDecrypter samlResponseDecrypter;

    @Inject
    public SamlResponseService(SamlResponseDecrypter samlResponseDecrypter) {
        this.samlResponseDecrypter = samlResponseDecrypter;
    }

    public ResponseStatus processResponse(String samlResponse, String relayState) {
        InboundResponseFromIdp inboundResponseFromIdp = samlResponseDecrypter.decryptSaml(samlResponse);
        switch(inboundResponseFromIdp.getStatus().getStatusCode()) {
            case Success: {
                return ResponseStatus.SUCCESS;
            }
            default: {
                return ResponseStatus.AUTHENTICATION_FAILED;
            }
        }
    }
}
