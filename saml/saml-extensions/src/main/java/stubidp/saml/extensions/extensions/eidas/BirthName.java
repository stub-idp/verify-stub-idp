package stubidp.saml.extensions.extensions.eidas;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.extensions.IdaConstants;

import javax.xml.namespace.QName;

import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURAL_PERSON_NS;

public interface BirthName extends AttributeValue {

    /** Element local name. */
    String DEFAULT_ELEMENT_LOCAL_NAME = "AttributeValue";

    /** Default element name. */
    QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

    /**  Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "BirthNameType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(EIDAS_NATURAL_PERSON_NS, TYPE_LOCAL_NAME, IdaConstants.EIDAS_NATURUAL_PREFIX);

    /**
     * Return the birth name.
     *
     * @return the birth name
     */
    String getBirthName();

    /**
     * Set the birth name.
     *
     * @param birthName the birth name
     */
    void setBirthName(String birthName);
}
