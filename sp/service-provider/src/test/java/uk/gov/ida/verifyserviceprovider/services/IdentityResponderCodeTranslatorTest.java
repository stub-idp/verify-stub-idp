package uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

public class IdentityResponderCodeTranslatorTest extends OpenSAMLRunner {

    private final IdentityResponderCodeTranslator responderResponseTranslator = new IdentityResponderCodeTranslator();

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithNoSubStatusCode() {
        StatusCode statusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> responderResponseTranslator.translateResponderCode(statusCode))
                .withMessage("Missing status code for non-Success response");
    }

    @Test
    public void shouldReturnScenarioNoAuthenticationWhenNoAuthnContextStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AUTHN_CONTEXT).build())
                .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.NO_AUTHENTICATION);
    }

    @Test
    public void shouldReturnScenarioAuthenticationFailedWhenAuthnFailedStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.AUTHN_FAILED).build())
                .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.AUTHENTICATION_FAILED);
    }

    @Test
    public void shouldReturnScenarioRequestErrorWhenRequesterStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.REQUESTER).build())
                .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.REQUEST_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithUnrecognisedStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AVAILABLE_IDP).build())
                .build();
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> responderResponseTranslator.translateResponderCode(statusCode))
                .withMessage("Unknown SAML sub-status: urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP");
    }

}