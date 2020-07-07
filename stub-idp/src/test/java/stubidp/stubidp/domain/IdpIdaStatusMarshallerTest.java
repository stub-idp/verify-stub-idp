package stubidp.stubidp.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.extensions.extensions.StatusValue;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class IdpIdaStatusMarshallerTest extends OpenSAMLRunner {

    private static final IdpIdaStatusMarshaller marshaller = new IdpIdaStatusMarshaller(new OpenSamlXmlObjectFactory());

    @BeforeEach
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void transform_shouldTransformSuccess() {
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.success());

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
    }

    @Test
    public void transform_shouldTransformNoAuthenticationContext() {
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.noAuthenticationContext());

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.NO_AUTHN_CONTEXT);
    }

    @Test
    public void transform_shouldTransformAuthenticationPending() {
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.authenticationPending());
        StatusValue actual = (StatusValue) transformedStatus.getStatusDetail().getOrderedChildren().get(0);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.NO_AUTHN_CONTEXT);
        assertThat(actual.getValue()).isEqualTo(StatusValue.PENDING);
    }

    @Test
    public void transform_shouldTransformAuthnFailedWithNoSubStatus() {
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.authenticationFailed());

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(StatusCode.AUTHN_FAILED);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getStatusCode()).isNull();
    }

    @Test
    public void transform_shouldTransformRequesterError() {
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.requesterError());

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
    }

    @Test
    public void transform_shouldTransformRequesterErrorWithMessage() {
        String message = "Oh dear";
        Status transformedStatus = marshaller.toSamlStatus(IdpIdaStatus.requesterError(Optional.of(message)));

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.REQUESTER);
        assertThat(transformedStatus.getStatusMessage().getValue()).isEqualTo(message);
    }

    @Test
    public void shouldMarshallStatusDetailElementWhenInCancelStatus() {
        Status status = marshaller.toSamlStatus(IdpIdaStatus.authenticationCancelled());

        assertThat(status.getStatusDetail()).isNotNull();
    }

    @Test
    public void shouldMarshallStatusDetailWithStatusValueContainingAuthnCancelInCaseOfAuthenticationCancelled() {
        Status status = marshaller.toSamlStatus(IdpIdaStatus.authenticationCancelled());

        StatusValue actual = (StatusValue) status.getStatusDetail().getOrderedChildren().get(0);

        assertThat(actual.getNamespaces()).isEmpty();
        assertThat(actual.getValue()).isEqualTo(StatusValue.CANCEL);
    }

    @Test
    public void shouldMarshallStatusDetailWithStatusValueContainingUpliftFailed() {
        Status status = marshaller.toSamlStatus(IdpIdaStatus.upliftFailed());

        StatusValue actual = (StatusValue) status.getStatusDetail().getOrderedChildren().get(0);

        assertThat(actual.getNamespaces()).isEmpty();
        assertThat(actual.getValue()).isEqualTo(StatusValue.UPLIFT_FAILED);
    }

    @Test
    public void shouldMarshallStatusDetailWithASingleStatusValueElementInCaseOfAuthenticationCancelled() {
        Status status = marshaller.toSamlStatus(IdpIdaStatus.authenticationCancelled());

        assertThat(status.getStatusDetail().getOrderedChildren()).hasSize(1);
    }
}
