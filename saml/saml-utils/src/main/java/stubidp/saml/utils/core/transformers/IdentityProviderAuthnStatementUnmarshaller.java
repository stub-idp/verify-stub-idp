package stubidp.saml.utils.core.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.IpAddress;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.IPAddress;

import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement;

public class IdentityProviderAuthnStatementUnmarshaller {

    private final AuthnContextFactory authnContextFactory;

    public IdentityProviderAuthnStatementUnmarshaller(
            AuthnContextFactory authnContextFactory) {

        this.authnContextFactory = authnContextFactory;
    }

    public IdentityProviderAuthnStatement fromAssertion(Assertion assertion) {

        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        String levelOfAssurance = authnStatement.getAuthnContext().getAuthnContextClassRef().getURI();
        IpAddress ipAddress = null;
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(IdaConstants.Attributes_1_1.IPAddress.NAME)) {
                    ipAddress = new IpAddress(((IPAddress) attribute.getAttributeValues().get(0)).getValue());
                    break;
                }
            }
        }
        return createIdentityProviderAuthnStatement(authnContextFactory.authnContextForLevelOfAssurance(levelOfAssurance), ipAddress);
    }
}
