package stubidp.saml.metadata;

import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;
import stubidp.saml.metadata.EidasMetadataConfiguration;
import stubidp.saml.metadata.MetadataResolverConfigBuilder;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import stubidp.saml.metadata.ResourceEncoder;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataResolverConfigBuilderTest {

    private final MetadataResolverConfigBuilder testBuilder = new MetadataResolverConfigBuilder();
    private final JWK mockTrustAnchor = mock(JWK.class);
    private final EidasMetadataConfiguration mockConfiguration = mock(EidasMetadataConfiguration.class);

    @Test
    public void shouldConcatenateMetadataSourceUriAndMetadataEntityIdIntoEncodedFullUri() throws CertificateException, UnsupportedEncodingException {
        String entityId = "https://example.com/ServiceMetadata";
        when(mockTrustAnchor.getKeyID()).thenReturn(entityId);

        when(mockConfiguration.getMetadataSourceUri()).thenReturn(UriBuilder.fromUri("https://source.com").build());

        MetadataResolverConfiguration metadataResolverConfiguration =
                testBuilder.createMetadataResolverConfiguration(mockTrustAnchor, mockConfiguration);
        URI targetUri = UriBuilder.fromUri("https://source.com/" + ResourceEncoder.entityIdAsResource(entityId)).build();
        assertThat(metadataResolverConfiguration.getUri()).isEqualTo(targetUri);
    }
}
