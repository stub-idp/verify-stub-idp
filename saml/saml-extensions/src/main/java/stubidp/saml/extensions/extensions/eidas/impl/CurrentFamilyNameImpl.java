package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;

import java.util.List;

public class CurrentFamilyNameImpl extends AbstractTransliterableString implements CurrentFamilyName {

    /** String to hold the family name. */
    private String familyName;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    CurrentFamilyNameImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getFamilyName() {
        return familyName;
    }

    /** {@inheritDoc} */
    public void setFamilyName(String s) {

        familyName = prepareForAssignment(familyName, s);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
