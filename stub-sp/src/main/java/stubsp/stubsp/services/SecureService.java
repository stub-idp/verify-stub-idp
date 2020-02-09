package stubsp.stubsp.services;

import stubsp.stubsp.saml.request.IdpAuthnRequestBuilder;

import javax.inject.Inject;

public class SecureService {

    @Inject
    public SecureService() {

    }

    public String getSamlAuthnRequest() {
        return IdpAuthnRequestBuilder.anAuthnRequest()
                .build();
    }
}
