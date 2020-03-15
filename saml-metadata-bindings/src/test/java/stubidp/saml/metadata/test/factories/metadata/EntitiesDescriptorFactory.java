package stubidp.saml.metadata.test.factories.metadata;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.utils.core.test.builders.metadata.EntitiesDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.SignatureBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static stubidp.saml.utils.core.test.builders.metadata.EntitiesDescriptorBuilder.anEntitiesDescriptor;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;

public class EntitiesDescriptorFactory {
    private final Instant validUntil = Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant();
    private EntityDescriptorFactory entityDescriptorFactory = new EntityDescriptorFactory();

    public EntitiesDescriptor defaultEntitiesDescriptor() {
        return entitiesDescriptor(defaultEntityDescriptors());
    }

    public EntitiesDescriptor emptyEntitiesDescriptor() {
        return entitiesDescriptor(emptyList());
    }

    public EntitiesDescriptor entitiesDescriptor(List<EntityDescriptor> entityDescriptors) {
        return buildEntitiesDescriptor(entityDescriptors, Optional.of(defaultSignature()), validUntil);
    }

    public EntitiesDescriptor expiredEntitiesDescriptor() {
        Instant expired = Instant.now().atZone(ZoneId.of("UTC")).minusWeeks(2).toInstant();
        return buildEntitiesDescriptor(defaultEntityDescriptors(), Optional.of(defaultSignature()), expired);
    }

    public EntitiesDescriptor unsignedEntitiesDescriptor() {
        return buildEntitiesDescriptor(defaultEntityDescriptors(), Optional.empty(), validUntil);
    }

    private EntitiesDescriptor buildEntitiesDescriptor(List<EntityDescriptor> entityDescriptors, Optional<Signature> signature, Instant validUntil) {
        EntitiesDescriptorBuilder entitiesDescriptorBuilder = entitiesDescriptorBuilder(entityDescriptors, signature, validUntil);
        return buildEntitiesDescriptor(entitiesDescriptorBuilder);
    }

    private EntitiesDescriptor buildEntitiesDescriptor(EntitiesDescriptorBuilder entitiesDescriptorBuilder) {
        try {
            return entitiesDescriptorBuilder.build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private EntitiesDescriptorBuilder entitiesDescriptorBuilder(List<EntityDescriptor> entityDescriptors, Optional<Signature> signature, Instant validUntil) {
        EntitiesDescriptorBuilder entitiesDescriptorBuilder = anEntitiesDescriptor()
                .withEntityDescriptors(entityDescriptors)
                .withValidUntil(validUntil);
        signature.ifPresent(entitiesDescriptorBuilder::withSignature);
        return entitiesDescriptorBuilder;
    }

    private List<EntityDescriptor> defaultEntityDescriptors() {
        return entityDescriptorFactory.defaultEntityDescriptors();
    }

    private Signature defaultSignature() {
        return buildSignature(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
    }

    private Signature buildSignature(String publicCertificate, String privateKey) {
        TestCredentialFactory testCredentialFactory = new TestCredentialFactory(publicCertificate, privateKey);
        Credential credential = testCredentialFactory.getSigningCredential();
        return SignatureBuilder.aSignature().withSigningCredential(credential).withX509Data(publicCertificate).build();
    }

    private Signature buildFullChainSignature(String publicCertificate, List<String> certificateChain, String privateKey) {
        TestCredentialFactory testCredentialFactory = new TestCredentialFactory(publicCertificate, privateKey);
        Credential credential = testCredentialFactory.getSigningCredential();
        return SignatureBuilder.aSignature().withSigningCredential(credential).withX509Data(certificateChain).build();
    }

    public EntitiesDescriptor signedEntitiesDescriptor(String publicCertificate, String privateKey) {
        return signedEntitiesDescriptor(defaultEntityDescriptors(), publicCertificate, privateKey);
    }

    public EntitiesDescriptor signedEntitiesDescriptor(List<EntityDescriptor> entityDescriptorList, String publicCertificate, String privateKey) {
        return buildEntitiesDescriptor(entityDescriptorList, Optional.of(buildSignature(publicCertificate, privateKey)), validUntil);
    }

    public EntitiesDescriptor signedEntitiesDescriptor(Signature signature) {
        return buildEntitiesDescriptor(defaultEntityDescriptors(), Optional.of(signature), validUntil);
    }

    public EntitiesDescriptor signedEntitiesDescriptor(String id, Signature signature) {
        EntitiesDescriptorBuilder entitiesDescriptorBuilder = entitiesDescriptorBuilder(defaultEntityDescriptors(), Optional.of(signature), validUntil);
        entitiesDescriptorBuilder.withId(id);
        return buildEntitiesDescriptor(entitiesDescriptorBuilder);
    }

    public EntitiesDescriptor fullChainSignedEntitiesDescriptor(String publicCertificate, List<String> certificateChain, String privateKey) {
        return buildEntitiesDescriptor(defaultEntityDescriptors(), Optional.of(buildFullChainSignature(publicCertificate, certificateChain, privateKey)), validUntil);
    }
}
