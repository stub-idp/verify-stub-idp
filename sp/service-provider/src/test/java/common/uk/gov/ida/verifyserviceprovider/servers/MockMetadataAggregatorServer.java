package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.SignatureBuilder;
import stubidp.saml.test.metadata.MetadataFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static stubidp.saml.metadata.ResourceEncoder.entityIdAsResource;
import static stubidp.test.devpki.PemCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;

public class MockMetadataAggregatorServer extends WireMockClassRule {

    public static final String METADATA_SOURCE_PATH = "/metadata-source";

    public MockMetadataAggregatorServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveAggregatedMetadata(String entityId) throws Exception {
        stubFor(
                get(urlEqualTo(METADATA_SOURCE_PATH + "/" + entityIdAsResource(entityId)))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(buildTestCountryEntityDescriptor(entityId))
                        )
        );
    }

    public String getUri() {
        return "http://localhost:" + port() + METADATA_SOURCE_PATH;
    }

    private String buildTestCountryEntityDescriptor(String countryEntityId) throws Exception {
        KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                .withX509ForSigning(STUB_COUNTRY_PUBLIC_PRIMARY_CERT)
                .build();

        IDPSSODescriptor idpSsoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                .withoutDefaultSigningKey()
                .addKeyDescriptor(signingKeyDescriptor)
                .build();

        Signature signature = SignatureBuilder.aSignature()
                .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withX509Data(METADATA_SIGNING_A_PUBLIC_CERT)
                .build();

        EntityDescriptor entityDescriptor = EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(countryEntityId)
                .withIdpSsoDescriptor(idpSsoDescriptor)
                .setAddDefaultSpServiceDescriptor(false)
                .withValidUntil(Instant.now().plus(2, ChronoUnit.WEEKS))
                .withSignature(signature)
                .build();

        return new MetadataFactory().singleEntityMetadata(entityDescriptor);
    }
}
