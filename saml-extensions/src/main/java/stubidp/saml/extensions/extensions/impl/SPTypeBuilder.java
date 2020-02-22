package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.SPType;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SPTypeBuilder extends AbstractSAMLObjectBuilder<SPType> {

    @NonNull
    @Override
    public SPType buildObject() {
        return buildObject(IdaConstants.EIDAS_NS, SPType.DEFAULT_ELEMENT_LOCAL_NAME, IdaConstants.EIDAS_PREFIX);
    }

    @NonNull
    @Override
    public SPType buildObject(@Nullable String namespaceURI, @NonNull @NotEmpty String localName, @Nullable String namespacePrefix) {
        return new SPTypeImpl(namespaceURI, localName, namespacePrefix);
    }
}
