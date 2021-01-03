package stubidp.saml.hub.validators.response.idp.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.transformers.inbound.SamlStatusToIdaStatusCodeMapper;
import stubidp.saml.hub.validators.response.helpers.ResponseValidatorTestHelper;
import stubidp.saml.test.OpenSAMLRunner;

import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.emptyIssuer;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.illegalIssuerFormat;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidStatusCode;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.invalidSubStatusCode;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingId;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingIssueInstant;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingIssuer;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingSignature;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.nestedSubStatusCodesBreached;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.nonSuccessHasUnEncryptedAssertions;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.signatureNotSigned;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.unexpectedNumberOfAssertions;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper.validateFail;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

class EncryptedResponseFromIdpValidatorTest extends OpenSAMLRunner {

    private EncryptedResponseFromIdpValidator<IdpIdaStatus.Status> validator;

    @BeforeEach
    void setup() {
        validator = new EncryptedResponseFromIdpValidator<>(new SamlStatusToIdaStatusCodeMapper());
    }

    @Test
    void validate_shouldThrowExceptionIfIdIsMissing() throws Exception {
        Response response = aResponse().withId(null).build();

        assertValidationFailure(response, missingId());
    }

    @Test
    void validate_shouldThrowExceptionIfIssuerElementIsMissing() throws Exception {
        Response response = aResponse().withIssuer(null).build();

        assertValidationFailure(response, missingIssuer());
    }

    @Test
    void validate_shouldThrowExceptionIfIssuerIdIsMissing() throws Exception {
        Issuer issuer = anIssuer().withIssuerId(null).build();
        Response response = aResponse().withIssuer(issuer).build();

        assertValidationFailure(response, emptyIssuer());
    }

    @Test
    void validate_shouldThrowExceptionIfIssueInstantIsMissing() throws Exception {
        String responseId = "test";
        Response response = aResponse().withId(responseId).withIssueInstant(null).build();

        assertValidationFailure(response, missingIssueInstant(responseId));
    }

    @Test
    void validateRequest_shouldNotErrorIfRequestIsSigned() throws Exception {
        Response response = ResponseValidatorTestHelper.getResponseBuilderWithTwoAssertions().build();

        validator.validate(response);
    }

    @Test
    void validateRequest_shouldThrowExceptionIfResponseDoesNotContainASignature() throws Exception {
        Response response = aResponse().withoutSignatureElement().build();

        assertValidationFailure(response, missingSignature());
    }

    @Test
    void validateRequest_shouldThrowExceptionIfResponseIsNotSigned() throws Exception {
        Response response = aResponse().withoutSigning().build();

        assertValidationFailure(response, signatureNotSigned());
    }

    @Test
    void validateIssuer_shouldThrowExceptionIfFormatAttributeHasInvalidValue() throws Exception {
        String invalidFormat = "goo";
        Issuer issuer = anIssuer().withFormat(invalidFormat).build();
        Response response = aResponse().withIssuer(issuer).build();

        assertValidationFailure(response, illegalIssuerFormat(
            invalidFormat,
            NameIDType.ENTITY
        ));
    }

    @Test
    void validateIssuer_shouldNotErrorIfFormatAttributeIsMissing() throws Exception {
        Issuer issuer = anIssuer().withFormat(null).build();
        Response response = ResponseValidatorTestHelper.getResponseBuilderWithTwoAssertions().withIssuer(issuer).build();

        validator.validate(response);
    }

    @Test
    void validateIssuer_shouldNotErrorIfFormatAttributeHasValidValue() throws Exception {
        Issuer issuer = anIssuer().withFormat(NameIDType.ENTITY).build();
        Response response = ResponseValidatorTestHelper.getResponseBuilderWithTwoAssertions().withIssuer(issuer).build();

        validator.validate(response);
    }

    @Test
    void validateResponse_shouldThrowExceptionIfResponseHasUnencryptedAssertion() throws Exception {
        Response response = aResponse().addAssertion(anAssertion().buildUnencrypted()).build();

        assertValidationFailure(response, SamlTransformationErrorFactory.unencryptedAssertion());
    }

    @Test
    void validateResponse_shouldThrowExceptionForSuccessResponsesWithNoAssertions() throws Exception {
        Response response = aResponse().withNoDefaultAssertion().build();

        assertValidationFailure(response, SamlTransformationErrorFactory.missingSuccessUnEncryptedAssertions());
    }

