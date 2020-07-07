package stubidp.stubidp.saml;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.glassfish.jersey.client.JerseyClientBuilder;
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
import stubidp.saml.test.metadata.EntitiesDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.saml.stubidp.configuration.SamlConfiguration;
import stubidp.saml.utils.Constants;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.SPSSODescriptorBuilder;
import stubidp.stubidp.Urls;
import stubidp.stubidp.exceptions.InvalidAuthnRequestException;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.integration.support.IdpAuthnRequestBuilder;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.rest.jerseyclient.JerseyClientConfigurationBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;

@ExtendWith(MockitoExtension.class)
class IdpAuthnRequestValidatorTest {

    private static final String IDP_NAME = "stub-idp-one";
    private static final String hubEntityId = "https://not.a.real.entity.uk/hub";
    private static final URI stubIdpBaseUri = UriBuilder.fromUri("https://dest:0").build();
    private static final String stubIdpDestination = UriBuilder.fromUri(stubIdpBaseUri+Urls.IDP_SAML2_SSO_RESOURCE).build(IDP_NAME).toASCIIString();
    private static final String HUB_METADATA_PATH = "/saml/metadata/sp";
    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource spTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("coreCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    public static MetadataResolver hubMetadataResolver;
    public static MetadataConfiguration metadataConfiguration;

    @Mock
    private SamlConfiguration samlConfiguration;

    private IdpAuthnRequestValidator idpAuthnRequestValidator;

    @BeforeAll
    public static void beforeAll() throws MarshallingException, SignatureException, JsonProcessingException {
        metadataTrustStore.create();
        spTrustStore.create();
        IdaSamlBootstrap.bootstrap();
        verifyMetadataServer.reset();
        verifyMetadataServer.register(HUB_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getMetadata());

        metadataConfiguration = new MultiTrustStoresBackedMetadataConfiguration(UriBuilder.fromUri("http://localhost:" + verifyMetadataServer.getPort() + HUB_METADATA_PATH).build(),
                Duration.ofSeconds(10L),
                Duration.ofSeconds(60L),
                hubEntityId,
                JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build(),
                IdpAuthnRequestValidatorTest.class.getName(),
                "federationId",
                new TestFileBackedTrustStoreConfiguration(metadataTrustStore.getAbsolutePath(), metadataTrustStore.getPassword()),
                new TestFileBackedTrustStoreConfiguration(spTrustStore.getAbsolutePath(), spTrustStore.getPassword()),
                null);
        hubMetadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolverWithClient(metadataConfiguration,true, JerseyClientBuilder.createClient());
    }

    @AfterAll
    public static void afterAll() {
        spTrustStore.delete();
        metadataTrustStore.delete();
    }

    @BeforeEach
    public void beforeEach() {
        when(samlConfiguration.getExpectedDestinationHost()).thenReturn(stubIdpBaseUri);
        idpAuthnRequestValidator = new IdpAuthnRequestValidator(hubMetadataResolver, metadataConfiguration, samlConfiguration);
    }

    @Test
    public void shouldThrowExceptionWhenRequestIsNull() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, null));
        assertThat(exception.getMessage()).contains("Missing SAML message");
    }

    @Test
    public void shouldThrowExceptionWhenRequestIsTooSmall() {
        SamlTransformationErrorException exception = assertThrows(SamlTransformationErrorException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, ""));
        assertThat(exception.getMessage()).contains("The size of string is 0; it should be at least 1,200.");
    }

    @Test
    public void shouldThrowExceptionWhenKeyInfoIsNotNull() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withKeyInfo(true)
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now())
                .withDestination(stubIdpDestination)
                .build();
        InvalidAuthnRequestException exception = assertThrows(InvalidAuthnRequestException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("KeyInfo was not null");
    }

    @Test
    public void shouldValidateAValidAuthnRequest() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now())
                .withDestination(stubIdpDestination)
                .build();
        idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest);
    }

    @Test
    public void shouldFailDuplicateSubmissionsOfSameRequest() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now())
                .withDestination(stubIdpDestination)
                .build();
        idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest);
        SamlDuplicateRequestIdException exception = assertThrows(SamlDuplicateRequestIdException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).contains("Duplicate request ID");
    }

    @Test
    public void shouldNotValidateAValidAuthnRequestSignedWithWrongKey() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now())
                .withDestination(stubIdpDestination)
                .withInvalidKey(true)
                .build();
        InvalidAuthnRequestException exception = assertThrows(InvalidAuthnRequestException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    public void shouldNotAllowAnIncorrectDestination() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now())
                .withDestination(UriBuilder.fromUri(stubIdpBaseUri + Urls.IDP_SAML2_SSO_RESOURCE).build("foo").toASCIIString())
                .build();
        SamlValidationException exception = assertThrows(SamlValidationException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).contains("Destination is incorrect.");
    }

    @Test
    public void shouldNotValidateRequestFromUnepectedIssuer() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId("something else")
                .withIssueInstant(Instant.now())
                .withDestination(stubIdpDestination)
                .build();
        InvalidAuthnRequestException exception = assertThrows(InvalidAuthnRequestException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).isEqualTo("signature verification failed");
    }

    @Test
    public void shouldNotAcceptExpiredRequest() {
        String _authnRequest = IdpAuthnRequestBuilder.anAuthnRequest()
                .withEntityId(hubEntityId)
                .withIssueInstant(Instant.now().atZone(ZoneId.of("UTC")).minusHours(1).toInstant())
                .withDestination(stubIdpDestination)
                .build();
        SamlRequestTooOldException exception = assertThrows(SamlRequestTooOldException.class, () -> idpAuthnRequestValidator.transformAndValidate(IDP_NAME, _authnRequest));
        assertThat(exception.getMessage()).contains("too old");
    }

    private static String getMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptorList = List.of(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(hubEntityId)
                .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                        .withoutDefaultEncryptionKey()
                        .withoutDefaultSigningKey()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT).build())
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT).build())
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