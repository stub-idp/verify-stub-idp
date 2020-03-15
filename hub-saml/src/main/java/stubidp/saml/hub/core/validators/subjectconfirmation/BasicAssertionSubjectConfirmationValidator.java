package stubidp.saml.hub.core.validators.subjectconfirmation;

import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;

import java.time.Instant;

public class BasicAssertionSubjectConfirmationValidator {

    public void validate(SubjectConfirmation subjectConfirmation) {

        final SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();

        if (subjectConfirmationData == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingSubjectConfirmationData();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        if (subjectConfirmationData.getInResponseTo() == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingBearerInResponseTo();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        if (subjectConfirmationData.getRecipient() == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingBearerRecipient();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        final Instant notOnOrAfter = subjectConfirmationData.getNotOnOrAfter();
        if (notOnOrAfter == null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.missingNotOnOrAfter();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }

        final Instant now = Instant.now();
        if (notOnOrAfter.equals(now) || notOnOrAfter.isBefore(now)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.exceededNotOnOrAfter(notOnOrAfter);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
        if (subjectConfirmationData.getNotBefore() != null) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.notBeforeExists();
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}
