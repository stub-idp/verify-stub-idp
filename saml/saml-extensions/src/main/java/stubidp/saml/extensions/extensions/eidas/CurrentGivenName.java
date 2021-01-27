package stubidp.saml.extensions.extensions.eidas;

import org.opensaml.saml.common.xml.SAMLConstants;
import stubidp.saml.extensions.IdaConstants;

import javax.xml.namespace.QName;

import static stubidp.saml.extensions.IdaConstants.EIDAS_NATURAL_PERSON_NS;

public interface CurrentGivenName extends TransliterableString {

    /** Element local name. */
    String DEFAULT_ELEMENT_LOCAL_NAME = "AttributeValue";

    /** Default element name. */
    QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

    /**  Local name of the XSI type. */
    String TYPE_LOCAL_NAME = "CurrentGivenNameType";

    /** QName of the XSI type. */
    QName TYPE_NAME = new QName(EIDAS_NATURAL_PERSON_NS, TYPE_LOCAL_NAME, IdaConstants.EIDAS_NATURUAL_PREFIX);

    /**
     * Return the given name.
     *
     * @return the given name
     */
    String getFirstName();

    /**
     * Set the given name.
     *
     * @param firstName the given name
     */
    void setFirstName(String firstName);
}
