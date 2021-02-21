package uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

public class MatchingResponderCodeTranslatorTest extends OpenSAMLRunner {

    private final MatchingResponderCodeTranslator msaAssertionService = new MatchingResponderCodeTranslator();

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithNoSubStatusCode() {
        StatusCode statusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionService.translateResponderCode(statusCode))
                .withMessage("Missing status code for non-Success response");
    }

    @Test
    public void shouldReturnScenarioCancelledWhenNoAuthnContextStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AUTHN_CONTEXT).build())
                .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.CANCELLATION);
    }

    @Test
    public void shouldReturnScenarioNoMatchWhenNoMatchStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(SamlStatusCode.NO_MATCH).build())
                .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.NO_MATCH);
    }

    @Test
    public void shouldReturnScenarioAuthenticationFailedWhenAuthnFailedStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.AUTHN_FAILED).build())
                .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.AUTHENTICATION_FAILED);
    }

    @Test
    public void shouldReturnScenarioRequestErrorWhenRequesterStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.REQUESTER).build())
                .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.REQUEST_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithUnrecognisedStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AVAILABLE_IDP).build())
                .build();
        assertThatExceptionOfType(SamlResponseValidationException.class)
                .isThrownBy(() -> msaAssertionService.translateResponderCode(statusCode))
                .withMessage("Unknown SAML sub-status: urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP");
    }
}
