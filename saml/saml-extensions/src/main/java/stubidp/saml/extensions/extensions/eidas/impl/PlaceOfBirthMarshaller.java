package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.PlaceOfBirth;

public class PlaceOfBirthMarshaller extends AbstractSAMLObjectMarshaller {

    public static final Marshaller MARSHALLER = new PlaceOfBirthMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        PlaceOfBirth placeOfBirth = (PlaceOfBirth) samlObject;
        ElementSupport.appendTextContent(domElement, placeOfBirth.getPlaceOfBirth());
    }
}
