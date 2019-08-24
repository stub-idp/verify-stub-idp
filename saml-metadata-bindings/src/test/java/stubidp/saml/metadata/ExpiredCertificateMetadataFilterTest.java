package stubidp.saml.metadata;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import stubidp.saml.metadata.test.factories.metadata.MetadataFactory;
import stubidp.saml.serializers.deserializers.OpenSamlXMLObjectUnmarshaller;
import stubidp.saml.serializers.deserializers.parser.SamlObjectParser;
import stubidp.test.devpki.TestCertificateStrings;

public class ExpiredCertificateMetadataFilterTest {

    private MetadataFactory metadataFactory = new MetadataFactory();
    private MetadataFilter metadataFilter;
    private OpenSamlXMLObjectUnmarshaller<XMLObject> unmarshaller = new OpenSamlXMLObjectUnmarshaller<>(new SamlObjectParser());

    @BeforeEach
    public void setUp() throws Exception {
        metadataFilter = new ExpiredCertificateMetadataFilter();
        InitializationService.initialize();
    }

    @Test
    public void shouldFailToFilterLoadingValidMetadataWhenSignedWithExpiredCertificate() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().plusYears(1000).getMillis());
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        Assertions.assertThrows(FilterException.class, () -> metadataFilter.filter(metadata));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldFailToFilterLoadingValidMetadataWhenSignedWithNotYetValidCertificate() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().minusYears(1000).getMillis());
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        Assertions.assertThrows(FilterException.class, () -> metadataFilter.filter(metadata));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldFilterMetadataSuccessfully() throws Exception {
        String signedMetadata = metadataFactory.signedMetadata(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT, TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        XMLObject metadata = unmarshaller.fromString(signedMetadata);
        metadata = metadataFilter.filter(metadata);
        Assertions.assertNotNull(metadata, "metadata should not have been filtered out");
    }
}
