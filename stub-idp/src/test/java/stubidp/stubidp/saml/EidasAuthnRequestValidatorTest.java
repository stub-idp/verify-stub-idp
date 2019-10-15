package stubidp.stubidp.saml;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.hub.hub.exception.SamlDuplicateRequestIdException;
import stubidp.saml.hub.hub.exception.SamlRequestTooOldException;
import stubidp.saml.hub.hub.exception.SamlValidationException;
import stubidp.saml.metadata.FileBackedTrustStoreConfiguration;
import stubidp.saml.metadata.MetadataConfiguration;
import stubidp.saml.metadata.MultiTrustStoresBackedMetadataConfiguration;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.metadata.test.factories.metadata.EntitiesDescriptorFactory;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.utils.Constants;
import stubidp.saml.utils.core.test.builders.metadata.EntityDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.KeyDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.SPSSODescriptorBuilder;
import stubidp.stubidp.OpenSAMLRunner;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.EuropeanIdentityConfiguration;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import stubsp.stubsp.saml.request.EidasAuthnRequestBuilder;

import javax.ws.rs.core.UriBuilder;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;

@ExtendWith(MockitoExtension.class)
class EidasAuthnRequestValidatorTest extends OpenSAMLRunner {

    private static final String CONNECTOR_METADATA_RESOURCE = "/saml/metadata/eidas/connector";
    private static final String SCHEME_ID = "cef-ref";
    private static final String hubConnectorEntityId = "https://not.a.real.entity.uk/connector";
    private static final String stubCountryBaseUri = "https://dest:0";
    private static final String stubCountryDestination = UriBuilder.fromUri(stubCountryBaseUri+Urls.EIDAS_SAML2_SSO_RESOURCE).build(SCHEME_ID).toASCIIString();
    private static final HttpStubRule eidasMetadataServer = new HttpStubRule();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource spTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("coreCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static MetadataResolver hubConnectorMetadataResolver;
    private static MetadataConfiguration metadataConfiguration;

    @Mock
    private EuropeanIdentityConfiguration europeanIdentityConfiguration;

    private EidasAuthnRequestValidator eidasAuthnRequestValidator;

    @BeforeAll
    static void beforeAll() throws MarshallingException, SignatureException, JsonProcessingException {
        metadataTrustStore.create();
        spTrustStore.create();
        eidasMetadataServer.reset();
        eidasMetadataServer.register(CONNECTOR_METADATA_RESOURCE, 200, Constants.APPLICATION_SAMLMETADATA_XML, getEidasConnectorMetadata());

        metadataConfiguration = new MultiTrustStoresBackedMetadataConfiguration(UriBuilder.fromUri("http://localhost:" + eidasMetadataServer.getPort() + CONNECTOR_METADATA_RESOURCE).build(),
                10L,
                60L,
                hubConnectorEntityId,
                JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build(),
                EidasAuthnRequestValidatorTest.class.getName(),
                "federationId",
                new TestFileBackedTrustStoreConfiguration(metadataTrustStore.getAbsolutePath(), metadataTrustStore.getPassword()),
                new TestFileBackedTrustStoreConfiguration(spTrustStore.getAbsolutePath(), spTrustStore.getPassword()),
                null);
        hubConnectorMetadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolverWithClient(metadataConfiguration,true, JerseyClientBuilder.createClient());
    }

    @AfterAll
    static void afterAll() {
        spTrustStore.delete();
        metadataTrustStore.delete();
    }

    @BeforeEach
    void beforeEach() {
        when(europeanIdentityConfiguration.getStubCountryBaseUrl()).thenReturn(stubCountryBaseUri);
        when(europeanIdentityConfiguration.getMetadata()).thenReturn(metadataConfiguration);
        eidasAuthnRequestValidator = new EidasAuthnRequestValidator(hubConnectorMetadataResolver, europeanIdentityConfiguration);
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, null));
        assertThat(exception.getMessage()).contains("Missing SAML message");
    }

    @Test
    void shouldThrowExceptionWhenRequestIsTooSmall() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, ""));
        assertThat(exception.getMessage()).contains("The size of string is 0; it should be at least 1,200.");
    }

    @Test
    void shouldThrowExceptionWhenKeyInfoIsPresentButNoX509DataAre() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .removeAllX509Datas(true)
                .withKeyStore(validIdaKeyStore())
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("no x509 data found");
    }

    @Test
    void shouldThrowExceptionWhenKeyInfoIsPresentButNoCertificatesAre() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .removeAllCertificates(true)
                .withKeyStore(validIdaKeyStore())
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("no x509 certificates found in x509 data");
    }

    @Test
    void shouldThrowExceptionWhenKeyInfoIsNull() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(false)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withKeyStore(validIdaKeyStore())
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("KeyInfo cannot be null");
    }

    @Test
    void shouldValidateAValidAuthnRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withKeyStore(validIdaKeyStore())
                .build();
        eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest);
    }

    @Test
    void shouldFailDuplicateSubmissionsOfSameRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withKeyStore(validIdaKeyStore())
                .build();
        eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest);
        SamlDuplicateRequestIdException exception = assertThrows(SamlDuplicateRequestIdException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("Duplicate request ID");
    }

    @Test
    void shouldNotValidateAValidAuthnRequestSignedWithWrongKey() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withKeyStore(invalidIdaKeyStore())
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    void shouldNotAllowAnIncorrectDestination() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(UriBuilder.fromUri(stubCountryBaseUri + Urls.EIDAS_SAML2_SSO_RESOURCE).build("foo").toASCIIString())
                .withKeyStore(validIdaKeyStore())
                .build();
        SamlValidationException exception = assertThrows(SamlValidationException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("Destination is incorrect.");
    }

    @Test
    void shouldNotValidateRequestFromUnepectedIssuer() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId("something else")
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withKeyStore(validIdaKeyStore())
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    void shouldNotAcceptExpiredRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now().minusHours(1))
                .withDestination(stubCountryDestination)
                .withKeyStore(validIdaKeyStore())
                .build();
        SamlRequestTooOldException exception = assertThrows(SamlRequestTooOldException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("too old");
    }

    private static String getEidasConnectorMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptorList = List.of(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(hubConnectorEntityId)
                .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                        .withoutDefaultEncryptionKey()
                        .withoutDefaultSigningKey()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT).build())
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT).build())
                        .build())
                .build());
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorFactory()
                .signedEntitiesDescriptor(entityDescriptorList, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        return new MetadataFactory().metadata(entitiesDescriptor);
    }

    private static class TestFileBackedTrustStoreConfiguration extends FileBackedTrustStoreConfiguration {
        TestFileBackedTrustStoreConfiguration(String store, String trustStorePassword) {
            this.store = store;
            this.trustStorePassword = trustStorePassword;
        }
    }

    private IdaKeyStore validIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Collections.singletonList(encryptionKeyPair));
    }

    private IdaKeyStore invalidIdaKeyStore() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

        PrivateKey privateSigningKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY));
        PublicKey publicSigningKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);

        PrivateKey privateEncKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncKey = publicKeyFactory.createPublicKey(TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT);

        KeyPair signingKeyPair = new KeyPair(publicSigningKey, privateSigningKey);
        KeyPair encryptionKeyPair = new KeyPair(publicEncKey, privateEncKey);

        X509Certificate certificate = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT);
        return new IdaKeyStore(certificate, signingKeyPair, Collections.singletonList(encryptionKeyPair));
    }
}