package stubidp.saml.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.domain.assertions.TransactionIdaStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.StatusBuilder.aStatus;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

class TransactionIdaStatusUnmarshallerTest extends OpenSAMLRunner {

    private TransactionIdaStatusUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new TransactionIdaStatusUnmarshaller();
    }

    @Test
    void transform_shouldTransformSuccessWithNoSubCode() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode successStatusCode = samlObjectFactory.createStatusCode();
        successStatusCode.setValue(StatusCode.SUCCESS);
        originalStatus.setStatusCode(successStatusCode);

        TransactionIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(TransactionIdaStatus.Success);
    }

    @Test
    void transform_shouldTransformNoAuthenticationContext() {
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
    void transform_shouldTransformNoMatchFromHub() {
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
    void transform_shouldTransformAuthnFailed() {
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
    void transform_shouldTransformRequesterErrorFromIdpAsSentByHub() {
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
