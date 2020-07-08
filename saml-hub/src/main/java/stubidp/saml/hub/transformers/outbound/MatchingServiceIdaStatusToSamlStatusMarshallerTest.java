package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.matching.MatchingServiceIdaStatus;
import stubidp.saml.utils.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchingServiceIdaStatusToSamlStatusMarshallerTest extends OpenSAMLRunner {

    private stubidp.saml.utils.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller marshaller;

    @BeforeEach
    public void setUp() throws Exception {
        marshaller = new MatchingServiceIdaStatusMarshaller(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void toSamlStatus_shouldTransformMatchingServiceMatch() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(MatchingServiceIdaStatus.MatchingServiceMatch);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
        assertThat(transformedStatus.getStatusCode().getStatusCode()).isNotNull();
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.MATCH);
    }

    @Test
    public void toSamlStatus_shouldTransformNoMatchingServiceMatch() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode()).isNotNull();
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.NO_MATCH);
    }

    @Test
    public void toSamlStatus_shouldTransformRequesterError() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(MatchingServiceIdaStatus.RequesterError);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
    }
}
