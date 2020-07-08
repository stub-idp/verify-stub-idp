package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;

public class PersonIdentifierMarshaller extends AbstractSAMLObjectMarshaller {

    public static final Marshaller MARSHALLER = new PersonIdentifierMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) throws MarshallingException {
        PersonIdentifier personIdentifier = (PersonIdentifier) samlObject;
        ElementSupport.appendTextContent(domElement, personIdentifier.getPersonIdentifier());
    }
}
