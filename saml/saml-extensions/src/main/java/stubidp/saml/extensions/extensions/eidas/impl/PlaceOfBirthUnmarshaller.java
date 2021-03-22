package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.PlaceOfBirth;

public class PlaceOfBirthUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new PlaceOfBirthUnmarshaller();

    public PlaceOfBirthUnmarshaller() {
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        PlaceOfBirth placeOfBirth = (PlaceOfBirth) samlObject;
        placeOfBirth.setPlaceOfBirth(elementContent);
    }
}
