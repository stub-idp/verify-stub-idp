package uk.gov.ida.rp.testrp.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import stubidp.saml.hub.metadata.exceptions.NoKeyConfiguredForEntityException;
import stubidp.saml.security.StringBackedMetadataResolver;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpMetadataPublicKeyStoreTest {

    private static MetadataResolver metadataResolver;

    @BeforeAll
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
            stringBackedMetadataResolver.setMinRefreshDelay(Duration.ofMillis(14400000));
            stringBackedMetadataResolver.setRequireValidMetadata(true);
            stringBackedMetadataResolver.setId("testResolver");
            stringBackedMetadataResolver.initialize();
            return stringBackedMetadataResolver;
        } catch (InitializationException | ComponentInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getX509Key(String encodedCertificate) throws CertificateException {
        byte[] derValue = Base64.getMimeDecoder().decode(encodedCertificate);
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

    @Test
    public void shouldRaiseAnExceptionWhenThereIsNoEntityDescriptor() throws Exception {
        SpMetadataPublicKeyStore spMetadataPublicKeyStore = new SpMetadataPublicKeyStore(metadataResolver);
        assertThrows(NoKeyConfiguredForEntityException.class, () -> spMetadataPublicKeyStore.getVerifyingKeysForEntity("my-invented-entity-id"));
    }
}
