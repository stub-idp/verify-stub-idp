package stubidp.stubidp;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.security.IdaKeyStore;
import stubidp.stubidp.domain.factories.StubTransformersFactory;
import stubidp.stubidp.repositories.Idp;
import stubidp.stubidp.saml.locators.AssignableEntityToEncryptForLocator;
import stubidp.stubidp.saml.transformers.OutboundResponseFromIdpTransformerProvider;
import stubidp.stubidp.security.HardCodedKeyStore;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OutboundResponseFromIdpTransformerProviderTest {

    private static final HardCodedKeyStore ENC_KEYSTORE_DUMMY = new HardCodedKeyStore(TestEntityIds.STUB_IDP_ONE);
    private static final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
    private static final PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(TestEntityIds.STUB_IDP_ONE)));
    private static final PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT);
    private static final PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY));
    private static final PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT);
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = new SignatureRSASHA256();
    private static final DigestAlgorithm DIGEST_ALGORITHM = new DigestSHA256();

    private static final KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

    private static final IdaKeyStore PRIV_KEYSTORE_DUMMY = new IdaKeyStore(new KeyPair(publicKey, privateKey), Collections.singletonList(encryptionKeyPair));
    private static final Optional<String> TEST_SIGNING_KEY = Optional.of("some/signing/key");
    private static final AssignableEntityToEncryptForLocator ENTITY_TO_ENCRYPT_FOR_LOCATOR = new AssignableEntityToEncryptForLocator();
    private static final String ISSUER_ID = TestEntityIds.STUB_IDP_ONE;

    private static StubTransformersFactory mockStubTransformersFactory;
    private OutboundResponseFromIdpTransformerProvider testTransformerProvider;

    @BeforeEach
    public void setUp() throws Exception {
        mockStubTransformersFactory = mock(StubTransformersFactory.class);

        testTransformerProvider = new OutboundResponseFromIdpTransformerProvider(
                ENC_KEYSTORE_DUMMY,
                PRIV_KEYSTORE_DUMMY,
                ENTITY_TO_ENCRYPT_FOR_LOCATOR,
                TEST_SIGNING_KEY,
                mockStubTransformersFactory,
                SIGNATURE_ALGORITHM,
                DIGEST_ALGORITHM
        );
    }

    @Test
    public void testGet_shouldReturnTransformerWhichAddsKeyInfoWhenConfiguredToSend() throws Exception {
        Idp idpSendingKeyInfo = new Idp("test-idp", "Test Idp", "test-idp-asset-id", true, ISSUER_ID, null);

        testTransformerProvider.get(idpSendingKeyInfo);

        verify(mockStubTransformersFactory).getOutboundResponseFromIdpToStringTransformer(
                ENC_KEYSTORE_DUMMY,
                PRIV_KEYSTORE_DUMMY,
                ENTITY_TO_ENCRYPT_FOR_LOCATOR,
                TEST_SIGNING_KEY.get(),
                ISSUER_ID,
                SIGNATURE_ALGORITHM,
                DIGEST_ALGORITHM
        );
    }

    @Test
    public void testGet_shouldReturnTransformerWithoutKeyInfoWhenConfiguredNotToSend() throws Exception {
        Idp idpNotSendingKeyInfo = new Idp("test-idp", "Test Idp", "test-idp-asset-id", false, ISSUER_ID, null);

        testTransformerProvider.get(idpNotSendingKeyInfo);

        verify(mockStubTransformersFactory).getOutboundResponseFromIdpToStringTransformer(
                ENC_KEYSTORE_DUMMY,
                PRIV_KEYSTORE_DUMMY,
                ENTITY_TO_ENCRYPT_FOR_LOCATOR,
                SIGNATURE_ALGORITHM,
                DIGEST_ALGORITHM
        );
    }

    @Test
    public void testGet_shouldReturnTransformerWithoutKeyInfoWhenSigningKeyIsAbsent() throws Exception {
        Idp idpNotSendingKeyInfo = new Idp("test-idp", "Test Idp", "test-idp-asset-id", false, ISSUER_ID, null);
        OutboundResponseFromIdpTransformerProvider nullPublicKeyTransformerProvider = new OutboundResponseFromIdpTransformerProvider(
                ENC_KEYSTORE_DUMMY,
                PRIV_KEYSTORE_DUMMY,
                ENTITY_TO_ENCRYPT_FOR_LOCATOR,
                Optional.empty(),
                mockStubTransformersFactory,
                SIGNATURE_ALGORITHM,
                DIGEST_ALGORITHM
        );

        nullPublicKeyTransformerProvider.get(idpNotSendingKeyInfo);

        verify(mockStubTransformersFactory).getOutboundResponseFromIdpToStringTransformer(
                ENC_KEYSTORE_DUMMY,
                PRIV_KEYSTORE_DUMMY,
                ENTITY_TO_ENCRYPT_FOR_LOCATOR,
                SIGNATURE_ALGORITHM,
                DIGEST_ALGORITHM
        );
    }
}
