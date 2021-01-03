package stubidp.saml.extensions.extensions.versioning.application;

import stubidp.saml.extensions.extensions.impl.StringValueSamlObjectImpl;
import stubidp.saml.extensions.extensions.versioning.Version;

class ApplicationVersionImpl extends StringValueSamlObjectImpl implements ApplicationVersion {

    public ApplicationVersionImpl() {
        super(Version.NAMESPACE_URI, DEFAULT_ELEMENT_LOCAL_NAME, NAMESPACE_PREFIX);
    }

    public ApplicationVersionImpl(String namespaceURI, String localName, String namespacePrefix) {
        super(namespaceURI, localName, namespacePrefix);
    }

    @Override
    public String toString() {
        return "ApplicationVersionImpl{" +
                    "value=" + getValue() +
                "}";
    }
}
