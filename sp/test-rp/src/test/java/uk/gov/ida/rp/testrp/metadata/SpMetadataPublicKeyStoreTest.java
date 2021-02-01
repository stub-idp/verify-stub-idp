package uk.gov.ida.rp.testrp.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.metadata.StringBackedMetadataResolver;
import uk.gov.ida.saml.metadata.exceptions.NoKeyConfiguredForEntityException;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static com.google.common.base.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;

public class SpMetadataPublicKeyStoreTest {

    private static MetadataResolver metadataResolver;

    @BeforeClass
    public static void setUp() throws Exception {
        metadataResolver = initializeMetadata();
    }

    private static MetadataResolver initializeMetadata() {
        try {
            InitializationService.initialize();
            String metadata = new MetadataFactory().defaultMetadata();
            StringBackedMetadataResolver stringBackedMetadataResolver = new StringBackedMetadataResolver(metadata);
            BasicParserPool basicParserPool = new BasicParserPool();
            basicParserPool.initialize();
            stringBackedMetadataResolver.setParserPool(basicParserPool);
            stringBackedMetadataResolver.setMinRefreshDelay(14400000);
            stringBackedMetadataResolver.setRequireValidMetadata(true);
            stringBackedMetadataResolver.setId("testResolver");
            stringBackedMetadataResolver.initialize();
            return stringBackedMetadataResolver;
        } catch (InitializationException | ComponentInitializationException e) {
            throw propagate(e);
        }
    }

    private static PublicKey getX509Key(String encodedCertificate) throws Base64DecodingException, CertificateException {
        byte[] derValue = Base64.decode(encodedCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(derValue));
        return certificate.getPublicKey();
    }

    @Test
    public void shouldReturnTheSigningKeysForAnEntity() throws Exception {
        SpMetadataPublicKeyStore spMetadataPublicKeyStore = new SpMetadataPublicKeyStore(metadataResolver);

        PublicKey expectedPublicKey = getX509Key(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT);
        assertThat(spMetadataPublicKeyStore.getVerifyingKeysForEntity(TestEntityIds.HUB_ENTITY_ID)).contains(expectedPublicKey);
    }

    @Test(expected = NoKeyConfiguredForEntityException.class)
    public void shouldRaiseAnExceptionWhenThereIsNoEntityDescriptor() throws Exception {
        SpMetadataPublicKeyStore spMetadataPublicKeyStore = new SpMetadataPublicKeyStore(metadataResolver);
        spMetadataPublicKeyStore.getVerifyingKeysForEntity("my-invented-entity-id");
    }
}
