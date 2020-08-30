package stubsp.stubsp.saml;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.Instant;

public class IdentityProviderAuthnStatementToAuthnStatementTransformer {

    public IdentityProviderAuthnStatementToAuthnStatementTransformer(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    public AuthnStatement transform(IdentityProviderAuthnStatement idaAuthnStatement) {
        AuthnStatement authnStatement = openSamlXmlObjectFactory.createAuthnStatement();
        AuthnContext authnContext = openSamlXmlObjectFactory.createAuthnContext();
        authnContext.setAuthnContextClassRef(openSamlXmlObjectFactory.createAuthnContextClassReference(idaAuthnStatement.getAuthnContext().getUri()));
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(Instant.now());
        return authnStatement;
    }

}
