package stubidp.saml.hub.validators.response.matchingservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.validators.assertion.IdentityProviderAssertionValidator;
import stubidp.saml.security.validators.ValidatedAssertions;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.devpki.TestEntityIds;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;
import static stubidp.saml.test.builders.StatusBuilder.aStatus;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

@ExtendWith(MockitoExtension.class)
class ResponseAssertionsFromMatchingServiceValidatorTest extends OpenSAMLRunner {

    @Mock
    private IdentityProviderAssertionValidator assertionValidator;

    private ResponseAssertionsFromMatchingServiceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ResponseAssertionsFromMatchingServiceValidator(assertionValidator, TestEntityIds.HUB_ENTITY_ID);
    }

    @Test
    void validate_shouldThrowExceptionIfAssertionDoesNotContainAnAuthnContext() throws Exception {
        String requestId = "some-request-id";
        final Response response = aResponse()
                .withInResponseTo(requestId)
                .build();
        final Assertion assertion = anAssertion().addAuthnStatement(anAuthnStatement().withAuthnContext(null).build()).buildUnencrypted();

        validateThrows(response, assertion, SamlTransformationErrorFactory.authnContextMissingError());
    }

    @Test
    void validate_shouldThrowExceptionIfAssertionDoesNotContainAnAuthnStatement() throws Exception {
        String requestId = "some-request-id";
        final Response response = aResponse()
                .withInResponseTo(requestId)
                .build();
        validateThrows(response, anAssertion().buildUnencrypted(), SamlTransformationErrorFactory.missingAuthnStatement());
    }

    @Test
    void validate_shouldNotThrowExceptionIfResponseIsANoMatch() throws Exception {
        String requestId = "some-request-id";
        final Response response = aResponse()
                .withStatus(aStatus().withStatusCode(aStatusCode().withValue(StatusCode.RESPONDER).build()).build())
                .withInResponseTo(requestId)
                .build();

        validator.validate(new ValidatedResponse(response), new ValidatedAssertions(singletonList(anAssertion().buildUnencrypted())));
    }

    private void validateThrows(Response response, Assertion assertion, SamlValidationSpecificationFailure samlValidationSpecificationFailure) {
        final SamlTransformationErrorException e = Assertions.assertThrows(SamlTransformationErrorException.class, () -> validator.validate(new ValidatedResponse(response), new ValidatedAssertions(singletonList(assertion))));
        assertThat(e.getMessage()).isEqualTo(samlValidationSpecificationFailure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(samlValidationSpecificationFailure.getLogLevel());
    }
}
