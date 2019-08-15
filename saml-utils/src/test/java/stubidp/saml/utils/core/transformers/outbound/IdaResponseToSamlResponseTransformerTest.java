package stubidp.saml.utils.core.transformers.outbound;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import stubidp.saml.utils.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.OutboundResponseFromHub;
import stubidp.saml.utils.core.test.builders.ResponseForHubBuilder;
import stubidp.test.devpki.TestEntityIds;

import static org.assertj.core.api.Assertions.assertThat;

public class IdaResponseToSamlResponseTransformerTest extends OpenSAMLRunner {

    private IdaResponseToSamlResponseTransformer<OutboundResponseFromHub> systemUnderTest;

    @BeforeEach
    public void setup() {
        OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        systemUnderTest = new TestTransformer(openSamlXmlObjectFactory);
    }

    @Test
    public void transform_shouldTransformResponseId() throws Exception {
    	OutboundResponseFromHub idaResponse = ResponseForHubBuilder.anAuthnResponse().withResponseId("response-id").buildOutboundResponseFromHub();

        Response transformedResponse = systemUnderTest.apply(idaResponse);

        assertThat(transformedResponse.getID()).isEqualTo(idaResponse.getId());
    }

    @Test
    public void transform_shouldTransformIssueInstant() throws Exception {
        DateTime issueInstant = new DateTime(2012, 1, 2, 3, 4);
        OutboundResponseFromHub idaResponse = ResponseForHubBuilder.anAuthnResponse().withIssueInstant(issueInstant).buildOutboundResponseFromHub();
        Response transformedResponse = systemUnderTest.apply(idaResponse);

        assertThat(transformedResponse.getIssueInstant().isEqual(idaResponse.getIssueInstant())).isTrue();
    }

    @Test
    public void transform_shouldTransformInResponseToIfPresent() throws Exception {
        String inResponseTo = "id of original request";
        OutboundResponseFromHub idaResponse = ResponseForHubBuilder.anAuthnResponse().withInResponseTo(inResponseTo).buildOutboundResponseFromHub();
        Response transformedResponse = systemUnderTest.apply(idaResponse);

        assertThat(transformedResponse.getInResponseTo()).isEqualTo(inResponseTo);
    }

    @Test
    public void transform_shouldNotTransformInResponseToIfMissing() throws Exception {
    	OutboundResponseFromHub idaResponse = ResponseForHubBuilder.anAuthnResponse().withInResponseTo(null).buildOutboundResponseFromHub();
        Response transformedResponse = systemUnderTest.apply(idaResponse);

        assertThat(transformedResponse.getInResponseTo()).isNull();
    }

    @Test
    public void transform_shouldTransformIssuer() throws Exception {
        String issuer = "response issuer";
        OutboundResponseFromHub idaResponse = ResponseForHubBuilder.anAuthnResponse().withIssuerId(issuer).buildOutboundResponseFromHub();
        Response transformedResponse = systemUnderTest.apply(idaResponse);

        assertThat(transformedResponse.getIssuer().getValue()).isEqualTo(TestEntityIds.HUB_ENTITY_ID);
    }

    public static class TestTransformer extends IdaResponseToSamlResponseTransformer<OutboundResponseFromHub>{

        public TestTransformer(
                OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
            super(openSamlXmlObjectFactory);
        }

        @Override
        protected void transformAssertions(OutboundResponseFromHub originalResponse, Response transformedResponse) {
        }

        @Override
        protected Status transformStatus(OutboundResponseFromHub originalResponse) {
            return null;
        }

        @Override
        protected void transformDestination(OutboundResponseFromHub originalResponse, Response transformedResponse) {
        }
    }
}
