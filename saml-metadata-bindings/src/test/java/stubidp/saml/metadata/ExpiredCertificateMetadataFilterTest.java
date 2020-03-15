package stubidp.saml.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.serializers.deserializers.OpenSamlXMLObjectUnmarshaller;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.test.devpki.TestCertificateStrings;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class ExpiredCertificateMetadataFilterTest {

    private MetadataFactory metadataFactory = new MetadataFactory();
    private MetadataFilter metadataFilter;
    private OpenSamlXMLObjectUnmarshaller<XMLObject> unmarshaller = new OpenSamlXMLObjectUnmarshaller<>(new SamlObjectParser());

    @BeforeAll
    public static void setUp() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldFailToFilterLoadingValidMetadataWhenSignedWithExpiredCertificate() {
        Clock clock = Clock.fixed(Instant.now().atZone(ZoneId.of("UTC")).plusYears(1000).toInstant(), ZoneId.of("UTC"));
        metadataFilter = new ExpiredCertificateMetadataFilter(clock);
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        Assertions.assertThrows(FilterException.class, () -> metadataFilter.filter(metadata, null));
    }

    @Test
    public void shouldFailToFilterLoadingValidMetadataWhenSignedWithNotYetValidCertificate() {
        Clock clock = Clock.fixed(Instant.now().atZone(ZoneId.of("UTC")).plusYears(1000).toInstant(), ZoneId.of("UTC"));
        metadataFilter = new ExpiredCertificateMetadataFilter(clock);
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        Assertions.assertThrows(FilterException.class, () -> metadataFilter.filter(metadata, null));
    }

    @Test
    public void shouldFilterMetadataSuccessfully() throws Exception {
        metadataFilter = new ExpiredCertificateMetadataFilter();
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        metadata = metadataFilter.filter(metadata, null);
        Assertions.assertNotNull(metadata, "metadata should not have been filtered out");
    }
}
