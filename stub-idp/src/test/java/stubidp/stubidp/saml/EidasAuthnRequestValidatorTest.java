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
import stubidp.saml.extensions.IdaSamlBootstrap;
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
import stubidp.saml.utils.Constants;
import stubidp.saml.utils.core.test.builders.metadata.EntityDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.KeyDescriptorBuilder;
import stubidp.saml.utils.core.test.builders.metadata.SPSSODescriptorBuilder;
import stubidp.stubidp.Urls;
import stubidp.stubidp.configuration.EuropeanIdentityConfiguration;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.integration.support.eidas.EidasAuthnRequestBuilder;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder;

import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;

@ExtendWith(MockitoExtension.class)
class EidasAuthnRequestValidatorTest {

    private static final String SCHEME_ID = "cef-ref";
    private static final String hubConnectorEntityId = "https://not.a.real.entity.uk/connector";
    private static final String stubCountryBaseUri = "https://dest:0";
    private static final String stubCountryDestination = UriBuilder.fromUri(stubCountryBaseUri+Urls.EIDAS_SAML2_SSO_RESOURCE).build(SCHEME_ID).toASCIIString();
    private static final String EIDAS_METADATA_PATH = "/saml/metadata/eidas/connector";
    private static final HttpStubRule eidasMetadataServer = new HttpStubRule();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource spTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("coreCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    public static MetadataResolver hubConnectorMetadataResolver;
    public static MetadataConfiguration metadataConfiguration;

    @Mock
    private EuropeanIdentityConfiguration europeanIdentityConfiguration;

    private EidasAuthnRequestValidator eidasAuthnRequestValidator;

    @BeforeAll
    public static void beforeAll() throws MarshallingException, SignatureException, JsonProcessingException {
        metadataTrustStore.create();
        spTrustStore.create();
        IdaSamlBootstrap.bootstrap();
        eidasMetadataServer.reset();
        eidasMetadataServer.register(EIDAS_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getEidasMetadata());

        metadataConfiguration = new MultiTrustStoresBackedMetadataConfiguration(UriBuilder.fromUri("http://localhost:" + eidasMetadataServer.getPort() + EIDAS_METADATA_PATH).build(),
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
    public static void afterAll() {
        spTrustStore.delete();
        metadataTrustStore.delete();
    }

    @BeforeEach
    public void beforeEach() {
        when(europeanIdentityConfiguration.getStubCountryBaseUrl()).thenReturn(stubCountryBaseUri);
        when(europeanIdentityConfiguration.getMetadata()).thenReturn(metadataConfiguration);
        eidasAuthnRequestValidator = new EidasAuthnRequestValidator(hubConnectorMetadataResolver, europeanIdentityConfiguration);
    }

    @Test
    public void shouldThrowExceptionWhenRequestIsNull() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, null));
        assertThat(exception.getMessage()).contains("Missing SAML message");
    }

    @Test
    public void shouldThrowExceptionWhenRequestIsTooSmall() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, ""));
        assertThat(exception.getMessage()).contains("The size of string is 0; it should be at least 1,200.");
    }

    @Test
    public void shouldThrowExceptionWhenKeyInfoIsPresentButNoX509DataAre() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .removeAllX509Datas(true)
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("no x509 data found");
    }

    @Test
    public void shouldThrowExceptionWhenKeyInfoIsPresentButNoCertificatesAre() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .removeAllCertificates(true)
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("no x509 certificates found in x509 data");
    }

    @Test
    public void shouldThrowExceptionWhenKeyInfoIsNull() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(false)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("KeyInfo cannot be null");
    }

    @Test
    public void shouldValidateAValidAuthnRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .build();
        eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest);
    }

    @Test
    public void shouldFailDuplicateSubmissionsOfSameRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .build();
        eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest);
        SamlDuplicateRequestIdException exception = assertThrows(SamlDuplicateRequestIdException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("Duplicate request ID");
    }

    @Test
    public void shouldNotValidateAValidAuthnRequestSignedWithWrongKey() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .withInvalidKey(true)
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    public void shouldNotAllowAnIncorrectDestination() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now())
                .withDestination(UriBuilder.fromUri(stubCountryBaseUri + Urls.EIDAS_SAML2_SSO_RESOURCE).build("foo").toASCIIString())
                .build();
        SamlValidationException exception = assertThrows(SamlValidationException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("Destination is incorrect. Expected: https://dest/eidas/cef-ref/SAML2/SSO Received: https://dest:0/eidas/foo/SAML2/SSO");
    }

    @Test
    public void shouldNotValidateRequestFromUnepectedIssuer() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId("something else")
                .withIssueInstant(DateTime.now())
                .withDestination(stubCountryDestination)
                .build();
        InvalidEidasAuthnRequestException exception = assertThrows(InvalidEidasAuthnRequestException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    public void shouldNotAcceptExpiredRequest() {
        String _authnRequest = EidasAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withIssuerEntityId(hubConnectorEntityId)
                .withIssueInstant(DateTime.now().minusHours(1))
                .withDestination(stubCountryDestination)
                .build();
        SamlRequestTooOldException exception = assertThrows(SamlRequestTooOldException.class, () -> eidasAuthnRequestValidator.transformAndValidate(SCHEME_ID, _authnRequest));
        assertThat(exception.getMessage()).contains("too old");
    }

    private static String getEidasMetadata() throws MarshallingException, SignatureException {
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
        final String metadata = new MetadataFactory().metadata(entitiesDescriptor);
        return metadata;
    }

    private static class TestFileBackedTrustStoreConfiguration extends FileBackedTrustStoreConfiguration {
        TestFileBackedTrustStoreConfiguration(String store, String trustStorePassword) {
            this.store = store;
            this.trustStorePassword = trustStorePassword;
        }
    }
}