package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;

import java.util.List;

public class CountrySamlResponseImpl extends XSAnyImpl implements CountrySamlResponse {
    private String countrySamlResponse;

    protected CountrySamlResponseImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public String getValue() {
        return countrySamlResponse;
    }

    @Override
    public void setValue(String value) {
        countrySamlResponse = prepareForAssignment(countrySamlResponse, value);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}