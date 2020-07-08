package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;

import java.util.List;

public class PersonIdentifierImpl extends XSAnyImpl implements PersonIdentifier {

    /** String to hold the person identifier. */
    private String personIdentifier;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected PersonIdentifierImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getPersonIdentifier() {
        return personIdentifier;
    }

    /** {@inheritDoc} */
    public void setPersonIdentifier(String s) {

        personIdentifier = prepareForAssignment(personIdentifier, s);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
