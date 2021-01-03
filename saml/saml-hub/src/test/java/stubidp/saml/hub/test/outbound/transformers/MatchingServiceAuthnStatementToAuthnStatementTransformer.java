package stubidp.saml.hub.test.outbound.transformers;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.matching.assertions.MatchingServiceAuthnStatement;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;
import java.time.Instant;

class MatchingServiceAuthnStatementToAuthnStatementTransformer {

    @Inject
    public MatchingServiceAuthnStatementToAuthnStatementTransformer(
            final OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {

        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    public AuthnStatement transform(MatchingServiceAuthnStatement idaAuthnStatement) {
        AuthnStatement authnStatement = openSamlXmlObjectFactory.createAuthnStatement();
        AuthnContext authnContext = openSamlXmlObjectFactory.createAuthnContext();
        authnContext.setAuthnContextClassRef(openSamlXmlObjectFactory.createAuthnContextClassReference(idaAuthnStatement.getAuthnContext().getUri()));
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(Instant.now());
        return authnStatement;
    }
}
