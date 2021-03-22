package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.Line;

public class LineBuilder extends AbstractSAMLObjectBuilder<Line> {

    public LineBuilder() {
    }

    @Override
    public Line buildObject() {
        return buildObject(Line.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public Line buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new LineImpl(namespaceURI, localName, namespacePrefix);
    }
}
