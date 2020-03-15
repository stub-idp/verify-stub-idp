package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.PlaceOfBirth;

import java.util.List;

public class PlaceOfBirthImpl extends XSAnyImpl implements PlaceOfBirth {

    /** String to hold the place of birth. */
    private String placeOfBirth;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected PlaceOfBirthImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    /** {@inheritDoc} */
    public void setPlaceOfBirth(String s) {

        placeOfBirth = prepareForAssignment(placeOfBirth, s);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
