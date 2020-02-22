package stubidp.saml.extensions.extensions.versioning.application;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.saml.common.AbstractSAMLObjectBuilder;

public class ApplicationVersionBuilder extends AbstractSAMLObjectBuilder<ApplicationVersion> {
    @NonNull
    @Override
    public ApplicationVersion buildObject() {
        return buildObject(ApplicationVersion.DEFAULT_ELEMENT_NAME);
    }

    @NonNull
    @Override
    public ApplicationVersion buildObject(@Nullable String namespaceURI, @NonNull String localName, @Nullable String namespacePrefix) {
        return new ApplicationVersionImpl(namespaceURI, localName, namespacePrefix);
    }
}
