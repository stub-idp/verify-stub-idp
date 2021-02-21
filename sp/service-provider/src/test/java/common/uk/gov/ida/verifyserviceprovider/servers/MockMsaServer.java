package common.uk.gov.ida.verifyserviceprovider.servers;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.test.metadata.EntityDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static stubidp.saml.test.builders.EntitiesDescriptorBuilder.anEntitiesDescriptor;
import static stubidp.test.devpki.TestEntityIds.TEST_RP_MS;

public class  MockMsaServer extends WireMockClassRule {

    public static final String MSA_ENTITY_ID = TEST_RP_MS;

    public static String msaMetadata() {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor(MSA_ENTITY_ID);
        try {
            return new MetadataFactory().metadata(anEntitiesDescriptor()
                    .withEntityDescriptors(ImmutableList.of(entityDescriptor))
                    .withValidUntil(Instant.now().plus(14, ChronoUnit.DAYS)).build());
        } catch (MarshallingException | SignatureException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }

    }

    public MockMsaServer() {
        super(wireMockConfig().dynamicPort());
    }

    public void serveDefaultMetadata() {
        stubFor(
            get(urlEqualTo("/matching-service/metadata"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(msaMetadata())
                )
        );
    }

    public String getUri() {
        return String.format("http://localhost:%s/matching-service/metadata", this.port());
    }
}
