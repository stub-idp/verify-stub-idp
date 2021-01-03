package stubidp.saml.utils.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.domain.matching.UnknownUserCreationIdaStatus;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class UnknownUserCreationIdaStatusMarshallerTest extends OpenSAMLRunner {

    private UnknownUserCreationIdaStatusMarshaller unknownUserCreationIdaStatusToSamlStatusMarshaller;

    @BeforeEach
    void setUp() {
        unknownUserCreationIdaStatusToSamlStatusMarshaller = new UnknownUserCreationIdaStatusMarshaller(new OpenSamlXmlObjectFactory());
    }

    @Test
    void transform_shouldTransformUnknownUserCreationSuccess() {

        Status transformedStatus = unknownUserCreationIdaStatusToSamlStatusMarshaller.toSamlStatus(UnknownUserCreationIdaStatus.Success);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.SUCCESS);
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.CREATED);
    }

    @Test
    void transform_shouldTransformUnknownUserCreationFailure() {

        Status transformedStatus = unknownUserCreationIdaStatusToSamlStatusMarshaller.toSamlStatus(UnknownUserCreationIdaStatus.CreateFailure);

        assertThat(transformedStatus.getStatusCode().getValue()).isEqualTo(StatusCode.RESPONDER);
        assertThat(transformedStatus.getStatusCode().getStatusCode()).isNotNull();
        assertThat(transformedStatus.getStatusCode().getStatusCode().getValue()).isEqualTo(SamlStatusCode.CREATE_FAILURE);
    }
}
