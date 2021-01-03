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

class SimpleProfileTransactionIdaStatusMarshallerTest extends OpenSAMLRunner {

    private SimpleProfileTransactionIdaStatusMarshaller marshaller;

    @BeforeEach
    void setUp() {
        marshaller = new SimpleProfileTransactionIdaStatusMarshaller(new OpenSamlXmlObjectFactory());
    }

    @Test
    void toSamlStatus_shouldTransformSuccess() {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.Success);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
    }

    @Test
    void toSamlStatus_shouldTransformNoAuthenticationContext() {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.NoAuthenticationContext);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.NO_AUTHN_CONTEXT);
    }

    @Test
    void toSamlStatus_shouldTransformAuthnFailedWithNoSubStatus() {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.AuthenticationFailed);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.AUTHN_FAILED);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getStatusCode()).isNull();
    }

    @Test
    void toSamlStatus_shouldTransformRequesterError() {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.RequesterError);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
    }

    @Test
    void toSamlStatus_shouldTransformNoMatchingServiceMatchMayRetry() {
        Status transformedStatus = marshaller.toSamlStatus(TransactionIdaStatus.NoMatchingServiceMatchFromHub);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode()).isNotNull();
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.NO_MATCH);
    }
}
