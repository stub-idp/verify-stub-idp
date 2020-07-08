package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

public class AuthnContextClassRefBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private String value = IdaAuthnContext.LEVEL_2_AUTHN_CTX;

    private AuthnContextClassRefBuilder() {}

    public static AuthnContextClassRefBuilder anAuthnContextClassRef() {
        return new AuthnContextClassRefBuilder();
    }

    public AuthnContextClassRefBuilder withAuthnContextClasRefValue(String value) {
        this.value = value;
        return this;
    }

    public AuthnContextClassRef build() {
        return openSamlXmlObjectFactory.createAuthnContextClassReference(value);
    }
}
