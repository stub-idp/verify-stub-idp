package stubidp.saml.extensions.extensions.versioning;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.impl.AttributeValueImpl;
import stubidp.saml.extensions.extensions.versioning.application.ApplicationVersion;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionImpl extends AttributeValueImpl implements Version {
    private ApplicationVersion applicationVersion;

    public VersionImpl() {
        super(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        super.setSchemaType(Version.TYPE_NAME);
    }

    public VersionImpl(@Nullable String namespaceURI, @NonNull String elementLocalName, @Nullable String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public ApplicationVersion getApplicationVersion() {
        return this.applicationVersion;
    }

    @Override
    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return Stream.of(applicationVersion).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "VersionImpl{" +
                "applicationVersion=" + applicationVersion +
                '}';
    }
}
