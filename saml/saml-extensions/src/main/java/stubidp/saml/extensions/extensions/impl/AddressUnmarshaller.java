package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.InternationalPostCode;
import stubidp.saml.extensions.extensions.Line;
import stubidp.saml.extensions.extensions.PostCode;
import stubidp.saml.extensions.extensions.UPRN;

public class AddressUnmarshaller extends BaseMdsSamlObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new AddressUnmarshaller();

    protected void processChildElement(XMLObject parentObject, XMLObject childObject) throws UnmarshallingException {
        Address address = (Address) parentObject;

        if (childObject instanceof Line) {
            address.getLines().add((Line) childObject);
        } else if (childObject instanceof PostCode) {
            address.setPostCode((PostCode) childObject);
        } else if (childObject instanceof InternationalPostCode) {
            address.setInternationalPostCode((InternationalPostCode) childObject);
        } else if (childObject instanceof UPRN) {
            address.setUPRN((UPRN) childObject);
        } else {
            super.processChildElement(parentObject, childObject);
        }
    }
}
