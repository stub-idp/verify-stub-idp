package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

public class AuthnContextBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private AuthnContextClassRef authnContextClassRef = AuthnContextClassRefBuilder.anAuthnContextClassRef().build();

    private AuthnContextBuilder() {}

    public static AuthnContextBuilder anAuthnContext() {
        return new AuthnContextBuilder();
    }

    public AuthnContext build() {
        AuthnContext authnContext = openSamlXmlObjectFactory.createAuthnContext();
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        return authnContext;
    }

    public AuthnContextBuilder withAuthnContextClassRef(AuthnContextClassRef authnContextClassRef) {
        this.authnContextClassRef = authnContextClassRef;
        return this;
    }
}
