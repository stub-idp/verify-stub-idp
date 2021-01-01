package stubidp.saml.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.security.exception.SamlFailedToDecryptException;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.EncryptedAssertionBuilder;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.EncryptedAssertionBuilder.anEncryptedAssertionBuilder;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;

public class AssertionDecrypterTest extends OpenSAMLRunner {

    private final String assertionId = "test-assertion";
    private IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever;
    private AssertionDecrypter assertionDecrypter;

    @BeforeEach
    public void setup() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(
                TestEntityIds.HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(
                new IdaKeyStore(new KeyPair(publicKey, privateKey), Collections.singletonList(encryptionKeyPair))
        );
        List<Credential> credentials = keyStoreCredentialRetriever.getDecryptingCredentials();
        Decrypter decrypter = new DecrypterFactory().createDecrypter(credentials);
        assertionDecrypter = new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);
    }

    @Test
    public void shouldConvertEncryptedAssertionIntoAssertion() throws Exception {
        final Response response = responseForAssertion(anEncryptedAssertionBuilder().withPublicEncryptionCert(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT).withId(assertionId).build());
        final List<Assertion> assertions = assertionDecrypter.decryptAssertions(new ValidatedResponse(response));
        assertThat(assertions.get(0).getID()).isEqualTo(assertionId);
    }

    @Test
    public void throwsExceptionIfCannotDecryptAssertions() throws MarshallingException, SignatureException {
        final EncryptedAssertion badlyEncryptedAssertion = anEncryptedAssertionBuilder().withId(assertionId).withEncrypterCredential(
                new TestCredentialFactory(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT, null).getEncryptingCredential()).build();

        final Response response = responseForAssertion(badlyEncryptedAssertion);

        Assertions.assertThrows(SamlFailedToDecryptException.class, () -> assertionDecrypter.decryptAssertions(new ValidatedResponse(response)));
    }

    private Response responseForAssertion(EncryptedAssertion encryptedAssertion) throws MarshallingException, SignatureException {
        return aResponse()
                .withSigningCredential(keyStoreCredentialRetriever.getSigningCredential())
                .withIssuer(anIssuer().withIssuerId(TestEntityIds.STUB_IDP_ONE).build())
                .addEncryptedAssertion(encryptedAssertion)
                .build();
    }
}
