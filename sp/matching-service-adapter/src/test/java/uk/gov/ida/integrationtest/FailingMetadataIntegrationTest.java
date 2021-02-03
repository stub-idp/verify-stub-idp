package uk.gov.ida.integrationtest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.constants.Constants;
import stubidp.saml.test.builders.AttributeQueryBuilder;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.KeyInfoBuilder;
import stubidp.saml.test.builders.SPSSODescriptorBuilder;
import stubidp.saml.test.builders.X509CertificateBuilder;
import stubidp.saml.test.builders.X509DataBuilder;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.utils.httpstub.HttpStubRule;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.REQUESTER;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.integrationtest.UserAccountCreationBaseTest.MATCHING_REQUEST_PATH;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.matchingserviceadapter.saml.matchers.SignableSAMLObjectBaseMatcher.signedBy;

@ExtendWith(DropwizardExtensionsSupport.class)
public class FailingMetadataIntegrationTest {
    private static final String METADATA_PATH = "/uk/gov/ida/saml/metadata/federation";

    private static final HttpStubRule metadataServer = new HttpStubRule();
    private static final HttpStubRule localMatchingService = new HttpStubRule();

    public static final MatchingServiceAdapterAppExtension applicationRule = new MatchingServiceAdapterAppExtension(
            Map.ofEntries(
                    Map.entry("metadata.uri", "http://localhost:" + metadataServer.getPort() + METADATA_PATH),
                    Map.entry("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH)
            ));

    private final String MATCHING_SERVICE_URI = "http://localhost:" + applicationRule.getLocalPort() + "/matching-service/POST";
    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA256();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    @BeforeEach
    void setup() {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 200, "application/json", "{\"result\": \"match\"}");
    }

    @Test
    public void shouldReturnErrorResponseWhenAMatchRequestIsReceivedAndThereIsAProblemValidatingTheCertificateChainOfAHubCertificate() throws IOException, InterruptedException {
        String metadata = new MetadataFactory().metadata(Collections.singletonList(badHubEntityDescriptor()));
        metadataServer.reset();
        metadataServer.register(METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, metadata);
        refreshMetadata();

        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("anId"),
                        aMatchingDatasetAssertion(Collections.emptyList(), false, "anId")
                        ), "request-id", "hub-entity-id")
                )
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getValue()).contains("Signature was not valid");
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    private EntityDescriptor badHubEntityDescriptor() {
        X509Certificate x509CertificateOne = X509CertificateBuilder.aX509Certificate().withCert(TestCertificateStrings.UNCHAINED_PUBLIC_CERT).build();
        X509Data x509DataOne = X509DataBuilder.aX509Data().withX509Certificate(x509CertificateOne).build();
        KeyInfo signingOne = KeyInfoBuilder.aKeyInfo().withKeyName("signing_one").withX509Data(x509DataOne).build();
        KeyDescriptor keyDescriptorOne = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signingOne).build();
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addKeyDescriptor(keyDescriptorOne)
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey()
                .build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(HUB_ENTITY_ID)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .setAddDefaultSpServiceDescriptor(false)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                    .withSignature(null)
                    .withoutSigning()
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshMetadata() {
        final javax.ws.rs.core.Response response = JerseyClientBuilder.createClient().target(UriBuilder.fromUri("http://localhost")
                .path("/tasks/metadata-refresh")
                .port(applicationRule.getAdminPort())
                .build())
                .request()
                .post(Entity.text("refresh!"));
        assertThat(response.getStatus()).isEqualTo(javax.ws.rs.core.Response.Status.OK.getStatusCode());
    }
}
