package stubidp.saml.hub.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import stubidp.saml.hub.core.InternalPublicKeyStore;
import stubidp.saml.hub.metadata.exceptions.HubEntityMissingException;
import stubidp.saml.security.MetadataBackedSignatureValidator;
import stubidp.saml.security.PublicKeyFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Use {@link MetadataBackedSignatureValidator} instead
 */
public class HubMetadataPublicKeyStore implements InternalPublicKeyStore {

    private final MetadataResolver metadataResolver;
    private final PublicKeyFactory publicKeyFactory;
    private final String hubEntityId;

    @Inject
    public HubMetadataPublicKeyStore(MetadataResolver metadataResolver,
                                     PublicKeyFactory publicKeyFactory,
                                     @Named("SpEntityId") String hubEntityId) {
        this.metadataResolver = metadataResolver;
        this.publicKeyFactory = publicKeyFactory;
        this.hubEntityId = hubEntityId;
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity() {
        try {
            CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(hubEntityId));
            return Optional.ofNullable(metadataResolver.resolveSingle(criteria))
                    .map(this::getPublicKeys)
                    .orElseThrow(hubMissingException());
        } catch (ResolverException e) {
            throw new RuntimeException(e);
        }
    }

    private Supplier<HubEntityMissingException> hubMissingException() {
        return () -> new HubEntityMissingException(MessageFormat.format("The HUB entity-id: \"{0}\" could not be found in the metadata. Metadata could be expired, invalid, or missing entities", this.hubEntityId));
    }

    private List<PublicKey> getPublicKeys(EntityDescriptor entityDescriptor) {
        return entityDescriptor
                .getSPSSODescriptor(SAMLConstants.SAML20P_NS)
                .getKeyDescriptors()
                .stream()
                .filter(keyDescriptor -> keyDescriptor.getUse() == UsageType.SIGNING)
                .flatMap(this::getCertificateFromKeyDescriptor)
                .map(publicKeyFactory::create)
                .collect(Collectors.toList());
    }

    private Stream<X509Certificate> getCertificateFromKeyDescriptor(KeyDescriptor keyDescriptor) {
        List<X509Data> x509Datas = keyDescriptor.getKeyInfo().getX509Datas();
        return x509Datas
                .stream()
                .flatMap(x509Data -> x509Data.getX509Certificates().stream());
    }

}
