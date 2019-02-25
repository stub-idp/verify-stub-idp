package uk.gov.ida.saml.core.test;

import org.apache.commons.codec.binary.Base64;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.InternalPublicKeyStore;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.PublicKeyInputStreamFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.SigningKeyStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HardCodedKeyStore implements SigningKeyStore, EncryptionKeyStore, InternalPublicKeyStore, PublicKeyInputStreamFactory {

    private final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
    private final String entityId;

    public HardCodedKeyStore(String whoAmI) {
        this.entityId = whoAmI;
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity(String entityId) {
        List<String> certs = Arrays.asList(TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(entityId));
        return certs.stream().map(cert -> publicKeyFactory.createPublicKey(cert)).collect(Collectors.toList());
    }

    @Override
    public PublicKey getEncryptionKeyForEntity(String entityId) {
        return getPrimaryEncryptionKeyForEntity(entityId);
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity() {
        return getVerifyingKeysForEntity(this.entityId);
    }

    @Override
    public InputStream createInputStream(String publicKeyUri) {
        switch (publicKeyUri) {
            case "../deploy/keys/test-rp.crt":
                return new ByteArrayInputStream(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT.getBytes());
            case "../deploy/keys/test-rp.pk8":
                return new ByteArrayInputStream(Base64.decodeBase64(TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY));
            case "../deploy/keys/hub_encryption.crt":
                return new ByteArrayInputStream(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT.getBytes());
            case "../deploy/keys/hub_signing.crt":
                return new ByteArrayInputStream(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT.getBytes());
            case "../deploy/keys/hub_encryption.pk8":
                return new ByteArrayInputStream(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
            case "../deploy/keys/hub_signing.pk8":
                return new ByteArrayInputStream(Base64.decodeBase64(TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY));
            default:
                throw new RuntimeException("Cert not found: " + publicKeyUri);
        }
    }

    public PublicKey getPrimaryEncryptionKeyForEntity(String entityId) {
        String cert = TestCertificateStrings.getPrimaryPublicEncryptionCert(entityId);

        return publicKeyFactory.createPublicKey(cert);
    }

    public PublicKey getSecondaryEncryptionKeyForEntity(String entityId) {
        String cert = TestCertificateStrings.getSecondaryPublicEncryptionCert(entityId);

        return publicKeyFactory.createPublicKey(cert);
    }

}
