package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.assertions.TransactionIdaStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionIdaStatusMarshallerTest extends OpenSAMLRunner {

    private TransactionIdaStatusMarshaller marshaller;

    @BeforeEach
    public void setUp() throws Exception {
        marshaller = new TransactionIdaStatusMarshaller(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void toSamlStatus_shouldTransformSuccess() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.Success);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
    }

    @Test
    public void toSamlStatus_shouldTransformNoAuthenticationContext() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.NoAuthenticationContext);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.NO_AUTHN_CONTEXT);
    }

    @Test
    public void toSamlStatus_shouldTransformAuthnFailedWithNoSubStatus() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.AuthenticationFailed);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.AUTHN_FAILED);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getStatusCode()).isNull();
    }

    @Test
    public void toSamlStatus_shouldTransformRequesterError() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.RequesterError);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
    }

    @Test
    public void toSamlStatus_shouldTransformNoMatchingServiceMatchMayRetry() throws Exception {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.NoMatchingServiceMatchFromHub);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
        assertThat(transformedStatus.getStatusCode().getStatusCode()).isNotNull();
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.NO_MATCH);
    }
}
