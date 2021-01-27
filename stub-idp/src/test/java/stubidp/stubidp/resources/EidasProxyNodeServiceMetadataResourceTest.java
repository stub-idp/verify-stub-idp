package stubidp.stubidp.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Document;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.security.IdaKeyStore;
import stubidp.stubidp.builders.CountryMetadataBuilder;
import stubidp.stubidp.exceptions.InvalidEidasSchemeException;
import stubidp.stubidp.resources.eidas.EidasProxyNodeServiceMetadataResource;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EidasProxyNodeServiceMetadataResourceTest {

    private EidasProxyNodeServiceMetadataResource resource;
    private static final String VALID_COUNTRY = "stub-country";
    private static final String INVALID_COUNTRY = "invalid-stub-country";
    private static final String METADATA_URL_PATTERN = "https://stub.test/{0}/ServiceMetadata";
    private static final String SSO_URL_PATTERN = "https://stub.test/eidas/{0}/SAML2/SSO";
    private EntityDescriptor entityDescriptor;
    private URI validCountryUri;

    @Mock
    private X509Certificate signingCertificate;

    @Mock
    private IdaKeyStore idaKeyStore;

    @Mock
    private CountryMetadataBuilder countryMetadataBuilder;

    @BeforeAll
    static void classSetUp() {
        IdaSamlBootstrap.bootstrap();
    }

    @BeforeEach
    void setUp() throws URISyntaxException {
        validCountryUri = new URI(MessageFormat.format(METADATA_URL_PATTERN, VALID_COUNTRY));
        resource = new EidasProxyNodeServiceMetadataResource(idaKeyStore, METADATA_URL_PATTERN, SSO_URL_PATTERN, countryMetadataBuilder);
        entityDescriptor = (EntityDescriptor) XMLObjectProviderRegistrySupport.getBuilderFactory()
          .getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME).buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME, EntityDescriptor.TYPE_NAME);
    }

    @Test
    void getShouldReturnAnErrorWhenIdpIsUnknown() {
        Assertions.assertThrows(InvalidEidasSchemeException.class, () -> resource.getMetadata(INVALID_COUNTRY));
    }

    @Test
    void getShouldReturnADocumentWhenIdpIsKnown() throws URISyntaxException, CertificateEncodingException, SignatureException, MarshallingException {
        when(idaKeyStore.getSigningCertificate()).thenReturn(signingCertificate);
        when(countryMetadataBuilder.createEntityDescriptorForProxyNodeService(any(), any(), any(), any())).thenReturn(entityDescriptor);

        final Response response = resource.getMetadata(VALID_COUNTRY);

        URI validCountrySsoUri = new URI(String.format("https://stub.test/eidas/%s/SAML2/SSO", VALID_COUNTRY));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(Document.class).isAssignableFrom(response.getEntity().getClass());
        verify(countryMetadataBuilder).createEntityDescriptorForProxyNodeService(eq(validCountryUri), eq(validCountrySsoUri), eq(signingCertificate), any());
    }

    @Test
    void getShouldReturnNotFoundWhenIdpIsNullOrEmpty() throws CertificateEncodingException, MarshallingException, SignatureException {
        Response response = resource.getMetadata(null);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(null);

        response = resource.getMetadata("");
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(null);

        verify(countryMetadataBuilder, times(0)).createEntityDescriptorForProxyNodeService(any(), any(), any(), any());
    }
}
