package stubidp.saml.extensions.extensions.impl;


import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import stubidp.saml.extensions.extensions.IdpFraudEventId;

public class IdpFraudEventIdImpl extends StringBasedMdsAttributeValueImpl implements IdpFraudEventId {
    public static final Marshaller MARSHALLER = new StringBasedMdsAttributeValueMarshaller(IdpFraudEventId.TYPE_LOCAL_NAME);
    public static final Unmarshaller UNMARSHALLER = new StringBasedMdsAttributeValueUnmarshaller();

    protected IdpFraudEventIdImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
