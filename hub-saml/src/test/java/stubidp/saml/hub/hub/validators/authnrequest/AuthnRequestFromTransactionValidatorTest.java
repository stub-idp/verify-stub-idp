package stubidp.saml.hub.hub.validators.authnrequest;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.DateTimeFreezer;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.hub.core.test.builders.NameIdPolicyBuilder;
import stubidp.saml.hub.core.test.builders.ScopingBuilder;
import stubidp.saml.hub.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;
import stubidp.saml.hub.hub.configuration.SamlDuplicateRequestValidationConfiguration;
import stubidp.saml.hub.hub.exception.SamlDuplicateRequestIdException;
import stubidp.saml.hub.hub.exception.SamlRequestTooOldException;
import stubidp.saml.security.validators.issuer.IssuerValidator;
import stubidp.saml.hub.core.OpenSAMLRunner;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.SAML_VERSION_NUMBER;
import static stubidp.saml.utils.core.test.AuthnRequestIdGenerator.generateRequestId;
import static stubidp.saml.utils.core.test.builders.AuthnRequestBuilder.anAuthnRequest;
import static stubidp.saml.utils.core.test.builders.IssuerBuilder.anIssuer;

public class AuthnRequestFromTransactionValidatorTest extends OpenSAMLRunner {

    private AuthnRequestFromTransactionValidator validator;

    @BeforeEach
    public void setup() {
        SamlDuplicateRequestValidationConfiguration samlDuplicateRequestValidationConfiguration = new SamlDuplicateRequestValidationConfiguration() {
            @Override
            public Duration getAuthnRequestIdExpirationDuration() {
                return Duration.hours(2);
            }
        };
        SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration = new SamlAuthnRequestValidityDurationConfiguration() {
            @Override
            public Duration getAuthnRequestValidityDuration() {
                return Duration.minutes(5);
            }
        };
        IdExpirationCache<AuthnRequestIdKey> idExpirationCache = new ConcurrentMapIdExpirationCache<>(new ConcurrentHashMap<>());
        validator = new AuthnRequestFromTransactionValidator(
                new IssuerValidator(),
                new DuplicateAuthnRequestValidator(idExpirationCache, samlDuplicateRequestValidationConfiguration),
                new AuthnRequestIssueInstantValidator(samlAuthnRequestValidityDurationConfiguration)
        );
    }

    @AfterEach
    public void unfreezeTime() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void validate_shouldThrowExceptionIfIdIsInvalid() throws Exception {
        validateThrows(
                anAuthnRequest().withId("6135ce2c-fe0d-413a-9d12-2ae1063153bd").build(),
                SamlTransformationErrorFactory.invalidRequestID()
        );

    }

    @Test
    public void validate_shouldDoNothingIfIdIsValid() throws Exception {
        validator.validate(anAuthnRequest().withId("a43qif88dsfv").build());

        validator.validate(anAuthnRequest().withId("_443qif88dsfv").build());
    }

