package stubidp.saml.test.support;

import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.MutableCredential;
import org.opensaml.security.credential.UsageType;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.InternalPublicKeyStore;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.PublicKeyInputStreamFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHardCodedKeyStore implements InternalPublicKeyStore, PublicKeyInputStreamFactory {
    private final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
    private final String entityId;

    public AbstractHardCodedKeyStore(String entityId) {
        this.entityId = entityId;
    }

    public List<PublicKey> getVerifyingKeysForEntity(String entityId) {
        List<String> certs = Collections.singletonList(TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(entityId));
        return certs.stream().map(publicKeyFactory::createPublicKey).collect(Collectors.toList());
    }

    public PublicKey getEncryptionKeyForEntity(String entityId) {
        return getPrimaryEncryptionKeyForEntity(entityId);
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity() {
        return getVerifyingKeysForEntity(this.entityId);
    }

    @Override
    public InputStream createInputStream(String publicKeyUri) {
        return switch (publicKeyUri) {
            case "../deploy/keys/test-rp.crt" -> new ByteArrayInputStream(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT.getBytes());
            case "../deploy/keys/test-rp.pk8" -> new ByteArrayInputStream(Base64.getMimeDecoder().decode(TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY));
            case "../deploy/keys/hub_encryption.crt" -> new ByteArrayInputStream(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT.getBytes());
            case "../deploy/keys/hub_signing.crt" -> new ByteArrayInputStream(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT.getBytes());
            case "../deploy/keys/hub_encryption.pk8" -> new ByteArrayInputStream(Base64.getMimeDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
            case "../deploy/keys/hub_signing.pk8" -> new ByteArrayInputStream(Base64.getMimeDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY));
            default -> throw new RuntimeException("Cert not found: " + publicKeyUri);
        };
    }

    private PublicKey getPrimaryEncryptionKeyForEntity(String entityId) {
        String cert = TestCertificateStrings.getPrimaryPublicEncryptionCert(entityId);
        return publicKeyFactory.createPublicKey(cert);
    }

    public PublicKey getSecondaryEncryptionKeyForEntity(String entityId) {
        String cert = TestCertificateStrings.getSecondaryPublicEncryptionCert(entityId);
        return publicKeyFactory.createPublicKey(cert);
    }

    public List<Credential> getVerifyingCredentials(String entityId) {
        List<Credential> verifyingCredentials = new ArrayList<>();
        List<PublicKey> verifyingKeysForEntity = getVerifyingKeysForEntity(entityId);
        for(PublicKey verifyingKeyForEntity: verifyingKeysForEntity){
            MutableCredential credential = new BasicCredential(verifyingKeyForEntity);
            credential.setUsageType(UsageType.SIGNING);
            verifyingCredentials.add(credential);
        }
        return verifyingCredentials;
    }
}
