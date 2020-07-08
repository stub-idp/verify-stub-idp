package stubidp.saml.extensions.extensions.impl;


import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.StringValueSamlObject;

import java.util.ArrayList;
import java.util.List;

public abstract class StringValueSamlObjectImpl extends XSAnyImpl implements StringValueSamlObject {
    public static final Marshaller MARSHALLER = new StringValueSamlObjectMarshaller();
    public static final Unmarshaller UNMARSHALLER = new StringValueSamlObjectUnmarshaller();
    private String value;

    protected StringValueSamlObjectImpl(String namespaceURI, String localName, String namespacePrefix) {
        super(namespaceURI, localName, namespacePrefix);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = prepareForAssignment(this.value, value);
    }

    @Override
    public List<XMLObject> getOrderedChildren() {
        return new ArrayList<>();
    }

}
