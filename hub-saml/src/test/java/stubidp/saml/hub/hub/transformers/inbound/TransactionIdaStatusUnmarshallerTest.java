package stubidp.saml.hub.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.hub.core.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.TransactionIdaStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.utils.core.test.builders.StatusBuilder.aStatus;
import static stubidp.saml.utils.core.test.builders.StatusCodeBuilder.aStatusCode;

public class TransactionIdaStatusUnmarshallerTest extends OpenSAMLRunner {

    private TransactionIdaStatusUnmarshaller unmarshaller;

    @BeforeEach
    public void setUp() throws Exception {
        unmarshaller = new TransactionIdaStatusUnmarshaller();
    }

    @Test
    public void transform_shouldTransformSuccessWithNoSubCode() throws Exception {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode successStatusCode = samlObjectFactory.createStatusCode();
        successStatusCode.setValue(StatusCode.SUCCESS);
        originalStatus.setStatusCode(successStatusCode);

        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.Success);
    }

    @Test
    public void transform_shouldTransformNoAuthenticationContext() throws Exception {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(StatusCode.NO_AUTHN_CONTEXT);
        topLevelStatusCode.setStatusCode(subStatusCode);
        originalStatus.setStatusCode(topLevelStatusCode);

        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.NoAuthenticationContext);
    }

    @Test
    public void transform_shouldTransformNoMatchFromHub() throws Exception {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.SUCCESS);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(SamlStatusCode.NO_MATCH);
        topLevelStatusCode.setStatusCode(subStatusCode);
        originalStatus.setStatusCode(topLevelStatusCode);

        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.NoMatchingServiceMatchFromHub);
    }

    @Test
    public void transform_shouldTransformAuthnFailed() throws Exception {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        status.setStatusCode(topLevelStatusCode);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(StatusCode.AUTHN_FAILED);
        topLevelStatusCode.setStatusCode(subStatusCode);
        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.AuthenticationFailed);
    }

    @Test
    public void transform_shouldTransformRequesterErrorFromIdpAsSentByHub() throws Exception {
        Status status =
            aStatus()
                .withStatusCode(
                    aStatusCode()
                        .withValue(StatusCode.RESPONDER)
                        .withSubStatusCode(
                            aStatusCode()
                            .withValue(StatusCode.REQUESTER)
                            .build()
                        )
                        .build())
                .build();


        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.RequesterError);
    }
}
