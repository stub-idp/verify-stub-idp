package stubidp.saml.hub.hub.transformers.outbound;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.IdaSamlMessage;

import javax.inject.Inject;
import java.util.function.Function;

public abstract class IdaAuthnRequestToAuthnRequestTransformer<TInput extends IdaSamlMessage> implements Function<TInput,AuthnRequest> {
    private OpenSamlXmlObjectFactory samlObjectFactory;

    @Inject
    protected IdaAuthnRequestToAuthnRequestTransformer(OpenSamlXmlObjectFactory samlObjectFactory) {
        this.samlObjectFactory = samlObjectFactory;
    }

    @Override
    public AuthnRequest apply(TInput originalRequestToIdp) {
        AuthnRequest authnRequest = samlObjectFactory.createAuthnRequest();
        authnRequest.setID(originalRequestToIdp.getId());
        authnRequest.setIssueInstant(originalRequestToIdp.getIssueInstant());
        authnRequest.setDestination(originalRequestToIdp.getDestination().toASCIIString());
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

        Issuer issuer = samlObjectFactory.createIssuer(originalRequestToIdp.getIssuer());
        authnRequest.setIssuer(issuer);

        supplementAuthnRequestWithDetails(originalRequestToIdp, authnRequest);

        return authnRequest;
    }

    protected abstract void supplementAuthnRequestWithDetails(TInput originalRequestToIdp, AuthnRequest authnRequest);

    protected OpenSamlXmlObjectFactory getSamlObjectFactory() {
        return samlObjectFactory;
    }

}
