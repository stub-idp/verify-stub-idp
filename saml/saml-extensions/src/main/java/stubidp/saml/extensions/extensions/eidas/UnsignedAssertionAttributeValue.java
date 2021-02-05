package stubidp.saml.extensions.extensions.eidas;

import org.opensaml.saml.saml2.core.AttributeValue;

public interface UnsignedAssertionAttributeValue extends AttributeValue {
    void setValue(String value);
    String getValue();
}
