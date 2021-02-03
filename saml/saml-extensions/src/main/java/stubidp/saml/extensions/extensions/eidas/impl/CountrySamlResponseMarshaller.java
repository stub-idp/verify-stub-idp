package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;

public class CountrySamlResponseMarshaller extends AbstractSAMLObjectMarshaller {

    public CountrySamlResponseMarshaller() {
    }

    @Override
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        CountrySamlResponse countrySamlResponse = (CountrySamlResponse) samlObject;
        ElementSupport.appendTextContent(domElement, countrySamlResponse.getValue());
    }
}