package uk.gov.ida.rp.testrp.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.exceptions.InsecureMetadataException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.net.URI;
import java.security.KeyStore;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetadataResolverProviderTest {

    public static MsaStubRule msaStubRule = new MsaStubRule("metadata.xml");

    @AfterAll
    public static void tearDown() {
        msaStubRule.stop();
    }

    @Mock
    private TestRpConfiguration configuration;

    private static Client client = ClientBuilder.newBuilder().hostnameVerifier(new NoopHostnameVerifier()).trustStore(createKeyStore()).build();

    @BeforeEach
    public void setUp() {
        System.setProperty("https.protocols", "TLSv1.2");
    }

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsNotPresent() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:"+msaStubRule.getSecurePort()+"/metadata"));

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:"+msaStubRule.getSecurePort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test
    public void shouldThrowExceptionWhenPerformingHttpRequestWhenInsecureMetadataFlagIsFalse() {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:"+msaStubRule.getPort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(false);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertThrows(InsecureMetadataException.class, provider::get);
    }

    @Test
    public void shouldPerformHttpRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:"+msaStubRule.getPort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    private void assertCanQueryMetadata(MetadataResolverProvider provider) throws net.shibboleth.utilities.java.support.resolver.ResolverException {
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion("http://www.test-rp-ms.gov.uk/SAML2/MD"));
        EntityDescriptor entityDescriptor = provider.get().resolveSingle(criteria);
        assertNotNull(entityDescriptor);
    }


    private static KeyStore createKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(FileUtils.openInputStream(new File("test_keys/dev_service_ssl.ks")), "marshmallow".toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ks;
    }
}
