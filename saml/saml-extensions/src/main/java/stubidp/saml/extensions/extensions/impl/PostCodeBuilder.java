package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.PostCode;

public class PostCodeBuilder extends AbstractSAMLObjectBuilder<PostCode> {

    @Override
    public PostCode buildObject() {
        return buildObject(PostCode.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public PostCode buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new PostCodeImpl(namespaceURI, localName, namespacePrefix);
    }
}
