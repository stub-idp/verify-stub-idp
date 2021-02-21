package unit.uk.gov.ida.verifyserviceprovider.factories.saml;

import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAttribute;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.crypto.KeySupport;
import stubidp.saml.extensions.extensions.versioning.Version;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.PrivateKeyStoreFactory;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.common.manifest.ManifestReader;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PrivateKeyStore;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.factories.EncrypterFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY;

@ExtendWith({MockitoExtension.class, DropwizardExtensionsSupport.class})
public class AuthnRequestFactoryTest extends OpenSAMLRunner {

    private static final URI DESTINATION = URI.create("http://example.com");
    private static final String SERVICE_ENTITY_ID = "http://entity-id";
    private static Encrypter encrypter;
    private static Decrypter decrypter;
    private static AuthnRequestFactory factory;

    @Mock
    private ManifestReader manifestReader;
    @Mock
    private EncrypterFactory encrypterFactory;

    @BeforeEach
    public void setUp() throws KeyException {
        reset(manifestReader);

        final BasicCredential basicCredential = createBasicCredential();
        encrypter = new stubidp.saml.security.EncrypterFactory().createEncrypter(basicCredential);
        decrypter = new DecrypterFactory().createDecrypter(ImmutableList.of(basicCredential));
        when(encrypterFactory.createEncrypter()).thenReturn(encrypter);
        PrivateKeyStore privateKeyStore = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP);
        KeyPair keyPair = new KeyPair(KeySupport.derivePublicKey(privateKeyStore.getSigningPrivateKey()), privateKeyStore.getSigningPrivateKey());
        factory = new AuthnRequestFactory(
            DESTINATION,
            keyPair,
            manifestReader,
            encrypterFactory
        );
    }

    @Test
    public void containsCorrectAttributes() throws KeyException {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);

        assertThat(authnRequest.getID()).isNotEmpty();
        assertThat(authnRequest.getIssueInstant()).isNotNull();
        assertThat(authnRequest.getDestination()).isNotEmpty();
        assertThat(authnRequest.getIssuer()).isNotNull();
        assertThat(authnRequest.getSignature()).isNotNull();
    }

    @Test
    public void shouldNotForceAuthn() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.isForceAuthn()).isFalse();
    }

    @Test
    public void signatureIDReferencesAuthnRequestID() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getSignatureReferenceID()).isEqualTo(authnRequest.getID());
    }

    @Test
    public void destinationShouldMatchConfiguredSSOLocation() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getDestination()).isEqualTo(DESTINATION.toString());
    }

    @Test
    public void issuerShouldMatchConfiguredEntityID() {
        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);
        assertThat(authnRequest.getIssuer().getValue()).isEqualTo(SERVICE_ENTITY_ID);
    }

    @Test
    public void shouldAddApplicationVersionInExtension() throws Exception {
        when(manifestReader.getAttributeValueFor(VerifyServiceProviderApplication.class, "Version")).thenReturn("some-version");

        AuthnRequest authnRequest = factory.build(SERVICE_ENTITY_ID);

        Extensions extensions = authnRequest.getExtensions();
        EncryptedAttribute encryptedAttribute = (EncryptedAttribute) extensions.getUnknownXMLObjects().get(0);

        Attribute attribute = decrypter.decrypt(encryptedAttribute);
        Version version = (Version) attribute.getAttributeValues().get(0);

        assertThat(attribute.getName()).isEqualTo("Versions");
        assertThat(version.getApplicationVersion().getValue()).isEqualTo("some-version");
    }

    @Test
    public void shouldGetVersionNumberFromManifestReader() throws IOException, KeyException {
        factory.build(SERVICE_ENTITY_ID);

        verify(manifestReader, times(1)).getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
    }

    private BasicCredential createBasicCredential() {
        final PublicKey publicKey = new PublicKeyFactory(new X509CertificateFactory()).createPublicKey(HUB_TEST_PUBLIC_ENCRYPTION_CERT);
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        return new BasicCredential(publicKey, privateKey);
    }
}