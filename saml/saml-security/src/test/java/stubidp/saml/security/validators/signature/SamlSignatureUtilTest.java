package stubidp.saml.security.validators.signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLRuntimeException;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.SignatureFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SamlSignatureUtilTest extends OpenSAMLRunner {

    private SignatureFactory signatureFactory;

    @BeforeEach
    void setup() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(
                TestEntityIds.HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keystore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keystore);
        signatureFactory = new SignatureFactory(keyStoreCredentialRetriever, new SignatureRSASHA256(), new DigestSHA256());
    }

    @Test
    void isSignatureSigned_shouldThrowExceptionIfSignatureIsNotMarshalled() {
        Signature signature = signatureFactory.createSignature();
        try {
            assertThat(SamlSignatureUtil.isSignaturePresent(signature)).isEqualTo(false);
        } catch (SAMLRuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Signature has not been marshalled");
            return;
        }
        fail("Signature has not been marshalled");
    }

    @Test
    void isSignatureSigned_shouldReturnFalseIfSignatureIsNotSigned() throws MarshallingException {
        Signature signature = signatureFactory.createSignature();
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signature).marshall(signature);
        assertThat(SamlSignatureUtil.isSignaturePresent(signature)).isEqualTo(false);
    }

    @Test
    void isSignatureSigned_shouldReturnTrueIfSignatureIsSigned() throws SignatureException, MarshallingException {
        Signature signature = signatureFactory.createSignature();
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signature).marshall(signature);
        Signer.signObject(signature);
        assertThat(SamlSignatureUtil.isSignaturePresent(signature)).isEqualTo(true);
    }
}
