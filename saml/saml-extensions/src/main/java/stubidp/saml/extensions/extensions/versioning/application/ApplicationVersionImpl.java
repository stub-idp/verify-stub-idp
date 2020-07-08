package stubidp.saml.extensions.extensions.versioning.application;

import stubidp.saml.extensions.extensions.impl.StringValueSamlObjectImpl;
import stubidp.saml.extensions.extensions.versioning.Version;

public class ApplicationVersionImpl extends StringValueSamlObjectImpl implements ApplicationVersion {

    public ApplicationVersionImpl() {
        super(Version.NAMESPACE_URI, DEFAULT_ELEMENT_LOCAL_NAME, Version.NAMESPACE_PREFIX);
    }

    public ApplicationVersionImpl(String namespaceURI, String localName, String namespacePrefix) {
        super(namespaceURI, localName, namespacePrefix);
    }
}
