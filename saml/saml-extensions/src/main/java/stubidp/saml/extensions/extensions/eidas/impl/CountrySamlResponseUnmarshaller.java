package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;

public class CountrySamlResponseUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    @Override
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        CountrySamlResponse countrySamlResponse = (CountrySamlResponse) samlObject;
        countrySamlResponse.setValue(elementContent);
    }
}