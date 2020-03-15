package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

public class DateOfBirthUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new DateOfBirthUnmarshaller();

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        DateOfBirth dateOfBirth = (DateOfBirth) samlObject;
        dateOfBirth.setDateOfBirth(BaseMdsSamlObjectUnmarshaller.InstantFromDate.of(elementContent));
    }
}