    @Test
    public void validateRequest_shouldDoNothingIfRequestIsSigned() throws Exception {

        validator.validate(anAuthnRequest().build());
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfRequestDoesNotContainASignature() throws Exception {

        validateThrows(
                anAuthnRequest().withoutSignatureElement().build(),
                SamlTransformationErrorFactory.missingSignature()
        );
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfRequestIsNotSigned() throws Exception {
        validateThrows(
                anAuthnRequest().withoutSigning().build(),
                SamlTransformationErrorFactory.signatureNotSigned()
        );
    }

    @Test
    public void validateIssuer_shouldThrowExceptionIfFormatAttributeHasInvalidValue() throws Exception {
        String invalidFormat = "goo";

        validateThrows(
                anAuthnRequest().withIssuer(anIssuer().withFormat(invalidFormat).build()).build(),
                SamlTransformationErrorFactory.illegalIssuerFormat(invalidFormat, NameIDType.ENTITY)
        );
    }

    @Test
    public void validateIssuer_shouldDoNothingIfFormatAttributeIsMissing() throws Exception {
        validator.validate(anAuthnRequest().withIssuer(anIssuer().withFormat(null).build()).build());

    }

    @Test
    public void validateIssuer_shouldDoNothingIfFormatAttributeHasValidValue() throws Exception {
        validator.validate(anAuthnRequest().withIssuer(anIssuer().withFormat(NameIDType.ENTITY).build()).build());

    }

    @Test
    public void validateNameIdPolicy_shouldDoNothingIfNameIdPolicyIsMissing() throws Exception {
        validator.validate(anAuthnRequest().build());

    }

    @Test
    public void validateNameIdPolicy_shouldDoNothingIfNameIdPolicyFormatHasValidValue() throws Exception {
        validator.validate(anAuthnRequest().withNameIdPolicy(NameIdPolicyBuilder.aNameIdPolicy().withFormat(NameIDType.PERSISTENT).build()).build());

    }

    @Test
    public void validateRequest_shouldThrowExceptionIfScopingIsPresent() throws Exception {
        validateThrows(
                anAuthnRequest().withScoping(ScopingBuilder.aScoping().build()).build(),
                SamlTransformationErrorFactory.scopingNotAllowed()
        );
    }

    @Test
    public void validateProtocolBinding_shouldDoNothingIfProtocolBindingHasValidValue() throws Exception {
        validator.validate(anAuthnRequest().withProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI).build());

    }

    @Test
    public void validateProtocolBinding_shouldThrowExceptionIfProtocolBindingHasInvalidValue() throws Exception {
        String invalidValue = "goo";
        validateThrows(
                anAuthnRequest().withProtocolBinding(invalidValue).build(),
                SamlTransformationErrorFactory.illegalProtocolBindingError(invalidValue, SAMLConstants.SAML2_POST_BINDING_URI)
        );
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfIsPassiveIsPresent() throws Exception {
        validateThrows(
                anAuthnRequest().withIsPassive(true).build(),
                SamlTransformationErrorFactory.isPassiveNotAllowed()
        );
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfIsDuplicateRequestIdIsPresent() throws Exception {
        final String requestId = generateRequestId();
        final String oneIssuerId = "some-issuer-id";
        final String anotherIssuerId = "some-other-issuer-id";
        final AuthnRequest authnRequest = anAuthnRequest().withId(requestId).withIssuer(anIssuer().withIssuerId(oneIssuerId).build()).build();

        validator.validate(authnRequest);

        final AuthnRequest duplicateIdAuthnRequest = anAuthnRequest().withId(requestId).withIssuer(anIssuer().withIssuerId(anotherIssuerId).build()).build();

        final SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.duplicateRequestId(requestId, duplicateIdAuthnRequest.getIssuer().getValue());

        final SamlDuplicateRequestIdException e = Assertions.assertThrows(SamlDuplicateRequestIdException.class, () -> validator.validate(duplicateIdAuthnRequest));
        assertThat(e.getMessage()).isEqualTo(failure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(failure.getLogLevel());
    }

    @Test
    public void validateRequest_shouldThrowExceptionIfRequestIsTooOld() throws Exception {
        DateTimeFreezer.freezeTime();

        String requestId = generateRequestId();
        DateTime issueInstant = DateTime.now().minusMinutes(5).minusSeconds(1);

        final AuthnRequest authnRequest = anAuthnRequest().withId(requestId).withIssueInstant(issueInstant).build();

        SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.requestTooOld(requestId, issueInstant.withZone(DateTimeZone.UTC), DateTime.now());

        final SamlRequestTooOldException e = Assertions.assertThrows(SamlRequestTooOldException.class, () -> validator.validate(authnRequest));
        assertThat(e.getMessage()).isEqualTo(failure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(failure.getLogLevel());
    }

    @Test
    public void validate_shouldThrowExceptionIfIdIsMissing() throws Exception {
        validateThrows(
                anAuthnRequest().withId(null).build(),
                SamlTransformationErrorFactory.missingRequestId()
        );
    }

    @Test
    public void validate_shouldThrowExceptionIfIssuerElementIsMissing() throws Exception {
        validateThrows(
                anAuthnRequest().withIssuer(null).build(),
                SamlTransformationErrorFactory.missingIssuer()
        );
    }

    @Test
    public void validate_shouldThrowExceptionIfIssuerIdIsMissing() throws Exception {
        validateThrows(
                anAuthnRequest().withIssuer(anIssuer().withIssuerId(null).build()).build(),
                SamlTransformationErrorFactory.emptyIssuer()
        );
    }

    @Test
    public void validate_shouldThrowExceptionIfIssueInstantIsMissing() throws Exception {
        AuthnRequest authnRequest = anAuthnRequest().withIssueInstant(null).build();
        validateThrows(
                authnRequest,
                SamlTransformationErrorFactory.missingRequestIssueInstant(authnRequest.getID())
        );
    }

    @Test
    public void validate_shouldThrowExceptionIfVersionNumberIsMissing() throws Exception {
        AuthnRequest authnRequest = anAuthnRequest().withVersionNumber(null).build();

        validateThrows(
                authnRequest,
                SamlTransformationErrorFactory.missingRequestVersion(authnRequest.getID())
        );
    }

    @Test
    public void validate_shouldThrowExceptionIfVersionNumberIsNotTwoPointZero() throws Exception {
        validateThrows(
                anAuthnRequest().withVersionNumber("1.0").build(),
                SamlTransformationErrorFactory.illegalRequestVersionNumber()
        );
    }

    @Test
    public void validate_shouldDoNothingIfVersionNumberIsTwoPointZero() throws Exception {
        validator.validate(anAuthnRequest().withVersionNumber(SAML_VERSION_NUMBER).build());
    }

    private void validateThrows(AuthnRequest authnRequest, SamlValidationSpecificationFailure samlValidationSpecificationFailure) {
        final SamlTransformationErrorException e = Assertions.assertThrows(SamlTransformationErrorException.class, () -> validator.validate(authnRequest));
        assertThat(e.getMessage()).isEqualTo(samlValidationSpecificationFailure.getErrorMessage());
        assertThat(e.getLogLevel()).isEqualTo(samlValidationSpecificationFailure.getLogLevel());
    }
}