    @Test
    void validateResponse_shouldThrowExceptionForFailureResponsesWithAssertions() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.RESPONDER, ResponseValidatorTestHelper.createSubStatusCode(StatusCode.AUTHN_FAILED));
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, nonSuccessHasUnEncryptedAssertions());
    }

    @Test
    void validateResponse_shouldThrowExceptionIfThereIsNoInResponseToAttribute() throws Exception {
        Response response = aResponse().withInResponseTo(null).build();

        assertValidationFailure(response, SamlTransformationErrorFactory.missingInResponseTo());
    }

    @Test
    void validateStatus_shouldNotErrorIfStatusIsResponderWithSubStatusAuthnFailed() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.RESPONDER, ResponseValidatorTestHelper.createSubStatusCode(StatusCode.AUTHN_FAILED));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        validator.validate(response);
    }

    @Test
    void validateStatus_shouldNotErrorIfStatusIsResponderWithSubStatusNoAuthnContext() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.RESPONDER, ResponseValidatorTestHelper.createSubStatusCode(StatusCode.NO_AUTHN_CONTEXT));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        validator.validate(response);
    }

    @Test
    void validateStatus_shouldNotErrorIfStatusIsRequesterWithNoSubStatus() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.REQUESTER);
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        validator.validate(response);
    }

    @Test
    void validateStatus_shouldThrowExceptionIfStatusIsResponderWithNoSubStatus() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.RESPONDER);
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        assertValidationFailure(response, invalidStatusCode(StatusCode.RESPONDER));
    }

    @Test
    void validateStatus_shouldNotErrorIfStatusIsSuccessWithNoSubStatus() throws Exception {
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.SUCCESS);
        Response response = ResponseValidatorTestHelper.getResponseBuilderWithTwoAssertions().withStatus(status).build();

        validator.validate(response);
    }

    @Test
    void validateStatus_shouldThrowExceptionIfTheStatusIsInvalid() throws Exception {
        String anInvalidStatusCode = "This is wrong";
        Status status = ResponseValidatorTestHelper.createStatus(anInvalidStatusCode);
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, invalidStatusCode(anInvalidStatusCode));
    }

    @Test
    void validateStatus_shouldThrowExceptionIfSuccessHasASubStatus() throws Exception {
        StatusCode subStatusCode = ResponseValidatorTestHelper.createSubStatusCode();
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.SUCCESS, subStatusCode);
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, invalidSubStatusCode(subStatusCode.getValue(), StatusCode.SUCCESS));
    }

    @Test
    void validateStatus_shouldThrowExceptionIfRequesterHasASubStatus() throws Exception {
        StatusCode subStatusCode = ResponseValidatorTestHelper.createSubStatusCode();
        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.REQUESTER, subStatusCode);
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, invalidSubStatusCode(subStatusCode.getValue(), StatusCode.REQUESTER));
    }

    @Test
    void validateStatus_shouldThrowExceptionIfAuthnFailedHasASubSubStatus() throws Exception {
        StatusCode subStatusCode = aStatusCode()
            .withValue(StatusCode.AUTHN_FAILED)
            .withSubStatusCode(ResponseValidatorTestHelper.createSubStatusCode())
            .build();

        Status status = ResponseValidatorTestHelper.createStatus(StatusCode.RESPONDER, subStatusCode);
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, nestedSubStatusCodesBreached(1));
    }

    @Test
    void validate_shouldThrowIfResponseContainsTooManyAssertions() throws Exception {
        EncryptedAssertion assertion = anAssertion().build();
        Response response = ResponseValidatorTestHelper.getResponseBuilderWithTwoAssertions().addEncryptedAssertion(assertion).build();

        assertValidationFailure(response, unexpectedNumberOfAssertions(2, 3));
    }

    @Test
    void validate_shouldThrowIfResponseContainsTooFewAssertions() throws Exception {
        EncryptedAssertion assertion = anAssertion().build();
        Response response = aResponse().addEncryptedAssertion(assertion).build();

        assertValidationFailure(response, unexpectedNumberOfAssertions(2, 1));
    }

    private void assertValidationFailure(Response response, SamlValidationSpecificationFailure failure) {
        validateFail(() -> validator.validate(response), failure);
    }
}
