package stubidp.saml.metadata;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterContext;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.metadata.exception.CertificateConversionException;
import stubidp.utils.security.security.verification.CertificateChainValidator;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;

import static org.opensaml.xmlsec.keyinfo.KeyInfoSupport.getCertificates;

public final class CertificateChainValidationFilter implements MetadataFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateChainValidationFilter.class);

    private final QName role;
    private final CertificateChainValidator certificateChainValidator;
    private final KeyStore keyStore;

    public CertificateChainValidationFilter(
        @NonNull final QName role,
        @NonNull final CertificateChainValidator certificateChainValidator,
        @NonNull final KeyStore keyStore) {

        this.role = role;
        this.certificateChainValidator = certificateChainValidator;
        this.keyStore = keyStore;
    }

    public QName getRole() {
        return role;
    }

    public CertificateChainValidator getCertificateChainValidator() {
        return certificateChainValidator;
    }

    private KeyStore getKeyStore() {
        return keyStore;
    }

    @Nullable
    @Override
    public XMLObject filter(@Nullable XMLObject metadata, @Nonnull MetadataFilterContext metadataFilterContext) {
        if (metadata == null) {
            return null;
        }

        try {
            if (metadata instanceof EntityDescriptor entityDescriptor) {
                filterOutUntrustedRoleDescriptors(entityDescriptor);
                if (entityDescriptor.getRoleDescriptors().isEmpty()) {
                    LOG.warn("EntityDescriptor '{}' has empty role descriptor list, metadata will be filtered out", entityDescriptor.getEntityID());
                    return null;
                }
            } else if (metadata instanceof EntitiesDescriptor entitiesDescriptor) {
                filterOutUntrustedEntityDescriptors(entitiesDescriptor);
                if (entitiesDescriptor.getEntityDescriptors().isEmpty()) {
                    LOG.warn("EntitiesDescriptor '{}' has empty entity descriptor list, metadata will be filtered out", entitiesDescriptor.getID());
                    return null;
                }
            } else {
                LOG.error("Internal error, metadata object was of an unsupported type: {}", metadata.getClass().getName());
                return null;
            }
        } catch (CertificateConversionException e) {
            LOG.error("Saw fatal error validating certificate chain, metadata will be filtered out", e);
            return null;
        }

        return metadata;
    }

    private void filterOutUntrustedEntityDescriptors(@NonNull EntitiesDescriptor entitiesDescriptor) {
        final String name = getGroupName(entitiesDescriptor);
        LOG.trace("Processing EntitiesDescriptor group: {}", name);

        // Can't use IndexedXMLObjectChildrenList sublist iterator remove() to remove members,
        // so just note them in a set and then remove after iteration has completed.
        final HashSet<EntityDescriptor> toRemove = new HashSet<>();

        entitiesDescriptor.getEntityDescriptors().forEach(
        entityDescriptor -> {
            filterOutUntrustedRoleDescriptors(entityDescriptor);
            if (entityDescriptor.getRoleDescriptors().isEmpty()) {
                LOG.warn("EntityDescriptor '{}' has empty role descriptor list, removing from metadata", entityDescriptor.getEntityID());
                toRemove.add(entityDescriptor);
            }
        });

        if (!toRemove.isEmpty()) {
            entitiesDescriptor.getEntityDescriptors().removeAll(toRemove);
            toRemove.clear();
        }
    }


    private void filterOutUntrustedRoleDescriptors(@NonNull EntityDescriptor entityDescriptor) {
        final String entityID = entityDescriptor.getEntityID();
        LOG.trace("Processing EntityDescriptor: {}", entityID);

        // Note that this is ok since we're iterating over an IndexedXMLObjectChildrenList directly,
        // rather than a sublist like in processEntityGroup, and iterator remove() is supported there.
        entityDescriptor.getRoleDescriptors()
            .removeIf(roleDescriptor -> {
                if (getRole().equals(roleDescriptor.getElementQName())) {
                    filterOutUntrustedKeyDescriptors(roleDescriptor);
                    if (roleDescriptor.getKeyDescriptors().isEmpty()) {
                        LOG.warn("KeyDescriptor '{}' has empty key descriptor list, removing from metadata", entityID);
                        return true;
                    }
                }
                return false;
            });
    }

    private void filterOutUntrustedKeyDescriptors(@NonNull RoleDescriptor roleDescriptor) {
        roleDescriptor.getKeyDescriptors().removeIf(
            keyDescriptor -> {
                KeyInfo keyInfo = keyDescriptor.getKeyInfo();
                try {
                    for (final X509Certificate certificate : getCertificates(keyInfo)) {
                        if (!getCertificateChainValidator().validate(certificate, getKeyStore()).isValid()) {
                            LOG.warn("Certificate chain validation failed for metadata entry {}", certificate.getSubjectX500Principal());
                            return true;
                        }
                    }
                    return false;
                } catch (CertificateException e) {
                    throw new CertificateConversionException(e);
                }
            }
        );
    }

    private String getGroupName(final EntitiesDescriptor group) {
        String name = group.getName();
        if (name != null) {
            return name;
        }
        name = group.getID();
        if (name != null) {
            return name;
        }
        return "(unnamed)";
    }
}
