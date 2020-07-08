package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;

public class CurrentGivenNameUnmarshaller extends AbstractTransliterableStringUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new CurrentGivenNameUnmarshaller();

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        CurrentGivenName currentGivenName = (CurrentGivenName) samlObject;
        currentGivenName.setFirstName(elementContent);
    }
}
