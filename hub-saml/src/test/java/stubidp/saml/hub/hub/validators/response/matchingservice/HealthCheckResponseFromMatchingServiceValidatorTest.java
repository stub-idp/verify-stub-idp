package stubidp.saml.hub.hub.validators.response.matchingservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;

import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;
import static stubidp.saml.test.builders.StatusBuilder.aStatus;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

public class HealthCheckResponseFromMatchingServiceValidatorTest extends OpenSAMLRunner {

    private HealthCheckResponseFromMatchingServiceValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        validator = new HealthCheckResponseFromMatchingServiceValidator();
    }

    @Test
    public void validate_shouldThrowExceptionIfIdIsMissing() throws Exception {
        Response response = aResponse().withId(null).build();

        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.missingId(), response);
    }

    @Test
    public void validate_shouldThrowInvalidSamlExceptionIfIssuerElementIsMissing() throws Exception {
        Response response = aResponse().withIssuer(null).build();
        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.missingIssuer(), response);
    }

    @Test
    public void validate_shouldThrowInvalidSamlExceptionIfIssuerIdIsMissing() throws Exception {
        Response response = aResponse().withIssuer(anIssuer().withIssuerId(null).build()).build();
        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.emptyIssuer(), response);
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfResponseDoesNotContainASignature() throws Exception {
        Response response = aResponse().withoutSignatureElement().build();

        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.missingSignature(), response);
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfResponseIsNotSigned() throws Exception {
        Response response = aResponse().withoutSigning().build();

        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.signatureNotSigned(), response);
    }

    @Test
    public void validateIssuer_shouldThrowExceptionIfFormatAttributeHasInvalidValue() throws Exception {
        String invalidFormat = "goo";
        Response response = aResponse().withIssuer(anIssuer().withFormat(invalidFormat).build()).build();

        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.illegalIssuerFormat(invalidFormat, NameIDType.ENTITY), response);
    }

    @Test
    public void validateResponse_shouldThrowExceptionIfThereIsNoInResponseToAttribute() throws Exception {
        Response response = aResponse().withInResponseTo(null).build();

        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.missingInResponseTo(), response);
    }

    @Test
    public void validate_shouldThrowExceptionIfSuccessResponseDoesNotContainSubStatusHealthy() throws Exception {
        final String subStatusValue = "something-other-than-healthy";
        Response response = buildResponseFromMatchingServiceWithStatusAndSubStatus(StatusCode.SUCCESS, subStatusValue);
        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.invalidSubStatusCode(subStatusValue, StatusCode.SUCCESS), response);
    }

    @Test
    public void validate_shouldThrowExceptionIfAResponderStatusIsNotSuccess() throws Exception {
        final String statusValue = "some-invalid-status";
        Response response = buildResponseFromMatchingServiceWithStatusAndSubStatus(statusValue, SamlStatusCode.HEALTHY);
        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.invalidStatusCode(statusValue), response);
    }

    @Test
    public void validate_shouldThrowExceptionIfSubStatusIsNull() throws Exception {
        Response response =
                aResponse().withStatus(
                        aStatus().withStatusCode(
                                aStatusCode().withValue(StatusCode.SUCCESS)
                                        .build()
                        ).build()
                ).build();
        assertValidationFailureSamlExceptionMessage(SamlTransformationErrorFactory.missingSubStatus(), response);
    }

    @Test
    public void validateResponse_shouldDoNothingIfStatusIsRequesterErrorAndHasNoSubStatus() throws Exception {
        Response response = aResponse().withNoDefaultAssertion().withStatus(
                aStatus().withStatusCode(
                        aStatusCode().withValue(StatusCode.REQUESTER).withSubStatusCode(null
                        ).build()
                ).build()
        ).build();
        validator.validate(response);
    }

    private Response buildResponseFromMatchingServiceWithStatusAndSubStatus(String status, String subStatus) throws Exception {
        return
                aResponse().withNoDefaultAssertion().withStatus(
                        aStatus().withStatusCode(
                                aStatusCode().withValue(status).withSubStatusCode(
                                        aStatusCode().withValue(subStatus).build()
                                ).build()
                        ).build()
                ).build();
    }

    private void assertValidationFailureSamlExceptionMessage(SamlValidationSpecificationFailure failure, final Response response) {
        SamlTransformationErrorManagerTestHelper.validateFail(
            () -> validator.validate(response),
            failure
        );
    }
}
