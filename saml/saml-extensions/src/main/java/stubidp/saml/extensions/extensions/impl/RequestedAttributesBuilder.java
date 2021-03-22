package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.RequestedAttributes;

public class RequestedAttributesBuilder extends AbstractSAMLObjectBuilder<RequestedAttributes> {

    public RequestedAttributesBuilder() {
    }

    @NonNull
    @Override
    public RequestedAttributes buildObject() {
        return buildObject(IdaConstants.EIDAS_NS, RequestedAttributes.DEFAULT_ELEMENT_LOCAL_NAME, IdaConstants.EIDAS_PREFIX);
    }

    @NonNull
    @Override
    public RequestedAttributes buildObject(@Nullable String namespaceURI, @NonNull @NotEmpty String localName, @Nullable String namespacePrefix) {
        return new RequestedAttributesImpl(namespaceURI, localName, namespacePrefix);
    }
}
