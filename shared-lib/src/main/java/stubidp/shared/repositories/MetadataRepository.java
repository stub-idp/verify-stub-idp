package stubidp.shared.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MetadataRepository {
    private final MetadataCredentialResolver metadataResolver;
    private final String expectedEntityId;

    public MetadataRepository(MetadataCredentialResolver metadataResolver, String expectedEntityId) {
        this.metadataResolver = metadataResolver;
        this.expectedEntityId = expectedEntityId;
    }

    public Iterable<String> getSigningCertificates() {
        return getCertificates(UsageType.SIGNING);
    }

    public String getEncryptionCertificate() {
        return getCertificates(UsageType.ENCRYPTION).stream().findFirst().orElseThrow();
    }

    private Set<String> extractAllCerts(KeyDescriptor keyDescriptor) {
        return keyDescriptor.getKeyInfo().getX509Datas().stream()
                .flatMap(x -> x.getX509Certificates().stream())
                .map(XSBase64Binary::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> getCertificates(final UsageType usageType) {
        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(expectedEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new UsageCriterion(usageType));
        criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));
        try {
            return StreamSupport.stream(metadataResolver.getRoleDescriptorResolver().resolve(criteriaSet).spliterator(), false)
                    .flatMap(x -> x.getKeyDescriptors().stream())
                    .filter(x -> usageType.equals(x.getUse()))
                    .flatMap(x -> extractAllCerts(x).stream())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (ResolverException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getAssertionConsumerServiceLocation() {
        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(expectedEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));
        try {
            return new URI(StreamSupport.stream(metadataResolver.getRoleDescriptorResolver().resolve(criteriaSet).spliterator(), false)
                    .flatMap(x -> ((SPSSODescriptor) x).getAssertionConsumerServices().stream())
                    .filter(x -> SAMLConstants.SAML2_POST_BINDING_URI.equals(x.getBinding()))
                    .findFirst().orElseThrow().getLocation());
        } catch (URISyntaxException | ResolverException e) {
            throw new RuntimeException(e);
        }
    }
}