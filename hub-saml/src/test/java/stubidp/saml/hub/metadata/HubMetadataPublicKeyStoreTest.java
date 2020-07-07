package stubidp.saml.hub.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import stubidp.saml.hub.metadata.exceptions.HubEntityMissingException;
import stubidp.saml.security.PublicKeyFactory;
import stubidp.saml.security.StringBackedMetadataResolver;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HubMetadataPublicKeyStoreTest extends OpenSAMLRunner {
    private static MetadataResolver metadataResolver;
    private static MetadataResolver invalidMetadataResolver;
    private static MetadataResolver emptyMetadataResolver;

    @BeforeAll
    public static void setUp() throws Exception {
        MetadataFactory metadataFactory = new MetadataFactory();
        metadataResolver = initializeMetadata(metadataFactory.defaultMetadata());
        invalidMetadataResolver = initializeMetadata(metadataFactory.expiredMetadata());
        emptyMetadataResolver = initializeMetadata(metadataFactory.emptyMetadata());
    }

    private static MetadataResolver initializeMetadata(String xml) throws ComponentInitializationException {
        AbstractReloadingMetadataResolver metadataResolver = new StringBackedMetadataResolver(xml);
        BasicParserPool basicParserPool = new BasicParserPool();
        basicParserPool.initialize();
        metadataResolver.setParserPool(basicParserPool);
        metadataResolver.setId("testResolver");
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.initialize();
        return metadataResolver;
    }

    private static PublicKey getX509Key(String encodedCertificate) throws CertificateException {
        byte[] derValue = Base64.getMimeDecoder().decode(encodedCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(derValue));
        return certificate.getPublicKey();
    }

    @Test
    public void shouldReturnTheSigningPublicKeysForTheHub() throws Exception {
        HubMetadataPublicKeyStore hubMetadataPublicKeyStore = new HubMetadataPublicKeyStore(metadataResolver, new PublicKeyFactory(), TestEntityIds.HUB_ENTITY_ID);
        List<PublicKey> verifyingKeysForEntity = hubMetadataPublicKeyStore.getVerifyingKeysForEntity();
        assertThat(verifyingKeysForEntity).containsOnly(getX509Key(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT), getX509Key(TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT));
    }

    @Test
    public void shouldErrorWhenMetadataIsInvalid() throws Exception {
        HubMetadataPublicKeyStore hubMetadataPublicKeyStore = new HubMetadataPublicKeyStore(invalidMetadataResolver, new PublicKeyFactory(), TestEntityIds.HUB_ENTITY_ID);
        final HubEntityMissingException e = Assertions.assertThrows(HubEntityMissingException.class, hubMetadataPublicKeyStore::getVerifyingKeysForEntity);
        assertThat(e).hasMessage("The HUB entity-id: \"https://signin.service.gov.uk\" could not be found in the metadata. Metadata could be expired, invalid, or missing entities");
    }

    @Test
    public void shouldErrorWhenMetadataIsEmpty() throws Exception {
        HubMetadataPublicKeyStore hubMetadataPublicKeyStore = new HubMetadataPublicKeyStore(emptyMetadataResolver, new PublicKeyFactory(), TestEntityIds.HUB_ENTITY_ID);
        final HubEntityMissingException e = Assertions.assertThrows(HubEntityMissingException.class, hubMetadataPublicKeyStore::getVerifyingKeysForEntity);
        assertThat(e).hasMessage("The HUB entity-id: \"https://signin.service.gov.uk\" could not be found in the metadata. Metadata could be expired, invalid, or missing entities");
    }

}
