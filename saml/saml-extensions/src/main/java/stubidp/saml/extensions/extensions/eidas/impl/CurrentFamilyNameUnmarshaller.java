package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;

public class CurrentFamilyNameUnmarshaller extends AbstractTransliterableStringUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new CurrentFamilyNameUnmarshaller();

    public CurrentFamilyNameUnmarshaller() {
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        CurrentFamilyName currentFamilyName = (CurrentFamilyName) samlObject;
        currentFamilyName.setFamilyName(elementContent);
    }
}
