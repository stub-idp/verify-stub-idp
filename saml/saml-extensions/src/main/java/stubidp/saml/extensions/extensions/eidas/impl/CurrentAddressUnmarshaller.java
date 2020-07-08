package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;

public class CurrentAddressUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new CurrentAddressUnmarshaller();

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        CurrentAddress currentAddress = (CurrentAddress) samlObject;
        currentAddress.setCurrentAddress(elementContent);
    }
}
