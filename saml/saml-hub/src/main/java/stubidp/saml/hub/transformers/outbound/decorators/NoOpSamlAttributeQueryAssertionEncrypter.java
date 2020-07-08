package stubidp.saml.hub.transformers.outbound.decorators;

import org.opensaml.saml.saml2.core.AttributeQuery;
import stubidp.saml.hub.core.transformers.outbound.decorators.SamlAttributeQueryAssertionEncrypter;

public class NoOpSamlAttributeQueryAssertionEncrypter extends SamlAttributeQueryAssertionEncrypter {
    public NoOpSamlAttributeQueryAssertionEncrypter() {
        super(null, null, null);
    }

    @Override
    public AttributeQuery encryptAssertions(AttributeQuery attributeQuery) {
        return attributeQuery;
    }
}
