package stubidp.saml.extensions.extensions.versioning;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.saml.common.AbstractSAMLObjectBuilder;

public class VersionBuilder extends AbstractSAMLObjectBuilder<Version> {

    public VersionBuilder() {
    }

    @NonNull
    @Override
    public Version buildObject() {
        return buildObject(Version.DEFAULT_ELEMENT_NAME, Version.TYPE_NAME);
    }

    @NonNull
    @Override
    public Version buildObject(@Nullable String namespaceURI, @NonNull String localName, @Nullable String namespacePrefix) {
        return new VersionImpl(namespaceURI, localName, namespacePrefix);
    }
}
