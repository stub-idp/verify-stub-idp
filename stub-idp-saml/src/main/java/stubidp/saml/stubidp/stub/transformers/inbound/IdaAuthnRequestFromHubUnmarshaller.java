package stubidp.saml.stubidp.stub.transformers.inbound;

import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import stubidp.saml.utils.core.domain.AuthnContext;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdaAuthnRequestFromHubUnmarshaller {
    public IdaAuthnRequestFromHub fromSaml(AuthnRequest authnRequest) {
        List<AuthnContextClassRef> authnContextClassRefs = authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs();

        ArrayList<AuthnContext> levelsOfAssurance = new ArrayList<>();
        for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
            levelsOfAssurance.add(mapAuthnContextClassReference(authnContextClassRef.getURI()));
        }
        Collections.sort(levelsOfAssurance); // use the fact that enums sort properly to guarantee the correct order

        return IdaAuthnRequestFromHub.createRequestReceivedFromHub(
            authnRequest.getID(),
            authnRequest.getIssuer().getValue(),
            levelsOfAssurance,
            authnRequest.isForceAuthn(),
            authnRequest.getConditions().getNotOnOrAfter(),
            authnRequest.getRequestedAuthnContext().getComparison());
    }

    private AuthnContext mapAuthnContextClassReference(String authnContextClassRef) {
        switch (authnContextClassRef) {
            case IdaAuthnContext.LEVEL_1_AUTHN_CTX:
                return AuthnContext.LEVEL_1;
            case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return AuthnContext.LEVEL_2;
            case IdaAuthnContext.LEVEL_3_AUTHN_CTX:
                return AuthnContext.LEVEL_3;
            case IdaAuthnContext.LEVEL_4_AUTHN_CTX:
                return AuthnContext.LEVEL_4;
        }
        throw new IllegalArgumentException("Missing validation on AuthnRequest Level of Assurance");
    }
}
