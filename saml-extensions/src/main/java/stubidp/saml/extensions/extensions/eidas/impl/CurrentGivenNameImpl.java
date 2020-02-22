package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;

import java.util.List;

public class CurrentGivenNameImpl extends AbstractTransliterableString implements CurrentGivenName {

    /** String to hold the first name. */
    private String firstName;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected CurrentGivenNameImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getFirstName() {
        return firstName;
    }

    /** {@inheritDoc} */
    public void setFirstName(String s) {

        firstName = prepareForAssignment(firstName, s);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
