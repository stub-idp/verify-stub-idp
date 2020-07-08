package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.IdpFraudEventId;

public class IdpFraudEventIdBuilder extends AbstractSAMLObjectBuilder<IdpFraudEventId> {

    @Override
    public IdpFraudEventId buildObject() {
        return buildObject(IdpFraudEventId.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public IdpFraudEventId buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new IdpFraudEventIdImpl(namespaceURI, localName, namespacePrefix);
    }
}
