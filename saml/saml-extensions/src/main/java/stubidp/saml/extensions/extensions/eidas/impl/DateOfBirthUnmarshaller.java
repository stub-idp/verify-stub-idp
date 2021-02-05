package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectUnmarshaller;

import java.time.LocalDate;

public class DateOfBirthUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public static final Unmarshaller UNMARSHALLER = new DateOfBirthUnmarshaller();

    public DateOfBirthUnmarshaller() {
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        DateOfBirth dateOfBirth = (DateOfBirth) samlObject;
        dateOfBirth.setDateOfBirth(LocalDate.parse(elementContent));
    }
}
