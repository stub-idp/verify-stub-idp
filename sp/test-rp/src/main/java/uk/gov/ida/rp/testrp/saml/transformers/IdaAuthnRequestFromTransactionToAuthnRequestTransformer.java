package uk.gov.ida.rp.testrp.saml.transformers;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.hub.domain.AuthnRequestFromTransaction;
import uk.gov.ida.saml.hub.transformers.outbound.IdaAuthnRequestToAuthnRequestTransformer;

import javax.inject.Inject;

public class IdaAuthnRequestFromTransactionToAuthnRequestTransformer extends IdaAuthnRequestToAuthnRequestTransformer<AuthnRequestFromTransaction> {

    @Inject
    public IdaAuthnRequestFromTransactionToAuthnRequestTransformer(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }

    protected void supplementAuthnRequestWithDetails(AuthnRequestFromTransaction originalRequestToIdp, AuthnRequest authnRequest) {

        if (originalRequestToIdp.getForceAuthentication().isPresent()){
            authnRequest.setForceAuthn(originalRequestToIdp.getForceAuthentication().get());
        }

        if (originalRequestToIdp.getAssertionConsumerServiceIndex().isPresent()) {
            authnRequest.setAssertionConsumerServiceIndex(originalRequestToIdp.getAssertionConsumerServiceIndex().get());
        }
    }
}
